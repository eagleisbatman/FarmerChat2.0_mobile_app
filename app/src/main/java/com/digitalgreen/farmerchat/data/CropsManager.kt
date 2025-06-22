package com.digitalgreen.farmerchat.data

/**
 * Manages the master list of crops with categorization and search functionality
 */
object CropsManager {
    
    data class Crop(
        val id: String,
        val defaultName: String, // Default English name
        val scientificName: String = "",
        val category: CropCategory,
        val emoji: String,
        val growingSeason: String = "",
        val commonRegions: List<String> = emptyList(),
        val searchKeywords: List<String> = emptyList()
    ) {
        // Get localized name
        fun getLocalizedName(languageCode: String): String {
            return CropTranslations.getCropName(id, languageCode)
        }
    }
    
    enum class CropCategory(val displayName: String) {
        CEREALS("Cereals & Grains"),
        PULSES("Pulses & Legumes"),
        VEGETABLES("Vegetables"),
        FRUITS("Fruits"),
        CASH_CROPS("Cash Crops"),
        OILSEEDS("Oilseeds"),
        SPICES("Spices & Herbs"),
        PLANTATION("Plantation Crops"),
        FODDER("Fodder Crops"),
        FLOWERS("Flowers & Ornamentals");
        
        fun getLocalizedName(languageCode: String): String {
            return com.digitalgreen.farmerchat.utils.StringsManager.getCropCategoryString(this, languageCode)
        }
    }
    
    private val crops = listOf(
        // Cereals & Grains
        Crop("wheat", "Wheat", "Triticum aestivum", CropCategory.CEREALS, "🌾",
            "Winter", listOf("Asia", "Europe", "North America"),
            listOf("gehun", "गेहूं", "ಗೋಧಿ", "கோதுமை")),
        Crop("rice", "Rice", "Oryza sativa", CropCategory.CEREALS, "🌾",
            "Monsoon", listOf("Asia", "Africa"),
            listOf("paddy", "dhaan", "chawal", "चावल", "ಅಕ್ಕಿ", "அரிசி")),
        Crop("maize", "Maize/Corn", "Zea mays", CropCategory.CEREALS, "🌽",
            "Summer", listOf("Americas", "Asia", "Africa"),
            listOf("corn", "makka", "मक्का", "ಜೋಳ", "மக்காச்சோளம்")),
        Crop("sorghum", "Sorghum", "Sorghum bicolor", CropCategory.CEREALS, "🌾",
            "Summer", listOf("Africa", "Asia"),
            listOf("jowar", "ज्वार", "ಜೋಳ", "சோளம்")),
        Crop("millet", "Millet", "Pennisetum glaucum", CropCategory.CEREALS, "🌾",
            "Summer", listOf("Africa", "Asia"),
            listOf("bajra", "बाजरा", "ಸಜ್ಜೆ", "கம்பு")),
        Crop("barley", "Barley", "Hordeum vulgare", CropCategory.CEREALS, "🌾",
            "Winter", listOf("Europe", "Asia"),
            listOf("jau", "जौ", "ಬಾರ್ಲಿ", "பார்லி")),
        
        // Pulses & Legumes
        Crop("chickpea", "Chickpea", "Cicer arietinum", CropCategory.PULSES, "🫘",
            "Winter", listOf("Asia", "Africa"),
            listOf("chana", "gram", "चना", "ಕಡಲೆ", "கொண்டைக்கடலை")),
        Crop("lentil", "Lentil", "Lens culinaris", CropCategory.PULSES, "🫘",
            "Winter", listOf("Asia", "Africa"),
            listOf("masoor", "मसूर", "ಮಸೂರ", "பருப்பு")),
        Crop("blackgram", "Black Gram", "Vigna mungo", CropCategory.PULSES, "🫘",
            "Summer", listOf("Asia"),
            listOf("urad", "उड़द", "ಉದ್ದು", "உளுந்து")),
        Crop("greengram", "Green Gram", "Vigna radiata", CropCategory.PULSES, "🫘",
            "Summer", listOf("Asia"),
            listOf("moong", "mung", "मूंग", "ಹೆಸರು", "பச்சைப்பயறு")),
        Crop("pigeonpea", "Pigeon Pea", "Cajanus cajan", CropCategory.PULSES, "🫘",
            "Monsoon", listOf("Asia", "Africa"),
            listOf("arhar", "tur", "अरहर", "ತೊಗರಿ", "துவரை")),
        Crop("fieldpea", "Field Pea", "Pisum sativum", CropCategory.PULSES, "🫘",
            "Winter", listOf("Europe", "Asia"),
            listOf("matar", "मटर", "ಬಟಾಣಿ", "பட்டாணி")),
        
        // Vegetables
        Crop("tomato", "Tomato", "Solanum lycopersicum", CropCategory.VEGETABLES, "🍅",
            "All Season", listOf("Global"),
            listOf("tamatar", "टमाटर", "ಟೊಮೇಟೊ", "தக்காளி")),
        Crop("onion", "Onion", "Allium cepa", CropCategory.VEGETABLES, "🧅",
            "Winter", listOf("Global"),
            listOf("pyaz", "प्याज", "ಈರುಳ್ಳಿ", "வெங்காயம்")),
        Crop("potato", "Potato", "Solanum tuberosum", CropCategory.VEGETABLES, "🥔",
            "Winter", listOf("Global"),
            listOf("aloo", "आलू", "ಆಲೂಗಡ್ಡೆ", "உருளைக்கிழங்கு")),
        Crop("cabbage", "Cabbage", "Brassica oleracea", CropCategory.VEGETABLES, "🥬",
            "Winter", listOf("Global"),
            listOf("patta gobhi", "बंदगोभी", "ಎಲೆಕೋಸು", "முட்டைக்கோஸ்")),
        Crop("cauliflower", "Cauliflower", "Brassica oleracea var. botrytis", CropCategory.VEGETABLES, "🥦",
            "Winter", listOf("Global"),
            listOf("phool gobhi", "फूलगोभी", "ಹೂಕೋಸು", "காலிஃபிளவர்")),
        Crop("carrot", "Carrot", "Daucus carota", CropCategory.VEGETABLES, "🥕",
            "Winter", listOf("Global"),
            listOf("gajar", "गाजर", "ಗಜ್ಜರಿ", "கேரட்")),
        Crop("eggplant", "Eggplant/Brinjal", "Solanum melongena", CropCategory.VEGETABLES, "🍆",
            "All Season", listOf("Asia", "Africa"),
            listOf("baingan", "brinjal", "बैंगन", "ಬದನೆ", "கத்திரிக்காய்")),
        Crop("okra", "Okra", "Abelmoschus esculentus", CropCategory.VEGETABLES, "🥒",
            "Summer", listOf("Asia", "Africa"),
            listOf("bhindi", "ladyfinger", "भिंडी", "ಬೆಂಡೆ", "வெண்டைக்காய்")),
        Crop("spinach", "Spinach", "Spinacia oleracea", CropCategory.VEGETABLES, "🥬",
            "Winter", listOf("Global"),
            listOf("palak", "पालक", "ಪಾಲಕ", "கீரை")),
        
        // Cash Crops
        Crop("cotton", "Cotton", "Gossypium spp.", CropCategory.CASH_CROPS, "🏵️",
            "Summer", listOf("Asia", "Americas", "Africa"),
            listOf("kapas", "कपास", "ಹತ್ತಿ", "பருத்தி")),
        Crop("sugarcane", "Sugarcane", "Saccharum officinarum", CropCategory.CASH_CROPS, "🎋",
            "All Season", listOf("Tropical regions"),
            listOf("ganna", "गन्ना", "ಕಬ್ಬು", "கரும்பு")),
        Crop("tobacco", "Tobacco", "Nicotiana tabacum", CropCategory.CASH_CROPS, "🍃",
            "Summer", listOf("Americas", "Asia"),
            listOf("tambaku", "तंबाकू", "ತಂಬಾಕು", "புகையிலை")),
        Crop("jute", "Jute", "Corchorus spp.", CropCategory.CASH_CROPS, "🌿",
            "Summer", listOf("Asia"),
            listOf("पटसन", "ಸೆಣಬು", "சணல்")),
        
        // Oilseeds
        Crop("groundnut", "Groundnut/Peanut", "Arachis hypogaea", CropCategory.OILSEEDS, "🥜",
            "Summer", listOf("Asia", "Africa", "Americas"),
            listOf("moongfali", "मूंगफली", "ಕಡಲೆಕಾಯಿ", "நிலக்கடலை")),
        Crop("soybean", "Soybean", "Glycine max", CropCategory.OILSEEDS, "🌱",
            "Summer", listOf("Americas", "Asia"),
            listOf("सोयाबीन", "ಸೋಯಾಬೀನ್", "சோயாபீன்")),
        Crop("mustard", "Mustard", "Brassica juncea", CropCategory.OILSEEDS, "🌼",
            "Winter", listOf("Asia"),
            listOf("sarson", "सरसों", "ಸಾಸಿವೆ", "கடுகு")),
        Crop("sunflower", "Sunflower", "Helianthus annuus", CropCategory.OILSEEDS, "🌻",
            "All Season", listOf("Global"),
            listOf("surajmukhi", "सूरजमुखी", "ಸೂರ್ಯಕಾಂತಿ", "சூரியகாந்தி")),
        Crop("sesame", "Sesame", "Sesamum indicum", CropCategory.OILSEEDS, "🌾",
            "Summer", listOf("Asia", "Africa"),
            listOf("til", "तिल", "ಎಳ್ಳು", "எள்")),
        
        // Spices & Herbs
        Crop("turmeric", "Turmeric", "Curcuma longa", CropCategory.SPICES, "🌿",
            "Monsoon", listOf("Asia"),
            listOf("haldi", "हल्दी", "ಅರಿಶಿನ", "மஞ்சள்")),
        Crop("ginger", "Ginger", "Zingiber officinale", CropCategory.SPICES, "🌿",
            "Summer", listOf("Asia"),
            listOf("adrak", "अदरक", "ಶುಂಠಿ", "இஞ்சி")),
        Crop("chilli", "Chilli", "Capsicum annuum", CropCategory.SPICES, "🌶️",
            "All Season", listOf("Global"),
            listOf("mirch", "मिर्च", "ಮೆಣಸಿನಕಾಯಿ", "மிளகாய்")),
        Crop("coriander", "Coriander", "Coriandrum sativum", CropCategory.SPICES, "🌿",
            "Winter", listOf("Asia"),
            listOf("dhania", "धनिया", "ಕೊತ್ತಂಬರಿ", "கொத்தமல்லி")),
        Crop("cumin", "Cumin", "Cuminum cyminum", CropCategory.SPICES, "🌿",
            "Winter", listOf("Asia"),
            listOf("jeera", "जीरा", "ಜೀರಿಗೆ", "சீரகம்")),
        
        // Fruits
        Crop("mango", "Mango", "Mangifera indica", CropCategory.FRUITS, "🥭",
            "Summer", listOf("Asia"),
            listOf("aam", "आम", "ಮಾವು", "மாம்பழம்")),
        Crop("banana", "Banana", "Musa spp.", CropCategory.FRUITS, "🍌",
            "All Season", listOf("Tropical regions"),
            listOf("kela", "केला", "ಬಾಳೆ", "வாழை")),
        Crop("apple", "Apple", "Malus domestica", CropCategory.FRUITS, "🍎",
            "Winter", listOf("Temperate regions"),
            listOf("seb", "सेब", "ಸೇಬು", "ஆப்பிள்")),
        Crop("grape", "Grape", "Vitis vinifera", CropCategory.FRUITS, "🍇",
            "Summer", listOf("Temperate regions"),
            listOf("angoor", "अंगूर", "ದ್ರಾಕ್ಷಿ", "திராட்சை")),
        Crop("orange", "Orange", "Citrus sinensis", CropCategory.FRUITS, "🍊",
            "Winter", listOf("Subtropical regions"),
            listOf("santra", "संतरा", "ಕಿತ್ತಳೆ", "ஆரஞ்சு")),
        Crop("papaya", "Papaya", "Carica papaya", CropCategory.FRUITS, "🥭",
            "All Season", listOf("Tropical regions"),
            listOf("papita", "पपीता", "ಪಪ್ಪಾಯಿ", "பப்பாளி")),
        
        // Plantation Crops
        Crop("tea", "Tea", "Camellia sinensis", CropCategory.PLANTATION, "🍃",
            "All Season", listOf("Asia"),
            listOf("chai", "चाय", "ಚಹಾ", "தேயிலை")),
        Crop("coffee", "Coffee", "Coffea spp.", CropCategory.PLANTATION, "☕",
            "All Season", listOf("Tropical regions"),
            listOf("कॉफी", "ಕಾಫಿ", "காபி")),
        Crop("coconut", "Coconut", "Cocos nucifera", CropCategory.PLANTATION, "🥥",
            "All Season", listOf("Tropical coastal"),
            listOf("nariyal", "नारियल", "ತೆಂಗು", "தேங்காய்")),
        Crop("rubber", "Rubber", "Hevea brasiliensis", CropCategory.PLANTATION, "🌳",
            "All Season", listOf("Tropical regions"),
            listOf("रबर", "ರಬ್ಬರ್", "இரப்பர்"))
    )
    
    fun getAllCrops(): List<Crop> = crops
    
    fun getCropsByCategory(category: CropCategory): List<Crop> = 
        crops.filter { it.category == category }
    
    fun getCategories(): List<CropCategory> = CropCategory.values().toList()
    
    fun searchCrops(query: String): List<Crop> {
        val lowercaseQuery = query.lowercase()
        return crops.filter { crop ->
            crop.id.lowercase().contains(lowercaseQuery) ||
            crop.scientificName.lowercase().contains(lowercaseQuery) ||
            crop.category.displayName.lowercase().contains(lowercaseQuery) ||
            crop.searchKeywords.any { it.lowercase().contains(lowercaseQuery) } ||
            // Search in all translations
            CropTranslations.getAllTranslations(crop.id).any { 
                it.lowercase().contains(lowercaseQuery) 
            }
        }
    }
    
    fun getCropById(id: String): Crop? = crops.find { it.id == id }
}