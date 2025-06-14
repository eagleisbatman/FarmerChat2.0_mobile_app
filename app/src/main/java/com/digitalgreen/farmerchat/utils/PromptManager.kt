package com.digitalgreen.farmerchat.utils

import com.digitalgreen.farmerchat.data.LocationInfo
import com.digitalgreen.farmerchat.data.UserProfile
import com.digitalgreen.farmerchat.data.LanguageManager

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
            appendLine("The follow-up questions MUST also be in ${language?.englishName ?: "English"}.")
            appendLine()
            appendLine("Format the follow-up questions section EXACTLY like this:")
            appendLine("---")
            appendLine("**${getFollowUpQuestionHeader(language?.code ?: "en")}**")
            appendLine("• [Question 1 in ${language?.englishName ?: "English"}]")
            appendLine("• [Question 2 in ${language?.englishName ?: "English"}]")
            appendLine("• [Question 3 in ${language?.englishName ?: "English"}]")
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
        return buildString {
            appendLine("Generate relevant agricultural questions for a farmer with this profile:")
            
            if (!userProfile?.crops.isNullOrEmpty()) {
                appendLine("Crops: ${userProfile.crops.joinToString(", ")}")
            }
            
            if (!userProfile?.livestock.isNullOrEmpty()) {
                appendLine("Livestock: ${userProfile.livestock.joinToString(", ")}")
            }
            
            userProfile?.locationInfo?.let { location ->
                appendLine("Location: ${location.country}, ${location.regionLevel1}")
                appendLine("Climate zone: ${getClimateZone(location)}")
            }
            
            appendLine()
            appendLine("Generate 5 practical questions this farmer might have based on:")
            appendLine("1. Current season and timing")
            appendLine("2. Common challenges in their region")
            appendLine("3. Their specific crops/livestock")
            appendLine("4. Sustainable farming practices")
            appendLine()
            appendLine("Format: Return only the questions, one per line, no numbering or bullets.")
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
}