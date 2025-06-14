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

### Key Components
```
FarmerChat/
├── app/
│   └── src/main/java/com/digitalgreen/farmerchat/
│       ├── MainActivity.kt
│       ├── data/
│       │   ├── FarmerChatRepository.kt
│       │   └── Models.kt
│       ├── navigation/
│       │   └── Navigation.kt
│       ├── ui/
│       │   ├── components/
│       │   │   ├── FeedbackDialog.kt
│       │   │   ├── MessageBubble.kt
│       │   │   └── VoiceRecordingButton.kt
│       │   ├── screens/
│       │   │   ├── SplashScreen.kt
│       │   │   ├── OnboardingScreen.kt
│       │   │   ├── ConversationsScreen.kt
│       │   │   └── ChatScreen.kt
│       │   └── theme/
│       └── utils/
│           ├── PreferencesManager.kt
│           ├── TextToSpeechManager.kt
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
- Options: English, हिन्दी, Kiswahili, తెలుగు
- Stores preference locally

**Screen 2: Location Input**
- Text field for location entry
- Helps provide localized advice

**Screen 3: Crop Selection**
- Multi-select chips
- Options: Potato, Sugarcane, Cotton, Maize, Tomato, Rice, Onion, Wheat

**Screen 4: Livestock Selection**
- Multi-select chips
- Options: Chicken, Goat, Sheep, Buffalo, Dairy Cow, Pig

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
2. **Personalized Starter Questions**: Based on selected crops and livestock
3. **Voice Input**: Record questions using voice
4. **Text-to-Speech**: Listen to AI responses
5. **Intelligent Conversation Titles**: AI-generated summaries of conversations
6. **Follow-up Questions**: Context-aware suggestions after each response
7. **Feedback System**: Users can rate and comment on AI responses
8. **Multi-language Support**: UI in multiple languages (content in English)

### AI Features
- Context-aware responses based on user profile
- Location-specific agricultural advice
- Crop and livestock-specific guidance
- Follow-up question generation
- Intelligent conversation summarization

## Database Structure

### Firestore Collections

#### 1. `users` Collection
```javascript
{
  userId: string,           // Firebase Auth UID
  name: string,            // User's name
  language: string,        // Selected language (en, hi, sw, te)
  location: string,        // User's location
  crops: string[],         // Selected crops
  livestock: string[],     // Selected livestock
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
You are an AI assistant helping smallholder farmers with agricultural advice.

User Profile:
- Language preference: ${profile?.language ?: "en"}
- Location: ${profile?.location ?: "Unknown"}
- Crops: ${profile?.crops?.joinToString(", ") ?: "None specified"}
- Livestock: ${profile?.livestock?.joinToString(", ") ?: "None specified"}

User Query: $userQuery

Please provide:
1. A helpful, practical response tailored to their context
2. Keep the response concise and easy to understand
3. If relevant, mention local conditions or practices

IMPORTANT: You MUST end your response with exactly this format:

FOLLOW_UP_QUESTIONS:
Question 1|Question 2|Question 3

The follow-up questions line MUST start with "FOLLOW_UP_QUESTIONS:" and have 2-3 questions separated by "|"
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

#### TextToSpeechManager
- Converts text to speech
- Manages TTS lifecycle

#### SpeechRecognitionManager
- Handles voice input
- Converts speech to text

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
1. Add language option in `OnboardingScreen`
2. Update `StarterQuestionSeeder` with translated questions
3. Add language-specific prompts in AI integration

### Adding New Crops/Livestock
1. Update options in `OnboardingScreen`
2. Add relevant starter questions in database
3. Update AI prompts to handle new categories

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

## Future Enhancements
1. Offline support with local caching
2. Image-based disease detection
3. Weather integration
4. Market price information
5. Community features
6. Expert consultation booking