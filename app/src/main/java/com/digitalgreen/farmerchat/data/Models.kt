package com.digitalgreen.farmerchat.data

import com.google.firebase.firestore.PropertyName
import java.util.Date

// User profile data model
data class UserProfile(
    val userId: String = "",
    val language: String = "en", // ISO 639-1 language code
    val location: String = "", // Will be replaced with LocationInfo
    val locationInfo: LocationInfo? = null, // New detailed location
    val crops: List<String> = emptyList(),
    val livestock: List<String> = emptyList(),
    val hasCompletedOnboarding: Boolean = false,
    val createdAt: Date = Date(),
    val lastUpdated: Date = Date()
)

// Detailed location information
data class LocationInfo(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val country: String = "",
    val countryCode: String = "", // ISO 3166-1 alpha-2
    val regionLevel1: String = "", // State/Province/Region
    val regionLevel2: String = "", // District/County
    val regionLevel3: String = "", // Sub-district/Sub-county
    val regionLevel4: String = "", // Block/Ward
    val regionLevel5: String = "", // Village/Locality
    val city: String = "",
    val postalCode: String = "",
    val formattedAddress: String = "",
    val timestamp: Date = Date()
)

// Chat message data model
data class ChatMessage(
    val id: String = "",
    val content: String = "",
    @field:JvmField
    val isUser: Boolean = true,
    val timestamp: Date = Date(),
    val audioUrl: String? = null,
    @field:JvmField
    val isVoiceMessage: Boolean = false,
    // Legacy fields for Firestore compatibility
    @field:JvmField
    val user: Boolean = true,
    @field:JvmField
    val voiceMessage: Boolean = false,
    // Follow-up questions for AI messages
    val followUpQuestions: List<String> = emptyList()
) {
    constructor() : this("", "", true, Date(), null, false, true, false, emptyList())
}

// Chat session data model
data class ChatSession(
    val sessionId: String = "",
    val userId: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val createdAt: Date = Date(),
    val lastUpdated: Date = Date()
)

// Starter question data model
data class StarterQuestion(
    val id: String = "",
    val question: String = "",
    val category: String = "", // crops, livestock, general
    val language: String = "en",
    val tags: List<String> = emptyList()
)

// Feedback data model
data class Feedback(
    val id: String = "",
    val sessionId: String = "",
    val messageId: String = "",
    val userId: String = "",
    val rating: Int = 0, // 1-5 stars
    val comment: String = "",
    val timestamp: Date = Date()
)

// Onboarding state
data class OnboardingState(
    val currentStep: Int = 0,
    val selectedLanguage: String = "en",
    val selectedLocation: String = "",
    val selectedCrops: List<String> = emptyList(),
    val selectedLivestock: List<String> = emptyList()
)

// Conversation/Chat list item
data class Conversation(
    val id: String = "",
    val title: String = "",  // Default title (usually in English)
    val localizedTitles: Map<String, String> = emptyMap(), // Language code -> Localized title
    val lastMessage: String = "",
    val lastMessageTime: Date = Date(),
    val lastMessageIsUser: Boolean = false,
    val unreadCount: Int = 0,
    val hasUnreadMessages: Boolean = false,
    val userId: String = "",
    val createdAt: Date = Date(),
    val tags: List<String> = emptyList(),
    val englishTags: List<String> = emptyList(), // Tags stored in English for analytics
    val summary: String? = null // AI-generated conversation summary
) {
    /**
     * Get the conversation title in the specified language
     * Falls back to default title if translation not available
     */
    fun getLocalizedTitle(languageCode: String): String {
        return localizedTitles[languageCode] ?: title
    }
}