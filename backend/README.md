# FarmerChat Backend

A Node.js/TypeScript backend for the FarmerChat application, providing REST APIs and WebSocket support for real-time agricultural advice powered by multiple AI providers.

## Features

- 🤖 **OpenAI Integration**: Currently using OpenAI gpt-4o-mini as the active AI provider
- 🔐 **Firebase Authentication**: Phone number OTP authentication via Firebase Auth
- 🗄️ **Neon Database**: Serverless PostgreSQL for scalable data storage
- 🌐 **Multi-Language Support**: 50+ languages with dynamic translation management
- 💬 **Real-time Chat**: WebSocket support for streaming AI responses
- 🔔 **Push Notifications**: Firebase Cloud Messaging (FCM) integration
- 📦 **Redis Caching**: High-performance caching for translations and API responses
- 🔒 **Security**: JWT authentication, rate limiting, and admin access controls

## Tech Stack

- **Runtime**: Node.js with TypeScript
- **Framework**: Express.js
- **Database**: Neon (Serverless PostgreSQL)
- **Cache**: Redis
- **Authentication**: Firebase Auth with JWT
- **AI Provider**: OpenAI (gpt-4o-mini) - Multi-provider architecture available but only OpenAI enabled
- **Real-time**: Socket.IO
- **Notifications**: Firebase Cloud Messaging

## Prerequisites

- Node.js 18+ and npm
- Redis server (optional, for caching)
- Neon database account
- Firebase project (for Auth and FCM)
- OpenAI API key (currently the only enabled provider)

## Installation

1. Clone the repository:
```bash
cd FarmerChat/backend
```

2. Install dependencies:
```bash
npm install
```

3. Copy environment variables:
```bash
cp .env.example .env
```

4. Configure your `.env` file with required values

5. Database schema is already set up in Neon (spring-flower-04114371)

## Development

Start the development server with hot reload:
```bash
npm run dev
```

The server will start on `http://localhost:3000` by default.

## Building for Production

1. Build the TypeScript code:
```bash
npm run build
```

2. Start the production server:
```bash
npm start
```

## Available Scripts

- `npm run dev` - Start development server with nodemon
- `npm run build` - Build TypeScript to JavaScript
- `npm start` - Start production server
- `npm run typecheck` - Check TypeScript types
- `npm run clean` - Clean build directory

## API Endpoints

### Authentication
- `GET /api/v1/auth/config` - Get Firebase configuration for client
- `POST /api/v1/auth/verify` - Verify Firebase ID token and get JWT
- `POST /api/v1/auth/refresh` - Refresh access token
- `POST /api/v1/auth/signout` - Sign out user

### Chat
- `POST /api/v1/chat/send` - Send message and get AI response
- `GET /api/v1/chat/stream` - Stream AI response via WebSocket
- `GET /api/v1/chat/history/:conversationId` - Get chat history

### Conversations
- `GET /api/v1/conversations` - List user conversations
- `POST /api/v1/conversations` - Create new conversation
- `PUT /api/v1/conversations/:id` - Update conversation
- `DELETE /api/v1/conversations/:id` - Delete conversation

### User Profile
- `GET /api/v1/users/profile` - Get user profile
- `PUT /api/v1/users/profile` - Update user profile
- `PUT /api/v1/users/preferences` - Update preferences

### Translations
- `GET /api/v1/translations/:languageCode` - Get all translations
- `GET /api/v1/translations/languages` - Get supported languages

### Admin APIs
- `POST /api/v1/admin/translations/import` - Import translations
- `GET /api/v1/admin/translations/export/:type/:languageCode` - Export translations
- `PUT /api/v1/admin/translations/:type/:key` - Update translation
- `GET /api/v1/admin/translations/stats` - Get translation statistics

## Authentication Flow

1. **Client-side (Android)**: 
   - Initialize Firebase Auth
   - Send OTP to phone number
   - Verify OTP and get Firebase ID token

2. **Server-side**:
   - Receive Firebase ID token
   - Verify with Firebase Admin SDK
   - Create/update user in Neon database
   - Return JWT for API access

## Database Schema (Already Created)

```sql
-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email TEXT UNIQUE,
    phone TEXT UNIQUE,
    fcm_token TEXT,
    name TEXT,
    language TEXT DEFAULT 'en',
    location TEXT,
    location_info JSONB,
    crops TEXT[],
    livestock TEXT[],
    preferences JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Conversations table
CREATE TABLE conversations (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    localized_titles JSONB DEFAULT '{}',
    last_message TEXT,
    last_message_time TIMESTAMPTZ,
    last_message_is_user BOOLEAN DEFAULT false,
    tags TEXT[],
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Messages table
CREATE TABLE messages (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    conversation_id UUID REFERENCES conversations(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    is_user BOOLEAN NOT NULL,
    audio_url TEXT,
    is_voice_message BOOLEAN DEFAULT false,
    follow_up_questions TEXT[],
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Add indexes for performance
CREATE INDEX idx_conversations_user_id ON conversations(user_id);
CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX idx_messages_created_at ON messages(created_at);
```

See the full schema in `database/schema.sql`.

## Environment Variables

Key environment variables to configure:

- `DATABASE_URL` - Neon PostgreSQL connection string
- `FIREBASE_PROJECT_ID` - Firebase project ID
- `FIREBASE_PRIVATE_KEY` - Firebase service account private key
- `FIREBASE_CLIENT_EMAIL` - Firebase service account email
- `FIREBASE_API_KEY` - Firebase web API key (for client config)
- `GEMINI_API_KEY` - Google Gemini API key
- `OPENAI_API_KEY` - OpenAI API key (optional)
- `ANTHROPIC_API_KEY` - Anthropic API key (optional)
- `DEFAULT_AI_PROVIDER` - Default AI provider (gemini/openai/anthropic)
- `JWT_SECRET` - Secret for JWT signing

See `.env.example` for all available options.

## WebSocket Events

The server supports real-time communication via Socket.IO:

### Client to Server
- `chat:message` - Send a chat message
- `chat:typing` - User typing indicator
- `chat:stop` - Stop AI generation

### Server to Client
- `chat:response` - AI response chunk
- `chat:complete` - AI response complete
- `chat:error` - Error occurred
- `chat:typing` - AI typing indicator

## Rate Limiting

- API endpoints: 1000 requests per hour per IP
- Translation endpoints: 5000 requests per hour per IP
- WebSocket connections: 10 per IP

## Security

- All endpoints except public ones require JWT authentication
- Admin endpoints require additional admin API key or admin email
- Rate limiting on all endpoints
- Input validation and sanitization
- SQL injection protection via parameterized queries
- XSS protection via helmet middleware

## Error Handling

The API returns consistent error responses:

```json
{
  "error": {
    "message": "Error description",
    "statusCode": 400,
    "code": "ERROR_CODE"
  }
}
```

## Monitoring

- Health check endpoint at `/health`
- Structured logging with timestamps
- Optional Sentry integration for error tracking
- Request/response logging in development

## Contributing

1. Create a feature branch
2. Make your changes
3. Run `npm run typecheck` to ensure type safety
4. Submit a pull request

## License

This project is licensed under the ISC License.