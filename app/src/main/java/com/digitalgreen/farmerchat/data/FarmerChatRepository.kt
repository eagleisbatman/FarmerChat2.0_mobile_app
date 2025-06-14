package com.digitalgreen.farmerchat.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.util.Date

class FarmerChatRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    // Get current user ID
    private fun getCurrentUserId(): String? = auth.currentUser?.uid
    
    // Create or update user profile
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            firestore.collection("users")
                .document(userId)
                .set(profile.copy(userId = userId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get user profile
    suspend fun getUserProfile(): Result<UserProfile?> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            if (document.exists()) {
                Result.success(document.toObject(UserProfile::class.java))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get starter questions based on user preferences
    suspend fun getStarterQuestions(language: String, crops: List<String>, livestock: List<String>): Result<List<StarterQuestion>> {
        return try {
            val questions = mutableListOf<StarterQuestion>()
            
            // Get general questions
            val generalQuestions = firestore.collection("starter_questions")
                .whereEqualTo("language", language)
                .whereEqualTo("category", "general")
                .limit(3)
                .get()
                .await()
            
            generalQuestions.documents.forEach { doc ->
                doc.toObject(StarterQuestion::class.java)?.let { questions.add(it) }
            }
            
            // Get crop-specific questions if user has crops
            if (crops.isNotEmpty()) {
                val cropQuestions = firestore.collection("starter_questions")
                    .whereEqualTo("language", language)
                    .whereEqualTo("category", "crops")
                    .whereArrayContainsAny("tags", crops)
                    .limit(2)
                    .get()
                    .await()
                
                cropQuestions.documents.forEach { doc ->
                    doc.toObject(StarterQuestion::class.java)?.let { questions.add(it) }
                }
            }
            
            // Get livestock-specific questions if user has livestock
            if (livestock.isNotEmpty()) {
                val livestockQuestions = firestore.collection("starter_questions")
                    .whereEqualTo("language", language)
                    .whereEqualTo("category", "livestock")
                    .whereArrayContainsAny("tags", livestock)
                    .limit(2)
                    .get()
                    .await()
                
                livestockQuestions.documents.forEach { doc ->
                    doc.toObject(StarterQuestion::class.java)?.let { questions.add(it) }
                }
            }
            
            Result.success(questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Create a new chat session
    suspend fun createChatSession(): Result<String> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            val sessionId = UUID.randomUUID().toString()
            val session = ChatSession(
                sessionId = sessionId,
                userId = userId
            )
            
            firestore.collection("chat_sessions")
                .document(sessionId)
                .set(session)
                .await()
            
            Result.success(sessionId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Save a message to a chat session
    suspend fun saveMessage(sessionId: String, message: ChatMessage): Result<Unit> {
        return try {
            val messageWithId = message.copy(id = UUID.randomUUID().toString())
            
            firestore.collection("chat_sessions")
                .document(sessionId)
                .collection("messages")
                .document(messageWithId.id)
                .set(messageWithId)
                .await()
            
            // Update session last updated time
            firestore.collection("chat_sessions")
                .document(sessionId)
                .update("lastUpdated", System.currentTimeMillis())
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get messages for a chat session
    fun getChatMessages(sessionId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = firestore.collection("chat_sessions")
            .document(sessionId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)
                } ?: emptyList()
                
                trySend(messages)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Save feedback
    suspend fun saveFeedback(feedback: Feedback): Result<Unit> {
        return try {
            firestore.collection("feedback")
                .document(feedback.id)
                .set(feedback)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Sign in anonymously
    suspend fun signInAnonymously(): Result<String> {
        return try {
            val result = auth.signInAnonymously().await()
            Result.success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    
    // Get user conversations
    fun getUserConversations(): Flow<List<Conversation>> = callbackFlow {
        val userId = getCurrentUserId()
        android.util.Log.d("FarmerChatRepository", "getUserConversations called with userId: $userId")
        
        if (userId == null) {
            android.util.Log.e("FarmerChatRepository", "User ID is null in getUserConversations")
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        // Note: Can't query all conversations due to security rules
        // Each user can only see their own conversations
        
        // Try without ordering first to see if index is the issue
        val listener = firestore.collection("conversations")
            .whereEqualTo("userId", userId)
            // .orderBy("lastMessageTime", Query.Direction.DESCENDING) // Commented out to test
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FarmerChatRepository", "Error getting conversations", error)
                    // Don't send empty list on error - let the existing data remain
                    // This prevents the UI from flickering to empty state on transient errors
                    return@addSnapshotListener
                }
                
                android.util.Log.d("FarmerChatRepository", "Conversations for user $userId: ${snapshot?.documents?.size} documents")
                snapshot?.documents?.forEach { doc ->
                    android.util.Log.d("FarmerChatRepository", "Doc ${doc.id}: ${doc.data}")
                }
                
                val conversations = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val conv = doc.toObject(Conversation::class.java)
                        android.util.Log.d("FarmerChatRepository", "Parsed conversation: id=${conv?.id}, userId=${conv?.userId}, title=${conv?.title}")
                        conv
                    } catch (e: Exception) {
                        android.util.Log.e("FarmerChatRepository", "Error parsing conversation ${doc.id}", e)
                        null
                    }
                } ?: emptyList()
                
                // Sort in memory for now
                val sortedConversations = conversations.sortedByDescending { it.lastMessageTime }
                
                android.util.Log.d("FarmerChatRepository", "Sending ${sortedConversations.size} conversations to UI")
                trySend(sortedConversations)
            }
        
        awaitClose { 
            listener.remove() 
        }
    }
    
    // Save or update conversation metadata
    suspend fun saveConversation(conversation: Conversation): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            firestore.collection("conversations")
                .document(conversation.id)
                .set(conversation.copy(userId = userId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Update conversation metadata
    suspend fun updateConversationMetadata(conversation: Conversation): Result<Unit> {
        return try {
            val currentUserId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            android.util.Log.d("FarmerChatRepository", "updateConversationMetadata - currentUserId: $currentUserId, conversation.userId: ${conversation.userId}")
            
            // Ensure we use the current user's ID
            val conversationToSave = conversation.copy(userId = currentUserId)
            
            // Just set the document - this will create it if it doesn't exist or update if it does
            val docRef = firestore.collection("conversations").document(conversation.id)
            
            android.util.Log.d("FarmerChatRepository", "Setting conversation document: ${conversation.id} with userId: $currentUserId")
            android.util.Log.d("FarmerChatRepository", "Full conversation object: $conversationToSave")
            
            try {
                // Use set with merge to handle both create and update cases
                docRef.set(conversationToSave, com.google.firebase.firestore.SetOptions.merge()).await()
                android.util.Log.d("FarmerChatRepository", "Successfully set conversation document")
            } catch (e: Exception) {
                android.util.Log.e("FarmerChatRepository", "Failed to set conversation document", e)
                throw e
            }
            
            android.util.Log.d("FarmerChatRepository", "Successfully updated conversation metadata")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("FarmerChatRepository", "Failed to update conversation metadata", e)
            Result.failure(e)
        }
    }
    
    // Delete conversation
    suspend fun deleteConversation(conversationId: String): Result<Unit> {
        return try {
            // Delete all messages first
            val messages = firestore.collection("chat_sessions")
                .document(conversationId)
                .collection("messages")
                .get()
                .await()
            
            messages.documents.forEach { doc ->
                doc.reference.delete().await()
            }
            
            // Delete the session
            firestore.collection("chat_sessions")
                .document(conversationId)
                .delete()
                .await()
            
            // Delete conversation metadata
            firestore.collection("conversations")
                .document(conversationId)
                .delete()
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}