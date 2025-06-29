import Anthropic from '@anthropic-ai/sdk';
import { pool } from '../src/database';
import { logger } from '../src/utils/logger';

const anthropic = new Anthropic({
  apiKey: process.env.ANTHROPIC_API_KEY || '',
});

async function completeRemainingTranslations() {
  logger.info('Completing remaining translations with error handling...\n');
  
  // First, let's get the exact status
  const statusResult = await pool.query(`
    WITH missing_translations AS (
      -- UI translations missing
      SELECT 
        'UI' as type,
        langs.language_code,
        langs.language_name,
        COUNT(DISTINCT en.key) - COUNT(DISTINCT t.key) as missing_count,
        STRING_AGG(
          CASE WHEN t.key IS NULL THEN en.key END, 
          ','
        ) as missing_keys
      FROM ui_translations en
      CROSS JOIN (
        VALUES 
          ('ig', 'Igbo'),
          ('kn', 'Kannada'), 
          ('or', 'Odia'),
          ('pa', 'Punjabi')
      ) AS langs(language_code, language_name)
      LEFT JOIN ui_translations t ON en.key = t.key AND t.language_code = langs.language_code
      WHERE en.language_code = 'en'
      GROUP BY langs.language_code, langs.language_name
      
      UNION ALL
      
      -- Crop translations missing
      SELECT 
        'Crops' as type,
        langs.language_code,
        langs.language_name,
        COUNT(DISTINCT en.crop_id) - COUNT(DISTINCT t.crop_id) as missing_count,
        STRING_AGG(
          CASE WHEN t.crop_id IS NULL THEN en.crop_id END, 
          ','
        ) as missing_keys
      FROM crop_translations en
      CROSS JOIN (
        VALUES 
          ('hi', 'Hindi'),
          ('sw', 'Swahili'),
          ('my', 'Burmese')
      ) AS langs(language_code, language_name)
      LEFT JOIN crop_translations t ON en.crop_id = t.crop_id AND t.language_code = langs.language_code
      WHERE en.language_code = 'en'
      GROUP BY langs.language_code, langs.language_name
    )
    SELECT * FROM missing_translations 
    WHERE missing_count > 0
    ORDER BY type, language_code
  `);
  
  logger.info('Current missing translations:');
  for (const row of statusResult.rows) {
    logger.info(`${row.type} - ${row.language_name} (${row.language_code}): ${row.missing_count} missing`);
  }
  
  // Process each missing translation
  for (const row of statusResult.rows) {
    if (!row.missing_keys) continue;
    
    const missingItems = row.missing_keys.split(',').filter(Boolean);
    if (missingItems.length === 0) continue;
    
    logger.info(`\nProcessing ${row.language_name} ${row.type}...`);
    
    if (row.type === 'UI') {
      await completeUITranslations(row.language_code, row.language_name, missingItems);
    } else if (row.type === 'Crops') {
      await completeCropTranslations(row.language_code, row.language_name, missingItems);
    }
  }
  
  // Final verification
  await verifyCompletion();
}

async function completeUITranslations(langCode: string, langName: string, missingKeys: string[]) {
  // Get English translations for missing keys
  const englishResult = await pool.query(
    'SELECT key, translation FROM ui_translations WHERE language_code = $1 AND key = ANY($2)',
    ['en', missingKeys]
  );
  
  const keysToTranslate: Record<string, string> = {};
  for (const row of englishResult.rows) {
    keysToTranslate[row.key] = row.translation;
  }
  
  logger.info(`Translating ${Object.keys(keysToTranslate).length} UI keys...`);
  
  // Process in smaller batches to avoid timeouts
  const batchSize = 30;
  const keys = Object.entries(keysToTranslate);
  
  for (let i = 0; i < keys.length; i += batchSize) {
    const batch = keys.slice(i, i + batchSize);
    const batchObj = Object.fromEntries(batch);
    
    try {
      const prompt = `Translate these UI strings from English to ${langName}.
IMPORTANT: "FarmerChat" is a brand name - NEVER translate it.
Keep translations natural and concise for mobile UI.
Preserve any %s, %d format specifiers exactly as they appear.

Return ONLY a valid JSON object mapping keys to translations:
${JSON.stringify(batchObj, null, 2)}`;

      const response = await anthropic.messages.create({
        model: 'claude-3-5-sonnet-20241022',
        max_tokens: 3000,
        temperature: 0.1,
        messages: [{ role: 'user', content: prompt }]
      });
      
      const content = response.content[0].type === 'text' ? response.content[0].text : '';
      const jsonMatch = content.match(/\{[\s\S]*\}/);
      if (!jsonMatch) {
        logger.error(`No JSON found in response for ${langName}`);
        continue;
      }
      
      const translations = JSON.parse(jsonMatch[0]);
      
      // Save each translation
      const client = await pool.connect();
      try {
        await client.query('BEGIN');
        
        for (const [key, translation] of Object.entries(translations)) {
          // Special handling for APP_NAME
          const finalTranslation = key === 'APP_NAME' ? 'FarmerChat' : String(translation);
          
          await client.query(
            `INSERT INTO ui_translations (language_code, key, translation)
             VALUES ($1, $2, $3)
             ON CONFLICT (language_code, key)
             DO UPDATE SET translation = $3, updated_at = NOW()`,
            [langCode, key, finalTranslation]
          );
        }
        
        await client.query('COMMIT');
        logger.info(`  Saved batch ${Math.floor(i/batchSize) + 1}/${Math.ceil(keys.length/batchSize)}`);
      } catch (error) {
        await client.query('ROLLBACK');
        logger.error(`Failed to save batch:`, error);
      } finally {
        client.release();
      }
      
      // Small delay to avoid rate limits
      await new Promise(resolve => setTimeout(resolve, 500));
    } catch (error) {
      logger.error(`Failed to translate batch for ${langName}:`, error);
    }
  }
}

async function completeCropTranslations(langCode: string, langName: string, missingCrops: string[]) {
  logger.info(`Translating ${missingCrops.length} crops...`);
  
  try {
    const prompt = `Translate these agricultural crop names from English to ${langName}.
Use terms that local farmers would understand and use.
Some crops may not have direct translations - use transliterations or common local terms.

Return ONLY a valid JSON object mapping crop IDs to their translated names:
${JSON.stringify(missingCrops, null, 2)}`;

    const response = await anthropic.messages.create({
      model: 'claude-3-5-sonnet-20241022',
      max_tokens: 2000,
      temperature: 0.1,
      messages: [{ role: 'user', content: prompt }]
    });
    
    const content = response.content[0].type === 'text' ? response.content[0].text : '';
    const jsonMatch = content.match(/\{[\s\S]*\}/);
    if (!jsonMatch) {
      logger.error(`No JSON found in crop response for ${langName}`);
      return;
    }
    
    const translations = JSON.parse(jsonMatch[0]);
    
    // Save translations
    const client = await pool.connect();
    try {
      await client.query('BEGIN');
      
      for (const [cropId, translation] of Object.entries(translations)) {
        await client.query(
          `INSERT INTO crop_translations (language_code, crop_id, name)
           VALUES ($1, $2, $3)
           ON CONFLICT (language_code, crop_id)
           DO UPDATE SET name = $3, updated_at = NOW()`,
          [langCode, cropId, String(translation)]
        );
      }
      
      await client.query('COMMIT');
      logger.info(`  Saved ${Object.keys(translations).length} crop translations`);
    } catch (error) {
      await client.query('ROLLBACK');
      logger.error(`Failed to save crops:`, error);
    } finally {
      client.release();
    }
  } catch (error) {
    logger.error(`Failed to translate crops for ${langName}:`, error);
  }
}

async function verifyCompletion() {
  logger.info('\n=== FINAL VERIFICATION ===\n');
  
  const result = await pool.query(`
    WITH completion_status AS (
      SELECT 
        'UI' as category,
        COUNT(DISTINCT language_code) as total_languages,
        COUNT(DISTINCT CASE WHEN key_count = 195 THEN language_code END) as complete_languages,
        STRING_AGG(
          CASE WHEN key_count < 195 
          THEN language_code || ' (' || key_count || '/195)' 
          END, ', '
        ) as incomplete
      FROM (
        SELECT language_code, COUNT(DISTINCT key) as key_count
        FROM ui_translations
        GROUP BY language_code
      ) ui_stats
      
      UNION ALL
      
      SELECT 
        'Crops' as category,
        COUNT(DISTINCT language_code) as total_languages,
        COUNT(DISTINCT CASE WHEN crop_count = 55 THEN language_code END) as complete_languages,
        STRING_AGG(
          CASE WHEN crop_count < 55 
          THEN language_code || ' (' || crop_count || '/55)' 
          END, ', '
        ) as incomplete
      FROM (
        SELECT language_code, COUNT(DISTINCT crop_id) as crop_count
        FROM crop_translations
        GROUP BY language_code
      ) crop_stats
      
      UNION ALL
      
      SELECT 
        'Livestock' as category,
        COUNT(DISTINCT language_code) as total_languages,
        COUNT(DISTINCT CASE WHEN livestock_count >= 10 THEN language_code END) as complete_languages,
        STRING_AGG(
          CASE WHEN livestock_count < 10 
          THEN language_code || ' (' || livestock_count || ')' 
          END, ', '
        ) as incomplete
      FROM (
        SELECT language_code, COUNT(DISTINCT livestock_id) as livestock_count
        FROM livestock_translations
        GROUP BY language_code
      ) livestock_stats
    )
    SELECT * FROM completion_status
  `);
  
  for (const row of result.rows) {
    const icon = row.complete_languages === row.total_languages ? '✅' : '⚠️';
    logger.info(`${icon} ${row.category}: ${row.complete_languages}/${row.total_languages} languages complete`);
    if (row.incomplete) {
      logger.info(`   Incomplete: ${row.incomplete}`);
    }
  }
  
  // Check critical keys
  const criticalResult = await pool.query(`
    SELECT 
      key,
      COUNT(DISTINCT language_code) as translated_languages
    FROM ui_translations
    WHERE key IN (
      'PHONE_AUTH_TITLE', 'PHONE_NUMBER', 'ENTER_PHONE_NUMBER',
      'SELECT_GENDER', 'MALE', 'FEMALE', 'OTHER',
      'SELECT_ROLE', 'FARMER', 'EXTENSION_WORKER'
    )
    GROUP BY key
    ORDER BY translated_languages DESC, key
  `);
  
  logger.info('\nCritical Phone/Gender/Role Keys:');
  for (const row of criticalResult.rows) {
    const icon = row.translated_languages >= 53 ? '✅' : '⚠️';
    logger.info(`${icon} ${row.key}: ${row.translated_languages}/53 languages`);
  }
}

// Main execution
if (require.main === module) {
  completeRemainingTranslations()
    .then(() => {
      logger.info('\n🎉 Translation completion process finished! 🎉');
      process.exit(0);
    })
    .catch(error => {
      logger.error('Script failed:', error);
      process.exit(1);
    });
}