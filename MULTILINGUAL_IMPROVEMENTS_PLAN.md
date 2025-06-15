# Multilingual Improvements Plan

## Issue 1: Crop/Livestock Names Not Showing in User's Language

### Diagnosis
The translation system is already in place:
- `CropTranslations.kt` has translations for all crops in Hindi, Swahili, and other languages
- `LivestockTranslations.kt` has translations for all animals
- UI correctly calls `getLocalizedName(currentLanguage)`

### Possible Causes
1. **App needs restart** after language change
2. **Cache issue** - old language preference cached
3. **Language code mismatch** - might be passing wrong language code

### Solution
1. Force app restart after language selection
2. Clear the app data and retest
3. Add debug logging to verify correct language code is being passed

## Issue 2: Multi-Language Conversation Titles

### Current Structure
```kotlin
data class Conversation(
    val id: String = "",
    val title: String = "",  // Single title in one language
    ...
)
```

### Proposed New Structure
```kotlin
data class Conversation(
    val id: String = "",
    val title: String = "",  // Default title (English)
    val localizedTitles: Map<String, String> = emptyMap(), // Lang code -> Title
    ...
) {
    fun getLocalizedTitle(languageCode: String): String {
        return localizedTitles[languageCode] ?: title
    }
}
```

### Implementation Steps

1. **Update Conversation Model**
   - Add `localizedTitles` field
   - Add `getLocalizedTitle()` method

2. **Update Title Generation in ChatViewModel**
   - Generate titles in multiple languages when conversation is created
   - Store all translations in `localizedTitles` map

3. **Update Repository**
   - Save/retrieve localizedTitles to/from Firestore

4. **Update UI**
   - Use `conversation.getLocalizedTitle(currentLanguage)` instead of `conversation.title`

### Benefits
- Users see conversation titles in their preferred language
- Can switch languages and see existing conversations in new language
- Maintains backward compatibility with existing data

## Testing Steps

### For Crop/Livestock Names:
1. Clear app data: Settings → Apps → FarmerChat → Storage → Clear Data
2. Restart app
3. Select Hindi or Swahili in onboarding
4. Navigate to crop/livestock selection
5. Verify names appear in selected language

### For Conversation Titles:
1. After implementing changes
2. Create new conversation in Hindi
3. Switch to English
4. Verify title appears in English
5. Switch back to Hindi
6. Verify title appears in Hindi