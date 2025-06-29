package com.digitalgreen.farmerchat.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.ui.theme.primaryTextColor
import com.digitalgreen.farmerchat.ui.theme.secondaryTextColor
import com.digitalgreen.farmerchat.ui.components.localizedString
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import com.digitalgreen.farmerchat.utils.LocationLanguageMapper
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneCollectionScreenV2(
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    viewModel: PhoneCollectionViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val phoneNumberFocusRequester = remember { FocusRequester() }
    
    // Get user location from view model to determine country code
    val userLocation by viewModel.userLocation.collectAsState(initial = "")
    val countryCode = remember(userLocation) {
        if (userLocation.isNotEmpty()) {
            LocationLanguageMapper.getCountryCodeForLocation(userLocation)
        } else {
            "+1" // Default to US
        }
    }
    
    var phoneNumber by remember { mutableStateOf("") }
    var showVerificationScreen by remember { mutableStateOf(false) }
    
    // Handle successful save
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            showVerificationScreen = true
            delay(1000) // Brief delay to show success
            onComplete()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = DesignSystem.Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xxxl))
            
            // Icon
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
            
            // Title
            Text(
                text = localizedString(StringKey.SECURE_YOUR_ACCOUNT),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor(),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
            
            // Subtitle
            Text(
                text = localizedString(StringKey.PHONE_COLLECTION_SUBTITLE),
                fontSize = DesignSystem.Typography.bodyLarge,
                color = secondaryTextColor(),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xxl))
            
            // Phone Input Card
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
                        text = localizedString(StringKey.PHONE_NUMBER),
                        fontSize = DesignSystem.Typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = secondaryTextColor(),
                        modifier = Modifier.padding(bottom = DesignSystem.Spacing.sm)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Country Code Chip
                        Surface(
                            modifier = Modifier,
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = countryCode,
                                modifier = Modifier.padding(
                                    horizontal = DesignSystem.Spacing.md,
                                    vertical = DesignSystem.Spacing.md
                                ),
                                fontSize = DesignSystem.Typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        
                        // Phone Number Input
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { value ->
                                // Only allow digits
                                if (value.all { it.isDigit() }) {
                                    phoneNumber = value.take(15) // Limit length
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(phoneNumberFocusRequester),
                            placeholder = {
                                Text(
                                    "123 456 7890",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    if (phoneNumber.length >= 10) {
                                        viewModel.savePhoneNumber("$countryCode$phoneNumber")
                                    }
                                }
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    
                    // Location hint
                    if (userLocation.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = userLocation,
                                fontSize = DesignSystem.Typography.bodySmall,
                                color = secondaryTextColor()
                            )
                        }
                    }
                }
            }
            
            // Info Card
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.lg))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignSystem.Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = localizedString(StringKey.PHONE_PRIVACY_INFO),
                        fontSize = DesignSystem.Typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Action buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = DesignSystem.Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.md)
            ) {
                // Continue button
                Button(
                    onClick = {
                        if (phoneNumber.length >= 10) {
                            viewModel.savePhoneNumber("$countryCode$phoneNumber")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = phoneNumber.length >= 10 && !uiState.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = localizedString(StringKey.CONTINUE),
                            fontSize = DesignSystem.Typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Skip button
                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    Text(
                        text = localizedString(StringKey.SKIP_FOR_NOW),
                        fontSize = DesignSystem.Typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Error Snackbar
        AnimatedVisibility(
            visible = uiState.error != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Snackbar(
                modifier = Modifier.padding(DesignSystem.Spacing.md),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text(localizedString(StringKey.DISMISS))
                    }
                }
            ) {
                Text(uiState.error ?: "")
            }
        }
    }
    
    // Request focus
    LaunchedEffect(Unit) {
        delay(300)
        phoneNumberFocusRequester.requestFocus()
    }
}