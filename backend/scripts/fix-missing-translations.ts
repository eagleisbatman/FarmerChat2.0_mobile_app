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

const missingTranslations = {
  zh: [
    'TERMS_LAST_UPDATED',
    'TERMS_LIABILITY_CONTENT',
    'TERMS_LIABILITY_TITLE',
    'TERMS_PRIVACY_CONTENT',
    'TERMS_PRIVACY_TITLE',
    'TERMS_READ_TO_CONTINUE',
    'TERMS_SERVICE_DESC_CONTENT',
    'TERMS_SERVICE_DESC_TITLE',
    'TERMS_TERMINATION_CONTENT',
    'TERMS_TERMINATION_TITLE'
  ],
  ro: [
    'TERMS_LAST_UPDATED',
    'TERMS_LIABILITY_CONTENT',
    'TERMS_LIABILITY_TITLE',
    'TERMS_PRIVACY_CONTENT',
    'TERMS_PRIVACY_TITLE',
    'TERMS_READ_TO_CONTINUE',
    'TERMS_SERVICE_DESC_CONTENT',
    'TERMS_SERVICE_DESC_TITLE',
    'TERMS_TERMINATION_CONTENT',
    'TERMS_TERMINATION_TITLE',
    'TERMS_USER_ACCOUNTS_CONTENT',
    'TERMS_USER_ACCOUNTS_TITLE',
    'TIME_FORMAT',
    'UNEXPECTED_ERROR',
    'WHATSAPP_LABEL'
  ]
};

async function getEnglishTranslations(keys: string[]): Promise<Record<string, string>> {
  const result = await pool.query(
    'SELECT key, translation FROM ui_translations WHERE language_code = $1 AND key = ANY($2)',
    ['en', keys]
  );
  
  const translations: Record<string, string> = {};
  result.rows.forEach((row: any) => {
    translations[row.key] = row.translation;
  });
  
  return translations;
}

async function translateTexts(texts: Record<string, string>, targetLanguage: string, languageName: string): Promise<Record<string, string>> {
  const textArray = Object.entries(texts);
  const prompt = `You are a professional translator. Translate the following UI text from English to ${languageName} (${targetLanguage}).

Rules:
1. Preserve any placeholders like {variable}
2. Keep translations natural and appropriate for a mobile app UI
3. Be consistent with terminology
4. Return ONLY the translations in the exact same order

English texts to translate:
${textArray.map(([key, text], i) => `${i + 1}. "${text}"`).join('\n')}

Return ONLY the translations, one per line, in the same order:`;

  try {
    const response = await anthropic.messages.create({
      model: 'claude-3-5-sonnet-20241022',
      max_tokens: 4000,
      messages: [{ role: 'user', content: prompt }]
    });

    const content = response.content[0].type === 'text' ? response.content[0].text : '';
    const translations = content.trim().split('\n').filter(line => line.trim());
    
    const result: Record<string, string> = {};
    textArray.forEach(([key], index) => {
      if (translations[index]) {
        // Remove quotes and line numbers if present
        let translation = translations[index].trim();
        translation = translation.replace(/^\d+\.\s*/, ''); // Remove "1. " prefix
        translation = translation.replace(/^["']|["']$/g, ''); // Remove quotes
        result[key] = translation;
      }
    });
    
    return result;
  } catch (error) {
    console.error(`Error translating to ${languageName}:`, error);
    throw error;
  }
}

async function insertTranslations(translations: Record<string, string>, languageCode: string) {
  for (const [key, translation] of Object.entries(translations)) {
    await pool.query(
      'INSERT INTO ui_translations (key, language_code, translation) VALUES ($1, $2, $3) ON CONFLICT (key, language_code) DO UPDATE SET translation = $3',
      [key, languageCode, translation]
    );
  }
}

async function main() {
  console.log('Fixing missing translations...\n');

  for (const [langCode, missingKeys] of Object.entries(missingTranslations)) {
    const langName = langCode === 'zh' ? 'Chinese (Simplified)' : 'Romanian';
    console.log(`\nProcessing ${langName} (${langCode})...`);
    console.log(`Missing keys: ${missingKeys.length}`);

    // Get English translations
    const englishTexts = await getEnglishTranslations(missingKeys);
    console.log(`Found ${Object.keys(englishTexts).length} English translations`);

    // Translate
    console.log('Translating...');
    const translations = await translateTexts(englishTexts, langCode, langName);
    console.log(`Translated ${Object.keys(translations).length} texts`);

    // Insert
    await insertTranslations(translations, langCode);
    console.log(`✓ Inserted translations for ${langName}`);
  }

  // Final verification
  console.log('\n=== Final Verification ===');
  const result = await pool.query(`
    SELECT language_code, COUNT(*) as key_count 
    FROM ui_translations 
    WHERE language_code IN ('zh', 'ro')
    GROUP BY language_code 
    ORDER BY language_code
  `);
  
  result.rows.forEach((row: any) => {
    console.log(`${row.language_code}: ${row.key_count} keys`);
  });

  await pool.end();
  console.log('\n✅ Done!');
}

main().catch(console.error);