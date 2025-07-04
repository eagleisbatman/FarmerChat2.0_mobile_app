package com.digitalgreen.farmerchat.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digitalgreen.farmerchat.R
import com.digitalgreen.farmerchat.ui.components.localizedString
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToChat: (conversationId: String?) -> Unit,
    onNavigateToConversations: () -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsState()
    val authState by viewModel.authenticationState.collectAsState()
    val navigationDecision by viewModel.navigationDecision.collectAsState()
    
    // Handle navigation based on authentication and onboarding state
    LaunchedEffect(authState, hasCompletedOnboarding, navigationDecision) {
        when (authState) {
            is SplashViewModel.AuthState.NotAuthenticated -> {
                delay(1500) // Show splash briefly
                onNavigateToLogin()
            }
            is SplashViewModel.AuthState.Authenticated -> {
                if (hasCompletedOnboarding != null) {
                    delay(1500) // Show splash briefly after successful auth
                    
                    if (hasCompletedOnboarding == true) {
                        // Use smart navigation decision
                        navigationDecision?.let { decision ->
                            if (decision.shouldGoToChat && decision.conversationId != null) {
                                onNavigateToChat(decision.conversationId)
                            } else {
                                onNavigateToConversations()
                            }
                        } ?: onNavigateToConversations() // Fallback to conversations if no decision
                    } else {
                        onNavigateToOnboarding()
                    }
                }
            }
            is SplashViewModel.AuthState.Loading -> {
                // Stay on splash screen
            }
            is SplashViewModel.AuthState.Error -> {
                // Show error and navigate to login after delay
                delay(2000)
                onNavigateToLogin()
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignSystem.Colors.Primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App logo placeholder
            Box(
                modifier = Modifier
                    .size(DesignSystem.IconSize.splash)
                    .background(Color.White, shape = MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🌾",
                    fontSize = DesignSystem.Typography.displayLarge
                )
            }
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.lg))
            
            Text(
                text = localizedString(StringKey.APP_NAME),
                fontSize = DesignSystem.Typography.headlineLarge,
                fontWeight = DesignSystem.Typography.Weight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
            
            Text(
                text = localizedString(StringKey.EMPOWERING_FARMERS_WITH_AI),
                fontSize = DesignSystem.Typography.bodyLarge,
                color = Color.White.copy(alpha = DesignSystem.Opacity.high),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
            
            // Show loading or error state
            when (authState) {
                is SplashViewModel.AuthState.Loading -> {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                is SplashViewModel.AuthState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = DesignSystem.Spacing.lg)
                    ) {
                        Text(
                            text = localizedString(StringKey.CANNOT_CONNECT_SERVER),
                            fontSize = DesignSystem.Typography.bodyLarge,
                            fontWeight = DesignSystem.Typography.Weight.Medium,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
                        Text(
                            text = localizedString(StringKey.ENSURE_BACKEND_RUNNING),
                            fontSize = DesignSystem.Typography.bodyMedium,
                            color = Color.White.copy(alpha = DesignSystem.Opacity.high),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.lg))
                        Button(
                            onClick = { viewModel.retryAuthentication() },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = DesignSystem.Colors.Primary
                            )
                        ) {
                            Text(localizedString(StringKey.RETRY), fontWeight = DesignSystem.Typography.Weight.Medium)
                        }
                    }
                }
                is SplashViewModel.AuthState.Authenticated -> {
                    // Show nothing extra when authenticated
                }
                is SplashViewModel.AuthState.NotAuthenticated -> {
                    // Show nothing extra when not authenticated
                }
            }
        }
    }
}