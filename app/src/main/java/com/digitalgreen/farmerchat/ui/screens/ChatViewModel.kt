package com.digitalgreen.farmerchat.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digitalgreen.farmerchat.BuildConfig
import com.digitalgreen.farmerchat.data.*
import com.digitalgreen.farmerchat.utils.PreferencesManager
import com.digitalgreen.farmerchat.utils.SpeechRecognitionManager
import com.digitalgreen.farmerchat.utils.TextToSpeechManager
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FarmerChatRepository()
    private val preferencesManager = PreferencesManager(application)
    private val ttsManager = TextToSpeechManager(application)
    private val speechRecognitionManager = SpeechRecognitionManager(application)
    
    // Generative AI model
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY // TODO: Add your API key in local.properties
    )
    
    init {
        if (BuildConfig.GEMINI_API_KEY.isEmpty()) {
            android.util.Log.e("ChatViewModel", "GEMINI_API_KEY is empty! Please add it to local.properties")
        } else {
            android.util.Log.d("ChatViewModel", "GEMINI_API_KEY is configured")
        }
    }
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages
    
    private val _starterQuestions = MutableStateFlow<List<StarterQuestion>>(emptyList())
    val starterQuestions: StateFlow<List<StarterQuestion>> = _starterQuestions
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    val isRecording: StateFlow<Boolean> = speechRecognitionManager.isListening
    val isSpeaking: StateFlow<Boolean> = ttsManager.isSpeaking
    val speechError: StateFlow<String?> = speechRecognitionManager.error
    
    private val _followUpQuestions = MutableStateFlow<List<String>>(emptyList())
    val followUpQuestions: StateFlow<List<String>> = _followUpQuestions
    
    private val _currentStreamingMessage = MutableStateFlow<String>("")
    val currentStreamingMessage: StateFlow<String> = _currentStreamingMessage
    
    private val _conversationTitles = MutableStateFlow<Map<String, String>>(emptyMap())
    val conversationTitles: StateFlow<Map<String, String>> = _conversationTitles
    
    private var currentSessionId: String? = null
    private var userProfile: UserProfile? = null
    private var streamingMessageId: String? = null
    
    fun initializeChat(conversationId: String) {
        android.util.Log.d("ChatViewModel", "initializeChat called with conversationId: $conversationId")
        currentSessionId = conversationId
        viewModelScope.launch {
            // Load user profile
            repository.getUserProfile().onSuccess { profile ->
                userProfile = profile
                android.util.Log.d("ChatViewModel", "User profile loaded: userId=${profile?.userId}")
                
                // Load starter questions based on user preferences
                if (profile != null) {
                    repository.getStarterQuestions(
                        language = profile.language,
                        crops = profile.crops,
                        livestock = profile.livestock
                    ).onSuccess { questions ->
                        _starterQuestions.value = questions
                    }
                }
                
                // Listen to messages for this session
                repository.getChatMessages(conversationId).collect { messages ->
                    android.util.Log.d("ChatViewModel", "Messages collected: ${messages.size} messages for conversation $conversationId")
                    _messages.value = messages
                    
                    // Update conversation metadata when messages change
                    if (messages.isNotEmpty()) {
                        android.util.Log.d("ChatViewModel", "Calling updateConversationMetadata for $conversationId with ${messages.size} messages")
                        updateConversationMetadata(conversationId, messages)
                    }
                }
            }
        }
    }
    
    fun sendMessage(content: String) {
        viewModelScope.launch {
            val sessionId = currentSessionId ?: return@launch
            
            // Add user message
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = content,
                isUser = true,
                timestamp = Date(),
                audioUrl = null,
                isVoiceMessage = false,
                user = true,
                voiceMessage = false
            )
            repository.saveMessage(sessionId, userMessage)
            
            // Generate AI response
            _isLoading.value = true
            _followUpQuestions.value = emptyList()
            _currentStreamingMessage.value = ""
            
            // Create placeholder AI message for streaming
            streamingMessageId = UUID.randomUUID().toString()
            val streamingMessage = ChatMessage(
                id = streamingMessageId!!,
                content = "",
                isUser = false,
                timestamp = Date(),
                audioUrl = null,
                isVoiceMessage = false,
                user = false,
                voiceMessage = false
            )
            
            try {
                val prompt = buildPrompt(content)
                
                // Use streaming API
                val response = generativeModel.generateContentStream(
                    content { text(prompt) }
                )
                
                var fullResponse = ""
                
                response.collect { chunk ->
                    chunk.text?.let { text ->
                        fullResponse += text
                        _currentStreamingMessage.value = fullResponse
                        
                        // Update the message list with streaming content
                        val currentMessages = _messages.value.toMutableList()
                        val messageIndex = currentMessages.indexOfFirst { it.id == streamingMessageId }
                        
                        if (messageIndex == -1) {
                            currentMessages.add(streamingMessage.copy(content = fullResponse))
                        } else {
                            currentMessages[messageIndex] = streamingMessage.copy(content = fullResponse)
                        }
                        _messages.value = currentMessages
                    }
                }
                
                // Parse final response for follow-up questions
                val (mainResponse, questions) = parseAIResponse(fullResponse)
                
                // Save final message to Firestore
                val finalMessage = ChatMessage(
                    id = streamingMessageId!!,
                    content = mainResponse,
                    isUser = false,
                    timestamp = Date(),
                    audioUrl = null,
                    isVoiceMessage = false,
                    user = false,
                    voiceMessage = false
                )
                repository.saveMessage(sessionId, finalMessage)
                
                // Update follow-up questions
                _followUpQuestions.value = questions
                
            } catch (e: Exception) {
                // Handle error
                android.util.Log.e("ChatViewModel", "Error generating AI response", e)
                val errorMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    content = "I apologize, but I'm having trouble responding right now. Please try again.",
                    isUser = false,
                    timestamp = Date(),
                    audioUrl = null,
                    isVoiceMessage = false,
                    user = false,
                    voiceMessage = false
                )
                repository.saveMessage(sessionId, errorMessage)
            } finally {
                _isLoading.value = false
                _currentStreamingMessage.value = ""
                streamingMessageId = null
            }
        }
    }
    
    private fun buildPrompt(userQuery: String): String {
        val profile = userProfile
        return """
            You are an AI assistant helping smallholder farmers with agricultural advice.
            
            User Profile:
            - Language preference: ${profile?.language ?: "en"}
            - Location: ${profile?.location ?: "Unknown"}
            - Crops: ${profile?.crops?.joinToString(", ") ?: "None specified"}
            - Livestock: ${profile?.livestock?.joinToString(", ") ?: "None specified"}
            
            User Query: $userQuery
            
            Please provide:
            1. A helpful, practical response tailored to their context
            2. Keep the response concise and easy to understand
            3. If relevant, mention local conditions or practices
            
            IMPORTANT: You MUST end your response with exactly this format:
            
            FOLLOW_UP_QUESTIONS:
            Question 1|Question 2|Question 3
            
            The follow-up questions line MUST start with "FOLLOW_UP_QUESTIONS:" and have 2-3 questions separated by "|"
        """.trimIndent()
    }
    
    private fun parseAIResponse(response: String): Pair<String, List<String>> {
        val parts = response.split("FOLLOW_UP_QUESTIONS:")
        val mainResponse = parts[0].trim()
        val questions = if (parts.size > 1) {
            parts[1].trim().split("|").map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            emptyList()
        }
        return mainResponse to questions
    }
    
    fun startRecording() {
        val language = userProfile?.language ?: "en"
        speechRecognitionManager.startListening(language) { recognizedText ->
            if (recognizedText.isNotBlank()) {
                sendMessage(recognizedText)
            }
        }
    }
    
    fun stopRecording() {
        speechRecognitionManager.stopListening()
    }
    
    fun speakMessage(text: String) {
        ttsManager.speak(text)
    }
    
    fun stopSpeaking() {
        ttsManager.stop()
    }
    
    fun clearSpeechError() {
        speechRecognitionManager.clearError()
    }
    
    fun submitFeedback(messageId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            val feedback = Feedback(
                id = UUID.randomUUID().toString(),
                sessionId = currentSessionId ?: "",
                messageId = messageId,
                userId = userProfile?.userId ?: "",
                rating = rating,
                comment = comment
            )
            repository.saveFeedback(feedback)
        }
    }
    
    private fun updateConversationMetadata(conversationId: String, messages: List<ChatMessage>) {
        if (messages.isNotEmpty()) {
            viewModelScope.launch {
                val lastMessage = messages.last()
                val userMessages = messages.filter { it.isUser }
                val aiMessages = messages.filter { !it.isUser }
                
                // Check if we already have a generated title
                val existingTitle = _conversationTitles.value[conversationId]
                val shouldGenerateTitle = existingTitle == null || existingTitle == "New Conversation"
                
                var title = existingTitle ?: "New Conversation"
                
                // Generate intelligent title after first AI response
                if (shouldGenerateTitle && userMessages.isNotEmpty() && aiMessages.isNotEmpty()) {
                    try {
                        val firstUserQuery = userMessages.first().content
                        val firstAiResponse = aiMessages.first().content.take(200) // Use first 200 chars of AI response
                        
                        val titlePrompt = """
                            Based on this farming conversation, generate a concise title (2-4 words) that captures the main topic:
                            
                            User Query: $firstUserQuery
                            AI Response (excerpt): $firstAiResponse
                            
                            Generate ONLY the title, nothing else. Examples:
                            - Rice Pest Control
                            - Organic Fertilizer Guide
                            - Tomato Disease Management
                            - Irrigation Schedule Help
                            - Wheat Harvest Timing
                            
                            Title:
                        """.trimIndent()
                        
                        val response = generativeModel.generateContent(
                            content { text(titlePrompt) }
                        )
                        
                        val generatedTitle = response.text?.trim()?.take(50) ?: ""
                        if (generatedTitle.isNotEmpty() && generatedTitle.length < 50) {
                            title = generatedTitle
                        } else {
                            // Fallback to keyword-based title if generation fails
                            title = generateFallbackTitle(firstUserQuery)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ChatViewModel", "Failed to generate title with AI", e)
                        // Use fallback title generation
                        title = generateFallbackTitle(userMessages.first().content)
                    }
                }
                
                // Update conversation title in title map
                _conversationTitles.value = _conversationTitles.value + (conversationId to title)
                
                // Get current user ID from auth instead of profile
                val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                
                android.util.Log.d("ChatViewModel", "Updating conversation: id=$conversationId, title=$title, userId=$userId")
                
                val conversation = Conversation(
                    id = conversationId,
                    title = title,
                    lastMessage = lastMessage.content.take(100),
                    lastMessageTime = lastMessage.timestamp,
                    lastMessageIsUser = lastMessage.isUser,
                    userId = userId,
                    createdAt = Date() // Add creation date
                )
                
                repository.updateConversationMetadata(conversation)
                    .onSuccess {
                        android.util.Log.d("ChatViewModel", "Successfully updated conversation metadata")
                    }
                    .onFailure { e ->
                        android.util.Log.e("ChatViewModel", "Failed to update conversation metadata", e)
                    }
            }
        }
    }
    
    private fun generateFallbackTitle(firstQuery: String): String {
        // Enhanced keyword-based title generation
        return when {
            firstQuery.contains("pest", ignoreCase = true) && firstQuery.contains("control", ignoreCase = true) -> "Pest Control Advice"
            firstQuery.contains("pest", ignoreCase = true) -> "Pest Management"
            firstQuery.contains("fertilizer", ignoreCase = true) || firstQuery.contains("fertiliser", ignoreCase = true) -> "Fertilizer Guidance"
            firstQuery.contains("disease", ignoreCase = true) && firstQuery.contains("plant", ignoreCase = true) -> "Plant Disease Help"
            firstQuery.contains("disease", ignoreCase = true) -> "Disease Management"
            firstQuery.contains("crop", ignoreCase = true) && firstQuery.contains("rotation", ignoreCase = true) -> "Crop Rotation Plan"
            firstQuery.contains("crop", ignoreCase = true) -> "Crop Management"
            firstQuery.contains("weather", ignoreCase = true) -> "Weather Advisory"
            firstQuery.contains("harvest", ignoreCase = true) -> "Harvest Guidance"
            firstQuery.contains("soil", ignoreCase = true) && firstQuery.contains("test", ignoreCase = true) -> "Soil Testing Guide"
            firstQuery.contains("soil", ignoreCase = true) -> "Soil Management"
            firstQuery.contains("irrigation", ignoreCase = true) || firstQuery.contains("water", ignoreCase = true) -> "Irrigation Support"
            firstQuery.contains("seed", ignoreCase = true) && firstQuery.contains("select", ignoreCase = true) -> "Seed Selection Help"
            firstQuery.contains("seed", ignoreCase = true) -> "Seed Guidance"
            firstQuery.contains("organic", ignoreCase = true) -> "Organic Farming"
            firstQuery.contains("market", ignoreCase = true) && firstQuery.contains("price", ignoreCase = true) -> "Market Prices"
            firstQuery.contains("storage", ignoreCase = true) -> "Storage Advice"
            firstQuery.contains("livestock", ignoreCase = true) -> "Livestock Care"
            firstQuery.contains("cattle", ignoreCase = true) || firstQuery.contains("cow", ignoreCase = true) -> "Cattle Management"
            firstQuery.contains("poultry", ignoreCase = true) || firstQuery.contains("chicken", ignoreCase = true) -> "Poultry Farming"
            firstQuery.contains("goat", ignoreCase = true) || firstQuery.contains("sheep", ignoreCase = true) -> "Small Ruminants"
            firstQuery.length > 30 -> firstQuery.take(27) + "..."
            else -> firstQuery.take(30)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
        speechRecognitionManager.destroy()
    }
}