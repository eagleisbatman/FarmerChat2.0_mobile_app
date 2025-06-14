
package com.digitalgreen.farmerchat.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.digitalgreen.farmerchat.data.LanguageManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.LocationOn
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
            .padding(16.dp)
    ) {
        Text(
            text = localizedString(StringKey.CHOOSE_LANGUAGE),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        Text(
            text = localizedString(StringKey.LANGUAGE_SUBTITLE),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
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
                .padding(bottom = 16.dp),
            singleLine = true
        )
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredLanguages) { language ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageSelected(language.code) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedLanguage == language.code) {
                            Color(0xFF4CAF50).copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (selectedLanguage == language.code) 4.dp else 1.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = language.name,
                                fontSize = 18.sp,
                                fontWeight = if (selectedLanguage == language.code) FontWeight.Bold else FontWeight.Normal
                            )
                            if (language.name != language.englishName) {
                                Text(
                                    text = language.englishName,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (selectedLanguage == language.code) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
            }
        }
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedLanguage.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text(localizedString(StringKey.CONTINUE), modifier = Modifier.padding(vertical = 8.dp))
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
            .padding(16.dp)
    ) {
        Text(
            text = localizedString(StringKey.WHERE_LOCATED),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        Text(
            text = localizedString(StringKey.LOCATION_SUBTITLE),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                when {
                    isLoadingLocation -> {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(localizedString(StringKey.GETTING_LOCATION))
                    }
                    detectedLocation != null -> {
                        Text(
                            text = localizedString(StringKey.LOCATION_DETECTED),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = locationManager.getFormattedLocationString(detectedLocation!!),
                            fontSize = 16.sp,
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
                        Spacer(modifier = Modifier.height(16.dp))
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
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { locationPermissionState.launchMultiplePermissionRequest() }
                        ) {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
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
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(localizedString(StringKey.DETECT_MY_LOCATION))
                        }
                    }
                }
            }
        }
        
        // Manual location entry option
        if (!isLoadingLocation) {
            Spacer(modifier = Modifier.height(24.dp))
            
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text(localizedString(StringKey.BACK), modifier = Modifier.padding(vertical = 8.dp))
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = selectedLocation.isNotEmpty() || detectedLocation != null,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text(localizedString(StringKey.CONTINUE), modifier = Modifier.padding(vertical = 8.dp))
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
            .padding(16.dp)
    ) {
        Text(
            text = localizedString(StringKey.SELECT_CROPS),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        Text(
            text = localizedString(StringKey.CROPS_SUBTITLE),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
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
                .padding(bottom = 8.dp),
            singleLine = true
        )
        
        // Category chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredCrops) { crop ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f)
                        .clickable { onCropToggled(crop.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (crop.id in selectedCrops) {
                            Color(0xFF4CAF50).copy(alpha = 0.2f)
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
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = crop.emoji,
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = crop.getLocalizedName(currentLanguage),
                            fontSize = 14.sp,
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text(localizedString(StringKey.BACK), modifier = Modifier.padding(vertical = 8.dp))
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text(localizedString(StringKey.CONTINUE), modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
fun LivestockSelectionStep(
    selectedLivestock: List<String>,
    onLivestockToggled: (String) -> Unit,
    onComplete: () -> Unit,
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
            .padding(16.dp)
    ) {
        Text(
            text = localizedString(StringKey.SELECT_LIVESTOCK),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        Text(
            text = localizedString(StringKey.LIVESTOCK_SUBTITLE),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
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
                .padding(bottom = 8.dp),
            singleLine = true
        )
        
        // Category chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredLivestock) { animal ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.3f)
                        .clickable { onLivestockToggled(animal.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (animal.id in selectedLivestock) {
                            Color(0xFF4CAF50).copy(alpha = 0.2f)
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
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = animal.emoji,
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = animal.getLocalizedName(currentLanguage),
                            fontSize = 14.sp,
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text(localizedString(StringKey.BACK), modifier = Modifier.padding(vertical = 8.dp))
            }
            
            Button(
                onClick = onComplete,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Start Chatting", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}