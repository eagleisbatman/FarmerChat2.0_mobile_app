package com.digitalgreen.farmerchat.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.digitalgreen.farmerchat.ui.components.FarmerChatAppBar
import com.digitalgreen.farmerchat.ui.components.localizedString
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneAuthScreen(
    onNavigateBack: () -> Unit,
    onAuthComplete: () -> Unit,
    onSkip: () -> Unit,
    viewModel: PhoneAuthViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val phoneNumberFocusRequester = remember { FocusRequester() }
    val otpFocusRequester = remember { FocusRequester() }
    
    var countryCode by remember { mutableStateOf("+1") }
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    
    // Auto focus on OTP field when OTP is sent
    LaunchedEffect(uiState.isOtpSent) {
        if (uiState.isOtpSent) {
            delay(100)
            otpFocusRequester.requestFocus()
        }
    }
    
    // Handle successful authentication
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onAuthComplete()
        }
    }
    
    Scaffold(
        topBar = {
            FarmerChatAppBar(
                title = localizedString(StringKey.PHONE_AUTH_TITLE),
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = DesignSystem.Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xxxl))
            
            // Icon
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                modifier = Modifier.size(DesignSystem.IconSize.xxxlarge),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.lg))
            
            // Description
            Text(
                text = localizedString(StringKey.PHONE_AUTH_DESC),
                fontSize = DesignSystem.Typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
            
            // Phone number input
            AnimatedVisibility(
                visible = !uiState.isOtpSent,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
                    ) {
                        // Country code dropdown
                        OutlinedTextField(
                            value = countryCode,
                            onValueChange = { countryCode = it },
                            modifier = Modifier.width(DesignSystem.Spacing.xxxl * 2),
                            label = { Text(localizedString(StringKey.COUNTRY_CODE)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone
                            )
                        )
                        
                        // Phone number field
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(phoneNumberFocusRequester),
                            label = { Text(localizedString(StringKey.ENTER_PHONE_NUMBER)) },
                            placeholder = { Text("1234567890") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    viewModel.sendOtp(countryCode + phoneNumber)
                                }
                            ),
                            isError = uiState.error != null
                        )
                    }
                    
                    // Error message
                    AnimatedVisibility(visible = uiState.error != null) {
                        Text(
                            text = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = DesignSystem.Typography.bodySmall,
                            modifier = Modifier.padding(top = DesignSystem.Spacing.xs)
                        )
                    }
                }
            }
            
            // OTP input
            AnimatedVisibility(
                visible = uiState.isOtpSent,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = localizedString(StringKey.OTP_SENT).format(countryCode + phoneNumber),
                        fontSize = DesignSystem.Typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
                    
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { otpCode = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(otpFocusRequester),
                        label = { Text(localizedString(StringKey.ENTER_OTP)) },
                        placeholder = { Text("123456") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                viewModel.verifyOtp(otpCode)
                            }
                        ),
                        isError = uiState.error != null
                    )
                    
                    // Error message
                    AnimatedVisibility(visible = uiState.error != null) {
                        Text(
                            text = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = DesignSystem.Typography.bodySmall,
                            modifier = Modifier.padding(top = DesignSystem.Spacing.xs)
                        )
                    }
                    
                    // Resend OTP button
                    TextButton(
                        onClick = { viewModel.sendOtp(countryCode + phoneNumber) },
                        modifier = Modifier.padding(top = DesignSystem.Spacing.sm)
                    ) {
                        Text(localizedString(StringKey.RESEND_OTP))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
            
            // Primary action button
            Button(
                onClick = {
                    if (uiState.isOtpSent) {
                        viewModel.verifyOtp(otpCode)
                    } else {
                        viewModel.sendOtp(countryCode + phoneNumber)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && (
                    (!uiState.isOtpSent && phoneNumber.isNotEmpty()) || 
                    (uiState.isOtpSent && otpCode.isNotEmpty())
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(DesignSystem.IconSize.small),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = if (uiState.isOtpSent) {
                            localizedString(StringKey.VERIFY)
                        } else {
                            localizedString(StringKey.SEND_OTP)
                        }
                    )
                }
            }
            
            // Skip button
            TextButton(
                onClick = onSkip,
                modifier = Modifier.padding(top = DesignSystem.Spacing.sm)
            ) {
                Text(localizedString(StringKey.SKIP_FOR_NOW))
            }
        }
    }
}