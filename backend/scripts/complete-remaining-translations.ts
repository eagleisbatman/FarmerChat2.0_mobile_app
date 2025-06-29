import Anthropic from '@anthropic-ai/sdk';
import { pool } from '../src/database';
import { logger } from '../src/utils/logger';

// Configuration
const ANTHROPIC_API_KEY = process.env.ANTHROPIC_API_KEY || '';

// Initialize Anthropic client
const anthropic = new Anthropic({
  apiKey: ANTHROPIC_API_KEY,
});

// Remaining languages that need crop/livestock translations
const REMAINING_LANGUAGES = [
  { code: 'cs', name: 'Czech', native: 'Čeština' },
  { code: 'da', name: 'Danish', native: 'Dansk' },
  { code: 'el', name: 'Greek', native: 'Ελληνικά' },
  { code: 'fa', name: 'Persian', native: 'فارسی' },
  { code: 'fi', name: 'Finnish', native: 'Suomi' },
  { code: 'he', name: 'Hebrew', native: 'עברית' },
  { code: 'hu', name: 'Hungarian', native: 'Magyar' },
  { code: 'it', name: 'Italian', native: 'Italiano' },
  { code: 'nl', name: 'Dutch', native: 'Nederlands' },
  { code: 'no', name: 'Norwegian', native: 'Norsk' },
  { code: 'pl', name: 'Polish', native: 'Polski' },
  { code: 'ro', name: 'Romanian', native: 'Română' },
  { code: 'sv', name: 'Swedish', native: 'Svenska' },
  { code: 'tr', name: 'Turkish', native: 'Türkçe' },
  { code: 'uk', name: 'Ukrainian', native: 'Українська' }
];

// Crops and livestock data (same as original)
const CROPS = [
  'rice', 'wheat', 'maize', 'millet', 'sorghum', 'barley', 'cotton', 'sugarcane',
  'jute', 'coffee', 'tea', 'rubber', 'coconut', 'groundnut', 'mustard', 'sunflower',
  'soybean', 'sesame', 'safflower', 'lentil', 'chickpea', 'pigeon_pea', 'black_gram',
  'green_gram', 'kidney_bean', 'potato', 'tomato', 'onion', 'brinjal', 'okra',
  'cabbage', 'cauliflower', 'carrot', 'radish', 'peas', 'beans', 'spinach',
  'coriander', 'chilli', 'garlic', 'ginger', 'turmeric', 'mango', 'banana',
  'apple', 'orange', 'grapes', 'papaya', 'guava', 'pomegranate', 'watermelon',
  'custard_apple', 'sapota', 'jackfruit', 'cashew'
];

const LIVESTOCK = [
  'cow', 'buffalo', 'goat', 'sheep', 'pig', 'chicken', 'duck', 'turkey',
  'goose', 'quail', 'rabbit', 'horse', 'donkey', 'mule', 'camel', 'yak',
  'fish', 'shrimp', 'crab', 'bee', 'silkworm'
];

async function translateBatch(type: 'crops' | 'livestock', items: string[], lang: any): Promise<Record<string, string>> {
  const itemType = type === 'crops' ? 'crop' : 'livestock/animal';
  const context = type === 'crops' 
    ? 'agricultural crops grown by farmers'
    : 'farm animals and livestock raised by farmers';
  
  const prompt = `You are a professional translator specializing in agricultural terminology.
  
Translate the following ${itemType} names from English to ${lang.name} (${lang.native}).

Context: These are ${context}. Use the most common and widely understood terms that farmers in the target region would use.

CRITICAL RULES:
1. Use the locally common name for each ${itemType}
2. If a ${itemType} is not commonly known in the target region, use the closest equivalent or transliteration
3. For technical/scientific names, prefer the common local name over direct translation
4. Keep names concise and practical
5. Return ONLY a valid JSON object mapping the original ID to the translated name

Items to translate:
${JSON.stringify(items, null, 2)}

Return ONLY this JSON format:
{
  "rice": "translated name",
  "wheat": "translated name",
  ...
}`;
  
  try {
    const response = await anthropic.messages.create({
      model: 'claude-3-5-sonnet-20241022',
      max_tokens: 2000,
      temperature: 0.1,
      messages: [
        {
          role: 'user',
          content: prompt
        }
      ]
    });
    
    const content = response.content[0].type === 'text' ? response.content[0].text : '';
    const jsonMatch = content.match(/\{[\s\S]*\}/);
    
    if (jsonMatch) {
      return JSON.parse(jsonMatch[0]);
    } else {
      throw new Error('No valid JSON found in response');
    }
  } catch (error) {
    logger.error(`Translation error for ${type} in ${lang.code}:`, error);
    throw error;
  }
}

async function completeRemainingTranslations() {
  if (!ANTHROPIC_API_KEY) {
    throw new Error('ANTHROPIC_API_KEY environment variable is required');
  }
  
  logger.info(`Starting translations for ${REMAINING_LANGUAGES.length} remaining languages...`);
  
  for (const lang of REMAINING_LANGUAGES) {
    logger.info(`\nProcessing ${lang.name} (${lang.code})...`);
    
    try {
      // Translate crops in batches of 20
      logger.info(`Translating ${CROPS.length} crops...`);
      const batchSize = 20;
      for (let i = 0; i < CROPS.length; i += batchSize) {
        const batch = CROPS.slice(i, i + batchSize);
        
        const translations = await translateBatch('crops', batch, lang);
        
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
              [lang.code, cropId, translation]
            );
          }
          
          await client.query('COMMIT');
          logger.info(`Saved crop batch ${Math.floor(i/batchSize) + 1}/${Math.ceil(CROPS.length/batchSize)}`);
        } catch (error) {
          await client.query('ROLLBACK');
          throw error;
        } finally {
          client.release();
        }
        
        await new Promise(resolve => setTimeout(resolve, 300));
      }
      
      // Translate livestock
      logger.info(`Translating ${LIVESTOCK.length} livestock...`);
      const livestockTranslations = await translateBatch('livestock', LIVESTOCK, lang);
      
      // Save livestock to database
      const client = await pool.connect();
      try {
        await client.query('BEGIN');
        
        for (const [livestockId, translation] of Object.entries(livestockTranslations)) {
          await client.query(
            `INSERT INTO livestock_translations (language_code, livestock_id, name)
             VALUES ($1, $2, $3)
             ON CONFLICT (language_code, livestock_id)
             DO UPDATE SET name = $3, updated_at = NOW()`,
            [lang.code, livestockId, translation]
          );
        }
        
        await client.query('COMMIT');
        logger.info('Saved livestock translations ✓');
      } catch (error) {
        await client.query('ROLLBACK');
        throw error;
      } finally {
        client.release();
      }
      
      logger.info(`${lang.name} translation complete ✓`);
    } catch (error) {
      logger.error(`Failed to translate ${lang.name}:`, error);
    }
    
    await new Promise(resolve => setTimeout(resolve, 500));
  }
  
  logger.info('\nRemaining translations complete!');
}

// Generate final report
async function generateFinalReport() {
  const client = await pool.connect();
  try {
    const result = await client.query(`
      SELECT 
        'UI Translations' as type,
        COUNT(DISTINCT language_code) as languages,
        COUNT(*) as total_entries
      FROM ui_translations
      
      UNION ALL
      
      SELECT 
        'Crop Translations' as type,
        COUNT(DISTINCT language_code) as languages,
        COUNT(*) as total_entries
      FROM crop_translations
      
      UNION ALL
      
      SELECT 
        'Livestock Translations' as type,
        COUNT(DISTINCT language_code) as languages,
        COUNT(*) as total_entries
      FROM livestock_translations
    `);
    
    logger.info('\n=== FINAL TRANSLATION COVERAGE ===');
    for (const row of result.rows) {
      logger.info(`${row.type}: ${row.languages} languages, ${row.total_entries} total entries`);
    }
    
    // Check if all 53 languages have crops/livestock
    const missingCrops = await client.query(`
      SELECT COUNT(*) as missing FROM (
        SELECT DISTINCT language_code FROM ui_translations
        EXCEPT
        SELECT DISTINCT language_code FROM crop_translations
      ) AS missing_langs
    `);
    
    const missingLivestock = await client.query(`
      SELECT COUNT(*) as missing FROM (
        SELECT DISTINCT language_code FROM ui_translations
        EXCEPT
        SELECT DISTINCT language_code FROM livestock_translations
      ) AS missing_langs
    `);
    
    logger.info(`\nLanguages missing crop translations: ${missingCrops.rows[0].missing}`);
    logger.info(`Languages missing livestock translations: ${missingLivestock.rows[0].missing}`);
    
  } finally {
    client.release();
  }
}

// Main execution
if (require.main === module) {
  completeRemainingTranslations()
    .then(() => generateFinalReport())
    .then(() => {
      logger.info('\nAll translations completed successfully! 🎉');
      process.exit(0);
    })
    .catch((error) => {
      logger.error('Script failed:', error);
      process.exit(1);
    });
}