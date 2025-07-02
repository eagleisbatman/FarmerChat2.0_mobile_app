# CLAUDE-backend.md

This file provides backend-specific guidance to Claude Code for the FarmerChat Node.js backend.

## 🔧 Backend Configuration

### Active Setup
- **Server**: Node.js + Express + TypeScript on port **3004** (NEVER use 3000, 3002)
- **AI Provider**: OpenAI (gpt-4o-mini) - Only OpenAI enabled (`AI_PROVIDERS_ENABLED=openai`)
- **Authentication**: Phone + PIN with bcrypt hashing
  - Primary: Phone + PIN (6 digits, bcrypt hashed with 10 salt rounds)
  - Alternative: Device-based authentication (device ID)
  - Future: SMS OTP (infrastructure ready, implementation pending)
- **Database**: Neon PostgreSQL (Firebase completely removed except FCM)
- **Real-time**: WebSocket via Socket.IO for streaming responses
- **Caching**: Redis for translations and responses
- **Push Notifications**: Firebase Cloud Messaging (FCM) - only Firebase service retained

### Environment Variables
```env
DEFAULT_AI_PROVIDER=openai
AI_PROVIDERS_ENABLED=openai  
OPENAI_MODEL=gpt-4o-mini
PORT=3004
```

## 🚀 Backend Commands

### Quick Start (RECOMMENDED)
```bash
# Use the automated startup script:
./start-backend.sh

# This script automatically:
# 1. Kills any process on port 3004
# 2. Navigates to backend directory
# 3. Installs dependencies if needed
# 4. Starts the backend server
```

### Manual Server Management
```bash
# Kill port and start server
lsof -ti:3004 | xargs kill -9 2>/dev/null || true && cd backend && npm run dev

# Health check
curl -s http://localhost:3004/health

# Monitor logs
cd backend && npm run dev
```

## 📡 API Endpoints

### Authentication
- `POST /api/v1/auth/login` - Login with phone + PIN
- `POST /api/v1/auth/register` - Register with phone + PIN
- `POST /api/v1/auth/device` - Authenticate with device ID
- `POST /api/v1/auth/refresh` - Refresh JWT token
- `POST /api/v1/auth/phone` - Request OTP (future implementation)
- `POST /api/v1/auth/verify-otp` - Verify OTP (future implementation)

### Chat & AI
- `POST /api/v1/chat/send` - Send message (WebSocket streaming)
- `GET /api/v1/chat/starter-questions` - Get dynamic starter questions
- `POST /api/v1/chat/generate-title` - Generate conversation title

### Conversations
- `GET /api/v1/conversations` - List user conversations
- `POST /api/v1/conversations` - Create new conversation
- `GET /api/v1/conversations/:id` - Get conversation details
- `DELETE /api/v1/conversations/:id` - Delete conversation

### User Profile
- `GET /api/v1/users/profile` - Get user profile
- `PUT /api/v1/users/profile` - Update profile
- `PUT /api/v1/users/language` - Update language preference

### Translations (Fully Implemented)
- `GET /api/v1/translations/:languageCode` - Get all translations for a language
- `GET /api/v1/translations/languages` - Get list of supported languages
- `POST /api/v1/translations` - Add/update translations (admin)
- `DELETE /api/v1/translations/:key/:languageCode` - Delete translation (admin)

**Translation System Status**:
- ✅ 270 unique translation keys
- ✅ 53 supported languages
- ✅ 14,310 total translations (270 × 53)
- ✅ All authentication screens use translation keys
- ✅ Redis caching for performance
- ✅ `sync-all-string-keys.ts` script with resume capabilities

## 🔒 Security

### JWT Authentication
- All protected routes require `Authorization: Bearer <token>` header
- Tokens expire in 7 days
- Automatic token refresh on 401 responses

### Middleware Stack
```typescript
app.use(helmet());
app.use(cors({ origin: process.env.ALLOWED_ORIGINS }));
app.use(rateLimiter);
app.use(authMiddleware); // For protected routes
```

## 🐛 Common Issues

### Port Already in Use
```bash
# Always kill port 3004 before starting
lsof -ti:3004 | xargs kill -9 2>/dev/null || true
```

### WebSocket Connection Failed
- Ensure backend is running on port 3004
- Check CORS configuration allows WebSocket origins
- Verify Socket.IO client version matches server

### AI Response Errors
- Check OpenAI API key is valid
- Monitor rate limits in logs
- Verify streaming is enabled in AI service

## 📊 Monitoring

### Real-time Logs
```bash
# Application logs
cd backend && npm run dev

# AI request logs
tail -f backend/logs/ai-requests.log

# Error logs
tail -f backend/logs/error.log
```

### Performance Metrics
- Redis cache hit ratio target: >80%
- Average response time: <2s for AI responses
- WebSocket connection stability: >99%

## 🧪 Testing Backend

### Unit Tests
```bash
cd backend
npm test
```

### Integration Tests
```bash
cd backend
npm run test:integration
```

### Manual API Testing
```bash
# Test health endpoint
curl http://localhost:3004/health

# Test phone + PIN login
curl -X POST http://localhost:3004/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone": "+919876543210", "pin": "123456"}'

# Test phone + PIN registration
curl -X POST http://localhost:3004/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"phone": "+919876543210", "pin": "123456"}'

# Test device-based auth
curl -X POST http://localhost:3004/api/v1/auth/device \
  -H "Content-Type: application/json" \
  -d '{"deviceId": "unique-device-id"}'

# Test translations API
curl http://localhost:3004/api/v1/translations/en
```

## 🔄 Backend Development Workflow

1. **Start Redis** (if using caching)
   ```bash
   redis-server
   ```

2. **Start Backend**
   ```bash
   ./start-backend.sh
   ```

3. **Watch for Changes**
   ```bash
   cd backend && npm run dev
   ```

4. **Test Changes**
   - Use Postman or curl for API testing
   - Monitor logs for errors
   - Check WebSocket connections

## 📝 Important Notes

- **NEVER** run on ports 3000 or 3002 (conflicts with other services)
- **ALWAYS** use port 3004 for consistency
- **Multi-provider architecture** exists but only OpenAI is enabled
- **Dynamic prompts** - No database seeding required
- **Translations** cached in Redis for performance
- **PIN Security** - PINs are hashed using bcrypt (10 rounds)

## 🔐 PIN Security

### Bcrypt Implementation
- All PINs are hashed using bcrypt with 10 salt rounds
- Plain text PINs are never stored in the database
- Migration script available: `ts-node scripts/hash-existing-pins.ts`

### Security Notes
- PINs must be 6 digits (validated in frontend and backend)
- Failed login attempts should be rate-limited (TODO)
- Consider implementing account lockout after multiple failures (TODO)

## 📚 Translation Scripts

### sync-all-string-keys.ts
Synchronizes all 270 translation keys across 53 languages using Claude API.

```bash
# Run with options
npx ts-node scripts/sync-all-string-keys.ts [options]

# Options:
--skip-completed     # Skip languages that already have all keys
--start-from=CODE    # Resume from specific language (e.g., --start-from=th)
--language=CODE      # Process only one language (e.g., --language=sw)
--help              # Show help

# Examples:
npx ts-node scripts/sync-all-string-keys.ts --skip-completed
npx ts-node scripts/sync-all-string-keys.ts --start-from=vi --skip-completed
```