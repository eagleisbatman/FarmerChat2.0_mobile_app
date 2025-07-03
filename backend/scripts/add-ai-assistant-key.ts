import { Pool } from 'pg';
import Anthropic from '@anthropic-ai/sdk';
import dotenv from 'dotenv';
import path from 'path';

dotenv.config({ path: path.resolve(__dirname, '../.env') });

const pool = new Pool({
  connectionString: process.env.DATABASE_URL
});

const anthropic = new Anthropic({
  apiKey: process.env.CLAUDE_API_KEY
});

const LANGUAGES = [
  { code: 'en', name: 'English' },
  { code: 'es', name: 'Spanish' },
  { code: 'zh', name: 'Chinese (Simplified)' },
  { code: 'ar', name: 'Arabic' },
  { code: 'fr', name: 'French' },
  { code: 'pt', name: 'Portuguese' },
  { code: 'ru', name: 'Russian' },
  { code: 'de', name: 'German' },
  { code: 'ja', name: 'Japanese' },
  { code: 'ko', name: 'Korean' },
  { code: 'hi', name: 'Hindi' },
  { code: 'bn', name: 'Bengali' },
  { code: 'te', name: 'Telugu' },
  { code: 'mr', name: 'Marathi' },
  { code: 'ta', name: 'Tamil' },
  { code: 'gu', name: 'Gujarati' },
  { code: 'kn', name: 'Kannada' },
  { code: 'ml', name: 'Malayalam' },
  { code: 'pa', name: 'Punjabi' },
  { code: 'or', name: 'Odia' },
  { code: 'as', name: 'Assamese' },
  { code: 'ur', name: 'Urdu' },
  { code: 'sw', name: 'Swahili' },
  { code: 'am', name: 'Amharic' },
  { code: 'ha', name: 'Hausa' },
  { code: 'yo', name: 'Yoruba' },
  { code: 'ig', name: 'Igbo' },
  { code: 'zu', name: 'Zulu' },
  { code: 'xh', name: 'Xhosa' },
  { code: 'af', name: 'Afrikaans' },
  { code: 'id', name: 'Indonesian' },
  { code: 'ms', name: 'Malay' },
  { code: 'th', name: 'Thai' },
  { code: 'vi', name: 'Vietnamese' },
  { code: 'fil', name: 'Filipino' },
  { code: 'km', name: 'Khmer' },
  { code: 'lo', name: 'Lao' },
  { code: 'my', name: 'Burmese' },
  { code: 'it', name: 'Italian' },
  { code: 'nl', name: 'Dutch' },
  { code: 'pl', name: 'Polish' },
  { code: 'uk', name: 'Ukrainian' },
  { code: 'ro', name: 'Romanian' },
  { code: 'el', name: 'Greek' },
  { code: 'cs', name: 'Czech' },
  { code: 'hu', name: 'Hungarian' },
  { code: 'sv', name: 'Swedish' },
  { code: 'da', name: 'Danish' },
  { code: 'fi', name: 'Finnish' },
  { code: 'no', name: 'Norwegian' },
  { code: 'tr', name: 'Turkish' },
  { code: 'he', name: 'Hebrew' },
  { code: 'fa', name: 'Persian' }
];

async function translateText(text: string, targetLanguage: string, languageName: string): Promise<string> {
  if (targetLanguage === 'en') return text;

  const prompt = `Translate the following UI text from English to ${languageName} (${targetLanguage}).
Keep it natural and appropriate for a mobile app UI.
Text: "${text}"
Return ONLY the translation, nothing else.`;

  try {
    const response = await anthropic.messages.create({
      model: 'claude-3-5-sonnet-20241022',
      max_tokens: 200,
      messages: [{ role: 'user', content: prompt }]
    });

    const content = response.content[0].type === 'text' ? response.content[0].text : '';
    return content.trim();
  } catch (error) {
    console.error(`Error translating to ${languageName}:`, error);
    return text; // Fallback to English
  }
}

async function main() {
  console.log('Adding AI_ASSISTANT key to all languages...\n');

  const englishText = 'AI Assistant';
  
  for (const lang of LANGUAGES) {
    console.log(`Processing ${lang.name} (${lang.code})...`);
    
    // Check if key already exists
    const existing = await pool.query(
      'SELECT 1 FROM ui_translations WHERE key = $1 AND language_code = $2',
      ['AI_ASSISTANT', lang.code]
    );
    
    if (existing.rows.length > 0) {
      console.log(`  Already exists, skipping...`);
      continue;
    }
    
    // Translate
    const translation = await translateText(englishText, lang.code, lang.name);
    
    // Insert
    await pool.query(
      'INSERT INTO ui_translations (key, language_code, translation) VALUES ($1, $2, $3)',
      ['AI_ASSISTANT', lang.code, translation]
    );
    
    console.log(`  ✓ Added: ${translation}`);
  }

  // Verify
  const count = await pool.query(
    'SELECT COUNT(*) FROM ui_translations WHERE key = $1',
    ['AI_ASSISTANT']
  );
  
  console.log(`\n✅ Done! AI_ASSISTANT key added to ${count.rows[0].count} languages.`);
  
  await pool.end();
}

main().catch(console.error);