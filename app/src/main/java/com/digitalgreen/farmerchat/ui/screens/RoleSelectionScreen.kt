package com.digitalgreen.farmerchat.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digitalgreen.farmerchat.ui.components.FarmerChatAppBar
import com.digitalgreen.farmerchat.ui.components.localizedString
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.ui.theme.secondaryTextColor
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionScreen(
    onNavigateBack: () -> Unit,
    viewModel: ApiSettingsViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as android.app.Application
        )
    )
) {
    val settingsState by viewModel.settingsState.collectAsState()
    var selectedRole by remember { mutableStateOf(settingsState.userRole ?: "") }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }
    
    LaunchedEffect(settingsState.userRole) {
        selectedRole = settingsState.userRole ?: ""
    }
    
    Scaffold(
        topBar = {
            FarmerChatAppBar(
                title = localizedString(StringKey.SELECT_ROLE),
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(DesignSystem.Spacing.md)
        ) {
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
            
            // Icon with background - beautiful onboarding style
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = DesignSystem.Spacing.xl),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Work,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Text(
                text = localizedString(StringKey.SELECT_ROLE),
                fontSize = DesignSystem.Typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = DesignSystem.Spacing.md)
            )
            
            Text(
                text = localizedString(StringKey.ROLE_SUBTITLE),
                fontSize = DesignSystem.Typography.bodyLarge,
                color = secondaryTextColor(),
                modifier = Modifier.padding(bottom = DesignSystem.Spacing.lg)
            )
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
            
            // Role options
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedRole = "farmer" },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedRole == "farmer") {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (selectedRole == "farmer") DesignSystem.Spacing.xs else 1.dp
                ),
                border = if (selectedRole == "farmer") {
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                } else {
                    null
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignSystem.Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = localizedString(StringKey.FARMER),
                            fontSize = DesignSystem.Typography.titleMedium,
                            fontWeight = if (selectedRole == "farmer") FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedRole == "farmer") {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                    if (selectedRole == "farmer") {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(DesignSystem.IconSize.medium)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = localizedString(StringKey.SELECTED),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .padding(2.dp)
                                    .fillMaxSize()
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedRole = "extension_worker" },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedRole == "extension_worker") {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (selectedRole == "extension_worker") DesignSystem.Spacing.xs else 1.dp
                ),
                border = if (selectedRole == "extension_worker") {
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                } else {
                    null
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignSystem.Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = localizedString(StringKey.EXTENSION_WORKER),
                            fontSize = DesignSystem.Typography.titleMedium,
                            fontWeight = if (selectedRole == "extension_worker") FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedRole == "extension_worker") {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                    if (selectedRole == "extension_worker") {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(DesignSystem.IconSize.medium)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = localizedString(StringKey.SELECTED),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .padding(2.dp)
                                    .fillMaxSize()
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.updateUserRole(selectedRole)
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = selectedRole.isNotEmpty(),
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