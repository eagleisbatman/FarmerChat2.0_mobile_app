package com.digitalgreen.farmerchat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.digitalgreen.farmerchat.ui.components.FarmerChatAppBar
import com.digitalgreen.farmerchat.ui.components.LanguageSelectionView
import com.digitalgreen.farmerchat.ui.components.EnhancedLanguageSelectionView
import com.digitalgreen.farmerchat.ui.components.localizedString
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionScreen(
    navController: NavController,
    settingsViewModel: ApiSettingsViewModel = viewModel(),
    onLanguageSelected: ((String) -> Unit)? = null,
    suggestedLanguages: List<String> = emptyList()
) {
    val settingsState by settingsViewModel.settingsState.collectAsState()
    
    Scaffold(
        topBar = {
            FarmerChatAppBar(
                title = localizedString(StringKey.SELECT_LANGUAGE),
                onBackClick = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Language explanation card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignSystem.Spacing.md),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(DesignSystem.Spacing.md)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
                        Text(
                            text = localizedString(StringKey.LANGUAGE_SUBTITLE),
                            fontSize = DesignSystem.Typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
                    Text(
                        text = localizedString(StringKey.LANGUAGE_BENEFIT),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
            
            EnhancedLanguageSelectionView(
                selectedLanguage = settingsState.currentLanguage,
                onLanguageSelected = { languageCode ->
                    if (onLanguageSelected != null) {
                        // Custom callback (from onboarding)
                        onLanguageSelected(languageCode)
                    } else {
                        // Default behavior (from settings)
                        settingsViewModel.updateLanguage(languageCode)
                        navController.navigateUp()
                    }
                },
                suggestedLanguages = suggestedLanguages,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}