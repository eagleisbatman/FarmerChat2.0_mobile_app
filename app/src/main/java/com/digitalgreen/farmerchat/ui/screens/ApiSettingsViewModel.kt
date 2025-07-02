package com.digitalgreen.farmerchat.ui.screens

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digitalgreen.farmerchat.BuildConfig
import com.digitalgreen.farmerchat.FarmerChatApplication
import com.digitalgreen.farmerchat.data.LanguageManager
import com.digitalgreen.farmerchat.network.UpdateUserRequest
import com.digitalgreen.farmerchat.utils.PreferencesManager
import com.digitalgreen.farmerchat.utils.StringProvider
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class SettingsState(
    val userName: String = "",
    val userLocation: String = "",
    val userRole: String? = null,
    val userGender: String? = null,
    val selectedCrops: List<String> = emptyList(),
    val selectedLivestock: List<String> = emptyList(),
    val currentLanguage: String = "en",
    val currentLanguageName: String = "English",
    val voiceResponsesEnabled: Boolean = true,
    val voiceInputEnabled: Boolean = true,
    val responseLength: String? = null,
    val formattedResponsesEnabled: Boolean = true,
    val appVersion: String = "1.0",
    val isLoading: Boolean = false
)

class ApiSettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as FarmerChatApplication).repository
    private val preferencesManager = PreferencesManager(application)
    private val stringProvider = StringProvider.create(application)
    
    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()
    
    // Remove init block to prevent premature API calls
    // Call initialize() from the UI when ready
    
    fun initialize() {
        loadSettings()
    }
    
    private fun loadCachedSettings() {
        // Load preferences immediately without API call
        val savedLanguage = preferencesManager.getSelectedLanguage()
        val languageName = LanguageManager.getLanguageByCode(savedLanguage)?.name ?: "English"
        val voiceResponsesEnabled = preferencesManager.getVoiceResponsesEnabled()
        val voiceInputEnabled = preferencesManager.getVoiceInputEnabled()
        val formattedResponsesEnabled = preferencesManager.getFormattedResponsesEnabled()
        
        // Get app version
        val appVersion = try {
            "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        } catch (e: Exception) {
            "1.0.0"
        }
        
        _settingsState.update { state ->
            state.copy(
                currentLanguage = savedLanguage,
                currentLanguageName = languageName,
                voiceResponsesEnabled = voiceResponsesEnabled,
                voiceInputEnabled = voiceInputEnabled,
                formattedResponsesEnabled = formattedResponsesEnabled,
                appVersion = appVersion
            )
        }
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            // Show loading state for skeleton loading
            _settingsState.update { it.copy(isLoading = true) }
            
            // First load cached settings immediately
            loadCachedSettings()
            
            try {
                // Load preferences first to get the source of truth for language
                val savedLanguage = preferencesManager.getSelectedLanguage()
                val languageName = LanguageManager.getLanguageByCode(savedLanguage)?.name ?: "English"
                
                // Load user profile from API
                repository.getUserProfile().onSuccess { apiUser ->
                    _settingsState.update { state ->
                        state.copy(
                            userName = apiUser.name ?: "",
                            userLocation = apiUser.location ?: "",
                            userRole = apiUser.role ?: "",
                            userGender = apiUser.gender ?: "",
                            selectedCrops = apiUser.crops,
                            selectedLivestock = apiUser.livestock,
                            // Don't use API language - use local preference
                            currentLanguage = savedLanguage,
                            currentLanguageName = languageName,
                            responseLength = apiUser.responseLength
                        )
                    }
                }
                
                // Update loading state to false after API call completes
                _settingsState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _settingsState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    fun updateLanguage(languageCode: String) {
        viewModelScope.launch {
            preferencesManager.saveSelectedLanguage(languageCode)
            val languageName = LanguageManager.getLanguageByCode(languageCode)?.name ?: "English"
            
            _settingsState.update { 
                it.copy(
                    currentLanguage = languageCode,
                    currentLanguageName = languageName
                )
            }
            
            // Update user profile via API
            repository.updateUserProfile(language = languageCode)
        }
    }
    
    fun toggleVoiceResponses() {
        viewModelScope.launch {
            val newValue = !_settingsState.value.voiceResponsesEnabled
            preferencesManager.saveVoiceResponsesEnabled(newValue)
            _settingsState.update { it.copy(voiceResponsesEnabled = newValue) }
        }
    }
    
    fun toggleVoiceInput() {
        viewModelScope.launch {
            val newValue = !_settingsState.value.voiceInputEnabled
            preferencesManager.saveVoiceInputEnabled(newValue)
            _settingsState.update { it.copy(voiceInputEnabled = newValue) }
        }
    }
    
    fun toggleFormattedResponses() {
        viewModelScope.launch {
            val newValue = !_settingsState.value.formattedResponsesEnabled
            preferencesManager.saveFormattedResponsesEnabled(newValue)
            _settingsState.update { it.copy(formattedResponsesEnabled = newValue) }
        }
    }
    
    fun updateResponseLength(length: String) {
        viewModelScope.launch {
            preferencesManager.saveResponseLength(length)
            _settingsState.update { it.copy(responseLength = length) }
            
            // Update via API
            repository.updateUserProfile(responseLength = length)
        }
    }
    
    suspend fun exportUserData(context: Context) {
        // TODO: Implement export using API data
        Toast.makeText(context, stringProvider.getString(StringKey.FEATURE_COMING_SOON), Toast.LENGTH_SHORT).show()
    }
    
    fun logout() {
        viewModelScope.launch {
            // Clear local preferences
            preferencesManager.clearAll()
            
            // Sign out from backend
            repository.signOut()
            
            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut()
        }
    }
    
    suspend fun resetOnboarding() {
        preferencesManager.setOnboardingCompleted(false)
        logout()
    }
    
    fun updateUserName(name: String) {
        viewModelScope.launch {
            _settingsState.update { it.copy(userName = name) }
            
            // Update via API
            repository.updateUserProfile(name = name)
        }
    }
    
    fun updateUserLocation(location: String) {
        viewModelScope.launch {
            _settingsState.update { it.copy(userLocation = location) }
            
            // Update via API
            repository.updateUserProfile(location = location)
        }
    }
    
    fun updateUserRole(role: String) {
        viewModelScope.launch {
            _settingsState.update { it.copy(userRole = role) }
            
            // Update via API
            repository.updateUserProfile(role = role)
        }
    }
    
    fun updateUserGender(gender: String) {
        viewModelScope.launch {
            _settingsState.update { it.copy(userGender = gender) }
            
            // Update via API
            repository.updateUserProfile(gender = gender)
        }
    }
    
    fun updateSelectedCrops(crops: List<String>) {
        viewModelScope.launch {
            _settingsState.update { it.copy(selectedCrops = crops) }
            
            // Update via API
            repository.updateUserProfile(crops = crops)
        }
    }
    
    fun updateSelectedLivestock(livestock: List<String>) {
        viewModelScope.launch {
            _settingsState.update { it.copy(selectedLivestock = livestock) }
            
            // Update via API
            repository.updateUserProfile(livestock = livestock)
        }
    }
}