package com.digitalgreen.farmerchat.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Transgender
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.LocalTextStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digitalgreen.farmerchat.ui.components.FarmerChatAppBar
import com.digitalgreen.farmerchat.ui.components.localizedString
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import com.digitalgreen.farmerchat.data.LanguageManager
import com.digitalgreen.farmerchat.utils.LocationManager
import com.digitalgreen.farmerchat.data.LocationInfo
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import android.Manifest
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UnifiedOnboardingScreen(
    onNavigateToCropSelection: () -> Unit,
    onNavigateToLivestockSelection: () -> Unit,
    onNavigateToLanguageSelection: () -> Unit,
    onOnboardingComplete: () -> Unit,
    viewModel: UnifiedOnboardingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Location permissions
    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    // Location manager
    val locationManager = remember { LocationManager(context) }
    
    // Load existing data on first composition
    LaunchedEffect(Unit) {
        viewModel.loadExistingUserData()
    }
    
    // Debug log to track state changes
    LaunchedEffect(uiState) {
        android.util.Log.d("UnifiedOnboarding", "State updated: " +
            "location=${uiState.location}, " +
            "language=${uiState.selectedLanguage}, " +
            "role=${uiState.role}, " +
            "gender=${uiState.gender}, " +
            "crops=${uiState.selectedCrops.size}, " +
            "livestock=${uiState.selectedLivestock.size}")
    }
    
    // Handle completion
    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onOnboardingComplete()
        }
    }
    
    // Handle navigation to crop/livestock selection
    LaunchedEffect(uiState.navigateToCropSelection) {
        if (uiState.navigateToCropSelection) {
            onNavigateToCropSelection()
            viewModel.resetCropNavigation()
        }
    }
    
    LaunchedEffect(uiState.navigateToLivestockSelection) {
        if (uiState.navigateToLivestockSelection) {
            onNavigateToLivestockSelection()
            viewModel.resetLivestockNavigation()
        }
    }
    
    LaunchedEffect(uiState.navigateToLanguageSelection) {
        if (uiState.navigateToLanguageSelection) {
            onNavigateToLanguageSelection()
            viewModel.resetLanguageNavigation()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        FarmerChatAppBar(
            title = localizedString(StringKey.PROFILE),
            onBackClick = null // No back button on onboarding
        )
        
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(DesignSystem.Spacing.lg)
            ) {
                // Profile completion status
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = DesignSystem.Spacing.lg),
                    colors = CardDefaults.cardColors(
                        containerColor = if (viewModel.isProfileComplete()) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignSystem.Spacing.md)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (viewModel.isProfileComplete()) {
                                    Icons.Default.CheckCircle
                                } else {
                                    Icons.Default.Info
                                },
                                contentDescription = null,
                                tint = if (viewModel.isProfileComplete()) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
                            Text(
                                text = if (viewModel.isProfileComplete()) {
                                    "All fields completed!"
                                } else {
                                    localizedString(StringKey.PERSONALIZATION_TITLE)
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (viewModel.isProfileComplete()) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        if (!viewModel.isProfileComplete()) {
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
                            Text(
                                text = "The more information you provide, the better we can personalize your farming advice.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                
                // Location Field
                Text(
                    text = localizedString(StringKey.LOCATION),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = DesignSystem.Spacing.xs)
                )
                Text(
                    text = localizedString(StringKey.LOCATION_BENEFIT),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = DesignSystem.Spacing.sm)
                )
                if (uiState.location.isEmpty()) {
                    // Show detection card when no location selected
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (locationPermissionState.allPermissionsGranted) {
                                    viewModel.setLoadingLocation(true)
                                    coroutineScope.launch {
                                        val location = locationManager.getCurrentLocation()
                                        if (location != null) {
                                            val locationInfo = locationManager.reverseGeocode(location.latitude, location.longitude)
                                            if (locationInfo != null) {
                                                viewModel.setLocation(locationManager.getFormattedLocationString(locationInfo))
                                                viewModel.setLocationInfo(locationInfo)
                                            }
                                        }
                                        viewModel.setLoadingLocation(false)
                                    }
                                } else {
                                    locationPermissionState.launchMultiplePermissionRequest()
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 1.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignSystem.Spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(DesignSystem.Spacing.md))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = localizedString(StringKey.LOCATION) + " *",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = localizedString(StringKey.DETECT_MY_LOCATION),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (uiState.isLoadingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    // Show selected location with option to change
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (locationPermissionState.allPermissionsGranted) {
                                    viewModel.setLoadingLocation(true)
                                    coroutineScope.launch {
                                        val location = locationManager.getCurrentLocation()
                                        if (location != null) {
                                            val locationInfo = locationManager.reverseGeocode(location.latitude, location.longitude)
                                            if (locationInfo != null) {
                                                viewModel.setLocation(locationManager.getFormattedLocationString(locationInfo))
                                                viewModel.setLocationInfo(locationInfo)
                                            }
                                        }
                                        viewModel.setLoadingLocation(false)
                                    }
                                } else {
                                    locationPermissionState.launchMultiplePermissionRequest()
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignSystem.Spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(DesignSystem.Spacing.md))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = localizedString(StringKey.LOCATION),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = uiState.location,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (uiState.isLoadingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Change location",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
                
                // Language Field
                Text(
                    text = localizedString(StringKey.LANGUAGE),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = DesignSystem.Spacing.xs)
                )
                Text(
                    text = localizedString(StringKey.LANGUAGE_BENEFIT),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = DesignSystem.Spacing.sm)
                )
                SelectionField(
                    count = if (uiState.selectedLanguage != null) -1 else 0,
                    value = uiState.selectedLanguage?.let { code ->
                        val language = LanguageManager.getLanguageByCode(code)
                        if (language != null) {
                            "${language.name} (${language.englishName})"
                        } else {
                            code
                        }
                    },
                    icon = Icons.Default.Language,
                    onClick = { viewModel.navigateToLanguageSelection() },
                    isSelected = uiState.selectedLanguage != null
                )
                
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
                
                // Name Field
                Text(
                    text = localizedString(StringKey.NAME),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = DesignSystem.Spacing.xs)
                )
                Text(
                    text = localizedString(StringKey.NAME_BENEFIT),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = DesignSystem.Spacing.sm)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    TextField(
                        value = uiState.name,
                        onValueChange = { viewModel.updateName(it) },
                        label = { 
                            Row {
                                Text(localizedString(StringKey.NAME))
                                Text(localizedString(StringKey.REQUIRED_FIELD_INDICATOR), color = MaterialTheme.colorScheme.error)
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
                
                // Role Selection
                Text(
                    text = localizedString(StringKey.SELECT_ROLE),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = DesignSystem.Spacing.xs)
                )
                Text(
                    text = localizedString(StringKey.ROLE_BENEFIT),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = DesignSystem.Spacing.sm)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.md)
                ) {
                    RoleCard(
                        role = "farmer",
                        label = localizedString(StringKey.FARMER),
                        isSelected = uiState.role == "farmer",
                        onClick = { viewModel.selectRole("farmer") },
                        modifier = Modifier.weight(1f)
                    )
                    RoleCard(
                        role = "extension_worker",
                        label = localizedString(StringKey.EXTENSION_WORKER),
                        isSelected = uiState.role == "extension_worker",
                        onClick = { viewModel.selectRole("extension_worker") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
                
                // Gender Selection
                Text(
                    text = localizedString(StringKey.SELECT_GENDER),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = DesignSystem.Spacing.xs)
                )
                Text(
                    text = localizedString(StringKey.GENDER_BENEFIT),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = DesignSystem.Spacing.sm)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
                ) {
                    GenderChip(
                        gender = "male",
                        label = localizedString(StringKey.MALE),
                        isSelected = uiState.gender == "male",
                        onClick = { viewModel.selectGender("male") },
                        modifier = Modifier.weight(1f)
                    )
                    GenderChip(
                        gender = "female",
                        label = localizedString(StringKey.FEMALE),
                        isSelected = uiState.gender == "female",
                        onClick = { viewModel.selectGender("female") },
                        modifier = Modifier.weight(1f)
                    )
                    GenderChip(
                        gender = "other",
                        label = localizedString(StringKey.OTHER),
                        isSelected = uiState.gender == "other",
                        onClick = { viewModel.selectGender("other") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.lg))
                
                // Crops Selection
                Text(
                    text = localizedString(StringKey.SELECT_CROPS),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = DesignSystem.Spacing.xs)
                )
                Text(
                    text = localizedString(StringKey.CROPS_BENEFIT),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = DesignSystem.Spacing.sm)
                )
                SelectionField(
                    count = uiState.selectedCrops.size,
                    icon = Icons.Default.Grass,
                    onClick = { viewModel.navigateToCropSelection() }
                )
                
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
                
                // Livestock Selection
                Text(
                    text = localizedString(StringKey.SELECT_LIVESTOCK),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = DesignSystem.Spacing.xs)
                )
                Text(
                    text = localizedString(StringKey.LIVESTOCK_BENEFIT),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = DesignSystem.Spacing.sm)
                )
                SelectionField(
                    count = uiState.selectedLivestock.size,
                    icon = Icons.Default.Pets,
                    onClick = { viewModel.navigateToLivestockSelection() }
                )
                
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
                
                // Debug info - remove after testing
                if (!viewModel.isProfileComplete()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = DesignSystem.Spacing.sm),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(DesignSystem.Spacing.sm)
                        ) {
                            Text(
                                text = "Debug: Missing Fields",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = buildString {
                                    appendLine("Location: ${if (uiState.location.isEmpty()) "❌ Empty" else "✓ ${uiState.location}"}")
                                    appendLine("Language: ${if (uiState.selectedLanguage == null) "❌ Not selected" else "✓ ${uiState.selectedLanguage}"}")
                                    appendLine("Name: ${if (uiState.name.isEmpty()) "❌ Empty" else "✓ ${uiState.name}"}")
                                    appendLine("Role: ${if (uiState.role.isEmpty()) "❌ Not selected" else "✓ ${uiState.role}"}")
                                    appendLine("Gender: ${if (uiState.gender.isEmpty()) "❌ Not selected" else "✓ ${uiState.gender}"}")
                                    append("Crops/Livestock: ${if (uiState.selectedCrops.isEmpty() && uiState.selectedLivestock.isEmpty()) "❌ None selected" else "✓ ${uiState.selectedCrops.size} crops, ${uiState.selectedLivestock.size} livestock"}")
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
                
                // Complete Button
                Button(
                    onClick = { viewModel.completeOnboarding() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = viewModel.isProfileComplete() && !uiState.isLoading,
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
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Error message
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
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.xxxl))
            }
        }
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isRequired: Boolean = false,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    placeholder: String = "",
    isDropdownExpanded: Boolean = false,
    dropdownContent: @Composable (() -> Unit)? = null
) {
    Column {
        OutlinedTextField(
            value = value.ifEmpty { placeholder },
            onValueChange = { /* Read only */ },
            label = { 
                Row {
                    Text(label)
                    if (isRequired) {
                        Text(localizedString(StringKey.REQUIRED_FIELD_INDICATOR), color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = if (isDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            enabled = false,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.primary,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
        )
        
        dropdownContent?.invoke()
    }
}

@Composable
private fun SelectionField(
    count: Int,
    value: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isSelected: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
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
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(DesignSystem.Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                when {
                    value != null -> {
                        Text(
                            text = value,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    count > 0 -> {
                        Text(
                            text = "$count selected",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    else -> {
                        Text(
                            text = "Tap to select",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (count > 0 || value != null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun RoleCard(
    role: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 1.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignSystem.Spacing.md),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (role == "farmer") Icons.Default.Agriculture else Icons.Default.School,
                    contentDescription = null,
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun GenderChip(
    gender: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = when (gender) {
                    "male" -> Icons.Default.Male
                    "female" -> Icons.Default.Female
                    else -> Icons.Default.Transgender
                },
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun LanguageDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onLanguageSelected: (com.digitalgreen.farmerchat.data.Language) -> Unit,
    suggestedLanguages: List<String>
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.9f)
    ) {
        // Suggested languages first
        if (suggestedLanguages.isNotEmpty()) {
            Text(
                text = "Suggested",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            suggestedLanguages.forEach { code ->
                LanguageManager.getLanguageByCode(code)?.let { language ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = language.name,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = language.englishName,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = { onLanguageSelected(language) }
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
        
        // All languages
        Text(
            text = "All Languages",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LanguageManager.languages.forEach { language ->
            DropdownMenuItem(
                text = {
                    Column {
                        Text(
                            text = language.name,
                            fontSize = 16.sp
                        )
                        Text(
                            text = language.englishName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                onClick = { onLanguageSelected(language) }
            )
        }
    }
}