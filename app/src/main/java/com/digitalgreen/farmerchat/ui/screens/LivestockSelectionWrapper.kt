package com.digitalgreen.farmerchat.ui.screens

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.digitalgreen.farmerchat.FarmerChatApplication
import androidx.compose.ui.platform.LocalContext

@Composable
fun LivestockSelectionWrapper(
    navController: NavController,
    fromOnboarding: Boolean = false,
    onboardingViewModel: UnifiedOnboardingViewModel? = null
) {
    val context = LocalContext.current
    val settingsViewModel: ApiSettingsViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as android.app.Application
        )
    )
    
    val settingsState by settingsViewModel.settingsState.collectAsState()
    
    // Track the initial selection when coming from onboarding
    val initialSelection = remember { 
        if (fromOnboarding && onboardingViewModel != null) {
            onboardingViewModel.uiState.value.selectedLivestock
        } else {
            settingsState.selectedLivestock.toSet()
        }
    }
    
    // Watch for changes in the settings view model and update onboarding view model immediately
    LaunchedEffect(settingsState.selectedLivestock) {
        if (fromOnboarding && onboardingViewModel != null) {
            // Update immediately when selection changes
            onboardingViewModel.updateSelectedLivestock(settingsState.selectedLivestock.toSet())
        }
    }
    
    // Also watch for any changes to force recomposition
    val currentCount = settingsState.selectedLivestock.size
    
    LivestockSelectionScreen(
        onNavigateBack = {
            navController.popBackStack()
        },
        viewModel = settingsViewModel
    )
    
    // If from onboarding, set initial selection
    LaunchedEffect(Unit) {
        if (fromOnboarding && onboardingViewModel != null) {
            settingsViewModel.updateSelectedLivestock(initialSelection.toList())
        }
    }
}