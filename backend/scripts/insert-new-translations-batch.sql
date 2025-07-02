-- Insert new translation keys for English (en), Hindi (hi), and Swahili (sw)
-- Content Descriptions
INSERT INTO ui_translations (key, language_code, translation) VALUES
-- SWIPE_TO_CANCEL
('SWIPE_TO_CANCEL', 'en', 'Swipe to cancel'),
('SWIPE_TO_CANCEL', 'hi', 'रद्द करने के लिए स्वाइप करें'),
('SWIPE_TO_CANCEL', 'sw', 'Telezesha ili kufuta'),

-- CANCEL
('CANCEL', 'en', 'Cancel'),
('CANCEL', 'hi', 'रद्द करें'),
('CANCEL', 'sw', 'Futa'),

-- SEARCH
('SEARCH', 'en', 'Search'),
('SEARCH', 'hi', 'खोजें'),
('SEARCH', 'sw', 'Tafuta'),

-- CLEAR
('CLEAR', 'en', 'Clear'),
('CLEAR', 'hi', 'साफ़ करें'),
('CLEAR', 'sw', 'Futa'),

-- SELECTED
('SELECTED', 'en', 'Selected'),
('SELECTED', 'hi', 'चयनित'),
('SELECTED', 'sw', 'Imechaguliwa'),

-- CLEAR_SEARCH
('CLEAR_SEARCH', 'en', 'Clear search'),
('CLEAR_SEARCH', 'hi', 'खोज साफ़ करें'),
('CLEAR_SEARCH', 'sw', 'Futa utafutaji'),

-- LOCATION
('LOCATION', 'en', 'Location'),
('LOCATION', 'hi', 'स्थान'),
('LOCATION', 'sw', 'Mahali'),

-- CHANGE_LOCATION
('CHANGE_LOCATION', 'en', 'Change location'),
('CHANGE_LOCATION', 'hi', 'स्थान बदलें'),
('CHANGE_LOCATION', 'sw', 'Badilisha mahali'),

-- BACK
('BACK', 'en', 'Back'),
('BACK', 'hi', 'वापस'),
('BACK', 'sw', 'Rudi'),

-- SHARE
('SHARE', 'en', 'Share'),
('SHARE', 'hi', 'साझा करें'),
('SHARE', 'sw', 'Sambaza'),

-- START_RECORDING
('START_RECORDING', 'en', 'Start recording'),
('START_RECORDING', 'hi', 'रिकॉर्डिंग शुरू करें'),
('START_RECORDING', 'sw', 'Anza kurekodi'),

-- STOP_RECORDING
('STOP_RECORDING', 'en', 'Stop recording'),
('STOP_RECORDING', 'hi', 'रिकॉर्डिंग रोकें'),
('STOP_RECORDING', 'sw', 'Simamisha kurekodi');

-- Toast Messages
INSERT INTO ui_translations (key, language_code, translation) VALUES
-- EXPORT_COMING_SOON
('EXPORT_COMING_SOON', 'en', 'Export feature coming soon'),
('EXPORT_COMING_SOON', 'hi', 'निर्यात सुविधा जल्द आ रही है'),
('EXPORT_COMING_SOON', 'sw', 'Kipengele cha kuhamisha kijaja hivi karibuni'),

-- COPIED_TO_CLIPBOARD
('COPIED_TO_CLIPBOARD', 'en', 'Copied to clipboard'),
('COPIED_TO_CLIPBOARD', 'hi', 'क्लिपबोर्ड पर कॉपी किया गया'),
('COPIED_TO_CLIPBOARD', 'sw', 'Imenakiliwa kwenye ubao wa kunakili');

-- Error Messages
INSERT INTO ui_translations (key, language_code, translation) VALUES
-- CANNOT_CONNECT_SERVER
('CANNOT_CONNECT_SERVER', 'en', 'Cannot connect to server'),
('CANNOT_CONNECT_SERVER', 'hi', 'सर्वर से कनेक्ट नहीं हो सकता'),
('CANNOT_CONNECT_SERVER', 'sw', 'Haiwezi kuunganisha na seva'),

-- ENSURE_BACKEND_RUNNING
('ENSURE_BACKEND_RUNNING', 'en', 'Please ensure the backend is running on port 3004'),
('ENSURE_BACKEND_RUNNING', 'hi', 'कृपया सुनिश्चित करें कि बैकएंड पोर्ट 3004 पर चल रहा है'),
('ENSURE_BACKEND_RUNNING', 'sw', 'Tafadhali hakikisha backend inaendesha kwenye mlango 3004'),

-- ENTER_VALID_PHONE
('ENTER_VALID_PHONE', 'en', 'Please enter a valid phone number with country code'),
('ENTER_VALID_PHONE', 'hi', 'कृपया देश कोड के साथ एक वैध फ़ोन नंबर दर्ज करें'),
('ENTER_VALID_PHONE', 'sw', 'Tafadhali weka nambari sahihi ya simu pamoja na kodi ya nchi'),

-- ENTER_VALID_OTP
('ENTER_VALID_OTP', 'en', 'Please enter a valid 6-digit OTP'),
('ENTER_VALID_OTP', 'hi', 'कृपया एक वैध 6-अंकीय OTP दर्ज करें'),
('ENTER_VALID_OTP', 'sw', 'Tafadhali weka OTP sahihi yenye tarakimu 6'),

-- COMPLETE_ALL_FIELDS
('COMPLETE_ALL_FIELDS', 'en', 'Please complete all required fields'),
('COMPLETE_ALL_FIELDS', 'hi', 'कृपया सभी आवश्यक फ़ील्ड भरें'),
('COMPLETE_ALL_FIELDS', 'sw', 'Tafadhali jaza sehemu zote zinazohitajika'),

-- FAILED_SAVE_PROFILE
('FAILED_SAVE_PROFILE', 'en', 'Failed to save profile. Please try again.'),
('FAILED_SAVE_PROFILE', 'hi', 'प्रोफ़ाइल सहेजने में विफल। कृपया पुनः प्रयास करें।'),
('FAILED_SAVE_PROFILE', 'sw', 'Imeshindwa kuhifadhi wasifu. Tafadhali jaribu tena.'),

-- FAILED_SAVE_PHONE
('FAILED_SAVE_PHONE', 'en', 'Failed to save phone number: %s'),
('FAILED_SAVE_PHONE', 'hi', 'फ़ोन नंबर सहेजने में विफल: %s'),
('FAILED_SAVE_PHONE', 'sw', 'Imeshindwa kuhifadhi nambari ya simu: %s'),

-- ERROR_WITH_MESSAGE
('ERROR_WITH_MESSAGE', 'en', 'Error: %s'),
('ERROR_WITH_MESSAGE', 'hi', 'त्रुटि: %s'),
('ERROR_WITH_MESSAGE', 'sw', 'Kosa: %s'),

-- FAILED_LOAD_PROFILE
('FAILED_LOAD_PROFILE', 'en', 'Failed to load user profile: %s'),
('FAILED_LOAD_PROFILE', 'hi', 'उपयोगकर्ता प्रोफ़ाइल लोड करने में विफल: %s'),
('FAILED_LOAD_PROFILE', 'sw', 'Imeshindwa kupakia wasifu wa mtumiaji: %s'),

-- FAILED_LOAD_CONVERSATIONS
('FAILED_LOAD_CONVERSATIONS', 'en', 'Failed to load conversations: %s'),
('FAILED_LOAD_CONVERSATIONS', 'hi', 'बातचीत लोड करने में विफल: %s'),
('FAILED_LOAD_CONVERSATIONS', 'sw', 'Imeshindwa kupakia mazungumzo: %s'),

-- FAILED_CREATE_CONVERSATION
('FAILED_CREATE_CONVERSATION', 'en', 'Failed to create conversation: %s'),
('FAILED_CREATE_CONVERSATION', 'hi', 'बातचीत बनाने में विफल: %s'),
('FAILED_CREATE_CONVERSATION', 'sw', 'Imeshindwa kuunda mazungumzo: %s'),

-- FAILED_DELETE_CONVERSATION
('FAILED_DELETE_CONVERSATION', 'en', 'Failed to delete conversation: %s'),
('FAILED_DELETE_CONVERSATION', 'hi', 'बातचीत हटाने में विफल: %s'),
('FAILED_DELETE_CONVERSATION', 'sw', 'Imeshindwa kufuta mazungumzo: %s'),

-- DATA_EXPORT_COMING
('DATA_EXPORT_COMING', 'en', 'Data export feature coming soon'),
('DATA_EXPORT_COMING', 'hi', 'डेटा निर्यात सुविधा जल्द आ रही है'),
('DATA_EXPORT_COMING', 'sw', 'Kipengele cha kuhamisha data kijaja hivi karibuni'),

-- ERROR_DURING_EXPORT
('ERROR_DURING_EXPORT', 'en', 'Error during data export: %s'),
('ERROR_DURING_EXPORT', 'hi', 'डेटा निर्यात के दौरान त्रुटि: %s'),
('ERROR_DURING_EXPORT', 'sw', 'Kosa wakati wa kuhamisha data: %s'),

-- CONVERSATION_NOT_FOUND
('CONVERSATION_NOT_FOUND', 'en', 'Conversation not found'),
('CONVERSATION_NOT_FOUND', 'hi', 'बातचीत नहीं मिली'),
('CONVERSATION_NOT_FOUND', 'sw', 'Mazungumzo hayajapatikana'),

-- NO_ACTIVE_CONVERSATION
('NO_ACTIVE_CONVERSATION', 'en', 'No active conversation'),
('NO_ACTIVE_CONVERSATION', 'hi', 'कोई सक्रिय बातचीत नहीं'),
('NO_ACTIVE_CONVERSATION', 'sw', 'Hakuna mazungumzo yanayoendelea'),

-- FAILED_SEND_MESSAGE
('FAILED_SEND_MESSAGE', 'en', 'Failed to send message: %s'),
('FAILED_SEND_MESSAGE', 'hi', 'संदेश भेजने में विफल: %s'),
('FAILED_SEND_MESSAGE', 'sw', 'Imeshindwa kutuma ujumbe: %s')

ON CONFLICT (key, language_code) DO UPDATE SET translation = EXCLUDED.translation;