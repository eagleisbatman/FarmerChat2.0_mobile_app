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
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Get all languages and organize them
    val (prioritizedLanguages, otherLanguages) = remember(searchQuery, suggestedLanguages) {
        val allLanguages = if (searchQuery.isEmpty()) {
            LanguageManager.languages
        } else {
            LanguageManager.searchLanguages(searchQuery)
        }
        
        // If we have suggested languages (based on country), prioritize them
        if (suggestedLanguages.isNotEmpty() && searchQuery.isEmpty()) {
            val suggestedSet = suggestedLanguages.toSet()
            val prioritized = allLanguages.filter { it.code in suggestedSet }
            val others = allLanguages.filter { it.code !in suggestedSet }
            prioritized to others
        } else {
            emptyList<Language>() to allLanguages
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
                text = "Choose from ${LanguageManager.languages.size} languages",
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
            // Show prioritized languages first (if any)
            if (prioritizedLanguages.isNotEmpty()) {
                item {
                    Text(
                        text = "Suggested for your region",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = DesignSystem.Spacing.sm)
                    )
                }
                
                items(prioritizedLanguages) { language ->
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
                            text = "All other languages",
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
                    if (isSuggested && !isSelected) {
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