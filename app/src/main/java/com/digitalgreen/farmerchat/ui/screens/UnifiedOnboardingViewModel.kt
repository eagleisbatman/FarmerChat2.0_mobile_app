package com.digitalgreen.farmerchat.ui.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digitalgreen.farmerchat.FarmerChatApplication
import com.digitalgreen.farmerchat.data.LocationInfo
import com.digitalgreen.farmerchat.utils.LocationLanguageMapper
import com.digitalgreen.farmerchat.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UnifiedOnboardingState(
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // Profile fields
    val location: String = "",
    val locationInfo: LocationInfo? = null,
    val isLoadingLocation: Boolean = false,
    val selectedLanguage: String? = null,
    val name: String = "",
    val role: String = "",
    val gender: String = "",
    val selectedCrops: Set<String> = emptySet(),
    val selectedLivestock: Set<String> = emptySet(),
    
    // Navigation flags
    val navigateToCropSelection: Boolean = false,
    val navigateToLivestockSelection: Boolean = false,
    val navigateToLanguageSelection: Boolean = false,
    val isComplete: Boolean = false
)

class UnifiedOnboardingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as FarmerChatApplication).repository
    private val preferencesManager = PreferencesManager(application)
    
    private val _uiState = MutableStateFlow(UnifiedOnboardingState())
    val uiState: StateFlow<UnifiedOnboardingState> = _uiState.asStateFlow()
    
    private var hasLoadedData = false
    
    // Load existing user data for resume functionality
    fun loadExistingUserData() {
        if (hasLoadedData) {
            Log.d("UnifiedOnboarding", "Data already loaded, skipping...")
            return
        }
        
        viewModelScope.launch {
            try {
                Log.d("UnifiedOnboarding", "Loading existing user data...")
                hasLoadedData = true
                
                // Only load if state is empty (first time)
                val currentState = _uiState.value
                if (currentState.name.isNotEmpty() || 
                    currentState.role.isNotEmpty() || 
                    currentState.gender.isNotEmpty() ||
                    currentState.selectedCrops.isNotEmpty() ||
                    currentState.selectedLivestock.isNotEmpty()) {
                    Log.d("UnifiedOnboarding", "State already has data, not overwriting")
                    return@launch
                }
                
                // Get current user from repository
                val currentUser = repository.currentUser.value
                
                // Get the current UI language from preferences
                val uiLanguage = preferencesManager.getSelectedLanguage()
                Log.d("UnifiedOnboarding", "Current UI language: $uiLanguage")
                
                if (currentUser != null) {
                    Log.d("UnifiedOnboarding", "Found user data: ${currentUser.name}, ${currentUser.location}")
                    
                    _uiState.update { state ->
                        state.copy(
                            location = if (state.location.isEmpty()) currentUser.location ?: "" else state.location,
                            selectedLanguage = if (state.selectedLanguage == null) uiLanguage else state.selectedLanguage,
                            name = if (state.name.isEmpty()) currentUser.name ?: "" else state.name,
                            role = if (state.role.isEmpty()) currentUser.role ?: "" else state.role,
                            gender = if (state.gender.isEmpty()) currentUser.gender ?: "" else state.gender,
                            selectedCrops = if (state.selectedCrops.isEmpty()) currentUser.crops.toSet() else state.selectedCrops,
                            selectedLivestock = if (state.selectedLivestock.isEmpty()) currentUser.livestock.toSet() else state.selectedLivestock
                        )
                    }
                } else {
                    Log.d("UnifiedOnboarding", "No user data found")
                    // Set UI language for new users
                    _uiState.update { state ->
                        state.copy(selectedLanguage = if (state.selectedLanguage == null) uiLanguage else state.selectedLanguage)
                    }
                }
            } catch (e: Exception) {
                Log.e("UnifiedOnboarding", "Error loading user data", e)
            }
        }
    }
    
    fun setLocation(location: String) {
        _uiState.update { it.copy(location = location) }
    }
    
    fun setLocationInfo(info: LocationInfo) {
        _uiState.update { it.copy(locationInfo = info) }
    }
    
    fun setLoadingLocation(loading: Boolean) {
        _uiState.update { it.copy(isLoadingLocation = loading) }
    }
    
    fun selectLanguage(language: String) {
        _uiState.update { it.copy(selectedLanguage = language) }
        
        // Load translations for the selected language
        viewModelScope.launch {
            val app = getApplication<FarmerChatApplication>()
            app.translationManager.loadTranslations(language)
            
            // Also save to preferences for immediate use
            preferencesManager.saveSelectedLanguage(language)
        }
    }
    
    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }
    
    fun selectRole(role: String) {
        _uiState.update { it.copy(role = role) }
    }
    
    fun selectGender(gender: String) {
        _uiState.update { it.copy(gender = gender) }
    }
    
    fun navigateToCropSelection() {
        _uiState.update { it.copy(navigateToCropSelection = true) }
    }
    
    fun resetCropNavigation() {
        _uiState.update { it.copy(navigateToCropSelection = false) }
    }
    
    fun navigateToLivestockSelection() {
        _uiState.update { it.copy(navigateToLivestockSelection = true) }
    }
    
    fun resetLivestockNavigation() {
        _uiState.update { it.copy(navigateToLivestockSelection = false) }
    }
    
    fun navigateToLanguageSelection() {
        _uiState.update { it.copy(navigateToLanguageSelection = true) }
    }
    
    fun resetLanguageNavigation() {
        _uiState.update { it.copy(navigateToLanguageSelection = false) }
    }
    
    fun updateSelectedCrops(crops: Set<String>) {
        Log.d("UnifiedOnboarding", "Updating crops: ${crops.size} selected")
        _uiState.update { state ->
            state.copy(selectedCrops = crops).also {
                Log.d("UnifiedOnboarding", "Crops updated in state: ${it.selectedCrops.size}")
            }
        }
    }
    
    fun updateSelectedLivestock(livestock: Set<String>) {
        Log.d("UnifiedOnboarding", "Updating livestock: ${livestock.size} selected")
        _uiState.update { state ->
            state.copy(selectedLivestock = livestock).also {
                Log.d("UnifiedOnboarding", "Livestock updated in state: ${it.selectedLivestock.size}")
            }
        }
    }
    
    fun getSuggestedLanguages(): List<String> {
        val locationInfo = _uiState.value.locationInfo
        return if (locationInfo != null) {
            // Try state/region first, then fall back to country
            val location = if (locationInfo.regionLevel1.isNotEmpty()) {
                locationInfo.regionLevel1
            } else {
                locationInfo.country
            }
            LocationLanguageMapper.getLanguagesForLocation(location)?.primaryLanguages ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    fun calculateProgress(): Float {
        val state = _uiState.value
        var completed = 0
        val total = 7
        
        if (state.location.isNotEmpty()) completed++
        if (state.selectedLanguage != null) completed++
        if (state.name.isNotEmpty()) completed++
        if (state.role.isNotEmpty()) completed++
        if (state.gender.isNotEmpty()) completed++
        if (state.selectedCrops.isNotEmpty()) completed++
        if (state.selectedLivestock.isNotEmpty()) completed++
        
        return completed.toFloat() / total.toFloat()
    }
    
    fun isProfileComplete(): Boolean {
        val state = _uiState.value
        return state.location.isNotEmpty() &&
               state.selectedLanguage != null &&
               state.name.isNotEmpty() &&
               state.role.isNotEmpty() &&
               state.gender.isNotEmpty() &&
               (state.selectedCrops.isNotEmpty() || state.selectedLivestock.isNotEmpty())
    }
    
    fun completeOnboarding() {
        if (!isProfileComplete()) {
            _uiState.update { it.copy(error = "Please complete all required fields") }
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                val state = _uiState.value
                
                // Update user profile
                val result = repository.updateUserProfile(
                    name = state.name,
                    language = state.selectedLanguage ?: "en",
                    location = state.location,
                    crops = state.selectedCrops.toList(),
                    livestock = state.selectedLivestock.toList(),
                    role = state.role,
                    gender = state.gender
                )
                
                result.fold(
                    onSuccess = {
                        viewModelScope.launch {
                            // Save preferences
                            preferencesManager.setOnboardingCompleted(true)
                            preferencesManager.saveUserName(state.name)
                            state.selectedLanguage?.let { preferencesManager.saveSelectedLanguage(it) }
                        }
                        
                        _uiState.update { it.copy(isLoading = false, isComplete = true) }
                    },
                    onFailure = { error ->
                        Log.e("UnifiedOnboarding", "Failed to update profile", error)
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = "Failed to save profile. Please try again."
                            ) 
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("UnifiedOnboarding", "Error completing onboarding", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "An error occurred. Please try again."
                    ) 
                }
            }
        }
    }
}