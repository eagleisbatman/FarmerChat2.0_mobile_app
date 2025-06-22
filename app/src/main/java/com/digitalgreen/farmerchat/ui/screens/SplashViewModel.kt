package com.digitalgreen.farmerchat.ui.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digitalgreen.farmerchat.FarmerChatApplication
import com.digitalgreen.farmerchat.utils.PreferencesManager
import com.digitalgreen.farmerchat.utils.NavigationHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SplashViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesManager = PreferencesManager(application)
    private val repository = (application as FarmerChatApplication).repository
    
    private val _hasCompletedOnboarding = MutableStateFlow<Boolean?>(null)
    val hasCompletedOnboarding: StateFlow<Boolean?> = _hasCompletedOnboarding
    
    private val _authenticationState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authenticationState: StateFlow<AuthState> = _authenticationState
    
    private val _navigationDecision = MutableStateFlow<NavigationHelper.NavigationDecision?>(null)
    val navigationDecision: StateFlow<NavigationHelper.NavigationDecision?> = _navigationDecision
    
    init {
        authenticateUser()
    }
    
    private fun authenticateUser() {
        viewModelScope.launch {
            try {
                Log.d("SplashViewModel", "=== SPLASH INIT START ===")
                _authenticationState.value = AuthState.Loading
                
                // Step 1: Sign in anonymously with Firebase
                val firebaseAuth = FirebaseAuth.getInstance()
                val currentUser = firebaseAuth.currentUser
                
                if (currentUser == null) {
                    Log.d("SplashViewModel", "No Firebase user, signing in anonymously")
                    firebaseAuth.signInAnonymously().await()
                    Log.d("SplashViewModel", "Firebase anonymous signin completed")
                } else {
                    Log.d("SplashViewModel", "Firebase user already exists: ${currentUser.uid}")
                }
                
                // Step 2: Authenticate with backend API - REQUIRED for app to function
                Log.d("SplashViewModel", "Starting backend authentication...")
                
                val authResult = repository.authenticateWithFirebase()
                
                authResult.fold(
                    onSuccess = { authResponse ->
                        Log.d("SplashViewModel", "✅ Backend auth SUCCESS - token: ${authResponse.token.take(10)}...")
                        Log.d("SplashViewModel", "User ID: ${authResponse.user.id}")
                        Log.d("SplashViewModel", "Expires in: ${authResponse.expiresIn}s")
                        
                        _authenticationState.value = AuthState.Authenticated
                        
                        // Only check onboarding after successful authentication
                        checkOnboardingStatus()
                    },
                    onFailure = { error ->
                        Log.e("SplashViewModel", "❌ Backend auth FAILED", error)
                        // Stay on splash screen with error - don't proceed without backend
                        _authenticationState.value = AuthState.Error(error.message ?: "Cannot connect to server")
                        // Don't check onboarding - stay on splash screen
                    }
                )
            } catch (e: Exception) {
                Log.e("SplashViewModel", "Error during initialization", e)
                _authenticationState.value = AuthState.Error(e.message ?: "Unknown error")
                // Don't check onboarding - stay on splash screen with error
            }
        }
    }
    
    private suspend fun checkOnboardingStatus() {
        try {
            // Get onboarding status with a single read, not continuous collection
            val completed = preferencesManager.hasCompletedOnboarding.first()
            Log.d("SplashViewModel", "Onboarding completed: $completed")
            _hasCompletedOnboarding.value = completed
            
            // If onboarding is complete, determine smart navigation
            if (completed) {
                Log.d("SplashViewModel", "User has completed onboarding, determining navigation...")
                val decision = NavigationHelper.determineAppLaunchNavigation(repository, preferencesManager)
                Log.d("SplashViewModel", "Navigation decision: shouldGoToChat=${decision.shouldGoToChat}, conversationId=${decision.conversationId}")
                _navigationDecision.value = decision
            }
        } catch (e: Exception) {
            Log.e("SplashViewModel", "Error checking onboarding status", e)
            _hasCompletedOnboarding.value = false
        }
    }
    
    fun retryAuthentication() {
        authenticateUser()
    }
    
    sealed class AuthState {
        object Loading : AuthState()
        object Authenticated : AuthState()
        data class Error(val message: String) : AuthState()
    }
}