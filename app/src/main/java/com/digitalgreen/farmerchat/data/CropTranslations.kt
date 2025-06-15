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
        ),
        
        // Additional cereals
        "sorghum" to mapOf(
            "en" to "Sorghum",
            "hi" to "ज्वार",
            "sw" to "Mtama"
        ),
        "millet" to mapOf(
            "en" to "Millet",
            "hi" to "बाजरा",
            "sw" to "Uwele"
        ),
        "barley" to mapOf(
            "en" to "Barley",
            "hi" to "जौ",
            "sw" to "Shayiri"
        ),
        
        // Pulses
        "chickpea" to mapOf(
            "en" to "Chickpea",
            "hi" to "चना",
            "sw" to "Dengu"
        ),
        "lentil" to mapOf(
            "en" to "Lentil",
            "hi" to "मसूर",
            "sw" to "Dengu"
        ),
        "blackgram" to mapOf(
            "en" to "Black Gram",
            "hi" to "उड़द",
            "sw" to "Choroko"
        ),
        "greengram" to mapOf(
            "en" to "Green Gram",
            "hi" to "मूंग",
            "sw" to "Choroko kijani"
        ),
        "pigeonpea" to mapOf(
            "en" to "Pigeon Pea",
            "hi" to "अरहर",
            "sw" to "Mbaazi"
        ),
        "fieldpea" to mapOf(
            "en" to "Field Pea",
            "hi" to "मटर",
            "sw" to "Njegere"
        ),
        
        // More vegetables
        "cabbage" to mapOf(
            "en" to "Cabbage",
            "hi" to "बंदगोभी",
            "sw" to "Kabeji"
        ),
        "cauliflower" to mapOf(
            "en" to "Cauliflower",
            "hi" to "फूलगोभी",
            "sw" to "Kolifwawa"
        ),
        "carrot" to mapOf(
            "en" to "Carrot",
            "hi" to "गाजर",
            "sw" to "Karoti"
        ),
        "eggplant" to mapOf(
            "en" to "Eggplant/Brinjal",
            "hi" to "बैंगन",
            "sw" to "Bilingani"
        ),
        "okra" to mapOf(
            "en" to "Okra",
            "hi" to "भिंडी",
            "sw" to "Bamia"
        ),
        "spinach" to mapOf(
            "en" to "Spinach",
            "hi" to "पालक",
            "sw" to "Mchicha"
        ),
        
        // Cash crops
        "tobacco" to mapOf(
            "en" to "Tobacco",
            "hi" to "तंबाकू",
            "sw" to "Tumbaku"
        ),
        "jute" to mapOf(
            "en" to "Jute",
            "hi" to "पटसन",
            "sw" to "Pamba ya maji"
        ),
        
        // Oilseeds
        "groundnut" to mapOf(
            "en" to "Groundnut/Peanut",
            "hi" to "मूंगफली",
            "sw" to "Karanga"
        ),
        "soybean" to mapOf(
            "en" to "Soybean",
            "hi" to "सोयाबीन",
            "sw" to "Soya"
        ),
        "mustard" to mapOf(
            "en" to "Mustard",
            "hi" to "सरसों",
            "sw" to "Haradali"
        ),
        "sunflower" to mapOf(
            "en" to "Sunflower",
            "hi" to "सूरजमुखी",
            "sw" to "Alizeti"
        ),
        "sesame" to mapOf(
            "en" to "Sesame",
            "hi" to "तिल",
            "sw" to "Ufuta"
        ),
        
        // Spices
        "turmeric" to mapOf(
            "en" to "Turmeric",
            "hi" to "हल्दी",
            "sw" to "Binzari"
        ),
        "ginger" to mapOf(
            "en" to "Ginger",
            "hi" to "अदरक",
            "sw" to "Tangawizi"
        ),
        "chilli" to mapOf(
            "en" to "Chilli",
            "hi" to "मिर्च",
            "sw" to "Pilipili"
        ),
        "coriander" to mapOf(
            "en" to "Coriander",
            "hi" to "धनिया",
            "sw" to "Giligilani"
        ),
        "cumin" to mapOf(
            "en" to "Cumin",
            "hi" to "जीरा",
            "sw" to "Bizari"
        ),
        
        // Fruits
        "mango" to mapOf(
            "en" to "Mango",
            "hi" to "आम",
            "sw" to "Embe"
        ),
        "banana" to mapOf(
            "en" to "Banana",
            "hi" to "केला",
            "sw" to "Ndizi"
        ),
        "apple" to mapOf(
            "en" to "Apple",
            "hi" to "सेब",
            "sw" to "Tofaa"
        ),
        "grape" to mapOf(
            "en" to "Grape",
            "hi" to "अंगूर",
            "sw" to "Zabibu"
        ),
        "orange" to mapOf(
            "en" to "Orange",
            "hi" to "संतरा",
            "sw" to "Chungwa"
        ),
        "papaya" to mapOf(
            "en" to "Papaya",
            "hi" to "पपीता",
            "sw" to "Papai"
        ),
        
        // Plantation crops
        "tea" to mapOf(
            "en" to "Tea",
            "hi" to "चाय",
            "sw" to "Chai"
        ),
        "coffee" to mapOf(
            "en" to "Coffee",
            "hi" to "कॉफी",
            "sw" to "Kahawa"
        ),
        "coconut" to mapOf(
            "en" to "Coconut",
            "hi" to "नारियल",
            "sw" to "Nazi"
        ),
        "rubber" to mapOf(
            "en" to "Rubber",
            "hi" to "रबर",
            "sw" to "Mpira"
        )
    )
    
    fun getCropName(cropId: String, languageCode: String): String {
        val translation = translations[cropId]?.get(languageCode)
        val englishFallback = translations[cropId]?.get("en")
        
        // Debug logging
        android.util.Log.d("CropTranslations", "Getting crop name for id: $cropId, language: $languageCode")
        android.util.Log.d("CropTranslations", "Translation found: $translation, English fallback: $englishFallback")
        
        return translation 
            ?: englishFallback // Fallback to English
            ?: cropId.replaceFirstChar { it.uppercase() } // Fallback to ID
    }
    
    // Get all translations for a crop (useful for search)
    fun getAllTranslations(cropId: String): List<String> {
        return translations[cropId]?.values?.toList() ?: emptyList()
    }
}