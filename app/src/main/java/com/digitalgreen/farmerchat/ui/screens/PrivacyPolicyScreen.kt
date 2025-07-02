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
                text = "Privacy Policy",
                style = DesignSystem.Typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = DesignSystem.Spacing.md)
            )
            
            Text(
                text = "Last updated: ${java.time.LocalDate.now()}",
                style = DesignSystem.Typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = DesignSystem.Spacing.lg)
            )
            
            // Introduction
            PrivacySection(
                title = "Introduction",
                content = "FarmerChat is committed to protecting your privacy. This Privacy Policy explains how we collect, use, disclose, and safeguard your information when you use our mobile application."
            )
            
            // Information We Collect
            PrivacySection(
                title = "Information We Collect",
                content = """
                    We collect information you provide directly to us, such as:
                    • Phone number and PIN for authentication
                    • Name and location
                    • Selected crops and livestock
                    • Gender and role information
                    • Chat messages and conversations
                    • Voice recordings (temporarily for transcription)
                """.trimIndent()
            )
            
            // How We Use Your Information
            PrivacySection(
                title = "How We Use Your Information",
                content = """
                    We use the information we collect to:
                    • Provide personalized agricultural advice
                    • Improve our AI responses based on your context
                    • Authenticate your account
                    • Send relevant notifications
                    • Analyze usage patterns to improve the app
                    • Comply with legal obligations
                """.trimIndent()
            )
            
            // Data Storage and Security
            PrivacySection(
                title = "Data Storage and Security",
                content = """
                    • Your data is stored securely in encrypted databases
                    • PINs are hashed using industry-standard encryption
                    • Voice recordings are processed and immediately deleted
                    • We implement appropriate security measures to protect your data
                    • Data is stored in secure cloud servers with regular backups
                """.trimIndent()
            )
            
            // Data Sharing
            PrivacySection(
                title = "Data Sharing",
                content = """
                    We do not sell, trade, or rent your personal information to third parties. We may share your information only in the following situations:
                    • With your consent
                    • To comply with legal obligations
                    • To protect our rights and safety
                    • With service providers who assist in app operations
                """.trimIndent()
            )
            
            // Your Rights
            PrivacySection(
                title = "Your Rights",
                content = """
                    You have the right to:
                    • Access your personal data
                    • Correct inaccurate data
                    • Delete your account and data
                    • Export your data
                    • Opt-out of certain data collection
                    • Contact us with privacy concerns
                """.trimIndent()
            )
            
            // Contact Information
            PrivacySection(
                title = "Contact Us",
                content = """
                    If you have questions about this Privacy Policy, please contact us at:
                    
                    Digital Green
                    Email: privacy@digitalgreen.org
                    Website: www.digitalgreen.org
                """.trimIndent()
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
            style = DesignSystem.Typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = DesignSystem.Spacing.sm)
        )
        Text(
            text = content,
            style = DesignSystem.Typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}