package com.digitalgreen.farmerchat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.digitalgreen.farmerchat.ui.components.FarmerChatAppBar
import com.digitalgreen.farmerchat.ui.components.localizedString
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsConditionsScreen(
    onNavigateBack: () -> Unit,
    onAccept: (() -> Unit)? = null,
    showAcceptButton: Boolean = false
) {
    var isAcceptEnabled by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    
    // Enable accept button when scrolled to bottom
    LaunchedEffect(scrollState.value) {
        if (showAcceptButton) {
            isAcceptEnabled = scrollState.value >= scrollState.maxValue - 100
        }
    }
    
    Scaffold(
        topBar = {
            FarmerChatAppBar(
                title = localizedString(StringKey.TERMS_CONDITIONS),
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            if (showAcceptButton) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignSystem.Spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.md)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(localizedString(StringKey.DECLINE_TERMS))
                        }
                        Button(
                            onClick = { onAccept?.invoke() },
                            modifier = Modifier.weight(1f),
                            enabled = isAcceptEnabled
                        ) {
                            Text(localizedString(StringKey.ACCEPT_TERMS))
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(DesignSystem.Spacing.lg)
        ) {
            Text(
                text = "Terms & Conditions",
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
            
            if (showAcceptButton && !isAcceptEnabled) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = DesignSystem.Spacing.md),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "Please read the entire document to continue",
                        style = DesignSystem.Typography.bodyMedium,
                        modifier = Modifier.padding(DesignSystem.Spacing.md)
                    )
                }
            }
            
            // Acceptance of Terms
            TermsSection(
                title = "1. Acceptance of Terms",
                content = "By downloading, installing, or using FarmerChat, you agree to be bound by these Terms & Conditions. If you do not agree to these terms, please do not use the application."
            )
            
            // Description of Service
            TermsSection(
                title = "2. Description of Service",
                content = """
                    FarmerChat provides:
                    • AI-powered agricultural advice and recommendations
                    • Personalized farming guidance based on your location and crops
                    • Access to agricultural best practices
                    • Voice-based interaction capabilities
                    • Multi-language support
                """.trimIndent()
            )
            
            // User Accounts
            TermsSection(
                title = "3. User Accounts",
                content = """
                    • You must provide accurate information during registration
                    • You are responsible for maintaining the confidentiality of your PIN
                    • You must notify us immediately of any unauthorized access
                    • One account per phone number is allowed
                    • You must be at least 13 years old to use this service
                """.trimIndent()
            )
            
            // Acceptable Use
            TermsSection(
                title = "4. Acceptable Use",
                content = """
                    You agree not to:
                    • Use the app for any illegal purposes
                    • Share false or misleading information
                    • Attempt to hack or disrupt the service
                    • Use automated systems to access the app
                    • Impersonate others or provide false information
                    • Use the service to harm others or their property
                """.trimIndent()
            )
            
            // Content and Advice
            TermsSection(
                title = "5. Agricultural Advice Disclaimer",
                content = """
                    • FarmerChat provides general agricultural guidance
                    • Advice should be adapted to your local conditions
                    • We are not responsible for crop failures or losses
                    • Always consult local experts for critical decisions
                    • Weather and market information is provided as-is
                    • Results may vary based on implementation
                """.trimIndent()
            )
            
            // Intellectual Property
            TermsSection(
                title = "6. Intellectual Property",
                content = """
                    • All content in FarmerChat is owned by Digital Green
                    • You may not copy, modify, or distribute our content
                    • Your conversations remain your property
                    • We may use anonymized data to improve our service
                    • Feedback you provide becomes our property
                """.trimIndent()
            )
            
            // Limitation of Liability
            TermsSection(
                title = "7. Limitation of Liability",
                content = """
                    Digital Green shall not be liable for:
                    • Indirect, incidental, or consequential damages
                    • Loss of profits or crop yields
                    • Damages from service interruptions
                    • Third-party actions or content
                    • Force majeure events
                    
                    Our total liability shall not exceed $100 USD.
                """.trimIndent()
            )
            
            // Privacy
            TermsSection(
                title = "8. Privacy",
                content = "Your use of FarmerChat is also governed by our Privacy Policy. Please review our Privacy Policy to understand our practices."
            )
            
            // Termination
            TermsSection(
                title = "9. Termination",
                content = """
                    • You may terminate your account at any time
                    • We may suspend or terminate accounts that violate these terms
                    • Upon termination, your data may be deleted
                    • Provisions that should survive termination will remain in effect
                """.trimIndent()
            )
            
            // Changes to Terms
            TermsSection(
                title = "10. Changes to Terms",
                content = "We reserve the right to modify these terms at any time. We will notify you of significant changes through the app. Continued use after changes constitutes acceptance."
            )
            
            // Governing Law
            TermsSection(
                title = "11. Governing Law",
                content = "These Terms shall be governed by the laws of the jurisdiction in which Digital Green operates, without regard to conflict of law provisions."
            )
            
            // Contact Information
            TermsSection(
                title = "12. Contact Information",
                content = """
                    For questions about these Terms & Conditions:
                    
                    Digital Green
                    Email: legal@digitalgreen.org
                    Website: www.digitalgreen.org
                """.trimIndent()
            )
            
            // Additional spacing at bottom for accept button visibility
            if (showAcceptButton) {
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
            }
        }
    }
}

@Composable
private fun TermsSection(
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