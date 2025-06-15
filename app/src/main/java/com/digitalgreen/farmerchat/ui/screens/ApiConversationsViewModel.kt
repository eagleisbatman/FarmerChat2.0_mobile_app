package com.digitalgreen.farmerchat.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digitalgreen.farmerchat.FarmerChatApplication
import com.digitalgreen.farmerchat.data.*
import com.digitalgreen.farmerchat.network.*
import com.digitalgreen.farmerchat.utils.PreferencesManager
import com.digitalgreen.farmerchat.utils.StringProvider
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ApiConversationsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as FarmerChatApplication
    private val repository = app.repository
    private val preferencesManager = PreferencesManager(application)
    private val stringProvider = StringProvider.create(application)
    
    // UI State
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing
    
    init {
        viewModelScope.launch {
            loadUserProfile()
            loadConversations()
            
            // Listen for real-time conversation updates
            listenForConversationUpdates()
        }
    }
    
    private suspend fun loadUserProfile() {
        repository.getUserProfile().onSuccess { apiUser ->
            _userProfile.value = apiUser.toUserProfile()
        }.onFailure { e ->
            android.util.Log.e("ApiConversationsViewModel", "Failed to load user profile", e)
            _error.value = "Failed to load user profile: ${e.message}"
        }
    }
    
    fun loadConversations(refresh: Boolean = false) {
        viewModelScope.launch {
            if (refresh) {
                _isRefreshing.value = true
            } else {
                _isLoading.value = true
            }
            
            _error.value = null
            
            val query = _searchQuery.value.takeIf { it.isNotEmpty() }
            
            repository.getConversations(
                limit = 50,
                offset = 0,
                search = query
            ).onSuccess { response ->
                _conversations.value = response.conversations.map { it.toConversation() }
            }.onFailure { e ->
                android.util.Log.e("ApiConversationsViewModel", "Failed to load conversations", e)
                _error.value = "Failed to load conversations: ${e.message}"
            }
            
            _isLoading.value = false
            _isRefreshing.value = false
        }
    }
    
    private fun listenForConversationUpdates() {
        // Repository already maintains conversation state internally
        // We observe changes and update our local state
        viewModelScope.launch {
            repository.conversations.collect { apiConversations ->
                if (apiConversations.isNotEmpty()) {
                    _conversations.value = apiConversations.map { it.toConversation() }
                }
            }
        }
    }
    
    fun createNewConversation(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val placeholderTitle = stringProvider.getString(StringKey.START_A_CONVERSATION)
            
            repository.createConversation(title = placeholderTitle).onSuccess { conversation ->
                onSuccess(conversation.id)
                // Conversation will be added to list via real-time updates
            }.onFailure { e ->
                android.util.Log.e("ApiConversationsViewModel", "Failed to create conversation", e)
                _error.value = "Failed to create conversation: ${e.message}"
            }
            
            _isLoading.value = false
        }
    }
    
    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            _error.value = null
            
            repository.deleteConversation(conversationId).onSuccess {
                // Conversation will be removed from list via repository state
            }.onFailure { e ->
                android.util.Log.e("ApiConversationsViewModel", "Failed to delete conversation", e)
                _error.value = "Failed to delete conversation: ${e.message}"
            }
        }
    }
    
    fun searchConversations(query: String) {
        _searchQuery.value = query
        // Trigger a new search
        loadConversations()
    }
    
    fun clearSearch() {
        _searchQuery.value = ""
        loadConversations()
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun refresh() {
        loadConversations(refresh = true)
    }
    
    fun resetOnboarding() {
        viewModelScope.launch {
            // Clear local preferences
            preferencesManager.clearOnboardingData()
            
            // Sign out and clear profile
            repository.signOut()
            _userProfile.value = null
            _conversations.value = emptyList()
        }
    }
    
    fun exportData() {
        viewModelScope.launch {
            _error.value = null
            
            try {
                // For now, just show that data export is not implemented
                android.util.Log.d("ApiConversationsViewModel", "Data export would be implemented here")
                _error.value = "Data export feature coming soon"
            } catch (e: Exception) {
                android.util.Log.e("ApiConversationsViewModel", "Error during data export", e)
                _error.value = "Error during data export: ${e.message}"
            }
        }
    }
    
    // Get placeholder text localized at display time
    fun getLocalizedPlaceholder(): String {
        return stringProvider.getString(StringKey.START_A_CONVERSATION)
    }
    
    // Check if a conversation has placeholder text in any language
    fun isPlaceholderText(message: String): Boolean {
        val commonPlaceholders = listOf(
            "Start a conversation...",
            "बातचीत शुरू करें...",
            "Anza mazungumzo...",
            stringProvider.getString(StringKey.START_A_CONVERSATION)
        )
        return message in commonPlaceholders
    }
}