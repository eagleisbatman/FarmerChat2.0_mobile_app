package com.digitalgreen.farmerchat.utils

import com.digitalgreen.farmerchat.data.LanguageManager

/**
 * Manages localized strings for the app
 * This provides a centralized place for all UI text that needs translation
 */
object StringsManager {
    
    enum class StringKey {
        // App Name
        APP_NAME,
        
        // Onboarding
        CHOOSE_LANGUAGE,
        LANGUAGE_SUBTITLE,
        WHERE_LOCATED,
        LOCATION_SUBTITLE,
        LOCATION_PERMISSION_RATIONALE,
        ENABLE_LOCATION,
        DETECT_MY_LOCATION,
        LOCATION_DETECTED,
        GETTING_LOCATION,
        OR_ENTER_MANUALLY,
        LOCATION_PLACEHOLDER,
        SELECT_CROPS,
        CROPS_SUBTITLE,
        SELECT_LIVESTOCK,
        LIVESTOCK_SUBTITLE,
        
        // Common Actions
        CONTINUE,
        BACK,
        NEXT,
        SKIP,
        DONE,
        CANCEL,
        OK,
        RETRY,
        SEARCH,
        CLEAR,
        SAVE,
        SEARCH_CROPS,
        SEARCH_LIVESTOCK,
        
        // Conversations Screen
        MY_CONVERSATIONS,
        NEW_CONVERSATION,
        NO_CONVERSATIONS,
        START_FIRST_CONVERSATION,
        DELETE_CONVERSATION,
        SEARCH_CONVERSATIONS,
        
        // Chat Screen
        TYPE_MESSAGE,
        ASK_QUESTION,
        STARTER_QUESTIONS,
        WHAT_TO_KNOW_MORE,
        LISTENING,
        TAP_TO_SPEAK,
        RATE_RESPONSE,
        SPEAK_MESSAGE,
        STOP_SPEAKING,
        
        // Settings
        SETTINGS,
        PROFILE,
        NAME,
        LOCATION,
        CROPS,
        LIVESTOCK,
        SELECTED,
        PREFERENCES,
        LANGUAGE,
        VOICE_RESPONSES,
        VOICE_RESPONSES_DESC,
        VOICE_INPUT,
        VOICE_INPUT_DESC,
        AI_SETTINGS,
        RESPONSE_LENGTH,
        CONCISE,
        DETAILED,
        COMPREHENSIVE,
        FORMATTED_RESPONSES,
        FORMATTED_RESPONSES_DESC,
        DATA_PRIVACY,
        EXPORT_DATA,
        EXPORT_DATA_DESC,
        DELETE_ALL_DATA,
        DELETE_ALL_DATA_DESC,
        DELETE_DATA_CONFIRM,
        DELETE,
        ABOUT,
        APP_VERSION,
        APP_DESCRIPTION,
        VERSION,
        HELP_FEEDBACK,
        HELP_FEEDBACK_DESC,
        RESET_ONBOARDING,
        RESET_ONBOARDING_DESC,
        SELECT_LANGUAGE,
        CLOSE,
        
        // Errors
        ERROR_GENERIC,
        ERROR_NO_INTERNET,
        ERROR_LOCATION,
        ERROR_VOICE_RECOGNITION,
        ERROR_AI_RESPONSE,
        
        // Voice
        VOICE_NOT_AVAILABLE,
        MICROPHONE_PERMISSION_REQUIRED,
        PROCESSING,
        SEND,
        
        // Feedback
        HOW_HELPFUL,
        RATE_THIS_RESPONSE,
        ADD_COMMENT,
        SUBMIT_FEEDBACK,
        THANK_YOU_FEEDBACK,
        
        // Settings additions
        CHANGE,
        DATA_EXPORTED,
        DATA_EXPORTED_MESSAGE,
        DATA_DELETED,
        DATA_DELETED_MESSAGE,
        FAILED,
        EXPORT_DATA_ERROR,
        DELETE_ACCOUNT_ERROR,
        
        // Voice confidence
        CONFIDENCE_HIGH,
        CONFIDENCE_MEDIUM,
        CONFIDENCE_LOW,
        
        // Additional UI strings from audit
        MORE,
        ASK_ME_ANYTHING,
        NO_RESULTS_FOUND,
        YESTERDAY,
        START_CHATTING,
        EMPOWERING_FARMERS_WITH_AI,
        COPYRIGHT,
        ALL,
        SELECTED_WITH_CHECK,
        CROPS_SELECTED,
        ANIMALS_SELECTED,
        
        // Dialog and Modal strings
        EDIT_NAME,
        EDIT_LOCATION,
        ENTER_YOUR_NAME,
        ENTER_LOCATION,
        UPDATE_CROPS,
        UPDATE_LIVESTOCK,
        CONFIRM_DELETE,
        ARE_YOU_SURE,
        THIS_ACTION_CANNOT_BE_UNDONE,
        
        // Filter UI
        SHOW_FILTERS,
        HIDE_FILTERS,
        TRY_DIFFERENT_KEYWORDS,
        
        // Feedback dialog specifics
        ADDITIONAL_FEEDBACK_OPTIONAL,
        TELL_US_MORE,
        STAR_RATING,
        
        // Success messages
        SETTINGS_SAVED,
        RESET_COMPLETE,
        
        // Error messages
        PERMISSION_DENIED,
        LOCATION_SERVICES_DISABLED,
        RECORDING_ERROR,
        NETWORK_ERROR,
        TIMEOUT_ERROR,
        
        // User defaults
        DEFAULT_USER_NAME,
        
        // Export/Share
        EXPORT_FARMERCHAT_DATA,
        FAILED_TO_EXPORT,
        NO_PROFILE_DATA,
        
        // Authentication
        USER_NOT_AUTHENTICATED,
        
        // Voice
        SPEECH_NOT_AVAILABLE,
        
        // Conversation Management
        DELETE_CONVERSATION_CONFIRM,
        CONVERSATION_DELETED,
        
        // Chat UI
        YOU,
        PLAY,
        STOP,
        RATE,
        
        // Additional actions
        RESET,
        RESET_ONBOARDING_CONFIRM
    }
    
    private val translations = mapOf(
        // English
        "en" to mapOf(
            StringKey.APP_NAME to "FarmerChat", // Brand name - DO NOT TRANSLATE
            StringKey.CHOOSE_LANGUAGE to "Choose your preferred language",
            StringKey.LANGUAGE_SUBTITLE to "Select the language you're most comfortable with",
            StringKey.WHERE_LOCATED to "Where are you located?",
            StringKey.LOCATION_SUBTITLE to "This helps us provide location-specific advice",
            StringKey.LOCATION_PERMISSION_RATIONALE to "We need location permission to provide accurate local agricultural advice",
            StringKey.ENABLE_LOCATION to "Enable Location",
            StringKey.DETECT_MY_LOCATION to "Detect My Location",
            StringKey.LOCATION_DETECTED to "Location detected:",
            StringKey.GETTING_LOCATION to "Getting your location...",
            StringKey.OR_ENTER_MANUALLY to "Or enter location manually",
            StringKey.LOCATION_PLACEHOLDER to "e.g., Nairobi, Kenya",
            StringKey.SELECT_CROPS to "Which crops do you grow?",
            StringKey.CROPS_SUBTITLE to "Select all that apply",
            StringKey.SELECT_LIVESTOCK to "Do you raise any livestock?",
            StringKey.LIVESTOCK_SUBTITLE to "Select all that apply",
            StringKey.CONTINUE to "Continue",
            StringKey.BACK to "Back",
            StringKey.NEXT to "Next",
            StringKey.SKIP to "Skip",
            StringKey.DONE to "Done",
            StringKey.CANCEL to "Cancel",
            StringKey.OK to "OK",
            StringKey.RETRY to "Retry",
            StringKey.SEARCH to "Search",
            StringKey.CLEAR to "Clear",
            StringKey.SAVE to "Save",
            StringKey.SEARCH_CROPS to "Search crops",
            StringKey.SEARCH_LIVESTOCK to "Search livestock",
            StringKey.MY_CONVERSATIONS to "My Conversations",
            StringKey.NEW_CONVERSATION to "New Conversation",
            StringKey.NO_CONVERSATIONS to "No conversations yet",
            StringKey.START_FIRST_CONVERSATION to "Start your first conversation",
            StringKey.DELETE_CONVERSATION to "Delete Conversation",
            StringKey.SEARCH_CONVERSATIONS to "Search conversations",
            StringKey.TYPE_MESSAGE to "Type a message",
            StringKey.ASK_QUESTION to "Ask a question...",
            StringKey.STARTER_QUESTIONS to "Here are some questions to get you started:",
            StringKey.WHAT_TO_KNOW_MORE to "What would you like to know more about?",
            StringKey.LISTENING to "Listening...",
            StringKey.TAP_TO_SPEAK to "Tap to speak",
            StringKey.RATE_RESPONSE to "Rate this response",
            StringKey.SPEAK_MESSAGE to "Read aloud",
            StringKey.STOP_SPEAKING to "Stop reading",
            StringKey.SETTINGS to "Settings",
            StringKey.PROFILE to "Profile",
            StringKey.NAME to "Name",
            StringKey.LOCATION to "Location",
            StringKey.CROPS to "Crops",
            StringKey.LIVESTOCK to "Livestock",
            StringKey.SELECTED to "selected",
            StringKey.PREFERENCES to "Preferences",
            StringKey.LANGUAGE to "Language",
            StringKey.VOICE_RESPONSES to "Voice Responses",
            StringKey.VOICE_RESPONSES_DESC to "Read AI responses aloud automatically",
            StringKey.VOICE_INPUT to "Voice Input",
            StringKey.VOICE_INPUT_DESC to "Enable voice recording for questions",
            StringKey.AI_SETTINGS to "AI Settings",
            StringKey.RESPONSE_LENGTH to "Response Length",
            StringKey.CONCISE to "Concise",
            StringKey.DETAILED to "Detailed",
            StringKey.COMPREHENSIVE to "Comprehensive",
            StringKey.FORMATTED_RESPONSES to "Formatted Responses",
            StringKey.FORMATTED_RESPONSES_DESC to "Show responses with bullets and formatting",
            StringKey.DATA_PRIVACY to "Data & Privacy",
            StringKey.EXPORT_DATA to "Export My Data",
            StringKey.EXPORT_DATA_DESC to "Download all your data as JSON",
            StringKey.DELETE_ALL_DATA to "Delete All Data",
            StringKey.DELETE_ALL_DATA_DESC to "Permanently delete your account and data",
            StringKey.DELETE_DATA_CONFIRM to "Are you sure you want to delete all your data? This action cannot be undone.",
            StringKey.DELETE to "Delete",
            StringKey.ABOUT to "About",
            StringKey.APP_VERSION to "App Version",
            StringKey.APP_DESCRIPTION to "AI-powered agricultural assistant for smallholder farmers",
            StringKey.VERSION to "Version",
            StringKey.HELP_FEEDBACK to "Help & Feedback",
            StringKey.HELP_FEEDBACK_DESC to "Get help or send feedback",
            StringKey.RESET_ONBOARDING to "Reset Onboarding",
            StringKey.RESET_ONBOARDING_DESC to "Go through the setup process again",
            StringKey.SELECT_LANGUAGE to "Select Language",
            StringKey.CLOSE to "Close",
            StringKey.ERROR_GENERIC to "Something went wrong. Please try again.",
            StringKey.ERROR_NO_INTERNET to "No internet connection",
            StringKey.ERROR_LOCATION to "Unable to get location. Please check your device settings.",
            StringKey.ERROR_VOICE_RECOGNITION to "Voice recognition error",
            StringKey.ERROR_AI_RESPONSE to "I apologize, but I'm having trouble responding right now. Please try again.",
            StringKey.VOICE_NOT_AVAILABLE to "Voice recognition is not available on this device",
            StringKey.MICROPHONE_PERMISSION_REQUIRED to "Microphone permission is required for voice input",
            StringKey.PROCESSING to "Processing...",
            StringKey.SEND to "Send",
            StringKey.HOW_HELPFUL to "How helpful was this response?",
            StringKey.RATE_THIS_RESPONSE to "Rate this response",
            StringKey.ADD_COMMENT to "Add a comment (optional)",
            StringKey.SUBMIT_FEEDBACK to "Submit Feedback",
            StringKey.THANK_YOU_FEEDBACK to "Thank you for your feedback!",
            StringKey.CHANGE to "Change",
            StringKey.DATA_EXPORTED to "Data Exported",
            StringKey.DATA_EXPORTED_MESSAGE to "Your data has been exported successfully",
            StringKey.DATA_DELETED to "Data Deleted",
            StringKey.DATA_DELETED_MESSAGE to "Your account and all data have been deleted",
            StringKey.FAILED to "Failed",
            StringKey.EXPORT_DATA_ERROR to "Failed to export data. Please try again.",
            StringKey.DELETE_ACCOUNT_ERROR to "Failed to delete account. Please try again.",
            StringKey.CONFIDENCE_HIGH to "High",
            StringKey.CONFIDENCE_MEDIUM to "Medium",
            StringKey.CONFIDENCE_LOW to "Low",
            StringKey.MORE to "More",
            StringKey.ASK_ME_ANYTHING to "Ask me anything or try one of the below:",
            StringKey.NO_RESULTS_FOUND to "No results found",
            StringKey.YESTERDAY to "Yesterday",
            StringKey.START_CHATTING to "Start Chatting",
            StringKey.EMPOWERING_FARMERS_WITH_AI to "Empowering Farmers with AI",
            StringKey.COPYRIGHT to "© 2024 Digital Green",
            StringKey.ALL to "All",
            StringKey.SELECTED_WITH_CHECK to "Selected",
            StringKey.CROPS_SELECTED to "crops selected",
            StringKey.ANIMALS_SELECTED to "animals selected",
            
            // Dialog and Modal strings
            StringKey.EDIT_NAME to "Edit Name",
            StringKey.EDIT_LOCATION to "Edit Location",
            StringKey.ENTER_YOUR_NAME to "Enter your name",
            StringKey.ENTER_LOCATION to "Enter location",
            StringKey.UPDATE_CROPS to "Update Crops",
            StringKey.UPDATE_LIVESTOCK to "Update Livestock",
            StringKey.CONFIRM_DELETE to "Confirm Delete",
            StringKey.ARE_YOU_SURE to "Are you sure?",
            StringKey.THIS_ACTION_CANNOT_BE_UNDONE to "This action cannot be undone",
            
            // Filter UI
            StringKey.SHOW_FILTERS to "Show filters",
            StringKey.HIDE_FILTERS to "Hide filters",
            StringKey.TRY_DIFFERENT_KEYWORDS to "Try searching with different keywords",
            
            // Feedback dialog specifics
            StringKey.ADDITIONAL_FEEDBACK_OPTIONAL to "Additional feedback (optional)",
            StringKey.TELL_US_MORE to "Tell us more...",
            StringKey.STAR_RATING to "Star %d",
            
            // Success messages
            StringKey.SETTINGS_SAVED to "Settings saved",
            StringKey.RESET_COMPLETE to "Reset complete",
            
            // Error messages
            StringKey.PERMISSION_DENIED to "Permission denied",
            StringKey.LOCATION_SERVICES_DISABLED to "Location services are disabled",
            StringKey.RECORDING_ERROR to "Recording error",
            StringKey.NETWORK_ERROR to "Network error",
            StringKey.TIMEOUT_ERROR to "Request timed out",
            
            // User defaults
            StringKey.DEFAULT_USER_NAME to "Farmer %s",
            
            // Export/Share
            StringKey.EXPORT_FARMERCHAT_DATA to "Export FarmerChat Data",
            StringKey.FAILED_TO_EXPORT to "Failed to export data: %s",
            StringKey.NO_PROFILE_DATA to "No profile data",
            
            // Authentication
            StringKey.USER_NOT_AUTHENTICATED to "User not authenticated",
            
            // Voice
            StringKey.SPEECH_NOT_AVAILABLE to "Speech recognition is not available on this device",
            
            // Conversation Management
            StringKey.DELETE_CONVERSATION_CONFIRM to "Are you sure you want to delete this conversation?",
            StringKey.CONVERSATION_DELETED to "Conversation deleted",
            
            // Chat UI
            StringKey.YOU to "You",
            StringKey.PLAY to "Play",
            StringKey.STOP to "Stop",
            StringKey.RATE to "Rate",
            
            // Additional actions
            StringKey.RESET to "Reset",
            StringKey.RESET_ONBOARDING_CONFIRM to "Are you sure you want to reset the onboarding process? You will need to set up your preferences again."
        ),
        
        // Hindi
        "hi" to mapOf(
            StringKey.APP_NAME to "FarmerChat", // Brand name - DO NOT TRANSLATE
            StringKey.CHOOSE_LANGUAGE to "अपनी पसंदीदा भाषा चुनें",
            StringKey.LANGUAGE_SUBTITLE to "वह भाषा चुनें जिसमें आप सबसे सहज हैं",
            StringKey.WHERE_LOCATED to "आप कहाँ स्थित हैं?",
            StringKey.LOCATION_SUBTITLE to "यह हमें स्थान-विशिष्ट सलाह प्रदान करने में मदद करता है",
            StringKey.LOCATION_PERMISSION_RATIONALE to "सटीक स्थानीय कृषि सलाह प्रदान करने के लिए हमें स्थान की अनुमति चाहिए",
            StringKey.ENABLE_LOCATION to "स्थान सक्षम करें",
            StringKey.DETECT_MY_LOCATION to "मेरा स्थान पता लगाएं",
            StringKey.LOCATION_DETECTED to "स्थान का पता चला:",
            StringKey.GETTING_LOCATION to "आपका स्थान प्राप्त कर रहे हैं...",
            StringKey.OR_ENTER_MANUALLY to "या स्थान मैन्युअल रूप से दर्ज करें",
            StringKey.LOCATION_PLACEHOLDER to "उदा., नई दिल्ली, भारत",
            StringKey.SELECT_CROPS to "आप कौन सी फसलें उगाते हैं?",
            StringKey.CROPS_SUBTITLE to "जो लागू हों सभी चुनें",
            StringKey.SELECT_LIVESTOCK to "क्या आप कोई पशुधन पालते हैं?",
            StringKey.LIVESTOCK_SUBTITLE to "जो लागू हों सभी चुनें",
            StringKey.CONTINUE to "जारी रखें",
            StringKey.BACK to "वापस",
            StringKey.NEXT to "आगे",
            StringKey.SKIP to "छोड़ें",
            StringKey.DONE to "पूर्ण",
            StringKey.CANCEL to "रद्द करें",
            StringKey.OK to "ठीक है",
            StringKey.RETRY to "पुनः प्रयास करें",
            StringKey.SEARCH to "खोजें",
            StringKey.CLEAR to "साफ़ करें",
            StringKey.MY_CONVERSATIONS to "मेरी बातचीत",
            StringKey.NEW_CONVERSATION to "नई बातचीत",
            StringKey.NO_CONVERSATIONS to "अभी तक कोई बातचीत नहीं",
            StringKey.START_FIRST_CONVERSATION to "अपनी पहली बातचीत शुरू करें",
            StringKey.TYPE_MESSAGE to "संदेश टाइप करें",
            StringKey.ASK_QUESTION to "प्रश्न पूछें...",
            StringKey.STARTER_QUESTIONS to "शुरू करने के लिए कुछ प्रश्न:",
            StringKey.WHAT_TO_KNOW_MORE to "आप किस बारे में और जानना चाहेंगे?",
            StringKey.LISTENING to "सुन रहे हैं...",
            StringKey.TAP_TO_SPEAK to "बोलने के लिए टैप करें",
            StringKey.ERROR_GENERIC to "कुछ गलत हो गया। कृपया पुनः प्रयास करें।",
            StringKey.ERROR_AI_RESPONSE to "मुझे खेद है, लेकिन मुझे अभी जवाब देने में परेशानी हो रही है। कृपया पुनः प्रयास करें।",
            
            // Additional missing Hindi translations
            StringKey.SAVE to "सहेजें",
            StringKey.SEARCH_CROPS to "फसलें खोजें",
            StringKey.SEARCH_LIVESTOCK to "पशुधन खोजें",
            StringKey.DELETE_CONVERSATION to "बातचीत हटाएं",
            StringKey.SEARCH_CONVERSATIONS to "बातचीत खोजें",
            StringKey.RATE_RESPONSE to "इस प्रतिक्रिया को रेट करें",
            StringKey.SPEAK_MESSAGE to "जोर से पढ़ें",
            StringKey.STOP_SPEAKING to "पढ़ना बंद करें",
            StringKey.SETTINGS to "सेटिंग्स",
            StringKey.PROFILE to "प्रोफ़ाइल",
            StringKey.NAME to "नाम",
            StringKey.LOCATION to "स्थान",
            StringKey.CROPS to "फसलें",
            StringKey.LIVESTOCK to "पशुधन",
            StringKey.SELECTED to "चयनित",
            StringKey.PREFERENCES to "प्राथमिकताएं",
            StringKey.LANGUAGE to "भाषा",
            StringKey.VOICE_RESPONSES to "वॉयस प्रतिक्रियाएं",
            StringKey.VOICE_RESPONSES_DESC to "AI प्रतिक्रियाओं को स्वचालित रूप से जोर से पढ़ें",
            StringKey.VOICE_INPUT to "वॉयस इनपुट",
            StringKey.VOICE_INPUT_DESC to "प्रश्नों के लिए वॉयस रिकॉर्डिंग सक्षम करें",
            StringKey.AI_SETTINGS to "AI सेटिंग्स",
            StringKey.RESPONSE_LENGTH to "प्रतिक्रिया की लंबाई",
            StringKey.CONCISE to "संक्षिप्त",
            StringKey.DETAILED to "विस्तृत",
            StringKey.COMPREHENSIVE to "व्यापक",
            StringKey.FORMATTED_RESPONSES to "स्वरूपित प्रतिक्रियाएं",
            StringKey.FORMATTED_RESPONSES_DESC to "बुलेट और फॉर्मेटिंग के साथ प्रतिक्रियाएं दिखाएं",
            StringKey.DATA_PRIVACY to "डेटा और गोपनीयता",
            StringKey.EXPORT_DATA to "मेरा डेटा निर्यात करें",
            StringKey.EXPORT_DATA_DESC to "अपना सारा डेटा JSON के रूप में डाउनलोड करें",
            StringKey.DELETE_ALL_DATA to "सभी डेटा हटाएं",
            StringKey.DELETE_ALL_DATA_DESC to "अपना खाता और डेटा स्थायी रूप से हटाएं",
            StringKey.DELETE_DATA_CONFIRM to "क्या आप वाकई अपना सारा डेटा हटाना चाहते हैं? यह क्रिया पूर्ववत नहीं की जा सकती।",
            StringKey.DELETE to "हटाएं",
            StringKey.ABOUT to "बारे में",
            StringKey.APP_VERSION to "ऐप संस्करण",
            StringKey.APP_DESCRIPTION to "छोटे किसानों के लिए AI-संचालित कृषि सहायक",
            StringKey.VERSION to "संस्करण",
            StringKey.HELP_FEEDBACK to "मदद और प्रतिक्रिया",
            StringKey.HELP_FEEDBACK_DESC to "मदद प्राप्त करें या प्रतिक्रिया भेजें",
            StringKey.RESET_ONBOARDING to "ऑनबोर्डिंग रीसेट करें",
            StringKey.RESET_ONBOARDING_DESC to "सेटअप प्रक्रिया से फिर से गुजरें",
            StringKey.SELECT_LANGUAGE to "भाषा चुनें",
            StringKey.CLOSE to "बंद करें",
            StringKey.ERROR_NO_INTERNET to "इंटरनेट कनेक्शन नहीं है",
            StringKey.ERROR_LOCATION to "स्थान प्राप्त करने में असमर्थ। कृपया अपनी डिवाइस सेटिंग्स जांचें।",
            StringKey.ERROR_VOICE_RECOGNITION to "वॉयस पहचान त्रुटि",
            StringKey.VOICE_NOT_AVAILABLE to "इस डिवाइस पर वॉयस पहचान उपलब्ध नहीं है",
            StringKey.MICROPHONE_PERMISSION_REQUIRED to "वॉयस इनपुट के लिए माइक्रोफ़ोन अनुमति आवश्यक है",
            StringKey.PROCESSING to "प्रसंस्करण...",
            StringKey.SEND to "भेजें",
            StringKey.HOW_HELPFUL to "यह प्रतिक्रिया कितनी सहायक थी?",
            StringKey.RATE_THIS_RESPONSE to "इस प्रतिक्रिया को रेट करें",
            StringKey.ADD_COMMENT to "टिप्पणी जोड़ें (वैकल्पिक)",
            StringKey.SUBMIT_FEEDBACK to "प्रतिक्रिया जमा करें",
            StringKey.THANK_YOU_FEEDBACK to "आपकी प्रतिक्रिया के लिए धन्यवाद!",
            StringKey.CHANGE to "बदलें",
            StringKey.DATA_EXPORTED to "डेटा निर्यात किया गया",
            StringKey.DATA_EXPORTED_MESSAGE to "आपका डेटा सफलतापूर्वक निर्यात किया गया है",
            StringKey.DATA_DELETED to "डेटा हटाया गया",
            StringKey.DATA_DELETED_MESSAGE to "आपका खाता और सभी डेटा हटा दिए गए हैं",
            StringKey.FAILED to "असफल",
            StringKey.EXPORT_DATA_ERROR to "डेटा निर्यात करने में विफल। कृपया पुनः प्रयास करें।",
            StringKey.DELETE_ACCOUNT_ERROR to "खाता हटाने में विफल। कृपया पुनः प्रयास करें।",
            StringKey.CONFIDENCE_HIGH to "उच्च",
            StringKey.CONFIDENCE_MEDIUM to "मध्यम",
            StringKey.CONFIDENCE_LOW to "कम",
            StringKey.MORE to "अधिक",
            StringKey.ASK_ME_ANYTHING to "मुझसे कुछ भी पूछें या नीचे से एक प्रयास करें:",
            StringKey.NO_RESULTS_FOUND to "कोई परिणाम नहीं मिला",
            StringKey.YESTERDAY to "कल",
            StringKey.START_CHATTING to "चैट शुरू करें",
            StringKey.EMPOWERING_FARMERS_WITH_AI to "AI के साथ किसानों को सशक्त बनाना",
            StringKey.COPYRIGHT to "© 2024 डिजिटल ग्रीन",
            StringKey.ALL to "सभी",
            StringKey.SELECTED_WITH_CHECK to "चयनित",
            StringKey.CROPS_SELECTED to "फसलें चयनित",
            StringKey.ANIMALS_SELECTED to "पशु चयनित",
            
            // New dialog and modal strings
            StringKey.EDIT_NAME to "नाम संपादित करें",
            StringKey.EDIT_LOCATION to "स्थान संपादित करें",
            StringKey.ENTER_YOUR_NAME to "अपना नाम दर्ज करें",
            StringKey.ENTER_LOCATION to "स्थान दर्ज करें",
            StringKey.UPDATE_CROPS to "फसलें अपडेट करें",
            StringKey.UPDATE_LIVESTOCK to "पशुधन अपडेट करें",
            StringKey.CONFIRM_DELETE to "हटाना पुष्टि करें",
            StringKey.ARE_YOU_SURE to "क्या आप निश्चित हैं?",
            StringKey.THIS_ACTION_CANNOT_BE_UNDONE to "यह क्रिया पूर्ववत नहीं की जा सकती",
            
            // Filter UI
            StringKey.SHOW_FILTERS to "फ़िल्टर दिखाएं",
            StringKey.HIDE_FILTERS to "फ़िल्टर छुपाएं",
            StringKey.TRY_DIFFERENT_KEYWORDS to "अलग कीवर्ड्स के साथ खोजने का प्रयास करें",
            
            // Feedback dialog
            StringKey.ADDITIONAL_FEEDBACK_OPTIONAL to "अतिरिक्त प्रतिक्रिया (वैकल्पिक)",
            StringKey.TELL_US_MORE to "हमें और बताएं...",
            StringKey.STAR_RATING to "स्टार %d",
            
            // Success messages
            StringKey.SETTINGS_SAVED to "सेटिंग्स सहेजी गईं",
            StringKey.RESET_COMPLETE to "रीसेट पूर्ण",
            
            // Error messages
            StringKey.PERMISSION_DENIED to "अनुमति अस्वीकृत",
            StringKey.LOCATION_SERVICES_DISABLED to "स्थान सेवाएं अक्षम हैं",
            StringKey.RECORDING_ERROR to "रिकॉर्डिंग त्रुटि",
            StringKey.NETWORK_ERROR to "नेटवर्क त्रुटि",
            StringKey.TIMEOUT_ERROR to "अनुरोध समय समाप्त",
            
            // User defaults
            StringKey.DEFAULT_USER_NAME to "किसान %s",
            
            // Export/Share
            StringKey.EXPORT_FARMERCHAT_DATA to "FarmerChat डेटा निर्यात करें",
            StringKey.FAILED_TO_EXPORT to "डेटा निर्यात करने में विफल: %s",
            StringKey.NO_PROFILE_DATA to "कोई प्रोफ़ाइल डेटा नहीं",
            
            // Authentication
            StringKey.USER_NOT_AUTHENTICATED to "उपयोगकर्ता प्रमाणित नहीं है",
            
            // Voice
            StringKey.SPEECH_NOT_AVAILABLE to "इस डिवाइस पर वाक् पहचान उपलब्ध नहीं है",
            
            // Conversation Management
            StringKey.DELETE_CONVERSATION_CONFIRM to "क्या आप वाकई इस बातचीत को हटाना चाहते हैं?",
            StringKey.CONVERSATION_DELETED to "बातचीत हटा दी गई",
            
            // Chat UI
            StringKey.YOU to "आप",
            StringKey.PLAY to "चलाएं",
            StringKey.STOP to "रोकें",
            StringKey.RATE to "रेटिंग",
            
            // Additional actions
            StringKey.RESET to "रीसेट करें",
            StringKey.RESET_ONBOARDING_CONFIRM to "क्या आप वाकई ऑनबोर्डिंग प्रक्रिया को रीसेट करना चाहते हैं? आपको अपनी प्राथमिकताएं फिर से सेट करनी होंगी।"
        ),
        
        // Swahili
        "sw" to mapOf(
            StringKey.APP_NAME to "FarmerChat", // Brand name - DO NOT TRANSLATE
            StringKey.CHOOSE_LANGUAGE to "Chagua lugha unayopendelea",
            StringKey.LANGUAGE_SUBTITLE to "Chagua lugha unayoijua zaidi",
            StringKey.WHERE_LOCATED to "Uko wapi?",
            StringKey.LOCATION_SUBTITLE to "Hii inatusaidia kutoa ushauri maalum wa eneo",
            StringKey.LOCATION_PERMISSION_RATIONALE to "Tunahitaji ruhusa ya mahali kutoa ushauri sahihi wa kilimo cha eneo",
            StringKey.ENABLE_LOCATION to "Wezesha Mahali",
            StringKey.DETECT_MY_LOCATION to "Tambua Mahali Pangu",
            StringKey.LOCATION_DETECTED to "Mahali pametambuliwa:",
            StringKey.GETTING_LOCATION to "Tunapata mahali pako...",
            StringKey.OR_ENTER_MANUALLY to "Au weka mahali mwenyewe",
            StringKey.LOCATION_PLACEHOLDER to "mfano, Nairobi, Kenya",
            StringKey.SELECT_CROPS to "Unapanda mazao gani?",
            StringKey.CROPS_SUBTITLE to "Chagua yote yanayotumika",
            StringKey.SELECT_LIVESTOCK to "Je, unafuga mifugo yoyote?",
            StringKey.LIVESTOCK_SUBTITLE to "Chagua yote yanayotumika",
            StringKey.CONTINUE to "Endelea",
            StringKey.BACK to "Rudi",
            StringKey.NEXT to "Mbele",
            StringKey.SKIP to "Ruka",
            StringKey.DONE to "Maliza",
            StringKey.CANCEL to "Ghairi",
            StringKey.OK to "Sawa",
            StringKey.RETRY to "Jaribu tena",
            StringKey.SEARCH to "Tafuta",
            StringKey.CLEAR to "Futa",
            StringKey.MY_CONVERSATIONS to "Mazungumzo Yangu",
            StringKey.NEW_CONVERSATION to "Mazungumzo Mapya",
            StringKey.NO_CONVERSATIONS to "Hakuna mazungumzo bado",
            StringKey.START_FIRST_CONVERSATION to "Anza mazungumzo yako ya kwanza",
            StringKey.TYPE_MESSAGE to "Andika ujumbe",
            StringKey.ASK_QUESTION to "Uliza swali...",
            StringKey.STARTER_QUESTIONS to "Hapa kuna maswali ya kuanza:",
            StringKey.WHAT_TO_KNOW_MORE to "Ungependa kujua zaidi kuhusu nini?",
            StringKey.LISTENING to "Nasikiliza...",
            StringKey.TAP_TO_SPEAK to "Gusa ili uongee",
            StringKey.ERROR_GENERIC to "Kuna kitu kimeenda vibaya. Tafadhali jaribu tena.",
            StringKey.ERROR_AI_RESPONSE to "Samahani, nina shida kujibu sasa hivi. Tafadhali jaribu tena.",
            
            // Additional missing Swahili translations
            StringKey.SAVE to "Hifadhi",
            StringKey.SEARCH_CROPS to "Tafuta mazao",
            StringKey.SEARCH_LIVESTOCK to "Tafuta mifugo",
            StringKey.DELETE_CONVERSATION to "Futa Mazungumzo",
            StringKey.SEARCH_CONVERSATIONS to "Tafuta mazungumzo",
            StringKey.RATE_RESPONSE to "Kadiria jibu hili",
            StringKey.SPEAK_MESSAGE to "Soma kwa sauti",
            StringKey.STOP_SPEAKING to "Acha kusoma",
            StringKey.SETTINGS to "Mipangilio",
            StringKey.PROFILE to "Wasifu",
            StringKey.NAME to "Jina",
            StringKey.LOCATION to "Mahali",
            StringKey.CROPS to "Mazao",
            StringKey.LIVESTOCK to "Mifugo",
            StringKey.SELECTED to "iliyochaguliwa",
            StringKey.PREFERENCES to "Mapendeleo",
            StringKey.LANGUAGE to "Lugha",
            StringKey.VOICE_RESPONSES to "Majibu ya Sauti",
            StringKey.VOICE_RESPONSES_DESC to "Soma majibu ya AI kwa sauti moja kwa moja",
            StringKey.VOICE_INPUT to "Kuingiza Sauti",
            StringKey.VOICE_INPUT_DESC to "Wezesha kurekodi sauti kwa maswali",
            StringKey.AI_SETTINGS to "Mipangilio ya AI",
            StringKey.RESPONSE_LENGTH to "Urefu wa Majibu",
            StringKey.CONCISE to "Mafupi",
            StringKey.DETAILED to "Kwa Kina",
            StringKey.COMPREHENSIVE to "Kamili",
            StringKey.FORMATTED_RESPONSES to "Majibu Yaliyopangwa",
            StringKey.FORMATTED_RESPONSES_DESC to "Onyesha majibu yenye alama na mpangilio",
            StringKey.DATA_PRIVACY to "Data na Faragha",
            StringKey.EXPORT_DATA to "Hamisha Data Yangu",
            StringKey.EXPORT_DATA_DESC to "Pakua data yako yote kama JSON",
            StringKey.DELETE_ALL_DATA to "Futa Data Yote",
            StringKey.DELETE_ALL_DATA_DESC to "Futa akaunti yako na data kabisa",
            StringKey.DELETE_DATA_CONFIRM to "Je una uhakika unataka kufuta data yako yote? Kitendo hiki hakiwezi kutengwa.",
            StringKey.DELETE to "Futa",
            StringKey.ABOUT to "Kuhusu",
            StringKey.APP_VERSION to "Toleo la Programu",
            StringKey.APP_DESCRIPTION to "Msaidizi wa kilimo wa AI kwa wakulima wadogo",
            StringKey.VERSION to "Toleo",
            StringKey.HELP_FEEDBACK to "Msaada na Maoni",
            StringKey.HELP_FEEDBACK_DESC to "Pata msaada au tuma maoni",
            StringKey.RESET_ONBOARDING to "Weka upya Uanzishaji",
            StringKey.RESET_ONBOARDING_DESC to "Pitia mchakato wa kusanidi tena",
            StringKey.SELECT_LANGUAGE to "Chagua Lugha",
            StringKey.CLOSE to "Funga",
            StringKey.ERROR_NO_INTERNET to "Hakuna muunganisho wa intaneti",
            StringKey.ERROR_LOCATION to "Imeshindwa kupata mahali. Tafadhali angalia mipangilio ya kifaa chako.",
            StringKey.ERROR_VOICE_RECOGNITION to "Kosa la kutambua sauti",
            StringKey.VOICE_NOT_AVAILABLE to "Utambuzi wa sauti haupatikani kwenye kifaa hiki",
            StringKey.MICROPHONE_PERMISSION_REQUIRED to "Ruhusa ya kipaza sauti inahitajika kwa kuingiza sauti",
            StringKey.PROCESSING to "Inachakata...",
            StringKey.SEND to "Tuma",
            StringKey.HOW_HELPFUL to "Je jibu hili lilikuwa la kusaidia kiasi gani?",
            StringKey.RATE_THIS_RESPONSE to "Kadiria jibu hili",
            StringKey.ADD_COMMENT to "Ongeza maoni (hiari)",
            StringKey.SUBMIT_FEEDBACK to "Wasilisha Maoni",
            StringKey.THANK_YOU_FEEDBACK to "Asante kwa maoni yako!",
            StringKey.CHANGE to "Badilisha",
            StringKey.DATA_EXPORTED to "Data Imehamishwa",
            StringKey.DATA_EXPORTED_MESSAGE to "Data yako imehamishwa kwa mafanikio",
            StringKey.DATA_DELETED to "Data Imefutwa",
            StringKey.DATA_DELETED_MESSAGE to "Akaunti yako na data yote zimefutwa",
            StringKey.FAILED to "Imeshindwa",
            StringKey.EXPORT_DATA_ERROR to "Imeshindwa kuhamisha data. Tafadhali jaribu tena.",
            StringKey.DELETE_ACCOUNT_ERROR to "Imeshindwa kufuta akaunti. Tafadhali jaribu tena.",
            StringKey.CONFIDENCE_HIGH to "Juu",
            StringKey.CONFIDENCE_MEDIUM to "Kati",
            StringKey.CONFIDENCE_LOW to "Chini",
            StringKey.MORE to "Zaidi",
            StringKey.ASK_ME_ANYTHING to "Niulize chochote au jaribu mojawapo ya zifuatazo:",
            StringKey.NO_RESULTS_FOUND to "Hakuna matokeo yaliyopatikana",
            StringKey.YESTERDAY to "Jana",
            StringKey.START_CHATTING to "Anza Kuzungumza",
            StringKey.EMPOWERING_FARMERS_WITH_AI to "Kuwezesha Wakulima kwa AI",
            StringKey.COPYRIGHT to "© 2024 Digital Green",
            StringKey.ALL to "Yote",
            StringKey.SELECTED_WITH_CHECK to "Iliyochaguliwa",
            StringKey.CROPS_SELECTED to "mazao yaliyochaguliwa",
            StringKey.ANIMALS_SELECTED to "wanyama waliochaguliwa",
            
            // New dialog and modal strings
            StringKey.EDIT_NAME to "Hariri Jina",
            StringKey.EDIT_LOCATION to "Hariri Mahali",
            StringKey.ENTER_YOUR_NAME to "Weka jina lako",
            StringKey.ENTER_LOCATION to "Weka mahali",
            StringKey.UPDATE_CROPS to "Sasisha Mazao",
            StringKey.UPDATE_LIVESTOCK to "Sasisha Mifugo",
            StringKey.CONFIRM_DELETE to "Thibitisha Kufuta",
            StringKey.ARE_YOU_SURE to "Una uhakika?",
            StringKey.THIS_ACTION_CANNOT_BE_UNDONE to "Kitendo hiki hakiwezi kutengwa",
            
            // Filter UI
            StringKey.SHOW_FILTERS to "Onyesha vichujio",
            StringKey.HIDE_FILTERS to "Ficha vichujio",
            StringKey.TRY_DIFFERENT_KEYWORDS to "Jaribu kutafuta kwa maneno tofauti",
            
            // Feedback dialog
            StringKey.ADDITIONAL_FEEDBACK_OPTIONAL to "Maoni ya ziada (hiari)",
            StringKey.TELL_US_MORE to "Tuambie zaidi...",
            StringKey.STAR_RATING to "Nyota %d",
            
            // Success messages
            StringKey.SETTINGS_SAVED to "Mipangilio imehifadhiwa",
            StringKey.RESET_COMPLETE to "Kuweka upya kumekamilika",
            
            // Error messages
            StringKey.PERMISSION_DENIED to "Ruhusa imekataliwa",
            StringKey.LOCATION_SERVICES_DISABLED to "Huduma za mahali zimezimwa",
            StringKey.RECORDING_ERROR to "Kosa la kurekodi",
            StringKey.NETWORK_ERROR to "Kosa la mtandao",
            StringKey.TIMEOUT_ERROR to "Ombi limeisha muda",
            
            // User defaults
            StringKey.DEFAULT_USER_NAME to "Mkulima %s",
            
            // Export/Share
            StringKey.EXPORT_FARMERCHAT_DATA to "Hamisha Data ya FarmerChat",
            StringKey.FAILED_TO_EXPORT to "Imeshindwa kuhamisha data: %s",
            StringKey.NO_PROFILE_DATA to "Hakuna data ya wasifu",
            
            // Authentication
            StringKey.USER_NOT_AUTHENTICATED to "Mtumiaji hajathibitishwa",
            
            // Voice
            StringKey.SPEECH_NOT_AVAILABLE to "Utambuzi wa sauti haupatikani kwenye kifaa hiki",
            
            // Conversation Management
            StringKey.DELETE_CONVERSATION_CONFIRM to "Je, una uhakika unataka kufuta mazungumzo haya?",
            StringKey.CONVERSATION_DELETED to "Mazungumzo yamefutwa",
            
            // Chat UI
            StringKey.YOU to "Wewe",
            StringKey.PLAY to "Cheza",
            StringKey.STOP to "Simamisha",
            StringKey.RATE to "Kadiria",
            
            // Additional actions
            StringKey.RESET to "Weka upya",
            StringKey.RESET_ONBOARDING_CONFIRM to "Je, una uhakika unataka kuweka upya mchakato wa kuanza? Utahitaji kuweka mapendekezo yako tena."
        )
    )
    
    /**
     * Get localized string for a given key and language code
     */
    fun getString(key: StringKey, languageCode: String = "en"): String {
        val languageStrings = translations[languageCode] ?: translations["en"]!!
        return languageStrings[key] ?: translations["en"]!![key] ?: key.name
    }
    
    /**
     * Check if a language is fully supported (has all translations)
     */
    fun isLanguageFullySupported(languageCode: String): Boolean {
        val languageStrings = translations[languageCode] ?: return false
        return languageStrings.size == StringKey.values().size
    }
    
    /**
     * Get list of fully supported language codes
     */
    fun getFullySupportedLanguages(): List<String> {
        return translations.keys.filter { isLanguageFullySupported(it) }
    }
    
    /**
     * Format string with parameters
     */
    fun getString(key: StringKey, languageCode: String = "en", vararg args: Any): String {
        val template = getString(key, languageCode)
        return String.format(template, *args)
    }
}