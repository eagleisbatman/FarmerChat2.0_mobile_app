package com.digitalgreen.farmerchat.network

import com.google.gson.annotations.SerializedName

// Common API Response wrapper
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null,
    val message: String? = null
)

// Authentication Models
data class AuthRequest(
    @SerializedName("idToken") val idToken: String,
    @SerializedName("deviceInfo") val deviceInfo: DeviceInfo? = null
)

data class DeviceInfo(
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("platform") val platform: String = "android",
    @SerializedName("appVersion") val appVersion: String,
    @SerializedName("fcmToken") val fcmToken: String? = null
)

data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("user") val user: ApiUser,
    @SerializedName("expiresIn") val expiresIn: Long
)

// User Models
data class ApiUser(
    @SerializedName("id") val id: String,
    @SerializedName("firebaseUid") val firebaseUid: String,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("language") val language: String = "en",
    @SerializedName("crops") val crops: List<String> = emptyList(),
    @SerializedName("livestock") val livestock: List<String> = emptyList(),
    @SerializedName("responseLength") val responseLength: String = "medium",
    @SerializedName("isActive") val isActive: Boolean = true,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class UpdateUserRequest(
    @SerializedName("name") val name: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("crops") val crops: List<String>? = null,
    @SerializedName("livestock") val livestock: List<String>? = null,
    @SerializedName("responseLength") val responseLength: String? = null
)

// Conversation Models
data class ApiConversation(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("lastMessage") val lastMessage: String,
    @SerializedName("lastMessageTime") val lastMessageTime: String,
    @SerializedName("lastMessageIsUser") val lastMessageIsUser: Boolean,
    @SerializedName("tags") val tags: List<String> = emptyList(),
    @SerializedName("englishTags") val englishTags: List<String> = emptyList(),
    @SerializedName("summary") val summary: String? = null,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class CreateConversationRequest(
    @SerializedName("title") val title: String? = null,
    @SerializedName("tags") val tags: List<String> = emptyList()
)

data class ConversationsResponse(
    @SerializedName("conversations") val conversations: List<ApiConversation>,
    @SerializedName("total") val total: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("offset") val offset: Int
)

// Message Models
data class ApiMessage(
    @SerializedName("id") val id: String,
    @SerializedName("conversationId") val conversationId: String,
    @SerializedName("content") val content: String,
    @SerializedName("isUser") val isUser: Boolean,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("rating") val rating: Int? = null,
    @SerializedName("followUpQuestions") val followUpQuestions: List<FollowUpQuestion> = emptyList()
)

data class FollowUpQuestion(
    @SerializedName("id") val id: String,
    @SerializedName("question") val question: String
)

data class SendMessageRequest(
    @SerializedName("message") val message: String,
    @SerializedName("conversationId") val conversationId: String
)

data class SendMessageResponse(
    @SerializedName("message") val message: ApiMessage,
    @SerializedName("aiResponse") val aiResponse: ApiMessage,
    @SerializedName("followUpQuestions") val followUpQuestions: List<FollowUpQuestion> = emptyList(),
    @SerializedName("conversationTitle") val conversationTitle: String? = null
)

data class RateMessageRequest(
    @SerializedName("rating") val rating: Int,
    @SerializedName("feedback") val feedback: String? = null
)

// Translation Models
data class TranslationBundle(
    @SerializedName("ui") val ui: Map<String, String>,
    @SerializedName("crops") val crops: Map<String, CropTranslation>,
    @SerializedName("livestock") val livestock: Map<String, LivestockTranslation>,
    @SerializedName("metadata") val metadata: TranslationMetadata
)

data class CropTranslation(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null
)

data class LivestockTranslation(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null
)

data class TranslationMetadata(
    @SerializedName("language") val language: String,
    @SerializedName("lastUpdated") val lastUpdated: String,
    @SerializedName("coverage") val coverage: Int
)

data class Language(
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("nativeName") val nativeName: String,
    @SerializedName("isRTL") val isRTL: Boolean,
    @SerializedName("coverage") val coverage: Int
)

// WebSocket Models
data class ChatStreamEvent(
    @SerializedName("type") val type: String, // "chunk", "complete", "error", "typing"
    @SerializedName("content") val content: String? = null,
    @SerializedName("chunkNumber") val chunkNumber: Int? = null,
    @SerializedName("isComplete") val isComplete: Boolean = false,
    @SerializedName("followUpQuestions") val followUpQuestions: List<FollowUpQuestion>? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("usage") val usage: ApiUsage? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("isTyping") val isTyping: Boolean? = null
)

data class ApiUsage(
    @SerializedName("promptTokens") val promptTokens: Int,
    @SerializedName("completionTokens") val completionTokens: Int,
    @SerializedName("totalTokens") val totalTokens: Int
)

// Starter Questions
data class StarterQuestionsRequest(
    @SerializedName("crops") val crops: List<String>? = null,
    @SerializedName("livestock") val livestock: List<String>? = null,
    @SerializedName("language") val language: String = "en"
)