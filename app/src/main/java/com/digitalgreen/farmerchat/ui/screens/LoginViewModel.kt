package com.digitalgreen.farmerchat.ui.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digitalgreen.farmerchat.FarmerChatApplication
import com.digitalgreen.farmerchat.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as FarmerChatApplication).repository
    private val preferencesManager = PreferencesManager(application)
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState
    
    fun login(phoneNumber: String, pin: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Format phone number (ensure it has country code)
                val formattedPhone = if (phoneNumber.startsWith("+")) {
                    phoneNumber
                } else {
                    // Default to +1 if no country code provided
                    "+$phoneNumber"
                }
                
                Log.d("LoginViewModel", "Attempting login with phone: $formattedPhone")
                
                // Call backend login endpoint
                val result = repository.loginWithPhone(formattedPhone, pin)
                
                result.fold(
                    onSuccess = { loginResponse ->
                        Log.d("LoginViewModel", "Login successful")
                        
                        // Save credentials for auto-login
                        preferencesManager.saveUserPhone(formattedPhone)
                        
                        // Check if user profile is complete
                        val profileComplete = isUserProfileComplete(loginResponse.user)
                        
                        if (profileComplete) {
                            // Update local onboarding status
                            preferencesManager.setOnboardingCompleted(true)
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            loginSuccess = profileComplete,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        Log.e("LoginViewModel", "Login failed", error)
                        
                        val errorMessage = when {
                            error.message?.contains("Invalid credentials", ignoreCase = true) == true ||
                            error.message?.contains("401") == true -> "Invalid phone number or PIN. Please check and try again."
                            error.message?.contains("No PIN set", ignoreCase = true) == true ||
                            error.message?.contains("403") == true -> "No PIN set for this account. Please register to set a PIN."
                            error.message?.contains("User not found", ignoreCase = true) == true ||
                            error.message?.contains("404") == true -> "Account not found. Please register first."
                            error.message?.contains("network", ignoreCase = true) == true -> "Network error. Please check your connection."
                            error.message?.contains("timeout", ignoreCase = true) == true -> "Connection timeout. Please try again."
                            error.message?.contains("Unable to resolve host", ignoreCase = true) == true -> "Cannot connect to server. Please check your internet connection."
                            else -> "Login failed. Please check your phone number and PIN."
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Unexpected error during login", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "An unexpected error occurred. Please try again."
                )
            }
        }
    }
    
    private fun isUserProfileComplete(user: com.digitalgreen.farmerchat.network.ApiUser): Boolean {
        // Check if all required onboarding fields are filled
        val hasLanguage = user.language.isNotEmpty() && user.language != "en"
        val hasLocation = !user.location.isNullOrEmpty()
        val hasCropsOrLivestock = user.crops.isNotEmpty() || user.livestock.isNotEmpty()
        val hasName = !user.name.isNullOrBlank()
        val hasRole = !user.role.isNullOrBlank()
        
        return hasLanguage && hasLocation && hasCropsOrLivestock && hasName && hasRole
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val loginSuccess: Boolean? = null, // null = not attempted, true = profile complete, false = needs onboarding
    val error: String? = null
)