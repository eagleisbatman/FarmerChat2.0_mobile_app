package com.digitalgreen.farmerchat.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.digitalgreen.farmerchat.ui.screens.ChatScreen
import com.digitalgreen.farmerchat.ui.screens.ConversationsScreen
import com.digitalgreen.farmerchat.ui.screens.OnboardingScreen
import com.digitalgreen.farmerchat.ui.screens.SplashScreen
import com.digitalgreen.farmerchat.ui.screens.SettingsScreen
import com.digitalgreen.farmerchat.ui.screens.CropSelectionScreen
import com.digitalgreen.farmerchat.ui.screens.LivestockSelectionScreen
import com.digitalgreen.farmerchat.ui.screens.PhoneCollectionScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Conversations : Screen("conversations") {
        fun createRoute(startNewChat: Boolean = false) = if (startNewChat) "conversations?startNewChat=true" else "conversations"
    }
    object Chat : Screen("chat/{conversationId}") {
        fun createRoute(conversationId: String) = "chat/$conversationId"
    }
    object Settings : Screen("settings")
    object CropSelection : Screen("crop_selection")
    object LivestockSelection : Screen("livestock_selection")
    object PhoneCollection : Screen("phone_collection")
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
        
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onOnboardingComplete = {
                    // After onboarding, create new conversation and navigate directly to chat
                    navController.navigate(Screen.Conversations.createRoute(startNewChat = true)) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
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
                onNavigateToChat = { conversationId ->
                    navController.navigate(Screen.Chat.createRoute(conversationId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToPhoneAuth = {
                    navController.navigate(Screen.PhoneCollection.route)
                },
                startNewChat = startNewChat
            )
        }
        
        composable(
            Screen.Chat.route,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            ChatScreen(
                conversationId = conversationId,
                onNavigateBack = {
                    // Navigate to conversations without startNewChat parameter
                    navController.navigate(Screen.Conversations.route) {
                        popUpTo(Screen.Chat.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToNewChat = {
                    navController.navigate(Screen.Conversations.createRoute(startNewChat = true)) {
                        popUpTo(Screen.Chat.route) { inclusive = true }
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
                }
            )
        }
        
        composable(Screen.CropSelection.route) {
            CropSelectionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCropsSelected = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.LivestockSelection.route) {
            LivestockSelectionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLivestockSelected = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.PhoneCollection.route) {
            PhoneCollectionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onComplete = {
                    navController.popBackStack()
                },
                onSkip = {
                    navController.popBackStack()
                }
            )
        }
    }
}