package com.digitalgreen.farmerchat.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digitalgreen.farmerchat.data.Language
import com.digitalgreen.farmerchat.data.LanguageManager
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import com.digitalgreen.farmerchat.ui.components.localizedString

@Composable
fun EnhancedLanguageSelectionView(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    suggestedLanguages: List<String> = emptyList(),
    actualRegionalLanguages: List<String> = suggestedLanguages,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Debug logging
    LaunchedEffect(suggestedLanguages, actualRegionalLanguages, selectedLanguage) {
        android.util.Log.d("EnhancedLanguageSelection", "Selected language: $selectedLanguage")
        android.util.Log.d("EnhancedLanguageSelection", "Received suggestedLanguages: $suggestedLanguages")
        android.util.Log.d("EnhancedLanguageSelection", "Actual regional languages: $actualRegionalLanguages")
    }
    
    // Get all languages and organize them
    val (currentLanguageList, regionalLanguages, otherLanguages) = remember(searchQuery, suggestedLanguages, actualRegionalLanguages, selectedLanguage) {
        val allLanguages = if (searchQuery.isEmpty()) {
            LanguageManager.languages
        } else {
            LanguageManager.searchLanguages(searchQuery)
        }
        
        // If we have regional languages and no search query
        if (actualRegionalLanguages.isNotEmpty() && searchQuery.isEmpty()) {
            // Get current language inside remember block
            val currentLang = LanguageManager.getLanguageByCode(selectedLanguage)
            
            // Check if current language is regional
            val isCurrentRegional = selectedLanguage in actualRegionalLanguages
            
            android.util.Log.d("EnhancedLanguageSelection", "Selected language code: $selectedLanguage")
            android.util.Log.d("EnhancedLanguageSelection", "Is current regional: $isCurrentRegional")
            android.util.Log.d("EnhancedLanguageSelection", "Current language object: $currentLang")
            
            // Get languages for each category
            val currentLangList = if (!isCurrentRegional && currentLang != null) {
                listOf(currentLang)
            } else {
                emptyList()
            }
            
            // Get only actual regional languages
            val regional = allLanguages.filter { it.code in actualRegionalLanguages }
            
            // Get all other languages (excluding current and regional)
            val excludeSet = if (isCurrentRegional) {
                actualRegionalLanguages.toSet()
            } else {
                actualRegionalLanguages.toSet() + selectedLanguage
            }
            val others = allLanguages.filter { it.code !in excludeSet }
            
            android.util.Log.d("EnhancedLanguageSelection", "Current list: ${currentLangList.map { it.code }}")
            android.util.Log.d("EnhancedLanguageSelection", "Regional list: ${regional.map { it.code }}")
            android.util.Log.d("EnhancedLanguageSelection", "Others list size: ${others.size}")
            
            Triple(currentLangList, regional, others)
        } else {
            // No regional languages or searching - show all languages
            Triple(emptyList<Language>(), emptyList(), allLanguages)
        }
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text(localizedString(StringKey.SEARCH)) },
            placeholder = { Text(localizedString(StringKey.SEARCH_LANGUAGES_FULL_PLACEHOLDER)) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = localizedString(StringKey.SEARCH))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = localizedString(StringKey.CLEAR))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignSystem.Spacing.md)
                .padding(bottom = DesignSystem.Spacing.md),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )
        
        // Show total language count
        if (searchQuery.isEmpty()) {
            Text(
                text = localizedString(StringKey.CHOOSE_FROM_LANGUAGES),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = DesignSystem.Spacing.md, vertical = DesignSystem.Spacing.xs)
            )
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = DesignSystem.Spacing.md, vertical = DesignSystem.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
        ) {
            // Show current language if it's not regional
            if (currentLanguageList.isNotEmpty()) {
                item {
                    Text(
                        text = localizedString(StringKey.YOUR_CURRENT_LANGUAGE),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = DesignSystem.Spacing.sm)
                    )
                }
                
                items(currentLanguageList) { language ->
                    EnhancedLanguageCard(
                        language = language,
                        isSelected = true,
                        isSuggested = false,
                        onClick = { onLanguageSelected(language.code) }
                    )
                }
                
                if (regionalLanguages.isNotEmpty()) {
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = DesignSystem.Spacing.md),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
            
            // Show regional languages
            if (regionalLanguages.isNotEmpty()) {
                item {
                    Text(
                        text = localizedString(StringKey.SUGGESTED_FOR_YOUR_REGION),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = DesignSystem.Spacing.sm)
                    )
                }
                
                items(regionalLanguages) { language ->
                    EnhancedLanguageCard(
                        language = language,
                        isSelected = selectedLanguage == language.code,
                        isSuggested = true,
                        onClick = { onLanguageSelected(language.code) }
                    )
                }
                
                if (otherLanguages.isNotEmpty()) {
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = DesignSystem.Spacing.md),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Text(
                            text = localizedString(StringKey.ALL_OTHER_LANGUAGES),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = DesignSystem.Spacing.sm)
                        )
                    }
                }
            }
            
            // Show other languages
            items(otherLanguages) { language ->
                EnhancedLanguageCard(
                    language = language,
                    isSelected = selectedLanguage == language.code,
                    isSuggested = false,
                    onClick = { onLanguageSelected(language.code) }
                )
            }
        }
    }
}

@Composable
fun EnhancedLanguageCard(
    language: Language,
    isSelected: Boolean,
    isSuggested: Boolean,
    onClick: () -> Unit
) {
    // Debug log
    if (language.code == "am") {
        LaunchedEffect(isSuggested) {
            android.util.Log.d("EnhancedLanguageCard", "Amharic card - isSuggested: $isSuggested")
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primary
                isSuggested -> MaterialTheme.colorScheme.surface
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else if (isSuggested) 2.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignSystem.Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Language icon
            Surface(
                shape = CircleShape,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (isSuggested) Icons.Default.Translate else Icons.Default.Language,
                            contentDescription = null,
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else if (isSuggested) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(DesignSystem.Spacing.md))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = language.name,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    if (isSuggested) {
                        Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "Suggested",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = language.englishName,
                        fontSize = 14.sp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    if (language.region.isNotEmpty()) {
                        Text(
                            text = " • ${language.region}",
                            fontSize = 14.sp,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            }
                        )
                    }
                }
            }
            
            // RTL indicator
            if (language.isRTL) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = "RTL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}