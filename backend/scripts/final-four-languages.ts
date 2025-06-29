import Anthropic from '@anthropic-ai/sdk';
import { pool } from '../src/database';
import { logger } from '../src/utils/logger';

// Configuration
const ANTHROPIC_API_KEY = process.env.ANTHROPIC_API_KEY || '';

// Initialize Anthropic client
const anthropic = new Anthropic({
  apiKey: ANTHROPIC_API_KEY,
});

// Final 4 languages
const FINAL_LANGUAGES = [
  { code: 'ro', name: 'Romanian', native: 'Română' },
  { code: 'sv', name: 'Swedish', native: 'Svenska' },
  { code: 'tr', name: 'Turkish', native: 'Türkçe' },
  { code: 'uk', name: 'Ukrainian', native: 'Українська' }
];

// All crops and livestock
const ALL_ITEMS = {
  crops: [
    'rice', 'wheat', 'maize', 'millet', 'sorghum', 'barley', 'cotton', 'sugarcane',
    'jute', 'coffee', 'tea', 'rubber', 'coconut', 'groundnut', 'mustard', 'sunflower',
    'soybean', 'sesame', 'safflower', 'lentil', 'chickpea', 'pigeon_pea', 'black_gram',
    'green_gram', 'kidney_bean', 'potato', 'tomato', 'onion', 'brinjal', 'okra',
    'cabbage', 'cauliflower', 'carrot', 'radish', 'peas', 'beans', 'spinach',
    'coriander', 'chilli', 'garlic', 'ginger', 'turmeric', 'mango', 'banana',
    'apple', 'orange', 'grapes', 'papaya', 'guava', 'pomegranate', 'watermelon',
    'custard_apple', 'sapota', 'jackfruit', 'cashew'
  ],
  livestock: [
    'cow', 'buffalo', 'goat', 'sheep', 'pig', 'chicken', 'duck', 'turkey',
    'goose', 'quail', 'rabbit', 'horse', 'donkey', 'mule', 'camel', 'yak',
    'fish', 'shrimp', 'crab', 'bee', 'silkworm'
  ]
};

async function translateAllForLanguage(lang: any) {
  // Translate all crops at once
  const cropsPrompt = `Translate these agricultural crop names from English to ${lang.name} (${lang.native}).
Use common farmer terms. Return ONLY valid JSON mapping IDs to translations:
${JSON.stringify(ALL_ITEMS.crops, null, 2)}`;

  const cropsResponse = await anthropic.messages.create({
    model: 'claude-3-5-sonnet-20241022',
    max_tokens: 3000,
    temperature: 0.1,
    messages: [{ role: 'user', content: cropsPrompt }]
  });
  
  const cropsContent = cropsResponse.content[0].type === 'text' ? cropsResponse.content[0].text : '';
  const cropsJson = JSON.parse(cropsContent.match(/\{[\s\S]*\}/)?.[0] || '{}');
  
  // Translate all livestock at once
  const livestockPrompt = `Translate these farm animal/livestock names from English to ${lang.name} (${lang.native}).
Use common farmer terms. Return ONLY valid JSON mapping IDs to translations:
${JSON.stringify(ALL_ITEMS.livestock, null, 2)}`;

  const livestockResponse = await anthropic.messages.create({
    model: 'claude-3-5-sonnet-20241022',
    max_tokens: 2000,
    temperature: 0.1,
    messages: [{ role: 'user', content: livestockPrompt }]
  });
  
  const livestockContent = livestockResponse.content[0].type === 'text' ? livestockResponse.content[0].text : '';
  const livestockJson = JSON.parse(livestockContent.match(/\{[\s\S]*\}/)?.[0] || '{}');
  
  return { crops: cropsJson, livestock: livestockJson };
}

async function completeFinalLanguages() {
  logger.info(`Completing translations for final ${FINAL_LANGUAGES.length} languages...`);
  
  for (const lang of FINAL_LANGUAGES) {
    logger.info(`\nProcessing ${lang.name} (${lang.code})...`);
    
    try {
      const translations = await translateAllForLanguage(lang);
      
      // Save all translations in one transaction
      const client = await pool.connect();
      try {
        await client.query('BEGIN');
        
        // Save crops
        for (const [cropId, translation] of Object.entries(translations.crops)) {
          await client.query(
            `INSERT INTO crop_translations (language_code, crop_id, name)
             VALUES ($1, $2, $3)
             ON CONFLICT (language_code, crop_id)
             DO UPDATE SET name = $3, updated_at = NOW()`,
            [lang.code, cropId, translation]
          );
        }
        
        // Save livestock
        for (const [livestockId, translation] of Object.entries(translations.livestock)) {
          await client.query(
            `INSERT INTO livestock_translations (language_code, livestock_id, name)
             VALUES ($1, $2, $3)
             ON CONFLICT (language_code, livestock_id)
             DO UPDATE SET name = $3, updated_at = NOW()`,
            [lang.code, livestockId, translation]
          );
        }
        
        await client.query('COMMIT');
        logger.info(`${lang.name} complete ✓ (${Object.keys(translations.crops).length} crops, ${Object.keys(translations.livestock).length} livestock)`);
      } catch (error) {
        await client.query('ROLLBACK');
        throw error;
      } finally {
        client.release();
      }
      
    } catch (error) {
      logger.error(`Failed to translate ${lang.name}:`, error);
    }
    
    await new Promise(resolve => setTimeout(resolve, 500));
  }
  
  // Final report
  const client = await pool.connect();
  try {
    const result = await client.query(`
      SELECT 
        (SELECT COUNT(DISTINCT language_code) FROM ui_translations) as ui_languages,
        (SELECT COUNT(DISTINCT language_code) FROM crop_translations) as crop_languages,
        (SELECT COUNT(DISTINCT language_code) FROM livestock_translations) as livestock_languages
    `);
    
    const stats = result.rows[0];
    logger.info('\n=== FINAL TRANSLATION STATISTICS ===');
    logger.info(`UI Translations: ${stats.ui_languages} languages ✓`);
    logger.info(`Crop Translations: ${stats.crop_languages} languages ${stats.crop_languages === '53' ? '✓' : '(missing ' + (53 - parseInt(stats.crop_languages)) + ')'}`);
    logger.info(`Livestock Translations: ${stats.livestock_languages} languages ${stats.livestock_languages === '53' ? '✓' : '(missing ' + (53 - parseInt(stats.livestock_languages)) + ')'}`);
    
  } finally {
    client.release();
  }
}

// Main execution
if (require.main === module) {
  completeFinalLanguages()
    .then(() => {
      logger.info('\n🎉 ALL TRANSLATIONS COMPLETE! 🎉');
      process.exit(0);
    })
    .catch((error) => {
      logger.error('Script failed:', error);
      process.exit(1);
    });
}