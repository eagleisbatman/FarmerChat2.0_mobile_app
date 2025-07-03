package com.digitalgreen.farmerchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.digitalgreen.farmerchat.navigation.FarmerChatNavigation
import com.digitalgreen.farmerchat.ui.theme.FarmerChatTheme
import com.digitalgreen.farmerchat.utils.StringsManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize StringsManager with TranslationManager
        StringsManager.initialize(application)
        
        setContent {
            FarmerChatTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    FarmerChatNavigation()
                }
            }
        }
    }
}