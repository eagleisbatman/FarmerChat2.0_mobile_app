# FarmerChat 2.0 - AI-Powered Agricultural Advisory Mobile App

FarmerChat 2.0 is a modern Android application that provides AI-powered agricultural advice to smallholder farmers through an intuitive chat interface with voice and text support, powered by a Node.js backend and OpenAI integration.

## 🏗️ Architecture

### Current Stack (2025)
- **Frontend**: Android (Kotlin + Jetpack Compose)
- **Backend**: Node.js + TypeScript + Express
- **Database**: Neon PostgreSQL (Serverless)
- **AI Provider**: OpenAI (gpt-4o-mini) - Only enabled provider
- **Authentication**: Firebase Auth (Phone OTP) → JWT tokens
- **Real-time**: WebSocket (Socket.IO) for streaming AI responses
- **Repository**: https://github.com/eagleisbatman/FarmerChat2.0_mobile_app

## ✨ Features

- **Multilingual Support**: 50+ global languages with RTL support
- **AI-Powered Chat**: OpenAI gpt-4o-mini with streaming responses
- **Voice & Text Input**: Complete speech recognition and text-to-speech
- **Phone Authentication**: Firebase OTP with JWT token persistence
- **Personalized Advice**: Context-aware responses based on location, crops, livestock
- **Dynamic Starter Questions**: AI-generated questions based on user profile
- **Real-time Streaming**: WebSocket-powered progressive response rendering
- **Markdown Support**: Rich text formatting with nested markdown support
- **Offline-Ready**: JWT token persistence and robust error handling

## 🚀 Quick Start

### Prerequisites
- **Android Studio** (latest version)
- **Node.js** 18+ and npm
- **Firebase account** (for phone authentication)
- **OpenAI API key**
- **Neon PostgreSQL database**

### 1. Backend Setup

```bash
# Clone the repository
git clone https://github.com/eagleisbatman/FarmerChat2.0_mobile_app.git
cd FarmerChat2.0_mobile_app

# Start backend (REQUIRED - app won't work without it)
./start-backend.sh  # Auto-kills port 3004, installs deps, starts server

# Backend runs on port 3004 (Android emulator uses 10.0.2.2:3004)
```

### 2. Environment Configuration

Create `backend/.env` with:

```env
# Server
NODE_ENV=development
PORT=3004

# Database (Neon PostgreSQL)
DATABASE_URL=your_neon_postgresql_url
NEON_PROJECT_ID=your_project_id

# AI Configuration (CURRENT SETUP)
DEFAULT_AI_PROVIDER=openai
AI_PROVIDERS_ENABLED=openai
OPENAI_API_KEY=your_openai_api_key
OPENAI_MODEL=gpt-4o-mini

# Firebase Auth
FIREBASE_PROJECT_ID=your_project_id
FIREBASE_PRIVATE_KEY="your_private_key"
FIREBASE_CLIENT_EMAIL=your_service_account_email

# JWT
JWT_SECRET=your_jwt_secret
JWT_EXPIRES_IN=7d
```

### 3. Android App Setup

```bash
# Build and install (with backend running)
./gradlew clean assembleDebug && ./gradlew installDebug

# The app automatically configures to use localhost:3004 via Android emulator
```

## 🧩 Current Architecture Details

### AI Integration
- **Provider**: OpenAI gpt-4o-mini (only enabled provider)
- **Integration**: Backend AIService handles all AI operations
- **Streaming**: Real-time response streaming via WebSocket
- **Multi-provider**: Architecture supports Gemini/Anthropic but they're disabled

### Authentication Flow
1. **Firebase Phone OTP** → User enters phone number
2. **Backend JWT Exchange** → Firebase token → JWT + refresh token
3. **Token Persistence** → DataStore saves tokens across app sessions
4. **API Authorization** → All API calls use Bearer JWT tokens

### Data Flow
```
Android App (Kotlin/Compose)
    ↓ REST API + WebSocket
Node.js Backend (TypeScript)
    ↓ SQL
Neon PostgreSQL Database
    ↓ API
OpenAI (gpt-4o-mini)
```

### Key Components

#### Backend (`/backend`)
- **AI Service**: OpenAI integration with streaming
- **Auth Service**: Firebase ↔ JWT token exchange
- **Database**: Neon PostgreSQL with schema migrations
- **WebSocket**: Real-time chat streaming
- **API Routes**: RESTful endpoints for all operations

#### Android App (`/app`)
- **API Repository**: Retrofit-based backend communication
- **ViewModels**: ApiChatViewModel, ApiConversationsViewModel, etc.
- **WebSocket Client**: Real-time streaming integration
- **UI Components**: Compose components with markdown support
- **Utils**: Speech recognition, TTS, JWT token management

## 🔧 Development Workflow

### Starting Development
```bash
# 1. Start backend in one terminal
./start-backend.sh

# 2. Build and deploy Android app in another terminal
./gradlew clean assembleDebug && ./gradlew installDebug

# 3. View logs (optional)
adb logcat | grep -E "FarmerChat|ApiChat|NetworkConfig"
```

### Testing Checklist
1. **Backend Running**: Check `http://localhost:3004/api/v1/health`
2. **Phone Auth**: Complete OTP flow (mock implementation)
3. **Chat Functionality**: Send message → get OpenAI streaming response
4. **Token Persistence**: Force close app → reopen → should stay logged in
5. **WebSocket**: Verify streaming responses appear progressively

### Common Issues
- **"No auth token" error**: Start backend before launching app
- **Connection refused**: Ensure backend is on port 3004
- **Chat not loading**: Clear app data and restart with backend running

## 📁 Project Structure

```
FarmerChat2.0_mobile_app/
├── app/                          # Android Application
│   ├── src/main/java/com/digitalgreen/farmerchat/
│   │   ├── data/                 # Data models and repositories
│   │   ├── network/              # API services and WebSocket
│   │   ├── ui/                   # Compose UI components and screens
│   │   ├── utils/                # Utilities (TTS, Speech, etc.)
│   │   └── FarmerChatApplication.kt
│   └── build.gradle.kts
├── backend/                      # Node.js Backend
│   ├── src/
│   │   ├── config/              # Environment configuration
│   │   ├── controllers/         # Request handlers
│   │   ├── services/            # Business logic (AI, Auth, etc.)
│   │   ├── routes/              # API route definitions
│   │   ├── socket/              # WebSocket handlers
│   │   └── database/            # Database utilities
│   ├── database/
│   │   ├── schema.sql           # Complete database schema
│   │   └── migrations/          # Database migrations
│   ├── .env                     # Environment variables
│   └── package.json
├── CLAUDE.md                     # Development guide for Claude Code
├── README.md                     # This file
└── start-backend.sh              # Backend startup script
```

## 🌍 Supported Languages

50+ languages including:
- **South Asian**: Hindi, Bengali, Telugu, Marathi, Tamil, Gujarati, Kannada, Urdu, Punjabi
- **African**: Swahili, Amharic, Yoruba, Hausa
- **European**: English, Spanish, French, German, Portuguese, Italian
- **Asian**: Chinese, Japanese, Korean, Thai, Vietnamese
- **RTL Support**: Arabic, Hebrew, Urdu

## 🔄 Migration Status

✅ **Completed Migration** (Firebase → Node.js + Neon):
- Database: Firebase Firestore → Neon PostgreSQL
- Authentication: Firebase Auth → Firebase + JWT hybrid
- AI: Direct Gemini → Backend OpenAI integration
- Real-time: Firestore listeners → WebSocket streaming
- Architecture: Single Firebase app → Android + Node.js backend

## 🚧 Future Enhancements

- [ ] Multi-AI provider switching (Gemini, Anthropic)
- [ ] Image recognition for crop disease detection
- [ ] Weather API integration
- [ ] Market price information
- [ ] Community features and farmer networks
- [ ] Push notifications for seasonal advice
- [ ] Offline AI model for basic queries

## 📊 API Endpoints

### Authentication
- `POST /api/v1/auth/verify` - Firebase token → JWT exchange
- `POST /api/v1/auth/refresh` - Refresh JWT tokens

### Chat
- `POST /api/v1/chat/send` - Send message and get AI response
- `GET /api/v1/chat/{conversationId}/messages` - Get chat history
- WebSocket: Real-time streaming at `ws://localhost:3004`

### User Management
- `GET /api/v1/users/profile` - Get user profile
- `PUT /api/v1/users/profile` - Update user profile

### Conversations
- `GET /api/v1/conversations` - List user conversations
- `POST /api/v1/conversations` - Create new conversation

## 🔐 Security

- **JWT Authentication**: All API endpoints require valid JWT tokens
- **Token Expiration**: 7-day expiry with refresh token rotation
- **Database Security**: User data isolation via user_id foreign keys
- **API Key Security**: OpenAI key stored in backend environment only

## 📞 Support

For issues, questions, or contributions:
- **Issues**: GitHub Issues tab
- **Development**: See `CLAUDE.md` for detailed development guide
- **Contact**: Digital Green development team

---

**Note**: This is FarmerChat 2.0 with a modern architecture. The previous version used direct Firebase integration, which has been fully migrated to this Node.js + OpenAI backend system.