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

class ApiChatViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as FarmerChatApplication
    private val repository = app.repository
    private val preferencesManager = PreferencesManager(application)
    private val stringProvider = StringProvider.create(application)
    private val ttsManager = TextToSpeechManager(application)
    private val speechRecognitionManager = SpeechRecognitionManager(application, stringProvider)
    
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
    val voiceConfidenceScore: StateFlow<Float> = speechRecognitionManager.confidenceScore
    val voiceConfidenceLevel: SpeechRecognitionManager.ConfidenceLevel 
        get() = speechRecognitionManager.getConfidenceLevel()
    
    private var currentConversationId: String? = null
    private var isStreaming = false
    
    fun initializeChat(conversationId: String) {
        currentConversationId = conversationId
        viewModelScope.launch {
            loadUserProfile()
            loadConversation(conversationId)
            loadMessages(conversationId)
            loadStarterQuestions()
            
            // Join conversation for real-time updates
            repository.joinConversation(conversationId)
            
            // Listen for streaming events
            listenForStreamingEvents()
        }
    }
    
    private suspend fun loadUserProfile() {
        repository.getUserProfile().onSuccess { apiUser ->
            _userProfile.value = apiUser.toUserProfile()
        }.onFailure { e ->
            android.util.Log.e("ApiChatViewModel", "Failed to load user profile", e)
            _error.value = "Failed to load user profile: ${e.message}"
        }
    }
    
    private suspend fun loadConversation(conversationId: String) {
        // Get conversation from conversations list since we don't have individual endpoint
        repository.getConversations().onSuccess { response ->
            val conversation = response.conversations.find { it.id == conversationId }
            _currentConversation.value = conversation?.toConversation()
        }.onFailure { e ->
            android.util.Log.e("ApiChatViewModel", "Failed to load conversation", e)
        }
    }
    
    private suspend fun loadMessages(conversationId: String) {
        repository.getMessages(conversationId).onSuccess { apiMessages ->
            _messages.value = apiMessages.map { it.toChatMessage() }
        }.onFailure { e ->
            android.util.Log.e("ApiChatViewModel", "Failed to load messages", e)
            _error.value = "Failed to load messages: ${e.message}"
        }
    }
    
    private suspend fun loadStarterQuestions() {
        val profile = _userProfile.value
        repository.getStarterQuestions(
            crops = profile?.crops,
            livestock = profile?.livestock,
            language = profile?.language ?: "en"
        ).onSuccess { apiQuestions ->
            _starterQuestions.value = apiQuestions.map { it.question }
        }.onFailure { e ->
            android.util.Log.e("ApiChatViewModel", "Failed to load starter questions", e)
        }
    }
    
    private fun listenForStreamingEvents() {
        viewModelScope.launch {
            repository.getChatEvents().collect { event ->
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
                            
                            // Update conversation title if provided
                            event.title?.let { newTitle ->
                                _currentConversation.value = _currentConversation.value?.copy(title = newTitle)
                            }
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
            
            // Use streaming for real-time response
            isStreaming = true
            _currentStreamingMessage.value = ""
            repository.startStreamingMessage(message, conversationId)
        }
    }
    
    fun stopStreaming() {
        if (isStreaming) {
            currentConversationId?.let { conversationId ->
                repository.stopStreaming(conversationId)
            }
            isStreaming = false
            _isLoading.value = false
            _currentStreamingMessage.value = ""
        }
    }
    
    fun rateMessage(messageId: String, rating: Int) {
        viewModelScope.launch {
            repository.rateMessage(messageId, rating).onFailure { e ->
                android.util.Log.e("ApiChatViewModel", "Failed to rate message", e)
                _error.value = "Failed to rate message: ${e.message}"
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
            repository.leaveConversation(conversationId)
        }
        ttsManager.cleanup()
        speechRecognitionManager.cleanup()
    }
}