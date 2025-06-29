package com.digitalgreen.farmerchat.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.digitalgreen.farmerchat.ui.screens.PhoneCollectionScreenV2
import com.digitalgreen.farmerchat.ui.screens.LanguageSelectionScreen
import com.digitalgreen.farmerchat.ui.screens.NameSelectionScreen
import com.digitalgreen.farmerchat.ui.screens.GenderSelectionScreen
import com.digitalgreen.farmerchat.ui.screens.RoleSelectionScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Onboarding : Screen("onboarding")
    object Conversations : Screen("conversations") {
        fun createRoute(startNewChat: Boolean = false) = if (startNewChat) "conversations?startNewChat=true" else "conversations"
    }
    object Chat : Screen("chat/{conversationId}") {
        fun createRoute(conversationId: String) = "chat/$conversationId"
    }
    object Settings : Screen("settings")
    object CropSelection : Screen("crop_selection") {
        fun createRoute(fromOnboarding: Boolean = false) = "crop_selection?fromOnboarding=$fromOnboarding"
    }
    object LivestockSelection : Screen("livestock_selection") {
        fun createRoute(fromOnboarding: Boolean = false) = "livestock_selection?fromOnboarding=$fromOnboarding"
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
                onNavigateToChat = { conversationId ->
                    navController.navigate(Screen.Chat.createRoute(conversationId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
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
                LanguageSelectionScreen(navController = navController)
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
    }
}