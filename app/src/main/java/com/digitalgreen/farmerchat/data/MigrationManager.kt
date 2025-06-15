package com.digitalgreen.farmerchat.data

import android.content.Context
import android.util.Log
import com.digitalgreen.farmerchat.FarmerChatApplication
import com.digitalgreen.farmerchat.utils.PreferencesManager
import kotlinx.coroutines.flow.firstOrNull

/**
 * Manages migration between Firebase and API backend
 * Handles data synchronization and gradual migration
 */
class MigrationManager(private val context: Context) {
    private val app = context.applicationContext as FarmerChatApplication
    private val firebaseRepository = app.firebaseRepository
    private val apiRepository = app.apiRepository
    private val preferencesManager = PreferencesManager(context)
    
    companion object {
        private const val TAG = "MigrationManager"
        private const val PREF_MIGRATION_COMPLETED = "migration_completed"
        private const val PREF_API_AUTHENTICATED = "api_authenticated"
    }
    
    /**
     * Check if the user is ready to migrate to API backend
     */
    suspend fun canMigrateToApi(): Boolean {
        // Check if Firebase Auth user exists
        val hasFirebaseUser = firebaseRepository.getCurrentUserId() != null
        
        // Check if user profile exists in Firebase
        val hasFirebaseProfile = firebaseRepository.getUserProfile()
            .getOrNull() != null
        
        return hasFirebaseUser && hasFirebaseProfile
    }
    
    /**
     * Authenticate with API backend using Firebase token
     */
    suspend fun authenticateWithApi(): Result<Boolean> {
        return try {
            Log.d(TAG, "Starting API authentication...")
            
            val authResult = apiRepository.authenticateWithFirebase()
            
            authResult.onSuccess { response ->
                Log.d(TAG, "API authentication successful")
                preferencesManager.setBoolean(PREF_API_AUTHENTICATED, true)
                
                // Sync user data from Firebase to API
                syncUserDataToApi()
                
            }.onFailure { e ->
                Log.e(TAG, "API authentication failed", e)
                preferencesManager.setBoolean(PREF_API_AUTHENTICATED, false)
            }
            
            authResult.map { true }
        } catch (e: Exception) {
            Log.e(TAG, "Error during API authentication", e)
            Result.failure(e)
        }
    }
    
    /**
     * Sync user profile data from Firebase to API
     */
    private suspend fun syncUserDataToApi() {
        try {
            Log.d(TAG, "Starting user data sync to API...")
            
            // Get user profile from Firebase
            val firebaseProfile = firebaseRepository.getUserProfile().getOrNull()
            
            if (firebaseProfile != null) {
                // Update API with Firebase profile data
                val updateRequest = firebaseProfile.toUpdateUserRequest()
                
                apiRepository.updateUserProfile(
                    name = null, // Firebase profile doesn't have name
                    location = updateRequest.location,
                    language = updateRequest.language,
                    crops = updateRequest.crops,
                    livestock = updateRequest.livestock,
                    responseLength = "medium" // Default value
                ).onSuccess {
                    Log.d(TAG, "User profile synced to API successfully")
                }.onFailure { e ->
                    Log.e(TAG, "Failed to sync user profile to API", e)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing user data to API", e)
        }
    }
    
    /**
     * Migrate conversation data from Firebase to API
     * Note: This is for reference - actual migration might be done server-side
     */
    private suspend fun migrateConversationsToApi() {
        try {
            Log.d(TAG, "Starting conversation migration to API...")
            
            // Get conversations from Firebase
            firebaseRepository.getConversations().firstOrNull()?.onSuccess { conversations ->
                Log.d(TAG, "Found ${conversations.size} conversations to migrate")
                
                // Note: Actual conversation migration might need to be done server-side
                // as it involves preserving message history and maintaining consistency
                
                for (conversation in conversations) {
                    Log.d(TAG, "Would migrate conversation: ${conversation.id} - ${conversation.title}")
                    // This would typically be handled by a server-side migration endpoint
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating conversations", e)
        }
    }
    
    /**
     * Enable API repository mode
     */
    fun enableApiMode() {
        Log.d(TAG, "Enabling API repository mode")
        MigrationHelper.enableApiRepository()
        preferencesManager.setBoolean("use_api_repository", true)
    }
    
    /**
     * Disable API repository mode (fallback to Firebase)
     */
    fun disableApiMode() {
        Log.d(TAG, "Disabling API repository mode")
        MigrationHelper.disableApiRepository()
        preferencesManager.setBoolean("use_api_repository", false)
    }
    
    /**
     * Check if migration has been completed
     */
    fun isMigrationCompleted(): Boolean {
        return preferencesManager.getBoolean(PREF_MIGRATION_COMPLETED, false)
    }
    
    /**
     * Mark migration as completed
     */
    fun markMigrationCompleted() {
        preferencesManager.setBoolean(PREF_MIGRATION_COMPLETED, true)
        Log.d(TAG, "Migration marked as completed")
    }
    
    /**
     * Check if API authentication was successful
     */
    fun isApiAuthenticated(): Boolean {
        return preferencesManager.getBoolean(PREF_API_AUTHENTICATED, false)
    }
    
    /**
     * Perform a complete migration from Firebase to API
     */
    suspend fun performFullMigration(): Result<Boolean> {
        return try {
            Log.d(TAG, "Starting full migration process...")
            
            // Step 1: Check if migration is possible
            if (!canMigrateToApi()) {
                return Result.failure(Exception("Migration not possible - missing Firebase data"))
            }
            
            // Step 2: Authenticate with API
            authenticateWithApi().onFailure { e ->
                return Result.failure(Exception("API authentication failed: ${e.message}"))
            }
            
            // Step 3: Enable API mode
            enableApiMode()
            
            // Step 4: Mark migration as completed
            markMigrationCompleted()
            
            Log.d(TAG, "Full migration completed successfully")
            Result.success(true)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during full migration", e)
            
            // Rollback on error
            disableApiMode()
            
            Result.failure(e)
        }
    }
    
    /**
     * Rollback migration (switch back to Firebase)
     */
    fun rollbackMigration() {
        Log.d(TAG, "Rolling back migration...")
        
        disableApiMode()
        preferencesManager.setBoolean(PREF_MIGRATION_COMPLETED, false)
        preferencesManager.setBoolean(PREF_API_AUTHENTICATED, false)
        
        Log.d(TAG, "Migration rollback completed")
    }
    
    /**
     * Get migration status information
     */
    fun getMigrationStatus(): MigrationStatus {
        return MigrationStatus(
            canMigrate = preferencesManager.getBoolean("firebase_auth_available", false),
            isApiAuthenticated = isApiAuthenticated(),
            isMigrationCompleted = isMigrationCompleted(),
            isUsingApiMode = MigrationHelper.shouldUseApi()
        )
    }
}

data class MigrationStatus(
    val canMigrate: Boolean,
    val isApiAuthenticated: Boolean,
    val isMigrationCompleted: Boolean,
    val isUsingApiMode: Boolean
) {
    val migrationStep: String
        get() = when {
            !canMigrate -> "Not Ready"
            !isApiAuthenticated -> "Authentication Needed"
            !isMigrationCompleted -> "Migration Pending"
            isUsingApiMode -> "API Mode Active"
            else -> "Firebase Mode Active"
        }
}