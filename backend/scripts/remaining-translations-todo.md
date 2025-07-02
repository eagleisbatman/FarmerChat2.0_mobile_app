# Remaining Hardcoded Strings in Android App

## Summary
Found approximately 60+ hardcoded strings that need to be moved to the localization system.

## Categories

### 1. Content Descriptions (Accessibility)
- "Swipe to cancel"
- "Cancel"
- "Search"
- "Clear"
- "Selected"
- "Clear search"
- "Location"
- "Change location"
- "Back"
- "Share"
- "Start recording"
- "Stop recording"

### 2. Toast Messages
- "Export feature coming soon"
- "Copied to clipboard"

### 3. Error Messages
- "Cannot connect to server"
- "Please ensure the backend is running on port 3004"
- "Please enter a valid phone number with country code"
- "Please enter a valid 6-digit OTP"
- "Please complete all required fields"
- "Failed to save profile. Please try again."
- "Failed to save phone number: %s"
- "Error: %s"
- "Failed to load user profile: %s"
- "Failed to load conversations: %s"
- "Failed to create conversation: %s"
- "Failed to delete conversation: %s"
- "Data export feature coming soon"
- "Error during data export: %s"
- "Conversation not found"
- "No active conversation"
- "Failed to send message: %s"

### 4. UI Text Labels
- "Retry"
- "Share Answer"
- "Q: "
- "A: "
- "WhatsApp"
- "Copy Text"
- "More Apps"
- "Cancel"
- "Back"
- "Continue"
- "Skip for now"
- " *" (asterisk for required fields)
- "+" (plus sign for tag count)
- "FarmerChat Answer" (clipboard label)
- "FarmerChat Answer" (email subject)
- "Share via"

### 5. Placeholder Text
- "Enter phone number"
- "Enter 6-digit PIN"
- "Confirm PIN"
- "1234567890"
- "123456"
- "Search from 50+ languages..."
- "Search languages..."

### 6. Field Labels
- "Code"
- "Phone Number"
- "Create PIN"
- "Confirm PIN"

### 7. App Store Link
- "Get more farming advice with FarmerChat: https://play.google.com/store/apps/details?id=com.digitalgreen.farmerchat"

## Implementation Plan

1. Create new StringKey entries in StringsManager.kt
2. Add translations to backend database for all supported languages
3. Use parameterized strings for error messages with dynamic content
4. Update all UI files to use localizedString() instead of hardcoded strings
5. Test thoroughly in multiple languages

## Files Affected
- WhatsAppVoiceRecorder.kt
- ShareBottomSheet.kt
- SplashScreen.kt
- PhoneAuthViewModel.kt
- UnifiedOnboardingViewModel.kt
- PhoneCollectionViewModel.kt
- ApiConversationsViewModel.kt
- ApiChatViewModel.kt
- OnboardingScreen.kt
- OnboardingPhoneStep.kt
- UnifiedOnboardingScreen.kt
- ConversationsScreen.kt
- PhoneCollectionScreen.kt
- PhoneAuthScreen.kt
- EnhancedLanguageSelectionView.kt
- SmartLanguageSelector.kt
- CropSelectionScreen.kt
- LivestockSelectionScreen.kt
- FarmerChatAppBar.kt
- MessageBubbleV2.kt
- AudioRecordingView.kt
- ApiSettingsViewModel.kt