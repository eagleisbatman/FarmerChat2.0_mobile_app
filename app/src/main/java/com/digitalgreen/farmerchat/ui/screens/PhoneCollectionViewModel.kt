package com.digitalgreen.farmerchat.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digitalgreen.farmerchat.FarmerChatApplication
import com.digitalgreen.farmerchat.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PhoneCollectionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as FarmerChatApplication).repository
    private val preferencesManager = PreferencesManager(application)
    
    private val _uiState = MutableStateFlow(PhoneCollectionUiState())
    val uiState: StateFlow<PhoneCollectionUiState> = _uiState
    
    val userLocation = preferencesManager.userLocation
    
    fun savePhoneAndPin(phoneNumber: String, pin: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // For now, update user profile with phone number
                // PIN would be stored separately in a secure way
                val result = repository.updateUserProfile(phone = phoneNumber)
                
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSaved = true,
                        error = null
                    )
                }.onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to save phone number: ${e.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error: ${e.message}"
                )
            }
        }
    }
    
    fun savePhoneNumber(phoneNumber: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Update user profile with phone number only (no PIN for V2)
                val result = repository.updateUserProfile(phone = phoneNumber)
                
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSaved = true,
                        error = null
                    )
                }.onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to save phone number: ${e.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class PhoneCollectionUiState(
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)