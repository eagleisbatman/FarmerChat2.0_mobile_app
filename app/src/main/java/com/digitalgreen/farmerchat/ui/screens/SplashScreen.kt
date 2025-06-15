package com.digitalgreen.farmerchat.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
    onNavigateToOnboarding: () -> Unit,
    onNavigateToChat: () -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsState()
    
    LaunchedEffect(hasCompletedOnboarding) {
        delay(2000) // Show splash for 2 seconds
        if (hasCompletedOnboarding == true) {
            onNavigateToChat()
        } else {
            onNavigateToOnboarding()
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
        }
    }
}