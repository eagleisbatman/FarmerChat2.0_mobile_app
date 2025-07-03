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
import androidx.compose.material.icons.filled.Person
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
fun GenderSelectionScreen(
    onNavigateBack: () -> Unit,
    viewModel: ApiSettingsViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as android.app.Application
        )
    )
) {
    val settingsState by viewModel.settingsState.collectAsState()
    var selectedGender by remember { mutableStateOf(settingsState.userGender ?: "") }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }
    
    LaunchedEffect(settingsState.userGender) {
        selectedGender = settingsState.userGender ?: ""
    }
    
    Scaffold(
        topBar = {
            FarmerChatAppBar(
                title = localizedString(StringKey.SELECT_GENDER),
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
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Text(
                text = localizedString(StringKey.SELECT_GENDER),
                fontSize = DesignSystem.Typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = DesignSystem.Spacing.md)
            )
            
            Text(
                text = localizedString(StringKey.GENDER_SUBTITLE),
                fontSize = DesignSystem.Typography.bodyLarge,
                color = secondaryTextColor(),
                modifier = Modifier.padding(bottom = DesignSystem.Spacing.lg)
            )
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
            
            // Gender options with beautiful card design
            // Hide "Other" option for Ethiopia
            val genderOptions = if (settingsState.userLocation.contains("Ethiopia", ignoreCase = true)) {
                listOf(
                    "male" to StringKey.MALE,
                    "female" to StringKey.FEMALE
                )
            } else {
                listOf(
                    "male" to StringKey.MALE,
                    "female" to StringKey.FEMALE,
                    "other" to StringKey.OTHER
                )
            }
            
            genderOptions.forEach { (value, stringKey) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedGender = value },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedGender == value) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (selectedGender == value) DesignSystem.Spacing.xs else 1.dp
                    ),
                    border = if (selectedGender == value) {
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
                                text = localizedString(stringKey),
                                fontSize = DesignSystem.Typography.titleMedium,
                                fontWeight = if (selectedGender == value) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedGender == value) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                        if (selectedGender == value) {
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
                
                if (value != "other") {
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.updateUserGender(selectedGender)
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = selectedGender.isNotEmpty(),
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