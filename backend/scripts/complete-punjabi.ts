import Anthropic from '@anthropic-ai/sdk';
import { pool } from '../src/database';
import { logger } from '../src/utils/logger';

const anthropic = new Anthropic({
  apiKey: process.env.ANTHROPIC_API_KEY || '',
});

async function completePunjabiTranslations() {
  logger.info('Completing remaining Punjabi UI translations...\n');
  
  // Get missing Punjabi keys
  const missingResult = await pool.query(`
    SELECT en.key, en.translation
    FROM ui_translations en
    WHERE en.language_code = 'en'
    AND en.key NOT IN (
      SELECT key FROM ui_translations WHERE language_code = 'pa'
    )
    ORDER BY en.key
  `);
  
  logger.info(`Found ${missingResult.rows.length} missing Punjabi translations`);
  
  if (missingResult.rows.length === 0) {
    logger.info('No missing translations!');
    return;
  }
  
  // Prepare translations
  const keysToTranslate: Record<string, string> = {};
  for (const row of missingResult.rows) {
    keysToTranslate[row.key] = row.translation;
  }
  
  try {
    const prompt = `Translate these UI strings from English to Punjabi (ਪੰਜਾਬੀ).
CRITICAL: "FarmerChat" is a brand name - NEVER translate it, keep it as "FarmerChat".
Use the Gurmukhi script for Punjabi.
Keep translations natural and concise for mobile UI.
Preserve any %s, %d format specifiers exactly as they appear.

Return ONLY a valid JSON object mapping keys to Punjabi translations:
${JSON.stringify(keysToTranslate, null, 2)}`;

    const response = await anthropic.messages.create({
      model: 'claude-3-5-sonnet-20241022',
      max_tokens: 4000,
      temperature: 0.1,
      messages: [{ role: 'user', content: prompt }]
    });
    
    const content = response.content[0].type === 'text' ? response.content[0].text : '';
    const jsonMatch = content.match(/\{[\s\S]*\}/);
    if (!jsonMatch) {
      logger.error('No JSON found in response');
      return;
    }
    
    const translations = JSON.parse(jsonMatch[0]);
    
    // Save translations
    const client = await pool.connect();
    try {
      await client.query('BEGIN');
      
      let savedCount = 0;
      for (const [key, translation] of Object.entries(translations)) {
        // Special handling for APP_NAME
        const finalTranslation = key === 'APP_NAME' ? 'FarmerChat' : String(translation);
        
        await client.query(
          `INSERT INTO ui_translations (language_code, key, translation)
           VALUES ($1, $2, $3)
           ON CONFLICT (language_code, key)
           DO UPDATE SET translation = $3, updated_at = NOW()`,
          ['pa', key, finalTranslation]
        );
        savedCount++;
      }
      
      await client.query('COMMIT');
      logger.info(`✅ Saved ${savedCount} Punjabi translations`);
    } catch (error) {
      await client.query('ROLLBACK');
      logger.error('Failed to save translations:', error);
    } finally {
      client.release();
    }
  } catch (error) {
    logger.error('Failed to translate:', error);
  }
  
  // Final verification
  const verifyResult = await pool.query(`
    SELECT 
      COUNT(DISTINCT key) as translated_keys,
      195 as expected_keys,
      195 - COUNT(DISTINCT key) as missing_keys
    FROM ui_translations
    WHERE language_code = 'pa'
  `);
  
  const stats = verifyResult.rows[0];
  logger.info('\n=== FINAL STATUS ===');
  logger.info(`Punjabi UI translations: ${stats.translated_keys}/${stats.expected_keys}`);
  if (stats.missing_keys === 0) {
    logger.info('✅ ALL PUNJABI TRANSLATIONS COMPLETE!');
  } else {
    logger.info(`❌ Still missing ${stats.missing_keys} translations`);
  }
}

// Main execution
if (require.main === module) {
  completePunjabiTranslations()
    .then(() => {
      logger.info('\n🎉 Punjabi translation completion finished! 🎉');
      process.exit(0);
    })
    .catch(error => {
      logger.error('Script failed:', error);
      process.exit(1);
    });
}