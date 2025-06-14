package com.digitalgreen.farmerchat.utils

import com.digitalgreen.farmerchat.data.StarterQuestion
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// This is a utility to seed starter questions in Firestore
// You can run this once to populate your database with initial questions
object StarterQuestionSeeder {
    
    suspend fun seedStarterQuestions() {
        val firestore = FirebaseFirestore.getInstance()
        
        val starterQuestions = listOf(
            // English - General
            StarterQuestion(
                id = "en_gen_1",
                question = "What are the best practices for soil preparation?",
                category = "general",
                language = "en",
                tags = listOf("soil", "preparation")
            ),
            StarterQuestion(
                id = "en_gen_2",
                question = "How can I improve water management on my farm?",
                category = "general",
                language = "en",
                tags = listOf("water", "irrigation")
            ),
            StarterQuestion(
                id = "en_gen_3",
                question = "What are organic farming methods I can adopt?",
                category = "general",
                language = "en",
                tags = listOf("organic", "sustainable")
            ),
            
            // English - Crops
            StarterQuestion(
                id = "en_crop_wheat_1",
                question = "When is the best time to plant wheat in my region?",
                category = "crops",
                language = "en",
                tags = listOf("wheat", "planting")
            ),
            StarterQuestion(
                id = "en_crop_rice_1",
                question = "How can I control pests in my rice field naturally?",
                category = "crops",
                language = "en",
                tags = listOf("rice", "pest-control")
            ),
            StarterQuestion(
                id = "en_crop_tomato_1",
                question = "What are common diseases in tomato plants and their remedies?",
                category = "crops",
                language = "en",
                tags = listOf("tomato", "diseases")
            ),
            
            // English - Livestock
            StarterQuestion(
                id = "en_livestock_cow_1",
                question = "What is the ideal diet for dairy cows?",
                category = "livestock",
                language = "en",
                tags = listOf("cow", "dairy", "nutrition")
            ),
            StarterQuestion(
                id = "en_livestock_chicken_1",
                question = "How can I improve egg production in my poultry farm?",
                category = "livestock",
                language = "en",
                tags = listOf("chicken", "eggs", "poultry")
            ),
            StarterQuestion(
                id = "en_livestock_goat_1",
                question = "What vaccinations do goats need?",
                category = "livestock",
                language = "en",
                tags = listOf("goat", "health", "vaccination")
            ),
            
            // Hindi - General
            StarterQuestion(
                id = "hi_gen_1",
                question = "मिट्टी की तैयारी के लिए सबसे अच्छे तरीके क्या हैं?",
                category = "general",
                language = "hi",
                tags = listOf("soil", "preparation")
            ),
            StarterQuestion(
                id = "hi_gen_2",
                question = "मैं अपने खेत में पानी प्रबंधन कैसे सुधार सकता हूं?",
                category = "general",
                language = "hi",
                tags = listOf("water", "irrigation")
            ),
            
            // Hindi - Crops
            StarterQuestion(
                id = "hi_crop_wheat_1",
                question = "मेरे क्षेत्र में गेहूं बोने का सबसे अच्छा समय कब है?",
                category = "crops",
                language = "hi",
                tags = listOf("wheat", "planting")
            ),
            StarterQuestion(
                id = "hi_crop_rice_1",
                question = "मैं अपने धान के खेत में कीटों को प्राकृतिक रूप से कैसे नियंत्रित कर सकता हूं?",
                category = "crops",
                language = "hi",
                tags = listOf("rice", "pest-control")
            )
        )
        
        // Add questions to Firestore
        starterQuestions.forEach { question ->
            firestore.collection("starter_questions")
                .document(question.id)
                .set(question)
                .await()
        }
    }
}