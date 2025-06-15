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

class HybridConversationsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as FarmerChatApplication
    private val firebaseRepository = app.firebaseRepository
    private val apiRepository = app.apiRepository
    private val preferencesManager = PreferencesManager(application)
    private val stringProvider = StringProvider.create(application)
    
    // Current repository mode
    private val useApiRepository get() = MigrationHelper.shouldUseApi()
    
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
            
            if (useApiRepository) {
                // Listen for real-time conversation updates
                listenForConversationUpdates()
            }
        }
    }
    
    private suspend fun loadUserProfile() {
        if (useApiRepository) {
            apiRepository.getUserProfile().onSuccess { apiUser ->
                _userProfile.value = apiUser.toUserProfile()
            }.onFailure { e ->
                android.util.Log.e("HybridConversationsViewModel", "Failed to load user profile from API", e)
                _error.value = "Failed to load user profile: ${e.message}"
            }
        } else {
            firebaseRepository.getUserProfile().onSuccess { profile ->
                _userProfile.value = profile
            }.onFailure { e ->
                android.util.Log.e("HybridConversationsViewModel", "Failed to load user profile from Firebase", e)
                _error.value = "Failed to load user profile: ${e.message}"
            }
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
            
            if (useApiRepository) {
                loadConversationsFromApi()
            } else {
                loadConversationsFromFirebase()
            }
            
            _isLoading.value = false
            _isRefreshing.value = false
        }
    }
    
    private suspend fun loadConversationsFromApi() {
        val query = _searchQuery.value.takeIf { it.isNotEmpty() }
        
        apiRepository.getConversations(
            limit = 50,
            offset = 0,
            search = query
        ).onSuccess { response ->
            _conversations.value = response.conversations.map { it.toConversation() }
        }.onFailure { e ->
            android.util.Log.e("HybridConversationsViewModel", "Failed to load conversations from API", e)
            _error.value = "Failed to load conversations: ${e.message}"
        }
    }
    
    private suspend fun loadConversationsFromFirebase() {
        firebaseRepository.getConversations().collect { result ->
            result.onSuccess { conversations ->
                val filteredConversations = if (_searchQuery.value.isNotEmpty()) {
                    conversations.filter { conversation ->
                        conversation.title.contains(_searchQuery.value, ignoreCase = true) ||
                        conversation.lastMessage.contains(_searchQuery.value, ignoreCase = true)
                    }
                } else {
                    conversations
                }
                _conversations.value = filteredConversations
            }.onFailure { e ->
                android.util.Log.e("HybridConversationsViewModel", "Failed to load conversations from Firebase", e)
                _error.value = "Failed to load conversations: ${e.message}"
            }
        }
    }
    
    private fun listenForConversationUpdates() {
        if (!useApiRepository) return
        
        // API repository already maintains conversation state internally
        // We just need to observe changes and update our local state
        viewModelScope.launch {
            apiRepository.conversations.collect { apiConversations ->
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
            
            if (useApiRepository) {
                apiRepository.createConversation(title = placeholderTitle).onSuccess { conversation ->
                    onSuccess(conversation.id)
                    // Conversation will be added to list via real-time updates
                }.onFailure { e ->
                    android.util.Log.e("HybridConversationsViewModel", "Failed to create conversation", e)
                    _error.value = "Failed to create conversation: ${e.message}"
                }
            } else {
                firebaseRepository.createConversation(placeholderTitle).onSuccess { conversationId ->
                    onSuccess(conversationId)
                    // Conversation will be added to list via real-time listener
                }.onFailure { e ->
                    android.util.Log.e("HybridConversationsViewModel", "Failed to create conversation", e)
                    _error.value = "Failed to create conversation: ${e.message}"
                }
            }
            
            _isLoading.value = false
        }
    }
    
    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            _error.value = null
            
            if (useApiRepository) {
                apiRepository.deleteConversation(conversationId).onSuccess {
                    // Conversation will be removed from list via repository state
                }.onFailure { e ->
                    android.util.Log.e("HybridConversationsViewModel", "Failed to delete conversation", e)
                    _error.value = "Failed to delete conversation: ${e.message}"
                }
            } else {
                firebaseRepository.deleteConversation(conversationId).onSuccess {
                    // Conversation will be removed from list via real-time listener
                }.onFailure { e ->
                    android.util.Log.e("HybridConversationsViewModel", "Failed to delete conversation", e)
                    _error.value = "Failed to delete conversation: ${e.message}"
                }
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
            // Clear user profile and reset onboarding state
            if (useApiRepository) {
                // For API, we would need to delete the user profile or reset it
                // This might require a specific API endpoint
                android.util.Log.d("HybridConversationsViewModel", "Reset onboarding not implemented for API yet")
            } else {
                preferencesManager.clearOnboardingData()
                
                // Delete user profile from Firebase
                firebaseRepository.deleteUserProfile().onSuccess {
                    _userProfile.value = null
                }.onFailure { e ->
                    android.util.Log.e("HybridConversationsViewModel", "Failed to delete user profile", e)
                    _error.value = "Failed to reset onboarding: ${e.message}"
                }
            }
        }
    }
    
    fun exportData() {
        viewModelScope.launch {
            _error.value = null
            
            try {
                if (useApiRepository) {
                    // Export data from API
                    // This would require implementing export functionality in the API
                    android.util.Log.d("HybridConversationsViewModel", "Data export not implemented for API yet")
                    _error.value = "Data export not available for API mode"
                } else {
                    // Export data from Firebase
                    firebaseRepository.exportUserData().onSuccess { exportData ->
                        // Handle export data (save to file, share, etc.)
                        android.util.Log.d("HybridConversationsViewModel", "Data exported successfully")
                    }.onFailure { e ->
                        android.util.Log.e("HybridConversationsViewModel", "Failed to export data", e)
                        _error.value = "Failed to export data: ${e.message}"
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("HybridConversationsViewModel", "Error during data export", e)
                _error.value = "Error during data export: ${e.message}"
            }
        }
    }
    
    // Get placeholder text localized at display time, not storage time
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