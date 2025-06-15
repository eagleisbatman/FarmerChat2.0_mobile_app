package com.digitalgreen.farmerchat.utils

import android.content.Context
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides localized strings to non-Composable classes like ViewModels.
 * This allows ViewModels to access translated strings without being Composable.
 */
class StringProvider(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    
    /**
     * Get a localized string for the given key using the current language preference
     */
    fun getString(key: StringKey): String {
        val currentLanguage = preferencesManager.getSelectedLanguage()
        return StringsManager.getString(key, currentLanguage)
    }
    
    /**
     * Get a localized string with format arguments
     */
    fun getString(key: StringKey, vararg args: Any): String {
        val currentLanguage = preferencesManager.getSelectedLanguage()
        val template = StringsManager.getString(key, currentLanguage)
        return try {
            String.format(template, *args)
        } catch (e: Exception) {
            // If formatting fails, return the template
            template
        }
    }
    
    /**
     * Get a localized string for a specific language
     */
    fun getString(key: StringKey, languageCode: String): String {
        return StringsManager.getString(key, languageCode)
    }
    
    companion object {
        /**
         * Factory method to create StringProvider
         */
        fun create(context: Context): StringProvider {
            return StringProvider(
                context.applicationContext,
                PreferencesManager(context.applicationContext)
            )
        }
    }
}