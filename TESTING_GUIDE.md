# FarmerChat Testing Guide

## Backend-Android Integration Testing

### Prerequisites

1. **Backend Server**: Ensure the backend server is running
   ```bash
   cd backend
   npm start
   ```

2. **Android Emulator**: Launch an Android emulator or connect a physical device
   ```bash
   adb devices  # Should show your device
   ```

3. **Network Configuration**: The app is configured to connect to `http://10.0.2.2:3000` (emulator's localhost)
   - For physical device: Update `BASE_URL` in `NetworkConfig.kt` to your computer's IP address

### Testing Flow

#### 1. App Launch & Authentication
- Launch the FarmerChat app
- The app should automatically authenticate using Firebase Anonymous Auth
- Check backend logs for authentication requests
- Verify JWT token is received and stored

#### 2. Onboarding Flow
- Complete the onboarding process:
  - Select language
  - Set location (manual or GPS)
  - Choose crops and livestock
  - Enter name
- Verify user profile is created/updated in backend

#### 3. Conversations
- After onboarding, you should be redirected to chat
- A new conversation should be created automatically
- Check backend logs for conversation creation

#### 4. Chat Functionality
- Send a message in the chat
- Verify:
  - Message appears in UI
  - Streaming response from AI
  - Follow-up questions are displayed
  - Conversation title is generated after first exchange

#### 5. Voice Features
- Test voice input (microphone button)
- Test text-to-speech (speaker icon on AI messages)
- Verify language-specific voice recognition

#### 6. Advanced Features
- Check conversation tagging (backend generates tags)
- View conversation list (should show tags)
- Test search functionality
- Try different languages

### Backend Monitoring

Monitor the backend console for:
- Authentication requests
- WebSocket connections
- AI streaming events
- Token usage tracking
- Error messages

### Common Issues

1. **Connection Refused**
   - Ensure backend is running
   - Check BASE_URL in NetworkConfig.kt
   - For physical device, use computer's IP address

2. **Authentication Failed**
   - Check Firebase configuration
   - Verify google-services.json is correct
   - Check backend Firebase Admin SDK setup

3. **No AI Response**
   - Verify OpenAI API key in backend .env
   - Check backend logs for AI errors
   - Ensure WebSocket connection is established

### API Endpoints to Test

1. **Auth**: `POST /api/v1/auth/login`
2. **User Profile**: `GET /api/v1/users/profile`
3. **Conversations**: `GET /api/v1/conversations`
4. **Messages**: `GET /api/v1/conversations/:id/messages`
5. **Streaming**: WebSocket at `/socket.io/`

### Debugging Tips

1. **ADB Logcat**: View Android logs
   ```bash
   adb logcat | grep -E "FarmerChat|HTTP|API"
   ```

2. **Backend Logs**: Check console output for detailed request/response info

3. **Network Inspector**: Use Android Studio's Network Profiler

4. **Chrome DevTools**: For WebSocket debugging
   - Open chrome://inspect
   - Select your emulator/device