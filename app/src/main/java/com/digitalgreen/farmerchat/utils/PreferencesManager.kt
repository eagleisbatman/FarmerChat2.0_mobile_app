package com.digitalgreen.farmerchat.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "farmer_chat_prefs")

class PreferencesManager(private val context: Context) {
    
    companion object {
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val USER_LANGUAGE = stringPreferencesKey("user_language")
        val USER_LOCATION = stringPreferencesKey("user_location")
        val USER_CROPS = stringSetPreferencesKey("user_crops")
        val USER_LIVESTOCK = stringSetPreferencesKey("user_livestock")
        val VOICE_RESPONSES_ENABLED = booleanPreferencesKey("voice_responses_enabled")
        val VOICE_INPUT_ENABLED = booleanPreferencesKey("voice_input_enabled")
        val RESPONSE_LENGTH = stringPreferencesKey("response_length")
        val FORMATTED_RESPONSES_ENABLED = booleanPreferencesKey("formatted_responses_enabled")
        val USER_NAME = stringPreferencesKey("user_name")
        val JWT_TOKEN = stringPreferencesKey("jwt_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val TOKEN_EXPIRES_AT = longPreferencesKey("token_expires_at")
        val USER_PHONE = stringPreferencesKey("user_phone")
    }
    
    val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[HAS_COMPLETED_ONBOARDING] ?: false
        }
    
    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAS_COMPLETED_ONBOARDING] = completed
        }
    }
    
    suspend fun saveUserPreferences(
        language: String,
        location: String,
        crops: Set<String>,
        livestock: Set<String>
    ) {
        context.dataStore.edit { preferences ->
            preferences[USER_LANGUAGE] = language
            preferences[USER_LOCATION] = location
            preferences[USER_CROPS] = crops
            preferences[USER_LIVESTOCK] = livestock
        }
    }
    
    val userLanguage: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[USER_LANGUAGE] ?: "en"
        }
    
    val userLocation: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[USER_LOCATION] ?: ""
        }
    
    val userCrops: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[USER_CROPS] ?: emptySet()
        }
    
    val userLivestock: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[USER_LIVESTOCK] ?: emptySet()
        }
    
    // New preference methods for settings
    fun getSelectedLanguage(): String {
        return runBlocking {
            context.dataStore.data.first()[USER_LANGUAGE] ?: "en"
        }
    }
    
    suspend fun saveSelectedLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_LANGUAGE] = language
        }
    }
    
    fun getVoiceResponsesEnabled(): Boolean {
        return runBlocking {
            context.dataStore.data.first()[VOICE_RESPONSES_ENABLED] ?: true
        }
    }
    
    suspend fun saveVoiceResponsesEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[VOICE_RESPONSES_ENABLED] = enabled
        }
    }
    
    fun getVoiceInputEnabled(): Boolean {
        return runBlocking {
            context.dataStore.data.first()[VOICE_INPUT_ENABLED] ?: true
        }
    }
    
    suspend fun saveVoiceInputEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[VOICE_INPUT_ENABLED] = enabled
        }
    }
    
    fun getResponseLength(): String? {
        return runBlocking {
            context.dataStore.data.first()[RESPONSE_LENGTH]
        }
    }
    
    suspend fun saveResponseLength(length: String) {
        context.dataStore.edit { preferences ->
            preferences[RESPONSE_LENGTH] = length
        }
    }
    
    fun getFormattedResponsesEnabled(): Boolean {
        return runBlocking {
            context.dataStore.data.first()[FORMATTED_RESPONSES_ENABLED] ?: true
        }
    }
    
    suspend fun saveFormattedResponsesEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FORMATTED_RESPONSES_ENABLED] = enabled
        }
    }
    
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    suspend fun saveUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME] = name
        }
    }
    
    // JWT Token management methods
    suspend fun saveAuthTokens(jwtToken: String, refreshToken: String, expiresIn: Long) {
        val expiresAt = System.currentTimeMillis() + (expiresIn * 1000) // Convert seconds to milliseconds
        context.dataStore.edit { preferences ->
            preferences[JWT_TOKEN] = jwtToken
            preferences[REFRESH_TOKEN] = refreshToken
            preferences[TOKEN_EXPIRES_AT] = expiresAt
        }
    }
    
    fun getJwtToken(): String? {
        return runBlocking {
            context.dataStore.data.first()[JWT_TOKEN]
        }
    }
    
    fun getRefreshToken(): String? {
        return runBlocking {
            context.dataStore.data.first()[REFRESH_TOKEN]
        }
    }
    
    fun getTokenExpiresAt(): Long {
        return runBlocking {
            context.dataStore.data.first()[TOKEN_EXPIRES_AT] ?: 0L
        }
    }
    
    fun isTokenExpired(): Boolean {
        val expiresAt = getTokenExpiresAt()
        return if (expiresAt == 0L) true else System.currentTimeMillis() >= expiresAt
    }
    
    suspend fun clearAuthTokens() {
        context.dataStore.edit { preferences ->
            preferences.remove(JWT_TOKEN)
            preferences.remove(REFRESH_TOKEN)
            preferences.remove(TOKEN_EXPIRES_AT)
        }
    }
    
    // Phone authentication
    suspend fun saveUserPhone(phone: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_PHONE] = phone
        }
    }
    
    fun getUserPhone(): String? {
        return runBlocking {
            context.dataStore.data.first()[USER_PHONE]
        }
    }
}