package com.digitalgreen.farmerchat.data

/**
 * Provides translations for livestock names in multiple languages
 */
object LivestockTranslations {
    
    private val translations = mapOf(
        // Cow
        "cow" to mapOf(
            "en" to "Cow",
            "hi" to "गाय",
            "bn" to "গরু",
            "te" to "ఆవు",
            "mr" to "गाय",
            "ta" to "பசு",
            "gu" to "ગાય",
            "kn" to "ಹಸು",
            "ml" to "പശു",
            "pa" to "ਗਾਂ",
            "sw" to "Ng'ombe",
            "fr" to "Vache",
            "es" to "Vaca",
            "pt" to "Vaca",
            "ar" to "بقرة"
        ),
        
        // Buffalo
        "buffalo" to mapOf(
            "en" to "Buffalo",
            "hi" to "भैंस",
            "bn" to "মহিষ",
            "te" to "గేదె",
            "mr" to "म्हैस",
            "ta" to "எருமை",
            "gu" to "ભેંસ",
            "kn" to "ಎಮ್ಮೆ",
            "ml" to "എരുമ",
            "pa" to "ਮੱਝ",
            "sw" to "Nyati",
            "fr" to "Buffle",
            "es" to "Búfalo",
            "pt" to "Búfalo",
            "ar" to "جاموس"
        ),
        
        // Goat
        "goat" to mapOf(
            "en" to "Goat",
            "hi" to "बकरी",
            "bn" to "ছাগল",
            "te" to "మేక",
            "mr" to "बकरी",
            "ta" to "ஆடு",
            "gu" to "બકરી",
            "kn" to "ಮೇಕೆ",
            "ml" to "ആട്",
            "pa" to "ਬੱਕਰੀ",
            "sw" to "Mbuzi",
            "fr" to "Chèvre",
            "es" to "Cabra",
            "pt" to "Cabra",
            "ar" to "ماعز"
        ),
        
        // Sheep
        "sheep" to mapOf(
            "en" to "Sheep",
            "hi" to "भेड़",
            "bn" to "ভেড়া",
            "te" to "గొర్రె",
            "mr" to "मेंढी",
            "ta" to "செம்மறி",
            "gu" to "ઘેટાં",
            "kn" to "ಕುರಿ",
            "ml" to "ചെമ്മരിയാട്",
            "pa" to "ਭੇਡ",
            "sw" to "Kondoo",
            "fr" to "Mouton",
            "es" to "Oveja",
            "pt" to "Ovelha",
            "ar" to "خروف"
        ),
        
        // Chicken
        "chicken" to mapOf(
            "en" to "Chicken",
            "hi" to "मुर्गी",
            "bn" to "মুরগি",
            "te" to "కోడి",
            "mr" to "कोंबडी",
            "ta" to "கோழி",
            "gu" to "મરઘી",
            "kn" to "ಕೋಳಿ",
            "ml" to "കോഴി",
            "pa" to "ਮੁਰਗੀ",
            "sw" to "Kuku",
            "fr" to "Poulet",
            "es" to "Pollo",
            "pt" to "Frango",
            "ar" to "دجاج"
        ),
        
        // Duck
        "duck" to mapOf(
            "en" to "Duck",
            "hi" to "बतख",
            "bn" to "হাঁস",
            "te" to "బాతు",
            "mr" to "बदक",
            "ta" to "வாத்து",
            "gu" to "બતક",
            "kn" to "ಬಾತುಕೋಳಿ",
            "ml" to "താറാവ്",
            "pa" to "ਬੱਤਖ",
            "sw" to "Bata",
            "fr" to "Canard",
            "es" to "Pato",
            "pt" to "Pato",
            "ar" to "بطة"
        ),
        
        // Pig
        "pig" to mapOf(
            "en" to "Pig",
            "hi" to "सूअर",
            "bn" to "শূকর",
            "te" to "పంది",
            "mr" to "डुकर",
            "ta" to "பன்றி",
            "gu" to "ડુક્કર",
            "kn" to "ಹಂದಿ",
            "ml" to "പന്നി",
            "pa" to "ਸੂਰ",
            "sw" to "Nguruwe",
            "fr" to "Porc",
            "es" to "Cerdo",
            "pt" to "Porco",
            "ar" to "خنزير"
        ),
        
        // Fish
        "fish" to mapOf(
            "en" to "Fish",
            "hi" to "मछली",
            "bn" to "মাছ",
            "te" to "చేప",
            "mr" to "मासा",
            "ta" to "மீன்",
            "gu" to "માછલી",
            "kn" to "ಮೀನು",
            "ml" to "മത്സ്യം",
            "pa" to "ਮੱਛੀ",
            "sw" to "Samaki",
            "fr" to "Poisson",
            "es" to "Pescado",
            "pt" to "Peixe",
            "ar" to "سمك"
        )
        
        // Add more livestock translations as needed...
    )
    
    fun getLivestockName(livestockId: String, languageCode: String): String {
        return translations[livestockId]?.get(languageCode) 
            ?: translations[livestockId]?.get("en") // Fallback to English
            ?: livestockId.replaceFirstChar { it.uppercase() } // Fallback to ID
    }
    
    // Get all translations for a livestock (useful for search)
    fun getAllTranslations(livestockId: String): List<String> {
        return translations[livestockId]?.values?.toList() ?: emptyList()
    }
}