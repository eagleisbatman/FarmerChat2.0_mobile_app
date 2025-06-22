package com.digitalgreen.farmerchat.utils

/**
 * Maps geographic locations to their primary supported languages
 * Used for smart language selection during onboarding
 */
object LocationLanguageMapper {
    
    data class LocationLanguageMapping(
        val location: String,
        val primaryLanguages: List<String>, // Always includes English + local language(s)
        val countryCode: String // For phone number formatting
    )
    
    // India - State-wise language mapping
    private val indiaStates = mapOf(
        "Andhra Pradesh" to LocationLanguageMapping("Andhra Pradesh", listOf("en", "te"), "+91"),
        "Telangana" to LocationLanguageMapping("Telangana", listOf("en", "te"), "+91"),
        "Bihar" to LocationLanguageMapping("Bihar", listOf("en", "hi", "ur"), "+91"),
        "Uttar Pradesh" to LocationLanguageMapping("Uttar Pradesh", listOf("en", "hi", "ur"), "+91"),
        "Maharashtra" to LocationLanguageMapping("Maharashtra", listOf("en", "mr", "hi"), "+91"),
        "Gujarat" to LocationLanguageMapping("Gujarat", listOf("en", "gu", "hi"), "+91"),
        "Karnataka" to LocationLanguageMapping("Karnataka", listOf("en", "kn", "hi"), "+91"),
        "Tamil Nadu" to LocationLanguageMapping("Tamil Nadu", listOf("en", "ta"), "+91"),
        "Kerala" to LocationLanguageMapping("Kerala", listOf("en", "ml"), "+91"),
        "Odisha" to LocationLanguageMapping("Odisha", listOf("en", "or", "hi"), "+91"),
        "West Bengal" to LocationLanguageMapping("West Bengal", listOf("en", "bn", "hi"), "+91"),
        "Assam" to LocationLanguageMapping("Assam", listOf("en", "as", "hi"), "+91"),
        "Punjab" to LocationLanguageMapping("Punjab", listOf("en", "pa", "hi"), "+91"),
        "Haryana" to LocationLanguageMapping("Haryana", listOf("en", "hi"), "+91"),
        "Rajasthan" to LocationLanguageMapping("Rajasthan", listOf("en", "hi"), "+91"),
        "Madhya Pradesh" to LocationLanguageMapping("Madhya Pradesh", listOf("en", "hi"), "+91"),
        "Chhattisgarh" to LocationLanguageMapping("Chhattisgarh", listOf("en", "hi"), "+91"),
        "Jharkhand" to LocationLanguageMapping("Jharkhand", listOf("en", "hi"), "+91"),
        "Delhi" to LocationLanguageMapping("Delhi", listOf("en", "hi", "ur"), "+91"),
        "Mumbai" to LocationLanguageMapping("Mumbai", listOf("en", "mr", "hi"), "+91"),
        "Bangalore" to LocationLanguageMapping("Bangalore", listOf("en", "kn", "hi"), "+91"),
        "Hyderabad" to LocationLanguageMapping("Hyderabad", listOf("en", "te", "hi"), "+91"),
        "Chennai" to LocationLanguageMapping("Chennai", listOf("en", "ta"), "+91"),
        "Kolkata" to LocationLanguageMapping("Kolkata", listOf("en", "bn", "hi"), "+91"),
        "Pune" to LocationLanguageMapping("Pune", listOf("en", "mr", "hi"), "+91")
    )
    
    // African countries - Swahili belt and other regions
    private val africaCountries = mapOf(
        "Kenya" to LocationLanguageMapping("Kenya", listOf("en", "sw"), "+254"),
        "Tanzania" to LocationLanguageMapping("Tanzania", listOf("en", "sw"), "+255"),
        "Uganda" to LocationLanguageMapping("Uganda", listOf("en", "sw"), "+256"),
        "Rwanda" to LocationLanguageMapping("Rwanda", listOf("en", "rw"), "+250"),
        "Burundi" to LocationLanguageMapping("Burundi", listOf("en", "rn"), "+257"),
        "Ethiopia" to LocationLanguageMapping("Ethiopia", listOf("en", "am"), "+251"),
        "Ghana" to LocationLanguageMapping("Ghana", listOf("en"), "+233"),
        "Nigeria" to LocationLanguageMapping("Nigeria", listOf("en", "ha", "yo", "ig"), "+234"),
        "South Africa" to LocationLanguageMapping("South Africa", listOf("en", "af", "zu", "xh"), "+27"),
        "Zambia" to LocationLanguageMapping("Zambia", listOf("en"), "+260"),
        "Malawi" to LocationLanguageMapping("Malawi", listOf("en"), "+265"),
        "Zimbabwe" to LocationLanguageMapping("Zimbabwe", listOf("en"), "+263"),
        "Botswana" to LocationLanguageMapping("Botswana", listOf("en"), "+267"),
        "Namibia" to LocationLanguageMapping("Namibia", listOf("en", "af"), "+264")
    )
    
    // Latin American countries
    private val latinAmericaCountries = mapOf(
        "Mexico" to LocationLanguageMapping("Mexico", listOf("es", "en"), "+52"),
        "Guatemala" to LocationLanguageMapping("Guatemala", listOf("es", "en"), "+502"),
        "Honduras" to LocationLanguageMapping("Honduras", listOf("es", "en"), "+504"),
        "El Salvador" to LocationLanguageMapping("El Salvador", listOf("es", "en"), "+503"),
        "Nicaragua" to LocationLanguageMapping("Nicaragua", listOf("es", "en"), "+505"),
        "Costa Rica" to LocationLanguageMapping("Costa Rica", listOf("es", "en"), "+506"),
        "Panama" to LocationLanguageMapping("Panama", listOf("es", "en"), "+507"),
        "Colombia" to LocationLanguageMapping("Colombia", listOf("es", "en"), "+57"),
        "Venezuela" to LocationLanguageMapping("Venezuela", listOf("es", "en"), "+58"),
        "Ecuador" to LocationLanguageMapping("Ecuador", listOf("es", "en"), "+593"),
        "Peru" to LocationLanguageMapping("Peru", listOf("es", "en"), "+51"),
        "Bolivia" to LocationLanguageMapping("Bolivia", listOf("es", "en"), "+591"),
        "Paraguay" to LocationLanguageMapping("Paraguay", listOf("es", "en"), "+595"),
        "Uruguay" to LocationLanguageMapping("Uruguay", listOf("es", "en"), "+598"),
        "Argentina" to LocationLanguageMapping("Argentina", listOf("es", "en"), "+54"),
        "Chile" to LocationLanguageMapping("Chile", listOf("es", "en"), "+56"),
        "Brazil" to LocationLanguageMapping("Brazil", listOf("pt", "en"), "+55")
    )
    
    // Other major countries
    private val otherCountries = mapOf(
        "United States" to LocationLanguageMapping("United States", listOf("en", "es"), "+1"),
        "Canada" to LocationLanguageMapping("Canada", listOf("en", "fr"), "+1"),
        "United Kingdom" to LocationLanguageMapping("United Kingdom", listOf("en"), "+44"),
        "Australia" to LocationLanguageMapping("Australia", listOf("en"), "+61"),
        "New Zealand" to LocationLanguageMapping("New Zealand", listOf("en"), "+64"),
        "Ireland" to LocationLanguageMapping("Ireland", listOf("en"), "+353"),
        "France" to LocationLanguageMapping("France", listOf("fr", "en"), "+33"),
        "Germany" to LocationLanguageMapping("Germany", listOf("de", "en"), "+49"),
        "Spain" to LocationLanguageMapping("Spain", listOf("es", "en"), "+34"),
        "Italy" to LocationLanguageMapping("Italy", listOf("it", "en"), "+39"),
        "Netherlands" to LocationLanguageMapping("Netherlands", listOf("nl", "en"), "+31"),
        "Bangladesh" to LocationLanguageMapping("Bangladesh", listOf("bn", "en"), "+880"),
        "Pakistan" to LocationLanguageMapping("Pakistan", listOf("ur", "en"), "+92"),
        "Sri Lanka" to LocationLanguageMapping("Sri Lanka", listOf("si", "ta", "en"), "+94"),
        "Nepal" to LocationLanguageMapping("Nepal", listOf("ne", "en"), "+977"),
        "Bhutan" to LocationLanguageMapping("Bhutan", listOf("dz", "en"), "+975"),
        "Myanmar" to LocationLanguageMapping("Myanmar", listOf("my", "en"), "+95"),
        "Thailand" to LocationLanguageMapping("Thailand", listOf("th", "en"), "+66"),
        "Vietnam" to LocationLanguageMapping("Vietnam", listOf("vi", "en"), "+84"),
        "Cambodia" to LocationLanguageMapping("Cambodia", listOf("km", "en"), "+855"),
        "Laos" to LocationLanguageMapping("Laos", listOf("lo", "en"), "+856"),
        "Philippines" to LocationLanguageMapping("Philippines", listOf("en", "tl"), "+63"),
        "Indonesia" to LocationLanguageMapping("Indonesia", listOf("id", "en"), "+62"),
        "Malaysia" to LocationLanguageMapping("Malaysia", listOf("ms", "en"), "+60"),
        "Singapore" to LocationLanguageMapping("Singapore", listOf("en", "ms", "zh"), "+65"),
        "China" to LocationLanguageMapping("China", listOf("zh", "en"), "+86"),
        "Japan" to LocationLanguageMapping("Japan", listOf("ja", "en"), "+81"),
        "South Korea" to LocationLanguageMapping("South Korea", listOf("ko", "en"), "+82"),
        "Russia" to LocationLanguageMapping("Russia", listOf("ru", "en"), "+7"),
        "Turkey" to LocationLanguageMapping("Turkey", listOf("tr", "en"), "+90"),
        "Iran" to LocationLanguageMapping("Iran", listOf("fa", "en"), "+98"),
        "Afghanistan" to LocationLanguageMapping("Afghanistan", listOf("fa", "ps", "en"), "+93"),
        "Saudi Arabia" to LocationLanguageMapping("Saudi Arabia", listOf("ar", "en"), "+966"),
        "UAE" to LocationLanguageMapping("UAE", listOf("ar", "en"), "+971"),
        "Egypt" to LocationLanguageMapping("Egypt", listOf("ar", "en"), "+20"),
        "Morocco" to LocationLanguageMapping("Morocco", listOf("ar", "fr", "en"), "+212"),
        "Algeria" to LocationLanguageMapping("Algeria", listOf("ar", "fr", "en"), "+213"),
        "Tunisia" to LocationLanguageMapping("Tunisia", listOf("ar", "fr", "en"), "+216"),
        "Libya" to LocationLanguageMapping("Libya", listOf("ar", "en"), "+218"),
        "Sudan" to LocationLanguageMapping("Sudan", listOf("ar", "en"), "+249"),
        "Jordan" to LocationLanguageMapping("Jordan", listOf("ar", "en"), "+962"),
        "Lebanon" to LocationLanguageMapping("Lebanon", listOf("ar", "fr", "en"), "+961"),
        "Syria" to LocationLanguageMapping("Syria", listOf("ar", "en"), "+963"),
        "Iraq" to LocationLanguageMapping("Iraq", listOf("ar", "en"), "+964"),
        "Israel" to LocationLanguageMapping("Israel", listOf("he", "ar", "en"), "+972")
    )
    
    // Combined mapping for easy lookup
    private val allMappings = indiaStates + africaCountries + latinAmericaCountries + otherCountries
    
    /**
     * Get language suggestions based on location
     * @param location The user's selected location
     * @return LocationLanguageMapping with suggested languages and country code
     */
    fun getLanguagesForLocation(location: String): LocationLanguageMapping? {
        return allMappings[location]
    }
    
    /**
     * Get all available locations for selection
     * @return List of all supported locations
     */
    fun getAllSupportedLocations(): List<String> {
        return allMappings.keys.sorted()
    }
    
    /**
     * Get country code for a location
     * @param location The user's selected location
     * @return Country code (e.g., "+91", "+1") or "+1" as default
     */
    fun getCountryCodeForLocation(location: String): String {
        // First try exact match
        allMappings[location]?.let { return it.countryCode }
        
        // Try partial matching for GPS locations that might be formatted differently
        val locationLower = location.lowercase()
        
        // Check if any known location is contained in the provided location string
        for ((key, mapping) in allMappings) {
            val keyLower = key.lowercase()
            
            // Check if the key is contained in the location or vice versa
            if (locationLower.contains(keyLower) || keyLower.contains(locationLower)) {
                android.util.Log.d("LocationMapper", "Partial match: '$location' matched '$key' -> ${mapping.countryCode}")
                return mapping.countryCode
            }
        }
        
        // Special cases for common patterns
        when {
            locationLower.contains("bangalore") || locationLower.contains("bengaluru") -> return "+91"
            locationLower.contains("mumbai") || locationLower.contains("bombay") -> return "+91"
            locationLower.contains("delhi") || locationLower.contains("new delhi") -> return "+91"
            locationLower.contains("chennai") || locationLower.contains("madras") -> return "+91"
            locationLower.contains("kolkata") || locationLower.contains("calcutta") -> return "+91"
            locationLower.contains("hyderabad") -> return "+91"
            locationLower.contains("pune") -> return "+91"
            locationLower.contains("karnataka") -> return "+91"
            locationLower.contains("maharashtra") -> return "+91"
            locationLower.contains("tamil nadu") -> return "+91"
            locationLower.contains("kerala") -> return "+91"
            locationLower.contains("gujarat") -> return "+91"
            locationLower.contains("rajasthan") -> return "+91"
            locationLower.contains("bihar") -> return "+91"
            locationLower.contains("uttar pradesh") -> return "+91"
            locationLower.contains("india") -> return "+91"
            locationLower.contains("united states") || locationLower.contains("usa") -> return "+1"
            locationLower.contains("canada") -> return "+1"
            locationLower.contains("kenya") -> return "+254"
            locationLower.contains("tanzania") -> return "+255"
            locationLower.contains("nigeria") -> return "+234"
            locationLower.contains("ghana") -> return "+233"
            locationLower.contains("south africa") -> return "+27"
            locationLower.contains("mexico") -> return "+52"
            locationLower.contains("brazil") -> return "+55"
            locationLower.contains("argentina") -> return "+54"
            locationLower.contains("chile") -> return "+56"
            locationLower.contains("colombia") -> return "+57"
            locationLower.contains("peru") -> return "+51"
        }
        
        android.util.Log.d("LocationMapper", "No match found for '$location', defaulting to +1")
        return "+1"
    }
    
    /**
     * Search locations by query
     * @param query Search term
     * @return List of matching locations
     */
    fun searchLocations(query: String): List<String> {
        if (query.isBlank()) return getAllSupportedLocations()
        
        return allMappings.keys.filter { location ->
            location.contains(query, ignoreCase = true)
        }.sorted()
    }
    
    /**
     * Get primary languages for a location (first 2-3 most common)
     * @param location The user's selected location
     * @return List of primary language codes
     */
    fun getPrimaryLanguagesForLocation(location: String): List<String> {
        return allMappings[location]?.primaryLanguages ?: listOf("en")
    }
    
    /**
     * Check if a location is in India (for special handling)
     * @param location The location to check
     * @return true if location is in India
     */
    fun isIndianLocation(location: String): Boolean {
        return indiaStates.containsKey(location)
    }
    
    /**
     * Get region name for a location
     * @param location The location
     * @return Region name (India, Africa, Latin America, etc.)
     */
    fun getRegionForLocation(location: String): String {
        return when {
            indiaStates.containsKey(location) -> "India"
            africaCountries.containsKey(location) -> "Africa"
            latinAmericaCountries.containsKey(location) -> "Latin America"
            else -> "Other"
        }
    }
}