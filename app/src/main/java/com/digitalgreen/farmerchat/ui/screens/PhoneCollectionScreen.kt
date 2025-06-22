package com.digitalgreen.farmerchat.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.digitalgreen.farmerchat.ui.components.FarmerChatAppBar
import com.digitalgreen.farmerchat.ui.components.localizedString
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import com.digitalgreen.farmerchat.utils.LocationLanguageMapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneCollectionScreen(
    onNavigateBack: () -> Unit,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    viewModel: PhoneCollectionViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val phoneNumberFocusRequester = remember { FocusRequester() }
    val pinFocusRequester = remember { FocusRequester() }
    
    // Get user location from view model to determine country code
    val userLocation by viewModel.userLocation.collectAsState(initial = "")
    val countryCode = remember(userLocation) {
        if (userLocation.isNotEmpty()) {
            val code = LocationLanguageMapper.getCountryCodeForLocation(userLocation)
            android.util.Log.d("PhoneCollection", "Location: $userLocation -> Country Code: $code")
            code
        } else {
            android.util.Log.d("PhoneCollection", "No location detected, defaulting to +1")
            "+1"
        }
    }
    
    var phoneNumber by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    
    // Handle successful save
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onComplete()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Secure Your Account",
            fontSize = DesignSystem.Typography.headlineMedium,
            fontWeight = DesignSystem.Typography.Weight.Bold,
            modifier = Modifier.padding(horizontal = DesignSystem.Spacing.md)
                .padding(top = DesignSystem.Spacing.md),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = "Add a phone number to secure your account and receive important updates",
            fontSize = DesignSystem.Typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = DesignSystem.Spacing.md)
                .padding(bottom = DesignSystem.Spacing.lg)
        )
        
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
            
        // Phone number section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignSystem.Spacing.md)
        ) {
            // Phone number input
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(DesignSystem.CornerRadius.medium),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(DesignSystem.Spacing.lg)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(DesignSystem.IconSize.medium)
                        )
                        Text(
                            text = "Phone Number",
                            fontSize = DesignSystem.Typography.titleMedium,
                            fontWeight = DesignSystem.Typography.Weight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    if (userLocation.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
                        Text(
                            text = "Detected location: $userLocation",
                            fontSize = DesignSystem.Typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = DesignSystem.Spacing.sm)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
                    ) {
                        // Country code (read-only, based on location)
                        OutlinedTextField(
                            value = countryCode,
                            onValueChange = { },
                            modifier = Modifier.width(90.dp),
                            readOnly = true,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                        
                        // Phone number field
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(phoneNumberFocusRequester),
                            placeholder = { Text("Enter phone number") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { pinFocusRequester.requestFocus() }
                            ),
                            isError = uiState.error != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.lg))
            
            // PIN input
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(DesignSystem.CornerRadius.medium),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(DesignSystem.Spacing.lg)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(DesignSystem.IconSize.medium)
                        )
                        Text(
                            text = "Security PIN",
                            fontSize = DesignSystem.Typography.titleMedium,
                            fontWeight = DesignSystem.Typography.Weight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
                    
                    Text(
                        text = "Create a 6-digit PIN to secure your account",
                        fontSize = DesignSystem.Typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
                    
                    // PIN field
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) pin = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(pinFocusRequester),
                        placeholder = { Text("Enter 6-digit PIN") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Next
                        ),
                        isError = uiState.error != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
                    
                    // Confirm PIN field
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) confirmPin = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Confirm PIN") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                if (isValidInput(phoneNumber, pin, confirmPin)) {
                                    viewModel.savePhoneAndPin(countryCode + phoneNumber, pin)
                                }
                            }
                        ),
                        isError = uiState.error != null || (confirmPin.isNotEmpty() && pin != confirmPin),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                    
                    // PIN mismatch error
                    if (confirmPin.isNotEmpty() && pin != confirmPin) {
                        Text(
                            text = "PINs don't match",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = DesignSystem.Typography.bodySmall,
                            modifier = Modifier.padding(top = DesignSystem.Spacing.xs)
                        )
                    }
                }
            }
            
            // Error message
            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = DesignSystem.Typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Bottom buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignSystem.Spacing.md)
                .padding(bottom = DesignSystem.Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Continue button
            Button(
                onClick = {
                    if (isValidInput(phoneNumber, pin, confirmPin)) {
                        viewModel.savePhoneAndPin(countryCode + phoneNumber, pin)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !uiState.isLoading && isValidInput(phoneNumber, pin, confirmPin),
                shape = RoundedCornerShape(DesignSystem.CornerRadius.medium),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(DesignSystem.IconSize.small),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Continue",
                        fontSize = DesignSystem.Typography.bodyLarge,
                        fontWeight = DesignSystem.Typography.Weight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
            
            // Skip button
            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Skip for now",
                    fontSize = DesignSystem.Typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun isValidInput(phoneNumber: String, pin: String, confirmPin: String): Boolean {
    return phoneNumber.isNotEmpty() && 
           pin.length == 6 && 
           confirmPin.length == 6 && 
           pin == confirmPin
}