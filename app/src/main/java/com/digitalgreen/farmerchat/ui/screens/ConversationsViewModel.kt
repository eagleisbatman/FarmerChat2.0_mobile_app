package com.digitalgreen.farmerchat.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digitalgreen.farmerchat.data.Conversation
import com.digitalgreen.farmerchat.data.FarmerChatRepository
import com.digitalgreen.farmerchat.utils.PreferencesManager
import com.digitalgreen.farmerchat.utils.StringProvider
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class ConversationsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FarmerChatRepository()
    private val preferencesManager = PreferencesManager(application)
    private val stringProvider = StringProvider.create(application)
    
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    init {
        loadConversations()
    }
    
    private fun loadConversations() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getUserConversations().collect { conversationsList ->
                android.util.Log.d("ConversationsViewModel", "Received ${conversationsList.size} conversations")
                conversationsList.forEach { conv ->
                    android.util.Log.d("ConversationsViewModel", "Conv: ${conv.id} - ${conv.title} - userId: ${conv.userId}")
                }
                _conversations.value = conversationsList.sortedByDescending { it.lastMessageTime }
                _isLoading.value = false
            }
        }
    }
    
    fun createNewConversation(onConversationCreated: (String) -> Unit) {
        viewModelScope.launch {
            repository.createChatSession().onSuccess { sessionId ->
                android.util.Log.d("ConversationsViewModel", "Created new chat session: $sessionId")
                
                // Create an initial conversation entry so it appears in the list
                val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                // Use localized message
                val localizedLastMessage = stringProvider.getString(StringKey.START_A_CONVERSATION)
                
                val newConversation = Conversation(
                    id = sessionId,
                    userId = userId,
                    title = "New Chat",
                    lastMessage = localizedLastMessage,
                    lastMessageTime = Date(),
                    lastMessageIsUser = false,
                    hasUnreadMessages = false,
                    unreadCount = 0,
                    tags = emptyList(),
                    localizedTitles = mapOf(
                        "en" to "New Chat",
                        "hi" to "नई चैट",
                        "sw" to "Gumzo Jipya"
                    )
                )
                
                // Save the conversation to Firestore
                repository.saveConversation(newConversation)
                
                onConversationCreated(sessionId)
            }.onFailure { e ->
                android.util.Log.e("ConversationsViewModel", "Failed to create chat session", e)
            }
        }
    }
    
    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            repository.deleteConversation(conversationId)
        }
    }
    
}