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
    
    private val _isOnboardingComplete = MutableStateFlow(false)
    val isOnboardingComplete: StateFlow<Boolean> = _isOnboardingComplete
    
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
    
    fun updatePhoneNumber(phoneNumber: String) {
        _onboardingState.update { it.copy(phoneNumber = phoneNumber) }
    }
    
    fun updatePin(pin: String) {
        _onboardingState.update { it.copy(pin = pin) }
    }
    
    fun updateConfirmPin(confirmPin: String) {
        _onboardingState.update { it.copy(confirmPin = confirmPin) }
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
            
            // IMPORTANT: Ensure we're authenticated before updating profile
            // Check if we already have a valid token from SplashViewModel
            val existingToken = com.digitalgreen.farmerchat.network.NetworkConfig.getAuthToken()
            
            if (existingToken == null) {
                android.util.Log.d("OnboardingViewModel", "No existing token, authenticating with backend...")
                val authResult = repository.authenticateWithFirebase()
                authResult.fold(
                    onSuccess = { authResponse ->
                        android.util.Log.d("OnboardingViewModel", "Authentication successful")
                    },
                    onFailure = { error ->
                        android.util.Log.e("OnboardingViewModel", "Failed to authenticate", error)
                        _isOnboardingComplete.value = true // Still complete onboarding
                        return@launch
                    }
                )
                
                // Wait a moment to ensure token is properly saved
                kotlinx.coroutines.delay(100)
            } else {
                android.util.Log.d("OnboardingViewModel", "Using existing token: ${existingToken.take(20)}...")
            }
            
            // Now update user profile via API
            android.util.Log.d("OnboardingViewModel", "Updating profile with data: name=${state.name}, crops=${state.selectedCrops}, livestock=${state.selectedLivestock}")
            
            val updateResult = repository.updateUserProfile(
                name = state.name.ifEmpty { "Farmer" }, // Use name from state, fallback to "Farmer" if empty
                language = state.selectedLanguage,
                location = state.selectedLocation,
                crops = state.selectedCrops.toList(),
                livestock = state.selectedLivestock.toList(),
                role = state.role.ifEmpty { null },
                gender = state.gender.ifEmpty { null },
                responseLength = "medium",
                phone = if (state.phoneNumber.isNotEmpty()) state.phoneNumber else null
            )
            
            updateResult.fold(
                onSuccess = { user ->
                    android.util.Log.d("OnboardingViewModel", "Profile updated successfully: ${user.name}, crops=${user.crops}, livestock=${user.livestock}")
                    _isOnboardingComplete.value = true
                },
                onFailure = { error ->
                    android.util.Log.e("OnboardingViewModel", "Failed to update profile", error)
                    _isOnboardingComplete.value = true // Still complete onboarding even if profile update fails
                }
            )
        }
    }
}