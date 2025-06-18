# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 🚨 Firebase to Node.js/Neon PostgreSQL Migration Status

### ✅ Migration Successfully Completed!

**Architecture**: Successfully migrated from Firebase-only to **Node.js + Neon PostgreSQL** backend with Firebase Auth for phone OTP only.

**Status**: 
- ✅ Backend: Node.js + Express + TypeScript running on port 3004
- ✅ Database: Neon PostgreSQL (Project: `spring-flower-04114371`) 
- ✅ API Endpoints: Complete RESTful API with full functionality
- ✅ Authentication: Firebase → Backend JWT exchange working perfectly
- ✅ **Token Authorization**: JWT tokens properly attached to all requests
- ✅ **Chat Functionality**: Messages send/receive working with OpenAI integration
- ✅ **Settings**: Profile data loads and updates correctly
- ✅ **Starter Questions**: Load and are clickable
- ✅ **Follow-up Questions**: Generated and displayed after AI responses
- ✅ **Conversation Management**: Create, list, and update conversations

**AI Configuration**: Using OpenAI gpt-4o-mini model (configurable in backend .env)

**Key Fixes Applied**:
1. Fixed auth interceptor to properly attach Bearer tokens
2. Updated API routes to match expected endpoints
3. Made model fields nullable to handle backend responses
4. Switched from WebSocket to HTTP for message sending (`/api/v1/chat/send`)
5. Optimized settings loading with cache-first approach
6. Updated `SendMessageResponse` model to match backend response format

**Implementation Details**:
- Using HTTP endpoint `/api/v1/chat/send` for messaging (not WebSocket)
- Backend supports both HTTP and WebSocket streaming (future enhancement)
- Optimistic UI updates for better user experience
- Comprehensive debug logging throughout the flow

**Important**: App is now fully functional with the new backend architecture.

## Common Development Commands

### Backend Server Management
```bash
# IMPORTANT: Always check and kill processes on port 3004 before starting
lsof -ti:3004 | xargs kill -9 2>/dev/null || true

# Start the backend server (always on port 3004)
cd backend
npm run dev

# The server MUST run on port 3004 to avoid port conflicts
# Never use ports 3000, 3002, etc. - always use 3004
```

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
- **Multi-AI Provider Support**: Seamlessly switch between Gemini, OpenAI, and Anthropic
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
1. **AIService**: Handles multiple AI providers with streaming support
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
# Default AI Configuration
DEFAULT_AI_PROVIDER=anthropic
DEFAULT_AI_MODEL=claude-4-sonnet
FALLBACK_AI_PROVIDER=google
FALLBACK_AI_MODEL=gemini-2.0-flash

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