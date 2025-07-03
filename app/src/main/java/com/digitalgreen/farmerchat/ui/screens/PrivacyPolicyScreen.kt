package com.digitalgreen.farmerchat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.digitalgreen.farmerchat.ui.components.FarmerChatAppBar
import com.digitalgreen.farmerchat.ui.components.localizedString
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            FarmerChatAppBar(
                title = localizedString(StringKey.PRIVACY_POLICY),
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(DesignSystem.Spacing.lg)
        ) {
            Text(
                text = localizedString(StringKey.PRIVACY_POLICY),
                fontSize = DesignSystem.Typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = DesignSystem.Spacing.md)
            )
            
            Text(
                text = localizedString(StringKey.PRIVACY_LAST_UPDATED, java.time.LocalDate.now().toString()),
                fontSize = DesignSystem.Typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = DesignSystem.Spacing.lg)
            )
            
            // Introduction
            PrivacySection(
                title = localizedString(StringKey.PRIVACY_INTRODUCTION_TITLE),
                content = localizedString(StringKey.PRIVACY_INTRODUCTION_CONTENT)
            )
            
            // Information We Collect
            PrivacySection(
                title = localizedString(StringKey.PRIVACY_INFO_COLLECT_TITLE),
                content = localizedString(StringKey.PRIVACY_INFO_COLLECT_CONTENT)
            )
            
            // How We Use Your Information
            PrivacySection(
                title = localizedString(StringKey.PRIVACY_HOW_USE_TITLE),
                content = localizedString(StringKey.PRIVACY_HOW_USE_CONTENT)
            )
            
            // Data Storage and Security
            PrivacySection(
                title = localizedString(StringKey.PRIVACY_DATA_STORAGE_TITLE),
                content = localizedString(StringKey.PRIVACY_DATA_STORAGE_CONTENT)
            )
            
            // Data Sharing
            PrivacySection(
                title = localizedString(StringKey.PRIVACY_DATA_SHARING_TITLE),
                content = localizedString(StringKey.PRIVACY_DATA_SHARING_CONTENT)
            )
            
            // Your Rights
            PrivacySection(
                title = localizedString(StringKey.PRIVACY_YOUR_RIGHTS_TITLE),
                content = localizedString(StringKey.PRIVACY_YOUR_RIGHTS_CONTENT)
            )
            
            // Contact Information
            PrivacySection(
                title = localizedString(StringKey.PRIVACY_CONTACT_TITLE),
                content = localizedString(StringKey.PRIVACY_CONTACT_CONTENT)
            )
        }
    }
}

@Composable
private fun PrivacySection(
    title: String,
    content: String
) {
    Column(
        modifier = Modifier.padding(bottom = DesignSystem.Spacing.lg)
    ) {
        Text(
            text = title,
            fontSize = DesignSystem.Typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = DesignSystem.Spacing.sm)
        )
        Text(
            text = content,
            fontSize = DesignSystem.Typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}