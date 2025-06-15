package com.digitalgreen.farmerchat.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.platform.LocalContext
import com.digitalgreen.farmerchat.data.Conversation
import com.digitalgreen.farmerchat.data.LanguageManager
import com.digitalgreen.farmerchat.ui.components.FarmerChatAppBar
import com.digitalgreen.farmerchat.ui.components.localizedString
import com.digitalgreen.farmerchat.ui.components.currentLanguage
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.ui.theme.appBarColor
import com.digitalgreen.farmerchat.ui.theme.primaryTextColor
import com.digitalgreen.farmerchat.ui.theme.secondaryTextColor
import com.digitalgreen.farmerchat.utils.StringsManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    onNavigateToChat: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    startNewChat: Boolean = false,
    viewModel: ConversationsViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val conversations by viewModel.conversations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showTagFilter by remember { mutableStateOf(false) }
    val languageCode = currentLanguage()
    
    // Get all unique tags from conversations
    val allTags = remember(conversations) {
        conversations.flatMap { it.tags }.distinct().sorted()
    }
    
    // Filter conversations based on search and tags
    val filteredConversations = remember(conversations, searchQuery, selectedTags) {
        conversations.filter { conversation ->
            val matchesSearch = searchQuery.isEmpty() || 
                conversation.title.contains(searchQuery, ignoreCase = true) ||
                conversation.lastMessage.contains(searchQuery, ignoreCase = true) ||
                conversation.tags.any { it.contains(searchQuery, ignoreCase = true) }
            
            val matchesTags = selectedTags.isEmpty() ||
                selectedTags.any { tag -> conversation.tags.contains(tag) }
            
            matchesSearch && matchesTags
        }
    }
    
    // Handle startNewChat navigation
    LaunchedEffect(startNewChat) {
        if (startNewChat) {
            viewModel.createNewConversation { conversationId ->
                onNavigateToChat(conversationId)
            }
        }
    }
    
    // Debug logging
    LaunchedEffect(conversations, isLoading) {
        android.util.Log.d("ConversationsScreen", "UI State - isLoading: $isLoading, conversations: ${conversations.size}")
        conversations.forEach { conv ->
            android.util.Log.d("ConversationsScreen", "Conv: ${conv.id} - ${conv.title}")
        }
    }
    
    Scaffold(
        topBar = {
            if (isSearching) {
                // Search Bar
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { 
                                Text(
                                    localizedString(StringKey.SEARCH_CONVERSATIONS),
                                    color = Color.White.copy(alpha = 0.7f)
                                ) 
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                cursorColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { 
                            isSearching = false
                            searchQuery = ""
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = localizedString(StringKey.BACK),
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = localizedString(StringKey.CLEAR),
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = appBarColor(),
                        titleContentColor = Color.White
                    )
                )
            } else {
                // Normal App Bar - Using regular size for better space utilization
                FarmerChatAppBar(
                    title = localizedString(StringKey.APP_NAME),
                    actions = {
                        IconButton(onClick = { isSearching = true }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = localizedString(StringKey.SEARCH),
                                tint = Color.White
                            )
                        }
                        if (allTags.isNotEmpty()) {
                            IconButton(onClick = { showTagFilter = !showTagFilter }) {
                                Icon(
                                    if (showTagFilter) Icons.Default.FilterListOff else Icons.Default.FilterList,
                                    contentDescription = if (showTagFilter) localizedString(StringKey.HIDE_FILTERS) else localizedString(StringKey.SHOW_FILTERS),
                                    tint = if (selectedTags.isNotEmpty()) DesignSystem.Colors.Primary else Color.White
                                )
                            }
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = localizedString(StringKey.SETTINGS),
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Create new conversation
                    viewModel.createNewConversation { conversationId ->
                        onNavigateToChat(conversationId)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    Icons.Default.ChatBubbleOutline,
                    contentDescription = localizedString(StringKey.NEW_CONVERSATION)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tag filter chips
            AnimatedVisibility(
                visible = showTagFilter && allTags.isNotEmpty(),
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Column {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = DesignSystem.Spacing.md, vertical = DesignSystem.Spacing.sm),
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
                    ) {
                        item {
                            if (selectedTags.isNotEmpty()) {
                                AssistChip(
                                    onClick = { selectedTags = emptySet() },
                                    label = { Text(localizedString(StringKey.CLEAR) + " " + localizedString(StringKey.ALL)) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = localizedString(StringKey.CLEAR),
                                            modifier = Modifier.size(DesignSystem.IconSize.small)
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        labelColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                )
                            }
                        }
                        items(allTags.size) { index ->
                            val tag = allTags[index]
                            FilterChip(
                                selected = selectedTags.contains(tag),
                                onClick = {
                                    selectedTags = if (selectedTags.contains(tag)) {
                                        selectedTags - tag
                                    } else {
                                        selectedTags + tag
                                    }
                                },
                                label = { Text(tag) },
                                leadingIcon = if (selectedTags.contains(tag)) {
                                    {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = localizedString(StringKey.SELECTED),
                                            modifier = Modifier.size(DesignSystem.IconSize.small)
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
            
            // Main content
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                filteredConversations.isEmpty() -> {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(DesignSystem.Spacing.xl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                Icon(
                    if (searchQuery.isNotEmpty()) Icons.Default.SearchOff else Icons.Outlined.Forum,
                    contentDescription = null,
                    modifier = Modifier.size(DesignSystem.IconSize.splash),
                    tint = secondaryTextColor().copy(alpha = DesignSystem.Opacity.medium)
                )
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.lg))
                Text(
                    if (searchQuery.isNotEmpty()) localizedString(StringKey.NO_RESULTS_FOUND) else localizedString(StringKey.NO_CONVERSATIONS),
                    fontSize = DesignSystem.Typography.titleMedium,
                    fontWeight = DesignSystem.Typography.Weight.Medium,
                    color = secondaryTextColor()
                )
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
                Text(
                    if (searchQuery.isNotEmpty()) {
                        localizedString(StringKey.TRY_DIFFERENT_KEYWORDS)
                    } else {
                        localizedString(StringKey.START_FIRST_CONVERSATION)
                    },
                    fontSize = DesignSystem.Typography.bodyLarge,
                    color = secondaryTextColor().copy(alpha = DesignSystem.Opacity.high),
                    textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.xxs)
                    ) {
                        items(filteredConversations) { conversation ->
                            ConversationItem(
                                conversation = conversation,
                                onClick = { onNavigateToChat(conversation.id) }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = DesignSystem.Spacing.md),
                                color = secondaryTextColor().copy(alpha = 0.2f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    val languageCode = currentLanguage()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = DesignSystem.Spacing.md, vertical = DesignSystem.Spacing.sm + DesignSystem.Spacing.xs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Title
            Text(
                conversation.getLocalizedTitle(languageCode),
                fontSize = DesignSystem.Typography.titleSmall,
                fontWeight = DesignSystem.Typography.Weight.Medium,
                color = primaryTextColor(),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Time
            Text(
                formatTimestamp(conversation.lastMessageTime, languageCode),
                fontSize = DesignSystem.Typography.labelMedium,
                color = if (conversation.hasUnreadMessages) {
                    DesignSystem.Colors.Primary
                } else {
                    secondaryTextColor()
                }
            )
        }
        
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
        
        // Tags
        if (conversation.tags.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                conversation.tags.take(3).forEach { tag ->
                    AssistChip(
                        onClick = { },
                        label = { 
                            Text(
                                tag, 
                                fontSize = DesignSystem.Typography.labelSmall,
                                maxLines = 1
                            ) 
                        },
                        modifier = Modifier
                            .padding(end = DesignSystem.Spacing.xs)
                            .height(DesignSystem.Spacing.lg),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = DesignSystem.Colors.Primary.copy(alpha = 0.1f),
                            labelColor = DesignSystem.Colors.Primary
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = DesignSystem.Colors.Primary.copy(alpha = 0.3f)
                        )
                    )
                }
                if (conversation.tags.size > 3) {
                    Text(
                        "+${conversation.tags.size - 3}",
                        fontSize = DesignSystem.Typography.labelSmall,
                        color = DesignSystem.Colors.Primary,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Last message preview
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!conversation.lastMessageIsUser) {
                    Icon(
                        Icons.Default.SmartToy,
                        contentDescription = null,
                        modifier = Modifier.size(DesignSystem.IconSize.small),
                        tint = secondaryTextColor()
                    )
                    Spacer(modifier = Modifier.width(DesignSystem.Spacing.xs))
                }
                Text(
                    // Check if this is the default message and localize it
                    if (conversation.lastMessage == "Start a conversation..." || 
                        conversation.lastMessage == "बातचीत शुरू करें..." ||
                        conversation.lastMessage == "Anza mazungumzo...") {
                        localizedString(StringKey.START_A_CONVERSATION)
                    } else {
                        conversation.lastMessage
                    },
                    fontSize = DesignSystem.Typography.bodyMedium,
                    color = secondaryTextColor(),
                    maxLines = if (conversation.tags.isEmpty()) 2 else 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = DesignSystem.Typography.titleMedium
                )
            }
            
            // Unread count
            if (conversation.unreadCount > 0) {
                Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
                Box(
                    modifier = Modifier
                        .size(DesignSystem.Spacing.lg)
                        .clip(CircleShape)
                        .background(DesignSystem.Colors.Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        conversation.unreadCount.toString(),
                        color = Color.White,
                        fontSize = DesignSystem.Typography.bodySmall,
                        fontWeight = DesignSystem.Typography.Weight.Bold
                    )
                }
            }
        }
    }
}

fun formatTimestamp(date: Date, languageCode: String): String {
    val now = Calendar.getInstance()
    val messageTime = Calendar.getInstance().apply { time = date }
    
    return when {
        isSameDay(now, messageTime) -> {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        }
        isYesterday(now, messageTime) -> {
            StringsManager.getString(StringKey.YESTERDAY, languageCode)
        }
        isSameWeek(now, messageTime) -> {
            SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
        }
        else -> {
            SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date)
        }
    }
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun isYesterday(today: Calendar, other: Calendar): Boolean {
    val yesterday = today.clone() as Calendar
    yesterday.add(Calendar.DAY_OF_YEAR, -1)
    return isSameDay(yesterday, other)
}

fun isSameWeek(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
}