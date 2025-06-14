# FarmerChat - Complete Application Documentation

## Overview

FarmerChat is an AI-powered Android application designed to help smallholder farmers with agricultural advice. The app provides personalized farming guidance based on user profiles, location, crops, and livestock preferences.

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [User Interface](#user-interface)
3. [Features](#features)
4. [Database Structure](#database-structure)
5. [Firebase Configuration](#firebase-configuration)
6. [AI Integration](#ai-integration)
7. [Technical Implementation](#technical-implementation)
8. [Setup Instructions](#setup-instructions)

## Architecture Overview

### Technology Stack
- **Frontend**: Android (Kotlin) with Jetpack Compose
- **Backend**: Firebase (Firestore, Authentication)
- **AI**: Google Gemini 1.5 Flash
- **Architecture Pattern**: MVVM (Model-View-ViewModel)
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Location Services**: Google Play Services Location API
- **Speech Recognition**: Android SpeechRecognizer API
- **Text-to-Speech**: Android TextToSpeech API

### Key Components
```
FarmerChat/
├── app/
│   └── src/main/java/com/digitalgreen/farmerchat/
│       ├── MainActivity.kt
│       ├── data/
│       │   ├── FarmerChatRepository.kt
│       │   ├── Models.kt
│       │   ├── LanguageManager.kt
│       │   ├── LocationInfo.kt
│       │   ├── CropsManager.kt
│       │   ├── LivestockManager.kt
│       │   ├── CropTranslations.kt
│       │   └── LivestockTranslations.kt
│       ├── navigation/
│       │   └── Navigation.kt
│       ├── ui/
│       │   ├── components/
│       │   │   ├── FeedbackDialog.kt
│       │   │   ├── MessageBubble.kt
│       │   │   ├── VoiceRecordingButton.kt
│       │   │   └── LocalizationUtils.kt
│       │   ├── screens/
│       │   │   ├── SplashScreen.kt
│       │   │   ├── OnboardingScreen.kt
│       │   │   ├── ConversationsScreen.kt
│       │   │   └── ChatScreen.kt
│       │   └── theme/
│       └── utils/
│           ├── PreferencesManager.kt
│           ├── TextToSpeechManager.kt
│           ├── SpeechRecognitionManager.kt
│           ├── LocationManager.kt
│           ├── StringsManager.kt
│           └── StarterQuestionSeeder.kt
├── firebase.json
├── firestore.rules
├── firestore.indexes.json
└── seed-database.js
```

## User Interface

### 1. Splash Screen
- Shows app logo and name
- Checks authentication status
- Routes to appropriate screen

### 2. Onboarding Flow
**Screen 1: Language Selection**
- Global language support with 50+ languages
- Agricultural priority languages prominently displayed
- Search functionality to find any language
- Stores preference for entire app experience

**Screen 2: Location Detection**
- Automatic GPS-based location detection
- Reverse geocoding to get address hierarchy
- Manual location entry as fallback option
- Captures country, state, district, and locality

**Screen 3: Crop Selection**
- Categorized crop selection (Cereals, Vegetables, Fruits, etc.)
- 50+ crops with multilingual names
- Search functionality across all translations
- Category filters for easy navigation
- Shows scientific names and emojis

**Screen 4: Livestock Selection**
- Categorized livestock selection (Cattle, Poultry, etc.)
- 20+ animals with multilingual names
- Search functionality across all translations
- Shows primary purposes (Dairy, Meat, Eggs, etc.)
- Category filters for easy navigation

### 3. Conversations Screen
- Lists all user conversations
- Shows conversation title, last message preview, and timestamp
- Floating Action Button to create new conversation
- Search and settings options in app bar

### 4. Chat Screen
- Real-time messaging interface
- Voice recording capability
- Text-to-speech for AI responses
- Feedback system for messages
- Intelligent follow-up questions
- Starter questions based on user profile

## Features

### Core Features
1. **Anonymous Authentication**: Users are automatically signed in anonymously
2. **Comprehensive Language Support**: 
   - 50+ languages with native names
   - Agricultural priority languages highlighted
   - UI labels and messages in selected language
   - Search across all language names
3. **GPS-Based Location Services**:
   - Automatic location detection with permissions
   - Reverse geocoding for address details
   - Hierarchical location data (country to locality)
   - Manual entry fallback option
4. **Enhanced Voice Features**:
   - Improved voice recognition with visual feedback
   - Language-specific speech recognition
   - Smart voice selection for TTS quality
   - Real-time transcription display
5. **Rich Crop/Livestock Selection**:
   - 50+ crops across 10 categories
   - 20+ livestock across 6 categories
   - Multilingual names with search
   - Scientific names and visual emojis
6. **Personalized Starter Questions**: Based on selected crops and livestock
7. **Intelligent Conversation Titles**: AI-generated summaries of conversations
8. **Follow-up Questions**: Context-aware suggestions after each response
9. **Feedback System**: Users can rate and comment on AI responses
10. **Formatted AI Responses**: Support for bullets, bold, and structured content

### AI Features
- Context-aware responses based on user profile
- Location-specific agricultural advice with GPS precision
- Crop and livestock-specific guidance for 70+ items
- Follow-up question generation with improved formatting
- Intelligent conversation summarization
- Language-aware prompt engineering
- Formatted responses with bullets and emphasis
- Enhanced context injection from user profile

## Database Structure

### Firestore Collections

#### 1. `users` Collection
```javascript
{
  userId: string,           // Firebase Auth UID
  name: string,            // User's name
  language: string,        // Selected language code (50+ options)
  location: string,        // Full formatted location string
  locationDetails: {       // Detailed location hierarchy
    country: string,
    adminArea: string,     // State/Province
    locality: string,      // City/Town
    subLocality: string,   // Area/Neighborhood
    latitude: number,
    longitude: number
  },
  crops: string[],         // Selected crop IDs
  livestock: string[],     // Selected livestock IDs
  createdAt: timestamp,    // Account creation time
  lastUpdated: timestamp   // Last profile update
}
```

#### 2. `conversations` Collection
```javascript
{
  id: string,                  // Conversation ID
  userId: string,              // Owner's UID
  title: string,               // AI-generated title
  lastMessage: string,         // Preview of last message
  lastMessageTime: timestamp,  // Time of last message
  lastMessageIsUser: boolean,  // Whether last message was from user
  createdAt: timestamp,        // Conversation creation time
  unreadCount: number,         // Unread message count
  hasUnreadMessages: boolean   // Quick unread check
}
```

#### 3. `chat_sessions` Collection
```javascript
{
  sessionId: string,       // Session/Conversation ID
  userId: string,          // Owner's UID
  createdAt: timestamp,    // Session creation time
  lastUpdated: timestamp   // Last activity time
}
```

#### 4. `chat_sessions/{sessionId}/messages` Subcollection
```javascript
{
  id: string,              // Message ID
  content: string,         // Message text
  isUser: boolean,         // true if from user, false if AI
  timestamp: timestamp,    // Message time
  audioUrl: string?,       // Optional audio URL
  isVoiceMessage: boolean, // Whether originated from voice
  user: boolean,           // Duplicate of isUser (legacy)
  voiceMessage: boolean    // Duplicate of isVoiceMessage (legacy)
}
```

#### 5. `starter_questions` Collection
```javascript
{
  id: string,              // Question ID
  language: string,        // Language code
  category: string,        // general, crops, livestock
  question: string,        // The question text
  tags: string[],          // Related crops/livestock
  priority: number         // Display order
}
```

#### 6. `feedback` Collection
```javascript
{
  id: string,              // Feedback ID
  sessionId: string,       // Related conversation
  messageId: string,       // Related message
  userId: string,          // User who gave feedback
  rating: number,          // 1-5 rating
  comment: string,         // Optional comment
  timestamp: timestamp     // Feedback time
}
```

## Firebase Configuration

### Security Rules (firestore.rules)
```javascript
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    // Allow authenticated users to read/write their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Allow authenticated users to read starter questions
    match /starter_questions/{document=**} {
      allow read: if request.auth != null;
    }
    
    // Allow authenticated users to manage their chat sessions
    match /chat_sessions/{sessionId} {
      allow read, write: if request.auth != null && 
        request.auth.uid == resource.data.userId;
      allow create: if request.auth != null && 
        request.auth.uid == request.resource.data.userId;
      
      // Allow access to messages within owned sessions
      match /messages/{messageId} {
        allow read, write: if request.auth != null && 
          get(/databases/$(database)/documents/chat_sessions/$(sessionId)).data.userId == request.auth.uid;
      }
    }
    
    // Allow authenticated users to submit feedback
    match /feedback/{feedbackId} {
      allow create: if request.auth != null && 
        request.auth.uid == request.resource.data.userId;
      allow read: if request.auth != null && 
        request.auth.uid == resource.data.userId;
    }
    
    // Allow authenticated users to manage their conversations
    match /conversations/{conversationId} {
      allow create: if request.auth != null && 
        request.resource.data.userId == request.auth.uid;
      allow read, update, delete: if request.auth != null && 
        resource.data.userId == request.auth.uid;
    }
  }
}
```

### Composite Indexes (firestore.indexes.json)
```json
{
  "indexes": [
    {
      "collectionGroup": "conversations",
      "queryScope": "COLLECTION",
      "fields": [
        {
          "fieldPath": "userId",
          "order": "ASCENDING"
        },
        {
          "fieldPath": "lastMessageTime",
          "order": "DESCENDING"
        }
      ]
    }
  ],
  "fieldOverrides": []
}
```

## AI Integration

### Gemini Configuration
- **Model**: gemini-1.5-flash
- **API Key**: Stored in BuildConfig (from local.properties)

### Prompt Template
```kotlin
"""
You are FarmerChat AI, a helpful agricultural assistant for smallholder farmers.

USER PROFILE:
- Language: ${languageName}
- Location: ${profile?.location ?: "Unknown"}
${locationContext}
- Crops: ${cropNames}
- Livestock: ${livestockNames}

USER'S QUESTION: $userQuery

RESPONSE GUIDELINES:
1. Provide practical, actionable advice specific to their location and context
2. Use simple, clear language that farmers can easily understand
3. When relevant, mention:
   - Local weather patterns and seasons
   - Region-specific farming practices
   - Available local resources
4. Format your response with:
   - Use bullet points (•) for lists
   - Use **bold** for important terms
   - Use clear paragraph breaks

You MUST end your response with EXACTLY this format:

FOLLOW_UP_QUESTIONS:
Question 1|Question 2|Question 3

Generate 2-3 relevant follow-up questions separated by "|" that help the farmer explore the topic further.
"""
```

### Title Generation Prompt
```kotlin
"""
Based on this farming conversation, generate a concise title (2-4 words) that captures the main topic:

User Query: $firstUserQuery
AI Response (excerpt): $firstAiResponse

Generate ONLY the title, nothing else. Examples:
- Rice Pest Control
- Organic Fertilizer Guide
- Tomato Disease Management
- Irrigation Schedule Help
- Wheat Harvest Timing

Title:
"""
```

## Technical Implementation

### Key ViewModels

#### SplashViewModel
- Checks authentication status
- Determines navigation destination
- Handles anonymous sign-in

#### OnboardingViewModel
- Manages user profile creation
- Validates input data
- Saves to Firestore

#### ConversationsViewModel
- Loads user conversations
- Manages real-time updates
- Handles conversation creation/deletion

#### ChatViewModel
- Manages chat messages
- Integrates with Gemini AI
- Handles voice recording/TTS
- Generates conversation titles
- Manages follow-up questions

### Repository Pattern
`FarmerChatRepository` provides a clean interface for:
- User authentication
- Profile management
- Conversation CRUD operations
- Message management
- Feedback collection

### Utilities

#### PreferencesManager
- Stores user preferences locally
- Manages onboarding completion status
- Persists language selection

#### TextToSpeechManager
- Enhanced voice selection algorithm
- Language-specific voice mapping
- Quality-based voice prioritization
- Manages TTS lifecycle

#### SpeechRecognitionManager
- Language-aware speech recognition
- Visual feedback during recording
- Error handling and retry logic
- Converts speech to text with locale support

#### LocationManager
- GPS-based location detection
- Reverse geocoding implementation
- Permission handling
- Formatted location string generation

#### StringsManager
- Centralized string management
- Multi-language string resources
- Dynamic string lookup by key

#### LanguageManager
- Global language database (50+ languages)
- Agricultural priority sorting
- Language search functionality
- Native name display

#### CropsManager & LivestockManager
- Categorized data management
- Multi-language name support
- Search across translations
- Scientific name tracking

## Setup Instructions

### Prerequisites
1. Android Studio Arctic Fox or later
2. JDK 11 or higher
3. Firebase project with Firestore and Authentication enabled
4. Gemini API key

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd FarmerChat
   ```

2. **Configure Firebase**
   - Create a Firebase project
   - Enable Anonymous Authentication
   - Enable Firestore Database
   - Download `google-services.json` and place in `app/` directory

3. **Set up API Keys**
   Create `local.properties` in project root:
   ```properties
   sdk.dir=/path/to/android/sdk
   gemini.api.key=YOUR_GEMINI_API_KEY
   ```

4. **Deploy Firebase Rules**
   ```bash
   npm install -g firebase-tools
   firebase login
   firebase init
   firebase deploy --only firestore:rules,firestore:indexes
   ```

5. **Seed Database (Optional)**
   ```bash
   node seed-database.js
   ```

6. **Build and Run**
   - Open project in Android Studio
   - Sync Gradle files
   - Run on emulator or device

### Environment Variables
- `GEMINI_API_KEY`: Required for AI functionality
- Firebase configuration is embedded in `google-services.json`

## Maintenance

### Adding New Languages
1. Add language to `LanguageManager.languages` list
2. Update `StringsManager` with translations for all UI strings
3. Add translations to `CropTranslations` and `LivestockTranslations`
4. Update `StarterQuestionSeeder` with translated questions
5. Test voice recognition and TTS for the new language

### Adding New Crops/Livestock
1. Add to `CropsManager.crops` or `LivestockManager.livestock` lists
2. Include category, emoji, and metadata
3. Add translations to respective translation files
4. Update starter questions in database
5. Test search functionality with new entries

### Location Services
- Requires ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permissions
- Fallback to manual entry if GPS unavailable
- Stores both formatted string and detailed hierarchy

### Performance Optimization
- Conversations are loaded with Firestore listeners for real-time updates
- Messages are streamed to show progress
- Images are lazy-loaded
- Voice recording uses efficient compression

## Security Considerations
- Anonymous authentication prevents user tracking
- Firestore rules ensure data isolation
- API keys are kept in local configuration
- No personal data is stored without user consent

## Recent Improvements (Latest Update)

### Language System Overhaul
- Migrated from 4 hardcoded languages to 50+ global languages
- Added comprehensive language search functionality
- Implemented agricultural priority sorting
- Full UI localization support

### Location System Enhancement
- Replaced city dropdown with GPS-based detection
- Added reverse geocoding for address hierarchy
- Implemented permission handling with fallbacks
- Captures precise lat/long for weather integration

### Voice Features Upgrade
- Enhanced speech recognition with visual feedback
- Improved TTS voice selection algorithm
- Added language-specific voice mapping
- Real-time transcription display

### Crop/Livestock Management
- Expanded from 8 crops to 50+ categorized crops
- Added 20+ livestock with purpose classification
- Implemented multilingual search across all names
- Added scientific names and visual emojis

### AI Response Improvements
- Enhanced prompt engineering with better context
- Added formatting support (bullets, bold text)
- Improved follow-up question generation
- Better location-aware responses

## Pending Features
1. Settings screen for preference management
2. Conversation search functionality
3. Tagging system for conversations
4. Language confidence scoring for voice input
5. Offline support with local caching
6. Image-based disease detection
7. Weather integration
8. Market price information
9. Community features
10. Expert consultation booking