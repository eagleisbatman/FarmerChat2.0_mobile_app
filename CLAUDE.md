# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Common Development Commands

### Building and Running
```bash
# Build the project
./gradlew build

# Build debug APK
./gradlew assembleDebug

# Install and run on connected device/emulator
./gradlew installDebug

# Clean build artifacts
./gradlew clean

# Run with specific build variant
./gradlew assembleRelease
```

### Testing
```bash
# Run all unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests "com.digitalgreen.farmerchat.ExampleUnitTest"

# Run tests with coverage
./gradlew createDebugCoverageReport
```

### Linting and Code Quality
```bash
# Run Android lint
./gradlew lint

# Run lint and generate HTML report
./gradlew lintDebug
# Report location: app/build/reports/lint-results-debug.html

# Check Kotlin code style
./gradlew ktlintCheck

# Auto-format Kotlin code (if ktlint is configured)
./gradlew ktlintFormat
```

### Firebase Deployment
```bash
# Deploy Firestore rules and indexes
firebase deploy --only firestore:rules,firestore:indexes

# Deploy only rules
firebase deploy --only firestore:rules

# Seed starter questions (run locally)
node seed-database.js
```

## High-Level Architecture

### Core Architecture Pattern
The app follows **MVVM (Model-View-ViewModel)** architecture with:
- **ViewModels** manage UI state and business logic
- **Repository** pattern for data access abstraction
- **Compose UI** for declarative UI components
- **StateFlow** for reactive state management

### Key Architectural Components

#### Authentication Flow
- App uses **Firebase Anonymous Authentication** automatically on first launch
- User ID persists across app sessions until app data is cleared
- All user data is keyed by this anonymous user ID
- No explicit login/logout - seamless experience

#### Data Flow Architecture
1. **UI Layer** (Compose Screens) → observes StateFlow from ViewModels
2. **ViewModels** → orchestrate business logic, call Repository methods
3. **Repository** (`FarmerChatRepository`) → abstracts Firebase operations
4. **Firebase Services** → Firestore for data, Anonymous Auth for identity

#### AI Integration Architecture
- **Gemini AI** integration in `ChatViewModel`
- Streaming responses for real-time UI updates
- Context injection from user profile (location, crops, livestock)
- Follow-up question extraction from AI responses
- Intelligent title generation using AI after first exchange
- Language-aware prompts ensure AI responds in user's selected language

#### Multi-Language Architecture
- **50+ Global Languages** supported via `LanguageManager`
- **Localized UI** with `StringsManager` and `StringKey` enum
- **Multilingual Crops/Livestock** via `CropTranslations` and `LivestockTranslations`
- **AI responses** in user's selected language via prompt engineering
- **Voice Recognition** with proper locale mapping for 40+ languages
- **Text-to-Speech** with intelligent voice selection and language mapping
- Search works across all language translations

#### Location Architecture
- **GPS-based location** with permission handling
- **Reverse geocoding** for automatic location detection
- **Generic location hierarchy** (country, regionLevel1-5) for global support
- Location context injected into AI prompts for localized advice

### Critical Integration Points

#### Firebase Security Model
- Firestore rules enforce user data isolation
- Each user can only access their own conversations/messages
- Starter questions are publicly readable
- See `firestore.rules` for complete security implementation

#### API Key Management
- Gemini API key loaded from `local.properties` at build time
- Injected into `BuildConfig.GEMINI_API_KEY`
- Never committed to version control

#### Voice Features Architecture
- `SpeechRecognitionManager` handles voice-to-text
- `TextToSpeechManager` handles text-to-voice
- Both managers handle lifecycle properly to avoid memory leaks
- Language parameter passed based on user preference

#### Conversation Management
- Conversations and chat_sessions are separate collections
- `conversations` stores metadata (title, last message, etc.)
- `chat_sessions/{id}/messages` stores actual message history
- Real-time listeners update UI automatically when data changes

### State Management Strategy
- Each screen has its own ViewModel
- ViewModels expose `StateFlow` for UI state
- Compose UI recomposes automatically on state changes
- Repository methods return `Result<T>` for error handling
- No global state management - each feature is self-contained

### Navigation Architecture
- Single Activity with Compose Navigation
- Routes defined in `Navigation.kt`
- Deep linking supported for future features
- Navigation state preserved across configuration changes