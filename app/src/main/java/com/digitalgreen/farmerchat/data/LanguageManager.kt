package com.digitalgreen.farmerchat.data

import java.util.Locale

data class Language(
    val code: String,           // ISO 639-1 code (e.g., "en", "hi", "sw")
    val name: String,           // Native name (e.g., "English", "हिन्दी", "Kiswahili")
    val englishName: String,    // English name for reference
    val locale: Locale,         // Locale object for system use
    val isRTL: Boolean = false, // Right-to-left languages like Arabic, Hebrew
    val region: String = "",    // Primary region where spoken
    val voiceSupported: Boolean = true // Whether STT/TTS is well-supported
)

object LanguageManager {
    // Comprehensive global language list
    val languages = listOf(
        // Major Global Languages
        Language("en", "English", "English", Locale("en", "US"), region = "Global"),
        Language("es", "Español", "Spanish", Locale("es", "ES"), region = "Global"),
        Language("zh", "中文", "Chinese (Simplified)", Locale("zh", "CN"), region = "Asia"),
        Language("ar", "العربية", "Arabic", Locale("ar", "SA"), isRTL = true, region = "Middle East"),
        Language("fr", "Français", "French", Locale("fr", "FR"), region = "Global"),
        Language("pt", "Português", "Portuguese", Locale("pt", "BR"), region = "Global"),
        Language("ru", "Русский", "Russian", Locale("ru", "RU"), region = "Europe/Asia"),
        Language("de", "Deutsch", "German", Locale("de", "DE"), region = "Europe"),
        Language("ja", "日本語", "Japanese", Locale("ja", "JP"), region = "Asia"),
        Language("ko", "한국어", "Korean", Locale("ko", "KR"), region = "Asia"),
        
        // Indian Languages
        Language("hi", "हिन्दी", "Hindi", Locale("hi", "IN"), region = "India"),
        Language("bn", "বাংলা", "Bengali", Locale("bn", "IN"), region = "India"),
        Language("te", "తెలుగు", "Telugu", Locale("te", "IN"), region = "India"),
        Language("mr", "मराठी", "Marathi", Locale("mr", "IN"), region = "India"),
        Language("ta", "தமிழ்", "Tamil", Locale("ta", "IN"), region = "India"),
        Language("gu", "ગુજરાતી", "Gujarati", Locale("gu", "IN"), region = "India"),
        Language("kn", "ಕನ್ನಡ", "Kannada", Locale("kn", "IN"), region = "India"),
        Language("ml", "മലയാളം", "Malayalam", Locale("ml", "IN"), region = "India"),
        Language("pa", "ਪੰਜਾਬੀ", "Punjabi", Locale("pa", "IN"), region = "India"),
        Language("or", "ଓଡ଼ିଆ", "Odia", Locale("or", "IN"), region = "India"),
        Language("as", "অসমীয়া", "Assamese", Locale("as", "IN"), region = "India"),
        Language("ur", "اردو", "Urdu", Locale("ur", "IN"), isRTL = true, region = "India/Pakistan"),
        
        // African Languages
        Language("sw", "Kiswahili", "Swahili", Locale("sw", "KE"), region = "East Africa"),
        Language("am", "አማርኛ", "Amharic", Locale("am", "ET"), region = "Ethiopia"),
        Language("ha", "Hausa", "Hausa", Locale("ha", "NG"), region = "West Africa"),
        Language("yo", "Yorùbá", "Yoruba", Locale("yo", "NG"), region = "Nigeria"),
        Language("ig", "Igbo", "Igbo", Locale("ig", "NG"), region = "Nigeria"),
        Language("zu", "isiZulu", "Zulu", Locale("zu", "ZA"), region = "South Africa"),
        Language("xh", "isiXhosa", "Xhosa", Locale("xh", "ZA"), region = "South Africa"),
        Language("af", "Afrikaans", "Afrikaans", Locale("af", "ZA"), region = "South Africa"),
        
        // Southeast Asian Languages
        Language("id", "Bahasa Indonesia", "Indonesian", Locale("id", "ID"), region = "Indonesia"),
        Language("ms", "Bahasa Melayu", "Malay", Locale("ms", "MY"), region = "Malaysia"),
        Language("th", "ไทย", "Thai", Locale("th", "TH"), region = "Thailand"),
        Language("vi", "Tiếng Việt", "Vietnamese", Locale("vi", "VN"), region = "Vietnam"),
        Language("fil", "Filipino", "Filipino", Locale("fil", "PH"), region = "Philippines"),
        Language("km", "ខ្មែរ", "Khmer", Locale("km", "KH"), region = "Cambodia"),
        Language("lo", "ລາວ", "Lao", Locale("lo", "LA"), region = "Laos"),
        Language("my", "မြန်မာ", "Burmese", Locale("my", "MM"), region = "Myanmar"),
        
        // European Languages
        Language("it", "Italiano", "Italian", Locale("it", "IT"), region = "Europe"),
        Language("nl", "Nederlands", "Dutch", Locale("nl", "NL"), region = "Europe"),
        Language("pl", "Polski", "Polish", Locale("pl", "PL"), region = "Europe"),
        Language("uk", "Українська", "Ukrainian", Locale("uk", "UA"), region = "Europe"),
        Language("ro", "Română", "Romanian", Locale("ro", "RO"), region = "Europe"),
        Language("el", "Ελληνικά", "Greek", Locale("el", "GR"), region = "Europe"),
        Language("cs", "Čeština", "Czech", Locale("cs", "CZ"), region = "Europe"),
        Language("hu", "Magyar", "Hungarian", Locale("hu", "HU"), region = "Europe"),
        Language("sv", "Svenska", "Swedish", Locale("sv", "SE"), region = "Europe"),
        Language("da", "Dansk", "Danish", Locale("da", "DK"), region = "Europe"),
        Language("fi", "Suomi", "Finnish", Locale("fi", "FI"), region = "Europe"),
        Language("no", "Norsk", "Norwegian", Locale("no", "NO"), region = "Europe"),
        
        // Other Important Languages
        Language("tr", "Türkçe", "Turkish", Locale("tr", "TR"), region = "Turkey"),
        Language("he", "עברית", "Hebrew", Locale("he", "IL"), isRTL = true, region = "Israel"),
        Language("fa", "فارسی", "Persian", Locale("fa", "IR"), isRTL = true, region = "Iran")
    )
    
    // Group languages by region for better UX
    fun getLanguagesByRegion(): Map<String, List<Language>> {
        return languages.groupBy { it.region }
    }
    
    // Get language by code
    fun getLanguageByCode(code: String): Language? {
        return languages.find { it.code == code }
    }
    
    // Get default language
    fun getDefaultLanguage(): Language {
        return languages.first() // English
    }
    
    // Search languages by name (native or English)
    fun searchLanguages(query: String): List<Language> {
        val lowercaseQuery = query.lowercase()
        return languages.filter {
            it.name.lowercase().contains(lowercaseQuery) ||
            it.englishName.lowercase().contains(lowercaseQuery)
        }
    }
    
    // Get languages for agricultural regions (prioritized list)
    fun getAgriculturalPriorityLanguages(): List<Language> {
        val priorityCodes = listOf(
            "en", "es", "hi", "bn", "sw", "ar", "fr", "pt", "id", "ha", 
            "yo", "am", "te", "mr", "ta", "ur", "zh", "vi", "th", "my"
        )
        return priorityCodes.mapNotNull { code -> languages.find { it.code == code } }
    }
}