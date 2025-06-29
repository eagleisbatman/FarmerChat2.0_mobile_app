package com.digitalgreen.farmerchat.ui.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digitalgreen.farmerchat.FarmerChatApplication
import com.digitalgreen.farmerchat.network.ApiUser
import com.digitalgreen.farmerchat.utils.PreferencesManager
import com.digitalgreen.farmerchat.utils.NavigationHelper
import com.digitalgreen.farmerchat.utils.DeviceIdManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesManager = PreferencesManager(application)
    private val repository = (application as FarmerChatApplication).repository
    private val deviceIdManager = DeviceIdManager(application)
    
    private val _hasCompletedOnboarding = MutableStateFlow<Boolean?>(null)
    val hasCompletedOnboarding: StateFlow<Boolean?> = _hasCompletedOnboarding
    
    private val _authenticationState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authenticationState: StateFlow<AuthState> = _authenticationState
    
    private val _navigationDecision = MutableStateFlow<NavigationHelper.NavigationDecision?>(null)
    val navigationDecision: StateFlow<NavigationHelper.NavigationDecision?> = _navigationDecision
    
    init {
        checkAuthenticationStatus()
    }
    
    private fun checkAuthenticationStatus() {
        viewModelScope.launch {
            try {
                Log.d("SplashViewModel", "=== SPLASH INIT START ===")
                _authenticationState.value = AuthState.Loading
                
                // Check if we have a saved JWT token
                val savedToken = preferencesManager.getJwtToken()
                val isTokenExpired = preferencesManager.isTokenExpired()
                
                if (savedToken != null && !isTokenExpired) {
                    Log.d("SplashViewModel", "Found valid saved token")
                    
                    // Validate token with backend
                    val userResult = repository.getUserProfile()
                    
                    userResult.fold(
                        onSuccess = { user ->
                            Log.d("SplashViewModel", "✅ Token valid, user: ${user.id}")
                            _authenticationState.value = AuthState.Authenticated
                            
                            // Check if user has completed onboarding
                            val profileComplete = isUserProfileComplete(user)
                            Log.d("SplashViewModel", "User profile complete: $profileComplete")
                            
                            if (profileComplete) {
                                preferencesManager.setOnboardingCompleted(true)
                                _hasCompletedOnboarding.value = true
                                
                                // Determine navigation
                                val decision = NavigationHelper.determineAppLaunchNavigation(repository, preferencesManager)
                                _navigationDecision.value = decision
                            } else {
                                _hasCompletedOnboarding.value = false
                            }
                        },
                        onFailure = { error ->
                            Log.e("SplashViewModel", "Token validation failed", error)
                            // Token is invalid, need to login
                            _authenticationState.value = AuthState.NotAuthenticated
                            preferencesManager.clearAuthTokens()
                        }
                    )
                } else {
                    Log.d("SplashViewModel", "No valid token found")
                    _authenticationState.value = AuthState.NotAuthenticated
                }
            } catch (e: Exception) {
                Log.e("SplashViewModel", "Error during authentication check", e)
                _authenticationState.value = AuthState.NotAuthenticated
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
        checkAuthenticationStatus()
    }
    
    private fun isUserProfileComplete(user: ApiUser): Boolean {
        // A user has completed onboarding if they have:
        // 1. Selected a language (not just default "en")
        // 2. Selected crops or livestock
        // 3. Provided their name
        // 4. Selected a role (farmer or extension worker)
        
        val hasLanguage = user.language != "en" || user.language.isNotEmpty()
        val hasCropsOrLivestock = user.crops.isNotEmpty() || user.livestock.isNotEmpty()
        val hasName = !user.name.isNullOrBlank()
        val hasRole = !user.role.isNullOrBlank()
        
        Log.d("SplashViewModel", "Profile check - Language: $hasLanguage, Crops/Livestock: $hasCropsOrLivestock, Name: $hasName, Role: $hasRole")
        Log.d("SplashViewModel", "User details - Name: ${user.name}, Language: ${user.language}, Crops: ${user.crops.size}, Livestock: ${user.livestock.size}, Role: ${user.role}")
        
        // User is considered to have completed onboarding if they have all required fields
        return hasLanguage && hasCropsOrLivestock && hasName && hasRole
    }
    
    sealed class AuthState {
        object Loading : AuthState()
        object Authenticated : AuthState()
        object NotAuthenticated : AuthState()
        data class Error(val message: String) : AuthState()
    }
}