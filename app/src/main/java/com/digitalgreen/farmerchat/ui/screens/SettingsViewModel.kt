package com.digitalgreen.farmerchat.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digitalgreen.farmerchat.BuildConfig
import com.digitalgreen.farmerchat.data.FarmerChatRepository
import com.digitalgreen.farmerchat.data.LanguageManager
import com.digitalgreen.farmerchat.utils.PreferencesManager
import com.digitalgreen.farmerchat.utils.StringProvider
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import com.google.firebase.auth.FirebaseAuth
// Remove Hilt imports as we're not using dependency injection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
// Remove inject import

data class SettingsState(
    val userName: String = "",
    val userLocation: String = "",
    val selectedCrops: List<String> = emptyList(),
    val selectedLivestock: List<String> = emptyList(),
    val userRole: String = "",
    val userGender: String = "",
    val currentLanguage: String = "en",
    val currentLanguageName: String = "English",
    val voiceResponsesEnabled: Boolean = true,
    val voiceInputEnabled: Boolean = true,
    val responseLength: ResponseLength = ResponseLength.DETAILED,
    val formattedResponsesEnabled: Boolean = true,
    val appVersion: String = "",
    val isLoading: Boolean = false
)

class SettingsViewModel(
    private val repository: FarmerChatRepository,
    private val preferencesManager: PreferencesManager,
    private val stringProvider: StringProvider
) : ViewModel() {
    
    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            _settingsState.update { it.copy(isLoading = true) }
            
            try {
                // Load user profile
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    val userProfile = repository.getUserProfile(userId)
                    userProfile?.let { profile ->
                        _settingsState.update { state ->
                            state.copy(
                                userName = stringProvider.getString(StringKey.DEFAULT_USER_NAME, userId.take(6)),
                                userLocation = profile.locationInfo?.formattedAddress ?: profile.location,
                                selectedCrops = profile.crops,
                                selectedLivestock = profile.livestock,
                                userRole = profile.role ?: "",
                                userGender = profile.gender ?: "",
                                currentLanguage = profile.language
                            )
                        }
                    }
                }
                
                // Load preferences
                val savedLanguage = preferencesManager.getSelectedLanguage()
                val languageName = LanguageManager.getLanguageByCode(savedLanguage)?.name ?: "English"
                
                val voiceResponsesEnabled = preferencesManager.getVoiceResponsesEnabled()
                val voiceInputEnabled = preferencesManager.getVoiceInputEnabled()
                val responseLength = ResponseLength.valueOf(
                    preferencesManager.getResponseLength() ?: ResponseLength.DETAILED.name
                )
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
                        responseLength = responseLength,
                        formattedResponsesEnabled = formattedResponsesEnabled,
                        appVersion = appVersion,
                        isLoading = false
                    )
                }
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
            
            // Update user profile in Firebase
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                repository.updateUserLanguage(userId, languageCode)
            }
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
    
    fun updateResponseLength(length: ResponseLength) {
        viewModelScope.launch {
            preferencesManager.saveResponseLength(length.name)
            _settingsState.update { it.copy(responseLength = length) }
        }
    }
    
    suspend fun exportUserData(context: Context) {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                
                // Collect all user data
                val userData = mutableMapOf<String, Any>()
                
                // User profile
                val profile = repository.getUserProfile(userId)
                userData["profile"] = profile ?: "No profile data"
                
                // Conversations
                val conversations = repository.getUserConversations(userId)
                userData["conversations"] = conversations
                
                // Messages for each conversation
                val allMessages = mutableMapOf<String, List<Any>>()
                conversations.forEach { conversation ->
                    val messages = repository.getConversationMessages(conversation.id)
                    allMessages[conversation.id] = messages
                }
                userData["messages"] = allMessages
                
                // Settings
                userData["settings"] = mapOf(
                    "language" to _settingsState.value.currentLanguage,
                    "voiceResponsesEnabled" to _settingsState.value.voiceResponsesEnabled,
                    "voiceInputEnabled" to _settingsState.value.voiceInputEnabled,
                    "responseLength" to _settingsState.value.responseLength.name,
                    "formattedResponsesEnabled" to _settingsState.value.formattedResponsesEnabled
                )
                
                // Create JSON file manually for now
                val jsonData = buildString {
                    appendLine("{")
                    appendLine("  \"exportDate\": \"${Date()}\",")
                    appendLine("  \"userId\": \"$userId\",")
                    appendLine("  \"profile\": ${profile?.let { "\"${it.toString()}\"" } ?: "null"},")
                    appendLine("  \"conversationCount\": ${conversations.size},")
                    appendLine("  \"messageCount\": ${allMessages.values.sumOf { it.size }},")
                    appendLine("  \"settings\": {")
                    appendLine("    \"language\": \"${_settingsState.value.currentLanguage}\",")
                    appendLine("    \"voiceResponsesEnabled\": ${_settingsState.value.voiceResponsesEnabled},")
                    appendLine("    \"voiceInputEnabled\": ${_settingsState.value.voiceInputEnabled},")
                    appendLine("    \"responseLength\": \"${_settingsState.value.responseLength.name}\",")
                    appendLine("    \"formattedResponsesEnabled\": ${_settingsState.value.formattedResponsesEnabled}")
                    appendLine("  }")
                    appendLine("}")
                }
                
                val fileName = "farmerchat_data_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.json"
                val file = File(context.cacheDir, fileName)
                file.writeText(jsonData)
                
                // Share file
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                context.startActivity(Intent.createChooser(shareIntent, stringProvider.getString(StringKey.EXPORT_FARMERCHAT_DATA)))
                
            } catch (e: Exception) {
                Toast.makeText(context, stringProvider.getString(StringKey.FAILED_TO_EXPORT, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
        }
    }
    
    suspend fun deleteAllUserData() {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                
                // Delete all user data from Firebase
                repository.deleteAllUserData(userId)
                
                // Clear local preferences
                preferencesManager.clearAll()
                
                // Sign out
                FirebaseAuth.getInstance().signOut()
                
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    suspend fun resetOnboarding() {
        preferencesManager.setOnboardingCompleted(false)
    }
    
    fun updateUserName(name: String) {
        viewModelScope.launch {
            _settingsState.update { it.copy(userName = name) }
            // TODO: Update user profile in Firebase
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                // Firebase doesn't have a name field yet, but we can store it locally
                preferencesManager.saveUserName(name)
            }
        }
    }
    
    fun updateUserLocation(location: String) {
        viewModelScope.launch {
            _settingsState.update { it.copy(userLocation = location) }
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val profile = repository.getUserProfile(userId)
                profile?.let {
                    val updatedProfile = it.copy(location = location)
                    repository.saveUserProfile(updatedProfile)
                }
            }
        }
    }
    
    fun updateUserRole(role: String) {
        viewModelScope.launch {
            _settingsState.update { it.copy(userRole = role) }
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val profile = repository.getUserProfile(userId)
                profile?.let {
                    val updatedProfile = it.copy(role = role)
                    repository.saveUserProfile(updatedProfile)
                }
            }
        }
    }
    
    fun updateUserGender(gender: String) {
        viewModelScope.launch {
            _settingsState.update { it.copy(userGender = gender) }
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val profile = repository.getUserProfile(userId)
                profile?.let {
                    val updatedProfile = it.copy(gender = gender)
                    repository.saveUserProfile(updatedProfile)
                }
            }
        }
    }
    
}