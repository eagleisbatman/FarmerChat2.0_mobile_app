package com.digitalgreen.farmerchat.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.digitalgreen.farmerchat.data.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val currentStep: RegistrationStep = RegistrationStep.PHONE,
    val phoneNumber: String = "",
    val pin: String = "",
    val confirmPin: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val registrationSuccess: Boolean = false
)

class RegisterViewModel(
    private val repository: AppRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
    
    fun updatePhoneNumber(phone: String) {
        _uiState.update { it.copy(phoneNumber = phone, error = null) }
    }
    
    fun updatePIN(pin: String) {
        _uiState.update { it.copy(pin = pin, error = null) }
    }
    
    fun updateConfirmPIN(confirmPin: String) {
        _uiState.update { it.copy(confirmPin = confirmPin, error = null) }
    }
    
    fun proceedToPinStep() {
        _uiState.update { it.copy(currentStep = RegistrationStep.PIN, error = null) }
    }
    
    
    fun register() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Format phone number - accept with or without country code
                val phone = _uiState.value.phoneNumber
                val formattedPhone = if (phone.startsWith("+")) phone else "+91$phone"
                
                Log.d("RegisterViewModel", "Registering user with phone: $formattedPhone")
                
                // Call the backend register endpoint
                val result = repository.registerWithPhone(formattedPhone, _uiState.value.pin)
                
                result.fold(
                    onSuccess = { authData ->
                        Log.d("RegisterViewModel", "Registration successful")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                registrationSuccess = true,
                                error = null
                            ) 
                        }
                    },
                    onFailure = { error ->
                        Log.e("RegisterViewModel", "Registration failed", error)
                        val errorMessage = when {
                            error.message?.contains("409") == true || 
                            error.message?.contains("already registered") == true -> {
                                "This phone number is already registered. Please login instead."
                            }
                            else -> error.message ?: "Registration failed. Please try again."
                        }
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = errorMessage
                            ) 
                        }
                    }
                )
                
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Registration error", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "An error occurred. Please try again."
                    ) 
                }
            }
        }
    }
    
    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RegisterViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}