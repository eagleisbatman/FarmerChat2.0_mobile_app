# Chat Interface Fixes

## Issue 1: Chat Window Title Shows English Instead of Localized Title

### Problem
The chat window title shows the English summary instead of the localized title, even though the conversations list correctly shows localized titles.

### Root Cause
- In `ChatScreen.kt`, the title is retrieved from `conversationTitles[conversationId]`
- This map in `ChatViewModel` is not properly populated with conversation data
- The `Conversation` model has localized titles but they're not being used in the chat screen

### Solution

#### Option 1: Load Conversation in ChatViewModel (Recommended)
Add conversation loading to `ChatViewModel.kt`:

```kotlin
// Add to ChatViewModel
private val _currentConversation = MutableStateFlow<Conversation?>(null)
val currentConversation: StateFlow<Conversation?> = _currentConversation

fun initializeChat(conversationId: String) {
    android.util.Log.d("ChatViewModel", "initializeChat called with conversationId: $conversationId")
    currentSessionId = conversationId
    viewModelScope.launch {
        // Load conversation metadata
        repository.getConversation(conversationId).onSuccess { conversation ->
            _currentConversation.value = conversation
        }
        
        // Load user profile
        repository.getUserProfile().onSuccess { profile ->
            // ... existing code ...
        }
    }
}
```

Update `ChatScreen.kt`:
```kotlin
// Replace this line:
val conversationTitle = conversationTitles[conversationId] ?: localizedString(StringKey.NEW_CONVERSATION)

// With:
val currentConversation by viewModel.currentConversation.collectAsState()
val conversationTitle = currentConversation?.getLocalizedTitle(userProfile?.language ?: "en") 
    ?: localizedString(StringKey.NEW_CONVERSATION)
```

Add to `FarmerChatRepository.kt`:
```kotlin
suspend fun getConversation(conversationId: String): Result<Conversation?> {
    return try {
        val doc = db.collection("conversations").document(conversationId).get().await()
        if (doc.exists()) {
            Result.success(doc.toObject<Conversation>())
        } else {
            Result.success(null)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

## Issue 2: Follow-up Questions Don't Persist

### Problem
Follow-up questions only appear during active conversation. When returning to a conversation later, they disappear.

### Root Cause
- Follow-up questions are stored in `_followUpQuestions` in ChatViewModel
- They're only populated when receiving a new AI response
- They're not persisted to Firestore or reloaded when returning to a conversation

### Solution

#### Option 1: Store Follow-up Questions in Last AI Message (Recommended)
Modify the message saving to include follow-up questions:

Update `Models.kt`:
```kotlin
data class ChatMessage(
    val id: String = "",
    val content: String = "",
    val isUser: Boolean = true,
    val timestamp: Date = Date(),
    val audioUrl: String? = null,
    val isVoiceMessage: Boolean = false,
    // Legacy fields for compatibility
    val user: Boolean = true,
    val voiceMessage: Boolean = false,
    // New field for follow-up questions
    val followUpQuestions: List<String> = emptyList()
)
```

Update `ChatViewModel.kt` in the `sendMessage` function:
```kotlin
// When saving AI message with follow-up questions
val aiMessage = ChatMessage(
    id = UUID.randomUUID().toString(),
    content = mainResponse,
    isUser = false,
    timestamp = Date(),
    audioUrl = null,
    isVoiceMessage = false,
    user = false,
    voiceMessage = false,
    followUpQuestions = followUpQuestions // Add this line
)
```

Update `ChatViewModel.kt` to restore follow-up questions:
```kotlin
fun initializeChat(conversationId: String) {
    // ... existing code ...
    
    // Listen to messages for this session
    repository.getChatMessages(conversationId).collect { messages ->
        android.util.Log.d("ChatViewModel", "Messages collected: ${messages.size} messages")
        _messages.value = messages
        
        // Restore follow-up questions from last AI message
        val lastAiMessage = messages.lastOrNull { !it.isUser }
        if (lastAiMessage != null && lastAiMessage.followUpQuestions.isNotEmpty()) {
            _followUpQuestions.value = lastAiMessage.followUpQuestions
        }
        
        // ... rest of existing code ...
    }
}
```

#### Option 2: Store Follow-up Questions in Conversation Metadata
Alternative approach - store in conversation document:

Update `Models.kt`:
```kotlin
data class Conversation(
    val id: String = "",
    val title: String = "",
    val localizedTitles: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTime: Date = Date(),
    val lastMessageIsUser: Boolean = false,
    val unreadCount: Int = 0,
    val hasUnreadMessages: Boolean = false,
    val userId: String = "",
    val createdAt: Date = Date(),
    val tags: List<String> = emptyList(),
    // New field
    val activeFollowUpQuestions: List<String> = emptyList()
)
```

Then update conversation when generating follow-up questions.

## Testing the Fixes

1. **For Title Fix:**
   - Create a new conversation in Hindi/Swahili
   - Check if chat window title shows in selected language
   - Navigate away and return - title should remain localized

2. **For Follow-up Questions:**
   - Start a conversation and get AI response with follow-up questions
   - Navigate away from chat
   - Return to same conversation - follow-up questions should still be visible
   - Send another message - old follow-up questions should be replaced with new ones

## Additional Improvements

1. **Clear Follow-up Questions**: Clear them when user sends a new message
2. **Limit Display**: Show follow-up questions only for the last AI response
3. **Visual Indicator**: Add a subtle animation when new follow-up questions appear