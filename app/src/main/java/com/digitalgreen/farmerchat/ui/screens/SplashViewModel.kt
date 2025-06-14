package com.digitalgreen.farmerchat.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digitalgreen.farmerchat.data.FarmerChatRepository
import com.digitalgreen.farmerchat.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SplashViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesManager = PreferencesManager(application)
    private val repository = FarmerChatRepository()
    
    private val _hasCompletedOnboarding = MutableStateFlow<Boolean?>(null)
    val hasCompletedOnboarding: StateFlow<Boolean?> = _hasCompletedOnboarding
    
    init {
        viewModelScope.launch {
            // Sign in anonymously
            repository.signInAnonymously()
            
            // Check onboarding status
            preferencesManager.hasCompletedOnboarding.collect { completed ->
                _hasCompletedOnboarding.value = completed
            }
        }
    }
}