# FarmerChat Setup Guide

## 📋 Overview

FarmerChat is a multilingual agricultural advisory app with Node.js backend and Android frontend. This guide covers complete setup from backend deployment to Android testing.

## 🏗️ Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Android App   │────│   Node.js API    │────│   Neon Database │
│                 │    │                  │    │                 │
│ • Firebase Auth │    │ • Multi-AI       │    │ • Conversations │
│ • API Client    │◄───┤ • WebSocket      │    │ • Messages      │
│ • Real-time UI  │    │ • Analytics      │    │ • Translations  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## 🔧 Backend Setup

### Prerequisites
- Node.js 18+ 
- PostgreSQL access (Neon)
- OpenAI API key
- Firebase project

### 1. Environment Configuration

```bash
cd backend
cp .env.example .env
```

**Critical Environment Variables:**

```bash
# Database (Already configured)
DATABASE_URL=postgresql://neondb_owner:npg_cA7jzSpEIC5H@ep-dark-bonus-a8qq752e-pooler.eastus2.azure.neon.tech/neondb?sslmode=require
NEON_PROJECT_ID=spring-flower-04114371

# OpenAI API (PRIMARY AI PROVIDER)
OPENAI_API_KEY=sk-proj-YOUR_OPENAI_API_KEY_HERE
OPENAI_MODEL=gpt-4o-mini
DEFAULT_AI_PROVIDER=openai

# Firebase (Authentication)
FIREBASE_PROJECT_ID=farmer-chat-eagle
FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\nYOUR_FIREBASE_PRIVATE_KEY\n-----END PRIVATE KEY-----"
FIREBASE_CLIENT_EMAIL=firebase-adminsdk-xyz@farmer-chat-eagle.iam.gserviceaccount.com

# Security
JWT_SECRET=your_minimum_32_character_secret_key_here
```

### 2. Installation & Startup

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Server will start on http://localhost:3000
# API endpoints: http://localhost:3000/api/v1/
```

### 3. API Key Configuration

**Where to add OpenAI API key:**

1. **Get OpenAI API Key:**
   - Visit: https://platform.openai.com/api-keys
   - Create new API key
   - Copy the key (starts with `sk-proj-...`)

2. **Add to Backend:**
   ```bash
   # In backend/.env file
   OPENAI_API_KEY=sk-proj-your_actual_api_key_here
   DEFAULT_AI_PROVIDER=openai
   ```

3. **Verify Configuration:**
   ```bash
   # Check logs when starting server
   npm run dev
   # Should see: "AI Service initialized with providers: openai"
   ```

## 📱 Android Setup

### Prerequisites
- Android Studio Arctic Fox+
- JDK 11+
- Firebase project access

### 1. Firebase Configuration

```bash
# Download google-services.json from Firebase Console
# Place in: app/google-services.json
```

### 2. Network Configuration

Update for your development environment:

```kotlin
// In app/src/main/java/com/digitalgreen/farmerchat/network/NetworkConfig.kt
private const val BASE_URL = "http://10.0.2.2:3000/api/v1/" // Android Emulator
// For physical device: "http://YOUR_COMPUTER_IP:3000/api/v1/"
```

### 3. Build & Run

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug
```

## 🔗 API Integration

### Authentication Flow
1. **Firebase Auth** - Phone OTP on Android
2. **Token Exchange** - Firebase ID token → JWT
3. **API Access** - JWT bearer token for all requests
4. **WebSocket** - Real-time chat with JWT authentication

### Key Endpoints
```
POST /api/v1/auth/verify          # Firebase token verification
GET  /api/v1/user/profile         # User management
GET  /api/v1/conversations        # Conversation list
POST /api/v1/chat/send            # Send message
WebSocket /                       # Real-time streaming
GET  /api/v1/translations/{lang}  # Multi-language support
```

## 🤖 AI Provider Configuration

### Current Setup: OpenAI Primary

**Models Available:**
- `gpt-4o-mini` (Default - Fast & Cost-effective)
- `gpt-4o` (High-quality responses)
- `gpt-3.5-turbo` (Fallback option)

**Features:**
- **Real-time Streaming** - Chunk-by-chunk response delivery
- **Context Injection** - User profile, location, crops, livestock
- **Follow-up Questions** - AI-generated suggestions
- **Conversation Analytics** - Automatic tagging and summarization
- **Multi-language** - Responses in user's preferred language

### Switching AI Providers

**To use Gemini:**
```bash
DEFAULT_AI_PROVIDER=gemini
GEMINI_API_KEY=your_gemini_key
```

**To use Anthropic:**
```bash
DEFAULT_AI_PROVIDER=anthropic
ANTHROPIC_API_KEY=your_anthropic_key
```

## 🗄️ Database Schema

### Core Tables
```sql
-- Users with profile data
users (id, firebase_uid, phone, name, location, language, crops, livestock)

-- Conversations with analytics
conversations (id, user_id, title, tags, english_tags, summary, last_message)

-- Messages with metadata
messages (id, conversation_id, content, is_user, follow_up_questions)

-- API usage tracking
api_usage (user_id, provider, model, prompt_tokens, completion_tokens)

-- Multi-language translations
ui_translations (key, language_code, translation)
crop_translations (crop_id, language_code, name)
livestock_translations (livestock_id, language_code, name)
```

## 🌍 Multi-language Support

### Supported Languages
- **English** (en) - Base language
- **Hindi** (hi) - हिंदी  
- **Swahili** (sw) - Kiswahili
- **Spanish** (es) - Español
- **French** (fr) - Français
- **Bengali** (bn) - বাংলা

### Translation Features
- **UI Translations** - All interface text
- **Crop/Livestock Names** - Agricultural terminology
- **AI Responses** - Natural language in user's language
- **Voice Features** - Speech recognition and TTS
- **Conversation Tags** - Localized fact extraction

## 🔥 Firebase Configuration

### Required Services
1. **Authentication** - Phone number OTP
2. **Cloud Messaging** - Push notifications (future)

### Setup Steps
1. Create Firebase project: `farmer-chat-eagle`
2. Enable Authentication → Phone
3. Download service account key
4. Add configuration to `.env`

## 🧪 Testing Guide

### 1. Backend Testing
```bash
# Health check
curl http://localhost:3000/health

# API documentation (if Swagger enabled)
curl http://localhost:3000/api-docs
```

### 2. Authentication Testing
```bash
# Test auth endpoint (need Firebase ID token)
curl -X POST http://localhost:3000/api/v1/auth/verify \
  -H "Content-Type: application/json" \
  -d '{"idToken": "FIREBASE_ID_TOKEN"}'
```

### 3. Android Testing
1. **Setup** - Configure Firebase and network URLs
2. **Auth Flow** - Test phone OTP → JWT → API access
3. **Chat Features** - Send messages, receive streaming responses
4. **Voice Features** - Speech-to-text and text-to-speech
5. **Multi-language** - Switch languages, verify translations

## 📊 Monitoring & Analytics

### API Usage Tracking
- **Token Consumption** - Per user, per model
- **Response Times** - API and AI provider latency
- **Error Rates** - Failed requests and AI errors
- **Conversation Metrics** - Tags, summaries, user engagement

### Conversation Analytics
- **Automatic Tagging** - AI extracts agricultural topics
- **Fact Extraction** - Key insights from conversations
- **Localized Tags** - Tags in user's language + English storage
- **Summarization** - AI-generated conversation summaries

## 🚀 Deployment Notes

### Development
- Backend: `npm run dev` (http://localhost:3000)
- Database: Neon PostgreSQL (serverless)
- Android: Debug builds with local API

### Production Considerations
- **Environment Variables** - Secure API key management
- **Database** - Connection pooling and migrations
- **Rate Limiting** - API request throttling
- **Caching** - Redis for translations and responses
- **Monitoring** - Error tracking and performance metrics

## 🔧 Troubleshooting

### Common Issues

**Backend not starting:**
```bash
# Check environment variables
npm run typecheck
# Verify database connection
npm run test:db
```

**Android connection issues:**
```bash
# For emulator, use: http://10.0.2.2:3000/api/v1/
# For device, use your computer's IP address
```

**Authentication failures:**
- Verify Firebase configuration
- Check JWT_SECRET in .env
- Ensure Firebase project ID matches

**AI provider errors:**
- Verify API key format and validity
- Check rate limits and quotas
- Review provider-specific error codes

## 📚 Development Resources

- **Backend API**: http://localhost:3000/api/v1/
- **Database**: Neon Console (spring-flower-04114371)
- **Firebase**: https://console.firebase.google.com/project/farmer-chat-eagle
- **OpenAI**: https://platform.openai.com/docs/
- **Android Docs**: CLAUDE.md in project root