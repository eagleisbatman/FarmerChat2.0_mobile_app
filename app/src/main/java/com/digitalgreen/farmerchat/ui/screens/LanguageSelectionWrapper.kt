package com.digitalgreen.farmerchat.ui.screens

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.digitalgreen.farmerchat.ui.screens.LanguageSelectionScreen
import com.digitalgreen.farmerchat.data.LanguageManager
import com.digitalgreen.farmerchat.utils.PreferencesManager
import kotlinx.coroutines.launch

@Composable
fun LanguageSelectionWrapper(
    navController: NavController,
    fromOnboarding: Boolean = false,
    onboardingViewModel: UnifiedOnboardingViewModel? = null
) {
    // Track the initial selection when coming from onboarding
    val initialLanguage = remember { 
        if (fromOnboarding && onboardingViewModel != null) {
            onboardingViewModel.uiState.value.selectedLanguage ?: "en"
        } else {
            null
        }
    }
    
    // Get suggested languages based on location
    val suggestedLanguages = remember {
        if (fromOnboarding && onboardingViewModel != null) {
            onboardingViewModel.getSuggestedLanguages()
        } else {
            emptyList()
        }
    }
    
    val coroutineScope = rememberCoroutineScope()
    val preferencesManager = remember { PreferencesManager(navController.context) }
    
    // Create a temporary view model that uses the onboarding state
    val temporarySettingsViewModel: ApiSettingsViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            navController.context.applicationContext as android.app.Application
        )
    )
    
    // Update the settings view model with the current language from onboarding
    LaunchedEffect(Unit) {
        if (fromOnboarding && onboardingViewModel != null) {
            val currentLanguage = onboardingViewModel.uiState.value.selectedLanguage
            if (currentLanguage != null) {
                temporarySettingsViewModel.updateLanguage(currentLanguage)
            }
        }
    }
    
    LanguageSelectionScreen(
        navController = navController,
        settingsViewModel = temporarySettingsViewModel,
        onLanguageSelected = { languageCode ->
            if (fromOnboarding && onboardingViewModel != null) {
                // Update the unified view model with selection immediately
                onboardingViewModel.selectLanguage(languageCode)
                
                // Update UI language immediately via preferences
                coroutineScope.launch {
                    preferencesManager.saveSelectedLanguage(languageCode)
                    // The UI will automatically update with the new language
                    // when screens recompose and read the preference
                }
            }
            // Small delay to show selection before navigating back
            coroutineScope.launch {
                kotlinx.coroutines.delay(100)
                navController.popBackStack()
            }
        },
        suggestedLanguages = suggestedLanguages
    )
}