package com.digitalgreen.farmerchat.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digitalgreen.farmerchat.data.FarmerChatRepository
import com.digitalgreen.farmerchat.data.OnboardingState
import com.digitalgreen.farmerchat.data.UserProfile
import com.digitalgreen.farmerchat.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesManager = PreferencesManager(application)
    private val repository = FarmerChatRepository()
    
    private val _onboardingState = MutableStateFlow(OnboardingState())
    val onboardingState: StateFlow<OnboardingState> = _onboardingState
    
    fun selectLanguage(language: String) {
        _onboardingState.update { it.copy(selectedLanguage = language) }
    }
    
    fun selectLocation(location: String) {
        _onboardingState.update { it.copy(selectedLocation = location) }
    }
    
    fun toggleCrop(cropId: String) {
        _onboardingState.update { state ->
            val updatedCrops = if (cropId in state.selectedCrops) {
                state.selectedCrops - cropId
            } else {
                state.selectedCrops + cropId
            }
            state.copy(selectedCrops = updatedCrops)
        }
    }
    
    fun toggleLivestock(livestockId: String) {
        _onboardingState.update { state ->
            val updatedLivestock = if (livestockId in state.selectedLivestock) {
                state.selectedLivestock - livestockId
            } else {
                state.selectedLivestock + livestockId
            }
            state.copy(selectedLivestock = updatedLivestock)
        }
    }
    
    fun nextStep() {
        _onboardingState.update { it.copy(currentStep = it.currentStep + 1) }
    }
    
    fun previousStep() {
        _onboardingState.update { it.copy(currentStep = it.currentStep - 1) }
    }
    
    fun completeOnboarding() {
        viewModelScope.launch {
            val state = _onboardingState.value
            
            // Save preferences locally
            preferencesManager.saveUserPreferences(
                language = state.selectedLanguage,
                location = state.selectedLocation,
                crops = state.selectedCrops.toSet(),
                livestock = state.selectedLivestock.toSet()
            )
            
            preferencesManager.setOnboardingCompleted(true)
            
            // Save user profile to Firestore
            val userProfile = UserProfile(
                language = state.selectedLanguage,
                location = state.selectedLocation,
                crops = state.selectedCrops,
                livestock = state.selectedLivestock,
                hasCompletedOnboarding = true
            )
            
            repository.saveUserProfile(userProfile)
        }
    }
}