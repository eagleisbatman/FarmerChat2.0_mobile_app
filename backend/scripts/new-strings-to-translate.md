# New Strings Added That Need Translation

## Authentication & Login Screens
These are hardcoded strings that need to be added to StringsManager and translated:

### LoginScreen.kt
- "Include country code (e.g., +91 for India, +1 for USA)" - Helper text for phone input
- "Invalid phone number or PIN" - Error message
- "No PIN set for this account. Please register to set a PIN." - Error when no PIN
- "Account not found. Please register." - Error when user doesn't exist
- "Network error. Please check your connection." - Network error
- "Login failed. Please try again." - Generic login error
- "An unexpected error occurred. Please try again." - Unexpected error

### RegisterScreen.kt  
- "Phone number must start with country code (e.g., +91, +1)" - Validation error
- "Include country code (e.g., +91 for India, +1 for USA)" - Helper text
- "Registration successful!" - Success message (currently not used)
- "Phone number already exists. Please login." - When user already exists

### Backend Error Messages (auth.service.ts)
- "Phone number must include country code (e.g., +91, +1, +44)" - Backend validation

## New Settings Screens
These screens were created with reused onboarding components:

### NameSelectionScreen.kt
- Uses existing StringKey.ENTER_YOUR_NAME
- Uses existing StringKey.NAME
- Uses existing StringKey.SAVE

### GenderSelectionScreen.kt
- Uses existing StringKey.SELECT_GENDER
- Uses existing StringKey.GENDER_SUBTITLE
- Uses existing StringKey.MALE/FEMALE/OTHER
- Uses existing StringKey.SAVE

### RoleSelectionScreen.kt
- Uses existing StringKey.SELECT_ROLE
- Uses existing StringKey.ROLE_SUBTITLE
- Uses existing StringKey.FARMER/EXTENSION_WORKER
- Uses existing StringKey.SAVE

### CropSelectionScreen.kt
- Uses existing StringKey.SELECT_CROPS
- Uses existing StringKey.SEARCH_CROPS
- Uses existing StringKey.SAVE

### LivestockSelectionScreen.kt
- Uses existing StringKey.SELECT_LIVESTOCK
- Uses existing StringKey.SEARCH_LIVESTOCK
- Uses existing StringKey.SAVE

## Missing StringKeys to Add

### New Authentication Keys Needed
```kotlin
COUNTRY_CODE_REQUIRED,          // "Include country code (e.g., +91 for India, +1 for USA)"
INVALID_CREDENTIALS,            // "Invalid phone number or PIN"
NO_PIN_SET,                    // "No PIN set for this account. Please register to set a PIN."
ACCOUNT_NOT_FOUND,             // "Account not found. Please register."
NETWORK_ERROR_CHECK_CONNECTION, // "Network error. Please check your connection."
LOGIN_FAILED_TRY_AGAIN,        // "Login failed. Please try again."
UNEXPECTED_ERROR_TRY_AGAIN,    // "An unexpected error occurred. Please try again."
PHONE_MUST_START_WITH_CODE,    // "Phone number must start with country code (e.g., +91, +1)"
PHONE_ALREADY_EXISTS_LOGIN,    // "Phone number already exists. Please login."
REGISTRATION_SUCCESSFUL,       // "Registration successful!"
```

## Existing Translation Infrastructure

The app already has comprehensive translation support for:
- 50+ languages in ui_translations table
- All crop names in crop_translations table
- All livestock names in livestock_translations table
- Complete onboarding flow translations

## Scripts Available for Translation

In `/backend/scripts/`:
- `generate-translations-claude.ts` - Main translation script using Claude
- `complete-all-ui-translations.ts` - Completes all UI translations
- `sync-all-string-keys.ts` - Syncs StringKeys with database
- `generate-crop-livestock-translations.ts` - For master data translations

## Recommended Approach for MVP

1. Add the new StringKeys listed above to StringsManager.kt
2. Update all hardcoded strings to use StringKeys
3. After MVP, run a comprehensive translation script to translate all new keys to 50+ languages
4. Update the ui_translations table in the database

## Current Language Support
The app supports 50+ languages including:
- Major Indian languages: Hindi, Bengali, Telugu, Tamil, Marathi, Gujarati, Kannada, Malayalam, Punjabi, Odia
- African languages: Swahili, Amharic, Hausa, Yoruba, Zulu, Xhosa
- Southeast Asian: Thai, Vietnamese, Burmese, Indonesian, Malay
- European: Spanish, French, Portuguese, German, Italian, Russian, Polish, Ukrainian
- And many more...

## Note
The translation infrastructure is already in place. We just need to:
1. Add the new StringKeys
2. Replace hardcoded strings
3. Run the translation scripts after MVP