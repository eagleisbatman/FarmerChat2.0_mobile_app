package com.digitalgreen.farmerchat.data

/**
 * Manages the master list of livestock with categorization and search functionality
 */
object LivestockManager {
    
    data class Livestock(
        val id: String,
        val defaultName: String, // Default English name
        val scientificName: String = "",
        val category: LivestockCategory,
        val emoji: String,
        val primaryPurpose: List<Purpose> = emptyList(),
        val commonBreeds: List<String> = emptyList(),
        val searchKeywords: List<String> = emptyList()
    ) {
        // Get localized name
        fun getLocalizedName(languageCode: String): String {
            return LivestockTranslations.getLivestockName(id, languageCode)
        }
    }
    
    enum class LivestockCategory(val displayName: String) {
        CATTLE("Cattle"),
        POULTRY("Poultry"),
        SMALL_RUMINANTS("Goats & Sheep"),
        SWINE("Pigs"),
        AQUACULTURE("Fish & Aquaculture"),
        OTHERS("Other Animals");
        
        fun getLocalizedName(languageCode: String): String {
            return com.digitalgreen.farmerchat.utils.StringsManager.getLivestockCategoryString(this, languageCode)
        }
    }
    
    enum class Purpose(val displayName: String) {
        DAIRY("Dairy"),
        MEAT("Meat"),
        EGGS("Eggs"),
        WOOL("Wool"),
        DRAFT("Draft/Work"),
        BREEDING("Breeding"),
        MANURE("Manure"),
        LEATHER("Leather"),
        HONEY("Honey"),
        SILK("Silk")
    }
    
    private val livestock = listOf(
        // Cattle
        Livestock("cow", "Cow", "Bos taurus", LivestockCategory.CATTLE, "🐄",
            listOf(Purpose.DAIRY, Purpose.MEAT, Purpose.DRAFT, Purpose.MANURE),
            listOf("Holstein", "Jersey", "Gir", "Sahiwal", "Red Sindhi"),
            listOf("gai", "गाय", "ಹಸು", "பசு", "inek")),
        Livestock("buffalo", "Buffalo", "Bubalus bubalis", LivestockCategory.CATTLE, "🐃",
            listOf(Purpose.DAIRY, Purpose.MEAT, Purpose.DRAFT),
            listOf("Murrah", "Nili-Ravi", "Surti", "Mehsana", "Jaffarabadi"),
            listOf("bhains", "भैंस", "ಎಮ್ಮೆ", "எருமை", "kerbau")),
        Livestock("bull", "Bull/Ox", "Bos taurus", LivestockCategory.CATTLE, "🐂",
            listOf(Purpose.DRAFT, Purpose.BREEDING),
            listOf("Hallikar", "Amritmahal", "Kangayam", "Red Sindhi"),
            listOf("bail", "बैल", "ಎತ್ತು", "காளை")),
        
        // Small Ruminants
        Livestock("goat", "Goat", "Capra hircus", LivestockCategory.SMALL_RUMINANTS, "🐐",
            listOf(Purpose.MEAT, Purpose.DAIRY, Purpose.LEATHER),
            listOf("Boer", "Jamunapari", "Beetal", "Black Bengal", "Sirohi"),
            listOf("bakri", "बकरी", "ಮೇಕೆ", "ஆடு", "kambing")),
        Livestock("sheep", "Sheep", "Ovis aries", LivestockCategory.SMALL_RUMINANTS, "🐑",
            listOf(Purpose.MEAT, Purpose.WOOL, Purpose.DAIRY),
            listOf("Merino", "Deccani", "Nellore", "Marwari", "Garole"),
            listOf("bhed", "भेड़", "ಕುರಿ", "செம்மறி", "biri-biri")),
        
        // Poultry
        Livestock("chicken", "Chicken", "Gallus gallus domesticus", LivestockCategory.POULTRY, "🐓",
            listOf(Purpose.EGGS, Purpose.MEAT),
            listOf("Leghorn", "Rhode Island Red", "Aseel", "Kadaknath", "Broiler"),
            listOf("murgi", "मुर्गी", "ಕೋಳಿ", "கோழி", "ayam")),
        Livestock("duck", "Duck", "Anas platyrhynchos domesticus", LivestockCategory.POULTRY, "🦆",
            listOf(Purpose.EGGS, Purpose.MEAT),
            listOf("Khaki Campbell", "Indian Runner", "Pekin", "Muscovy"),
            listOf("batakh", "बतख", "ಬಾತುಕೋಳಿ", "வாத்து", "bebek")),
        Livestock("turkey", "Turkey", "Meleagris gallopavo", LivestockCategory.POULTRY, "🦃",
            listOf(Purpose.MEAT),
            listOf("Broad Breasted White", "Beltsville", "Bronze"),
            listOf("टर्की", "ಟರ್ಕಿ", "வான்கோழி")),
        Livestock("quail", "Quail", "Coturnix coturnix", LivestockCategory.POULTRY, "🐦",
            listOf(Purpose.EGGS, Purpose.MEAT),
            listOf("Japanese Quail", "Bobwhite"),
            listOf("bater", "बटेर", "ಕ್ವೇಲ್", "காடை")),
        Livestock("goose", "Goose", "Anser anser domesticus", LivestockCategory.POULTRY, "🦢",
            listOf(Purpose.MEAT, Purpose.EGGS),
            listOf("Toulouse", "Embden", "Chinese"),
            listOf("hans", "हंस", "ಹಂಸ", "வாத்து")),
        
        // Swine
        Livestock("pig", "Pig", "Sus scrofa domesticus", LivestockCategory.SWINE, "🐖",
            listOf(Purpose.MEAT),
            listOf("Yorkshire", "Landrace", "Duroc", "Hampshire", "Ghungroo"),
            listOf("suar", "सूअर", "ಹಂದಿ", "பன்றி", "babi")),
        
        // Aquaculture
        Livestock("fish", "Fish", "Various species", LivestockCategory.AQUACULTURE, "🐟",
            listOf(Purpose.MEAT),
            listOf("Rohu", "Catla", "Tilapia", "Carp", "Pangasius"),
            listOf("machli", "मछली", "ಮೀನು", "மீன்", "ikan")),
        Livestock("shrimp", "Shrimp/Prawn", "Penaeus spp.", LivestockCategory.AQUACULTURE, "🦐",
            listOf(Purpose.MEAT),
            listOf("Tiger Shrimp", "Vannamei", "Freshwater Prawn"),
            listOf("jhinga", "झींगा", "ಸೀಗಡಿ", "இறால்", "udang")),
        
        // Others
        Livestock("rabbit", "Rabbit", "Oryctolagus cuniculus", LivestockCategory.OTHERS, "🐰",
            listOf(Purpose.MEAT, Purpose.WOOL),
            listOf("New Zealand White", "Soviet Chinchilla", "Grey Giant"),
            listOf("khargosh", "खरगोश", "ಮೊಲ", "முயல்", "kelinci")),
        Livestock("horse", "Horse", "Equus caballus", LivestockCategory.OTHERS, "🐴",
            listOf(Purpose.DRAFT, Purpose.BREEDING),
            listOf("Marwari", "Kathiawari", "Thoroughbred", "Arabian"),
            listOf("ghoda", "घोड़ा", "ಕುದುರೆ", "குதிரை", "kuda")),
        Livestock("donkey", "Donkey", "Equus asinus", LivestockCategory.OTHERS, "🫏",
            listOf(Purpose.DRAFT),
            listOf("Halari", "Spiti", "Kutchi"),
            listOf("gadha", "गधा", "ಕತ್ತೆ", "கழுதை", "keledai")),
        Livestock("camel", "Camel", "Camelus dromedarius", LivestockCategory.OTHERS, "🐪",
            listOf(Purpose.DRAFT, Purpose.DAIRY, Purpose.MEAT),
            listOf("Bikaneri", "Jaisalmeri", "Kutchi", "Mewari"),
            listOf("oont", "ऊंट", "ಒಂಟೆ", "ஒட்டகம்", "unta")),
        Livestock("bee", "Honey Bee", "Apis spp.", LivestockCategory.OTHERS, "🐝",
            listOf(Purpose.HONEY),
            listOf("Indian Bee", "Italian Bee", "Rock Bee"),
            listOf("madhumakhi", "मधुमक्खी", "ಜೇನುನೊಣ", "தேனீ", "lebah")),
        Livestock("silkworm", "Silkworm", "Bombyx mori", LivestockCategory.OTHERS, "🐛",
            listOf(Purpose.SILK),
            listOf("Mulberry", "Tasar", "Eri", "Muga"),
            listOf("resham keet", "रेशम कीट", "ರೇಷ್ಮೆ ಹುಳು", "பட்டுப்புழு"))
    )
    
    fun getAllLivestock(): List<Livestock> = livestock
    
    fun getLivestockByCategory(category: LivestockCategory): List<Livestock> = 
        livestock.filter { it.category == category }
    
    fun getCategories(): List<LivestockCategory> = LivestockCategory.values().toList()
    
    fun getLivestockByPurpose(purpose: Purpose): List<Livestock> =
        livestock.filter { it.primaryPurpose.contains(purpose) }
    
    fun searchLivestock(query: String): List<Livestock> {
        val lowercaseQuery = query.lowercase()
        return livestock.filter { animal ->
            animal.id.lowercase().contains(lowercaseQuery) ||
            animal.scientificName.lowercase().contains(lowercaseQuery) ||
            animal.category.displayName.lowercase().contains(lowercaseQuery) ||
            animal.primaryPurpose.any { it.displayName.lowercase().contains(lowercaseQuery) } ||
            animal.commonBreeds.any { it.lowercase().contains(lowercaseQuery) } ||
            animal.searchKeywords.any { it.lowercase().contains(lowercaseQuery) } ||
            // Search in all translations
            LivestockTranslations.getAllTranslations(animal.id).any { 
                it.lowercase().contains(lowercaseQuery) 
            }
        }
    }
    
    fun getLivestockById(id: String): Livestock? = livestock.find { it.id == id }
}