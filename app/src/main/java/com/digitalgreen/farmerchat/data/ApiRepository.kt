package com.digitalgreen.farmerchat.data

import android.content.Context
import android.util.Log
import com.digitalgreen.farmerchat.network.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class ApiRepository(private val context: Context) {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val webSocketClient = ChatWebSocketClient()
    
    private val _currentUser = MutableStateFlow<ApiUser?>(null)
    val currentUser = _currentUser.asStateFlow()
    
    private val _conversations = MutableStateFlow<List<ApiConversation>>(emptyList())
    val conversations = _conversations.asStateFlow()
    
    companion object {
        private const val TAG = "ApiRepository"
    }
    
    init {
        NetworkConfig.initialize(context)
    }
    
    // Authentication
    suspend fun authenticateWithFirebase(): Result<AuthResponse> {
        return try {
            val idToken = firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
                ?: return Result.failure(Exception("No Firebase user or token"))
            
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
                    NetworkConfig.setAuthToken(response.data.token)
                    _currentUser.value = response.data.user
                    
                    // Connect WebSocket
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
    
    // User Profile
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
        responseLength: String? = null
    ): Result<ApiUser> {
        val request = UpdateUserRequest(
            name = name,
            location = location,
            language = language,
            crops = crops,
            livestock = livestock,
            responseLength = responseLength
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
    
    // Conversations
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
        val request = CreateConversationRequest(title = title, tags = tags)
        
        return safeApiCall { NetworkConfig.conversationApi.createConversation(request) }
            .mapCatching { response ->
                if (response.success && response.data != null) {
                    // Update local conversations list
                    _conversations.value = listOf(response.data) + _conversations.value
                    response.data
                } else {
                    throw Exception(response.error ?: "Failed to create conversation")
                }
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
    
    // Messages
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
                    response.data
                } else {
                    throw Exception(response.error ?: "Failed to send message")
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
    
    // Real-time chat streaming
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
    
    // Translations
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
                    response.data
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
    
    // Utility functions
    fun signOut() {
        NetworkConfig.setAuthToken(null)
        _currentUser.value = null
        _conversations.value = emptyList()
        webSocketClient.disconnect()
        firebaseAuth.signOut()
    }
    
    fun isAuthenticated(): Boolean = _currentUser.value != null
    
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            "1.0"
        }
    }
}

// Extension function for Firebase Task to Kotlin coroutines
suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T {
    return kotlinx.coroutines.tasks.await(this)
}