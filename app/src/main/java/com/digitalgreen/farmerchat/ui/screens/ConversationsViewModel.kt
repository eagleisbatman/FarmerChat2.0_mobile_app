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
    private val repository = (application as com.digitalgreen.farmerchat.FarmerChatApplication).repository
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
            repository.getConversations().fold(
                onSuccess = { response ->
                    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    val conversationsList = response.conversations.map { apiConv ->
                        // Convert API conversation to local model
                        Conversation(
                            id = apiConv.id,
                            userId = currentUserId,
                            title = apiConv.title,
                            lastMessage = apiConv.lastMessage,
                            lastMessageTime = Date(), // API returns string, convert to Date
                            lastMessageIsUser = apiConv.lastMessageIsUser,
                            hasUnreadMessages = false,
                            unreadCount = 0,
                            tags = apiConv.tags,
                            localizedTitles = mapOf(
                                "en" to apiConv.title,
                                "hi" to "नई चैट",
                                "sw" to "Gumzo Jipya"
                            )
                        )
                    }
                    android.util.Log.d("ConversationsViewModel", "Received ${conversationsList.size} conversations")
                    conversationsList.forEach { conv ->
                        android.util.Log.d("ConversationsViewModel", "Conv: ${conv.id} - ${conv.title} - userId: ${conv.userId}")
                    }
                    _conversations.value = conversationsList.sortedByDescending { it.lastMessageTime }
                    _isLoading.value = false
                },
                onFailure = { error ->
                    android.util.Log.e("ConversationsViewModel", "Failed to load conversations", error)
                    _isLoading.value = false
                }
            )
        }
    }
    
    fun createNewConversation(onConversationCreated: (String) -> Unit) {
        viewModelScope.launch {
            repository.createConversation(title = "New Chat").fold(
                onSuccess = { apiConversation ->
                    android.util.Log.d("ConversationsViewModel", "Created new conversation: ${apiConversation.id}")
                    
                    // Add to local list immediately
                    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    val localizedLastMessage = stringProvider.getString(StringKey.START_A_CONVERSATION)
                    val newConversation = Conversation(
                        id = apiConversation.id,
                        userId = currentUserId,
                        title = apiConversation.title,
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
                    
                    _conversations.value = listOf(newConversation) + _conversations.value
                    
                    onConversationCreated(apiConversation.id)
                },
                onFailure = { e ->
                    android.util.Log.e("ConversationsViewModel", "Failed to create conversation", e)
                }
            )
        }
    }
    
    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            repository.deleteConversation(conversationId)
        }
    }
    
}