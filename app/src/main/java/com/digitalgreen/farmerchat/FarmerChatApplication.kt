package com.digitalgreen.farmerchat

import android.app.Application
import com.digitalgreen.farmerchat.data.AppRepository
import com.digitalgreen.farmerchat.data.LanguageManager
import com.digitalgreen.farmerchat.network.NetworkConfig
import com.digitalgreen.farmerchat.utils.TranslationManager
import com.google.firebase.FirebaseApp

class FarmerChatApplication : Application() {
    
    // Main repository using API backend
    val repository by lazy { AppRepository(this) }
    
    // Translation manager for dynamic translations
    val translationManager by lazy { TranslationManager.getInstance(this, repository) }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase (for Auth only)
        FirebaseApp.initializeApp(this)
        
        // Initialize Network configuration
        NetworkConfig.initialize(this)
        
        // Load translations for current language
        val preferencesManager = com.digitalgreen.farmerchat.utils.PreferencesManager(this)
        val currentLanguage = preferencesManager.getSelectedLanguage()
        translationManager.loadTranslations(currentLanguage)
        
        // Preload common languages in background
        translationManager.preloadCommonLanguages()
    }
}

// Extension function to get the application instance
fun Application.asFarmerChatApplication(): FarmerChatApplication {
    return this as FarmerChatApplication
}