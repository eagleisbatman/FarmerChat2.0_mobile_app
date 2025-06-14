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
        LANGUAGE_SETTING,
        LOCATION_SETTING,
        CROPS_SETTING,
        LIVESTOCK_SETTING,
        THEME_SETTING,
        DARK_MODE,
        LIGHT_MODE,
        SYSTEM_DEFAULT,
        RESPONSE_LENGTH,
        SHORT_RESPONSES,
        DETAILED_RESPONSES,
        ABOUT,
        VERSION,
        
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
        THANK_YOU_FEEDBACK
    }
    
    private val translations = mapOf(
        // English
        "en" to mapOf(
            StringKey.APP_NAME to "FarmerChat",
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
            StringKey.LANGUAGE_SETTING to "Language",
            StringKey.LOCATION_SETTING to "Location",
            StringKey.CROPS_SETTING to "Crops",
            StringKey.LIVESTOCK_SETTING to "Livestock",
            StringKey.THEME_SETTING to "Theme",
            StringKey.DARK_MODE to "Dark Mode",
            StringKey.LIGHT_MODE to "Light Mode",
            StringKey.SYSTEM_DEFAULT to "System Default",
            StringKey.RESPONSE_LENGTH to "Response Length",
            StringKey.SHORT_RESPONSES to "Short Responses",
            StringKey.DETAILED_RESPONSES to "Detailed Responses",
            StringKey.ABOUT to "About",
            StringKey.VERSION to "Version",
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
            StringKey.THANK_YOU_FEEDBACK to "Thank you for your feedback!"
        ),
        
        // Hindi
        "hi" to mapOf(
            StringKey.APP_NAME to "किसान चैट",
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
            StringKey.ERROR_AI_RESPONSE to "मुझे खेद है, लेकिन मुझे अभी जवाब देने में परेशानी हो रही है। कृपया पुनः प्रयास करें।"
        ),
        
        // Swahili
        "sw" to mapOf(
            StringKey.APP_NAME to "MkulimaChat",
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
            StringKey.ERROR_AI_RESPONSE to "Samahani, nina shida kujibu sasa hivi. Tafadhali jaribu tena."
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