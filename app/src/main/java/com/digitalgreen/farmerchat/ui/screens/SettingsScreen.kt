package com.digitalgreen.farmerchat.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.digitalgreen.farmerchat.ui.components.localizedString
import com.digitalgreen.farmerchat.ui.components.currentLanguage
import com.digitalgreen.farmerchat.ui.components.FarmerChatAppBar
import com.digitalgreen.farmerchat.ui.components.SettingsSectionSkeleton
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import com.digitalgreen.farmerchat.utils.PreferencesManager
import com.digitalgreen.farmerchat.utils.StringProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToCropSelection: () -> Unit,
    onNavigateToLivestockSelection: () -> Unit,
    onNavigateToLanguageSelection: () -> Unit,
    onNavigateToNameSelection: () -> Unit,
    onNavigateToGenderSelection: () -> Unit,
    onNavigateToRoleSelection: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToTermsConditions: () -> Unit,
    viewModel: ApiSettingsViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val context = LocalContext.current
    val settingsState by viewModel.settingsState.collectAsState()
    var showAboutDialog by remember { mutableStateOf(false) }
    var showDeleteDataDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showResponseLengthDialog by remember { mutableStateOf(false) }
    var showResetOnboardingDialog by remember { mutableStateOf(false) }
    var editableLocation by remember { mutableStateOf("") }
    
    // Update editable values when settings state changes
    LaunchedEffect(settingsState) {
        editableLocation = settingsState.userLocation
    }
    
    // Initialize the ViewModel when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }
    
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            FarmerChatAppBar(
                title = localizedString(StringKey.SETTINGS),
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = DesignSystem.Spacing.sm)
        ) {
            if (settingsState.isLoading) {
                // Show skeleton loading for all sections
                item {
                    SettingsSectionSkeleton(itemCount = 6) // Profile section has 6 items
                }
                item {
                    SettingsSectionSkeleton(itemCount = 3) // Preferences section has 3 items
                }
                item {
                    SettingsSectionSkeleton(itemCount = 2) // AI Settings section has 2 items
                }
                item {
                    SettingsSectionSkeleton(itemCount = 2) // Data & Privacy section has 2 items
                }
                item {
                    SettingsSectionSkeleton(itemCount = 4) // About section has 4 items
                }
            } else {
                // Show actual content when loaded
                // Profile Section
                item {
                    SettingsSection(title = localizedString(StringKey.PROFILE)) {
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = localizedString(StringKey.NAME),
                        subtitle = settingsState.userName,
                        onClick = { onNavigateToNameSelection() }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.LocationOn,
                        title = localizedString(StringKey.LOCATION),
                        subtitle = settingsState.userLocation,
                        onClick = { 
                            editableLocation = settingsState.userLocation
                            showLocationDialog = true 
                        }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Work,
                        title = localizedString(StringKey.SELECT_ROLE),
                        subtitle = when(settingsState.userRole) {
                            "farmer" -> localizedString(StringKey.FARMER)
                            "extension_worker" -> localizedString(StringKey.EXTENSION_WORKER)
                            else -> ""
                        },
                        onClick = { onNavigateToRoleSelection() }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = localizedString(StringKey.SELECT_GENDER),
                        subtitle = when(settingsState.userGender) {
                            "male" -> localizedString(StringKey.MALE)
                            "female" -> localizedString(StringKey.FEMALE)
                            "other" -> localizedString(StringKey.OTHER)
                            else -> ""
                        },
                        onClick = { onNavigateToGenderSelection() }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Grass,
                        title = localizedString(StringKey.CROPS),
                        subtitle = "${settingsState.selectedCrops.size} ${localizedString(StringKey.SELECTED)}",
                        onClick = { onNavigateToCropSelection() }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Pets,
                        title = localizedString(StringKey.LIVESTOCK),
                        subtitle = "${settingsState.selectedLivestock.size} ${localizedString(StringKey.SELECTED)}",
                        onClick = { onNavigateToLivestockSelection() }
                    )
                }
            }
            
            // Preferences Section
            item {
                SettingsSection(title = localizedString(StringKey.PREFERENCES)) {
                    SettingsItem(
                        icon = Icons.Default.Language,
                        title = localizedString(StringKey.LANGUAGE),
                        subtitle = settingsState.currentLanguageName,
                        onClick = { onNavigateToLanguageSelection() }
                    )
                    
                    SettingsSwitchItem(
                        icon = Icons.Default.RecordVoiceOver,
                        title = localizedString(StringKey.VOICE_RESPONSES),
                        subtitle = localizedString(StringKey.VOICE_RESPONSES_DESC),
                        checked = settingsState.voiceResponsesEnabled,
                        onCheckedChange = { viewModel.toggleVoiceResponses() }
                    )
                    
                    SettingsSwitchItem(
                        icon = Icons.Default.Mic,
                        title = localizedString(StringKey.VOICE_INPUT),
                        subtitle = localizedString(StringKey.VOICE_INPUT_DESC),
                        checked = settingsState.voiceInputEnabled,
                        onCheckedChange = { viewModel.toggleVoiceInput() }
                    )
                }
            }
            
            // AI Settings Section
            item {
                SettingsSection(title = localizedString(StringKey.AI_SETTINGS)) {
                    SettingsItem(
                        icon = Icons.Default.Speed,
                        title = localizedString(StringKey.RESPONSE_LENGTH),
                        subtitle = when(settingsState.responseLength) {
                            "concise" -> localizedString(StringKey.CONCISE)
                            "comprehensive" -> localizedString(StringKey.COMPREHENSIVE)
                            else -> localizedString(StringKey.DETAILED)
                        },
                        onClick = { showResponseLengthDialog = true }
                    )
                    
                    SettingsSwitchItem(
                        icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                        title = localizedString(StringKey.FORMATTED_RESPONSES),
                        subtitle = localizedString(StringKey.FORMATTED_RESPONSES_DESC),
                        checked = settingsState.formattedResponsesEnabled,
                        onCheckedChange = { viewModel.toggleFormattedResponses() }
                    )
                }
            }
            
            // Data & Privacy Section
            item {
                SettingsSection(title = localizedString(StringKey.DATA_PRIVACY)) {
                    SettingsItem(
                        icon = Icons.Default.CloudDownload,
                        title = localizedString(StringKey.EXPORT_DATA),
                        subtitle = localizedString(StringKey.EXPORT_DATA_DESC),
                        onClick = { 
                            coroutineScope.launch {
                                viewModel.exportUserData(context)
                            }
                        }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Delete,
                        title = localizedString(StringKey.DELETE_ALL_DATA),
                        subtitle = localizedString(StringKey.DELETE_ALL_DATA_DESC),
                        textColor = MaterialTheme.colorScheme.error,
                        onClick = { showDeleteDataDialog = true }
                    )
                }
            }
            
            // About Section
            item {
                SettingsSection(title = localizedString(StringKey.ABOUT)) {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = localizedString(StringKey.APP_VERSION),
                        subtitle = settingsState.appVersion,
                        onClick = { showAboutDialog = true }
                    )
                    
                    SettingsItem(
                        icon = Icons.AutoMirrored.Filled.Help,
                        title = localizedString(StringKey.HELP_FEEDBACK),
                        subtitle = localizedString(StringKey.HELP_FEEDBACK_DESC),
                        onClick = { /* TODO: Open help/feedback */ }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.PrivacyTip,
                        title = localizedString(StringKey.PRIVACY_POLICY),
                        subtitle = localizedString(StringKey.VIEW_PRIVACY_POLICY),
                        onClick = {
                            onNavigateToPrivacyPolicy()
                        }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Article,
                        title = localizedString(StringKey.TERMS_CONDITIONS),
                        subtitle = localizedString(StringKey.VIEW_TERMS),
                        onClick = {
                            onNavigateToTermsConditions()
                        }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Book,
                        title = localizedString(StringKey.RESET_ONBOARDING),
                        subtitle = localizedString(StringKey.RESET_ONBOARDING_DESC),
                        onClick = {
                            showResetOnboardingDialog = true
                        }
                    )
                    
                    SettingsItem(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        title = localizedString(StringKey.LOGOUT),
                        subtitle = localizedString(StringKey.LOGOUT_DESC),
                        onClick = {
                            viewModel.logout()
                            onNavigateToOnboarding()
                        }
                    )
                }
            }
            } // Close else block for skeleton loading
        }
    }
    
    
    // About Dialog
    if (showAboutDialog) {
        AboutDialog(
            appVersion = settingsState.appVersion,
            onDismiss = { showAboutDialog = false }
        )
    }
    
    // Delete Data Confirmation Dialog
    if (showDeleteDataDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDataDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(localizedString(StringKey.DELETE_ALL_DATA)) },
            text = { Text(localizedString(StringKey.DELETE_DATA_CONFIRM)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        // TODO: Implement delete all data
                        showDeleteDataDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(localizedString(StringKey.DELETE))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDataDialog = false }) {
                    Text(localizedString(StringKey.CANCEL))
                }
            }
        )
    }
    
    
    // Location Edit Dialog
    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text(localizedString(StringKey.EDIT_LOCATION)) },
            text = {
                Column {
                    TextField(
                        value = editableLocation,
                        onValueChange = { editableLocation = it },
                        placeholder = { Text(localizedString(StringKey.ENTER_LOCATION)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
                    TextButton(
                        onClick = {
                            // TODO: Implement GPS location detection
                            showLocationDialog = false
                        }
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Spacer(modifier = Modifier.width(DesignSystem.Spacing.xs))
                        Text(localizedString(StringKey.DETECT_MY_LOCATION))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateUserLocation(editableLocation)
                        showLocationDialog = false
                    }
                ) {
                    Text(localizedString(StringKey.OK))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text(localizedString(StringKey.CANCEL))
                }
            }
        )
    }
    
    // Response Length Dialog
    if (showResponseLengthDialog) {
        AlertDialog(
            onDismissRequest = { showResponseLengthDialog = false },
            title = { Text(localizedString(StringKey.RESPONSE_LENGTH)) },
            text = {
                Column {
                    val responseLengths = listOf("concise", "medium", "comprehensive")
                    responseLengths.forEach { length ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateResponseLength(length)
                                    showResponseLengthDialog = false
                                }
                                .padding(vertical = DesignSystem.Spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settingsState.responseLength == length,
                                onClick = {
                                    viewModel.updateResponseLength(length)
                                    showResponseLengthDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
                            Text(
                                text = when(length) {
                                    "concise" -> localizedString(StringKey.CONCISE)
                                    "comprehensive" -> localizedString(StringKey.COMPREHENSIVE)
                                    else -> localizedString(StringKey.DETAILED)
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showResponseLengthDialog = false }) {
                    Text(localizedString(StringKey.CLOSE))
                }
            }
        )
    }
    
    // Reset Onboarding Confirmation Dialog
    if (showResetOnboardingDialog) {
        AlertDialog(
            onDismissRequest = { showResetOnboardingDialog = false },
            icon = {
                Icon(
                    Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text(localizedString(StringKey.RESET_ONBOARDING)) },
            text = { Text(localizedString(StringKey.RESET_ONBOARDING_CONFIRM)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.resetOnboarding()
                            showResetOnboardingDialog = false
                            onNavigateToOnboarding()
                        }
                    }
                ) {
                    Text(localizedString(StringKey.RESET))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetOnboardingDialog = false }) {
                    Text(localizedString(StringKey.CANCEL))
                }
            }
        )
    }
    
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = DesignSystem.Typography.labelLarge,
            fontWeight = DesignSystem.Typography.Weight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(
                horizontal = DesignSystem.Spacing.md, 
                vertical = DesignSystem.Spacing.sm
            )
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = DesignSystem.Spacing.md, 
                    vertical = DesignSystem.Spacing.xs
                ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignSystem.Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (textColor == MaterialTheme.colorScheme.error) textColor else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(DesignSystem.IconSize.medium)
            )
            
            Spacer(modifier = Modifier.width(DesignSystem.Spacing.md))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = DesignSystem.Typography.bodyLarge,
                    fontWeight = DesignSystem.Typography.Weight.Normal,
                    color = textColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = DesignSystem.Typography.bodyMedium,
                        color = textColor.copy(alpha = DesignSystem.Opacity.high)
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignSystem.Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(DesignSystem.IconSize.medium)
            )
            
            Spacer(modifier = Modifier.width(DesignSystem.Spacing.md))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = DesignSystem.Typography.bodyLarge,
                    fontWeight = DesignSystem.Typography.Weight.Normal
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = DesignSystem.Typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.8f),
        title = { Text(localizedString(StringKey.SELECT_LANGUAGE)) },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(localizedString(StringKey.SEARCH)) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = localizedString(StringKey.SEARCH))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = DesignSystem.Spacing.md),
                    singleLine = true
                )
                
                val languages = remember(searchQuery) {
                    if (searchQuery.isEmpty()) {
                        com.digitalgreen.farmerchat.data.LanguageManager.getAgriculturalPriorityLanguages()
                    } else {
                        com.digitalgreen.farmerchat.data.LanguageManager.searchLanguages(searchQuery)
                    }
                }
                
                LazyColumn {
                    items(languages.size) { index ->
                        val language = languages[index]
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLanguageSelected(language.code) },
                            color = if (language.code == currentLanguage) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(DesignSystem.Spacing.md),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = language.name,
                                        fontSize = DesignSystem.Typography.bodyLarge,
                                        fontWeight = if (language.code == currentLanguage) DesignSystem.Typography.Weight.Bold else DesignSystem.Typography.Weight.Normal
                                    )
                                    if (language.name != language.englishName) {
                                        Text(
                                            text = language.englishName,
                                            fontSize = DesignSystem.Typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                if (language.code == currentLanguage) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = localizedString(StringKey.SELECTED),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        
                        if (index < languages.size - 1) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(localizedString(StringKey.CLOSE))
            }
        }
    )
}

@Composable
fun AboutDialog(
    appVersion: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Agriculture,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(DesignSystem.IconSize.xlarge)
            )
        },
        title = { Text(localizedString(StringKey.APP_NAME)) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = localizedString(StringKey.APP_DESCRIPTION),
                    fontSize = DesignSystem.Typography.bodyMedium,
                    modifier = Modifier.padding(vertical = DesignSystem.Spacing.sm)
                )
                
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
                
                Text(
                    text = "${localizedString(StringKey.VERSION)}: $appVersion",
                    fontSize = DesignSystem.Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = localizedString(StringKey.COPYRIGHT),
                    fontSize = DesignSystem.Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(localizedString(StringKey.OK))
            }
        }
    )
}

// ResponseLength enum removed - using String values directly