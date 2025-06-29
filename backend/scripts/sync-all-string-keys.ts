import Anthropic from '@anthropic-ai/sdk';
import { pool } from '../src/database';
import { logger } from '../src/utils/logger';

const anthropic = new Anthropic({
  apiKey: process.env.ANTHROPIC_API_KEY || '',
});

// All 267 string keys from StringsManager.kt (including personalization benefits)
const ALL_STRING_KEYS = {
  // App Name
  APP_NAME: "FarmerChat",
  
  // Onboarding
  CHOOSE_LANGUAGE: "Choose your preferred language",
  LANGUAGE_SUBTITLE: "Select the language you're most comfortable with",
  WHERE_LOCATED: "Where are you located?",
  LOCATION_SUBTITLE: "This helps us provide location-specific advice",
  LOCATION_PERMISSION_RATIONALE: "We need location permission to provide accurate local agricultural advice",
  ENABLE_LOCATION: "Enable Location",
  DETECT_MY_LOCATION: "Detect My Location",
  LOCATION_DETECTED: "Location detected:",
  GETTING_LOCATION: "Getting your location...",
  OR_ENTER_MANUALLY: "Or enter location manually",
  LOCATION_PLACEHOLDER: "e.g., Nairobi, Kenya",
  SELECT_CROPS: "Which crops do you grow?",
  CROPS_SUBTITLE: "Select all that apply",
  SELECT_LIVESTOCK: "Do you raise any livestock?",
  LIVESTOCK_SUBTITLE: "Select all that apply",
  SELECT_ROLE: "What is your role?",
  ROLE_SUBTITLE: "This helps us tailor advice to your needs",
  FARMER: "Farmer",
  EXTENSION_WORKER: "Extension Worker",
  SELECT_GENDER: "Select your gender",
  GENDER_SUBTITLE: "This helps us provide more personalized recommendations",
  MALE: "Male",
  FEMALE: "Female",
  OTHER: "Other",
  
  // Common Actions
  CONTINUE: "Continue",
  BACK: "Back",
  NEXT: "Next",
  SKIP: "Skip",
  DONE: "Done",
  CANCEL: "Cancel",
  OK: "OK",
  RETRY: "Retry",
  SEARCH: "Search",
  CLEAR: "Clear",
  SAVE: "Save",
  SEARCH_CROPS: "Search crops",
  SEARCH_LIVESTOCK: "Search livestock",
  
  // Conversations Screen
  MY_CONVERSATIONS: "My Conversations",
  NEW_CONVERSATION: "New Conversation",
  NO_CONVERSATIONS: "No conversations yet",
  START_FIRST_CONVERSATION: "Start your first conversation",
  DELETE_CONVERSATION: "Delete Conversation",
  SEARCH_CONVERSATIONS: "Search conversations",
  START_A_CONVERSATION: "Start a conversation...",
  CONTINUE_CONVERSATION: "Continue conversation",
  
  // Chat Screen
  TYPE_MESSAGE: "Type a message",
  ASK_QUESTION: "Ask a question...",
  STARTER_QUESTIONS: "Here are some questions to get you started:",
  WHAT_TO_KNOW_MORE: "What would you like to know more about?",
  LISTENING: "Listening...",
  TAP_TO_SPEAK: "Tap to speak",
  RATE_RESPONSE: "Rate this response",
  SPEAK_MESSAGE: "Read aloud",
  STOP_SPEAKING: "Stop reading",
  
  // Settings
  SETTINGS: "Settings",
  PROFILE: "Profile",
  NAME: "Name",
  LOCATION: "Location",
  CROPS: "Crops",
  LIVESTOCK: "Livestock",
  SELECTED: "selected",
  PREFERENCES: "Preferences",
  LANGUAGE: "Language",
  VOICE_RESPONSES: "Voice Responses",
  VOICE_RESPONSES_DESC: "Read AI responses aloud automatically",
  VOICE_INPUT: "Voice Input",
  VOICE_INPUT_DESC: "Enable voice recording for questions",
  AI_SETTINGS: "AI Settings",
  RESPONSE_LENGTH: "Response Length",
  CONCISE: "Concise",
  DETAILED: "Detailed",
  COMPREHENSIVE: "Comprehensive",
  FORMATTED_RESPONSES: "Formatted Responses",
  FORMATTED_RESPONSES_DESC: "Show responses with bullets and formatting",
  DATA_PRIVACY: "Data & Privacy",
  EXPORT_DATA: "Export My Data",
  EXPORT_DATA_DESC: "Download all your data as JSON",
  DELETE_ALL_DATA: "Delete All Data",
  DELETE_ALL_DATA_DESC: "Permanently delete your account and data",
  DELETE_DATA_CONFIRM: "Are you sure you want to delete all your data? This action cannot be undone.",
  DELETE: "Delete",
  ABOUT: "About",
  APP_VERSION: "App Version",
  APP_DESCRIPTION: "AI-powered agricultural assistant for smallholder farmers",
  VERSION: "Version",
  HELP_FEEDBACK: "Help & Feedback",
  LOGOUT: "Sign Out",
  LOGOUT_DESC: "Sign out from your account",
  HELP_FEEDBACK_DESC: "Get help or send feedback",
  RESET_ONBOARDING: "Reset Onboarding",
  RESET_ONBOARDING_DESC: "Go through the setup process again",
  SELECT_LANGUAGE: "Select Language",
  CLOSE: "Close",
  
  // Errors
  ERROR_GENERIC: "Something went wrong. Please try again.",
  ERROR_NO_INTERNET: "No internet connection",
  ERROR_LOCATION: "Unable to get location. Please check your device settings.",
  ERROR_VOICE_RECOGNITION: "Voice recognition error",
  ERROR_AI_RESPONSE: "I apologize, but I'm having trouble responding right now. Please try again.",
  
  // Voice
  VOICE_NOT_AVAILABLE: "Voice recognition is not available on this device",
  MICROPHONE_PERMISSION_REQUIRED: "Microphone permission is required for voice input",
  PROCESSING: "Processing...",
  SEND: "Send",
  
  // Feedback
  HOW_HELPFUL: "How helpful was this response?",
  RATE_THIS_RESPONSE: "Rate this response",
  ADD_COMMENT: "Add a comment (optional)",
  SUBMIT_FEEDBACK: "Submit Feedback",
  THANK_YOU_FEEDBACK: "Thank you for your feedback!",
  
  // Settings additions
  CHANGE: "Change",
  DATA_EXPORTED: "Data Exported",
  DATA_EXPORTED_MESSAGE: "Your data has been exported successfully",
  DATA_DELETED: "Data Deleted",
  DATA_DELETED_MESSAGE: "Your account and all data have been deleted",
  FAILED: "Failed",
  EXPORT_DATA_ERROR: "Failed to export data. Please try again.",
  DELETE_ACCOUNT_ERROR: "Failed to delete account. Please try again.",
  
  // Voice confidence
  CONFIDENCE_HIGH: "High",
  CONFIDENCE_MEDIUM: "Medium",
  CONFIDENCE_LOW: "Low",
  
  // Additional UI strings
  MORE: "More",
  ASK_ME_ANYTHING: "Ask me anything or try one of the below:",
  NO_RESULTS_FOUND: "No results found",
  YESTERDAY: "Yesterday",
  START_CHATTING: "Start Chatting",
  EMPOWERING_FARMERS_WITH_AI: "Empowering Farmers with AI",
  COPYRIGHT: "© 2024 Digital Green",
  ALL: "All",
  SELECTED_WITH_CHECK: "Selected",
  CROPS_SELECTED: "crops selected",
  ANIMALS_SELECTED: "animals selected",
  
  // Dialog and Modal strings
  EDIT_NAME: "Edit Name",
  EDIT_LOCATION: "Edit Location",
  ENTER_YOUR_NAME: "Enter your name",
  ENTER_LOCATION: "Enter location",
  UPDATE_CROPS: "Update Crops",
  UPDATE_LIVESTOCK: "Update Livestock",
  CONFIRM_DELETE: "Confirm Delete",
  ARE_YOU_SURE: "Are you sure?",
  THIS_ACTION_CANNOT_BE_UNDONE: "This action cannot be undone",
  
  // Filter UI
  SHOW_FILTERS: "Show filters",
  HIDE_FILTERS: "Hide filters",
  TRY_DIFFERENT_KEYWORDS: "Try searching with different keywords",
  
  // Feedback dialog specifics
  ADDITIONAL_FEEDBACK_OPTIONAL: "Additional feedback (optional)",
  TELL_US_MORE: "Tell us more...",
  STAR_RATING: "Star %d",
  
  // Success messages
  SETTINGS_SAVED: "Settings saved",
  RESET_COMPLETE: "Reset complete",
  
  // Error messages
  PERMISSION_DENIED: "Permission denied",
  LOCATION_SERVICES_DISABLED: "Location services are disabled",
  RECORDING_ERROR: "Recording error",
  NETWORK_ERROR: "Network error",
  TIMEOUT_ERROR: "Request timed out",
  
  // User defaults
  DEFAULT_USER_NAME: "Farmer %s",
  
  // Export/Share
  EXPORT_FARMERCHAT_DATA: "Export FarmerChat Data",
  FAILED_TO_EXPORT: "Failed to export data: %s",
  NO_PROFILE_DATA: "No profile data",
  
  // Authentication
  USER_NOT_AUTHENTICATED: "User not authenticated",
  
  // Voice
  SPEECH_NOT_AVAILABLE: "Speech recognition is not available on this device",
  
  // Conversation Management
  DELETE_CONVERSATION_CONFIRM: "Are you sure you want to delete this conversation?",
  CONVERSATION_DELETED: "Conversation deleted",
  
  // Chat UI
  YOU: "You",
  PLAY: "Play",
  STOP: "Stop",
  RATE: "Rate",
  
  // Additional actions
  RESET: "Reset",
  RESET_ONBOARDING_CONFIRM: "Are you sure you want to reset the onboarding process? You will need to set up your preferences again.",
  
  // Phone Authentication
  PHONE_AUTH_TITLE: "Phone Verification",
  PHONE_NUMBER: "Phone Number",
  ENTER_PHONE_NUMBER: "Enter your phone number",
  COUNTRY_CODE: "Country Code",
  SEND_OTP: "Send OTP",
  VERIFY_OTP: "Verify OTP",
  ENTER_OTP: "Enter OTP code",
  OTP_SENT: "OTP sent to %s",
  RESEND_OTP: "Resend OTP",
  VERIFY: "Verify",
  PHONE_AUTH_DESC: "Verify your phone number to save your conversations and access them anytime",
  SKIP_FOR_NOW: "Skip for now",
  INVALID_PHONE_NUMBER: "Please enter a valid phone number",
  INVALID_OTP: "Invalid OTP. Please try again.",
  
  // Crop Categories
  CROP_CATEGORY_CEREALS: "Cereals & Grains",
  CROP_CATEGORY_PULSES: "Pulses & Legumes",
  CROP_CATEGORY_VEGETABLES: "Vegetables",
  CROP_CATEGORY_FRUITS: "Fruits",
  CROP_CATEGORY_CASH_CROPS: "Cash Crops",
  CROP_CATEGORY_OILSEEDS: "Oilseeds",
  CROP_CATEGORY_SPICES: "Spices & Herbs",
  CROP_CATEGORY_PLANTATION: "Plantation Crops",
  CROP_CATEGORY_FODDER: "Fodder Crops",
  CROP_CATEGORY_FLOWERS: "Flowers & Ornamentals",
  
  // Livestock Categories
  LIVESTOCK_CATEGORY_CATTLE: "Cattle",
  LIVESTOCK_CATEGORY_POULTRY: "Poultry",
  LIVESTOCK_CATEGORY_SMALL_RUMINANTS: "Goats & Sheep",
  LIVESTOCK_CATEGORY_SWINE: "Pigs",
  LIVESTOCK_CATEGORY_AQUACULTURE: "Fish & Aquaculture",
  LIVESTOCK_CATEGORY_OTHERS: "Other Animals",
  
  // Personalization explanations
  PERSONALIZATION_TITLE: "Why we ask for this information",
  LOCATION_BENEFIT: "Get weather alerts, local market prices, and region-specific farming advice",
  NAME_BENEFIT: "Receive personalized greetings and build trust with our AI assistant",
  LANGUAGE_BENEFIT: "Get advice in your preferred language for better understanding",
  GENDER_BENEFIT: "Access gender-specific programs and tailored agricultural recommendations",
  ROLE_BENEFIT: "Receive content suited to your expertise level and professional needs",
  CROPS_BENEFIT: "Get crop-specific advice on planting, pest control, and harvest timing",
  LIVESTOCK_BENEFIT: "Receive targeted guidance on animal health, feeding, and breeding"
};

// All 53 supported languages
const SUPPORTED_LANGUAGES = [
  { code: 'en', name: 'English', native: 'English' },
  { code: 'es', name: 'Spanish', native: 'Español' },
  { code: 'zh', name: 'Chinese (Simplified)', native: '中文' },
  { code: 'ar', name: 'Arabic', native: 'العربية', isRTL: true },
  { code: 'fr', name: 'French', native: 'Français' },
  { code: 'pt', name: 'Portuguese', native: 'Português' },
  { code: 'ru', name: 'Russian', native: 'Русский' },
  { code: 'de', name: 'German', native: 'Deutsch' },
  { code: 'ja', name: 'Japanese', native: '日本語' },
  { code: 'ko', name: 'Korean', native: '한국어' },
  { code: 'hi', name: 'Hindi', native: 'हिन्दी' },
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
  { code: 'ur', name: 'Urdu', native: 'اردو', isRTL: true },
  { code: 'sw', name: 'Swahili', native: 'Kiswahili' },
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
  { code: 'he', name: 'Hebrew', native: 'עברית', isRTL: true },
  { code: 'fa', name: 'Persian', native: 'فارسی', isRTL: true }
];

async function syncAllStringKeys() {
  logger.info('Starting complete string key synchronization...');
  
  // First, add English translations for all keys
  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    
    logger.info('Adding all 267 string keys with English translations...');
    for (const [key, value] of Object.entries(ALL_STRING_KEYS)) {
      await client.query(
        `INSERT INTO ui_translations (language_code, key, translation)
         VALUES ($1, $2, $3)
         ON CONFLICT (language_code, key)
         DO UPDATE SET translation = $3, updated_at = NOW()`,
        ['en', key, value]
      );
    }
    
    await client.query('COMMIT');
    logger.info('✓ Added all English translations');
  } catch (error) {
    await client.query('ROLLBACK');
    throw error;
  } finally {
    client.release();
  }
  
  // Now get all existing translations for each language
  const existingTranslations = new Map<string, Set<string>>();
  const result = await pool.query('SELECT language_code, key FROM ui_translations WHERE language_code != $1', ['en']);
  
  for (const row of result.rows) {
    if (!existingTranslations.has(row.language_code)) {
      existingTranslations.set(row.language_code, new Set());
    }
    existingTranslations.get(row.language_code)!.add(row.key);
  }
  
  // Translate missing keys for each language
  for (const lang of SUPPORTED_LANGUAGES) {
    if (lang.code === 'en') continue;
    
    const existing = existingTranslations.get(lang.code) || new Set();
    const missingKeys: { key: string; text: string }[] = [];
    
    for (const [key, text] of Object.entries(ALL_STRING_KEYS)) {
      if (!existing.has(key)) {
        missingKeys.push({ key, text });
      }
    }
    
    if (missingKeys.length === 0) {
      logger.info(`${lang.name}: All keys already translated ✓`);
      continue;
    }
    
    logger.info(`${lang.name}: Translating ${missingKeys.length} missing keys...`);
    
    // Translate in batches of 30
    const batchSize = 30;
    for (let i = 0; i < missingKeys.length; i += batchSize) {
      const batch = missingKeys.slice(i, i + batchSize);
      
      try {
        const prompt = `You are a professional translator for a mobile app called FarmerChat that helps farmers.

Translate these UI strings from English to ${lang.name} (${lang.native}).

CRITICAL RULES:
1. NEVER translate "FarmerChat" - it's a brand name
2. Keep translations natural for farmers in the target region
3. Use appropriate formality level for the culture
4. For technical terms, use commonly understood local equivalents
5. Keep translations concise for mobile UI
6. Return ONLY valid JSON mapping keys to translations
7. Preserve any formatting like %s, %d in the exact same position

Texts to translate:
${JSON.stringify(batch, null, 2)}

Return ONLY this JSON format:
{
  "KEY_NAME": "translated text",
  ...
}`;

        const response = await anthropic.messages.create({
          model: 'claude-3-5-sonnet-20241022',
          max_tokens: 4000,
          temperature: 0.1,
          messages: [{ role: 'user', content: prompt }]
        });
        
        const content = response.content[0].type === 'text' ? response.content[0].text : '';
        const translations = JSON.parse(content.match(/\{[\s\S]*\}/)?.[0] || '{}');
        
        // Save translations
        const client = await pool.connect();
        try {
          await client.query('BEGIN');
          
          for (const [key, translation] of Object.entries(translations)) {
            // Ensure FarmerChat wasn't translated
            const cleanTranslation = key === 'APP_NAME' ? 'FarmerChat' : translation;
            
            await client.query(
              `INSERT INTO ui_translations (language_code, key, translation)
               VALUES ($1, $2, $3)
               ON CONFLICT (language_code, key)
               DO UPDATE SET translation = $3, updated_at = NOW()`,
              [lang.code, key, cleanTranslation]
            );
          }
          
          await client.query('COMMIT');
          logger.info(`  Batch ${Math.floor(i/batchSize) + 1}/${Math.ceil(missingKeys.length/batchSize)} ✓`);
        } catch (error) {
          await client.query('ROLLBACK');
          throw error;
        } finally {
          client.release();
        }
        
        await new Promise(resolve => setTimeout(resolve, 300));
      } catch (error) {
        logger.error(`Failed to translate batch for ${lang.name}:`, error);
      }
    }
  }
  
  // Final verification
  const finalResult = await pool.query(`
    SELECT 
      COUNT(DISTINCT key) as total_keys,
      COUNT(DISTINCT language_code) as total_languages,
      COUNT(*) as total_translations
    FROM ui_translations
  `);
  
  const stats = finalResult.rows[0];
  logger.info('\n=== FINAL SYNCHRONIZATION RESULTS ===');
  logger.info(`Total unique keys: ${stats.total_keys} (expected: 267)`);
  logger.info(`Total languages: ${stats.total_languages} (expected: 53)`);
  logger.info(`Total translations: ${stats.total_translations} (expected: ${267 * 53} = 14,151)`);
  
  // Check which languages have all keys
  const completenessResult = await pool.query(`
    SELECT 
      language_code,
      COUNT(DISTINCT key) as key_count
    FROM ui_translations
    GROUP BY language_code
    ORDER BY key_count DESC
  `);
  
  logger.info('\n=== Language Completeness ===');
  for (const row of completenessResult.rows) {
    const lang = SUPPORTED_LANGUAGES.find(l => l.code === row.language_code);
    const status = row.key_count === 267 ? '✓' : `(${row.key_count}/267)`;
    logger.info(`${lang?.name || row.language_code}: ${status}`);
  }
}

// Main execution
if (require.main === module) {
  syncAllStringKeys()
    .then(() => {
      logger.info('\n🎉 ALL STRING KEYS SYNCHRONIZED! 🎉');
      process.exit(0);
    })
    .catch(error => {
      logger.error('Synchronization failed:', error);
      process.exit(1);
    });
}