package com.digitalgreen.farmerchat.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.utils.LocationLanguageMapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingPhoneStep(
    userLocation: String?,
    phoneNumber: String,
    pin: String,
    confirmPin: String,
    onPhoneNumberChanged: (String) -> Unit,
    onPinChanged: (String) -> Unit,
    onConfirmPinChanged: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val phoneNumberFocusRequester = remember { FocusRequester() }
    val pinFocusRequester = remember { FocusRequester() }
    val confirmPinFocusRequester = remember { FocusRequester() }
    
    // Auto-detect country code based on location
    val countryCode = remember(userLocation) {
        val code = userLocation?.let { LocationLanguageMapper.getCountryCodeForLocation(it) } ?: "+1"
        android.util.Log.d("OnboardingPhone", "Location: $userLocation -> Country Code: $code")
        code
    }
    
    val isValidInput = phoneNumber.isNotEmpty() && 
                      pin.length == 6 && 
                      confirmPin.length == 6 && 
                      pin == confirmPin
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = DesignSystem.Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
        
        // Header
        Text(
            text = "Secure Your Account",
            fontSize = DesignSystem.Typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
        
        Text(
            text = "Add your phone number and create a secure PIN to protect your data",
            fontSize = DesignSystem.Typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
        
        // Phone number section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(DesignSystem.Spacing.md)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(DesignSystem.IconSize.small),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(DesignSystem.Spacing.xs))
                    Text(
                        text = "Phone Number",
                        fontSize = DesignSystem.Typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (userLocation != null) {
                    Text(
                        text = "Location: $userLocation",
                        fontSize = DesignSystem.Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = DesignSystem.Spacing.xs)
                    )
                    Text(
                        text = "Country code: $countryCode",
                        fontSize = DesignSystem.Typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = DesignSystem.Spacing.xxs)
                    )
                }
                
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
                ) {
                    // Country code field (read-only for now, based on location)
                    OutlinedTextField(
                        value = countryCode,
                        onValueChange = { /* Read-only based on location */ },
                        modifier = Modifier.width(100.dp),
                        label = { Text("Code") },
                        readOnly = true,
                        singleLine = true
                    )
                    
                    // Phone number field
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = onPhoneNumberChanged,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(phoneNumberFocusRequester),
                        label = { Text("Phone Number") },
                        placeholder = { Text("1234567890") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { pinFocusRequester.requestFocus() }
                        )
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.lg))
        
        // PIN section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(DesignSystem.Spacing.md)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(DesignSystem.IconSize.small),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(DesignSystem.Spacing.xs))
                    Text(
                        text = "6-Digit Security PIN",
                        fontSize = DesignSystem.Typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
                
                // PIN field
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6) onPinChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(pinFocusRequester),
                    label = { Text("Create PIN") },
                    placeholder = { Text("123456") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { confirmPinFocusRequester.requestFocus() }
                    )
                )
                
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
                
                // Confirm PIN field
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 6) onConfirmPinChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(confirmPinFocusRequester),
                    label = { Text("Confirm PIN") },
                    placeholder = { Text("123456") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            if (isValidInput) onNext()
                        }
                    ),
                    isError = confirmPin.isNotEmpty() && pin != confirmPin
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
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.md)
        ) {
            // Back button
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back")
            }
            
            // Skip button
            TextButton(
                onClick = onSkip,
                modifier = Modifier.weight(1f)
            ) {
                Text("Skip for now")
            }
            
            // Next button
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = isValidInput
            ) {
                Text("Continue")
            }
        }
        
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.lg))
    }
}