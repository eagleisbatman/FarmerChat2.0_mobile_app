# FarmerChat Translation Management Guide

## Overview
This guide explains how to manage translations for FarmerChat's 52 supported languages across UI strings, crop names, and livestock names.

## Translation Architecture

### 1. Translation Components
- **StringsManager.kt** - UI strings and messages (201 keys)
- **CropTranslations.kt** - Crop name translations (45+ crops)
- **LivestockTranslations.kt** - Livestock translations (20+ animals)
- **TranslationManager.kt** - Centralized management system

### 2. Language Support
Currently supporting 52 languages with ISO 639-1 codes:
- **Fully Translated**: en, hi, sw (3/52)
- **Needs Translation**: 49 languages including es, fr, bn, pt, id, ar, zh, ja, ko, de, etc.

## Managing Translations

### Option 1: Web-Based Translation Tool
1. Open `tools/translation-editor.html` in a web browser
2. Select target language from dropdown
3. Edit translations directly in the interface
4. Use filters to find missing translations
5. Export to CSV for external translation
6. Import completed translations

### Option 2: Direct Code Editing
For small updates like adding "bull" translation:

```kotlin
// In LivestockTranslations.kt
"bull" to mapOf(
    "en" to "Bull/Ox",
    "hi" to "बैल",
    "sw" to "Ng'ombe dume",
    "es" to "Toro",
    "fr" to "Taureau"
),
```

### Option 3: CSV Export/Import
1. Run the Python script:
   ```bash
   cd scripts
   python3 export_translations.py
   ```

2. Send CSV files to translators:
   - `string_keys_translations.csv`
   - `crops_translations.csv`
   - `livestock_translations.csv`

3. Import completed translations back

### Option 4: Professional Translation Service
1. Export master translation file using TranslationManager
2. Format: Category | ID | English | Target Language
3. Send to translation service (Google Translate, DeepL, Human translators)
4. Import completed translations

## Adding New Translations

### For a New Crop:
1. Add to CropsManager.kt:
   ```kotlin
   Crop("mango", "Mango", "Mangifera indica", CropCategory.FRUITS, "🥭", ...)
   ```

2. Add translations to CropTranslations.kt:
   ```kotlin
   "mango" to mapOf(
       "en" to "Mango",
       "hi" to "आम",
       "sw" to "Embe",
       // Add more languages
   ),
   ```

### For a New UI String:
1. Add to StringKey enum:
   ```kotlin
   enum class StringKey {
       // ...
       NEW_FEATURE_LABEL,
   }
   ```

2. Add translations:
   ```kotlin
   "en" to mapOf(
       StringKey.NEW_FEATURE_LABEL to "New Feature",
       // ...
   ),
   "hi" to mapOf(
       StringKey.NEW_FEATURE_LABEL to "नई सुविधा",
       // ...
   ),
   ```

## Quality Assurance

### Translation Validation
Use TranslationManager to validate:
```kotlin
val issues = TranslationManager.validateTranslations()
issues.forEach { (category, problems) ->
    println("$category issues:")
    problems.forEach { println("  - $it") }
}
```

### Testing Checklist
1. **Language Switching**: Change language in settings, verify all UI updates
2. **Crop/Livestock Names**: Check selection screens show translated names
3. **RTL Languages**: Test Arabic, Hebrew for layout issues
4. **Long Translations**: Ensure German, French translations don't break layouts
5. **Special Characters**: Verify Chinese, Japanese, Korean display correctly

## Best Practices

### 1. Translation Guidelines
- Keep translations concise for UI elements
- Use formal language appropriate for farmers
- Maintain consistency (e.g., always use same word for "Save")
- Consider regional variations (e.g., British vs American English)
- Test translations in context, not just isolated

### 2. Missing Translation Handling
The app has a 3-tier fallback system:
1. Try user's selected language
2. Fallback to English if not found
3. Show StringKey name if English missing

This ensures no empty text ever appears.

### 3. Performance Considerations
- Current: All translations loaded in memory
- Future: Consider lazy loading for 50+ languages
- Option: Server-side translation delivery

### 4. Maintenance Process
1. **Weekly**: Run validation to find missing translations
2. **Monthly**: Export status report, prioritize languages
3. **Quarterly**: Professional translation review
4. **Annually**: Full translation audit

## Common Issues & Solutions

### Issue: Crop/Livestock names not translating
**Solution**: 
1. Check translation exists in CropTranslations/LivestockTranslations
2. Clear app data to reset language cache
3. Verify correct language code is being passed

### Issue: New string shows key name instead of translation
**Solution**:
1. Ensure string added to all language maps
2. At minimum, add English translation
3. Rebuild and reinstall app

### Issue: Special characters not displaying
**Solution**:
1. Ensure files saved with UTF-8 encoding
2. Test on actual device, not just emulator
3. Check font supports the character set

## Priority Language Roadmap

### Phase 1 (Immediate):
- ✅ English (en)
- ✅ Hindi (hi) 
- ✅ Swahili (sw)

### Phase 2 (High Priority):
- Spanish (es) - 500M speakers
- French (fr) - 280M speakers
- Bengali (bn) - 260M speakers
- Portuguese (pt) - 250M speakers
- Indonesian (id) - 200M speakers

### Phase 3 (Regional):
- Arabic (ar)
- Chinese (zh)
- Tamil (ta)
- Telugu (te)
- Marathi (mr)

### Phase 4 (Complete Coverage):
- Remaining 35+ languages

## Automation Tools

### Translation Status Script
```bash
# Check translation coverage
./gradlew checkTranslations

# Generate translation report
./gradlew generateTranslationReport
```

### Bulk Translation Update
```kotlin
// Use TranslationManager
val file = File("translations.csv")
val result = TranslationManager.importTranslationsFromCSV(file)
```

## External Resources

### Translation Services
1. **Google Cloud Translation API** - For automated translation
2. **DeepL API** - Higher quality for European languages
3. **Crowdin** - Community translation platform
4. **Local translators** - For regional dialects

### Language Codes Reference
- ISO 639-1: https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes
- Android Locales: https://developer.android.com/reference/java/util/Locale

## Summary

Managing translations for 52 languages requires:
1. **Organized structure** - Separate files for strings, crops, livestock
2. **Efficient tools** - Web editor, CSV export/import, validation
3. **Clear process** - Regular validation, professional review
4. **Quality fallbacks** - Never show missing text

The current system is scalable and maintainable, ready for expanding to all 52 languages.