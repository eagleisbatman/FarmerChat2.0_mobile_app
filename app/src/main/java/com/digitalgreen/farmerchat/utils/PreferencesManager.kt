package com.digitalgreen.farmerchat.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "farmer_chat_prefs")

class PreferencesManager(private val context: Context) {
    
    companion object {
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val USER_LANGUAGE = stringPreferencesKey("user_language")
        val USER_LOCATION = stringPreferencesKey("user_location")
        val USER_CROPS = stringSetPreferencesKey("user_crops")
        val USER_LIVESTOCK = stringSetPreferencesKey("user_livestock")
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
}