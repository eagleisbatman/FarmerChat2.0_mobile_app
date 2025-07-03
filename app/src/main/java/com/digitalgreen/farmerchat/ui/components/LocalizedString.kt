package com.digitalgreen.farmerchat.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.digitalgreen.farmerchat.FarmerChatApplication
import com.digitalgreen.farmerchat.utils.PreferencesManager
import com.digitalgreen.farmerchat.utils.StringsManager
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey

/**
 * Composable helper to get localized strings based on user's language preference
 */
@Composable
fun localizedString(key: StringKey, vararg args: Any): String {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val language by preferencesManager.userLanguage.collectAsState(initial = "en")
    
    // Also observe translation loading state to trigger recomposition when translations are loaded
    val application = context.applicationContext as? FarmerChatApplication
    val translationsLoaded by application?.translationManager?.translationsLoaded?.collectAsState() ?: remember { 
        androidx.compose.runtime.mutableStateOf(false) 
    }
    
    return if (args.isEmpty()) {
        StringsManager.getString(key, language)
    } else {
        StringsManager.getString(key, language, *args)
    }
}

/**
 * Get the current user's language preference
 */
@Composable
fun currentLanguage(): String {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val language by preferencesManager.userLanguage.collectAsState(initial = "en")
    return language
}