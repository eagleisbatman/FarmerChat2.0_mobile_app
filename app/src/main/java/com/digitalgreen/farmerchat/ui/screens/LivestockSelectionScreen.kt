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
import com.digitalgreen.farmerchat.data.AppRepository
import com.digitalgreen.farmerchat.data.LivestockManager
import com.digitalgreen.farmerchat.ui.components.localizedString
import com.digitalgreen.farmerchat.ui.components.currentLanguage
import com.digitalgreen.farmerchat.ui.components.FarmerChatAppBar
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivestockSelectionScreen(
    onNavigateBack: () -> Unit,
    onLivestockSelected: () -> Unit,
    showAppBar: Boolean = true,
    viewModel: LivestockSelectionViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val selectedLivestock by viewModel.selectedLivestock.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val filteredLivestock by viewModel.filteredLivestock.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val currentLanguageCode = currentLanguage()
    
    val appTitle = localizedString(StringKey.SELECT_LIVESTOCK)
    val searchPlaceholder = localizedString(StringKey.SEARCH_LIVESTOCK)
    val saveButtonText = localizedString(StringKey.SAVE)
    
    Scaffold(
        topBar = {
            if (showAppBar) {
                FarmerChatAppBar(
                    title = appTitle,
                    onBackClick = onNavigateBack
                )
            }
        },
        bottomBar = {
            if (showAppBar) {
                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignSystem.Spacing.md)
                    ) {
                        Button(
                            onClick = {
                                viewModel.saveLivestock()
                                onLivestockSelected()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Text(saveButtonText)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = DesignSystem.Spacing.md)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                    viewModel.filterLivestock(query, currentLanguageCode)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = DesignSystem.Spacing.sm),
                placeholder = { Text(searchPlaceholder) },
                singleLine = true
            )
            
            // Selection count
            Text(
                text = "${selectedLivestock.size} ${localizedString(StringKey.SELECTED)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = DesignSystem.Spacing.sm)
            )
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredLivestock) { livestock ->
                        LivestockItem(
                            livestockName = livestock.getLocalizedName(currentLanguageCode),
                            isSelected = selectedLivestock.contains(livestock.id),
                            onToggle = { viewModel.toggleLivestock(livestock.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LivestockItem(
    livestockName: String,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) CardDefaults.outlinedCardBorder() else null
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = livestockName,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = DesignSystem.Spacing.sm)
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(DesignSystem.Spacing.xs)
                        .size(DesignSystem.IconSize.small),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

class LivestockSelectionViewModel(
    application: android.app.Application
) : androidx.lifecycle.AndroidViewModel(application) {
    
    private val repository = (application as com.digitalgreen.farmerchat.FarmerChatApplication).repository
    
    private val _selectedLivestock = MutableStateFlow<Set<String>>(emptySet())
    val selectedLivestock: StateFlow<Set<String>> = _selectedLivestock.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _filteredLivestock = MutableStateFlow(LivestockManager.getAllLivestock())
    val filteredLivestock: StateFlow<List<LivestockManager.Livestock>> = _filteredLivestock.asStateFlow()
    
    init {
        loadUserLivestock()
    }
    
    private fun loadUserLivestock() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getUserProfile().onSuccess { user ->
                _selectedLivestock.value = user.livestock.toSet()
            }.onFailure { e ->
                android.util.Log.e("LivestockSelection", "Failed to load user profile", e)
            }
            _isLoading.value = false
        }
    }
    
    fun toggleLivestock(livestockName: String) {
        _selectedLivestock.value = if (_selectedLivestock.value.contains(livestockName)) {
            _selectedLivestock.value - livestockName
        } else {
            _selectedLivestock.value + livestockName
        }
    }
    
    fun saveLivestock() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateUserProfile(livestock = _selectedLivestock.value.toList()).onSuccess {
                // Success
            }.onFailure { e ->
                android.util.Log.e("LivestockSelection", "Failed to update livestock", e)
            }
            _isLoading.value = false
        }
    }
    
    fun filterLivestock(query: String, languageCode: String) {
        _filteredLivestock.value = if (query.isBlank()) {
            LivestockManager.getAllLivestock()
        } else {
            LivestockManager.searchLivestock(query)
        }
    }
}