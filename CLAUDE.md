# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 🚫 CRITICAL DEVELOPMENT RULES

### NO SHORTCUTS POLICY
1. **NEVER take shortcuts** - Always follow the proper process
2. **NEVER create temporary files** - Use existing scripts and infrastructure
3. **NEVER bypass established workflows** - Follow the documented procedures
4. **ALWAYS use existing scripts** - Check the scripts folder before creating new files
5. **ALWAYS run proper translation scripts** - Use the backend translation system for all UI strings
6. **ALWAYS follow the complete build process** - No partial solutions or workarounds

## 🚨 Firebase to Node.js/Neon PostgreSQL Migration Status

### ✅ Migration Successfully Completed!

**Architecture**: Successfully migrated from Firebase-only to **Node.js + Neon PostgreSQL** backend with Firebase Auth for phone OTP only.

**Status**: 
- ✅ Backend: Node.js + Express + TypeScript running on port **3004** (NEVER use 3000, 3002)
- ✅ Database: Neon PostgreSQL (Project: `spring-flower-04114371`) 
- ✅ API Endpoints: Complete RESTful API with full functionality
- ✅ Authentication: Firebase → Backend JWT exchange working perfectly
- ✅ **Token Authorization**: JWT tokens properly persisted and attached to all requests
- ✅ **Chat Functionality**: Messages send/receive working with OpenAI integration
- ✅ **All Firebase Removed**: Except Firebase Auth for phone OTP
- ✅ **AI Provider**: Only OpenAI enabled and active (gpt-4o-mini)
- ✅ **Phone OTP**: Registration flow already implemented

## 🤖 Current AI Configuration

**Active Setup**: 
- **Primary Provider**: OpenAI (gpt-4o-mini)
- **Status**: Only OpenAI is enabled (`AI_PROVIDERS_ENABLED=openai`)
- **Other Providers**: Gemini and Anthropic API keys exist in config but are **disabled**
- **Architecture**: Multi-provider support built but not currently used

**Environment Variables**:
```env
DEFAULT_AI_PROVIDER=openai
AI_PROVIDERS_ENABLED=openai  
OPENAI_MODEL=gpt-4o-mini
```

**Key Implementation Details**:
- JWT tokens persist between app sessions using DataStore
- Backend MUST run on port 3004 (kill existing processes first)
- All Firebase database code removed - using API only
- Phone OTP registration screen already exists and works

**Important**: Fresh deployment, API-only mode. No Firebase database operations.

## Recent Fixes (Jun 21, 2025)

### Navigation Loop Fix
- Fixed infinite loop in ConversationsScreen when `startNewChat` parameter was true
- Added `hasHandledStartNewChat` flag to ensure navigation only happens once

### Dynamic Prompts Implementation
- Removed dependency on database-seeded prompts
- All prompts (system, starter questions, follow-up, titles) are now dynamically generated
- No need to run seed scripts - everything works out of the box

### Multilingual Onboarding
- Added translations for role selection (Farmer/Extension Worker) in Hindi and Swahili
- Added translations for gender selection (Male/Female/Other) in Hindi and Swahili
- Added translations for phone authentication screens in all languages

### 🔧 Recent Integration Fixes (January 2025)

**Fixed Issues**:
1. **Backend Authentication** - Added missing authentication middleware to protected routes
2. **Token Race Condition** - Fixed async token loading and removed blocking operations
3. **ViewModel Initialization** - Prevented premature API calls by adding explicit initialization
4. **Authentication Flow** - Improved error handling and retry capability in SplashViewModel
5. **WebSocket Reconnection** - Added automatic reconnection after app restart
6. **401 Error Handling** - Added detection and handling of expired tokens

**Key Changes**:
- All protected backend routes now require JWT authentication
- ViewModels use `initialize()` pattern instead of init blocks
- WebSocket automatically reconnects using stored tokens
- Proper async/await for token operations
- Authentication state management with loading/error/success states

## Common Development Commands

### Backend Server Management

#### 🚀 Quick Start Script (RECOMMENDED)
```bash
# Use the automated startup script:
./start-backend.sh

# This script automatically:
# 1. Kills any process on port 3004
# 2. Navigates to backend directory
# 3. Installs dependencies if needed
# 4. Starts the backend server
```

#### Manual Backend Start
```bash
# IMPORTANT: Always check and kill processes on port 3004 before starting
lsof -ti:3004 | xargs kill -9 2>/dev/null || true

# Start the backend server (always on port 3004)
cd backend
npm run dev

# The server MUST run on port 3004 to avoid port conflicts
# Never use ports 3000, 3002, etc. - always use 3004
```

#### ✅ Server Health Check Commands
```bash
# Check if backend is running and healthy
curl -s http://localhost:3004/health

# Expected response:
# {"status":"healthy","timestamp":"...","environment":"development","version":"v1"}

# Check what's running on port 3004
lsof -i:3004

# Comprehensive server status check
echo "🔍 Backend Status:" && \
curl -s http://localhost:3004/health | jq . 2>/dev/null || echo "❌ Backend not responding" && \
echo "📡 Port 3004:" && \
lsof -i:3004 | head -2 || echo "❌ No process on port 3004"
```

#### Port Management Commands
```bash
# Kill process on port 3004 (macOS/Linux)
lsof -ti:3004 | xargs kill -9 2>/dev/null || true

# Alternative: Find and kill Node.js processes
ps aux | grep node | grep -v grep | awk '{print $2}' | xargs kill -9 2>/dev/null || true

# One-liner to kill port and start backend
lsof -ti:3004 | xargs kill -9 2>/dev/null || true && cd backend && npm run dev
```

### 📱 Mobile App Building and Running

#### 🎯 Complete Build and Deploy Process (USE THIS BY DEFAULT)
```bash
# STEP 1: Ensure backend is running first
curl -s http://localhost:3004/health || echo "⚠️ Start backend first: ./start-backend.sh"

# STEP 2: Build and deploy app (ALWAYS use this sequence after making code changes)
./gradlew clean assembleDebug && ./gradlew installDebug

# This command:
# 1. Cleans previous build artifacts
# 2. Builds a fresh debug APK  
# 3. Installs it on the running emulator/device
# 4. App should auto-launch to onboarding screen (for new users) or conversations (returning users)
```

#### 🔄 App Launch and Initial Screen Behavior
```bash
# After installation, the app will:
# 1. First launch: Show language selection → onboarding flow → conversations
# 2. Subsequent launches: Direct to conversations screen (if authenticated)
# 3. If no backend: Show "No auth token available" error

# Force launch app manually (if doesn't auto-start):
adb shell am start -n com.digitalgreen.farmerchat/.MainActivity

# Check app logs for troubleshooting:
adb logcat | grep -E "FarmerChat|ApiChat|NetworkConfig"
```

#### Individual Commands (for specific needs)
```bash
# Build the project
./gradlew build

# Build debug APK only
./gradlew assembleDebug

# Install already built APK
./gradlew installDebug

# Clean build artifacts
./gradlew clean

# Build release variant
./gradlew assembleRelease

# Run app after installation (if not auto-started)
adb shell am start -n com.digitalgreen.farmerchat/.MainActivity
```

#### Android App Management
```bash
# Force stop the app (kill running instance)
adb shell am force-stop com.digitalgreen.farmerchat

# Clear app data (complete reset - removes all stored data)
adb shell pm clear com.digitalgreen.farmerchat

# Uninstall the app
adb uninstall com.digitalgreen.farmerchat

# Check if app is installed
adb shell pm list packages | grep farmerchat

# Launch the app
adb shell am start -n com.digitalgreen.farmerchat/.MainActivity

# One-liner to kill app, rebuild, and redeploy
adb shell am force-stop com.digitalgreen.farmerchat && ./gradlew clean assembleDebug && ./gradlew installDebug

# Complete reset: Clear data, rebuild, and launch
adb shell pm clear com.digitalgreen.farmerchat && ./gradlew clean assembleDebug && ./gradlew installDebug && adb shell am start -n com.digitalgreen.farmerchat/.MainActivity
```

#### Emulator Management
```bash
# List available emulators
emulator -list-avds

# Start specific emulator
emulator -avd <emulator_name>

# Start default emulator in background
emulator -avd $(emulator -list-avds | head -1) &

# Check if emulator is running
adb devices

# Wait for emulator to be ready
adb wait-for-device
```

#### 🔧 Complete Development Workflow (Full Testing Setup)
```bash
# 1. Start backend (in one terminal)
cd backend
lsof -ti:3004 | xargs kill -9 2>/dev/null || true
npm run dev

# 2. Start emulator (in another terminal)
emulator -avd $(emulator -list-avds | head -1) &
adb wait-for-device

# 3. Build and deploy app (in main terminal)
./gradlew clean assembleDebug && ./gradlew installDebug

# 4. Verify everything is working
echo "🔍 System Status Check:"
echo "Backend:" && curl -s http://localhost:3004/health | jq .status 2>/dev/null || echo "❌ Backend down"
echo "App:" && adb shell pm list packages | grep farmerchat && echo "✅ App installed" || echo "❌ App not installed"
echo "Logs:" && adb logcat | grep -E "FarmerChat|ApiChat|NetworkConfig" | head -5
```

#### 📋 Quick Status Check (Use Before Testing)
```bash
# One-liner to check everything is ready for testing
echo "🚀 Pre-test Status:" && \
echo "Backend: $(curl -s http://localhost:3004/health | jq -r .status 2>/dev/null || echo '❌ Down')" && \
echo "App: $(adb shell pm list packages | grep farmerchat > /dev/null && echo '✅ Installed' || echo '❌ Not installed')" && \
echo "Device: $(adb devices | grep -v List | wc -l | tr -d ' ') connected"
```

#### Quick Development Commands (One-Liners)

```bash
# Kill backend port and restart server
lsof -ti:3004 | xargs kill -9 2>/dev/null || true && cd backend && npm run dev

# Kill app, clean build, and redeploy
adb shell am force-stop com.digitalgreen.farmerchat && ./gradlew clean assembleDebug && ./gradlew installDebug

# Complete fresh start (clear data, rebuild, launch)
adb shell pm clear com.digitalgreen.farmerchat && ./gradlew clean assembleDebug && ./gradlew installDebug && adb shell am start -n com.digitalgreen.farmerchat/.MainActivity

# Backend + App restart (run in separate terminals)
# Terminal 1:
lsof -ti:3004 | xargs kill -9 2>/dev/null || true && cd backend && npm run dev
# Terminal 2:
adb shell am force-stop com.digitalgreen.farmerchat && ./gradlew clean assembleDebug && ./gradlew installDebug

# Check all services status
echo "Backend:" && curl -s http://localhost:3004/health || echo "Not running" && echo "App:" && adb shell pm list packages | grep farmerchat || echo "Not installed"
```

#### Troubleshooting Process Conflicts

```bash
# Find all Node.js processes
ps aux | grep node

# Find what's using port 3004
lsof -i:3004

# Kill all Node.js processes (use with caution)
pkill -9 node

# Find and kill specific backend process
ps aux | grep "backend" | grep -v grep | awk '{print $2}' | xargs kill -9

# Check Android app process
adb shell ps | grep farmerchat

# Complete system cleanup
pkill -9 node && adb shell am force-stop com.digitalgreen.farmerchat && adb shell pm clear com.digitalgreen.farmerchat
```

### 🧪 Testing the App

#### ⚡ Quick Test After Changes (DEFAULT TESTING FLOW)
```bash
# STEP 1: Ensure backend is running (CRITICAL!)
curl -s http://localhost:3004/health || echo "⚠️ Backend down! Run: ./start-backend.sh"

# STEP 2: Build and deploy app
./gradlew clean assembleDebug && ./gradlew installDebug

# STEP 3: Launch app (should auto-launch, but force if needed)
adb shell am start -n com.digitalgreen.farmerchat/.MainActivity

# STEP 4: Monitor for issues
adb logcat | grep -E "FarmerChat|ApiChat|NetworkConfig|JWT"
```

#### 🎯 Audio Recording Feature Testing (NEW)
```bash
# Test the new audio recording → transcription workflow:
# 1. Navigate to any chat conversation
# 2. Tap the microphone button 
# 3. Should show AudioRecordingView with record button
# 4. Record audio → shows waveform visualization
# 5. Stop recording → can play/discard/send for transcription
# 6. Send → should transcribe via OpenAI Whisper and populate text field

# Check audio transcription logs:
adb logcat | grep -E "AudioRecording|transcribe|Whisper"
```

#### Common Issues and Solutions

1. **"No auth token available" Error**:
   - **Cause**: Backend not running when app started
   - **Solution**: 
     ```bash
     # 1. Start backend first
     ./start-backend.sh
     
     # 2. Clear app data and restart
     adb shell pm clear com.digitalgreen.farmerchat
     ./gradlew clean assembleDebug && ./gradlew installDebug
     ```

2. **"Connection refused" Error**:
   - **Cause**: Backend not running or wrong port
   - **Solution**: Ensure backend is on port 3004
   - **Check**: `lsof -i:3004` should show node process

3. **Chat not loading/Settings empty**:
   - **Cause**: No authentication token
   - **Solution**: Clear app data and restart with backend running

4. **Onboarding appears repeatedly**:
   - **Cause**: JWT token not persisting
   - **Solution**: Already fixed - tokens now persist using DataStore

#### Testing Checklist
1. **First Launch** (with backend running):
   - Language selection appears
   - Complete onboarding (select language, crops, livestock)
   - Redirected to conversations list
   - Backend shows: `POST /api/v1/auth/verify` success

2. **Chat Functionality**:
   - Tap "+" to create new conversation
   - Starter questions load
   - Tap a question - should get OpenAI (gpt-4o-mini) response
   - Follow-up questions appear after response
   - Backend shows: `POST /api/v1/chat/send` requests
   - WebSocket connects automatically for streaming

3. **Phone Auth** (after first conversation):
   - Phone auth prompt appears
   - Can enter phone number or skip
   - OTP screen works (currently mock implementation)

4. **Token Persistence**:
   - Force close app: `adb shell am force-stop com.digitalgreen.farmerchat`
   - Reopen: `adb shell am start -n com.digitalgreen.farmerchat/.MainActivity`
   - Should NOT see onboarding again
   - Should load conversations directly

5. **Settings**:
   - All settings load properly
   - Can edit name, location, language
   - Can change crops/livestock selections
   - Backend shows: `GET /api/v1/user/profile` requests

#### Unit Testing
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

#### Debugging Commands
```bash
# View all logs
adb logcat

# View app-specific logs
adb logcat | grep -E "FarmerChat|ApiChat|NetworkConfig|JWT"

# Clear app data (forces fresh install)
adb shell pm clear com.digitalgreen.farmerchat

# Check network requests
adb logcat | grep "HTTP"

# Monitor backend requests (check backend terminal)
# You should see requests to:
# - POST /api/v1/auth/verify (authentication)
# - GET /api/v1/conversations (list conversations)
# - POST /api/v1/chat/send (send messages)
# - GET /api/v1/users/profile (user profile)
# - WebSocket connections on port 3004
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

### Database Management

#### 🚨 CRITICAL DATABASE RULES 🚨
**NEVER DELETE OR TRUNCATE TRANSLATION/MASTER DATA TABLES:**
- `crop_translations` - Contains 2915+ crop translations
- `livestock_translations` - Contains 1080+ livestock translations  
- `ui_translations` - Contains 10335+ UI translations
- `translation_metadata` - Contains 53+ language metadata
- `starter_questions`, `prompts`, `prompt_versions` - System data

**ONLY CLEAR USER-SPECIFIC DATA WHEN NEEDED:**
```sql
-- Clear specific user data (preferred)
DELETE FROM users WHERE phone = 'specific_phone_number';
DELETE FROM messages WHERE user_id = 'specific_user_id';
DELETE FROM conversations WHERE user_id = 'specific_user_id';

-- NEVER use TRUNCATE on translation tables!
```

#### Note on Prompts
As of June 21, 2025, the backend no longer requires seeded prompts. All prompts (system, starter questions, follow-up questions, and titles) are generated dynamically based on user profile and context. No seed scripts needed!

### Firebase Deployment (Legacy)
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
- **OpenAI** integration in backend `AIService` and frontend `ChatViewModel`
- Streaming responses for real-time UI updates via WebSocket
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
- OpenAI API key configured in backend `.env` file
- Multiple provider keys supported but only OpenAI currently used
- Keys loaded via environment variables, never committed to version control

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

## Backend Architecture (Node.js + Supabase)

### Project Structure
```
FarmerChat/
├── backend/                      # Node.js backend
│   ├── src/
│   │   ├── config/              # Environment configuration
│   │   ├── controllers/         # Request handlers
│   │   ├── services/            # Business logic
│   │   ├── models/              # Data models
│   │   ├── routes/              # API routes
│   │   ├── middleware/          # Express middleware
│   │   ├── utils/               # Utility functions
│   │   ├── types/               # TypeScript types
│   │   └── database/            # Database utilities
│   ├── database/
│   │   └── schema.sql           # Complete database schema
│   ├── package.json
│   ├── tsconfig.json
│   └── README.md
├── app/                         # Android app
└── FarmerChat-Translation-Tools/ # Translation utilities

### Backend Features
- **OpenAI Integration**: Currently using **OpenAI gpt-4o-mini** as the only enabled AI provider
- **Multi-Provider Architecture**: Backend supports multiple AI providers (OpenAI, Gemini, Anthropic) but only OpenAI is currently enabled
- **Neon PostgreSQL**: Serverless PostgreSQL database (replaced Supabase)
- **WebSocket Support**: Real-time streaming of AI responses via Socket.IO
- **Redis Caching**: High-performance caching for translations and responses  
- **Firebase Auth Integration**: Phone OTP authentication with JWT tokens
- **Hybrid Architecture**: Gradual migration from Firebase to Node.js backend
- **Rate Limiting**: Configurable limits per endpoint
- **Admin APIs**: For translation and prompt management

### Running the Backend
```bash
cd backend
npm install
cp .env.example .env  # Configure your environment
npm run dev           # Start development server
```

### Key Backend Services
1. **AIService**: OpenAI integration with streaming support (multi-provider architecture available but only OpenAI enabled)
2. **CacheService**: Redis-based caching with TTL support
3. **TranslationService**: Dynamic translation management
4. **AuthService**: Firebase Auth integration with JWT tokens
5. **ConversationService**: Chat session management
6. **WebSocket Handler**: Real-time streaming via Socket.IO
7. **NotificationService**: FCM push notifications

### Android Integration
- **ApiRepository**: New repository using Retrofit for API communication
- **HybridViewModels**: Support both Firebase and API backends during migration
- **MigrationManager**: Handles gradual transition from Firebase to API
- **WebSocket Client**: Real-time chat streaming with Socket.IO
- **Model Adapters**: Seamless conversion between Firebase and API data structures

### API Endpoints
- **Auth**: `/api/v1/auth/*` - Authentication endpoints
- **Chat**: `/api/v1/chat/*` - Chat and AI interaction
- **Conversations**: `/api/v1/conversations/*` - Conversation management
- **Translations**: `/api/v1/translations/*` - Translation APIs
- **Admin**: `/api/v1/admin/*` - Admin management endpoints

## Latest Chat Interface Improvements

### Language-Aware Message Bubbles
- **RTL Support** - Message bubbles automatically align based on language direction
- **MessageBubbleV2 Component** - New component with built-in RTL support
- **Intelligent Alignment** - LTR languages (English, Spanish) align left, RTL languages (Arabic, Hebrew) align right
- **Space Optimization** - Reduced bubble width to 80% for better space utilization

### Enhanced Starter Questions
- **Dynamic Generation** - AI-powered generation based on user's crops and livestock
- **Context-Aware** - Questions specific to user's agricultural interests
- **Seasonally Relevant** - Questions consider current month for seasonal advice
- **Character Limits** - Questions limited to 60 characters for better readability
- **Loading States** - Visual feedback while questions are being generated
- **Error Handling** - Graceful fallback to repository questions if generation fails

### Navigation Flow Improvements
- **Direct to Chat** - After onboarding, users go directly to chat screen
- **Skip Empty List** - No more empty conversations screen after setup
- **Immediate Conversation Creation** - New conversation created automatically
- **Parameter Passing** - Navigation supports `startNewChat` parameter

### Localization Enhancements
- **Display-Time Localization** - Placeholder text localized when displayed, not stored
- **New String Keys** - Added START_A_CONVERSATION, RESET, RESET_ONBOARDING_CONFIRM
- **AndroidViewModel Pattern** - ConversationsViewModel uses AndroidViewModel for context access
- **StringProvider** - Non-composable string access for ViewModels

### User Experience Updates
- **Reset Confirmation** - Onboarding reset now shows confirmation dialog
- **Always Visible Follow-ups** - Follow-up questions always displayed after AI response
- **Shorter Follow-ups** - Follow-up questions limited to 40 characters
- **Localized Placeholders** - "Start a conversation..." text properly localized

### Implementation Patterns
- **RTL Language Detection**:
  ```kotlin
  val isRtlLanguage = LanguageManager.getLanguageByCode(currentLanguageCode)?.isRTL ?: false
  CompositionLocalProvider(
      LocalLayoutDirection provides if (isRtlLanguage) LayoutDirection.Rtl else LayoutDirection.Ltr
  )
  ```

- **Dynamic Localization**:
  ```kotlin
  // Check for placeholder text at display time
  if (conversation.lastMessage == "Start a conversation..." || 
      conversation.lastMessage == "बातचीत शुरू करें..." ||
      conversation.lastMessage == "Anza mazungumzo...") {
      localizedString(StringKey.START_A_CONVERSATION)
  }
  ```

- **Context-Aware Prompts**:
  ```kotlin
  appendLine("1. Generate questions in ${language?.englishName ?: "English"} language ONLY")
  appendLine("2. Each question must be SHORT and CONCISE (maximum 60 characters)")
  appendLine("3. Questions MUST be specific to the farmer's crops/livestock listed above")
  appendLine("4. Questions should be seasonally relevant for $currentMonth")
  ```

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

## Backend Migration to Node.js + Supabase

### Migration Overview
The app is being migrated from direct Firebase integration to a Node.js backend with Supabase database. This provides:
- Complete control over business logic
- Flexible AI model configuration
- Centralized translation management
- Better security and scalability

### New Architecture
```
Android App (Kotlin)
    ↓ REST API + WebSocket
Backend (Node.js + TypeScript)
    ├── Supabase (Database + Auth)
    ├── AI Service (Multi-provider)
    └── Translation System
```

### Backend Technology Stack
```json
{
  "dependencies": {
    // Core
    "express": "^4.18",
    "typescript": "^5.0",
    
    // Database & Auth
    "@supabase/supabase-js": "^2.0",
    "twilio": "^4.0",  // For SMS OTP
    
    // AI Providers
    "@anthropic-ai/sdk": "^0.20",
    "openai": "^4.0",
    "@google/generative-ai": "^0.1",
    
    // Utilities
    "zod": "^3.0",
    "winston": "^3.0",
    "bull": "^4.0",
    "redis": "^4.0",
    "socket.io": "^4.0"
  }
}
```

### Latest AI Models Configuration (2025)

#### OpenAI Models
```env
# GPT-4.1 Family (Newest - 1M context, June 2024 knowledge)
OPENAI_MODEL_GPT41=gpt-4.1
OPENAI_MODEL_GPT41_MINI=gpt-4.1-mini
OPENAI_MODEL_GPT41_NANO=gpt-4.1-nano

# GPT-4o Family (Multimodal)
OPENAI_MODEL_GPT4O=gpt-4o
OPENAI_MODEL_GPT4O_MINI=gpt-4o-mini
OPENAI_MODEL_GPT4O_REALTIME=gpt-4o-realtime

# Reasoning Models
OPENAI_MODEL_O1_PREVIEW=o1-preview
OPENAI_MODEL_O1_MINI=o1-mini
OPENAI_MODEL_O3_MINI=o3-mini

# API Pricing (per million tokens)
# GPT-4o: $3 input / $10 output
# GPT-4o-mini: $0.15 input / $0.60 output
# GPT-4o-realtime: $5 input / $20 output (text)
```

#### Google Gemini Models
```env
# Gemini 2.5 Family (Latest - 2025)
GOOGLE_MODEL_GEMINI_25_PRO=gemini-2.5-pro
GOOGLE_MODEL_GEMINI_25_FLASH=gemini-2.5-flash-preview

# Gemini 2.0 Family
GOOGLE_MODEL_GEMINI_20_FLASH=gemini-2.0-flash

# API Pricing (per million tokens)
# Gemini 2.5 Pro: $1.25 input / $10 output (up to 200k tokens)
# Gemini 2.5 Pro: $2.50 input / $15 output (over 200k tokens)
# Gemini 2.0 Flash: $0.10 input / $0.40 output
```

#### Anthropic Claude Models
```env
# Claude 4 Family (Latest - May 2025)
ANTHROPIC_MODEL_CLAUDE4_OPUS=claude-4-opus
ANTHROPIC_MODEL_CLAUDE4_SONNET=claude-4-sonnet

# Features: 200K context, hybrid modes (instant/extended thinking)
# API Pricing (per million tokens)
# Claude 4 Opus: $15 input / $75 output
# Claude 4 Sonnet: $3 input / $15 output
```

### Environment Configuration Template
```env
# Current AI Configuration (ACTIVE)
DEFAULT_AI_PROVIDER=openai
AI_PROVIDERS_ENABLED=openai
OPENAI_MODEL=gpt-4o-mini

# Model Selection Strategy
MODEL_SELECTION_STRATEGY=cost-optimized
# Options: primary-fallback, round-robin, cost-optimized, performance-based

# AI Provider Keys
ANTHROPIC_API_KEY=your_key_here
OPENAI_API_KEY=your_key_here
GOOGLE_AI_API_KEY=your_key_here

# Model Parameters
AI_MAX_TOKENS=4000
AI_TEMPERATURE=0.7
AI_STREAMING_ENABLED=true

# Rate Limiting
RATE_LIMIT_PER_USER_HOUR=100
RATE_LIMIT_AI_PER_DAY=1000
```

### Prompt Management System

#### Database Schema
```sql
-- Flexible prompt templates
CREATE TABLE prompt_templates (
    id UUID PRIMARY KEY,
    key TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    template TEXT NOT NULL,
    variables JSONB DEFAULT '[]',
    version INTEGER DEFAULT 1,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Model-specific overrides
CREATE TABLE model_prompt_overrides (
    id UUID PRIMARY KEY,
    template_id UUID REFERENCES prompt_templates(id),
    model_provider TEXT NOT NULL,
    model_name TEXT NOT NULL,
    override_template TEXT NOT NULL
);
```

### Android Migration Changes

#### Remove Firebase Dependencies
```kotlin
// Remove from build.gradle.kts
implementation("com.google.firebase:firebase-auth")
implementation("com.google.firebase:firebase-firestore")

// Keep only for push notifications
implementation("com.google.firebase:firebase-messaging")
```

#### New API Client Structure
```kotlin
interface FarmerChatAPI {
    // Auth endpoints
    @POST("auth/send-otp")
    suspend fun sendOTP(@Body request: OTPRequest): Response<OTPResponse>
    
    @POST("auth/verify-otp")
    suspend fun verifyOTP(@Body request: VerifyOTPRequest): Response<AuthToken>
    
    // Conversation endpoints
    @GET("conversations")
    suspend fun getConversations(): Response<List<Conversation>>
    
    @POST("conversations")
    suspend fun createConversation(): Response<Conversation>
    
    // Message endpoints with streaming
    @POST("messages/stream")
    @Streaming
    suspend fun streamMessage(@Body request: MessageRequest): Response<ResponseBody>
    
    // Translation endpoints
    @GET("translations/{language}")
    suspend fun getTranslations(@Path("language") language: String): Response<TranslationBundle>
}
```

### Migration Timeline
1. **Backend Setup** (Week 1-2)
   - Node.js project structure
   - Supabase integration
   - Authentication system
   
2. **Core Features** (Week 2-3)
   - AI provider integration
   - Prompt management
   - Translation APIs
   
3. **Android Updates** (Week 3-4)
   - Remove Firebase code
   - Implement API client
   - Update ViewModels
   
4. **Testing & Deployment** (Week 4-5)
   - End-to-end testing
   - Performance optimization
   - Production deployment

### Key Benefits
- **AI Flexibility**: Switch between models based on cost/performance
- **Prompt A/B Testing**: Test different prompts and track performance
- **Centralized Translations**: Manage all translations from backend
- **Better Error Handling**: Consistent error responses across platforms
- **Cost Optimization**: Route queries to appropriate models based on complexity

## 🚀 Future Architecture Plans (Post-MVP)

### Regional Feature Modules with App Bundles

**Target**: After MVP completion, implement dynamic feature delivery based on user regions.

#### Why App Bundles?
- Single app listing on Play Store
- Region-specific features delivered dynamically
- Reduced app size (users only get relevant features)
- Better maintenance and A/B testing capabilities

#### Implementation Strategy

1. **Dynamic Feature Modules Structure**
```kotlin
// app/build.gradle.kts
android {
    dynamicFeatures = listOf(
        ":features:ethiopia",
        ":features:kenya", 
        ":features:india",
        ":features:market_prices",
        ":features:government_schemes",
        ":features:weather_alerts"
    )
}
```

2. **Conditional Delivery Configuration**
```xml
<!-- Example: Ethiopia-specific features -->
<dist:module dist:title="@string/ethiopia_features">
    <dist:delivery>
        <dist:install-time>
            <dist:conditions>
                <dist:user-countries dist:include="true">
                    <dist:country dist:code="ET"/>
                </dist:user-countries>
            </dist:conditions>
        </dist:install-time>
    </dist:delivery>
</dist:module>
```

3. **Feature Examples by Region**
- **East Africa (KE, UG, TZ)**: Market prices, weather alerts, cooperative features
- **India (IN)**: Government schemes, MSP prices, Kisan credit features  
- **Ethiopia (ET)**: Local language support, specific crop calendars
- **West Africa**: French language priority, ECOWAS trade features

#### API Versioning Strategy

**Required Backend Changes**:

1. **Versioned Endpoints**
```
/api/v1/chat/send         (current MVP)
/api/v2/chat/send         (with regional context)
/api/v2/india/schemes     (India-specific)
/api/v2/kenya/markets     (Kenya-specific)
```

2. **Region-Aware Middleware**
```typescript
// Detect region from request headers
app.use((req, res, next) => {
    req.region = req.headers['x-user-region'] || 'global';
    req.apiVersion = req.path.split('/')[2]; // v1, v2, etc
    next();
});
```

3. **Backward Compatibility Approach**
- Keep v1 endpoints frozen after MVP
- All new features go to v2 with region support
- Deprecation notices with 6-month timeline
- Feature flags for gradual rollout

4. **Database Schema Evolution**
```sql
-- Add region-specific tables
CREATE TABLE regional_features (
    id UUID PRIMARY KEY,
    region_code VARCHAR(2),
    feature_name VARCHAR(100),
    is_enabled BOOLEAN DEFAULT true
);

-- Region-specific data
CREATE TABLE market_prices_kenya (
    -- Kenya-specific market data
);

CREATE TABLE gov_schemes_india (
    -- India-specific government schemes
);
```

#### Challenges to Address
1. **API Complexity**: Multiple versions and regions increase complexity
2. **Testing**: Need comprehensive testing for each region/version combo
3. **Data Sync**: Keeping regional data synchronized
4. **Rollback Strategy**: How to handle feature rollbacks per region
5. **Analytics**: Tracking usage across different feature modules
6. **Offline Support**: Ensuring features work offline

#### Migration Timeline (Post-MVP)
1. **Phase 1**: API v2 architecture (2 weeks)
2. **Phase 2**: App Bundle setup (1 week)  
3. **Phase 3**: First regional module (2 weeks)
4. **Phase 4**: Testing and rollout (2 weeks)
5. **Phase 5**: Additional regions (ongoing)

#### Success Metrics
- App size reduction: Target 40-60% smaller for most users
- Feature adoption: Track usage per region
- Load time: Faster initial app launch
- Crash rate: Monitor per feature module

### Note: This is a POST-MVP initiative. Current focus remains on core functionality.