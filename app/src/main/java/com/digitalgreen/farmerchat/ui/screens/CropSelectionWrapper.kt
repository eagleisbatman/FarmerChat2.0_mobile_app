package com.digitalgreen.farmerchat.ui.screens

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.digitalgreen.farmerchat.FarmerChatApplication
import androidx.compose.ui.platform.LocalContext

@Composable
fun CropSelectionWrapper(
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
            onboardingViewModel.uiState.value.selectedCrops
        } else {
            settingsState.selectedCrops.toSet()
        }
    }
    
    // Watch for changes in the settings view model and update onboarding view model immediately
    LaunchedEffect(settingsState.selectedCrops) {
        if (fromOnboarding && onboardingViewModel != null) {
            // Update immediately when selection changes
            onboardingViewModel.updateSelectedCrops(settingsState.selectedCrops.toSet())
        }
    }
    
    // Also watch for any changes to force recomposition
    val currentCount = settingsState.selectedCrops.size
    
    CropSelectionScreen(
        onNavigateBack = {
            navController.popBackStack()
        },
        viewModel = settingsViewModel
    )
    
    // If from onboarding, set initial selection
    LaunchedEffect(Unit) {
        if (fromOnboarding && onboardingViewModel != null) {
            settingsViewModel.updateSelectedCrops(initialSelection.toList())
        }
    }
}