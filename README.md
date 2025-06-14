# FarmerChat - AI Assistant for Smallholder Farmers

FarmerChat is an Android application that provides AI-powered agricultural advice to smallholder farmers through an intuitive chat interface with voice and text support.

## Features

- **Multilingual Support**: Supports 8 Indian languages including English, Hindi, Bengali, Telugu, Marathi, Tamil, Gujarati, and Kannada
- **Voice & Text Input**: Farmers can ask questions using voice or text
- **Text-to-Speech**: Listen to AI responses for better accessibility
- **Staged Onboarding**: Value-first approach with simple setup (language, location, crops/livestock selection)
- **Personalized Advice**: Context-aware responses based on user's location, crops, and livestock
- **Starter Questions**: Pre-populated relevant questions based on user profile
- **Follow-up Questions**: AI suggests related questions to explore topics deeper
- **Feedback System**: Rate and provide feedback on AI responses
- **Offline-First**: Uses local storage for preferences with Firebase sync

## Setup Instructions

### 1. Prerequisites
- Android Studio (latest version)
- Firebase account
- Google AI (Gemini) API key

### 2. Firebase Setup
The project is already configured with Firebase. The following services are enabled:
- **Firestore**: For storing user profiles, chat sessions, and feedback
- **Authentication**: Anonymous authentication for easy onboarding

### 3. Configuration Steps

1. **Add your Gemini API Key**:
   - Open `ChatViewModel.kt`
   - Replace `YOUR_API_KEY_HERE` with your actual Gemini API key
   ```kotlin
   private val generativeModel = GenerativeModel(
       modelName = "gemini-pro",
       apiKey = "YOUR_ACTUAL_API_KEY"
   )
   ```

2. **Build and Run**:
   ```bash
   ./gradlew build
   ```

3. **Seed Starter Questions** (Optional):
   - Use the `StarterQuestionSeeder` utility to populate initial questions in Firestore
   - This can be called from a temporary admin screen or Firebase console

### 4. Project Structure

```
app/src/main/java/com/digitalgreen/farmerchat/
├── data/
│   ├── Models.kt              # Data models
│   └── FarmerChatRepository.kt # Firebase operations
├── navigation/
│   └── Navigation.kt          # App navigation setup
├── ui/
│   ├── components/           # Reusable UI components
│   │   ├── MessageBubble.kt
│   │   ├── VoiceRecordingButton.kt
│   │   └── FeedbackDialog.kt
│   ├── screens/             # App screens
│   │   ├── SplashScreen.kt
│   │   ├── OnboardingScreen.kt
│   │   └── ChatScreen.kt
│   └── theme/              # Material theme
├── utils/
│   ├── PreferencesManager.kt   # Local storage
│   ├── TextToSpeechManager.kt  # TTS functionality
│   └── StarterQuestionSeeder.kt # Database seeder
└── MainActivity.kt
```

### 5. Key Components

- **Onboarding Flow**: 
  1. Language selection
  2. Location selection
  3. Crops selection (optional)
  4. Livestock selection (optional)

- **Chat Features**:
  - Real-time messaging with Firestore
  - Voice input with speech recognition
  - Text-to-speech for responses
  - Context-aware AI responses
  - Follow-up question suggestions

- **Feedback System**:
  - 5-star rating
  - Optional text feedback
  - Stored in Firestore for analysis

### 6. Testing

1. Run the app on an emulator or physical device
2. Complete the onboarding flow
3. Try asking questions via text or voice
4. Test the text-to-speech functionality
5. Submit feedback on responses

### 7. Deployment

1. Update `versionCode` and `versionName` in `build.gradle.kts`
2. Generate signed APK/AAB
3. Deploy to Google Play Store

## Future Enhancements

- [ ] Offline AI model for basic queries
- [ ] Image recognition for crop disease detection
- [ ] Weather integration
- [ ] Market price information
- [ ] Community features
- [ ] Push notifications for seasonal advice

## Support

For issues or questions, please contact the Digital Green development team.