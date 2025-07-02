package com.digitalgreen.farmerchat.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.platform.testTag
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.ui.theme.primaryTextColor
import com.digitalgreen.farmerchat.ui.theme.secondaryTextColor
import com.digitalgreen.farmerchat.ui.components.localizedString
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPin: () -> Unit,
    onLoginSuccess: (Boolean) -> Unit, // Boolean indicates if profile is complete
    viewModel: LoginViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as android.app.Application
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val phoneNumberFocusRequester = remember { FocusRequester() }
    val pinFocusRequester = remember { FocusRequester() }
    
    var phoneNumber by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    
    // Handle successful login
    LaunchedEffect(uiState.loginSuccess) {
        uiState.loginSuccess?.let { profileComplete ->
            onLoginSuccess(profileComplete)
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = DesignSystem.Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xxxl))
            
            // App Icon/Logo
            Surface(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Agriculture,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
            
            // Title
            Text(
                text = localizedString(StringKey.APP_NAME),
                fontSize = 40.sp,  // Increased from 32.sp for better visibility
                fontWeight = FontWeight.Bold,
                color = primaryTextColor()
            )
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
            
            // Subtitle
            Text(
                text = localizedString(StringKey.WELCOME_BACK),
                fontSize = 20.sp,  // Increased from bodyLarge (18sp) for better readability
                color = secondaryTextColor(),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xxl))
            
            // Login Card
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
                    // Phone Number Field
                    Text(
                        text = localizedString(StringKey.PHONE_NUMBER),
                        fontSize = 18.sp,  // Increased from bodyMedium (16sp)
                        fontWeight = FontWeight.Medium,
                        color = secondaryTextColor(),
                        modifier = Modifier.padding(bottom = DesignSystem.Spacing.sm)
                    )
                    
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { value ->
                            // Only allow digits and limit length
                            if (value.all { it.isDigit() || it == '+' }) {
                                phoneNumber = value.take(15)
                            }
                        },
                        textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),  // Larger input text
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(phoneNumberFocusRequester)
                            .testTag("phoneNumberInput"),
                        placeholder = {
                            Text(
                                "+91 9876543210",
                                fontSize = 16.sp,  // Explicit font size for placeholder
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
                        keyboardActions = KeyboardActions(
                            onNext = {
                                pinFocusRequester.requestFocus()
                            }
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        isError = uiState.error != null
                    )
                    
                    // Helper text for country code requirement
                    Text(
                        text = "Include country code (e.g., +91 for India, +1 for USA)",
                        fontSize = 15.sp,  // Increased from bodySmall (14sp)
                        color = if (phoneNumber.isNotEmpty() && !phoneNumber.startsWith("+")) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.lg))
                    
                    // PIN Field
                    Text(
                        text = localizedString(StringKey.SECURITY_PIN),
                        fontSize = 18.sp,  // Increased from bodyMedium (16sp)
                        fontWeight = FontWeight.Medium,
                        color = secondaryTextColor(),
                        modifier = Modifier.padding(bottom = DesignSystem.Spacing.sm)
                    )
                    
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { value ->
                            // Only allow digits and limit to 6
                            if (value.all { it.isDigit() } && value.length <= 6) {
                                pin = value
                            }
                        },
                        textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),  // Larger input text
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(pinFocusRequester)
                            .testTag("pinInput"),
                        placeholder = {
                            Text(
                                "••••••",
                                fontSize = 16.sp,  // Explicit font size for placeholder
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
                        trailingIcon = {
                            IconButton(onClick = { showPin = !showPin }) {
                                Icon(
                                    imageVector = if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPin) "Hide PIN" else "Show PIN"
                                )
                            }
                        },
                        visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                if (phoneNumber.isNotEmpty() && pin.length == 6) {
                                    viewModel.login(phoneNumber, pin)
                                }
                            }
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        isError = uiState.error != null
                    )
                }
            }
            
            // Forgot PIN link
            TextButton(
                onClick = onNavigateToForgotPin,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = DesignSystem.Spacing.sm)
            ) {
                Text(
                    text = localizedString(StringKey.FORGOT_PIN),
                    color = DesignSystem.Colors.Primary,
                    style = DesignSystem.Typography.bodyMedium
                )
            }
            
            // Error Message
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
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
            
            // Login Button
            Button(
                onClick = {
                    viewModel.login(phoneNumber, pin)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("loginButton"),
                enabled = phoneNumber.startsWith("+") && phoneNumber.length >= 10 && pin.length == 6 && !uiState.isLoading,
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
                        text = localizedString(StringKey.LOGIN),
                        fontSize = 20.sp,  // Increased from bodyLarge (18sp)
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.lg))
            
            // Create Account Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = localizedString(StringKey.DONT_HAVE_ACCOUNT),
                    color = secondaryTextColor(),
                    fontSize = 17.sp  // Increased from bodyMedium (16sp)
                )
                TextButton(
                    onClick = onNavigateToRegister,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.testTag("registerButton")
                ) {
                    Text(
                        text = localizedString(StringKey.CREATE_ACCOUNT),
                        fontSize = 17.sp,  // Increased from bodyMedium (16sp)
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
        }
    }
    
    // Request focus on phone number field
    LaunchedEffect(Unit) {
        delay(300)
        phoneNumberFocusRequester.requestFocus()
    }
}