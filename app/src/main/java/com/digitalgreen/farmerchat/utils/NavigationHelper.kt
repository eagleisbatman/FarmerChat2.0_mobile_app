package com.digitalgreen.farmerchat.utils

import com.digitalgreen.farmerchat.data.AppRepository
import com.digitalgreen.farmerchat.network.ApiConversation
import kotlinx.coroutines.flow.first
import android.util.Log

/**
 * Helper class for smart navigation decisions based on user's conversation history
 */
object NavigationHelper {
    
    data class NavigationDecision(
        val shouldGoToChat: Boolean,
        val conversationId: String? = null,
        val isFirstTime: Boolean = false
    )
    
    /**
     * Determines where to navigate the user after onboarding completion
     * 
     * Logic:
     * - If user has conversations with messages → Navigate to most recent chat
     * - If user has empty conversations → Navigate to conversations list  
     * - If user has no conversations → Create new conversation and navigate to chat
     */
    suspend fun determinePostOnboardingNavigation(
        repository: AppRepository,
        preferencesManager: PreferencesManager
    ): NavigationDecision {
        try {
            Log.d("NavigationHelper", "Determining post-onboarding navigation...")
            
            // Check if this is truly the first time by looking at preferences
            val isFirstRun = !preferencesManager.hasCompletedOnboarding.first()
            Log.d("NavigationHelper", "Is first run: $isFirstRun")
            
            // Get user's conversations
            val conversationsResult = repository.getConversations(limit = 10, offset = 0)
            
            conversationsResult.fold(
                onSuccess = { response ->
                    val conversations = response.conversations
                    Log.d("NavigationHelper", "Found ${conversations.size} conversations")
                    
                    when {
                        conversations.isEmpty() -> {
                            // No conversations - create one and go to chat
                            Log.d("NavigationHelper", "No conversations found, will create new one")
                            return createNewConversationAndNavigate(repository, isFirstTime = true)
                        }
                        
                        hasConversationsWithMessages(conversations) -> {
                            // Has conversations with messages - go to most recent chat
                            val recentConversation = conversations
                                .filter { !it.lastMessage.isNullOrEmpty() && it.lastMessage != "Start a conversation..." }
                                .maxByOrNull { it.createdAt ?: "" }
                            
                            if (recentConversation != null) {
                                Log.d("NavigationHelper", "Found conversation with messages: ${recentConversation.id}")
                                return NavigationDecision(
                                    shouldGoToChat = true,
                                    conversationId = recentConversation.id,
                                    isFirstTime = false
                                )
                            } else {
                                // Fallback to conversations list
                                Log.d("NavigationHelper", "No conversations with real messages, going to conversations list")
                                return NavigationDecision(shouldGoToChat = false)
                            }
                        }
                        
                        else -> {
                            // Has conversations but no messages - go to conversations list
                            Log.d("NavigationHelper", "Has conversations but no messages, going to conversations list")
                            return NavigationDecision(shouldGoToChat = false)
                        }
                    }
                },
                onFailure = { error ->
                    Log.e("NavigationHelper", "Failed to get conversations", error)
                    // On error, create new conversation for first-time users
                    if (isFirstRun) {
                        return createNewConversationAndNavigate(repository, isFirstTime = true)
                    } else {
                        return NavigationDecision(shouldGoToChat = false)
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("NavigationHelper", "Error in navigation determination", e)
            return NavigationDecision(shouldGoToChat = false)
        }
    }
    
    private suspend fun createNewConversationAndNavigate(
        repository: AppRepository,
        isFirstTime: Boolean
    ): NavigationDecision {
        return try {
            Log.d("NavigationHelper", "Creating new conversation for navigation...")
            
            val result = repository.createConversation(title = "Start a conversation...")
            result.fold(
                onSuccess = { conversation ->
                    Log.d("NavigationHelper", "Successfully created conversation: ${conversation.id}")
                    NavigationDecision(
                        shouldGoToChat = true,
                        conversationId = conversation.id,
                        isFirstTime = isFirstTime
                    )
                },
                onFailure = { error ->
                    Log.e("NavigationHelper", "Failed to create conversation", error)
                    // Fallback to conversations screen
                    NavigationDecision(shouldGoToChat = false, isFirstTime = isFirstTime)
                }
            )
        } catch (e: Exception) {
            Log.e("NavigationHelper", "Exception creating conversation", e)
            NavigationDecision(shouldGoToChat = false, isFirstTime = isFirstTime)
        }
    }
    
    private fun hasConversationsWithMessages(conversations: List<ApiConversation>): Boolean {
        return conversations.any { conversation ->
            !conversation.lastMessage.isNullOrEmpty() && 
            !isPlaceholderMessage(conversation.lastMessage)
        }
    }
    
    private fun isPlaceholderMessage(message: String?): Boolean {
        if (message.isNullOrEmpty()) return true
        
        val placeholders = listOf(
            "Start a conversation...",
            "बातचीत शुरू करें...",
            "Anza mazungumzo...",
            "Iniciar conversación...",
            "Commencer la conversation...",
            "开始对话...",
            "Memulai percakapan..."
        )
        
        return message in placeholders
    }
    
    /**
     * Determines navigation for returning users (not during onboarding)
     */
    suspend fun determineAppLaunchNavigation(
        repository: AppRepository,
        preferencesManager: PreferencesManager
    ): NavigationDecision {
        // For app launch, we want to be more conservative
        // Only go to chat if there's a very recent conversation with messages
        
        try {
            val conversationsResult = repository.getConversations(limit = 5, offset = 0)
            
            conversationsResult.fold(
                onSuccess = { response ->
                    val conversations = response.conversations
                    
                    // Look for the most recent conversation with actual messages
                    val recentConversationWithMessages = conversations
                        .filter { !isPlaceholderMessage(it.lastMessage) }
                        .maxByOrNull { it.updatedAt ?: it.createdAt ?: "" }
                    
                    return if (recentConversationWithMessages != null) {
                        NavigationDecision(
                            shouldGoToChat = true,
                            conversationId = recentConversationWithMessages.id
                        )
                    } else {
                        NavigationDecision(shouldGoToChat = false)
                    }
                },
                onFailure = {
                    return NavigationDecision(shouldGoToChat = false)
                }
            )
        } catch (e: Exception) {
            Log.e("NavigationHelper", "Error in app launch navigation", e)
            return NavigationDecision(shouldGoToChat = false)
        }
    }
}