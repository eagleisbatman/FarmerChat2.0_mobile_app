import Anthropic from '@anthropic-ai/sdk';
import { pool } from '../src/database';
import { logger } from '../src/utils/logger';

const anthropic = new Anthropic({
  apiKey: process.env.ANTHROPIC_API_KEY || '',
});

async function completeAllUITranslations() {
  // Get all English keys and translations
  const englishResult = await pool.query(
    'SELECT key, translation FROM ui_translations WHERE language_code = $1 ORDER BY key',
    ['en']
  );
  
  const englishTranslations = new Map<string, string>();
  for (const row of englishResult.rows) {
    englishTranslations.set(row.key, row.translation);
  }
  
  logger.info(`Found ${englishTranslations.size} English keys to translate`);
  
  // Get current status for all languages
  const statusResult = await pool.query(`
    SELECT 
      language_code,
      COUNT(DISTINCT key) as translated_count
    FROM ui_translations
    GROUP BY language_code
    ORDER BY language_code
  `);
  
  const languageStatus = new Map<string, number>();
  for (const row of statusResult.rows) {
    languageStatus.set(row.language_code, row.translated_count);
  }
  
  // All 53 languages
  const ALL_LANGUAGES = [
    'en', 'es', 'zh', 'ar', 'fr', 'pt', 'ru', 'de', 'ja', 'ko',
    'hi', 'bn', 'te', 'mr', 'ta', 'gu', 'kn', 'ml', 'pa', 'or',
    'as', 'ur', 'sw', 'am', 'ha', 'yo', 'ig', 'zu', 'xh', 'af',
    'id', 'ms', 'th', 'vi', 'fil', 'km', 'lo', 'my', 'it', 'nl',
    'pl', 'uk', 'ro', 'el', 'cs', 'hu', 'sv', 'da', 'fi', 'no',
    'tr', 'he', 'fa'
  ];
  
  const LANGUAGE_NAMES: Record<string, string> = {
    'es': 'Spanish', 'zh': 'Chinese', 'ar': 'Arabic', 'fr': 'French',
    'pt': 'Portuguese', 'ru': 'Russian', 'de': 'German', 'ja': 'Japanese',
    'ko': 'Korean', 'hi': 'Hindi', 'bn': 'Bengali', 'te': 'Telugu',
    'mr': 'Marathi', 'ta': 'Tamil', 'gu': 'Gujarati', 'kn': 'Kannada',
    'ml': 'Malayalam', 'pa': 'Punjabi', 'or': 'Odia', 'as': 'Assamese',
    'ur': 'Urdu', 'sw': 'Swahili', 'am': 'Amharic', 'ha': 'Hausa',
    'yo': 'Yoruba', 'ig': 'Igbo', 'zu': 'Zulu', 'xh': 'Xhosa',
    'af': 'Afrikaans', 'id': 'Indonesian', 'ms': 'Malay', 'th': 'Thai',
    'vi': 'Vietnamese', 'fil': 'Filipino', 'km': 'Khmer', 'lo': 'Lao',
    'my': 'Burmese', 'it': 'Italian', 'nl': 'Dutch', 'pl': 'Polish',
    'uk': 'Ukrainian', 'ro': 'Romanian', 'el': 'Greek', 'cs': 'Czech',
    'hu': 'Hungarian', 'sv': 'Swedish', 'da': 'Danish', 'fi': 'Finnish',
    'no': 'Norwegian', 'tr': 'Turkish', 'he': 'Hebrew', 'fa': 'Persian'
  };
  
  // Process each language
  for (const langCode of ALL_LANGUAGES) {
    if (langCode === 'en') continue;
    
    const currentCount = languageStatus.get(langCode) || 0;
    const targetCount = englishTranslations.size;
    
    if (currentCount >= targetCount) {
      logger.info(`${LANGUAGE_NAMES[langCode]} (${langCode}): Already complete ✓`);
      continue;
    }
    
    // Get existing translations
    const existingResult = await pool.query(
      'SELECT key FROM ui_translations WHERE language_code = $1',
      [langCode]
    );
    
    const existingKeys = new Set(existingResult.rows.map(r => r.key));
    const missingKeys: { key: string; text: string }[] = [];
    
    for (const [key, text] of englishTranslations) {
      if (!existingKeys.has(key)) {
        missingKeys.push({ key, text });
      }
    }
    
    logger.info(`${LANGUAGE_NAMES[langCode]} (${langCode}): Translating ${missingKeys.length} missing keys...`);
    
    // Translate all at once for efficiency
    try {
      const prompt = `Translate these UI strings from English to ${LANGUAGE_NAMES[langCode]}.
FarmerChat is a mobile app for farmers. NEVER translate "FarmerChat" - it's a brand name.
Keep translations natural and concise for mobile UI.
Return ONLY valid JSON mapping keys to translations:

${JSON.stringify(Object.fromEntries(missingKeys.map(k => [k.key, k.text])), null, 2)}`;

      const response = await anthropic.messages.create({
        model: 'claude-3-5-sonnet-20241022',
        max_tokens: 8000,
        temperature: 0.1,
        messages: [{ role: 'user', content: prompt }]
      });
      
      const content = response.content[0].type === 'text' ? response.content[0].text : '';
      const translations = JSON.parse(content.match(/\{[\s\S]*\}/)?.[0] || '{}');
      
      // Save all translations
      const client = await pool.connect();
      try {
        await client.query('BEGIN');
        
        for (const [key, translation] of Object.entries(translations)) {
          const cleanTranslation = key === 'APP_NAME' ? 'FarmerChat' : translation;
          
          await client.query(
            `INSERT INTO ui_translations (language_code, key, translation)
             VALUES ($1, $2, $3)
             ON CONFLICT (language_code, key)
             DO UPDATE SET translation = $3, updated_at = NOW()`,
            [langCode, key, cleanTranslation]
          );
        }
        
        await client.query('COMMIT');
        logger.info(`${LANGUAGE_NAMES[langCode]} (${langCode}): Complete ✓`);
      } catch (error) {
        await client.query('ROLLBACK');
        throw error;
      } finally {
        client.release();
      }
    } catch (error) {
      logger.error(`Failed to translate ${LANGUAGE_NAMES[langCode]}:`, error);
    }
    
    await new Promise(resolve => setTimeout(resolve, 500));
  }
  
  // Final report
  const finalResult = await pool.query(`
    SELECT 
      COUNT(DISTINCT key) as total_keys,
      COUNT(DISTINCT language_code) as total_languages,
      COUNT(*) as total_translations,
      (SELECT COUNT(*) FROM (
        SELECT language_code 
        FROM ui_translations 
        GROUP BY language_code 
        HAVING COUNT(DISTINCT key) = (SELECT COUNT(DISTINCT key) FROM ui_translations WHERE language_code = 'en')
      ) complete) as complete_languages
    FROM ui_translations
  `);
  
  const stats = finalResult.rows[0];
  logger.info('\n=== FINAL UI TRANSLATION SUMMARY ===');
  logger.info(`Total unique keys: ${stats.total_keys}`);
  logger.info(`Total languages: ${stats.total_languages}`);
  logger.info(`Languages with complete translations: ${stats.complete_languages}/53`);
  logger.info(`Total translation entries: ${stats.total_translations}`);
}

// Main execution
if (require.main === module) {
  completeAllUITranslations()
    .then(() => {
      logger.info('\n🎉 ALL UI TRANSLATIONS COMPLETE! 🎉');
      process.exit(0);
    })
    .catch(error => {
      logger.error('Failed:', error);
      process.exit(1);
    });
}