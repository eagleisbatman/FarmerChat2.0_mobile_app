package com.digitalgreen.farmerchat.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digitalgreen.farmerchat.FarmerChatApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

data class PhoneAuthUiState(
    val isLoading: Boolean = false,
    val isOtpSent: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
    val verificationId: String? = null
)

class PhoneAuthViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as FarmerChatApplication
    private val repository = app.repository
    private val firebaseAuth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow(PhoneAuthUiState())
    val uiState: StateFlow<PhoneAuthUiState> = _uiState.asStateFlow()
    
    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    
    fun sendOtp(phoneNumber: String) {
        if (!isValidPhoneNumber(phoneNumber)) {
            _uiState.value = _uiState.value.copy(
                error = "Please enter a valid phone number with country code"
            )
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-verification completed
                signInWithPhoneAuthCredential(credential)
            }
            
            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Verification failed"
                )
            }
            
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                storedVerificationId = verificationId
                resendToken = token
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isOtpSent = true,
                    verificationId = verificationId,
                    error = null
                )
            }
        }
        
        // Note: In a real implementation, you would need to pass the activity context
        // For now, we'll use a simpler approach with Firebase Auth UI or manual OTP
        // This is a placeholder implementation
        
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isOtpSent = true,
            verificationId = "mock-verification-id", // In real app, this would come from Firebase
            error = null
        )
    }
    
    fun verifyOtp(otp: String) {
        val verificationId = storedVerificationId
        if (verificationId == null) {
            _uiState.value = _uiState.value.copy(
                error = "Verification ID not found. Please request OTP again."
            )
            return
        }
        
        if (otp.length != 6) {
            _uiState.value = _uiState.value.copy(
                error = "Please enter a valid 6-digit OTP"
            )
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        signInWithPhoneAuthCredential(credential)
    }
    
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                val currentUser = firebaseAuth.currentUser
                if (currentUser != null && currentUser.isAnonymous) {
                    // Link phone credential to existing anonymous account
                    currentUser.linkWithCredential(credential).await()
                } else {
                    // Sign in with phone credential
                    firebaseAuth.signInWithCredential(credential).await()
                }
                
                // Update backend with phone number
                repository.authenticateWithFirebase()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Authentication failed"
                )
            }
        }
    }
    
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Basic validation - starts with + and has at least 10 digits
        return phoneNumber.startsWith("+") && phoneNumber.length >= 10
    }
}