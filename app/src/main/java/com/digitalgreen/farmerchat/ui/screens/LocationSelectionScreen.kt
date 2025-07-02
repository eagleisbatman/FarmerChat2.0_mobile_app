package com.digitalgreen.farmerchat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digitalgreen.farmerchat.ui.components.FarmerChatAppBar
import com.digitalgreen.farmerchat.ui.components.localizedString
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.ui.theme.secondaryTextColor
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSelectionScreen(
    onNavigateBack: () -> Unit,
    viewModel: ApiSettingsViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as android.app.Application
        )
    )
) {
    val settingsState by viewModel.settingsState.collectAsState()
    var location by remember { mutableStateOf(settingsState.userLocation) }
    val coroutineScope = rememberCoroutineScope()
    var isDetectingLocation by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }
    
    LaunchedEffect(settingsState.userLocation) {
        location = settingsState.userLocation
    }
    
    Scaffold(
        topBar = {
            FarmerChatAppBar(
                title = localizedString(StringKey.LOCATION),
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .testTag("locationselectionColumn1")
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(DesignSystem.Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
            
            // Icon with background - beautiful onboarding style
            Surface(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
            
            Text(
                text = localizedString(StringKey.WHERE_LOCATED),
                fontSize = DesignSystem.Typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = DesignSystem.Spacing.md)
            )
            
            Text(
                text = localizedString(StringKey.LOCATION_SUBTITLE),
                fontSize = DesignSystem.Typography.bodyLarge,
                color = secondaryTextColor(),
                modifier = Modifier.padding(bottom = DesignSystem.Spacing.lg)
            )
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
            
            // GPS Detection Button - prominent like in onboarding
            FilledTonalButton(
                onClick = {
                    coroutineScope.launch {
                        isDetectingLocation = true
                        // TODO: Implement actual GPS detection
                        kotlinx.coroutines.delay(2000) // Simulate detection
                        isDetectingLocation = false
                    }
                },
                modifier = Modifier
                    .testTag("detectLocationButton")
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isDetectingLocation
            ) {
                if (isDetectingLocation) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
                    Text(localizedString(StringKey.GETTING_LOCATION))
                } else {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
                    Text(
                        localizedString(StringKey.DETECT_MY_LOCATION),
                        fontSize = DesignSystem.Typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.lg))
            
            // Or divider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = DesignSystem.Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = localizedString(StringKey.OR_ENTER_MANUALLY),
                    modifier = Modifier.padding(horizontal = DesignSystem.Spacing.md),
                    color = secondaryTextColor(),
                    fontSize = DesignSystem.Typography.bodyMedium
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
            
            // Card for input field - matching login/register screen style
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignSystem.Spacing.lg)
                ) {
                    Text(
                        text = localizedString(StringKey.LOCATION),
                        fontSize = DesignSystem.Typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = DesignSystem.Spacing.sm)
                    )
                    
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        placeholder = { 
                            Text(
                                localizedString(StringKey.LOCATION_PLACEHOLDER),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            ) 
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .testTag("locationInput")
                            .fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.updateUserLocation(location)
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = location.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    localizedString(StringKey.SAVE),
                    fontSize = DesignSystem.Typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
        }
    }
}