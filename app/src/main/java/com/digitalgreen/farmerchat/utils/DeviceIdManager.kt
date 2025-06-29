package com.digitalgreen.farmerchat.utils

import android.content.Context
import android.provider.Settings
import java.util.UUID

class DeviceIdManager(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "device_prefs"
        private const val KEY_DEVICE_ID = "device_id"
    }
    
    fun getDeviceId(): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // First, check if we have a stored device ID
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        
        if (deviceId == null) {
            // Try to get Android ID (unique to each device)
            deviceId = try {
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            } catch (e: Exception) {
                null
            }
            
            // If Android ID is not available or is the emulator default, generate a UUID
            if (deviceId == null || deviceId == "9774d56d682e549c") {
                deviceId = UUID.randomUUID().toString()
            }
            
            // Store the device ID for future use
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
        
        return deviceId
    }
}