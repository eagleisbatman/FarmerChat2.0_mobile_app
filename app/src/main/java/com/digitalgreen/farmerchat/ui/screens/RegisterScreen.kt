package com.digitalgreen.farmerchat.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.LocalTextStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digitalgreen.farmerchat.FarmerChatApplication
import com.digitalgreen.farmerchat.ui.components.FarmerChatAppBar
import com.digitalgreen.farmerchat.ui.components.localizedString
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.ui.theme.primaryTextColor
import com.digitalgreen.farmerchat.ui.theme.secondaryTextColor
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import kotlinx.coroutines.delay

enum class RegistrationStep {
    PHONE, PIN
}

@OptIn(ExperimentalAnimationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = viewModel(
        factory = RegisterViewModel.Factory(
            (androidx.compose.ui.platform.LocalContext.current.applicationContext as FarmerChatApplication).repository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        FarmerChatAppBar(
            title = localizedString(StringKey.CREATE_ACCOUNT),
            onBackClick = onNavigateBack
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        slideInHorizontally { width -> width } + fadeIn() with
                        slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() with
                        slideOutHorizontally { width -> width } + fadeOut()
                    }
                },
                label = "registration_step"
            ) { step ->
                when (step) {
                    RegistrationStep.PHONE -> PhoneStep(viewModel, focusManager)
                    RegistrationStep.PIN -> PINStep(viewModel, focusManager, onRegisterSuccess)
                }
            }
        }
    }
}

@Composable
private fun PhoneStep(
    viewModel: RegisterViewModel,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    val uiState by viewModel.uiState.collectAsState()
    val phonePattern = remember { Regex("^\\+[0-9]{10,15}$") } // Country code is mandatory
    val isValidPhone = uiState.phoneNumber.isNotEmpty() && phonePattern.matches(uiState.phoneNumber)
    val phoneFocusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        phoneFocusRequester.requestFocus()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DesignSystem.Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xxxl))
        
        // Icon with background - matching login screen style
        Surface(
            modifier = Modifier.size(100.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
        
        Text(
            text = localizedString(StringKey.CREATE_ACCOUNT),
            fontSize = 32.sp, // Match LoginScreen title size
            fontWeight = FontWeight.Bold,
            color = primaryTextColor(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
        
        Text(
            text = localizedString(StringKey.ENTER_PHONE_NUMBER),
            fontSize = DesignSystem.Typography.bodyLarge,
            color = secondaryTextColor(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xxl))
        
        // Card for input field - matching login screen style
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = DesignSystem.Spacing.sm)
                )
                
                OutlinedTextField(
                    value = uiState.phoneNumber,
                    onValueChange = { value ->
                        if (value.all { it.isDigit() || it == '+' }) {
                            viewModel.updatePhoneNumber(value.take(15))
                        }
                    },
                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),  // Larger input text
                    placeholder = { 
                        Text(
                            localizedString(StringKey.PHONE_PLACEHOLDER_EXAMPLE),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = uiState.phoneNumber.isNotEmpty() && !isValidPhone,
                    supportingText = {
                        Text(
                            text = if (uiState.phoneNumber.isNotEmpty() && !isValidPhone) {
                                localizedString(StringKey.PHONE_MUST_START_WITH_CODE)
                            } else {
                                localizedString(StringKey.COUNTRY_CODE_HELPER)
                            },
                            color = if (uiState.phoneNumber.isNotEmpty() && !isValidPhone) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            },
                            fontSize = DesignSystem.Typography.bodySmall
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(phoneFocusRequester)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
        
        Button(
            onClick = { 
                focusManager.clearFocus()
                viewModel.proceedToPinStep() 
            },
            enabled = isValidPhone && !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
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
                    localizedString(StringKey.CONTINUE),
                    fontSize = DesignSystem.Typography.bodyLarge,
                    fontWeight = DesignSystem.Typography.Weight.Medium
                )
            }
        }
        
        AnimatedVisibility(
            visible = uiState.error != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = DesignSystem.Spacing.md),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignSystem.Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
                    Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 15.sp  // Increased from bodySmall (14sp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PINStep(
    viewModel: RegisterViewModel,
    focusManager: androidx.compose.ui.focus.FocusManager,
    onRegisterSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPin by remember { mutableStateOf(false) }
    var showConfirmPin by remember { mutableStateOf(false) }
    val pinFocusRequester = remember { FocusRequester() }
    val confirmPinFocusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        pinFocusRequester.requestFocus()
    }
    
    LaunchedEffect(uiState.registrationSuccess) {
        if (uiState.registrationSuccess) {
            delay(500)
            onRegisterSuccess()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DesignSystem.Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xxxl))
        
        // Icon with background - matching login screen style
        Surface(
            modifier = Modifier.size(100.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Password,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
        
        Text(
            text = localizedString(StringKey.CREATE_YOUR_PIN),
            fontSize = DesignSystem.Typography.headlineMedium,
            fontWeight = DesignSystem.Typography.Weight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
        
        Text(
            text = localizedString(StringKey.CREATE_PIN_SUBTITLE),
            fontSize = DesignSystem.Typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xxl))
        
        // Card for input fields - matching login screen style
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
                // PIN Field
                Text(
                    text = localizedString(StringKey.SECURITY_PIN),
                    fontSize = DesignSystem.Typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = DesignSystem.Spacing.sm)
                )
                
                OutlinedTextField(
                    value = uiState.pin,
                    onValueChange = { value ->
                        if (value.all { it.isDigit() } && value.length <= 6) {
                            viewModel.updatePIN(value)
                        }
                    },
                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),  // Larger input text
                    placeholder = { 
                        Text(
                            localizedString(StringKey.PIN_PLACEHOLDER),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPin = !showPin }) {
                            Icon(
                                imageVector = if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPin) localizedString(StringKey.HIDE_PIN) else localizedString(StringKey.SHOW_PIN)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(pinFocusRequester)
                )
                
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.lg))
                
                // Confirm PIN Field
                Text(
                    text = localizedString(StringKey.CONFIRM_PIN),
                    fontSize = DesignSystem.Typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = DesignSystem.Spacing.sm)
                )
                
                OutlinedTextField(
                    value = uiState.confirmPin,
                    onValueChange = { value ->
                        if (value.all { it.isDigit() } && value.length <= 6) {
                            viewModel.updateConfirmPIN(value)
                        }
                    },
                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),  // Larger input text
                    placeholder = { 
                        Text(
                            localizedString(StringKey.PIN_PLACEHOLDER),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    visualTransformation = if (showConfirmPin) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPin = !showConfirmPin }) {
                            Icon(
                                imageVector = if (showConfirmPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showConfirmPin) localizedString(StringKey.HIDE_PIN) else localizedString(StringKey.SHOW_PIN)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = uiState.confirmPin.isNotEmpty() && uiState.confirmPin != uiState.pin,
                    supportingText = if (uiState.confirmPin.isNotEmpty() && uiState.confirmPin != uiState.pin) {
                        { Text(localizedString(StringKey.ERROR_PIN_MISMATCH)) }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(confirmPinFocusRequester)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
        
        Button(
            onClick = { 
                focusManager.clearFocus()
                viewModel.register() 
            },
            enabled = uiState.pin.length == 6 && uiState.pin == uiState.confirmPin && !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
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
                    localizedString(StringKey.CREATE_ACCOUNT),
                    fontSize = DesignSystem.Typography.bodyLarge,
                    fontWeight = DesignSystem.Typography.Weight.Medium
                )
            }
        }
        
        AnimatedVisibility(
            visible = uiState.error != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = DesignSystem.Spacing.md),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignSystem.Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
                    Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 15.sp  // Increased from bodySmall (14sp)
                    )
                }
            }
        }
    }
}