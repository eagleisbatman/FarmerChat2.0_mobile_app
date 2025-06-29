import Anthropic from '@anthropic-ai/sdk';
import { pool } from '../src/database';
import { logger } from '../src/utils/logger';

const anthropic = new Anthropic({
  apiKey: process.env.ANTHROPIC_API_KEY || '',
});

// All crops that should be translated
const ALL_CROPS = [
  'rice', 'wheat', 'maize', 'millet', 'sorghum', 'barley', 'cotton', 'sugarcane',
  'jute', 'coffee', 'tea', 'rubber', 'coconut', 'groundnut', 'mustard', 'sunflower',
  'soybean', 'sesame', 'safflower', 'lentil', 'chickpea', 'pigeon_pea', 'black_gram',
  'green_gram', 'kidney_bean', 'potato', 'tomato', 'onion', 'brinjal', 'okra',
  'cabbage', 'cauliflower', 'carrot', 'radish', 'peas', 'beans', 'spinach',
  'coriander', 'chilli', 'garlic', 'ginger', 'turmeric', 'mango', 'banana',
  'apple', 'orange', 'grapes', 'papaya', 'guava', 'pomegranate', 'watermelon',
  'custard_apple', 'sapota', 'jackfruit', 'cashew'
];

async function getMissingUIKeys(languageCode: string): Promise<{ key: string; text: string }[]> {
  // Get all English keys
  const englishResult = await pool.query(
    'SELECT key, translation FROM ui_translations WHERE language_code = $1',
    ['en']
  );
  
  // Get existing keys for target language
  const existingResult = await pool.query(
    'SELECT key FROM ui_translations WHERE language_code = $1',
    [languageCode]
  );
  
  const englishMap = new Map(englishResult.rows.map(r => [r.key, r.translation]));
  const existingKeys = new Set(existingResult.rows.map(r => r.key));
  
  const missing: { key: string; text: string }[] = [];
  for (const [key, text] of englishMap) {
    if (!existingKeys.has(key)) {
      missing.push({ key, text });
    }
  }
  
  return missing;
}

async function getMissingCrops(languageCode: string): Promise<string[]> {
  const result = await pool.query(
    'SELECT crop_id FROM crop_translations WHERE language_code = $1',
    [languageCode]
  );
  
  const existing = new Set(result.rows.map(r => r.crop_id));
  return ALL_CROPS.filter(crop => !existing.has(crop));
}

async function translateAndSaveUI(languageCode: string, languageName: string, missingKeys: { key: string; text: string }[]) {
  if (missingKeys.length === 0) return;
  
  logger.info(`Translating ${missingKeys.length} UI keys for ${languageName} (${languageCode})...`);
  
  // Translate in batches of 50
  const batchSize = 50;
  for (let i = 0; i < missingKeys.length; i += batchSize) {
    const batch = missingKeys.slice(i, i + batchSize);
    
    try {
      const keysToTranslate = Object.fromEntries(batch.map(k => [k.key, k.text]));
      
      const prompt = `Translate these UI strings from English to ${languageName}.
FarmerChat is a mobile app for farmers. NEVER translate "FarmerChat" - it's a brand name.
Keep translations natural and concise for mobile UI.
Preserve any %s or %d format specifiers exactly.
Return ONLY valid JSON mapping keys to translations:

${JSON.stringify(keysToTranslate, null, 2)}`;

      const response = await anthropic.messages.create({
        model: 'claude-3-5-sonnet-20241022',
        max_tokens: 4000,
        temperature: 0.1,
        messages: [{ role: 'user', content: prompt }]
      });
      
      const content = response.content[0].type === 'text' ? response.content[0].text : '';
      const translations = JSON.parse(content.match(/\{[\s\S]*\}/)?.[0] || '{}');
      
      // Save to database
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
            [languageCode, key, cleanTranslation]
          );
        }
        
        await client.query('COMMIT');
        logger.info(`  Batch ${Math.floor(i/batchSize) + 1}/${Math.ceil(missingKeys.length/batchSize)} saved`);
      } catch (error) {
        await client.query('ROLLBACK');
        throw error;
      } finally {
        client.release();
      }
      
      await new Promise(resolve => setTimeout(resolve, 300));
    } catch (error) {
      logger.error(`Failed to translate batch for ${languageName}:`, error);
    }
  }
}

async function translateAndSaveCrops(languageCode: string, languageName: string, missingCrops: string[]) {
  if (missingCrops.length === 0) return;
  
  logger.info(`Translating ${missingCrops.length} crops for ${languageName} (${languageCode})...`);
  
  try {
    const prompt = `Translate these agricultural crop names from English to ${languageName}.
Use common farmer terms that would be understood by local farmers.
Return ONLY valid JSON mapping crop IDs to their translated names:

${JSON.stringify(missingCrops, null, 2)}`;

    const response = await anthropic.messages.create({
      model: 'claude-3-5-sonnet-20241022',
      max_tokens: 3000,
      temperature: 0.1,
      messages: [{ role: 'user', content: prompt }]
    });
    
    const content = response.content[0].type === 'text' ? response.content[0].text : '';
    const translations = JSON.parse(content.match(/\{[\s\S]*\}/)?.[0] || '{}');
    
    // Save to database
    const client = await pool.connect();
    try {
      await client.query('BEGIN');
      
      for (const [cropId, translation] of Object.entries(translations)) {
        await client.query(
          `INSERT INTO crop_translations (language_code, crop_id, name)
           VALUES ($1, $2, $3)
           ON CONFLICT (language_code, crop_id)
           DO UPDATE SET name = $3, updated_at = NOW()`,
          [languageCode, cropId, translation]
        );
      }
      
      await client.query('COMMIT');
      logger.info(`  Saved ${Object.keys(translations).length} crop translations`);
    } catch (error) {
      await client.query('ROLLBACK');
      throw error;
    } finally {
      client.release();
    }
  } catch (error) {
    logger.error(`Failed to translate crops for ${languageName}:`, error);
  }
}

async function completeFinalTranslations() {
  logger.info('Starting FINAL translation completion...\n');
  
  // Languages that need UI completion
  const uiLanguages = [
    { code: 'ig', name: 'Igbo' },
    { code: 'kn', name: 'Kannada' },
    { code: 'or', name: 'Odia' },
    { code: 'pa', name: 'Punjabi' }
  ];
  
  // Languages that need crop completion
  const cropLanguages = [
    { code: 'hi', name: 'Hindi' },
    { code: 'sw', name: 'Swahili' },
    { code: 'my', name: 'Burmese' }
  ];
  
  // Complete UI translations
  logger.info('=== COMPLETING UI TRANSLATIONS ===');
  for (const lang of uiLanguages) {
    const missingKeys = await getMissingUIKeys(lang.code);
    await translateAndSaveUI(lang.code, lang.name, missingKeys);
  }
  
  // Complete crop translations
  logger.info('\n=== COMPLETING CROP TRANSLATIONS ===');
  for (const lang of cropLanguages) {
    const missingCrops = await getMissingCrops(lang.code);
    await translateAndSaveCrops(lang.code, lang.name, missingCrops);
  }
  
  // Final verification
  logger.info('\n=== FINAL VERIFICATION ===');
  
  const verifyResult = await pool.query(`
    WITH stats AS (
      SELECT 
        'UI' as type,
        language_code,
        COUNT(DISTINCT key) as count,
        195 as expected
      FROM ui_translations
      WHERE language_code IN ('ig', 'kn', 'or', 'pa', 'en')
      GROUP BY language_code
      
      UNION ALL
      
      SELECT 
        'Crops' as type,
        language_code,
        COUNT(DISTINCT crop_id) as count,
        55 as expected
      FROM crop_translations
      WHERE language_code IN ('hi', 'sw', 'my', 'en')
      GROUP BY language_code
    )
    SELECT * FROM stats ORDER BY type, language_code
  `);
  
  logger.info('\nVerification Results:');
  for (const row of verifyResult.rows) {
    const status = row.count === row.expected ? '✅' : '❌';
    logger.info(`${status} ${row.type} - ${row.language_code}: ${row.count}/${row.expected}`);
  }
  
  // Overall statistics
  const overallStats = await pool.query(`
    SELECT 
      (SELECT COUNT(*) FROM (
        SELECT language_code FROM ui_translations 
        GROUP BY language_code 
        HAVING COUNT(DISTINCT key) = 195
      ) t) as complete_ui_languages,
      (SELECT COUNT(*) FROM (
        SELECT language_code FROM crop_translations 
        GROUP BY language_code 
        HAVING COUNT(DISTINCT crop_id) = 55
      ) t) as complete_crop_languages,
      (SELECT COUNT(*) FROM (
        SELECT language_code FROM livestock_translations 
        GROUP BY language_code 
        HAVING COUNT(DISTINCT livestock_id) >= 10
      ) t) as complete_livestock_languages
  `);
  
  const stats = overallStats.rows[0];
  logger.info('\n=== OVERALL COMPLETION STATUS ===');
  logger.info(`✅ UI Translations: ${stats.complete_ui_languages}/53 languages complete`);
  logger.info(`✅ Crop Translations: ${stats.complete_crop_languages}/53 languages complete`);
  logger.info(`✅ Livestock Translations: ${stats.complete_livestock_languages}/53 languages complete`);
}

// Main execution
if (require.main === module) {
  completeFinalTranslations()
    .then(() => {
      logger.info('\n🎉 FINAL TRANSLATION COMPLETION FINISHED! 🎉');
      process.exit(0);
    })
    .catch(error => {
      logger.error('Failed:', error);
      process.exit(1);
    });
}