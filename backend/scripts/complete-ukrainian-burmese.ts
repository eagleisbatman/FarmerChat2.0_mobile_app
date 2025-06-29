import Anthropic from '@anthropic-ai/sdk';
import { pool } from '../src/database';
import { logger } from '../src/utils/logger';

const anthropic = new Anthropic({
  apiKey: process.env.ANTHROPIC_API_KEY || '',
});

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

async function completeFinal() {
  logger.info('Completing final translations...');
  
  // Complete Ukrainian crops
  logger.info('\nTranslating Ukrainian crops...');
  const ukCropsResponse = await anthropic.messages.create({
    model: 'claude-3-5-sonnet-20241022',
    max_tokens: 3000,
    temperature: 0.1,
    messages: [{
      role: 'user',
      content: `Translate these agricultural crop names from English to Ukrainian (Українська).
Use common farmer terms. Return ONLY valid JSON mapping IDs to translations:
${JSON.stringify(CROPS, null, 2)}`
    }]
  });
  
  const ukCrops = JSON.parse(ukCropsResponse.content[0].type === 'text' ? 
    ukCropsResponse.content[0].text.match(/\{[\s\S]*\}/)?.[0] || '{}' : '{}');
  
  // Complete Ukrainian livestock
  logger.info('Translating Ukrainian livestock...');
  const ukLivestockResponse = await anthropic.messages.create({
    model: 'claude-3-5-sonnet-20241022',
    max_tokens: 2000,
    temperature: 0.1,
    messages: [{
      role: 'user',
      content: `Translate these farm animal/livestock names from English to Ukrainian (Українська).
Use common farmer terms. Return ONLY valid JSON mapping IDs to translations:
${JSON.stringify(LIVESTOCK, null, 2)}`
    }]
  });
  
  const ukLivestock = JSON.parse(ukLivestockResponse.content[0].type === 'text' ? 
    ukLivestockResponse.content[0].text.match(/\{[\s\S]*\}/)?.[0] || '{}' : '{}');
  
  // Complete Burmese livestock (missing)
  logger.info('Translating Burmese livestock...');
  const myLivestockResponse = await anthropic.messages.create({
    model: 'claude-3-5-sonnet-20241022',
    max_tokens: 2000,
    temperature: 0.1,
    messages: [{
      role: 'user',
      content: `Translate these farm animal/livestock names from English to Burmese (မြန်မာ).
Use common farmer terms. Return ONLY valid JSON mapping IDs to translations:
${JSON.stringify(LIVESTOCK, null, 2)}`
    }]
  });
  
  const myLivestock = JSON.parse(myLivestockResponse.content[0].type === 'text' ? 
    myLivestockResponse.content[0].text.match(/\{[\s\S]*\}/)?.[0] || '{}' : '{}');
  
  // Save all to database
  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    
    // Ukrainian crops
    for (const [cropId, translation] of Object.entries(ukCrops)) {
      await client.query(
        `INSERT INTO crop_translations (language_code, crop_id, name)
         VALUES ($1, $2, $3)
         ON CONFLICT (language_code, crop_id)
         DO UPDATE SET name = $3, updated_at = NOW()`,
        ['uk', cropId, translation]
      );
    }
    logger.info(`Saved ${Object.keys(ukCrops).length} Ukrainian crops`);
    
    // Ukrainian livestock
    for (const [livestockId, translation] of Object.entries(ukLivestock)) {
      await client.query(
        `INSERT INTO livestock_translations (language_code, livestock_id, name)
         VALUES ($1, $2, $3)
         ON CONFLICT (language_code, livestock_id)
         DO UPDATE SET name = $3, updated_at = NOW()`,
        ['uk', livestockId, translation]
      );
    }
    logger.info(`Saved ${Object.keys(ukLivestock).length} Ukrainian livestock`);
    
    // Burmese livestock
    for (const [livestockId, translation] of Object.entries(myLivestock)) {
      await client.query(
        `INSERT INTO livestock_translations (language_code, livestock_id, name)
         VALUES ($1, $2, $3)
         ON CONFLICT (language_code, livestock_id)
         DO UPDATE SET name = $3, updated_at = NOW()`,
        ['my', livestockId, translation]
      );
    }
    logger.info(`Saved ${Object.keys(myLivestock).length} Burmese livestock`);
    
    await client.query('COMMIT');
    
    // Final check
    const result = await client.query(`
      SELECT 
        (SELECT COUNT(DISTINCT language_code) FROM ui_translations) as ui,
        (SELECT COUNT(DISTINCT language_code) FROM crop_translations) as crops,
        (SELECT COUNT(DISTINCT language_code) FROM livestock_translations) as livestock
    `);
    
    const stats = result.rows[0];
    logger.info('\n🎉 FINAL RESULTS 🎉');
    logger.info(`✅ UI Translations: ${stats.ui}/53 languages`);
    logger.info(`✅ Crop Translations: ${stats.crops}/53 languages`);
    logger.info(`✅ Livestock Translations: ${stats.livestock}/53 languages`);
    
  } catch (error) {
    await client.query('ROLLBACK');
    throw error;
  } finally {
    client.release();
  }
}

completeFinal()
  .then(() => process.exit(0))
  .catch(error => {
    logger.error('Failed:', error);
    process.exit(1);
  });