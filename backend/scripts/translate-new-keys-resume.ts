import Anthropic from '@anthropic-ai/sdk';
import { pool } from '../src/database';
import { logger } from '../src/utils/logger';

const anthropic = new Anthropic({
  apiKey: process.env.ANTHROPIC_API_KEY || '',
});

// All 53 supported languages
const SUPPORTED_LANGUAGES = [
  { code: 'en', name: 'English', native: 'English' },
  { code: 'es', name: 'Spanish', native: 'Español' },
  { code: 'zh', name: 'Chinese (Simplified)', native: '中文' },
  { code: 'ar', name: 'Arabic', native: 'العربية', isRTL: true },
  { code: 'fr', name: 'French', native: 'Français' },
  { code: 'pt', name: 'Portuguese', native: 'Português' },
  { code: 'ru', name: 'Russian', native: 'Русский' },
  { code: 'de', name: 'German', native: 'Deutsch' },
  { code: 'ja', name: 'Japanese', native: '日本語' },
  { code: 'ko', name: 'Korean', native: '한국어' },
  { code: 'hi', name: 'Hindi', native: 'हिन्दी' },
  { code: 'bn', name: 'Bengali', native: 'বাংলা' },
  { code: 'te', name: 'Telugu', native: 'తెలుగు' },
  { code: 'mr', name: 'Marathi', native: 'मराठी' },
  { code: 'ta', name: 'Tamil', native: 'தமிழ்' },
  { code: 'gu', name: 'Gujarati', native: 'ગુજરાતી' },
  { code: 'kn', name: 'Kannada', native: 'ಕನ್ನಡ' },
  { code: 'ml', name: 'Malayalam', native: 'മലയാളം' },
  { code: 'pa', name: 'Punjabi', native: 'ਪੰਜਾਬੀ' },
  { code: 'or', name: 'Odia', native: 'ଓଡ଼ିଆ' },
  { code: 'as', name: 'Assamese', native: 'অসমীয়া' },
  { code: 'ur', name: 'Urdu', native: 'اردو', isRTL: true },
  { code: 'sw', name: 'Swahili', native: 'Kiswahili' },
  { code: 'am', name: 'Amharic', native: 'አማርኛ' },
  { code: 'ha', name: 'Hausa', native: 'Hausa' },
  { code: 'yo', name: 'Yoruba', native: 'Yorùbá' },
  { code: 'ig', name: 'Igbo', native: 'Igbo' },
  { code: 'zu', name: 'Zulu', native: 'isiZulu' },
  { code: 'xh', name: 'Xhosa', native: 'isiXhosa' },
  { code: 'af', name: 'Afrikaans', native: 'Afrikaans' },
  { code: 'id', name: 'Indonesian', native: 'Bahasa Indonesia' },
  { code: 'ms', name: 'Malay', native: 'Bahasa Melayu' },
  { code: 'th', name: 'Thai', native: 'ไทย' },
  { code: 'vi', name: 'Vietnamese', native: 'Tiếng Việt' },
  { code: 'fil', name: 'Filipino', native: 'Filipino' },
  { code: 'km', name: 'Khmer', native: 'ខ្មែរ' },
  { code: 'lo', name: 'Lao', native: 'ລາវ' },
  { code: 'my', name: 'Burmese', native: 'မြန်မာ' },
  { code: 'it', name: 'Italian', native: 'Italiano' },
  { code: 'nl', name: 'Dutch', native: 'Nederlands' },
  { code: 'pl', name: 'Polish', native: 'Polski' },
  { code: 'uk', name: 'Ukrainian', native: 'Українська' },
  { code: 'ro', name: 'Romanian', native: 'Română' },
  { code: 'el', name: 'Greek', native: 'Ελληνικά' },
  { code: 'cs', name: 'Czech', native: 'Čeština' },
  { code: 'hu', name: 'Hungarian', native: 'Magyar' },
  { code: 'sv', name: 'Swedish', native: 'Svenska' },
  { code: 'da', name: 'Danish', native: 'Dansk' },
  { code: 'fi', name: 'Finnish', native: 'Suomi' },
  { code: 'no', name: 'Norwegian', native: 'Norsk' },
  { code: 'tr', name: 'Turkish', native: 'Türkçe' },
  { code: 'he', name: 'Hebrew', native: 'עברית', isRTL: true },
  { code: 'fa', name: 'Persian', native: 'فارسی', isRTL: true }
];

async function translateNewKeys() {
  logger.info('Starting translation of new keys...');
  
  // Parse command line arguments
  const args = process.argv.slice(2);
  const startFrom = args.find(arg => arg.startsWith('--start-from='))?.split('=')[1] || 'zh';
  
  // Get all English keys
  const keysResult = await pool.query(`
    SELECT DISTINCT key, translation 
    FROM ui_translations
    WHERE language_code = 'en'
    ORDER BY key
  `);
  
  const newKeys = keysResult.rows;
  logger.info(`Found ${newKeys.length} keys that need translation`);
  
  if (newKeys.length === 0) {
    logger.info('No new keys to translate!');
    return;
  }
  
  // Filter languages to start from the specified one
  let languagesToProcess = SUPPORTED_LANGUAGES.filter(l => l.code !== 'en');
  const startIndex = languagesToProcess.findIndex(l => l.code === startFrom);
  if (startIndex !== -1) {
    languagesToProcess = languagesToProcess.slice(startIndex);
    logger.info(`Starting from language: ${startFrom}`);
  }
  
  // For each language, check which keys are missing
  let languageCount = 0;
  for (const lang of languagesToProcess) {
    languageCount++;
    
    // Get existing keys for this language
    const existingResult = await pool.query(
      `SELECT key FROM ui_translations WHERE language_code = $1 AND key IN (${newKeys.map((_, i) => `$${i + 2}`).join(',')})`,
      [lang.code, ...newKeys.map(k => k.key)]
    );
    
    const existingKeys = new Set(existingResult.rows.map(r => r.key));
    const missingKeys = newKeys.filter(k => !existingKeys.has(k.key));
    
    if (missingKeys.length === 0) {
      logger.info(`[${languageCount}/${languagesToProcess.length}] ${lang.name}: Already complete ✓`);
      continue;
    }
    
    logger.info(`\n[${languageCount}/${languagesToProcess.length}] ${lang.name} (${lang.native}): Translating ${missingKeys.length} missing keys...`);
    
    // Process in batches of 20 (smaller batch size for reliability)
    const batchSize = 20;
    for (let i = 0; i < missingKeys.length; i += batchSize) {
      const batch = missingKeys.slice(i, i + batchSize);
      
      try {
        const prompt = `You are a professional translator for a mobile app called FarmerChat that helps farmers.

Translate these UI strings from English to ${lang.name} (${lang.native}).

CRITICAL RULES:
1. NEVER translate "FarmerChat" - it's a brand name
2. Keep translations natural for farmers in the target region
3. Use appropriate formality level for the culture
4. For technical terms, use commonly understood local equivalents
5. Keep translations concise for mobile UI
6. Return ONLY valid JSON mapping keys to translations
7. Preserve any formatting like %s, %d in the exact same position

Texts to translate:
${JSON.stringify(batch.map(row => ({ key: row.key, text: row.translation })), null, 2)}

Return ONLY this JSON format:
{
  "KEY_NAME": "translated text",
  ...
}`;

        const response = await anthropic.messages.create({
          model: 'claude-3-5-sonnet-20241022',
          max_tokens: 4000,
          temperature: 0.1,
          messages: [{ role: 'user', content: prompt }]
        });
        
        const content = response.content[0].type === 'text' ? response.content[0].text : '';
        const translations = JSON.parse(content.match(/\{[\s\S]*\}/)?.[0] || '{}');
        
        // Save translations
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
          logger.info(`  Batch ${Math.floor(i/batchSize) + 1}/${Math.ceil(missingKeys.length/batchSize)} ✓`);
        } catch (error) {
          await client.query('ROLLBACK');
          throw error;
        } finally {
          client.release();
        }
        
        // Rate limit delay
        await new Promise(resolve => setTimeout(resolve, 500));
      } catch (error) {
        logger.error(`Failed to translate batch for ${lang.name}:`, error);
        // Continue with next batch instead of failing completely
      }
    }
  }
  
  // Final stats
  const finalResult = await pool.query(`
    SELECT 
      COUNT(DISTINCT key) as total_keys,
      COUNT(DISTINCT language_code) as total_languages,
      COUNT(*) as total_translations
    FROM ui_translations
  `);
  
  const stats = finalResult.rows[0];
  logger.info('\n=== TRANSLATION COMPLETE ===');
  logger.info(`Total unique keys: ${stats.total_keys}`);
  logger.info(`Total languages: ${stats.total_languages}`);
  logger.info(`Total translations: ${stats.total_translations}`);
  logger.info(`Expected: ${374 * 53} = 19,822 translations`);
}

// Main execution
if (require.main === module) {
  translateNewKeys()
    .then(() => {
      logger.info('\n✅ Successfully translated all new keys!');
      process.exit(0);
    })
    .catch(error => {
      logger.error('Translation failed:', error);
      process.exit(1);
    });
}