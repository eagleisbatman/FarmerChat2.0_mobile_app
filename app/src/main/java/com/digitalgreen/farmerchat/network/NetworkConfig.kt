package com.digitalgreen.farmerchat.network

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
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
    
    // TODO: Update with your backend URL
    private const val BASE_URL = "http://10.0.2.2:3000/api/v1/" // Android emulator localhost
    // For physical device, use your computer's IP: "http://192.168.1.XXX:3000/api/v1/"
    
    private var authToken: String? = null
    private lateinit var applicationContext: Context
    
    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }
    
    fun setAuthToken(token: String?) {
        authToken = token
        Log.d(TAG, "Auth token updated: ${if (token != null) "Set" else "Cleared"}")
    }
    
    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder().apply {
            authToken?.let { token ->
                addHeader("Authorization", "Bearer $token")
            }
            addHeader("Content-Type", "application/json")
            addHeader("Accept", "application/json")
            
            // Add device info headers
            try {
                val packageInfo = applicationContext.packageManager.getPackageInfo(
                    applicationContext.packageName, 0
                )
                addHeader("X-App-Version", packageInfo.versionName)
                addHeader("X-Platform", "android")
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e(TAG, "Error getting app version", e)
            }
        }.build()
        
        chain.proceed(request)
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
            Result.failure(Exception("HTTP ${response.code()}: $errorMsg"))
        }
    } catch (e: Exception) {
        Log.e("API_EXCEPTION", "API call failed", e)
        Result.failure(e)
    }
}