package com.digitalgreen.farmerchat.ui.screens

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digitalgreen.farmerchat.data.ChatMessage
import com.digitalgreen.farmerchat.data.StarterQuestion
import com.digitalgreen.farmerchat.ui.components.FeedbackDialog
import com.digitalgreen.farmerchat.ui.components.MessageBubble
import com.digitalgreen.farmerchat.ui.components.QuestionChip
import com.digitalgreen.farmerchat.ui.components.CompactQuestionChip
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ChatScreen(
    conversationId: String,
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val starterQuestions by viewModel.starterQuestions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val speechError by viewModel.speechError.collectAsState()
    val followUpQuestions by viewModel.followUpQuestions.collectAsState()
    val conversationTitles by viewModel.conversationTitles.collectAsState()
    
    val conversationTitle = conversationTitles[conversationId] ?: "New Conversation"
    
    var textInput by remember { mutableStateOf("") }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var feedbackMessageId by remember { mutableStateOf<String?>(null) }
    
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Permission for recording audio
    val recordAudioPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    
    // Auto-scroll to bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                scrollState.animateScrollToItem(messages.size - 1)
            }
        }
    }
    
    // Show speech error as snackbar
    LaunchedEffect(speechError) {
        speechError?.let { error ->
            coroutineScope.launch {
                // Show error briefly
                delay(3000)
                viewModel.clearSpeechError()
            }
        }
    }
    
    // Initialize with conversation ID
    LaunchedEffect(conversationId) {
        viewModel.initializeChat(conversationId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = conversationTitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF075E54),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { /* TODO: More options */ }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Chat messages area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Show starter questions if no messages
                    if (messages.isEmpty() && starterQuestions.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(60.dp))
                                
                                Text(
                                    text = "Ask me anything or try one of the below:",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                        
                        items(starterQuestions) { question ->
                            QuestionChip(
                                text = question.question,
                                category = question.category,
                                onClick = {
                                    viewModel.sendMessage(question.question)
                                },
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                            )
                        }
                } else {
                    // Show chat messages
                    items(messages) { message ->
                        MessageBubble(
                            message = message,
                            onPlayAudio = {
                                if (isSpeaking) {
                                    viewModel.stopSpeaking()
                                } else {
                                    viewModel.speakMessage(message.content)
                                }
                            },
                            isSpeaking = isSpeaking,
                            onFeedback = {
                                feedbackMessageId = message.id
                                showFeedbackDialog = true
                            }
                        )
                    }
                    
                    // Show typing indicator when loading
                    if (isLoading) {
                        item {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        repeat(3) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(
                                                        Color.Gray,
                                                        shape = CircleShape
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
            
            // Follow-up questions
            AnimatedVisibility(
                visible = followUpQuestions.isNotEmpty() && !isLoading
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(followUpQuestions) { question ->
                        CompactQuestionChip(
                            text = question,
                            onClick = {
                                viewModel.sendMessage(question)
                            }
                        )
                    }
                }
            }
            
            // Input area
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    HorizontalDivider()
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Text input field
                        TextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp)),
                            placeholder = { 
                                Text(
                                    "Ask about farming...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                ) 
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            trailingIcon = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Voice recording button
                                    IconButton(
                                        onClick = {
                                            if (recordAudioPermission.status.isGranted) {
                                                if (isRecording) {
                                                    viewModel.stopRecording()
                                                } else {
                                                    viewModel.startRecording()
                                                }
                                            } else {
                                                recordAudioPermission.launchPermissionRequest()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                            contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                                            tint = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    // Send button
                                    IconButton(
                                        onClick = {
                                            if (textInput.isNotBlank()) {
                                                viewModel.sendMessage(textInput)
                                                textInput = ""
                                            }
                                        },
                                        enabled = textInput.isNotBlank() && !isLoading
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription = "Send",
                                            tint = if (textInput.isNotBlank() && !isLoading) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            }
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Feedback dialog
    if (showFeedbackDialog && feedbackMessageId != null) {
        FeedbackDialog(
            onDismiss = { showFeedbackDialog = false },
            onSubmit = { rating, comment ->
                viewModel.submitFeedback(feedbackMessageId!!, rating, comment)
                showFeedbackDialog = false
            }
        )
    }
}