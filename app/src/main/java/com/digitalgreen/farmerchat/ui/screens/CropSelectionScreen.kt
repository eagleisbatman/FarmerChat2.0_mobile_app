package com.digitalgreen.farmerchat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.digitalgreen.farmerchat.data.FarmerChatRepository
import com.digitalgreen.farmerchat.data.CropsManager
import com.digitalgreen.farmerchat.ui.components.localizedString
import com.digitalgreen.farmerchat.ui.components.currentLanguage
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropSelectionScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: CropSelectionViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return CropSelectionViewModel(
                    repository = FarmerChatRepository()
                ) as T
            }
        }
    )
    
    val selectedCrops by viewModel.selectedCrops.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    val allCrops = remember { CropsManager.getAllCrops() }
    val filteredCrops = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            allCrops
        } else {
            CropsManager.searchCrops(searchQuery)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(localizedString(StringKey.SELECT_CROPS)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveCrops()
                            onNavigateBack()
                        },
                        enabled = selectedCrops.isNotEmpty()
                    ) {
                        Text(localizedString(StringKey.SAVE))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(localizedString(StringKey.SEARCH_CROPS)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )
            
            // Selected count
            Text(
                text = "${selectedCrops.size} ${localizedString(StringKey.SELECTED)}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Crops grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(120.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCrops) { crop ->
                        val currentLanguage = currentLanguage()
                        FilterChip(
                            selected = selectedCrops.contains(crop.id),
                            onClick = { viewModel.toggleCrop(crop.id) },
                            label = {
                                Text(
                                    text = "${crop.emoji} ${crop.getLocalizedName(currentLanguage)}",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            leadingIcon = if (selectedCrops.contains(crop.id)) {
                                {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

class CropSelectionViewModel(
    private val repository: FarmerChatRepository
) : androidx.lifecycle.ViewModel() {
    
    private val _selectedCrops = MutableStateFlow<Set<String>>(emptySet())
    val selectedCrops: StateFlow<Set<String>> = _selectedCrops.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadUserCrops()
    }
    
    private fun loadUserCrops() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    val userProfile = repository.getUserProfile(userId)
                    userProfile?.let {
                        _selectedCrops.value = it.crops.toSet()
                    }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun toggleCrop(cropName: String) {
        _selectedCrops.value = if (_selectedCrops.value.contains(cropName)) {
            _selectedCrops.value - cropName
        } else {
            _selectedCrops.value + cropName
        }
    }
    
    fun saveCrops() {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    val userProfile = repository.getUserProfile(userId)
                    userProfile?.let { profile ->
                        val updatedProfile = profile.copy(crops = _selectedCrops.value.toList())
                        repository.saveUserProfile(updatedProfile)
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}