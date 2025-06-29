package com.digitalgreen.farmerchat.network

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.digitalgreen.farmerchat.utils.PreferencesManager
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkConfig {
    private const val TAG = "NetworkConfig"
    
    private const val BASE_URL = "http://10.0.2.2:3004/api/v1/" // Android emulator localhost
    // For physical device, use your computer's IP: "http://192.168.1.XXX:3004/api/v1/"
    
    private var authToken: String? = null
    private lateinit var applicationContext: Context
    private lateinit var preferencesManager: PreferencesManager
    
    fun initialize(context: Context) {
        applicationContext = context.applicationContext
        preferencesManager = PreferencesManager(context)
        
        Log.d(TAG, "NetworkConfig initializing...")
        // Load stored token on initialization
        loadStoredToken()
        Log.d(TAG, "NetworkConfig initialized with token: ${authToken?.take(20) ?: "null"}")
    }
    
    private fun loadStoredToken() {
        try {
            val storedToken = preferencesManager.getJwtToken()
            if (storedToken != null && !preferencesManager.isTokenExpired()) {
                authToken = storedToken
                Log.d(TAG, "Loaded valid token from storage")
            } else if (storedToken != null) {
                Log.d(TAG, "Stored token is expired, will clear asynchronously")
                authToken = null
                // Clear tokens asynchronously to avoid blocking
                // Note: We can't use coroutines here, so we'll just mark the token as null
                // The actual cleanup will happen elsewhere
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading stored token", e)
            authToken = null
        }
    }
    
    suspend fun ensureTokenLoaded() {
        if (authToken == null && ::preferencesManager.isInitialized) {
            val storedToken = preferencesManager.getJwtToken()
            if (storedToken != null && !preferencesManager.isTokenExpired()) {
                authToken = storedToken
                Log.d(TAG, "Async loaded valid token from storage")
            }
        }
    }
    
    fun setAuthToken(token: String?) {
        authToken = token
        Log.d(TAG, "Auth token updated: ${if (token != null) "Set" else "Cleared"}")
    }
    
    suspend fun setAuthTokens(jwtToken: String, refreshToken: String, expiresIn: Long) {
        authToken = jwtToken
        Log.d(TAG, "Set auth token in memory: ${jwtToken.take(20)}...")
        // Save tokens to persistent storage
        preferencesManager.saveAuthTokens(jwtToken, refreshToken, expiresIn)
        Log.d(TAG, "Auth tokens saved to storage")
    }
    
    suspend fun clearAuthTokens() {
        authToken = null
        preferencesManager.clearAuthTokens()
        Log.d(TAG, "Auth tokens cleared from storage")
    }
    
    fun getAuthToken(): String? = authToken
    
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
        
        Log.d(TAG, "=== AUTH INTERCEPTOR DEBUG ===")
        Log.d(TAG, "Request URL: ${originalRequest.url}")
        
        // Check if this is an auth endpoint that doesn't require authentication
        val isAuthEndpoint = originalRequest.url.encodedPath.contains("/auth/") || 
                           originalRequest.url.encodedPath.contains("/auth/verify") ||
                           originalRequest.url.encodedPath.contains("/auth/config")
        
        var currentToken: String? = null
        
        if (!isAuthEndpoint) {
            // Simple approach: use in-memory token first, then check storage as fallback
            currentToken = authToken ?: run {
                if (::preferencesManager.isInitialized) {
                    // Only use runBlocking as a last resort in interceptor
                    runBlocking {
                        ensureTokenLoaded()
                    }
                    authToken
                } else null
            }
            
            Log.d(TAG, "Token to use: ${currentToken?.take(20) ?: "null"}...")
            
            // Add headers
            currentToken?.let { token ->
                requestBuilder.addHeader("Authorization", "Bearer $token")
                Log.d(TAG, "✅ Added Authorization header")
            } ?: run {
                Log.w(TAG, "❌ No auth token available - request will fail")
            }
        } else {
            Log.d(TAG, "✅ Auth endpoint - skipping token requirement")
        }
        
        requestBuilder.addHeader("Content-Type", "application/json")
        requestBuilder.addHeader("Accept", "application/json")
        
        // Add device info headers
        if (::applicationContext.isInitialized) {
            try {
                val packageInfo = applicationContext.packageManager.getPackageInfo(
                    applicationContext.packageName, 0
                )
                requestBuilder.addHeader("X-App-Version", packageInfo.versionName ?: "1.0")
                requestBuilder.addHeader("X-Platform", "android")
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e(TAG, "Error getting app version", e)
            }
        } else {
            requestBuilder.addHeader("X-Platform", "android")
            requestBuilder.addHeader("X-App-Version", "1.0")
        }
        
        val request = requestBuilder.build()
        val response = chain.proceed(request)
        
        // Handle 401 Unauthorized - token might be expired
        if (response.code == 401 && currentToken != null) {
            Log.w(TAG, "Got 401 response, token might be expired")
            response.close()
            
            // Clear the expired token
            runBlocking {
                clearAuthTokens()
            }
            
            // Return the 401 response - let the app handle re-authentication
            // In a production app, you might want to trigger automatic re-authentication here
            response
        } else {
            response
        }
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("HTTP", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    // API Service instances
    val authApi: AuthApiService = retrofit.create(AuthApiService::class.java)
    val userApi: UserApiService = retrofit.create(UserApiService::class.java)
    val conversationApi: ConversationApiService = retrofit.create(ConversationApiService::class.java)
    val chatApi: ChatApiService = retrofit.create(ChatApiService::class.java)
    val translationApi: TranslationApiService = retrofit.create(TranslationApiService::class.java)
}

// Extension functions for easier error handling
suspend inline fun <T> safeApiCall(
    crossinline apiCall: suspend () -> retrofit2.Response<T>
): Result<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) }
                ?: Result.failure(Exception("Response body is null"))
        } else {
            val errorMsg = response.errorBody()?.string() ?: "Unknown error"
            Log.e("API_ERROR", "HTTP ${response.code()}: $errorMsg")
            
            // Parse error message from backend
            val actualErrorMessage = try {
                // Try to parse JSON error response
                val errorJson = org.json.JSONObject(errorMsg)
                errorJson.optString("message", errorMsg)
            } catch (e: Exception) {
                errorMsg
            }
            
            // Special handling for 401 errors
            if (response.code() == 401) {
                Result.failure(UnauthorizedException(actualErrorMessage))
            } else {
                Result.failure(Exception("HTTP ${response.code()}: $actualErrorMessage"))
            }
        }
    } catch (e: Exception) {
        Log.e("API_EXCEPTION", "API call failed", e)
        Result.failure(e)
    }
}

// Custom exception for unauthorized access
class UnauthorizedException(message: String) : Exception(message)