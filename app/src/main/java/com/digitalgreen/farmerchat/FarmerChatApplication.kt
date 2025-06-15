package com.digitalgreen.farmerchat

import android.app.Application
import com.digitalgreen.farmerchat.data.AppRepository
import com.digitalgreen.farmerchat.network.NetworkConfig
import com.google.firebase.FirebaseApp

class FarmerChatApplication : Application() {
    
    // Main repository using API backend
    val repository by lazy { AppRepository(this) }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase (for Auth only)
        FirebaseApp.initializeApp(this)
        
        // Initialize Network configuration
        NetworkConfig.initialize(this)
    }
}

// Extension function to get the application instance
fun Application.asFarmerChatApplication(): FarmerChatApplication {
    return this as FarmerChatApplication
}