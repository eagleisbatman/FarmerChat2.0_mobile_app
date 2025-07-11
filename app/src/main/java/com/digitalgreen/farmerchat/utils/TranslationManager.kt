package com.digitalgreen.farmerchat.utils

import android.content.Context
import com.digitalgreen.farmerchat.data.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Manages dynamic translation loading from the API
 * Falls back to hardcoded translations if API fails
 */
class TranslationManager(
    private val context: Context,
    private val repository: AppRepository
) {
    private val _translationsLoaded = MutableStateFlow(false)
    val translationsLoaded: StateFlow<Boolean> = _translationsLoaded
    
    private val _currentTranslations = MutableStateFlow<Map<String, Map<String, String>>>(emptyMap())
    val currentTranslations: StateFlow<Map<String, Map<String, String>>> = _currentTranslations
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    init {
        // Load cached translations from SharedPreferences on init
        loadCachedTranslations()
    }
    
    /**
     * Load translations for a specific language from API
     */
    fun loadTranslations(languageCode: String) {
        coroutineScope.launch {
            try {
                android.util.Log.d("TranslationManager", "Starting to load translations for language: $languageCode")
                android.util.Log.d("TranslationManager", "Making API call to getTranslations...")
                
                repository.getTranslations(languageCode).fold(
                    onSuccess = { bundle ->
                        android.util.Log.d("TranslationManager", "API call successful for $languageCode")
                        // Convert StringKey enum names to match the API keys
                        val convertedTranslations = mutableMapOf<String, String>()
                        
                        // Check if ui translations exist
                        if (bundle.ui != null) {
                            android.util.Log.d("TranslationManager", "Found ${bundle.ui.size} UI translations for $languageCode")
                            bundle.ui.forEach { (apiKey, value) ->
                                // API keys are like "APP_NAME", StringKey enum is also "APP_NAME"
                                convertedTranslations[apiKey] = value
                            }
                            // Log some samples
                            android.util.Log.d("TranslationManager", "Sample translations: CONTINUE=${bundle.ui["CONTINUE"]}, NEW_CONVERSATION=${bundle.ui["NEW_CONVERSATION"]}")
                        } else {
                            android.util.Log.w("TranslationManager", "No UI translations found for $languageCode")
                        }
                        
                        // Update the translations map
                        val updatedMap = _currentTranslations.value.toMutableMap()
                        updatedMap[languageCode] = convertedTranslations
                        _currentTranslations.value = updatedMap
                        
                        android.util.Log.d("TranslationManager", "Successfully updated translations map for $languageCode with ${convertedTranslations.size} translations")
                        
                        // Cache to SharedPreferences
                        if (convertedTranslations.isNotEmpty()) {
                            cacheTranslations(languageCode, convertedTranslations)
                        }
                        
                        _translationsLoaded.value = true
                    },
                    onFailure = { error ->
                        android.util.Log.e("TranslationManager", "Failed to load translations for $languageCode: ${error.message}", error)
                        android.util.Log.e("TranslationManager", "Stack trace:", error)
                        // Fall back to hardcoded translations
                        _translationsLoaded.value = true
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("TranslationManager", "Unexpected error loading translations: ${e.message}", e)
                _translationsLoaded.value = true
            }
        }
    }
    
    /**
     * Get a translated string for a specific key and language
     */
    fun getString(key: String, languageCode: String): String? {
        val translation = _currentTranslations.value[languageCode]?.get(key)
        android.util.Log.d("TranslationManager", "getString(key=$key, lang=$languageCode) = $translation")
        return translation
    }
    
    /**
     * Preload translations for common languages
     */
    fun preloadCommonLanguages() {
        val commonLanguages = listOf("en", "hi", "sw", "es", "fr", "ar", "zh")
        commonLanguages.forEach { language ->
            if (!_currentTranslations.value.containsKey(language)) {
                loadTranslations(language)
            }
        }
    }
    
    /**
     * Cache translations to SharedPreferences
     */
    private fun cacheTranslations(languageCode: String, translations: Map<String, String>) {
        val prefs = context.getSharedPreferences("translations_cache", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        
        // Store as JSON string
        val json = translations.entries.joinToString(",", "{", "}") { (key, value) ->
            "\"$key\":\"${value.replace("\"", "\\\"")}\""
        }
        
        editor.putString("translations_$languageCode", json)
        editor.putLong("translations_${languageCode}_timestamp", System.currentTimeMillis())
        editor.apply()
    }
    
    /**
     * Load cached translations from SharedPreferences
     */
    private fun loadCachedTranslations() {
        val prefs = context.getSharedPreferences("translations_cache", Context.MODE_PRIVATE)
        val languages = listOf("en", "hi", "sw", "es", "fr", "ar", "zh")
        
        val cachedMap = mutableMapOf<String, Map<String, String>>()
        
        languages.forEach { language ->
            val json = prefs.getString("translations_$language", null)
            val timestamp = prefs.getLong("translations_${language}_timestamp", 0)
            
            // Use cache if less than 24 hours old
            if (json != null && (System.currentTimeMillis() - timestamp) < 24 * 60 * 60 * 1000) {
                try {
                    // Simple JSON parsing - handle empty or invalid JSON
                    val translations = mutableMapOf<String, String>()
                    if (json.length > 2) {  // Check if JSON has content beyond {}
                        json.removeSurrounding("{", "}")
                            .split("\",\"")  // Split by "," pattern to handle commas in values
                            .forEach { pair ->
                                val parts = pair.split("\":\"", limit = 2)
                                if (parts.size == 2) {
                                    translations[parts[0].removePrefix("\"")] = parts[1].removeSuffix("\"")
                                        .replace("\\\"", "\"")
                                }
                            }
                    }
                    cachedMap[language] = translations
                } catch (e: Exception) {
                    android.util.Log.e("TranslationManager", "Failed to parse cached translations for $language", e)
                }
            }
        }
        
        if (cachedMap.isNotEmpty()) {
            _currentTranslations.value = cachedMap
            _translationsLoaded.value = true
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: TranslationManager? = null
        
        fun getInstance(context: Context, repository: AppRepository): TranslationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TranslationManager(context, repository).also { INSTANCE = it }
            }
        }
    }
}