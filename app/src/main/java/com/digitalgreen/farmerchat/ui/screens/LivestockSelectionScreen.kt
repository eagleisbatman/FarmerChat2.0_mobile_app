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
import com.digitalgreen.farmerchat.data.LivestockManager
import com.digitalgreen.farmerchat.ui.components.localizedString
import com.digitalgreen.farmerchat.ui.components.currentLanguage
import com.digitalgreen.farmerchat.ui.components.FarmerChatAppBar
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivestockSelectionScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: LivestockSelectionViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return LivestockSelectionViewModel(
                    repository = FarmerChatRepository()
                ) as T
            }
        }
    )
    
    val selectedLivestock by viewModel.selectedLivestock.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    val allLivestock = remember { LivestockManager.getAllLivestock() }
    val filteredLivestock = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            allLivestock
        } else {
            LivestockManager.searchLivestock(searchQuery)
        }
    }
    
    Scaffold(
        topBar = {
            FarmerChatAppBar(
                title = localizedString(StringKey.SELECT_LIVESTOCK),
                onBackClick = onNavigateBack,
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveLivestock()
                            onNavigateBack()
                        },
                        enabled = selectedLivestock.isNotEmpty()
                    ) {
                        Text(
                            text = localizedString(StringKey.SAVE),
                            color = Color.White
                        )
                    }
                }
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
                label = { Text(localizedString(StringKey.SEARCH_LIVESTOCK)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignSystem.Spacing.md),
                singleLine = true
            )
            
            // Selected count
            Text(
                text = "${selectedLivestock.size} ${localizedString(StringKey.SELECTED)}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(
                    horizontal = DesignSystem.Spacing.md, 
                    vertical = DesignSystem.Spacing.sm
                )
            )
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Livestock grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(120.dp),
                    contentPadding = PaddingValues(DesignSystem.Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
                ) {
                    items(filteredLivestock) { livestock ->
                        val currentLanguage = currentLanguage()
                        FilterChip(
                            selected = selectedLivestock.contains(livestock.id),
                            onClick = { viewModel.toggleLivestock(livestock.id) },
                            label = {
                                Text(
                                    text = "${livestock.emoji} ${livestock.getLocalizedName(currentLanguage)}",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            leadingIcon = if (selectedLivestock.contains(livestock.id)) {
                                {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = localizedString(StringKey.SELECTED),
                                        modifier = Modifier.size(DesignSystem.IconSize.small)
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

class LivestockSelectionViewModel(
    private val repository: FarmerChatRepository
) : androidx.lifecycle.ViewModel() {
    
    private val _selectedLivestock = MutableStateFlow<Set<String>>(emptySet())
    val selectedLivestock: StateFlow<Set<String>> = _selectedLivestock.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadUserLivestock()
    }
    
    private fun loadUserLivestock() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    val userProfile = repository.getUserProfile(userId)
                    userProfile?.let {
                        _selectedLivestock.value = it.livestock.toSet()
                    }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
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
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    val userProfile = repository.getUserProfile(userId)
                    userProfile?.let { profile ->
                        val updatedProfile = profile.copy(livestock = _selectedLivestock.value.toList())
                        repository.saveUserProfile(updatedProfile)
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}