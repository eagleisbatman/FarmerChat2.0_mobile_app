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
import com.digitalgreen.farmerchat.data.Conversation
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    onNavigateToChat: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ConversationsViewModel = viewModel()
) {
    val conversations by viewModel.conversations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showTagFilter by remember { mutableStateOf(false) }
    
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
                                    "Search conversations",
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
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF075E54),
                        titleContentColor = Color.White
                    )
                )
            } else {
                // Normal App Bar
                TopAppBar(
                    title = { 
                        Text(
                            "FarmerChat",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF075E54),
                        titleContentColor = Color.White
                    ),
                    actions = {
                        IconButton(onClick = { isSearching = true }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White
                            )
                        }
                        if (allTags.isNotEmpty()) {
                            IconButton(onClick = { showTagFilter = !showTagFilter }) {
                                Icon(
                                    if (showTagFilter) Icons.Default.FilterListOff else Icons.Default.FilterList,
                                    contentDescription = if (showTagFilter) "Hide filters" else "Show filters",
                                    tint = if (selectedTags.isNotEmpty()) Color(0xFF25D366) else Color.White
                                )
                            }
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Settings",
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
                containerColor = Color(0xFF25D366),
                contentColor = Color.White
            ) {
                Icon(
                    Icons.Default.ChatBubbleOutline,
                    contentDescription = "New chat"
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
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            if (selectedTags.isNotEmpty()) {
                                AssistChip(
                                    onClick = { selectedTags = emptySet() },
                                    label = { Text("Clear all") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            modifier = Modifier.size(16.dp)
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
                                            contentDescription = "Selected",
                                            modifier = Modifier.size(16.dp)
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
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                Icon(
                    if (searchQuery.isNotEmpty()) Icons.Default.SearchOff else Icons.Outlined.Forum,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = Color.Gray.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    if (searchQuery.isNotEmpty()) "No results found" else "No conversations yet",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (searchQuery.isNotEmpty()) {
                        "Try searching with different keywords"
                    } else {
                        "Tap the button below to start a new chat"
                    },
                    fontSize = 16.sp,
                    color = Color.Gray.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        items(filteredConversations) { conversation ->
                            ConversationItem(
                                conversation = conversation,
                                onClick = { onNavigateToChat(conversation.id) }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 16.dp),
                                color = Color.Gray.copy(alpha = 0.2f),
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Title
            Text(
                conversation.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Time
            Text(
                formatTimestamp(conversation.lastMessageTime),
                fontSize = 13.sp,
                color = if (conversation.hasUnreadMessages) {
                    Color(0xFF25D366)
                } else {
                    Color.Gray
                }
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
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
                                fontSize = 11.sp,
                                maxLines = 1
                            ) 
                        },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .height(24.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f),
                            labelColor = Color(0xFF4CAF50)
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = Color(0xFF4CAF50).copy(alpha = 0.3f)
                        )
                    )
                }
                if (conversation.tags.size > 3) {
                    Text(
                        "+${conversation.tags.size - 3}",
                        fontSize = 11.sp,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
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
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    conversation.lastMessage,
                    fontSize = 15.sp,
                    color = Color.Gray,
                    maxLines = if (conversation.tags.isEmpty()) 2 else 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
            }
            
            // Unread count
            if (conversation.unreadCount > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF25D366)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        conversation.unreadCount.toString(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun formatTimestamp(date: Date): String {
    val now = Calendar.getInstance()
    val messageTime = Calendar.getInstance().apply { time = date }
    
    return when {
        isSameDay(now, messageTime) -> {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        }
        isYesterday(now, messageTime) -> {
            "Yesterday"
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