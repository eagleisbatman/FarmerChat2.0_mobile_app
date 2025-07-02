# CLAUDE-mobile.md

This file provides Android app-specific guidance to Claude Code for the FarmerChat mobile application.

## 📱 Android App Architecture

### Core Architecture
- **Pattern**: MVVM (Model-View-ViewModel) with Repository pattern
- **UI**: Jetpack Compose with Material 3
- **State Management**: StateFlow and Compose State
- **Dependency Injection**: Manual injection (no Hilt/Dagger)
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

### Key Components
- **ViewModels**: Business logic and state management
- **Repository**: `AppRepository` for API calls
- **Compose UI**: Declarative UI components
- **Navigation**: Compose Navigation with type-safe routes

## 🚀 Build & Deploy Commands

### Complete Build Process (DEFAULT)
```bash
# Ensure backend is running first!
curl -s http://localhost:3004/health || echo "⚠️ Start backend first: ./start-backend.sh"

# Build and deploy (ALWAYS use after code changes)
./gradlew clean assembleDebug && ./gradlew installDebug
```

### Quick Commands
```bash
# Force stop and rebuild
adb shell am force-stop com.digitalgreen.farmerchat && ./gradlew clean assembleDebug && ./gradlew installDebug

# Clear data and fresh install
adb shell pm clear com.digitalgreen.farmerchat && ./gradlew clean assembleDebug && ./gradlew installDebug

# Launch app
adb shell am start -n com.digitalgreen.farmerchat/.MainActivity
```

## 🎨 UI/UX Guidelines

### Design System Usage
```kotlin
// ALWAYS use DesignSystem constants
color = DesignSystem.Colors.Primary
spacing = DesignSystem.Spacing.md
fontSize = DesignSystem.Typography.titleLarge

// NEVER hardcode values
color = Color(0xFF4CAF50) // ❌ Bad
spacing = 16.dp // ❌ Bad
```

### Component Standards
- Use `FarmerChatAppBar` for all screens
- Use `MessageBubbleV2` for chat messages (RTL support)
- Use `WhatsAppVoiceRecorder` for audio recording
- Apply Material 3 theming consistently

### Localization Requirements
```kotlin
// ALWAYS use StringsManager
text = localizedString(StringKey.Settings)

// NEVER hardcode strings
text = "Settings" // ❌ Bad
```

## 🔄 App Flow & Navigation

### Initial Launch Flow
1. **SplashScreen** → Check authentication
2. **Language Selection** (first time only)
3. **Onboarding** (role, crops, livestock selection)
4. **Login/Register** → Phone + PIN authentication
5. **ConversationsScreen** → Main app experience

### Navigation Routes
```kotlin
Routes.Splash -> Routes.Conversations
Routes.Conversations -> Routes.Chat(conversationId)
Routes.Settings -> Routes.CropSelection
Routes.Settings -> Routes.LivestockSelection
```

## 🎯 Key Features

### Audio Recording (NEW)
- **Component**: `WhatsAppVoiceRecorder`
- **Manager**: `AudioRecordingManager`
- **Flow**: Record → Play/Discard → Send for transcription
- **API**: OpenAI Whisper for transcription

### Multi-Language Support
- **50+ Languages** via `LanguageManager`
- **RTL Support** for Arabic, Hebrew, Urdu, Farsi
- **Voice Recognition** with proper locale mapping
- **Dynamic Translations** from backend

### Real-time Chat
- **WebSocket** connection for streaming responses
- **Message Queue** for offline support
- **Typing Indicators** during AI processing
- **Auto-retry** on connection failure

## 🐛 Common Issues & Solutions

### "No auth token available"
```bash
# Backend not running - start it first
./start-backend.sh

# Then clear app and restart
adb shell pm clear com.digitalgreen.farmerchat
./gradlew clean assembleDebug && ./gradlew installDebug
```

### Build Failures
```bash
# Clean everything
./gradlew clean
rm -rf ~/.gradle/caches/
./gradlew --stop

# Rebuild
./gradlew assembleDebug
```

### App Crashes
```bash
# Check logs
adb logcat | grep -E "FarmerChat|FATAL|AndroidRuntime"

# Common fixes:
# 1. Ensure backend is running
# 2. Check network permissions
# 3. Verify API endpoints
```

## 📊 Debugging & Monitoring

### Logcat Filters
```bash
# All app logs
adb logcat | grep FarmerChat

# Network requests
adb logcat | grep -E "ApiChat|NetworkConfig|OkHttp"

# Audio features
adb logcat | grep -E "AudioRecording|WhatsApp|transcribe"

# Authentication
adb logcat | grep -E "JWT|Auth|Token"
```

### Performance Monitoring
- Use Android Studio Profiler for memory/CPU
- Monitor network calls in Profiler
- Check Compose recomposition counts

## 🧪 Testing Guidelines

### Manual Testing Checklist
1. ✅ Fresh install flow (onboarding)
2. ✅ Chat creation and messaging
3. ✅ Audio recording and transcription
4. ✅ Language switching
5. ✅ Offline mode handling
6. ✅ Token persistence across restarts
7. ✅ Settings updates
8. ✅ RTL language display

### Automated Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

## 📝 Code Style Rules

### Kotlin Conventions
```kotlin
// Use explicit types for public APIs
fun processMessage(message: String): Result<Message>

// Use type inference for local variables
val result = apiService.sendMessage(text)

// Prefer single-expression functions
fun isValid() = text.isNotEmpty() && text.length < 500
```

### Compose Best Practices
```kotlin
// Hoist state
@Composable
fun ChatScreen(
    state: ChatUiState,
    onSendMessage: (String) -> Unit
)

// Use remember for expensive operations
val processedText = remember(rawText) {
    processText(rawText)
}

// Minimize recompositions
val stableList = remember { mutableStateListOf<Message>() }
```

## 🔐 Security Considerations

- **NEVER** log sensitive data (tokens, user info)
- **NEVER** hardcode API keys
- **ALWAYS** use HTTPS for API calls
- **Validate** all user inputs
- **Sanitize** data before display

## 🚨 Critical Warnings

1. **Translation System**: Backend has translations but app uses static `StringsManager.kt`
   - TODO: Implement dynamic translation loading

2. **Audio Permissions**: Always check before recording
   ```kotlin
   if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) 
       != PackageManager.PERMISSION_GRANTED) {
       // Request permission
   }
   ```

3. **Network State**: Handle offline gracefully
   ```kotlin
   if (!isNetworkAvailable()) {
       // Show offline UI
   }
   ```