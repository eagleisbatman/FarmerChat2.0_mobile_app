package com.digitalgreen.farmerchat.data

import android.content.Context
import android.util.Log
import com.digitalgreen.farmerchat.network.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * Main repository that uses the API backend directly
 * Simplified for new development without Firebase migration complexity
 */
class AppRepository(private val context: Context) {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val webSocketClient = ChatWebSocketClient()
    
    private val _currentUser = MutableStateFlow<ApiUser?>(null)
    val currentUser = _currentUser.asStateFlow()
    
    private val _conversations = MutableStateFlow<List<ApiConversation>>(emptyList())
    val conversations = _conversations.asStateFlow()
    
    companion object {
        private const val TAG = "AppRepository"
    }
    
    init {
        NetworkConfig.initialize(context)
    }
    
    // Device-based Authentication (NEW - replaces Firebase)
    suspend fun authenticateWithDevice(deviceId: String): Result<AuthResponse> {
        return try {
            Log.d(TAG, "Authenticating with device ID: $deviceId")
            
            val deviceInfo = DeviceInfo(
                deviceId = deviceId,
                appVersion = getAppVersion(),
                fcmToken = null // TODO: Get FCM token when implementing push notifications
            )
            
            val request = DeviceAuthRequest(deviceId = deviceId, deviceInfo = deviceInfo)
            val result = safeApiCall { NetworkConfig.authApi.authenticateDevice(request) }
            
            result.onSuccess { response ->
                if (response.success && response.data != null) {
                    // IMPORTANT: Set token in memory IMMEDIATELY for subsequent requests
                    NetworkConfig.setAuthToken(response.data.token)
                    Log.d(TAG, "JWT token set in memory: ${response.data.token.take(20)}...")
                    
                    // Then save to persistent storage
                    NetworkConfig.setAuthTokens(
                        jwtToken = response.data.token,
                        refreshToken = response.data.refreshToken ?: response.data.token, // Use same token if no refresh token
                        expiresIn = response.data.expiresIn.toLong()
                    )
                    Log.d(TAG, "JWT token saved to persistent storage")
                    _currentUser.value = response.data.user
                    
                    // Connect WebSocket for real-time features
                    webSocketClient.connect(response.data.token)
                    
                    return Result.success(response.data)
                } else {
                    return Result.failure(Exception(response.error ?: "Authentication failed"))
                }
            }
            
            result.mapCatching { it.data!! }
        } catch (e: Exception) {
            Log.e(TAG, "Device authentication error", e)
            Result.failure(e)
        }
    }
    
    // Login with phone and PIN
    suspend fun loginWithPhone(phone: String, pin: String): Result<AuthResponse> {
        return try {
            Log.d(TAG, "Login attempt for phone: $phone")
            
            val result = safeApiCall { 
                NetworkConfig.authApi.loginWithPhone(
                    mapOf(
                        "phone" to phone,
                        "pin" to pin
                    )
                )
            }
            
            result.onSuccess { response ->
                if (response.success && response.data != null) {
                    // Set token in memory immediately
                    NetworkConfig.setAuthToken(response.data.token)
                    Log.d(TAG, "JWT token set after login: ${response.data.token.take(20)}...")
                    
                    // Save to persistent storage
                    NetworkConfig.setAuthTokens(
                        jwtToken = response.data.token,
                        refreshToken = response.data.refreshToken ?: response.data.token,
                        expiresIn = response.data.expiresIn.toLong()
                    )
                    
                    _currentUser.value = response.data.user
                    
                    // Connect WebSocket
                    webSocketClient.connect(response.data.token)
                    
                    return Result.success(response.data)
                } else {
                    return Result.failure(Exception(response.error ?: "Login failed"))
                }
            }
            
            result.mapCatching { it.data!! }
        } catch (e: Exception) {
            Log.e(TAG, "Login error", e)
            Result.failure(e)
        }
    }
    
    // Register with phone and PIN
    suspend fun registerWithPhone(phone: String, pin: String): Result<AuthResponse> {
        return try {
            Log.d(TAG, "Registration attempt for phone: $phone")
            
            val result = safeApiCall { 
                NetworkConfig.authApi.registerWithPhone(
                    mapOf(
                        "phone" to phone,
                        "pin" to pin
                    )
                )
            }
            
            result.onSuccess { response ->
                if (response.success && response.data != null) {
                    // Set token in memory immediately
                    NetworkConfig.setAuthToken(response.data.token)
                    Log.d(TAG, "JWT token set after registration: ${response.data.token.take(20)}...")
                    
                    // Save to persistent storage
                    NetworkConfig.setAuthTokens(
                        jwtToken = response.data.token,
                        refreshToken = response.data.refreshToken ?: response.data.token,
                        expiresIn = response.data.expiresIn.toLong()
                    )
                    
                    _currentUser.value = response.data.user
                    
                    // Connect WebSocket
                    webSocketClient.connect(response.data.token)
                    
                    return Result.success(response.data)
                } else {
                    return Result.failure(Exception(response.error ?: "Registration failed"))
                }
            }
            
            result.mapCatching { it.data!! }
        } catch (e: Exception) {
            Log.e(TAG, "Registration error", e)
            Result.failure(e)
        }
    }
    
    // Legacy Firebase Authentication (to be removed)
    suspend fun authenticateWithFirebase(): Result<AuthResponse> {
        return try {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                return Result.failure(Exception("No Firebase user authenticated"))
            }
            
            // Force token refresh to ensure we have a valid token
            val idToken = firebaseUser.getIdToken(true).await().token
                ?: return Result.failure(Exception("Failed to get Firebase ID token"))
            
            val deviceInfo = DeviceInfo(
                deviceId = android.provider.Settings.Secure.getString(
                    context.contentResolver,
                    android.provider.Settings.Secure.ANDROID_ID
                ),
                appVersion = getAppVersion(),
                fcmToken = null // TODO: Get FCM token when implementing push notifications
            )
            
            val request = AuthRequest(idToken = idToken, deviceInfo = deviceInfo)
            val result = safeApiCall { NetworkConfig.authApi.verifyFirebaseToken(request) }
            
            result.onSuccess { response ->
                if (response.success && response.data != null) {
                    // IMPORTANT: Set token in memory IMMEDIATELY for subsequent requests
                    NetworkConfig.setAuthToken(response.data.token)
                    Log.d(TAG, "JWT token set in memory: ${response.data.token.take(20)}...")
                    
                    // Then save to persistent storage
                    NetworkConfig.setAuthTokens(
                        jwtToken = response.data.token,
                        refreshToken = response.data.refreshToken ?: response.data.token, // Use same token if no refresh token
                        expiresIn = response.data.expiresIn.toLong()
                    )
                    Log.d(TAG, "JWT token saved to persistent storage")
                    _currentUser.value = response.data.user
                    
                    // Connect WebSocket for real-time features
                    webSocketClient.connect(response.data.token)
                    
                    return Result.success(response.data)
                } else {
                    return Result.failure(Exception(response.error ?: "Authentication failed"))
                }
            }
            
            result.mapCatching { it.data!! }
        } catch (e: Exception) {
            Log.e(TAG, "Authentication error", e)
            Result.failure(e)
        }
    }
    
    // WebSocket Management
    suspend fun ensureWebSocketConnected() {
        if (!webSocketClient.isConnected()) {
            val token = NetworkConfig.getAuthToken()
            if (token != null) {
                Log.d(TAG, "Reconnecting WebSocket with existing token")
                webSocketClient.connect(token)
            } else {
                Log.w(TAG, "No auth token available for WebSocket connection")
            }
        }
    }
    
    fun disconnectWebSocket() {
        webSocketClient.disconnect()
    }
    
    // User Profile Management
    suspend fun getUserProfile(): Result<ApiUser> {
        return safeApiCall { NetworkConfig.userApi.getUserProfile() }
            .mapCatching { response ->
                if (response.success && response.data != null) {
                    _currentUser.value = response.data
                    response.data
                } else {
                    throw Exception(response.error ?: "Failed to get user profile")
                }
            }
    }
    
    suspend fun updateUserProfile(
        name: String? = null,
        location: String? = null,
        language: String? = null,
        crops: List<String>? = null,
        livestock: List<String>? = null,
        role: String? = null,
        gender: String? = null,
        responseLength: String? = null,
        phone: String? = null
    ): Result<ApiUser> {
        val request = UpdateUserRequest(
            name = name,
            location = location,
            language = language,
            crops = crops,
            livestock = livestock,
            role = role,
            gender = gender,
            responseLength = responseLength,
            phone = phone
        )
        
        return safeApiCall { NetworkConfig.userApi.updateUserProfile(request) }
            .mapCatching { response ->
                if (response.success && response.data != null) {
                    _currentUser.value = response.data
                    response.data
                } else {
                    throw Exception(response.error ?: "Failed to update user profile")
                }
            }
    }
    
    // Conversation Management
    suspend fun getConversations(
        limit: Int = 20,
        offset: Int = 0,
        search: String? = null
    ): Result<ConversationsResponse> {
        return safeApiCall { 
            NetworkConfig.conversationApi.getConversations(limit, offset, search) 
        }.mapCatching { response ->
            if (response.success && response.data != null) {
                _conversations.value = response.data.conversations
                response.data
            } else {
                throw Exception(response.error ?: "Failed to get conversations")
            }
        }
    }
    
    suspend fun createConversation(title: String? = null, tags: List<String> = emptyList()): Result<ApiConversation> {
        Log.d(TAG, "AppRepository: Creating conversation with title: $title")
        val token = NetworkConfig.getAuthToken()
        Log.d(TAG, "AppRepository: Current auth token: ${if (token != null) "Present" else "NULL"}")
        
        val request = CreateConversationRequest(title = title, tags = tags)
        
        return safeApiCall { NetworkConfig.conversationApi.createConversation(request) }
            .mapCatching { response ->
                Log.d(TAG, "AppRepository: Create conversation response: success=${response.success}, data=${response.data}")
                if (response.success && response.data != null) {
                    // Update local conversations list
                    _conversations.value = listOf(response.data) + _conversations.value
                    response.data
                } else {
                    throw Exception(response.error ?: "Failed to create conversation")
                }
            }
            .onFailure { e ->
                Log.e(TAG, "AppRepository: Failed to create conversation", e)
            }
    }
    
    suspend fun deleteConversation(conversationId: String): Result<Unit> {
        return safeApiCall { NetworkConfig.conversationApi.deleteConversation(conversationId) }
            .mapCatching { response ->
                if (response.success) {
                    // Remove from local list
                    _conversations.value = _conversations.value.filter { it.id != conversationId }
                } else {
                    throw Exception(response.error ?: "Failed to delete conversation")
                }
            }
    }
    
    // Message Management
    suspend fun getMessages(conversationId: String, limit: Int = 50, offset: Int = 0): Result<List<ApiMessage>> {
        return safeApiCall { 
            NetworkConfig.chatApi.getMessages(conversationId, limit, offset) 
        }.mapCatching { response ->
            if (response.success && response.data != null) {
                response.data
            } else {
                throw Exception(response.error ?: "Failed to get messages")
            }
        }
    }
    
    suspend fun sendMessage(message: String, conversationId: String): Result<SendMessageResponse> {
        val request = SendMessageRequest(message = message, conversationId = conversationId)
        
        return safeApiCall { NetworkConfig.chatApi.sendMessage(request) }
            .mapCatching { response ->
                if (response.success && response.data != null) {
                    // Update the conversation in the list with the new message
                    updateConversationLastMessage(conversationId, message, true)
                    response.data
                } else {
                    throw Exception(response.error ?: "Failed to send message")
                }
            }
    }
    
    fun updateConversationLastMessage(conversationId: String, message: String, isUser: Boolean) {
        _conversations.value = _conversations.value.map { conversation ->
            if (conversation.id == conversationId) {
                conversation.copy(
                    lastMessage = message,
                    lastMessageTime = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                        timeZone = java.util.TimeZone.getTimeZone("UTC")
                    }.format(java.util.Date()),
                    lastMessageIsUser = isUser
                )
            } else {
                conversation
            }
        }
    }
    
    suspend fun rateMessage(messageId: String, rating: Int, feedback: String? = null): Result<Unit> {
        val request = RateMessageRequest(rating = rating, feedback = feedback)
        
        return safeApiCall { NetworkConfig.chatApi.rateMessage(messageId, request) }
            .mapCatching { response ->
                if (!response.success) {
                    throw Exception(response.error ?: "Failed to rate message")
                }
            }
    }
    
    // Real-time Chat Streaming
    fun startStreamingMessage(message: String, conversationId: String) {
        webSocketClient.sendStreamingMessage(message, conversationId)
    }
    
    fun stopStreaming(conversationId: String) {
        webSocketClient.stopGeneration(conversationId)
    }
    
    fun getChatEvents(): Flow<ChatStreamEvent> = webSocketClient.chatEvents
    
    fun joinConversation(conversationId: String) {
        webSocketClient.joinConversation(conversationId)
    }
    
    fun leaveConversation(conversationId: String) {
        webSocketClient.leaveConversation(conversationId)
    }
    
    // Starter Questions
    suspend fun getStarterQuestions(
        crops: List<String>? = null,
        livestock: List<String>? = null,
        language: String = "en"
    ): Result<List<FollowUpQuestion>> {
        val request = StarterQuestionsRequest(crops = crops, livestock = livestock, language = language)
        
        return safeApiCall { NetworkConfig.chatApi.getStarterQuestions(request) }
            .mapCatching { response ->
                if (response.success && response.data != null) {
                    response.data
                } else {
                    throw Exception(response.error ?: "Failed to get starter questions")
                }
            }
    }
    
    // Generate follow-up questions
    suspend fun generateFollowUpQuestions(
        message: String,
        language: String = "en"
    ): Result<List<FollowUpQuestion>> {
        val request = GenerateFollowUpRequest(message = message, language = language)
        
        return safeApiCall { NetworkConfig.chatApi.generateFollowUpQuestions(request) }
            .mapCatching { response ->
                if (response.success && response.data != null) {
                    response.data
                } else {
                    throw Exception(response.error ?: "Failed to generate follow-up questions")
                }
            }
    }
    
    // Translation Management
    suspend fun getTranslations(languageCode: String): Result<TranslationBundle> {
        return safeApiCall { NetworkConfig.translationApi.getTranslations(languageCode) }
            .mapCatching { response ->
                if (response.success && response.data != null) {
                    response.data
                } else {
                    throw Exception(response.error ?: "Failed to get translations")
                }
            }
    }
    
    suspend fun getSupportedLanguages(): Result<List<Language>> {
        return safeApiCall { NetworkConfig.translationApi.getSupportedLanguages() }
            .mapCatching { response ->
                if (response.success && response.data != null) {
                    response.data.map { it.toDataLanguage() }
                } else {
                    throw Exception(response.error ?: "Failed to get supported languages")
                }
            }
    }
    
    suspend fun getCropTranslations(languageCode: String, search: String? = null): Result<Map<String, CropTranslation>> {
        return safeApiCall { 
            NetworkConfig.translationApi.getCropTranslations(languageCode, search) 
        }.mapCatching { response ->
            if (response.success && response.data != null) {
                response.data
            } else {
                throw Exception(response.error ?: "Failed to get crop translations")
            }
        }
    }
    
    suspend fun getLivestockTranslations(languageCode: String, search: String? = null): Result<Map<String, LivestockTranslation>> {
        return safeApiCall { 
            NetworkConfig.translationApi.getLivestockTranslations(languageCode, search) 
        }.mapCatching { response ->
            if (response.success && response.data != null) {
                response.data
            } else {
                throw Exception(response.error ?: "Failed to get livestock translations")
            }
        }
    }
    
    // Authentication Management
    suspend fun signOut() {
        NetworkConfig.clearAuthTokens() // This clears both in-memory and persistent tokens
        _currentUser.value = null
        _conversations.value = emptyList()
        webSocketClient.disconnect()
        firebaseAuth.signOut()
    }
    
    fun isAuthenticated(): Boolean = _currentUser.value != null
    
    fun getCurrentUserId(): String? = _currentUser.value?.id
    
    // Audio transcription
    suspend fun transcribeAudio(audioFile: File, language: String = "en"): Result<String> {
        return safeApiCall {
            // Create request body for audio file
            val audioRequestBody = audioFile.asRequestBody("audio/m4a".toMediaTypeOrNull())
            val audioPart = MultipartBody.Part.createFormData("audio", audioFile.name, audioRequestBody)
            
            // Create request body for language
            val languageRequestBody = language.toRequestBody("text/plain".toMediaType())
            
            NetworkConfig.chatApi.transcribeAudio(audioPart, languageRequestBody)
        }.mapCatching { response ->
            if (response.success && response.data != null) {
                response.data.transcription
            } else {
                throw Exception(response.error ?: "Failed to transcribe audio")
            }
        }
    }
    
    // Utility Functions
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }
}