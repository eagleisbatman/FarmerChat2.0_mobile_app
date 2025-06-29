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

// Languages to translate (same as UI translations)
const SUPPORTED_LANGUAGES = [
  { code: 'es', name: 'Spanish', native: 'Español' },
  { code: 'zh', name: 'Chinese (Simplified)', native: '中文' },
  { code: 'ar', name: 'Arabic', native: 'العربية' },
  { code: 'fr', name: 'French', native: 'Français' },
  { code: 'pt', name: 'Portuguese', native: 'Português' },
  { code: 'ru', name: 'Russian', native: 'Русский' },
  { code: 'de', name: 'German', native: 'Deutsch' },
  { code: 'ja', name: 'Japanese', native: '日本語' },
  { code: 'ko', name: 'Korean', native: '한국어' },
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
  { code: 'ur', name: 'Urdu', native: 'اردو' },
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
  { code: 'lo', name: 'Lao', native: 'ລາວ' },
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
  { code: 'he', name: 'Hebrew', native: 'עברית' },
  { code: 'fa', name: 'Persian', native: 'فارسی' }
];

// Crops from CropsManager.kt
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

// Livestock from LivestockManager.kt
const LIVESTOCK = [
  'cow', 'buffalo', 'goat', 'sheep', 'pig', 'chicken', 'duck', 'turkey',
  'goose', 'quail', 'rabbit', 'horse', 'donkey', 'mule', 'camel', 'yak',
  'fish', 'shrimp', 'crab', 'bee', 'silkworm'
];

interface TranslationBatch {
  type: 'crops' | 'livestock';
  items: string[];
  sourceLanguage: string;
  targetLanguage: string;
  targetLanguageName: string;
  nativeName: string;
}

class CropLivestockTranslator {
  
  async translateBatch(batch: TranslationBatch): Promise<Record<string, string>> {
    const itemType = batch.type === 'crops' ? 'crop' : 'livestock/animal';
    const context = batch.type === 'crops' 
      ? 'agricultural crops grown by farmers'
      : 'farm animals and livestock raised by farmers';
    
    const prompt = `You are a professional translator specializing in agricultural terminology.
    
Translate the following ${itemType} names from ${batch.sourceLanguage} to ${batch.targetLanguageName} (${batch.nativeName}).

Context: These are ${context}. Use the most common and widely understood terms that farmers in the target region would use.

CRITICAL RULES:
1. Use the locally common name for each ${itemType}
2. If a ${itemType} is not commonly known in the target region, use the closest equivalent or transliteration
3. For technical/scientific names, prefer the common local name over direct translation
4. Keep names concise and practical
5. Return ONLY a valid JSON object mapping the original ID to the translated name

Special considerations:
- For crops: Use names farmers would use in markets and fields
- For livestock: Use common colloquial names, not scientific terminology
- If multiple varieties exist (e.g., different types of rice), use the general term
- For region-specific items, provide the most appropriate local equivalent

Items to translate:
${JSON.stringify(batch.items, null, 2)}

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
      logger.error(`Translation error for ${batch.type} in ${batch.targetLanguage}:`, error);
      throw error;
    }
  }
}

async function loadExistingCropTranslations(): Promise<Map<string, Set<string>>> {
  const client = await pool.connect();
  try {
    const result = await client.query(
      'SELECT language_code, crop_id FROM crop_translations'
    );
    
    const translations = new Map<string, Set<string>>();
    
    for (const row of result.rows) {
      if (!translations.has(row.language_code)) {
        translations.set(row.language_code, new Set());
      }
      translations.get(row.language_code)!.add(row.crop_id);
    }
    
    return translations;
  } finally {
    client.release();
  }
}

async function loadExistingLivestockTranslations(): Promise<Map<string, Set<string>>> {
  const client = await pool.connect();
  try {
    const result = await client.query(
      'SELECT language_code, livestock_id FROM livestock_translations'
    );
    
    const translations = new Map<string, Set<string>>();
    
    for (const row of result.rows) {
      if (!translations.has(row.language_code)) {
        translations.set(row.language_code, new Set());
      }
      translations.get(row.language_code)!.add(row.livestock_id);
    }
    
    return translations;
  } finally {
    client.release();
  }
}

async function generateCropLivestockTranslations() {
  if (!ANTHROPIC_API_KEY) {
    throw new Error('ANTHROPIC_API_KEY environment variable is required');
  }
  
  const translator = new CropLivestockTranslator();
  const existingCropTranslations = await loadExistingCropTranslations();
  const existingLivestockTranslations = await loadExistingLivestockTranslations();
  
  logger.info('Starting crop and livestock translations...');
  
  for (const lang of SUPPORTED_LANGUAGES) {
    logger.info(`\nProcessing ${lang.name} (${lang.code})...`);
    
    // Process crops
    const existingCrops = existingCropTranslations.get(lang.code) || new Set();
    const missingCrops = CROPS.filter(crop => !existingCrops.has(crop));
    
    if (missingCrops.length > 0) {
      logger.info(`Translating ${missingCrops.length} crops...`);
      
      try {
        // Translate in batches of 20
        const batchSize = 20;
        for (let i = 0; i < missingCrops.length; i += batchSize) {
          const batch = missingCrops.slice(i, i + batchSize);
          
          const translations = await translator.translateBatch({
            type: 'crops',
            items: batch,
            sourceLanguage: 'English',
            targetLanguage: lang.code,
            targetLanguageName: lang.name,
            nativeName: lang.native
          });
          
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
            logger.info(`Saved crop batch ${Math.floor(i/batchSize) + 1}/${Math.ceil(missingCrops.length/batchSize)}`);
          } catch (error) {
            await client.query('ROLLBACK');
            throw error;
          } finally {
            client.release();
          }
          
          await new Promise(resolve => setTimeout(resolve, 300));
        }
      } catch (error) {
        logger.error(`Failed to translate crops for ${lang.name}:`, error);
      }
    } else {
      logger.info('All crops already translated ✓');
    }
    
    // Process livestock
    const existingLivestock = existingLivestockTranslations.get(lang.code) || new Set();
    const missingLivestock = LIVESTOCK.filter(animal => !existingLivestock.has(animal));
    
    if (missingLivestock.length > 0) {
      logger.info(`Translating ${missingLivestock.length} livestock...`);
      
      try {
        const translations = await translator.translateBatch({
          type: 'livestock',
          items: missingLivestock,
          sourceLanguage: 'English',
          targetLanguage: lang.code,
          targetLanguageName: lang.name,
          nativeName: lang.native
        });
        
        // Save to database
        const client = await pool.connect();
        try {
          await client.query('BEGIN');
          
          for (const [livestockId, translation] of Object.entries(translations)) {
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
      } catch (error) {
        logger.error(`Failed to translate livestock for ${lang.name}:`, error);
      }
    } else {
      logger.info('All livestock already translated ✓');
    }
    
    await new Promise(resolve => setTimeout(resolve, 500));
  }
  
  logger.info('\nCrop and livestock translation complete!');
}

// Generate coverage report
async function generateCoverageReport() {
  const client = await pool.connect();
  try {
    const cropResult = await client.query(`
      SELECT 
        language_code,
        COUNT(*) as translated_count
      FROM crop_translations
      GROUP BY language_code
      ORDER BY language_code
    `);
    
    const livestockResult = await client.query(`
      SELECT 
        language_code,
        COUNT(*) as translated_count
      FROM livestock_translations
      GROUP BY language_code
      ORDER BY language_code
    `);
    
    logger.info('\n=== Crop & Livestock Translation Coverage ===');
    logger.info(`Total crops: ${CROPS.length}`);
    logger.info(`Total livestock: ${LIVESTOCK.length}`);
    logger.info('\nLanguage | Crops | Livestock');
    logger.info('---------|-------|----------');
    
    const cropCounts = new Map(cropResult.rows.map(r => [r.language_code, r.translated_count]));
    const livestockCounts = new Map(livestockResult.rows.map(r => [r.language_code, r.translated_count]));
    
    for (const lang of [{ code: 'en', name: 'English' }, ...SUPPORTED_LANGUAGES]) {
      const crops = cropCounts.get(lang.code) || 0;
      const livestock = livestockCounts.get(lang.code) || 0;
      const langName = `${lang.name} (${lang.code})`;
      logger.info(`${langName.padEnd(25)} | ${crops.toString().padStart(5)} | ${livestock.toString().padStart(9)}`);
    }
  } finally {
    client.release();
  }
}

// Main execution
if (require.main === module) {
  generateCropLivestockTranslations()
    .then(() => generateCoverageReport())
    .then(() => {
      logger.info('\nCrop and livestock translation script completed!');
      process.exit(0);
    })
    .catch((error) => {
      logger.error('Script failed:', error);
      process.exit(1);
    });
}

export { generateCropLivestockTranslations, generateCoverageReport };