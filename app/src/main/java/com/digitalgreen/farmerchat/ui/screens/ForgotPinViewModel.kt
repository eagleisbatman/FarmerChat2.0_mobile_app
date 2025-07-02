package com.digitalgreen.farmerchat.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digitalgreen.farmerchat.data.AppRepository
import com.digitalgreen.farmerchat.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ForgotPinUiState(
    val step: ForgotPinStep = ForgotPinStep.PHONE,
    val phoneNumber: String = "",
    val phoneError: String? = null,
    val verificationCode: String = "",
    val verificationError: String? = null,
    val newPin: String = "",
    val pinError: String? = null,
    val confirmPin: String = "",
    val confirmPinError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class ForgotPinViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(application)
    private val preferencesManager = PreferencesManager(application)
    
    private val _uiState = MutableStateFlow(ForgotPinUiState())
    val uiState: StateFlow<ForgotPinUiState> = _uiState.asStateFlow()
    
    private var tempVerificationToken: String? = null
    
    fun updatePhoneNumber(phone: String) {
        _uiState.update { 
            it.copy(
                phoneNumber = phone,
                phoneError = null
            )
        }
    }
    
    fun updateVerificationCode(code: String) {
        _uiState.update { 
            it.copy(
                verificationCode = code,
                verificationError = null
            )
        }
    }
    
    fun updateNewPin(pin: String) {
        _uiState.update { 
            it.copy(
                newPin = pin,
                pinError = null
            )
        }
    }
    
    fun updateConfirmPin(pin: String) {
        _uiState.update { 
            it.copy(
                confirmPin = pin,
                confirmPinError = null
            )
        }
    }
    
    fun sendVerificationCode() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Validate phone number
                if (_uiState.value.phoneNumber.isEmpty()) {
                    _uiState.update { 
                        it.copy(
                            phoneError = "Please enter your phone number",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                // TODO: Call backend API to send verification code
                // For now, we'll simulate the process
                // In production, this would be:
                // val response = repository.sendPinResetCode(_uiState.value.phoneNumber)
                
                // Simulate API call
                kotlinx.coroutines.delay(1000)
                
                // Move to verification step
                _uiState.update { 
                    it.copy(
                        step = ForgotPinStep.VERIFY,
                        isLoading = false
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = e.message ?: "Failed to send verification code",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun verifyCode() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Validate verification code
                if (_uiState.value.verificationCode.isEmpty()) {
                    _uiState.update { 
                        it.copy(
                            verificationError = "Please enter the verification code",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                // TODO: Call backend API to verify code
                // For now, we'll simulate the process
                // In production, this would be:
                // val response = repository.verifyPinResetCode(
                //     _uiState.value.phoneNumber,
                //     _uiState.value.verificationCode
                // )
                // tempVerificationToken = response.token
                
                // Simulate API call
                kotlinx.coroutines.delay(1000)
                
                // For demo, accept any 6-digit code
                if (_uiState.value.verificationCode.length == 6) {
                    _uiState.update { 
                        it.copy(
                            step = ForgotPinStep.NEW_PIN,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            verificationError = "Invalid verification code",
                            isLoading = false
                        )
                    }
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = e.message ?: "Failed to verify code",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun resendCode() {
        sendVerificationCode()
    }
    
    fun resetPin() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Validate PINs
                val newPin = _uiState.value.newPin
                val confirmPin = _uiState.value.confirmPin
                
                if (newPin.length != 6) {
                    _uiState.update { 
                        it.copy(
                            pinError = "PIN must be 6 digits",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                if (newPin != confirmPin) {
                    _uiState.update { 
                        it.copy(
                            confirmPinError = "PINs do not match",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                // TODO: Call backend API to reset PIN
                // For now, we'll simulate the process
                // In production, this would be:
                // val response = repository.resetPin(
                //     tempVerificationToken!!,
                //     newPin
                // )
                
                // Simulate API call
                kotlinx.coroutines.delay(1000)
                
                // Success
                _uiState.update { 
                    it.copy(
                        isSuccess = true,
                        isLoading = false
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = e.message ?: "Failed to reset PIN",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}