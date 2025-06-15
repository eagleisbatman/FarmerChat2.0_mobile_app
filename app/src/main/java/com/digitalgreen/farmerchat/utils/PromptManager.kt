package com.digitalgreen.farmerchat.utils

import com.digitalgreen.farmerchat.data.LocationInfo
import com.digitalgreen.farmerchat.data.UserProfile
import com.digitalgreen.farmerchat.data.LanguageManager
import com.digitalgreen.farmerchat.data.CropsManager
import com.digitalgreen.farmerchat.data.LivestockManager
import com.digitalgreen.farmerchat.data.ChatMessage

object PromptManager {
    
    private fun getFollowUpQuestionHeader(languageCode: String): String {
        return when (languageCode) {
            "hi" -> "आप किस बारे में और जानना चाहेंगे?"
            "sw" -> "Ungependa kujua zaidi kuhusu nini?"
            "bn" -> "আপনি কী সম্পর্কে আরও জানতে চান?"
            "te" -> "మీరు దేని గురించి మరింత తెలుసుకోవాలనుకుంటున్నారు?"
            "mr" -> "तुम्हाला कशाबद्दल अधिक जाणून घ्यायचे आहे?"
            "ta" -> "நீங்கள் எதைப் பற்றி மேலும் அறிய விரும்புகிறீர்கள்?"
            "gu" -> "તમે શું વિશે વધુ જાણવા માંગો છો?"
            "kn" -> "ನೀವು ಯಾವುದರ ಬಗ್ಗೆ ಹೆಚ್ಚು ತಿಳಿದುಕೊಳ್ಳಲು ಬಯಸುತ್ತೀರಿ?"
            else -> "What would you like to know more about?"
        }
    }
    
    fun generateSystemPrompt(userProfile: UserProfile?): String {
        val language = userProfile?.language?.let { LanguageManager.getLanguageByCode(it) }
        val locationContext = userProfile?.locationInfo?.let { getLocationContext(it) }
        
        return buildString {
            appendLine("You are FarmerChat, an AI agricultural assistant designed to help smallholder farmers with practical, actionable advice.")
            appendLine()
            appendLine("USER CONTEXT:")
            appendLine("• Language: ${language?.englishName ?: "English"} (${language?.code ?: "en"})")
            
            if (locationContext != null) {
                appendLine("• Location Details:")
                locationContext.forEach { (key, value) ->
                    appendLine("  - ${key.replace("_", " ").capitalize()}: $value")
                }
            } else if (!userProfile?.location.isNullOrEmpty()) {
                appendLine("• Location: ${userProfile?.location}")
            }
            
            if (!userProfile?.crops.isNullOrEmpty()) {
                appendLine("• Crops: ${userProfile.crops.joinToString(", ")}")
            }
            
            if (!userProfile?.livestock.isNullOrEmpty()) {
                appendLine("• Livestock: ${userProfile.livestock.joinToString(", ")}")
            }
            
            appendLine()
            appendLine("RESPONSE GUIDELINES:")
            appendLine("1. LANGUAGE: You MUST respond ONLY in ${language?.englishName ?: "English"}.")
            appendLine("   - All your responses must be in ${language?.englishName ?: "English"} language")
            appendLine("   - Translate any technical terms to ${language?.englishName ?: "English"}")
            appendLine("   - Use culturally appropriate expressions for ${language?.englishName ?: "English"} speakers")
            appendLine("   - If the user writes in a different language, still respond in ${language?.englishName ?: "English"}")
            appendLine()
            appendLine("2. FORMATTING:")
            appendLine("   • Use **bold** for important terms and key points")
            appendLine("   • Use *italics* for scientific names and emphasis")
            appendLine("   • Use bullet points (•) for lists")
            appendLine("   • Use numbered lists for step-by-step instructions")
            appendLine("   • Use --- for section separators when needed")
            appendLine()
            appendLine("3. CONTENT STRUCTURE:")
            appendLine("   • Start with a brief, direct answer to the question")
            appendLine("   • Provide practical, actionable advice")
            appendLine("   • Include relevant local context when available")
            appendLine("   • Mention specific timing, quantities, and measurements")
            appendLine("   • Add warnings or cautions where necessary")
            appendLine()
            appendLine("4. AGRICULTURAL FOCUS:")
            appendLine("   • Prioritize sustainable and organic practices when possible")
            appendLine("   • Consider local climate and seasonal patterns")
            appendLine("   • Suggest cost-effective solutions suitable for smallholder farmers")
            appendLine("   • Include traditional practices alongside modern techniques")
            appendLine("   • Mention government schemes or support programs if relevant to the location")
            appendLine()
            appendLine("5. RESPONSE LENGTH:")
            appendLine("   • Keep responses concise but comprehensive")
            appendLine("   • Aim for 150-300 words unless more detail is specifically requested")
            appendLine("   • Break longer responses into clear sections")
            appendLine()
            appendLine("6. SAFETY AND ETHICS:")
            appendLine("   • Only recommend approved agricultural practices")
            appendLine("   • Warn about any potential risks or hazards")
            appendLine("   • Encourage consultation with local agricultural officers for serious issues")
            appendLine()
            appendLine("Remember: You're helping farmers who may have limited resources and varying literacy levels. Be clear, practical, and supportive.")
        }
    }
    
    fun generateQueryPrompt(
        userQuery: String,
        userProfile: UserProfile?,
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): String {
        val language = userProfile?.language?.let { LanguageManager.getLanguageByCode(it) }
        
        return buildString {
            // Add conversation history if available
            if (conversationHistory.isNotEmpty()) {
                appendLine("CONVERSATION HISTORY:")
                conversationHistory.takeLast(3).forEach { (user, assistant) ->
                    appendLine("User: $user")
                    appendLine("Assistant: ${assistant.take(200)}...")
                    appendLine()
                }
            }
            
            appendLine("CURRENT QUERY: $userQuery")
            appendLine()
            appendLine("Please provide a helpful response following all the guidelines.")
            appendLine()
            appendLine("IMPORTANT: After your main response, you must provide exactly 3 relevant follow-up questions that the farmer might want to ask next.")
            appendLine("The follow-up questions MUST:")
            appendLine("1. Be in ${language?.englishName ?: "English"}")
            appendLine("2. Be SHORT and CONCISE (maximum 40 characters each)")
            appendLine("3. Be practical and actionable")
            appendLine("4. Be IMMEDIATELY relevant to the current topic (not future stages)")
            appendLine("5. Focus on the NEXT logical step or current concern")
            appendLine("6. Consider the farming stage/timeline appropriately")
            appendLine()
            appendLine("Examples of good follow-up questions:")
            appendLine("- If discussing seed varieties → ask about seed treatment, planting depth, spacing")
            appendLine("- If discussing pests → ask about identification, organic controls, prevention")
            appendLine("- If discussing fertilizer → ask about application rates, timing, mixing")
            appendLine()
            appendLine("AVOID questions about:")
            appendLine("- Future stages (e.g., harvest/storage when discussing planting)")
            appendLine("- Unrelated topics")
            appendLine("- Generic questions")
            appendLine()
            appendLine("Format the follow-up questions section EXACTLY like this:")
            appendLine("---")
            appendLine("**${getFollowUpQuestionHeader(language?.code ?: "en")}**")
            appendLine("• [Short question 1, max 40 chars]")
            appendLine("• [Short question 2, max 40 chars]")
            appendLine("• [Short question 3, max 40 chars]")
        }
    }
    
    fun extractFollowUpQuestions(response: String): List<String> {
        // Look for the follow-up questions section - check for any language header
        val lines = response.lines()
        val startIndex = lines.indexOfFirst { line ->
            line.contains("What would you like to know more about?") ||
            line.contains("आप किस बारे में और जानना चाहेंगे?") ||
            line.contains("Ungependa kujua zaidi kuhusu nini?") ||
            line.contains("আপনি কী সম্পর্কে আরও জানতে চান?") ||
            line.contains("మీరు దేని గురించి మరింత తెలుసుకోవాలనుకుంటున్నారు?") ||
            line.contains("तुम्हाला कशाबद्दल अधिक जाणून घ्यायचे आहे?") ||
            line.contains("நீங்கள் எதைப் பற்றி மேலும் அறிய விரும்புகிறீர்கள்?") ||
            line.contains("તમે શું વિશે વધુ જાણવા માંગો છો?") ||
            line.contains("ನೀವು ಯಾವುದರ ಬಗ್ಗೆ ಹೆಚ್ಚು ತಿಳಿದುಕೊಳ್ಳಲು ಬಯಸುತ್ತೀರಿ?")
        }
        
        if (startIndex == -1) return emptyList()
        
        val questions = mutableListOf<String>()
        for (i in (startIndex + 1) until lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("•") || line.startsWith("-") || line.startsWith("*")) {
                val question = line.removePrefix("•").removePrefix("-").removePrefix("*").trim()
                if (question.isNotEmpty() && questions.size < 3) {
                    questions.add(question)
                }
            }
        }
        
        return questions
    }
    
    fun generateTitlePrompt(firstUserQuery: String, firstAiResponse: String): String {
        return buildString {
            appendLine("Based on this farming conversation, generate a concise title (2-4 words) that captures the main topic.")
            appendLine()
            appendLine("User Query: $firstUserQuery")
            appendLine("AI Response (excerpt): ${firstAiResponse.take(200)}...")
            appendLine()
            appendLine("Generate ONLY the title, nothing else. Examples:")
            appendLine("• Rice Pest Control")
            appendLine("• Organic Fertilizer Guide")
            appendLine("• Tomato Disease Management")
            appendLine("• Irrigation Schedule Help")
            appendLine("• Wheat Harvest Timing")
            appendLine("• Soil pH Testing")
            appendLine("• Dairy Cow Nutrition")
            appendLine()
            appendLine("Title:")
        }
    }
    
    fun generateStarterQuestionPrompt(userProfile: UserProfile?): String {
        val language = userProfile?.language?.let { LanguageManager.getLanguageByCode(it) }
        val currentMonth = java.time.LocalDate.now().month.name
        
        return buildString {
            appendLine("Generate relevant agricultural questions for a farmer with this profile:")
            
            if (!userProfile?.crops.isNullOrEmpty()) {
                // Get localized crop names
                val localizedCrops = userProfile.crops.mapNotNull { cropId ->
                    CropsManager.getCropById(cropId)?.getLocalizedName(userProfile.language)
                }
                appendLine("Crops grown: ${localizedCrops.joinToString(", ")}")
                appendLine("Crop IDs: ${userProfile.crops.joinToString(", ")}")
            }
            
            if (!userProfile?.livestock.isNullOrEmpty()) {
                // Get localized livestock names
                val localizedLivestock = userProfile.livestock.mapNotNull { livestockId ->
                    LivestockManager.getLivestockById(livestockId)?.getLocalizedName(userProfile.language)
                }
                appendLine("Livestock raised: ${localizedLivestock.joinToString(", ")}")
            }
            
            userProfile?.locationInfo?.let { location ->
                appendLine("Location: ${location.country}, ${location.regionLevel1}")
                appendLine("Climate zone: ${getClimateZone(location)}")
            }
            
            appendLine("Current month: $currentMonth")
            
            appendLine()
            appendLine("IMPORTANT REQUIREMENTS:")
            appendLine("1. Generate questions in ${language?.englishName ?: "English"} language ONLY")
            appendLine("2. Each question must be SHORT and CONCISE (maximum 60 characters)")
            appendLine("3. Questions MUST be specific to the farmer's crops/livestock listed above")
            appendLine("4. Questions should be seasonally relevant for $currentMonth")
            appendLine("5. Use crop/livestock names in ${language?.englishName ?: "English"}")
            appendLine()
            appendLine("Generate 5 practical questions this farmer might have. Examples:")
            if (!userProfile?.crops.isNullOrEmpty()) {
                val firstCrop = userProfile.crops.firstOrNull()?.let { 
                    CropsManager.getCropById(it)?.getLocalizedName(userProfile.language) 
                }
                if (firstCrop != null) {
                    when (language?.code) {
                        "hi" -> {
                            appendLine("- $firstCrop में कीट नियंत्रण कैसे करें?")
                            appendLine("- $firstCrop के लिए सबसे अच्छी खाद कौन सी है?")
                        }
                        "sw" -> {
                            appendLine("- Jinsi ya kudhibiti wadudu katika $firstCrop?")
                            appendLine("- Mbolea bora zaidi kwa $firstCrop ni ipi?")
                        }
                        else -> {
                            appendLine("- How to control pests in $firstCrop?")
                            appendLine("- Best fertilizer for $firstCrop?")
                        }
                    }
                }
            }
            appendLine()
            appendLine("Format: Return only the questions in ${language?.englishName ?: "English"}, one per line, no numbering or bullets.")
        }
    }
    
    private fun getClimateZone(location: LocationInfo): String {
        // Simplified climate zone determination based on country/region
        return when {
            location.latitude in -23.5..23.5 -> "Tropical"
            location.latitude in -35.0..-23.5 || location.latitude in 23.5..35.0 -> "Subtropical"
            location.latitude in -45.0..-35.0 || location.latitude in 35.0..45.0 -> "Temperate"
            else -> "Continental/Polar"
        }
    }
    
    private fun getLocationContext(locationInfo: LocationInfo): Map<String, String> {
        val context = mutableMapOf<String, String>()
        
        if (locationInfo.country.isNotBlank()) {
            context["Country"] = locationInfo.country
        }
        
        if (locationInfo.regionLevel1.isNotBlank()) {
            context["State/Province"] = locationInfo.regionLevel1
        }
        
        if (locationInfo.regionLevel2.isNotBlank()) {
            context["District/County"] = locationInfo.regionLevel2
        }
        
        if (locationInfo.city.isNotBlank()) {
            context["City/Town"] = locationInfo.city
        }
        
        // Add climate zone
        val climateZone = getClimateZone(locationInfo)
        context["Climate Zone"] = climateZone
        
        return context
    }
    
    fun cleanResponse(response: String): String {
        // Remove any follow-up questions section for display
        val lines = response.lines()
        val separatorIndex = lines.indexOfLast { it.trim() == "---" }
        
        return if (separatorIndex > 0) {
            lines.take(separatorIndex).joinToString("\n").trim()
        } else {
            response.trim()
        }
    }
    
    fun generateDynamicFollowUpQuestionsPrompt(
        conversationHistory: List<ChatMessage>,
        userProfile: UserProfile?
    ): String {
        val language = userProfile?.language?.let { LanguageManager.getLanguageByCode(it) }
        val lastMessages = conversationHistory.takeLast(4) // Last 2 exchanges
        
        return buildString {
            appendLine("Based on this farming conversation, generate 3 immediate follow-up questions.")
            appendLine()
            
            // Include recent conversation context
            appendLine("RECENT CONVERSATION:")
            lastMessages.forEach { msg ->
                val role = if (msg.isUser) "Farmer" else "Assistant"
                appendLine("$role: ${msg.content.take(200)}")
            }
            appendLine()
            
            // User context
            if (!userProfile?.crops.isNullOrEmpty()) {
                val localizedCrops = userProfile.crops.mapNotNull { cropId ->
                    CropsManager.getCropById(cropId)?.getLocalizedName(userProfile.language)
                }
                appendLine("Farmer's crops: ${localizedCrops.joinToString(", ")}")
            }
            
            if (!userProfile?.livestock.isNullOrEmpty()) {
                val localizedLivestock = userProfile.livestock.mapNotNull { livestockId ->
                    LivestockManager.getLivestockById(livestockId)?.getLocalizedName(userProfile.language)
                }
                appendLine("Farmer's livestock: ${localizedLivestock.joinToString(", ")}")
            }
            
            appendLine()
            appendLine("Generate 3 follow-up questions that:")
            appendLine("1. Are in ${language?.englishName ?: "English"} language")
            appendLine("2. Are SHORT (maximum 40 characters)")
            appendLine("3. Directly relate to the CURRENT topic being discussed")
            appendLine("4. Focus on immediate next steps or clarifications")
            appendLine("5. Are practical and actionable NOW")
            appendLine()
            appendLine("DO NOT generate questions about:")
            appendLine("- Future farming stages")
            appendLine("- Unrelated topics")
            appendLine("- General farming advice")
            appendLine()
            appendLine("Format: Return only the 3 questions in ${language?.englishName ?: "English"}, one per line.")
        }
    }
}