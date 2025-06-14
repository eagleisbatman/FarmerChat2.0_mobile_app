package com.digitalgreen.farmerchat.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digitalgreen.farmerchat.data.Conversation
import com.digitalgreen.farmerchat.data.FarmerChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class ConversationsViewModel : ViewModel() {
    private val repository = FarmerChatRepository()
    
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
                // Don't save conversation to list yet - wait for first message
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