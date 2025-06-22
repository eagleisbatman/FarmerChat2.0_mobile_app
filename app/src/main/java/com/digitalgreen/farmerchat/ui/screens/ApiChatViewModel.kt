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
    
    private val _starterQuestions = MutableStateFlow<List<StarterQuestion>>(emptyList())
    val starterQuestions: StateFlow<List<String>> = _starterQuestions.map { questions ->
        questions.map { it.question }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    
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
    
    private val _currentMessage = MutableStateFlow("")
    val currentMessage: StateFlow<String> = _currentMessage
    
    // Voice recognition state
    val isRecording: StateFlow<Boolean> = speechRecognitionManager.isListening
    val isSpeaking: StateFlow<Boolean> = ttsManager.isSpeaking
    val speechError: StateFlow<String?> = speechRecognitionManager.error
    val recognizedText: StateFlow<String> = speechRecognitionManager.recognizedText
    val voiceConfidenceScore: StateFlow<Float> = speechRecognitionManager.confidenceScore
    val voiceConfidenceLevel: SpeechRecognitionManager.ConfidenceLevel 
        get() = speechRecognitionManager.getConfidenceLevel()
    
    // Starter questions loading state
    private val _starterQuestionsLoading = MutableStateFlow(false)
    val starterQuestionsLoading: StateFlow<Boolean> = _starterQuestionsLoading
    
    private val _starterQuestionsError = MutableStateFlow<String?>(null)
    val starterQuestionsError: StateFlow<String?> = _starterQuestionsError
    
    private var currentConversationId: String? = null
    private var isStreaming = false
    
    private var hasInitialized = false
    
    fun initializeChat(conversationId: String) {
        // Prevent multiple initializations
        if (hasInitialized && currentConversationId == conversationId) return
        
        currentConversationId = conversationId
        hasInitialized = true
        
        viewModelScope.launch {
            android.util.Log.d("ApiChatViewModel", "Initializing chat with conversationId: $conversationId")
            
            // Set loading state
            _isLoading.value = true
            
            try {
                // Ensure WebSocket is connected before loading chat
                repository.ensureWebSocketConnected()
                
                loadUserProfile()
                loadConversation(conversationId)
                val messages = loadMessages(conversationId)
                
                // Only load starter questions if the conversation has no messages
                if (messages.isEmpty()) {
                    android.util.Log.d("ApiChatViewModel", "No messages found, loading starter questions")
                    loadStarterQuestions()
                } else {
                    android.util.Log.d("ApiChatViewModel", "Found ${messages.size} existing messages, skipping starter questions")
                    _starterQuestions.value = emptyList()
                    _starterQuestionsLoading.value = false
                }
                
                // Join conversation for real-time updates
                repository.joinConversation(conversationId)
                
                // Listen for streaming events
                listenForStreamingEvents()
                
            } catch (e: Exception) {
                android.util.Log.e("ApiChatViewModel", "Error initializing chat", e)
                _error.value = "Failed to initialize chat: ${e.message}"
            } finally {
                // Clear loading state
                _isLoading.value = false
            }
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
            if (conversation != null) {
                _currentConversation.value = conversation.toConversation()
                android.util.Log.d("ApiChatViewModel", "Successfully loaded conversation: ${conversation.id} - ${conversation.title}")
            } else {
                android.util.Log.w("ApiChatViewModel", "Conversation with ID $conversationId not found in ${response.conversations.size} conversations")
                android.util.Log.d("ApiChatViewModel", "Available conversation IDs: ${response.conversations.map { it.id }}")
                _error.value = "Conversation not found"
            }
        }.onFailure { e ->
            android.util.Log.e("ApiChatViewModel", "Failed to load conversations list", e)
            _error.value = "Failed to load conversation: ${e.message}"
        }
    }
    
    private suspend fun loadMessages(conversationId: String): List<ChatMessage> {
        var loadedMessages = emptyList<ChatMessage>()
        repository.getMessages(conversationId).onSuccess { apiMessages ->
            val chatMessages = apiMessages.map { it.toChatMessage() }
            _messages.value = chatMessages
            loadedMessages = chatMessages
            
            android.util.Log.d("ApiChatViewModel", "Loaded ${chatMessages.size} messages for conversation $conversationId")
            
            // Load follow-up questions from the most recent AI message
            val lastAiMessage = chatMessages.findLast { !it.isUser }
            lastAiMessage?.let { aiMessage ->
                if (aiMessage.followUpQuestions.isNotEmpty()) {
                    _followUpQuestions.value = aiMessage.followUpQuestions
                    android.util.Log.d("ApiChatViewModel", "Loaded ${aiMessage.followUpQuestions.size} follow-up questions from last AI message")
                }
            }
        }.onFailure { e ->
            android.util.Log.e("ApiChatViewModel", "Failed to load messages for conversation $conversationId", e)
            _error.value = "Failed to load messages: ${e.message}"
        }
        return loadedMessages
    }
    
    private suspend fun loadStarterQuestions() {
        _starterQuestionsLoading.value = true
        _starterQuestionsError.value = null
        
        val profile = _userProfile.value
        repository.getStarterQuestions(
            crops = profile?.crops,
            livestock = profile?.livestock,
            language = profile?.language ?: "en"
        ).onSuccess { apiQuestions ->
            // Convert FollowUpQuestion to StarterQuestion
            _starterQuestions.value = apiQuestions.map { followUp ->
                StarterQuestion(
                    id = followUp.id,
                    question = followUp.question,
                    category = "General", // Default category since API doesn't provide it
                    language = profile?.language ?: "en"
                )
            }
            _starterQuestionsLoading.value = false
        }.onFailure { e ->
            android.util.Log.e("ApiChatViewModel", "Failed to load starter questions", e)
            _starterQuestionsError.value = "Failed to load starter questions"
            _starterQuestionsLoading.value = false
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
                            
                            // Add the completed message to the list instead of reloading
                            val completedMessage = ChatMessage(
                                id = UUID.randomUUID().toString(),
                                content = _currentStreamingMessage.value,
                                isUser = false,
                                timestamp = Date(),
                                followUpQuestions = event.followUpQuestions?.map { it.question } ?: emptyList(),
                                user = false // Ensure both fields are set
                            )
                            _messages.value = _messages.value + completedMessage
                            android.util.Log.d("ApiChatViewModel", "Added AI message with isUser=${completedMessage.isUser}")
                            
                            // Clear streaming message and update UI
                            _currentStreamingMessage.value = ""
                            _isLoading.value = false
                            _followUpQuestions.value = event.followUpQuestions?.map { it.question } ?: emptyList()
                            
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
        android.util.Log.d("ApiChatViewModel", "sendMessage called with: $message")
        android.util.Log.d("ApiChatViewModel", "currentConversationId: $currentConversationId")
        
        val conversationId = currentConversationId
        if (conversationId == null) {
            android.util.Log.e("ApiChatViewModel", "Cannot send message - no conversation ID")
            _error.value = "No active conversation"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // Add user message to the list immediately
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = message,
                isUser = true,
                timestamp = Date(),
                isVoiceMessage = false,
                user = true // Ensure both fields are set
            )
            _messages.value = _messages.value + userMessage
            android.util.Log.d("ApiChatViewModel", "Added user message with isUser=${userMessage.isUser}")
            
            android.util.Log.d("ApiChatViewModel", "Sending message via WebSocket streaming...")
            
            // Use WebSocket streaming for real-time responses
            isStreaming = true
            _currentStreamingMessage.value = ""
            repository.startStreamingMessage(message, conversationId)
            
            // The response will be handled by listenForStreamingEvents()
            // Comment out HTTP fallback for now:
            /*
            repository.sendMessage(message, conversationId).onSuccess { response ->
                // Add AI response to messages
                val aiMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    content = response.response,
                    isUser = false,
                    timestamp = Date(),
                    isVoiceMessage = false
                )
                _messages.value = _messages.value + aiMessage
                
                // Update follow-up questions
                _followUpQuestions.value = response.followUpQuestions.map { it.question }
                
                // Update conversation title if provided
                response.title?.let { newTitle ->
                    _currentConversation.value = _currentConversation.value?.copy(title = newTitle)
                }
                
                _isLoading.value = false
            }.onFailure { e ->
                android.util.Log.e("ApiChatViewModel", "Failed to send message", e)
                _error.value = "Failed to send message: ${e.message}"
                _isLoading.value = false
                // Remove the user message on failure
                _messages.value = _messages.value.filter { it.id != userMessage.id }
            }
            */
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
    
    fun submitFeedback(messageId: String, rating: Int, comment: String?) {
        viewModelScope.launch {
            repository.rateMessage(messageId, rating).onFailure { e ->
                android.util.Log.e("ApiChatViewModel", "Failed to submit feedback", e)
                _error.value = "Failed to submit feedback: ${e.message}"
            }
        }
    }
    
    // Voice recognition methods
    fun startRecording() {
        val profile = _userProfile.value
        speechRecognitionManager.startListening(
            languageCode = profile?.language ?: "en",
            onResult = { text ->
                _currentMessage.value = text
            }
        )
    }
    
    fun stopRecording() {
        speechRecognitionManager.stopListening()
    }
    
    fun clearRecognizedText() {
        speechRecognitionManager.clearRecognizedText()
    }
    
    fun clearSpeechError() {
        speechRecognitionManager.clearError()
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
        ttsManager.shutdown()
        speechRecognitionManager.destroy()
    }
}