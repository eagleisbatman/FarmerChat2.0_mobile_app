package com.digitalgreen.farmerchat.data

import com.digitalgreen.farmerchat.network.*
import java.text.SimpleDateFormat
import java.util.*
import com.digitalgreen.farmerchat.network.Language as ApiLanguage
import com.digitalgreen.farmerchat.data.Language as DataLanguage

/**
 * Adapter functions to convert between old Firebase models and new API models
 * This helps during the migration period to maintain compatibility
 */

// Date formatting for API communication
private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

// Convert API User to UserProfile
fun ApiUser.toUserProfile(): UserProfile {
    return UserProfile(
        userId = this.id,
        language = this.language,
        location = this.location ?: "",
        crops = this.crops,
        livestock = this.livestock,
        role = this.role ?: "",
        gender = this.gender ?: "",
        hasCompletedOnboarding = true, // If they have an API profile, they've completed onboarding
        createdAt = this.createdAt?.let { parseApiDate(it) } ?: Date(),
        lastUpdated = this.updatedAt?.let { parseApiDate(it) } ?: Date()
    )
}

// Convert UserProfile to UpdateUserRequest
fun UserProfile.toUpdateUserRequest(): UpdateUserRequest {
    return UpdateUserRequest(
        name = null, // UserProfile doesn't have name field
        location = this.location.takeIf { it.isNotEmpty() },
        language = this.language,
        crops = this.crops.takeIf { it.isNotEmpty() },
        livestock = this.livestock.takeIf { it.isNotEmpty() },
        responseLength = null // UserProfile doesn't have responseLength
    )
}

// Convert API Conversation to Conversation
fun ApiConversation.toConversation(): Conversation {
    return Conversation(
        id = this.id,
        title = this.title,
        lastMessage = this.lastMessage ?: "Start a conversation...",
        lastMessageTime = this.lastMessageTime?.let { parseApiDate(it) } ?: Date(),
        lastMessageIsUser = this.lastMessageIsUser,
        userId = "", // Will be filled by repository
        createdAt = this.createdAt?.let { parseApiDate(it) } ?: Date(),
        tags = this.tags,
        englishTags = this.english_tags ?: this.englishTags ?: emptyList(),
        summary = this.summary
    )
}

// Convert API Message to ChatMessage
fun ApiMessage.toChatMessage(): ChatMessage {
    android.util.Log.d("ModelAdapters", "Converting ApiMessage: id=${this.id}, isUser=${this.isUser}, content=${this.content.take(50)}, followUpQuestions=${this.followUpQuestions.size}")
    if (this.followUpQuestions.isNotEmpty()) {
        android.util.Log.d("ModelAdapters", "Follow-up questions: ${this.followUpQuestions.map { it.question }}")
    }
    return try {
        ChatMessage(
            id = this.id,
            content = this.content,
            isUser = this.isUser,
            timestamp = this.createdAt?.let { parseApiDate(it) } ?: Date(),
            followUpQuestions = this.followUpQuestions.map { it.question },
            user = this.isUser,
            voiceMessage = false
        )
    } catch (e: Exception) {
        android.util.Log.e("ModelAdapters", "Error converting ApiMessage to ChatMessage", e)
        // Return a safe fallback
        ChatMessage(
            id = this.id,
            content = this.content,
            isUser = this.isUser,
            timestamp = Date(),
            followUpQuestions = emptyList(),
            user = this.isUser,
            voiceMessage = false
        )
    }
}

// Convert API FollowUpQuestion to old format
fun FollowUpQuestion.toOldFormat(): String = this.question

// Convert old follow-up questions to API format
fun List<String>.toApiFollowUpQuestions(): List<FollowUpQuestion> {
    return this.mapIndexed { index, question ->
        FollowUpQuestion(
            id = "follow-up-${index + 1}",
            question = question
        )
    }
}

// Convert StarterQuestion to API format
fun StarterQuestion.toApiRequest(): StarterQuestionsRequest {
    return StarterQuestionsRequest(
        crops = null, // Will be set by caller based on user profile
        livestock = null, // Will be set by caller based on user profile
        language = this.language
    )
}

// Convert API Languages to a simple list for compatibility
fun List<ApiLanguage>.toLanguageCodes(): List<String> {
    return this.map { it.code }
}

// Parse API date string to Date object
private fun parseApiDate(dateString: String): Date? {
    return try {
        apiDateFormat.parse(dateString)
    } catch (e: Exception) {
        null
    }
}

// Format Date to API string
private fun formatDateForApi(date: Date): String {
    return apiDateFormat.format(date)
}

// Convert Conversation to API CreateConversationRequest
fun Conversation.toCreateRequest(): CreateConversationRequest {
    return CreateConversationRequest(
        title = this.title.takeIf { it.isNotEmpty() },
        tags = this.tags
    )
}

// Convert ChatMessage to API SendMessageRequest
fun ChatMessage.toSendRequest(conversationId: String): SendMessageRequest {
    return SendMessageRequest(
        message = this.content,
        conversationId = conversationId
    )
}

// Helper to convert API error responses to exceptions
fun <T> ApiResponse<T>.toResultOrThrow(): Result<T> {
    return if (this.success && this.data != null) {
        Result.success(this.data)
    } else {
        Result.failure(Exception(this.error ?: this.message ?: "Unknown API error"))
    }
}

// Convert streaming events to legacy format for ViewModels
data class LegacyStreamingResponse(
    val content: String,
    val isComplete: Boolean,
    val followUpQuestions: List<String> = emptyList(),
    val conversationTitle: String? = null,
    val error: String? = null
)

fun ChatStreamEvent.toLegacyFormat(): LegacyStreamingResponse {
    return LegacyStreamingResponse(
        content = this.content ?: "",
        isComplete = this.isComplete,
        followUpQuestions = this.followUpQuestions?.map { it.question } ?: emptyList(),
        conversationTitle = this.title,
        error = this.error
    )
}

// Extension functions for easier migration
fun UserProfile.hasApiCompatibleData(): Boolean {
    return this.userId.isNotEmpty() && this.language.isNotEmpty()
}

fun Conversation.hasApiCompatibleData(): Boolean {
    return this.id.isNotEmpty()
}

fun ChatMessage.hasApiCompatibleData(): Boolean {
    return this.id.isNotEmpty() && this.content.isNotEmpty()
}

// Migration helper to determine if we should use API or Firebase
object MigrationHelper {
    var useApiRepository: Boolean = false
    
    fun enableApiRepository() {
        useApiRepository = true
    }
    
    fun disableApiRepository() {
        useApiRepository = false
    }
    
    fun shouldUseApi(): Boolean = useApiRepository
}

// Convert API Language to Data Language
fun ApiLanguage.toDataLanguage(): DataLanguage {
    return DataLanguage(
        code = this.code,
        name = this.nativeName,  // Use nativeName as name
        englishName = this.name,  // Use name as englishName (API doesn't have separate englishName)
        locale = Locale(this.code),  // Create locale from code
        isRTL = this.isRTL
    )
}