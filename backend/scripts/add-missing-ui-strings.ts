import { pool } from '../src/database';
import { logger } from '../src/utils/logger';

// New translation keys to add (based on our search findings)
const NEW_STRING_KEYS = {
  // Content Descriptions (Accessibility)
  BACK_BUTTON: "Back",
  SEARCH_BUTTON: "Search", 
  CLEAR_BUTTON: "Clear",
  SELECTED_ITEM: "Selected",
  DISCARD_RECORDING: "Discard recording",
  PAUSE_PLAYBACK: "Pause",
  PLAY_AUDIO: "Play",
  SEND_TRANSCRIPTION: "Send for transcription",
  SHARE_BUTTON: "Share",
  
  // Error Messages - ViewModels
  FAILED_SEND_VERIFICATION: "Failed to send verification code",
  PLEASE_ENTER_VERIFICATION: "Please enter the verification code",
  INVALID_VERIFICATION_CODE: "Invalid verification code",
  FAILED_VERIFY_CODE: "Failed to verify code",
  PIN_LENGTH_ERROR: "PIN must be 6 digits",
  REGISTRATION_FAILED: "Registration failed. Please try again.",
  INVALID_PHONE_OR_PIN: "Invalid phone number or PIN. Please check and try again.",
  NO_PIN_SET: "No PIN set for this account. Please register to set a PIN.",
  ACCOUNT_NOT_FOUND: "Account not found. Please register first.",
  NETWORK_ERROR_CHECK: "Network error. Please check your connection.",
  CONNECTION_TIMEOUT: "Connection timeout. Please try again.",
  CANNOT_CONNECT_SERVER_CHECK: "Cannot connect to server. Please check your internet connection.",
  LOGIN_FAILED_CHECK: "Login failed. Please check your phone number and PIN.",
  UNEXPECTED_ERROR: "An error occurred. Please try again.",
  
  // Placeholders  
  PHONE_PLACEHOLDER_EXAMPLE: "123 456 7890",
  
  // UI Messages
  QUESTION_NOT_FOUND: "Question not found",
  ALL_FIELDS_COMPLETED: "All fields completed!",
  PERSONALIZATION_INFO: "The more information you provide, the better we can personalize your farming advice.",
  DEBUG_MISSING_FIELDS: "Debug: Missing Fields",
  TAP_TO_SELECT_TEXT: "Tap to select",
  SUGGESTED_LABEL: "Suggested", 
  ALL_LANGUAGES_LABEL: "All Languages",
  
  // Field Labels
  CODE_LABEL: "Code",
  PHONE_NUMBER_LABEL: "Phone Number", 
  CREATE_PIN_LABEL: "Create PIN",
  CONFIRM_PIN_LABEL: "Confirm PIN",
  SKIP_FOR_NOW_BUTTON: "Skip for now",
  CONTINUE_BUTTON_TEXT: "Continue",
  
  // Share Labels
  WHATSAPP_LABEL: "WhatsApp",
  COPY_TEXT_LABEL: "Copy Text",
  MORE_APPS_LABEL: "More Apps",
  CANCEL_BUTTON: "Cancel",
  
  // Date Formats (consider keeping as is or using locale-specific formats)
  TIME_FORMAT: "HH:mm",
  DAY_FORMAT: "EEEE", 
  DATE_FORMAT: "dd/MM/yy",
  
  // Privacy Policy
  PRIVACY_POLICY_TITLE: "Privacy Policy",
  PRIVACY_LAST_UPDATED: "Last updated: %s",
  PRIVACY_INTRODUCTION_TITLE: "Introduction",
  PRIVACY_INTRODUCTION_CONTENT: "FarmerChat is committed to protecting your privacy. This Privacy Policy explains how we collect, use, disclose, and safeguard your information when you use our mobile application.",
  PRIVACY_INFO_COLLECT_TITLE: "Information We Collect",
  PRIVACY_INFO_COLLECT_CONTENT: "• Personal Information: Name, location, phone number, gender\n• Agricultural Data: Crops grown, livestock raised, farming practices\n• Usage Data: App interactions, conversation history, preferences\n• Device Information: Device type, operating system, unique identifiers",
  PRIVACY_HOW_USE_TITLE: "How We Use Your Information",
  PRIVACY_HOW_USE_CONTENT: "• Provide personalized agricultural advice\n• Improve our AI recommendations\n• Send relevant notifications and updates\n• Analyze usage patterns to enhance the app\n• Ensure proper app functionality\n• Comply with legal obligations",
  PRIVACY_DATA_STORAGE_TITLE: "Data Storage and Security",
  PRIVACY_DATA_STORAGE_CONTENT: "• Your data is encrypted in transit and at rest\n• We use industry-standard security measures\n• Data is stored on secure cloud servers\n• Regular security audits are performed\n• Access is restricted to authorized personnel only",
  PRIVACY_DATA_SHARING_TITLE: "Data Sharing",
  PRIVACY_DATA_SHARING_CONTENT: "We do not sell your personal information. We may share data with:\n• Agricultural research organizations (anonymized)\n• Service providers who assist our operations\n• Legal authorities when required by law",
  PRIVACY_YOUR_RIGHTS_TITLE: "Your Rights",
  PRIVACY_YOUR_RIGHTS_CONTENT: "You have the right to:\n• Access your personal data\n• Correct inaccurate information\n• Delete your account and data\n• Export your data\n• Opt-out of certain data uses\n• Lodge a complaint with authorities",
  PRIVACY_CONTACT_TITLE: "Contact Us",
  PRIVACY_CONTACT_CONTENT: "If you have questions about this Privacy Policy:\n• Email: privacy@digitalgreen.org\n• Website: www.digitalgreen.org\n• Address: Digital Green, [Address]",
  
  // Terms & Conditions
  TERMS_CONDITIONS_TITLE: "Terms & Conditions",
  TERMS_LAST_UPDATED: "Last updated: %s",
  TERMS_READ_TO_CONTINUE: "Please read the entire document to continue",
  TERMS_ACCEPTANCE_TITLE: "1. Acceptance of Terms",
  TERMS_ACCEPTANCE_CONTENT: "By downloading, installing, or using FarmerChat, you agree to be bound by these Terms & Conditions. If you do not agree to these terms, please do not use the application.",
  TERMS_SERVICE_DESC_TITLE: "2. Description of Service", 
  TERMS_SERVICE_DESC_CONTENT: "FarmerChat provides:\n• AI-powered agricultural advice\n• Personalized recommendations\n• Access to farming best practices\n• Weather and market information\n• Community features",
  TERMS_USER_ACCOUNTS_TITLE: "3. User Accounts",
  TERMS_USER_ACCOUNTS_CONTENT: "• You must provide accurate information\n• You are responsible for account security\n• One account per user\n• You must be 13+ years old\n• Commercial use requires permission",
  TERMS_ACCEPTABLE_USE_TITLE: "4. Acceptable Use",
  TERMS_ACCEPTABLE_USE_CONTENT: "You agree NOT to:\n• Share false or misleading information\n• Harass or harm other users\n• Attempt to hack or disrupt the service\n• Use the app for illegal activities\n• Violate others' intellectual property\n• Spam or send unsolicited messages",
  TERMS_AGRI_DISCLAIMER_TITLE: "5. Agricultural Advice Disclaimer",
  TERMS_AGRI_DISCLAIMER_CONTENT: "• Advice is general in nature\n• Consult local experts for specific issues\n• We are not liable for crop losses\n• Results may vary by location\n• Always verify critical information\n• Use at your own risk",
  TERMS_INTELLECTUAL_TITLE: "6. Intellectual Property",
  TERMS_INTELLECTUAL_CONTENT: "• FarmerChat content is protected by copyright\n• User-generated content remains yours\n• You grant us license to use your content\n• Respect others' intellectual property\n• Report any violations to us",
  TERMS_LIABILITY_TITLE: "7. Limitation of Liability",
  TERMS_LIABILITY_CONTENT: "TO THE MAXIMUM EXTENT PERMITTED BY LAW:\n• We provide the service \"AS IS\"\n• No warranties are given\n• We are not liable for indirect damages\n• Our liability is limited to service fees paid\n• Some jurisdictions don't allow these limitations",
  TERMS_PRIVACY_TITLE: "8. Privacy",
  TERMS_PRIVACY_CONTENT: "Your use of FarmerChat is also governed by our Privacy Policy. Please review our Privacy Policy to understand our practices.",
  TERMS_TERMINATION_TITLE: "9. Termination",
  TERMS_TERMINATION_CONTENT: "• Either party may terminate at any time\n• We may suspend access for violations\n• Your data may be deleted after termination\n• Some provisions survive termination",
  TERMS_CHANGES_TITLE: "10. Changes to Terms",
  TERMS_CHANGES_CONTENT: "We reserve the right to modify these terms at any time. We will notify you of significant changes through the app. Continued use after changes constitutes acceptance.",
  TERMS_GOVERNING_LAW_TITLE: "11. Governing Law",
  TERMS_GOVERNING_LAW_CONTENT: "These Terms shall be governed by the laws of the jurisdiction in which Digital Green operates, without regard to conflict of law provisions.",
  TERMS_CONTACT_TITLE: "12. Contact Information",
  TERMS_CONTACT_CONTENT: "For questions about these Terms:\n• Email: legal@digitalgreen.org\n• Website: www.digitalgreen.org\n• Address: Digital Green, [Address]",
  
  // Role/Gender Values (for UI display)
  ROLE_FARMER: "farmer",
  ROLE_EXTENSION_WORKER: "extension_worker",
  GENDER_MALE: "male", 
  GENDER_FEMALE: "female",
  GENDER_OTHER: "other",
  
  // Response Length Options
  RESPONSE_CONCISE: "concise",
  RESPONSE_MEDIUM: "medium", 
  RESPONSE_COMPREHENSIVE: "comprehensive"
};

async function addMissingUIStrings() {
  logger.info('Adding missing UI strings to database...');
  
  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    
    const totalKeys = Object.keys(NEW_STRING_KEYS).length;
    logger.info(`Adding ${totalKeys} new string keys...`);
    
    // First add all keys with English translations
    for (const [key, value] of Object.entries(NEW_STRING_KEYS)) {
      await client.query(
        `INSERT INTO ui_translations (language_code, key, translation)
         VALUES ($1, $2, $3)
         ON CONFLICT (language_code, key)
         DO UPDATE SET translation = $3, updated_at = NOW()`,
        ['en', key, value]
      );
    }
    
    await client.query('COMMIT');
    logger.info('✓ Added all new English translations');
    
    // Get count of total keys now
    const result = await pool.query(
      'SELECT COUNT(DISTINCT key) as total_keys FROM ui_translations'
    );
    
    logger.info(`Total translation keys in database: ${result.rows[0].total_keys}`);
    
    // Show which keys were added
    logger.info('\nNew keys added:');
    Object.keys(NEW_STRING_KEYS).forEach(key => {
      logger.info(`  - ${key}`);
    });
    
  } catch (error) {
    await client.query('ROLLBACK');
    logger.error('Failed to add translations:', error);
    throw error;
  } finally {
    client.release();
  }
}

// Main execution
if (require.main === module) {
  addMissingUIStrings()
    .then(() => {
      logger.info('\n✅ Successfully added all missing UI strings!');
      logger.info('\nNext steps:');
      logger.info('1. Run: npx ts-node scripts/sync-all-string-keys.ts --skip-completed');
      logger.info('2. This will translate all new keys to all 53 languages');
      process.exit(0);
    })
    .catch(error => {
      logger.error('Failed to add missing strings:', error);
      process.exit(1);
    });
}