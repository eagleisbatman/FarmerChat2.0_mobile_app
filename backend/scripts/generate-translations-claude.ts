import Anthropic from '@anthropic-ai/sdk';
import { pool } from '../src/database';
import { logger } from '../src/utils/logger';
import * as fs from 'fs/promises';
import * as path from 'path';

// Configuration
const ANTHROPIC_API_KEY = process.env.ANTHROPIC_API_KEY || '';

// Initialize Anthropic client
const anthropic = new Anthropic({
  apiKey: ANTHROPIC_API_KEY,
});

// All 53 supported languages from LanguageManager.kt
const SUPPORTED_LANGUAGES = [
  // Major Global Languages
  { code: 'en', name: 'English', native: 'English', isRTL: false },
  { code: 'es', name: 'Spanish', native: 'Español', isRTL: false },
  { code: 'zh', name: 'Chinese (Simplified)', native: '中文', isRTL: false },
  { code: 'ar', name: 'Arabic', native: 'العربية', isRTL: true },
  { code: 'fr', name: 'French', native: 'Français', isRTL: false },
  { code: 'pt', name: 'Portuguese', native: 'Português', isRTL: false },
  { code: 'ru', name: 'Russian', native: 'Русский', isRTL: false },
  { code: 'de', name: 'German', native: 'Deutsch', isRTL: false },
  { code: 'ja', name: 'Japanese', native: '日本語', isRTL: false },
  { code: 'ko', name: 'Korean', native: '한국어', isRTL: false },
  
  // Indian Languages
  { code: 'hi', name: 'Hindi', native: 'हिन्दी', isRTL: false },
  { code: 'bn', name: 'Bengali', native: 'বাংলা', isRTL: false },
  { code: 'te', name: 'Telugu', native: 'తెలుగు', isRTL: false },
  { code: 'mr', name: 'Marathi', native: 'मराठी', isRTL: false },
  { code: 'ta', name: 'Tamil', native: 'தமிழ்', isRTL: false },
  { code: 'gu', name: 'Gujarati', native: 'ગુજરાતી', isRTL: false },
  { code: 'kn', name: 'Kannada', native: 'ಕನ್ನಡ', isRTL: false },
  { code: 'ml', name: 'Malayalam', native: 'മലയാളം', isRTL: false },
  { code: 'pa', name: 'Punjabi', native: 'ਪੰਜਾਬੀ', isRTL: false },
  { code: 'or', name: 'Odia', native: 'ଓଡ଼ିଆ', isRTL: false },
  { code: 'as', name: 'Assamese', native: 'অসমীয়া', isRTL: false },
  { code: 'ur', name: 'Urdu', native: 'اردو', isRTL: true },
  
  // African Languages
  { code: 'sw', name: 'Swahili', native: 'Kiswahili', isRTL: false },
  { code: 'am', name: 'Amharic', native: 'አማርኛ', isRTL: false },
  { code: 'ha', name: 'Hausa', native: 'Hausa', isRTL: false },
  { code: 'yo', name: 'Yoruba', native: 'Yorùbá', isRTL: false },
  { code: 'ig', name: 'Igbo', native: 'Igbo', isRTL: false },
  { code: 'zu', name: 'Zulu', native: 'isiZulu', isRTL: false },
  { code: 'xh', name: 'Xhosa', native: 'isiXhosa', isRTL: false },
  { code: 'af', name: 'Afrikaans', native: 'Afrikaans', isRTL: false },
  
  // Southeast Asian Languages
  { code: 'id', name: 'Indonesian', native: 'Bahasa Indonesia', isRTL: false },
  { code: 'ms', name: 'Malay', native: 'Bahasa Melayu', isRTL: false },
  { code: 'th', name: 'Thai', native: 'ไทย', isRTL: false },
  { code: 'vi', name: 'Vietnamese', native: 'Tiếng Việt', isRTL: false },
  { code: 'fil', name: 'Filipino', native: 'Filipino', isRTL: false },
  { code: 'km', name: 'Khmer', native: 'ខ្មែរ', isRTL: false },
  { code: 'lo', name: 'Lao', native: 'ລາວ', isRTL: false },
  { code: 'my', name: 'Burmese', native: 'မြန်မာ', isRTL: false },
  
  // European Languages
  { code: 'it', name: 'Italian', native: 'Italiano', isRTL: false },
  { code: 'nl', name: 'Dutch', native: 'Nederlands', isRTL: false },
  { code: 'pl', name: 'Polish', native: 'Polski', isRTL: false },
  { code: 'uk', name: 'Ukrainian', native: 'Українська', isRTL: false },
  { code: 'ro', name: 'Romanian', native: 'Română', isRTL: false },
  { code: 'el', name: 'Greek', native: 'Ελληνικά', isRTL: false },
  { code: 'cs', name: 'Czech', native: 'Čeština', isRTL: false },
  { code: 'hu', name: 'Hungarian', native: 'Magyar', isRTL: false },
  { code: 'sv', name: 'Swedish', native: 'Svenska', isRTL: false },
  { code: 'da', name: 'Danish', native: 'Dansk', isRTL: false },
  { code: 'fi', name: 'Finnish', native: 'Suomi', isRTL: false },
  { code: 'no', name: 'Norwegian', native: 'Norsk', isRTL: false },
  
  // Other Important Languages
  { code: 'tr', name: 'Turkish', native: 'Türkçe', isRTL: false },
  { code: 'he', name: 'Hebrew', native: 'עברית', isRTL: true },
  { code: 'fa', name: 'Persian', native: 'فارسی', isRTL: true }
];

// Critical strings that need translation first (Priority 1)
const PRIORITY_STRING_KEYS = [
  // Phone Auth
  'PHONE_AUTH_TITLE', 'PHONE_NUMBER', 'ENTER_PHONE_NUMBER', 'COUNTRY_CODE',
  'SEND_OTP', 'VERIFY_OTP', 'ENTER_OTP', 'OTP_SENT', 'RESEND_OTP',
  'VERIFY', 'PHONE_AUTH_DESC', 'SKIP_FOR_NOW', 'INVALID_PHONE_NUMBER', 'INVALID_OTP',
  
  // Gender Selection
  'SELECT_GENDER', 'GENDER_SUBTITLE', 'MALE', 'FEMALE', 'OTHER',
  
  // Role Selection  
  'SELECT_ROLE', 'ROLE_SUBTITLE', 'FARMER', 'EXTENSION_WORKER',
  
  // Crop Categories
  'CROP_CATEGORY_CEREALS', 'CROP_CATEGORY_PULSES', 'CROP_CATEGORY_VEGETABLES',
  'CROP_CATEGORY_FRUITS', 'CROP_CATEGORY_CASH_CROPS', 'CROP_CATEGORY_OILSEEDS',
  'CROP_CATEGORY_SPICES', 'CROP_CATEGORY_PLANTATION', 'CROP_CATEGORY_FODDER',
  'CROP_CATEGORY_FLOWERS',
  
  // Livestock Categories
  'LIVESTOCK_CATEGORY_CATTLE', 'LIVESTOCK_CATEGORY_POULTRY', 'LIVESTOCK_CATEGORY_SMALL_RUMINANTS',
  'LIVESTOCK_CATEGORY_SWINE', 'LIVESTOCK_CATEGORY_AQUACULTURE', 'LIVESTOCK_CATEGORY_OTHERS',
  
  // Core Navigation
  'CONTINUE', 'BACK', 'NEXT', 'SKIP', 'DONE', 'CANCEL', 'OK',
  'SAVE', 'SEARCH', 'CLEAR', 'ALL',
  
  // Core App Strings
  'APP_NAME', 'CHOOSE_LANGUAGE', 'LANGUAGE_SUBTITLE', 'WHERE_LOCATED',
  'LOCATION_SUBTITLE', 'SELECT_CROPS', 'CROPS_SUBTITLE', 'SELECT_LIVESTOCK',
  'LIVESTOCK_SUBTITLE', 'ENTER_YOUR_NAME', 'NAME', 'START_CHATTING'
];

interface TranslationRequest {
  sourceLanguage: string;
  targetLanguage: string;
  targetLanguageName: string;
  nativeName: string;
  isRTL: boolean;
  texts: { key: string; text: string }[];
  context: string;
}

class ClaudeTranslationService {
  
  async translateBatch(request: TranslationRequest): Promise<Record<string, string>> {
    const prompt = `You are a professional translator specializing in agricultural content and mobile app localization for farmers.
    
Translate the following UI strings from ${request.sourceLanguage} to ${request.targetLanguageName} (${request.nativeName}).
Target language code: ${request.targetLanguage}
Is RTL language: ${request.isRTL}

Context: ${request.context}

CRITICAL RULES:
1. **NEVER TRANSLATE "FarmerChat"** - This is a brand name and must remain as "FarmerChat" in all languages
2. Keep translations natural and culturally appropriate for farmers in the target region
3. Use formal/respectful tone appropriate for the target culture
4. For technical/agricultural terms, use commonly understood local equivalents
5. Keep translations concise - mobile UI space is very limited
6. For RTL languages (Arabic, Hebrew, Urdu, Persian), ensure proper text direction
7. Return ONLY a valid JSON object with key-value pairs, no additional text
8. Preserve any formatting like %s, %d, {{variable}} if present

Special considerations by language:
- For Indian languages: Use respectful forms (आप not तुम in Hindi)
- For African languages: Use inclusive, community-oriented language
- For Asian languages: Maintain appropriate honorifics and politeness levels
- For Arabic: Use Modern Standard Arabic unless regional variant specified

Texts to translate:
${JSON.stringify(request.texts, null, 2)}

Return ONLY this JSON format:
{
  "PHONE_AUTH_TITLE": "translated text here",
  "APP_NAME": "FarmerChat",
  ...
}`;
    
    try {
      const response = await anthropic.messages.create({
        model: 'claude-3-5-sonnet-20241022', // Latest Sonnet model
        max_tokens: 4000,
        temperature: 0.1, // Low temperature for consistent translations
        messages: [
          {
            role: 'user',
            content: prompt
          }
        ]
      });
      
      // Extract JSON from response
      const content = response.content[0].type === 'text' ? response.content[0].text : '';
      const jsonMatch = content.match(/\{[\s\S]*\}/);
      
      if (jsonMatch) {
        const translations = JSON.parse(jsonMatch[0]);
        
        // Validate that FarmerChat wasn't translated
        if (translations.APP_NAME && translations.APP_NAME !== 'FarmerChat') {
          logger.warn(`Brand name was translated to "${translations.APP_NAME}" for ${request.targetLanguage}, fixing...`);
          translations.APP_NAME = 'FarmerChat';
        }
        
        return translations;
      } else {
        throw new Error('No valid JSON found in response');
      }
    } catch (error) {
      logger.error(`Translation error for ${request.targetLanguage}:`, error);
      throw error;
    }
  }
}

async function loadExistingTranslations(): Promise<Map<string, Map<string, string>>> {
  const client = await pool.connect();
  try {
    const result = await client.query(
      'SELECT language_code, key, translation FROM ui_translations'
    );
    
    const translations = new Map<string, Map<string, string>>();
    
    for (const row of result.rows) {
      if (!translations.has(row.language_code)) {
        translations.set(row.language_code, new Map());
      }
      translations.get(row.language_code)!.set(row.key, row.translation);
    }
    
    return translations;
  } finally {
    client.release();
  }
}

async function updateTranslationMetadata() {
  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    
    // Update translation metadata for all languages
    for (const lang of SUPPORTED_LANGUAGES) {
      await client.query(
        `INSERT INTO translation_metadata (
          language_code, language_name, native_name, is_rtl, is_enabled
        ) VALUES ($1, $2, $3, $4, $5)
        ON CONFLICT (language_code) 
        DO UPDATE SET 
          language_name = $2,
          native_name = $3,
          is_rtl = $4,
          last_updated = NOW()`,
        [lang.code, lang.name, lang.native, lang.isRTL, true]
      );
    }
    
    await client.query('COMMIT');
    logger.info('Updated translation metadata for all languages');
  } catch (error) {
    await client.query('ROLLBACK');
    throw error;
  } finally {
    client.release();
  }
}

async function generateTranslations() {
  if (!ANTHROPIC_API_KEY) {
    throw new Error('ANTHROPIC_API_KEY environment variable is required');
  }
  
  const translator = new ClaudeTranslationService();
  const existingTranslations = await loadExistingTranslations();
  
  // Get English translations as source
  const englishTranslations = existingTranslations.get('en');
  if (!englishTranslations) {
    throw new Error('English translations not found in database');
  }
  
  // Filter to priority keys only for initial translation
  const priorityTranslations = new Map<string, string>();
  for (const key of PRIORITY_STRING_KEYS) {
    const value = englishTranslations.get(key);
    if (value) {
      priorityTranslations.set(key, value);
    }
  }
  
  logger.info(`Found ${priorityTranslations.size} priority strings to translate`);
  
  // Process each language
  const translationStats = {
    total: 0,
    success: 0,
    failed: 0,
    skipped: 0
  };
  
  for (const lang of SUPPORTED_LANGUAGES) {
    if (lang.code === 'en') continue; // Skip English
    
    const existingLangTranslations = existingTranslations.get(lang.code);
    const missingKeys: { key: string; text: string }[] = [];
    
    // Find missing translations
    for (const [key, text] of priorityTranslations) {
      if (!existingLangTranslations?.has(key)) {
        missingKeys.push({ key, text });
      }
    }
    
    if (missingKeys.length === 0) {
      logger.info(`${lang.name}: All priority translations exist ✓`);
      translationStats.skipped++;
      continue;
    }
    
    logger.info(`${lang.name}: Translating ${missingKeys.length} missing priority strings...`);
    translationStats.total++;
    
    try {
      // Translate in batches of 15 for better quality
      const batchSize = 15;
      for (let i = 0; i < missingKeys.length; i += batchSize) {
        const batch = missingKeys.slice(i, i + batchSize);
        
        logger.info(`${lang.name}: Processing batch ${Math.floor(i/batchSize) + 1}/${Math.ceil(missingKeys.length/batchSize)}...`);
        
        const translations = await translator.translateBatch({
          sourceLanguage: 'English',
          targetLanguage: lang.code,
          targetLanguageName: lang.name,
          nativeName: lang.native,
          isRTL: lang.isRTL,
          texts: batch,
          context: 'FarmerChat is a mobile app providing AI-powered agricultural advice to farmers. The app helps farmers with crop selection, livestock management, weather information, and farming best practices.'
        });
        
        // Save to database
        const client = await pool.connect();
        try {
          await client.query('BEGIN');
          
          for (const [key, translation] of Object.entries(translations)) {
            await client.query(
              `INSERT INTO ui_translations (language_code, key, translation)
               VALUES ($1, $2, $3)
               ON CONFLICT (language_code, key)
               DO UPDATE SET translation = $3, updated_at = NOW()`,
              [lang.code, key, translation]
            );
          }
          
          await client.query('COMMIT');
          logger.info(`${lang.name}: Saved batch ${Math.floor(i/batchSize) + 1} ✓`);
        } catch (error) {
          await client.query('ROLLBACK');
          throw error;
        } finally {
          client.release();
        }
        
        // Rate limiting pause (Claude has generous limits but let's be respectful)
        await new Promise(resolve => setTimeout(resolve, 500));
      }
      
      translationStats.success++;
      logger.info(`${lang.name}: Translation complete ✓`);
    } catch (error) {
      logger.error(`Failed to translate ${lang.name}:`, error);
      translationStats.failed++;
      // Continue with next language
    }
  }
  
  logger.info('\n=== Translation Summary ===');
  logger.info(`Total languages processed: ${translationStats.total}`);
  logger.info(`Successful: ${translationStats.success}`);
  logger.info(`Failed: ${translationStats.failed}`);
  logger.info(`Skipped (already complete): ${translationStats.skipped}`);
}

// Generate a translation coverage report
async function generateCoverageReport() {
  const client = await pool.connect();
  try {
    const result = await client.query(`
      SELECT 
        language_code,
        COUNT(DISTINCT key) as translated_keys,
        (SELECT COUNT(DISTINCT key) FROM ui_translations WHERE language_code = 'en') as total_keys
      FROM ui_translations
      GROUP BY language_code
      ORDER BY language_code
    `);
    
    logger.info('\n=== Translation Coverage Report ===');
    logger.info('Language | Translated | Total | Coverage');
    logger.info('---------|------------|-------|----------');
    
    for (const row of result.rows) {
      const coverage = ((row.translated_keys / row.total_keys) * 100).toFixed(1);
      const lang = SUPPORTED_LANGUAGES.find(l => l.code === row.language_code);
      const langName = lang ? `${lang.name} (${lang.code})` : row.language_code;
      logger.info(`${langName.padEnd(25)} | ${row.translated_keys.toString().padStart(10)} | ${row.total_keys.toString().padStart(5)} | ${coverage}%`);
    }
  } finally {
    client.release();
  }
}

// Export translations to JSON files for backup
async function exportTranslationsToJSON() {
  const client = await pool.connect();
  try {
    const result = await client.query(
      'SELECT language_code, key, translation FROM ui_translations ORDER BY language_code, key'
    );
    
    const translationsByLang: Record<string, Record<string, string>> = {};
    
    for (const row of result.rows) {
      if (!translationsByLang[row.language_code]) {
        translationsByLang[row.language_code] = {};
      }
      translationsByLang[row.language_code][row.key] = row.translation;
    }
    
    // Create exports directory
    const exportDir = path.join(__dirname, '../../translations-export');
    await fs.mkdir(exportDir, { recursive: true });
    
    // Write individual language files
    for (const [lang, translations] of Object.entries(translationsByLang)) {
      await fs.writeFile(
        path.join(exportDir, `${lang}.json`),
        JSON.stringify(translations, null, 2)
      );
    }
    
    // Write combined file
    await fs.writeFile(
      path.join(exportDir, 'all-translations.json'),
      JSON.stringify(translationsByLang, null, 2)
    );
    
    // Write language metadata
    await fs.writeFile(
      path.join(exportDir, 'languages.json'),
      JSON.stringify(SUPPORTED_LANGUAGES, null, 2)
    );
    
    logger.info(`\nExported translations to ${exportDir}`);
  } finally {
    client.release();
  }
}

// Main execution
if (require.main === module) {
  updateTranslationMetadata()
    .then(() => generateTranslations())
    .then(() => generateCoverageReport())
    .then(() => exportTranslationsToJSON())
    .then(() => {
      logger.info('\nTranslation script completed successfully!');
      process.exit(0);
    })
    .catch((error) => {
      logger.error('Translation script failed:', error);
      process.exit(1);
    });
}

export { generateTranslations, exportTranslationsToJSON, generateCoverageReport };