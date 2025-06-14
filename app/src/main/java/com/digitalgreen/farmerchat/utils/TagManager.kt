package com.digitalgreen.farmerchat.utils

import com.digitalgreen.farmerchat.data.CropsManager
import com.digitalgreen.farmerchat.data.LivestockManager

/**
 * Manages conversation tags and automatic tag generation
 */
object TagManager {
    
    // Predefined tag categories
    private val cropTags = CropsManager.getAllCrops().map { it.defaultName.lowercase() }
    private val livestockTags = LivestockManager.getAllLivestock().map { it.defaultName.lowercase() }
    
    private val problemTags = listOf(
        "pest control", "disease", "irrigation", "fertilizer", "soil health",
        "weather", "harvest", "planting", "seeds", "organic farming",
        "market prices", "storage", "equipment", "finance", "insurance"
    )
    
    private val actionTags = listOf(
        "urgent", "seasonal", "planning", "maintenance", "prevention",
        "treatment", "monitoring", "improvement", "investment"
    )
    
    /**
     * Generate tags based on conversation content
     */
    fun generateTags(conversationTitle: String, messages: List<String>): List<String> {
        val tags = mutableSetOf<String>()
        val allContent = (conversationTitle + " " + messages.joinToString(" ")).lowercase()
        
        // Check for crop mentions
        cropTags.forEach { crop ->
            if (allContent.contains(crop)) {
                tags.add(crop.replaceFirstChar { it.uppercase() })
            }
        }
        
        // Check for livestock mentions
        livestockTags.forEach { animal ->
            if (allContent.contains(animal)) {
                tags.add(animal.replaceFirstChar { it.uppercase() })
            }
        }
        
        // Check for problem/topic tags
        problemTags.forEach { problem ->
            if (allContent.contains(problem)) {
                tags.add(problem.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } })
            }
        }
        
        // Check for action tags
        actionTags.forEach { action ->
            if (allContent.contains(action)) {
                tags.add(action.replaceFirstChar { it.uppercase() })
            }
        }
        
        // Limit to 5 most relevant tags
        return tags.take(5).toList()
    }
    
    /**
     * Get all available tags for filtering
     */
    fun getAllAvailableTags(): List<String> {
        val allTags = mutableSetOf<String>()
        
        // Add main crop categories
        CropsManager.getCategories().forEach { category ->
            allTags.add(category.displayName)
        }
        
        // Add main livestock categories
        LivestockManager.getCategories().forEach { category ->
            allTags.add(category.displayName)
        }
        
        // Add problem tags
        problemTags.forEach { tag ->
            allTags.add(tag.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } })
        }
        
        return allTags.sorted()
    }
    
    /**
     * Get suggested tags based on user profile
     */
    fun getSuggestedTags(userCrops: List<String>, userLivestock: List<String>): List<String> {
        val tags = mutableListOf<String>()
        
        // Add user's crops
        userCrops.forEach { cropId ->
            CropsManager.getCropById(cropId)?.let { crop ->
                tags.add(crop.defaultName)
            }
        }
        
        // Add user's livestock
        userLivestock.forEach { animalId ->
            LivestockManager.getLivestockById(animalId)?.let { animal ->
                tags.add(animal.defaultName)
            }
        }
        
        // Add common problem tags
        tags.addAll(listOf("Pest Control", "Disease", "Weather", "Market Prices"))
        
        return tags.distinct()
    }
}