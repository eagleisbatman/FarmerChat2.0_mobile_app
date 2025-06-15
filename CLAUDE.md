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

## Recent Feature Enhancements

### Design System Implementation
- **Comprehensive Design System** in `ui/theme/DesignSystem.kt`
- **Standardized Colors** with primary green palette and proper dark/light theme support
- **Typography Scale** with consistent font sizes and weights across the app
- **Spacing System** using predefined scales (xxs to xxxl)
- **Icon Sizing** standardized for small (16dp), medium (24dp), large (32dp) contexts
- **Reusable Components** like `FarmerChatAppBar` for consistency

### Theme Consistency
- **Disabled Dynamic Colors** by default to maintain brand identity
- **Green Color Scheme** replacing default Material purple colors
- **Consistent AppBar** styling using `appBarColor()` helper function
- **Proper Theme Colors** for all UI elements (no more hardcoded colors)
- **Dark Theme Support** with appropriate color mappings

### Voice Confidence Scoring
- **Real-time confidence tracking** in `SpeechRecognitionManager`
- **Visual feedback** with color-coded progress bars (High/Medium/Low confidence)
- **Confidence levels** categorized as HIGH (0.8+), MEDIUM (0.5-0.8), LOW (<0.5)
- **UI integration** in ChatScreen with live confidence display during voice input

### Enhanced Settings Management
- **Fully editable settings** with dedicated dialogs for each option
- **Name editing** with immediate profile updates
- **Location editing** with manual input and GPS detection capability
- **Response length preferences** (Concise/Detailed/Comprehensive)
- **Data export functionality** with proper file provider configuration
- **Separate screens** for crop and livestock selection (no onboarding flow)

### Improved User Experience
- **Dedicated CropSelectionScreen** and **LivestockSelectionScreen** for settings updates
- **Search functionality** across crops and livestock with multi-language support
- **Real-time selection counts** and visual feedback
- **Conversation categorization** via TagManager for better organization
- **Persistent user preferences** with proper state management

### Technical Improvements
- **File provider configuration** for secure data sharing and export
- **Enhanced error handling** across all ViewModels
- **Improved state synchronization** between UI and data layers
- **Cleaner separation** between onboarding and settings flows
- **Deprecated API Updates** (Icons.AutoMirrored, HorizontalDivider)
- **Accessibility Improvements** with proper content descriptions

## Development Best Practices

### UI/UX Guidelines
1. **Use Design System Constants** - Always use values from `DesignSystem.kt` instead of hardcoding
   - Colors: `DesignSystem.Colors.Primary` instead of `Color(0xFF4CAF50)`
   - Spacing: `DesignSystem.Spacing.md` instead of `16.dp`
   - Typography: `DesignSystem.Typography.titleMedium` instead of `20.sp`

2. **Consistent AppBars** - Use `FarmerChatAppBar` composable for all screens
   ```kotlin
   FarmerChatAppBar(
       title = "Screen Title",
       onBackClick = { /* navigation */ }
   )
   ```

3. **Theme Colors** - Always use Material theme colors
   ```kotlin
   // Good
   color = MaterialTheme.colorScheme.primary
   
   // Bad
   color = Color(0xFF4CAF50)
   ```

4. **Localization** - All user-facing strings must be in `StringsManager`
   ```kotlin
   // Good
   text = stringResource(StringKey.Settings)
   
   // Bad
   text = "Settings"
   ```

5. **Modern Icons** - Use AutoMirrored icons for directional icons
   ```kotlin
   // Good
   Icons.AutoMirrored.Filled.ArrowBack
   
   // Bad
   Icons.Default.ArrowBack
   ```

### Code Quality Standards
1. **No Magic Numbers** - Use constants from DesignSystem
2. **Consistent Naming** - "FarmerChat" (not "Farmer Chat")
3. **Accessibility** - All icons need `contentDescription`
4. **State Management** - Use `StateFlow` and proper ViewModel patterns
5. **Error Handling** - Wrap Firebase calls in try-catch blocks

### Testing Guidelines
- Test on both light and dark themes
- Verify all languages display correctly
- Check voice features with different locales
- Ensure offline functionality works properly

### Component Usage
1. **Standardized Components** - Prefer design system components
   ```kotlin
   // Use predefined spacing
   Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
   
   // Use predefined icon sizes
   Icon(
       modifier = Modifier.size(DesignSystem.IconSize.medium),
       // ...
   )
   ```

2. **Consistent Padding** - Use design system spacing values
   ```kotlin
   // Good
   modifier = Modifier.padding(DesignSystem.Spacing.md)
   
   // Bad
   modifier = Modifier.padding(16.dp)
   ```

3. **Typography Consistency** - Use design system text styles
   ```kotlin
   Text(
       text = "Title",
       fontSize = DesignSystem.Typography.titleLarge,
       fontWeight = DesignSystem.Typography.Weight.Bold
   )
   ```