package com.digitalgreen.farmerchat.data

/**
 * Provides translations for crop names in multiple languages
 */
object CropTranslations {
    
    private val translations = mapOf(
        // Wheat
        "wheat" to mapOf(
            "en" to "Wheat",
            "hi" to "गेहूं",
            "bn" to "গম",
            "te" to "గోధుమ",
            "mr" to "गहू",
            "ta" to "கோதுமை",
            "gu" to "ઘઉં",
            "kn" to "ಗೋಧಿ",
            "ml" to "ഗോതമ്പ്",
            "pa" to "ਕਣਕ",
            "sw" to "Ngano",
            "fr" to "Blé",
            "es" to "Trigo",
            "pt" to "Trigo",
            "ar" to "قمح"
        ),
        
        // Rice
        "rice" to mapOf(
            "en" to "Rice",
            "hi" to "चावल",
            "bn" to "ধান",
            "te" to "వరి",
            "mr" to "तांदूळ",
            "ta" to "அரிசி",
            "gu" to "ચોખા",
            "kn" to "ಅಕ್ಕಿ",
            "ml" to "അരി",
            "pa" to "ਚਾਵਲ",
            "sw" to "Mchele",
            "fr" to "Riz",
            "es" to "Arroz",
            "pt" to "Arroz",
            "ar" to "أرز"
        ),
        
        // Maize
        "maize" to mapOf(
            "en" to "Maize/Corn",
            "hi" to "मक्का",
            "bn" to "ভুট্টা",
            "te" to "మొక్కజొన్న",
            "mr" to "मका",
            "ta" to "மக்காச்சோளம்",
            "gu" to "મકાઈ",
            "kn" to "ಜೋಳ",
            "ml" to "ചോളം",
            "pa" to "ਮੱਕੀ",
            "sw" to "Mahindi",
            "fr" to "Maïs",
            "es" to "Maíz",
            "pt" to "Milho",
            "ar" to "ذرة"
        ),
        
        // Tomato
        "tomato" to mapOf(
            "en" to "Tomato",
            "hi" to "टमाटर",
            "bn" to "টমেটো",
            "te" to "టమాటా",
            "mr" to "टोमॅटो",
            "ta" to "தக்காளி",
            "gu" to "ટામેટા",
            "kn" to "ಟೊಮೇಟೊ",
            "ml" to "തക്കാളി",
            "pa" to "ਟਮਾਟਰ",
            "sw" to "Nyanya",
            "fr" to "Tomate",
            "es" to "Tomate",
            "pt" to "Tomate",
            "ar" to "طماطم"
        ),
        
        // Onion
        "onion" to mapOf(
            "en" to "Onion",
            "hi" to "प्याज",
            "bn" to "পেঁয়াজ",
            "te" to "ఉల్లిపాయ",
            "mr" to "कांदा",
            "ta" to "வெங்காயம்",
            "gu" to "ડુંગળી",
            "kn" to "ಈರುಳ್ಳಿ",
            "ml" to "സവാള",
            "pa" to "ਪਿਆਜ਼",
            "sw" to "Kitunguu",
            "fr" to "Oignon",
            "es" to "Cebolla",
            "pt" to "Cebola",
            "ar" to "بصل"
        ),
        
        // Potato
        "potato" to mapOf(
            "en" to "Potato",
            "hi" to "आलू",
            "bn" to "আলু",
            "te" to "బంగాళాదుంప",
            "mr" to "बटाटा",
            "ta" to "உருளைக்கிழங்கு",
            "gu" to "બટાકા",
            "kn" to "ಆಲೂಗಡ್ಡೆ",
            "ml" to "ഉരുളക്കിഴങ്ങ്",
            "pa" to "ਆਲੂ",
            "sw" to "Viazi",
            "fr" to "Pomme de terre",
            "es" to "Patata",
            "pt" to "Batata",
            "ar" to "بطاطس"
        ),
        
        // Cotton
        "cotton" to mapOf(
            "en" to "Cotton",
            "hi" to "कपास",
            "bn" to "তুলা",
            "te" to "పత్తి",
            "mr" to "कापूस",
            "ta" to "பருத்தி",
            "gu" to "કપાસ",
            "kn" to "ಹತ್ತಿ",
            "ml" to "പരുത്തി",
            "pa" to "ਕਪਾਹ",
            "sw" to "Pamba",
            "fr" to "Coton",
            "es" to "Algodón",
            "pt" to "Algodão",
            "ar" to "قطن"
        ),
        
        // Sugarcane
        "sugarcane" to mapOf(
            "en" to "Sugarcane",
            "hi" to "गन्ना",
            "bn" to "আখ",
            "te" to "చెరుకు",
            "mr" to "ऊस",
            "ta" to "கரும்பு",
            "gu" to "શેરડી",
            "kn" to "ಕಬ್ಬು",
            "ml" to "കരിമ്പ്",
            "pa" to "ਗੰਨਾ",
            "sw" to "Miwa",
            "fr" to "Canne à sucre",
            "es" to "Caña de azúcar",
            "pt" to "Cana-de-açúcar",
            "ar" to "قصب السكر"
        )
        
        // Add more crop translations as needed...
    )
    
    fun getCropName(cropId: String, languageCode: String): String {
        return translations[cropId]?.get(languageCode) 
            ?: translations[cropId]?.get("en") // Fallback to English
            ?: cropId.replaceFirstChar { it.uppercase() } // Fallback to ID
    }
    
    // Get all translations for a crop (useful for search)
    fun getAllTranslations(cropId: String): List<String> {
        return translations[cropId]?.values?.toList() ?: emptyList()
    }
}