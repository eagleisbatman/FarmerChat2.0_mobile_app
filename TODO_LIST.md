# FarmerChat TODO List

## Backend Integration TODOs

### 1. FCM Token Implementation
**Files:** 
- `AppRepository.kt:51`
- `ApiRepository.kt:43`

**Task:** Get FCM token when implementing push notifications
```kotlin
fcmToken = null // TODO: Get FCM token when implementing push notifications
```

### 2. WebSocket Support
**File:** `ApiChatViewModel.kt:83,86,245,301`

**Tasks:**
- Implement `joinConversation()` for real-time updates
- Implement `listenForStreamingEvents()` for streaming chat
- Implement `stopStreaming()` to stop message streaming
- Implement `leaveConversation()` for cleanup

**Note:** Currently using HTTP endpoints instead of WebSocket for chat messages.

### 3. WebSocket URL Configuration
**File:** `ChatWebSocketClient.kt:20`

**Task:** Update WebSocket URL when implementing real-time features
```kotlin
// TODO: Update with your backend WebSocket URL
private const val BASE_URL = "ws://10.0.2.2:3000" // Android emulator localhost
```

## UI/UX TODOs

### 4. Chat Screen More Options
**File:** `ChatScreen.kt:148`

**Task:** Implement more options menu in chat screen app bar
```kotlin
IconButton(onClick = { /* TODO: More options */ }) {
    Icon(Icons.Default.MoreVert, contentDescription = "More options")
}
```

### 5. Settings Screen Features
**File:** `SettingsScreen.kt:228,290,357`

**Tasks:**
- Implement help/feedback functionality
- Implement "Delete all data" feature  
- Implement GPS location detection for location settings

### 6. Data Export Feature
**File:** `ApiSettingsViewModel.kt:184`

**Task:** Implement data export using API data
```kotlin
// TODO: Implement export using API data
```

## Configuration TODOs

### 7. Backend URL Configuration
**File:** `NetworkConfig.kt:20`

**Task:** Update with production backend URL (currently using localhost)
```kotlin
// TODO: Update with your backend URL
private const val BASE_URL = "http://10.0.2.2:3002/api/v1/"
```

### 8. Gemini API Key
**File:** `ChatViewModel.kt:31`

**Task:** Add Gemini API key in local.properties (for Firebase-only mode)
```kotlin
apiKey = BuildConfig.GEMINI_API_KEY // TODO: Add your API key in local.properties
```

### 9. User Profile Updates
**File:** `SettingsViewModel.kt:271`

**Task:** Update user profile in Firebase (legacy - may not be needed with API backend)
```kotlin
// TODO: Update user profile in Firebase
```

## Priority Recommendations

### High Priority
1. **FCM Token Implementation** - Required for push notifications
2. **Data Export Feature** - User-requested feature
3. **GPS Location Detection** - Improves user experience

### Medium Priority
4. **WebSocket Support** - For real-time chat (currently HTTP works fine)
5. **Delete All Data** - Important for user privacy
6. **Help/Feedback** - User support feature

### Low Priority
7. **More Options Menu** - Additional features can be added later
8. **Firebase Profile Updates** - Legacy code, may be removed

## Notes
- Most WebSocket TODOs are commented out as the app currently uses HTTP endpoints successfully
- The backend URL configuration is marked as TODO but is already functional for development
- Some TODOs may be legacy from the Firebase-to-API migration