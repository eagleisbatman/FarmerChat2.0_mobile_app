package com.digitalgreen.farmerchat.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digitalgreen.farmerchat.FarmerChatApplication
import com.digitalgreen.farmerchat.data.OnboardingState
import com.digitalgreen.farmerchat.data.UserProfile
import com.digitalgreen.farmerchat.data.LocationInfo
import com.digitalgreen.farmerchat.network.UpdateUserRequest
import com.digitalgreen.farmerchat.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesManager = PreferencesManager(application)
    private val repository = (application as FarmerChatApplication).repository
    
    private val _onboardingState = MutableStateFlow(OnboardingState())
    val onboardingState: StateFlow<OnboardingState> = _onboardingState
    
    private var locationInfo: LocationInfo? = null
    
    fun selectLanguage(language: String) {
        _onboardingState.update { it.copy(selectedLanguage = language) }
        // Immediately save the language preference so UI updates right away
        viewModelScope.launch {
            preferencesManager.saveUserPreferences(
                language = language,
                location = _onboardingState.value.selectedLocation,
                crops = _onboardingState.value.selectedCrops.toSet(),
                livestock = _onboardingState.value.selectedLivestock.toSet()
            )
        }
    }
    
    fun selectLocation(location: String) {
        _onboardingState.update { it.copy(selectedLocation = location) }
    }
    
    fun setLocationInfo(info: LocationInfo) {
        locationInfo = info
        _onboardingState.update { it.copy(selectedLocation = info.formattedAddress) }
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
    
    fun updateName(name: String) {
        _onboardingState.update { it.copy(name = name) }
    }
    
    fun updateRole(role: String) {
        _onboardingState.update { it.copy(role = role) }
    }
    
    fun updateGender(gender: String) {
        _onboardingState.update { it.copy(gender = gender) }
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
            
            // Update user profile via API
            repository.updateUserProfile(
                name = state.name.ifEmpty { "Farmer" }, // Use name from state, fallback to "Farmer" if empty
                language = state.selectedLanguage,
                location = state.selectedLocation,
                crops = state.selectedCrops.toList(),
                livestock = state.selectedLivestock.toList(),
                role = state.role,
                gender = state.gender,
                responseLength = "medium"
            ).onFailure { error ->
                android.util.Log.e("OnboardingViewModel", "Failed to update profile", error)
            }
        }
    }
}