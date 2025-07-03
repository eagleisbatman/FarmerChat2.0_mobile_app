package com.digitalgreen.farmerchat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.digitalgreen.farmerchat.ui.components.FarmerChatAppBar
import com.digitalgreen.farmerchat.ui.components.localizedString
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPinScreen(
    onNavigateBack: () -> Unit,
    onPinReset: () -> Unit,
    viewModel: ForgotPinViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as android.app.Application
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Show error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    // Navigate on success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onPinReset()
        }
    }
    
    Scaffold(
        topBar = {
            FarmerChatAppBar(
                title = localizedString(StringKey.FORGOT_PIN),
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = DesignSystem.Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
            
            // Instructions
            Text(
                text = when (uiState.step) {
                    ForgotPinStep.PHONE -> localizedString(StringKey.FORGOT_PIN_ENTER_PHONE)
                    ForgotPinStep.VERIFY -> localizedString(StringKey.FORGOT_PIN_VERIFY_IDENTITY)
                    ForgotPinStep.NEW_PIN -> localizedString(StringKey.FORGOT_PIN_CREATE_NEW)
                },
                fontSize = DesignSystem.Typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = DesignSystem.Spacing.lg)
            )
            
            when (uiState.step) {
                ForgotPinStep.PHONE -> {
                    // Phone number input
                    OutlinedTextField(
                        value = uiState.phoneNumber,
                        onValueChange = { viewModel.updatePhoneNumber(it) },
                        label = { Text(localizedString(StringKey.PHONE_NUMBER)) },
                        placeholder = { Text(localizedString(StringKey.ENTER_PHONE_NUMBER)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        isError = uiState.phoneError != null,
                        supportingText = uiState.phoneError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.lg))
                    
                    Button(
                        onClick = { viewModel.sendVerificationCode() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(localizedString(StringKey.SEND_VERIFICATION_CODE))
                        }
                    }
                }
                
                ForgotPinStep.VERIFY -> {
                    // Verification options
                    Text(
                        text = localizedString(StringKey.VERIFICATION_CODE_SENT),
                        fontSize = DesignSystem.Typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = DesignSystem.Spacing.md)
                    )
                    
                    // For now, we'll use security questions or email verification
                    // In production, this would be SMS OTP
                    OutlinedTextField(
                        value = uiState.verificationCode,
                        onValueChange = { viewModel.updateVerificationCode(it) },
                        label = { Text(localizedString(StringKey.VERIFICATION_CODE)) },
                        placeholder = { Text(localizedString(StringKey.ENTER_VERIFICATION_CODE)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = uiState.verificationError != null,
                        supportingText = uiState.verificationError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.lg))
                    
                    Button(
                        onClick = { viewModel.verifyCode() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(localizedString(StringKey.VERIFY))
                        }
                    }
                    
                    TextButton(
                        onClick = { viewModel.resendCode() },
                        modifier = Modifier.padding(top = DesignSystem.Spacing.sm)
                    ) {
                        Text(localizedString(StringKey.RESEND_CODE))
                    }
                }
                
                ForgotPinStep.NEW_PIN -> {
                    var showPin by remember { mutableStateOf(false) }
                    var showConfirmPin by remember { mutableStateOf(false) }
                    
                    // New PIN
                    OutlinedTextField(
                        value = uiState.newPin,
                        onValueChange = { viewModel.updateNewPin(it) },
                        label = { Text(localizedString(StringKey.NEW_PIN)) },
                        placeholder = { Text(localizedString(StringKey.ENTER_NEW_PIN)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPin = !showPin }) {
                                Icon(
                                    imageVector = if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPin) localizedString(StringKey.HIDE_PIN) else localizedString(StringKey.SHOW_PIN)
                                )
                            }
                        },
                        singleLine = true,
                        isError = uiState.pinError != null,
                        supportingText = uiState.pinError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
                    
                    // Confirm PIN
                    OutlinedTextField(
                        value = uiState.confirmPin,
                        onValueChange = { viewModel.updateConfirmPin(it) },
                        label = { Text(localizedString(StringKey.CONFIRM_PIN)) },
                        placeholder = { Text(localizedString(StringKey.CONFIRM_NEW_PIN)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = if (showConfirmPin) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPin = !showConfirmPin }) {
                                Icon(
                                    imageVector = if (showConfirmPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showConfirmPin) localizedString(StringKey.HIDE_PIN) else localizedString(StringKey.SHOW_PIN)
                                )
                            }
                        },
                        singleLine = true,
                        isError = uiState.confirmPinError != null,
                        supportingText = uiState.confirmPinError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.lg))
                    
                    Button(
                        onClick = { viewModel.resetPin() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(localizedString(StringKey.RESET_PIN))
                        }
                    }
                }
            }
        }
    }
}

enum class ForgotPinStep {
    PHONE,
    VERIFY,
    NEW_PIN
}