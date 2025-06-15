package com.digitalgreen.farmerchat.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.ui.theme.secondaryTextColor
import com.digitalgreen.farmerchat.data.LanguageManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import com.digitalgreen.farmerchat.data.LocationInfo
import com.digitalgreen.farmerchat.utils.LocationManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import android.Manifest
import com.digitalgreen.farmerchat.ui.components.localizedString
import com.digitalgreen.farmerchat.ui.components.currentLanguage
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey

// Data classes for onboarding
data class Location(val id: String, val name: String, val state: String)

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    val state by viewModel.onboardingState.collectAsState()
    
    when (state.currentStep) {
        0 -> LanguageSelectionStep(
            selectedLanguage = state.selectedLanguage,
            onLanguageSelected = viewModel::selectLanguage,
            onNext = viewModel::nextStep
        )
        1 -> LocationSelectionStep(
            selectedLocation = state.selectedLocation,
            onLocationSelected = viewModel::selectLocation,
            onNext = viewModel::nextStep,
            onBack = viewModel::previousStep
        )
        2 -> CropSelectionStep(
            selectedCrops = state.selectedCrops,
            onCropToggled = viewModel::toggleCrop,
            onNext = viewModel::nextStep,
            onBack = viewModel::previousStep
        )
        3 -> LivestockSelectionStep(
            selectedLivestock = state.selectedLivestock,
            onLivestockToggled = viewModel::toggleLivestock,
            onNext = viewModel::nextStep,
            onBack = viewModel::previousStep
        )
        4 -> RoleSelectionStep(
            selectedRole = state.role,
            onRoleSelected = viewModel::updateRole,
            onNext = viewModel::nextStep,
            onBack = viewModel::previousStep
        )
        5 -> GenderSelectionStep(
            selectedGender = state.gender,
            onGenderSelected = viewModel::updateGender,
            onNext = viewModel::nextStep,
            onBack = viewModel::previousStep
        )
        6 -> NameInputStep(
            name = state.name,
            onNameChanged = viewModel::updateName,
            onComplete = {
                viewModel.completeOnboarding()
                onOnboardingComplete()
            },
            onBack = viewModel::previousStep
        )
    }
}

@Composable
fun LanguageSelectionStep(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onNext: () -> Unit
) {
    // Use agricultural priority languages for better UX
    val languages = remember { 
        LanguageManager.getAgriculturalPriorityLanguages()
    }
    
    var searchQuery by remember { mutableStateOf("") }
    val filteredLanguages = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            languages
        } else {
            LanguageManager.searchLanguages(searchQuery)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DesignSystem.Spacing.md)
    ) {
        Text(
            text = localizedString(StringKey.CHOOSE_LANGUAGE),
            fontSize = DesignSystem.Typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = DesignSystem.Spacing.md)
        )
        
        Text(
            text = localizedString(StringKey.LANGUAGE_SUBTITLE),
            fontSize = DesignSystem.Typography.titleSmall,
            color = secondaryTextColor(),
            modifier = Modifier.padding(bottom = DesignSystem.Spacing.md)
        )
        
        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text(localizedString(StringKey.SEARCH)) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = DesignSystem.Spacing.md),
            singleLine = true
        )
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm + DesignSystem.Spacing.xs)
        ) {
            items(filteredLanguages) { language ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageSelected(language.code) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedLanguage == language.code) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (selectedLanguage == language.code) DesignSystem.Spacing.xs else 1.dp
                    ),
                    border = if (selectedLanguage == language.code) {
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        null
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignSystem.Spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = language.name,
                                fontSize = DesignSystem.Typography.titleSmall,
                                fontWeight = if (selectedLanguage == language.code) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedLanguage == language.code) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            if (language.name != language.englishName) {
                                Text(
                                    text = language.englishName,
                                    fontSize = DesignSystem.Typography.bodyMedium,
                                    color = if (selectedLanguage == language.code) {
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                        if (selectedLanguage == language.code) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(DesignSystem.IconSize.medium)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedLanguage.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(localizedString(StringKey.CONTINUE), modifier = Modifier.padding(vertical = DesignSystem.Spacing.sm))
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationSelectionStep(
    selectedLocation: String,
    onLocationSelected: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    val context = LocalContext.current
    val locationManager = remember { LocationManager(context) }
    
    var isLoadingLocation by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var detectedLocation by remember { mutableStateOf<LocationInfo?>(null) }
    
    // Location permission state
    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    val coroutineScope = rememberCoroutineScope()
    
    // Function to get current location
    fun getLocation() {
        if (!locationPermissionState.allPermissionsGranted) {
            locationPermissionState.launchMultiplePermissionRequest()
            return
        }
        
        coroutineScope.launch {
            isLoadingLocation = true
            locationError = null
            
            try {
                val location = locationManager.getCurrentLocation()
                if (location != null) {
                    val locationInfo = locationManager.reverseGeocode(
                        location.latitude,
                        location.longitude
                    )
                    detectedLocation = locationInfo
                    locationInfo?.let {
                        val formattedLocation = locationManager.getFormattedLocationString(it)
                        onLocationSelected(formattedLocation)
                        viewModel.setLocationInfo(it)
                    }
                } else {
                    locationError = "Unable to get location. Please check your device settings."
                }
            } catch (e: Exception) {
                locationError = "Error getting location: ${e.message}"
            } finally {
                isLoadingLocation = false
            }
        }
    }
    
    // Effect to request location when permissions are granted
    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted && detectedLocation == null) {
            getLocation()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DesignSystem.Spacing.md)
    ) {
        Text(
            text = localizedString(StringKey.WHERE_LOCATED),
            fontSize = DesignSystem.Typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = DesignSystem.Spacing.md)
        )
        
        Text(
            text = localizedString(StringKey.LOCATION_SUBTITLE),
            fontSize = DesignSystem.Typography.titleSmall,
            color = secondaryTextColor(),
            modifier = Modifier.padding(bottom = DesignSystem.Spacing.lg)
        )
        
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
        
        // Location detection card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignSystem.Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    modifier = Modifier.size(DesignSystem.IconSize.xlarge),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
                
                when {
                    isLoadingLocation -> {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
                        Text(localizedString(StringKey.GETTING_LOCATION))
                    }
                    detectedLocation != null -> {
                        Text(
                            text = localizedString(StringKey.LOCATION_DETECTED),
                            fontWeight = FontWeight.Bold,
                            fontSize = DesignSystem.Typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
                        Text(
                            text = locationManager.getFormattedLocationString(detectedLocation!!),
                            fontSize = DesignSystem.Typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    locationError != null -> {
                        Text(
                            text = locationError!!,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
                        Button(
                            onClick = { getLocation() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(localizedString(StringKey.RETRY))
                        }
                    }
                    !locationPermissionState.allPermissionsGranted -> {
                        Text(
                            text = localizedString(StringKey.LOCATION_PERMISSION_RATIONALE),
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
                        Button(
                            onClick = { locationPermissionState.launchMultiplePermissionRequest() }
                        ) {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = null,
                                modifier = Modifier.size(DesignSystem.IconSize.medium - DesignSystem.Spacing.xs)
                            )
                            Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
                            Text(localizedString(StringKey.ENABLE_LOCATION))
                        }
                    }
                    else -> {
                        Button(
                            onClick = { getLocation() }
                        ) {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = null,
                                modifier = Modifier.size(DesignSystem.IconSize.medium - DesignSystem.Spacing.xs)
                            )
                            Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
                            Text(localizedString(StringKey.DETECT_MY_LOCATION))
                        }
                    }
                }
            }
        }
        
        // Manual location entry option
        if (!isLoadingLocation) {
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.lg))
            
            OutlinedTextField(
                value = if (detectedLocation == null) selectedLocation else "",
                onValueChange = { 
                    onLocationSelected(it)
                    detectedLocation = null
                },
                label = { Text(localizedString(StringKey.OR_ENTER_MANUALLY)) },
                placeholder = { Text(localizedString(StringKey.LOCATION_PLACEHOLDER)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoadingLocation,
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = "Location")
                }
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text(localizedString(StringKey.BACK), modifier = Modifier.padding(vertical = DesignSystem.Spacing.sm))
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = selectedLocation.isNotEmpty() || detectedLocation != null,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(localizedString(StringKey.CONTINUE), modifier = Modifier.padding(vertical = DesignSystem.Spacing.sm))
            }
        }
    }
}

@Composable
fun CropSelectionStep(
    selectedCrops: List<String>,
    onCropToggled: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<com.digitalgreen.farmerchat.data.CropsManager.CropCategory?>(null) }
    
    val currentLanguage = currentLanguage()
    val allCrops = remember { com.digitalgreen.farmerchat.data.CropsManager.getAllCrops() }
    val categories = remember { com.digitalgreen.farmerchat.data.CropsManager.getCategories() }
    
    val filteredCrops = remember(searchQuery, selectedCategory) {
        when {
            searchQuery.isNotEmpty() -> com.digitalgreen.farmerchat.data.CropsManager.searchCrops(searchQuery)
            selectedCategory != null -> com.digitalgreen.farmerchat.data.CropsManager.getCropsByCategory(selectedCategory!!)
            else -> allCrops
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DesignSystem.Spacing.md)
    ) {
        Text(
            text = localizedString(StringKey.SELECT_CROPS),
            fontSize = DesignSystem.Typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = DesignSystem.Spacing.md)
        )
        
        Text(
            text = localizedString(StringKey.CROPS_SUBTITLE),
            fontSize = DesignSystem.Typography.titleSmall,
            color = secondaryTextColor(),
            modifier = Modifier.padding(bottom = DesignSystem.Spacing.md)
        )
        
        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                selectedCategory = null // Clear category when searching
            },
            label = { Text(localizedString(StringKey.SEARCH)) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { 
                        searchQuery = ""
                        selectedCategory = null
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = DesignSystem.Spacing.sm),
            singleLine = true
        )
        
        // Category chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = DesignSystem.Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null && searchQuery.isEmpty(),
                    onClick = { 
                        selectedCategory = null
                        searchQuery = ""
                    },
                    label = { Text("All") }
                )
            }
            items(categories.size) { index ->
                val category = categories[index]
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { 
                        selectedCategory = if (selectedCategory == category) null else category
                        searchQuery = ""
                    },
                    label = { Text(category.displayName) }
                )
            }
        }
        
        // Selected crops count
        if (selectedCrops.isNotEmpty()) {
            Text(
                text = "${selectedCrops.size} crops selected",
                fontSize = DesignSystem.Typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = DesignSystem.Spacing.sm)
            )
        }
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm + DesignSystem.Spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm + DesignSystem.Spacing.xs)
        ) {
            items(filteredCrops) { crop ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f)
                        .clickable { onCropToggled(crop.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (crop.id in selectedCrops) {
                            DesignSystem.Colors.Primary.copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = if (crop.id in selectedCrops) {
                        CardDefaults.outlinedCardBorder().copy(width = 2.dp)
                    } else null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(DesignSystem.Spacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = crop.emoji,
                            fontSize = DesignSystem.Spacing.xl.value.sp
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
                        Text(
                            text = crop.getLocalizedName(currentLanguage),
                            fontSize = DesignSystem.Typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        if (crop.scientificName.isNotEmpty()) {
                            Text(
                                text = crop.scientificName,
                                fontSize = 10.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text(localizedString(StringKey.BACK), modifier = Modifier.padding(vertical = DesignSystem.Spacing.sm))
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(localizedString(StringKey.CONTINUE), modifier = Modifier.padding(vertical = DesignSystem.Spacing.sm))
            }
        }
    }
}

@Composable
fun LivestockSelectionStep(
    selectedLivestock: List<String>,
    onLivestockToggled: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<com.digitalgreen.farmerchat.data.LivestockManager.LivestockCategory?>(null) }
    var selectedPurpose by remember { mutableStateOf<com.digitalgreen.farmerchat.data.LivestockManager.Purpose?>(null) }
    
    val currentLanguage = currentLanguage()
    val allLivestock = remember { com.digitalgreen.farmerchat.data.LivestockManager.getAllLivestock() }
    val categories = remember { com.digitalgreen.farmerchat.data.LivestockManager.getCategories() }
    
    val filteredLivestock = remember(searchQuery, selectedCategory, selectedPurpose) {
        when {
            searchQuery.isNotEmpty() -> com.digitalgreen.farmerchat.data.LivestockManager.searchLivestock(searchQuery)
            selectedCategory != null -> com.digitalgreen.farmerchat.data.LivestockManager.getLivestockByCategory(selectedCategory!!)
            selectedPurpose != null -> com.digitalgreen.farmerchat.data.LivestockManager.getLivestockByPurpose(selectedPurpose!!)
            else -> allLivestock
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DesignSystem.Spacing.md)
    ) {
        Text(
            text = localizedString(StringKey.SELECT_LIVESTOCK),
            fontSize = DesignSystem.Typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = DesignSystem.Spacing.md)
        )
        
        Text(
            text = localizedString(StringKey.LIVESTOCK_SUBTITLE),
            fontSize = DesignSystem.Typography.titleSmall,
            color = secondaryTextColor(),
            modifier = Modifier.padding(bottom = DesignSystem.Spacing.md)
        )
        
        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                selectedCategory = null
                selectedPurpose = null
            },
            label = { Text(localizedString(StringKey.SEARCH)) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { 
                        searchQuery = ""
                        selectedCategory = null
                        selectedPurpose = null
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = DesignSystem.Spacing.sm),
            singleLine = true
        )
        
        // Category chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = DesignSystem.Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null && selectedPurpose == null && searchQuery.isEmpty(),
                    onClick = { 
                        selectedCategory = null
                        selectedPurpose = null
                        searchQuery = ""
                    },
                    label = { Text("All") }
                )
            }
            items(categories.size) { index ->
                val category = categories[index]
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { 
                        selectedCategory = if (selectedCategory == category) null else category
                        selectedPurpose = null
                        searchQuery = ""
                    },
                    label = { Text(category.displayName) }
                )
            }
        }
        
        // Selected livestock count
        if (selectedLivestock.isNotEmpty()) {
            Text(
                text = "${selectedLivestock.size} animals selected",
                fontSize = DesignSystem.Typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = DesignSystem.Spacing.sm)
            )
        }
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm + DesignSystem.Spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm + DesignSystem.Spacing.xs)
        ) {
            items(filteredLivestock) { animal ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.3f)
                        .clickable { onLivestockToggled(animal.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (animal.id in selectedLivestock) {
                            DesignSystem.Colors.Primary.copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = if (animal.id in selectedLivestock) {
                        CardDefaults.outlinedCardBorder().copy(width = 2.dp)
                    } else null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(DesignSystem.Spacing.sm + DesignSystem.Spacing.xs),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = animal.emoji,
                            fontSize = DesignSystem.Spacing.xl.value.sp
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
                        Text(
                            text = animal.getLocalizedName(currentLanguage),
                            fontSize = DesignSystem.Typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        if (animal.primaryPurpose.isNotEmpty()) {
                            Text(
                                text = animal.primaryPurpose.take(2).joinToString(", ") { it.displayName },
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text(localizedString(StringKey.BACK), modifier = Modifier.padding(vertical = DesignSystem.Spacing.sm))
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(localizedString(StringKey.CONTINUE), modifier = Modifier.padding(vertical = DesignSystem.Spacing.sm))
            }
        }
    }
}

@Composable
fun NameInputStep(
    name: String,
    onNameChanged: (String) -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DesignSystem.Spacing.md)
    ) {
        Text(
            text = localizedString(StringKey.ENTER_YOUR_NAME),
            fontSize = DesignSystem.Typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = DesignSystem.Spacing.md)
        )
        
        Text(
            text = localizedString(StringKey.NAME),
            fontSize = DesignSystem.Typography.titleSmall,
            color = secondaryTextColor(),
            modifier = Modifier.padding(bottom = DesignSystem.Spacing.lg)
        )
        
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
        
        // Name input field
        OutlinedTextField(
            value = name,
            onValueChange = onNameChanged,
            label = { Text(localizedString(StringKey.NAME)) },
            placeholder = { Text(localizedString(StringKey.ENTER_YOUR_NAME)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text(localizedString(StringKey.BACK), modifier = Modifier.padding(vertical = DesignSystem.Spacing.sm))
            }
            
            Button(
                onClick = onComplete,
                modifier = Modifier.weight(1f),
                enabled = name.isNotBlank(), // Enable only when name is not empty
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(localizedString(StringKey.START_CHATTING), modifier = Modifier.padding(vertical = DesignSystem.Spacing.sm))
            }
        }
    }
}

@Composable
fun RoleSelectionStep(
    selectedRole: String,
    onRoleSelected: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DesignSystem.Spacing.md)
    ) {
        Text(
            text = localizedString(StringKey.SELECT_ROLE),
            fontSize = DesignSystem.Typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = DesignSystem.Spacing.md)
        )
        
        Text(
            text = localizedString(StringKey.ROLE_SUBTITLE),
            fontSize = DesignSystem.Typography.titleSmall,
            color = secondaryTextColor(),
            modifier = Modifier.padding(bottom = DesignSystem.Spacing.lg)
        )
        
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
        
        // Role options
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRoleSelected("farmer") },
            colors = CardDefaults.cardColors(
                containerColor = if (selectedRole == "farmer") {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (selectedRole == "farmer") DesignSystem.Spacing.xs else 1.dp
            ),
            border = if (selectedRole == "farmer") {
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
                        text = localizedString(StringKey.FARMER),
                        fontSize = DesignSystem.Typography.titleMedium,
                        fontWeight = if (selectedRole == "farmer") FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedRole == "farmer") {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                if (selectedRole == "farmer") {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(DesignSystem.IconSize.medium)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .padding(2.dp)
                                .fillMaxSize()
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRoleSelected("extension_worker") },
            colors = CardDefaults.cardColors(
                containerColor = if (selectedRole == "extension_worker") {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (selectedRole == "extension_worker") DesignSystem.Spacing.xs else 1.dp
            ),
            border = if (selectedRole == "extension_worker") {
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
                        text = localizedString(StringKey.EXTENSION_WORKER),
                        fontSize = DesignSystem.Typography.titleMedium,
                        fontWeight = if (selectedRole == "extension_worker") FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedRole == "extension_worker") {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                if (selectedRole == "extension_worker") {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(DesignSystem.IconSize.medium)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .padding(2.dp)
                                .fillMaxSize()
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text(localizedString(StringKey.BACK), modifier = Modifier.padding(vertical = DesignSystem.Spacing.sm))
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = selectedRole.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(localizedString(StringKey.CONTINUE), modifier = Modifier.padding(vertical = DesignSystem.Spacing.sm))
            }
        }
    }
}

@Composable
fun GenderSelectionStep(
    selectedGender: String,
    onGenderSelected: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DesignSystem.Spacing.md)
    ) {
        Text(
            text = localizedString(StringKey.SELECT_GENDER),
            fontSize = DesignSystem.Typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = DesignSystem.Spacing.md)
        )
        
        Text(
            text = localizedString(StringKey.GENDER_SUBTITLE),
            fontSize = DesignSystem.Typography.titleSmall,
            color = secondaryTextColor(),
            modifier = Modifier.padding(bottom = DesignSystem.Spacing.lg)
        )
        
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xl))
        
        // Gender options
        val genderOptions = listOf(
            "male" to StringKey.MALE,
            "female" to StringKey.FEMALE,
            "other" to StringKey.OTHER
        )
        
        genderOptions.forEach { (value, stringKey) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onGenderSelected(value) },
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
                                contentDescription = "Selected",
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
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text(localizedString(StringKey.BACK), modifier = Modifier.padding(vertical = DesignSystem.Spacing.sm))
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = selectedGender.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(localizedString(StringKey.CONTINUE), modifier = Modifier.padding(vertical = DesignSystem.Spacing.sm))
            }
        }
    }
}