# Hardcoded Strings Audit Report

## 1. Placeholder Text
- `OnboardingPhoneStep.kt`: "1234567890", "123456" (PIN placeholders)
- `EnhancedLanguageSelectionView.kt`: "Search from 50+ languages..."
- `SmartLanguageSelector.kt`: "Search languages..."

## 2. Error Messages
### ForgotPinViewModel.kt
- "Failed to send verification code"
- "Failed to verify code" 
- "Failed to reset PIN"
- "Please enter your phone number"
- "Please enter the verification code"
- "PIN must be 6 digits"
- "PINs do not match"
- "Invalid verification code"

### UnifiedOnboardingViewModel.kt
- "Please complete all required fields"
- "Failed to save profile. Please try again."
- "An error occurred. Please try again."

### LoginViewModel.kt
- "An unexpected error occurred. Please try again."

### RegisterViewModel.kt
- "An error occurred. Please try again."
- "PINs do not match" (in RegisterScreen.kt)

### ChatWebSocketClient.kt
- "Unknown error"

## 3. UI Text
- `SplashScreen.kt`: "Retry"
- `UnifiedOnboardingScreen.kt`: " *" (asterisk for required fields)

## 4. Toast Messages
- `ApiSettingsViewModel.kt`: "Export feature coming soon"
- `ShareBottomSheet.kt`: "Copied to clipboard"

## 5. Missing Functionality Messages
- `SettingsScreen.kt`: Data export - "TODO: Implement export using API data"
- `SettingsScreen.kt`: Delete data - "TODO: Implement delete all data"
- `SettingsScreen.kt`: Help/Feedback - "TODO: Open help/feedback"

## 6. API Error Messages to Standardize
- Network connection errors
- Server timeout errors
- Invalid credentials
- Session expired
- Rate limiting

## 7. Confirmation Messages
- Data deletion confirmation
- Logout confirmation
- Reset onboarding confirmation
- PIN reset success

## New String Keys Needed

### Placeholders
- PHONE_NUMBER_PLACEHOLDER = "1234567890"
- PIN_PLACEHOLDER = "123456"
- SEARCH_LANGUAGES_PLACEHOLDER = "Search languages..."
- SEARCH_LANGUAGES_FULL_PLACEHOLDER = "Search from 50+ languages..."

### Error Messages
- ERROR_SEND_VERIFICATION = "Failed to send verification code"
- ERROR_VERIFY_CODE = "Failed to verify code"
- ERROR_RESET_PIN = "Failed to reset PIN"
- ERROR_PHONE_REQUIRED = "Please enter your phone number"
- ERROR_VERIFICATION_REQUIRED = "Please enter the verification code"
- ERROR_PIN_LENGTH = "PIN must be 6 digits"
- ERROR_PIN_MISMATCH = "PINs do not match"
- ERROR_INVALID_VERIFICATION = "Invalid verification code"
- ERROR_REQUIRED_FIELDS = "Please complete all required fields"
- ERROR_SAVE_PROFILE = "Failed to save profile. Please try again."
- ERROR_UNEXPECTED = "An unexpected error occurred. Please try again."
- ERROR_UNKNOWN = "Unknown error"

### UI Text
- RETRY = "Retry" (already exists as StringKey.RETRY)
- REQUIRED_FIELD_INDICATOR = " *"
- COPIED_TO_CLIPBOARD = "Copied to clipboard"
- FEATURE_COMING_SOON = "This feature is coming soon"

### Network/API Errors
- ERROR_NO_NETWORK = "No internet connection. Please check your connection."
- ERROR_SERVER_TIMEOUT = "Server timeout. Please try again."
- ERROR_INVALID_CREDENTIALS = "Invalid credentials. Please try again."
- ERROR_SESSION_EXPIRED = "Your session has expired. Please login again."
- ERROR_RATE_LIMITED = "Too many requests. Please try again later."