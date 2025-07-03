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
                text = localizedString(StringKey.TERMS_CONDITIONS),
                fontSize = DesignSystem.Typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = DesignSystem.Spacing.md)
            )
            
            Text(
                text = localizedString(StringKey.TERMS_LAST_UPDATED, java.time.LocalDate.now().toString()),
                fontSize = DesignSystem.Typography.bodySmall,
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
                        text = localizedString(StringKey.TERMS_READ_TO_CONTINUE),
                        fontSize = DesignSystem.Typography.bodyMedium,
                        modifier = Modifier.padding(DesignSystem.Spacing.md)
                    )
                }
            }
            
            // Acceptance of Terms
            TermsSection(
                title = localizedString(StringKey.TERMS_ACCEPTANCE_TITLE),
                content = localizedString(StringKey.TERMS_ACCEPTANCE_CONTENT)
            )
            
            // Description of Service
            TermsSection(
                title = localizedString(StringKey.TERMS_SERVICE_DESC_TITLE),
                content = localizedString(StringKey.TERMS_SERVICE_DESC_CONTENT)
            )
            
            // User Accounts
            TermsSection(
                title = localizedString(StringKey.TERMS_USER_ACCOUNTS_TITLE),
                content = localizedString(StringKey.TERMS_USER_ACCOUNTS_CONTENT)
            )
            
            // Acceptable Use
            TermsSection(
                title = localizedString(StringKey.TERMS_ACCEPTABLE_USE_TITLE),
                content = localizedString(StringKey.TERMS_ACCEPTABLE_USE_CONTENT)
            )
            
            // Content and Advice
            TermsSection(
                title = localizedString(StringKey.TERMS_AGRI_DISCLAIMER_TITLE),
                content = localizedString(StringKey.TERMS_AGRI_DISCLAIMER_CONTENT)
            )
            
            // Intellectual Property
            TermsSection(
                title = localizedString(StringKey.TERMS_INTELLECTUAL_TITLE),
                content = localizedString(StringKey.TERMS_INTELLECTUAL_CONTENT)
            )
            
            // Limitation of Liability
            TermsSection(
                title = localizedString(StringKey.TERMS_LIABILITY_TITLE),
                content = localizedString(StringKey.TERMS_LIABILITY_CONTENT)
            )
            
            // Privacy
            TermsSection(
                title = localizedString(StringKey.TERMS_PRIVACY_TITLE),
                content = localizedString(StringKey.TERMS_PRIVACY_CONTENT)
            )
            
            // Termination
            TermsSection(
                title = localizedString(StringKey.TERMS_TERMINATION_TITLE),
                content = localizedString(StringKey.TERMS_TERMINATION_CONTENT)
            )
            
            // Changes to Terms
            TermsSection(
                title = localizedString(StringKey.TERMS_CHANGES_TITLE),
                content = localizedString(StringKey.TERMS_CHANGES_CONTENT)
            )
            
            // Governing Law
            TermsSection(
                title = localizedString(StringKey.TERMS_GOVERNING_LAW_TITLE),
                content = localizedString(StringKey.TERMS_GOVERNING_LAW_CONTENT)
            )
            
            // Contact Information
            TermsSection(
                title = localizedString(StringKey.TERMS_CONTACT_TITLE),
                content = localizedString(StringKey.TERMS_CONTACT_CONTENT)
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