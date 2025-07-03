package com.digitalgreen.farmerchat.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.digitalgreen.farmerchat.ui.screens.ChatScreen
import com.digitalgreen.farmerchat.ui.screens.ConversationsScreen
import com.digitalgreen.farmerchat.ui.screens.UnifiedOnboardingScreen
import com.digitalgreen.farmerchat.ui.screens.UnifiedOnboardingViewModel
import com.digitalgreen.farmerchat.ui.screens.SplashScreen
import com.digitalgreen.farmerchat.ui.screens.LoginScreen
import com.digitalgreen.farmerchat.ui.screens.RegisterScreen
import com.digitalgreen.farmerchat.ui.screens.SettingsScreen
import com.digitalgreen.farmerchat.ui.screens.CropSelectionScreen
import com.digitalgreen.farmerchat.ui.screens.LivestockSelectionScreen
import com.digitalgreen.farmerchat.ui.screens.CropSelectionWrapper
import com.digitalgreen.farmerchat.ui.screens.LivestockSelectionWrapper
import com.digitalgreen.farmerchat.ui.screens.LanguageSelectionWrapper
// import com.digitalgreen.farmerchat.ui.screens.PhoneCollectionScreenV2 // Unused screen
import com.digitalgreen.farmerchat.ui.screens.LanguageSelectionScreen
import com.digitalgreen.farmerchat.ui.screens.NameSelectionScreen
import com.digitalgreen.farmerchat.ui.screens.GenderSelectionScreen
import com.digitalgreen.farmerchat.ui.screens.RoleSelectionScreen
import com.digitalgreen.farmerchat.ui.screens.ForgotPinScreen
import com.digitalgreen.farmerchat.ui.screens.PrivacyPolicyScreen
import com.digitalgreen.farmerchat.ui.screens.TermsConditionsScreen
import com.digitalgreen.farmerchat.ui.screens.ApiSettingsViewModel
import com.digitalgreen.farmerchat.utils.PreferencesManager
import com.digitalgreen.farmerchat.utils.LocationLanguageMapper

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Onboarding : Screen("onboarding")
    object Conversations : Screen("conversations") {
        fun createRoute(startNewChat: Boolean = false) = if (startNewChat) "conversations?startNewChat=true" else "conversations"
    }
    object Chat : Screen("chat/{conversationId}") {
        fun createRoute(conversationId: String, title: String? = null) = 
            if (title != null) "chat/$conversationId?title=${java.net.URLEncoder.encode(title, "UTF-8")}" 
            else "chat/$conversationId"
    }
    object Settings : Screen("settings")
    object CropSelection : Screen("crop_selection") {
        fun createRoute(fromOnboarding: Boolean = false) = "crop_selection?fromOnboarding=$fromOnboarding"
    }
    object LivestockSelection : Screen("livestock_selection") {
        fun createRoute(fromOnboarding: Boolean = false) = "livestock_selection?fromOnboarding=$fromOnboarding"
    }
    object ForgotPin : Screen("forgot_pin")
    object PrivacyPolicy : Screen("privacy_policy")
    object TermsConditions : Screen("terms_conditions") {
        fun createRoute(showAccept: Boolean = false) = "terms_conditions?showAccept=$showAccept"
    }
    object PhoneCollection : Screen("phone_collection")
    object LanguageSelection : Screen("language_selection")
    object NameSelection : Screen("name_selection")
    object GenderSelection : Screen("gender_selection")
    object RoleSelection : Screen("role_selection")
}

@Composable
fun FarmerChatNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToChat = { conversationId ->
                    if (conversationId != null) {
                        navController.navigate(Screen.Chat.createRoute(conversationId)) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Conversations.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                },
                onNavigateToConversations = {
                    navController.navigate(Screen.Conversations.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPin = {
                    navController.navigate(Screen.ForgotPin.route)
                },
                onLoginSuccess = { profileComplete ->
                    if (profileComplete) {
                        navController.navigate(Screen.Conversations.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    // After successful registration, go to onboarding
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Onboarding.route) { backStackEntry ->
            val unifiedViewModel: UnifiedOnboardingViewModel = viewModel(backStackEntry)
            
            UnifiedOnboardingScreen(
                onNavigateToCropSelection = {
                    navController.navigate(Screen.CropSelection.createRoute(fromOnboarding = true))
                },
                onNavigateToLivestockSelection = {
                    navController.navigate(Screen.LivestockSelection.createRoute(fromOnboarding = true))
                },
                onNavigateToLanguageSelection = {
                    navController.navigate(Screen.LanguageSelection.route + "?fromOnboarding=true")
                },
                onOnboardingComplete = {
                    // After onboarding, create new conversation and navigate directly to chat
                    navController.navigate(Screen.Conversations.createRoute(startNewChat = true)) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                viewModel = unifiedViewModel
            )
        }
        
        composable(
            Screen.Conversations.route + "?startNewChat={startNewChat}",
            arguments = listOf(navArgument("startNewChat") { 
                type = NavType.BoolType
                defaultValue = false
            })
        ) { backStackEntry ->
            val startNewChat = backStackEntry.arguments?.getBoolean("startNewChat") ?: false
            ConversationsScreen(
                onNavigateToChat = { conversationId, title ->
                    navController.navigate(Screen.Chat.createRoute(conversationId, title))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                startNewChat = startNewChat
            )
        }
        
        composable(
            Screen.Chat.route + "?title={title}",
            arguments = listOf(
                navArgument("conversationId") { type = NavType.StringType },
                navArgument("title") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            val encodedTitle = backStackEntry.arguments?.getString("title")
            val title = encodedTitle?.let { java.net.URLDecoder.decode(it, "UTF-8") }
            
            ChatScreen(
                conversationId = conversationId,
                initialTitle = title,
                onNavigateBack = {
                    // Navigate to conversations without startNewChat parameter
                    navController.navigate(Screen.Conversations.route) {
                        popUpTo(Screen.Chat.route + "?title={title}") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToNewChat = {
                    navController.navigate(Screen.Conversations.createRoute(startNewChat = true)) {
                        popUpTo(Screen.Chat.route + "?title={title}") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Settings.route) { inclusive = true }
                    }
                },
                onNavigateToCropSelection = {
                    navController.navigate(Screen.CropSelection.route)
                },
                onNavigateToLivestockSelection = {
                    navController.navigate(Screen.LivestockSelection.route)
                },
                onNavigateToLanguageSelection = {
                    navController.navigate(Screen.LanguageSelection.route)
                },
                onNavigateToNameSelection = {
                    navController.navigate(Screen.NameSelection.route)
                },
                onNavigateToGenderSelection = {
                    navController.navigate(Screen.GenderSelection.route)
                },
                onNavigateToRoleSelection = {
                    navController.navigate(Screen.RoleSelection.route)
                },
                onNavigateToPrivacyPolicy = {
                    navController.navigate(Screen.PrivacyPolicy.route)
                },
                onNavigateToTermsConditions = {
                    navController.navigate(Screen.TermsConditions.route)
                }
            )
        }
        
        composable(
            route = Screen.CropSelection.route + "?fromOnboarding={fromOnboarding}",
            arguments = listOf(
                navArgument("fromOnboarding") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val fromOnboarding = backStackEntry.arguments?.getBoolean("fromOnboarding") ?: false
            
            if (fromOnboarding) {
                // Get the onboarding view model from the parent route
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.Onboarding.route)
                }
                val onboardingViewModel: UnifiedOnboardingViewModel = viewModel(parentEntry)
                
                CropSelectionWrapper(
                    navController = navController,
                    fromOnboarding = true,
                    onboardingViewModel = onboardingViewModel
                )
            } else {
                // Regular crop selection from settings
                CropSelectionScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        composable(
            route = Screen.LivestockSelection.route + "?fromOnboarding={fromOnboarding}",
            arguments = listOf(
                navArgument("fromOnboarding") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val fromOnboarding = backStackEntry.arguments?.getBoolean("fromOnboarding") ?: false
            
            if (fromOnboarding) {
                // Get the onboarding view model from the parent route
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.Onboarding.route)
                }
                val onboardingViewModel: UnifiedOnboardingViewModel = viewModel(parentEntry)
                
                LivestockSelectionWrapper(
                    navController = navController,
                    fromOnboarding = true,
                    onboardingViewModel = onboardingViewModel
                )
            } else {
                // Regular livestock selection from settings
                LivestockSelectionScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        // Phone auth removed - now handled at registration
        
        composable(
            route = Screen.LanguageSelection.route + "?fromOnboarding={fromOnboarding}",
            arguments = listOf(
                navArgument("fromOnboarding") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val fromOnboarding = backStackEntry.arguments?.getBoolean("fromOnboarding") ?: false
            
            if (fromOnboarding) {
                // Get the onboarding view model from the parent route
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.Onboarding.route)
                }
                val onboardingViewModel: UnifiedOnboardingViewModel = viewModel(parentEntry)
                
                LanguageSelectionWrapper(
                    navController = navController,
                    fromOnboarding = true,
                    onboardingViewModel = onboardingViewModel
                )
            } else {
                // Regular language selection from settings
                val context = LocalContext.current
                val preferencesManager = remember { PreferencesManager(context) }
                val userLocation by preferencesManager.userLocation.collectAsState(initial = "")
                
                // Parse location string to find state/region
                val suggestedLanguages = remember(userLocation) {
                    android.util.Log.d("LanguageSelection", "Settings: userLocation = '$userLocation'")
                    
                    if (userLocation.isNotEmpty()) {
                        // First try the whole location string
                        var mapping = LocationLanguageMapper.getLanguagesForLocation(userLocation)
                        android.util.Log.d("LanguageSelection", "Settings: Full location mapping = ${mapping?.primaryLanguages}")
                        
                        if (mapping != null) {
                            mapping.primaryLanguages
                        } else {
                            // Split location string (e.g., "Bangalore, Karnataka, India")
                            val locationParts = userLocation.split(",").map { it.trim() }
                            android.util.Log.d("LanguageSelection", "Settings: locationParts = $locationParts")
                            
                            // Try to find languages based on different parts
                            val languages = mutableSetOf<String>()
                            
                            // Check each part of the location
                            for (part in locationParts) {
                                val partMapping = LocationLanguageMapper.getLanguagesForLocation(part)
                                android.util.Log.d("LanguageSelection", "Settings: Checking part '$part' -> found mapping: ${partMapping?.primaryLanguages}")
                                partMapping?.let {
                                    languages.addAll(it.primaryLanguages)
                                }
                            }
                            
                            android.util.Log.d("LanguageSelection", "Settings: Combined languages = $languages")
                            
                            // If we found languages, return them; otherwise, try partial matching
                            if (languages.isNotEmpty()) {
                                languages.toList()
                            } else {
                                // Try partial matching for common locations
                                when {
                                    userLocation.contains("Bangalore", ignoreCase = true) || 
                                    userLocation.contains("Bengaluru", ignoreCase = true) -> {
                                        android.util.Log.d("LanguageSelection", "Settings: Found Bangalore/Bengaluru")
                                        listOf("en", "kn", "hi")
                                    }
                                    userLocation.contains("Karnataka", ignoreCase = true) -> {
                                        android.util.Log.d("LanguageSelection", "Settings: Found Karnataka")
                                        listOf("en", "kn", "hi")
                                    }
                                    userLocation.contains("Mumbai", ignoreCase = true) -> {
                                        android.util.Log.d("LanguageSelection", "Settings: Found Mumbai")
                                        listOf("en", "mr", "hi")
                                    }
                                    userLocation.contains("Delhi", ignoreCase = true) -> {
                                        android.util.Log.d("LanguageSelection", "Settings: Found Delhi")
                                        listOf("en", "hi", "ur")
                                    }
                                    userLocation.contains("Chennai", ignoreCase = true) -> {
                                        android.util.Log.d("LanguageSelection", "Settings: Found Chennai")
                                        listOf("en", "ta")
                                    }
                                    userLocation.contains("Hyderabad", ignoreCase = true) -> {
                                        android.util.Log.d("LanguageSelection", "Settings: Found Hyderabad")
                                        listOf("en", "te", "hi")
                                    }
                                    userLocation.contains("Kolkata", ignoreCase = true) -> {
                                        android.util.Log.d("LanguageSelection", "Settings: Found Kolkata")
                                        listOf("en", "bn", "hi")
                                    }
                                    else -> {
                                        android.util.Log.d("LanguageSelection", "Settings: No match found for location")
                                        emptyList()
                                    }
                                }
                            }
                        }
                    } else {
                        android.util.Log.d("LanguageSelection", "Settings: Location is empty")
                        emptyList()
                    }
                }
                
                // Get current language and pass regional suggestions as-is
                val currentLanguage = preferencesManager.getSelectedLanguage()
                val settingsViewModel: ApiSettingsViewModel = viewModel()
                
                android.util.Log.d("LanguageSelection", "Current language: $currentLanguage")
                android.util.Log.d("LanguageSelection", "Regional languages: $suggestedLanguages")
                
                LanguageSelectionScreen(
                    navController = navController,
                    settingsViewModel = settingsViewModel,
                    suggestedLanguages = suggestedLanguages,
                    actualRegionalLanguages = suggestedLanguages
                )
            }
        }
        
        composable(Screen.NameSelection.route) {
            NameSelectionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.GenderSelection.route) {
            GenderSelectionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.ForgotPin.route) {
            ForgotPinScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPinReset = {
                    // Navigate back to login after successful PIN reset
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ForgotPin.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.TermsConditions.route + "?showAccept={showAccept}",
            arguments = listOf(
                navArgument("showAccept") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val showAccept = backStackEntry.arguments?.getBoolean("showAccept") ?: false
            
            TermsConditionsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAccept = if (showAccept) {
                    {
                        // Mark terms as accepted and continue
                        navController.popBackStack()
                    }
                } else null,
                showAcceptButton = showAccept
            )
        }
    }
}