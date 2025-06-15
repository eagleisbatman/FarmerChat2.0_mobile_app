package com.digitalgreen.farmerchat.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digitalgreen.farmerchat.FarmerChatApplication
import com.digitalgreen.farmerchat.data.*
import com.digitalgreen.farmerchat.network.*
import com.digitalgreen.farmerchat.utils.PreferencesManager
import com.digitalgreen.farmerchat.utils.SpeechRecognitionManager
import com.digitalgreen.farmerchat.utils.TextToSpeechManager
import com.digitalgreen.farmerchat.utils.StringProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class HybridChatViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as FarmerChatApplication
    private val firebaseRepository = app.firebaseRepository
    private val apiRepository = app.apiRepository
    private val preferencesManager = PreferencesManager(application)
    private val stringProvider = StringProvider.create(application)
    private val ttsManager = TextToSpeechManager(application)
    private val speechRecognitionManager = SpeechRecognitionManager(application, stringProvider)
    
    // Current repository mode
    private val useApiRepository get() = MigrationHelper.shouldUseApi()
    
    // UI State
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages
    
    private val _starterQuestions = MutableStateFlow<List<String>>(emptyList())
    val starterQuestions: StateFlow<List<String>> = _starterQuestions
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _followUpQuestions = MutableStateFlow<List<String>>(emptyList())
    val followUpQuestions: StateFlow<List<String>> = _followUpQuestions
    
    private val _currentStreamingMessage = MutableStateFlow<String>("")
    val currentStreamingMessage: StateFlow<String> = _currentStreamingMessage
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile
    
    private val _currentConversation = MutableStateFlow<Conversation?>(null)
    val currentConversation: StateFlow<Conversation?> = _currentConversation
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    // Voice recognition state
    val isRecording: StateFlow<Boolean> = speechRecognitionManager.isListening
    val isSpeaking: StateFlow<Boolean> = ttsManager.isSpeaking
    val speechError: StateFlow<String?> = speechRecognitionManager.error
    val recognizedText: StateFlow<String> = speechRecognitionManager.recognizedText
    
    private var currentConversationId: String? = null
    private var isStreaming = false
    
    fun initializeChat(conversationId: String) {
        currentConversationId = conversationId
        viewModelScope.launch {
            loadUserProfile()
            loadConversation(conversationId)
            loadMessages(conversationId)
            loadStarterQuestions()
            
            if (useApiRepository) {
                // Join conversation for real-time updates
                apiRepository.joinConversation(conversationId)
                // Listen for streaming events
                listenForStreamingEvents()
            }
        }
    }
    
    private suspend fun loadUserProfile() {
        if (useApiRepository) {
            apiRepository.getUserProfile().onSuccess { apiUser ->
                _userProfile.value = apiUser.toUserProfile()
            }.onFailure { e ->
                android.util.Log.e("HybridChatViewModel", "Failed to load user profile from API", e)
                _error.value = "Failed to load user profile: ${e.message}"
            }
        } else {
            firebaseRepository.getUserProfile().onSuccess { profile ->
                _userProfile.value = profile
            }.onFailure { e ->
                android.util.Log.e("HybridChatViewModel", "Failed to load user profile from Firebase", e)
                _error.value = "Failed to load user profile: ${e.message}"
            }
        }
    }
    
    private suspend fun loadConversation(conversationId: String) {
        if (useApiRepository) {
            // For API, we might need to get conversation from the conversations list
            // since individual conversation endpoint might not exist
            apiRepository.getConversations().onSuccess { response ->
                val conversation = response.conversations.find { it.id == conversationId }
                _currentConversation.value = conversation?.toConversation()
            }.onFailure { e ->
                android.util.Log.e("HybridChatViewModel", "Failed to load conversation from API", e)
            }
        } else {
            firebaseRepository.getConversation(conversationId).onSuccess { conversation ->
                _currentConversation.value = conversation
            }.onFailure { e ->
                android.util.Log.e("HybridChatViewModel", "Failed to load conversation from Firebase", e)
            }
        }
    }
    
    private suspend fun loadMessages(conversationId: String) {
        if (useApiRepository) {
            apiRepository.getMessages(conversationId).onSuccess { apiMessages ->
                _messages.value = apiMessages.map { it.toChatMessage() }
            }.onFailure { e ->
                android.util.Log.e("HybridChatViewModel", "Failed to load messages from API", e)
                _error.value = "Failed to load messages: ${e.message}"
            }
        } else {
            firebaseRepository.getConversationMessages(conversationId).collect { result ->
                result.onSuccess { messages ->
                    _messages.value = messages
                }.onFailure { e ->
                    android.util.Log.e("HybridChatViewModel", "Failed to load messages from Firebase", e)
                    _error.value = "Failed to load messages: ${e.message}"
                }
            }
        }
    }
    
    private suspend fun loadStarterQuestions() {
        val profile = _userProfile.value
        if (useApiRepository) {
            apiRepository.getStarterQuestions(
                crops = profile?.crops,
                livestock = profile?.livestock,
                language = profile?.language ?: "en"
            ).onSuccess { apiQuestions ->
                _starterQuestions.value = apiQuestions.map { it.question }
            }.onFailure { e ->
                android.util.Log.e("HybridChatViewModel", "Failed to load starter questions from API", e)
            }
        } else {
            firebaseRepository.getStarterQuestions(profile?.language ?: "en").onSuccess { questions ->
                _starterQuestions.value = questions.map { it.question }
            }.onFailure { e ->
                android.util.Log.e("HybridChatViewModel", "Failed to load starter questions from Firebase", e)
            }
        }
    }
    
    private fun listenForStreamingEvents() {
        if (!useApiRepository) return
        
        viewModelScope.launch {
            apiRepository.getChatEvents().collect { event ->
                when (event.type) {
                    "chunk" -> {
                        if (isStreaming) {
                            _currentStreamingMessage.value += event.content ?: ""
                        }
                    }
                    "complete" -> {
                        if (isStreaming) {
                            isStreaming = false
                            _isLoading.value = false
                            _currentStreamingMessage.value = ""
                            _followUpQuestions.value = event.followUpQuestions?.map { it.question } ?: emptyList()
                            
                            // Reload messages to get the complete conversation
                            currentConversationId?.let { loadMessages(it) }
                        }
                    }
                    "error" -> {
                        isStreaming = false
                        _isLoading.value = false
                        _currentStreamingMessage.value = ""
                        _error.value = event.error
                    }
                    "typing" -> {
                        // Handle typing indicators if needed
                    }
                }
            }
        }
    }
    
    fun sendMessage(message: String) {
        val conversationId = currentConversationId ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            if (useApiRepository) {
                // Use streaming for API
                isStreaming = true
                _currentStreamingMessage.value = ""
                apiRepository.startStreamingMessage(message, conversationId)
            } else {
                // Use traditional send for Firebase
                firebaseRepository.sendMessage(conversationId, message).onSuccess { response ->
                    _followUpQuestions.value = response.followUpQuestions
                    // Messages are updated via real-time listener
                }.onFailure { e ->
                    android.util.Log.e("HybridChatViewModel", "Failed to send message", e)
                    _error.value = "Failed to send message: ${e.message}"
                }
                _isLoading.value = false
            }
        }
    }
    
    fun stopStreaming() {
        if (useApiRepository && isStreaming) {
            currentConversationId?.let { conversationId ->
                apiRepository.stopStreaming(conversationId)
            }
            isStreaming = false
            _isLoading.value = false
            _currentStreamingMessage.value = ""
        }
    }
    
    fun rateMessage(messageId: String, rating: Int) {
        viewModelScope.launch {
            if (useApiRepository) {
                apiRepository.rateMessage(messageId, rating).onFailure { e ->
                    android.util.Log.e("HybridChatViewModel", "Failed to rate message", e)
                    _error.value = "Failed to rate message: ${e.message}"
                }
            } else {
                // Firebase rating logic would go here
                // firebaseRepository.rateMessage(messageId, rating)
            }
        }
    }
    
    // Voice recognition methods
    fun startVoiceRecognition() {
        val profile = _userProfile.value
        speechRecognitionManager.startListening(profile?.language ?: "en")
    }
    
    fun stopVoiceRecognition() {
        speechRecognitionManager.stopListening()
    }
    
    fun speakMessage(message: String) {
        val profile = _userProfile.value
        ttsManager.speak(message, profile?.language ?: "en")
    }
    
    fun stopSpeaking() {
        ttsManager.stop()
    }
    
    fun clearError() {
        _error.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        currentConversationId?.let { conversationId ->
            if (useApiRepository) {
                apiRepository.leaveConversation(conversationId)
            }
        }
        ttsManager.cleanup()
        speechRecognitionManager.cleanup()
    }
}

// Extension function to access the hybrid ViewModel
fun Application.getHybridChatViewModel(): HybridChatViewModel {
    return HybridChatViewModel(this)
}