package com.digitalgreen.farmerchat.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digitalgreen.farmerchat.ui.components.FarmerChatAppBar
import com.digitalgreen.farmerchat.ui.components.currentLanguage
import com.digitalgreen.farmerchat.ui.components.localizedString
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.ui.theme.secondaryTextColor
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivestockSelectionScreen(
    onNavigateBack: () -> Unit,
    viewModel: ApiSettingsViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as android.app.Application
        )
    )
) {
    val settingsState by viewModel.settingsState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<com.digitalgreen.farmerchat.data.LivestockManager.LivestockCategory?>(null) }
    var selectedPurpose by remember { mutableStateOf<com.digitalgreen.farmerchat.data.LivestockManager.Purpose?>(null) }
    
    val currentLanguage = currentLanguage()
    val allLivestock = remember { com.digitalgreen.farmerchat.data.LivestockManager.getAllLivestock() }
    val categories = remember { com.digitalgreen.farmerchat.data.LivestockManager.getCategories() }
    val coroutineScope = rememberCoroutineScope()
    
    // Track local selection state
    var selectedLivestock by remember { mutableStateOf(settingsState.selectedLivestock.toSet()) }
    
    val filteredLivestock = remember(searchQuery, selectedCategory, selectedPurpose) {
        when {
            searchQuery.isNotEmpty() -> com.digitalgreen.farmerchat.data.LivestockManager.searchLivestock(searchQuery)
            selectedCategory != null -> com.digitalgreen.farmerchat.data.LivestockManager.getLivestockByCategory(selectedCategory!!)
            selectedPurpose != null -> com.digitalgreen.farmerchat.data.LivestockManager.getLivestockByPurpose(selectedPurpose!!)
            else -> allLivestock
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }
    
    LaunchedEffect(settingsState.selectedLivestock) {
        selectedLivestock = settingsState.selectedLivestock.toSet()
    }
    
    Scaffold(
        topBar = {
            FarmerChatAppBar(
                title = localizedString(StringKey.SELECT_LIVESTOCK),
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.updateSelectedLivestock(selectedLivestock.toList())
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignSystem.Spacing.md),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        localizedString(StringKey.SAVE),
                        modifier = Modifier.padding(vertical = DesignSystem.Spacing.sm)
                    )
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
            // Header with description
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = DesignSystem.Spacing.md),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(DesignSystem.Spacing.md)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
                        Text(
                            text = localizedString(StringKey.LIVESTOCK_SUBTITLE),
                            fontSize = DesignSystem.Typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
                    Text(
                        text = localizedString(StringKey.LIVESTOCK_BENEFIT),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
            
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
                    Icon(Icons.Default.Search, contentDescription = localizedString(StringKey.SEARCH))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { 
                            searchQuery = ""
                            selectedCategory = null
                            selectedPurpose = null
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = localizedString(StringKey.CLEAR))
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
                        label = { Text(localizedString(StringKey.ALL)) }
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
                        label = { Text(category.getLocalizedName(currentLanguage)) }
                    )
                }
            }
            
            // Selected livestock count
            if (selectedLivestock.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = DesignSystem.Spacing.sm),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignSystem.Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🐄",
                            fontSize = DesignSystem.IconSize.medium.value.sp
                        )
                        Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
                        Text(
                            text = "${selectedLivestock.size} ${localizedString(StringKey.ANIMALS_SELECTED)}",
                            fontSize = DesignSystem.Typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Livestock grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
            ) {
                items(filteredLivestock) { animal ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.3f)
                            .clickable { 
                                selectedLivestock = if (animal.id in selectedLivestock) {
                                    selectedLivestock - animal.id
                                } else {
                                    selectedLivestock + animal.id
                                }
                            },
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
                                .padding(DesignSystem.Spacing.sm),
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
                                textAlign = TextAlign.Center,
                                color = if (animal.id in selectedLivestock) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
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
        }
    }
}