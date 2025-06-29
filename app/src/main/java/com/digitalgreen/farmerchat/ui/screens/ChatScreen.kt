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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.digitalgreen.farmerchat.data.ChatMessage
import com.digitalgreen.farmerchat.data.StarterQuestion
import com.digitalgreen.farmerchat.ui.components.FeedbackDialog
import com.digitalgreen.farmerchat.ui.components.MessageBubbleV2
import com.digitalgreen.farmerchat.ui.components.QuestionChip
import com.digitalgreen.farmerchat.ui.components.CompactQuestionChip
import com.digitalgreen.farmerchat.ui.components.VoiceRecordingButton
import com.digitalgreen.farmerchat.ui.components.FarmerChatAppBar
import com.digitalgreen.farmerchat.ui.components.localizedString
import com.digitalgreen.farmerchat.ui.components.MessageSkeleton
import com.digitalgreen.farmerchat.ui.components.StarterQuestionsSkeleton
import com.digitalgreen.farmerchat.ui.components.TypingIndicator
import com.digitalgreen.farmerchat.ui.components.AudioRecordingView
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.ui.theme.primaryTextColor
import com.digitalgreen.farmerchat.ui.theme.secondaryTextColor
import com.digitalgreen.farmerchat.utils.SpeechRecognitionManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ChatScreen(
    conversationId: String,
    onNavigateBack: () -> Unit,
    onNavigateToNewChat: () -> Unit = {},
    viewModel: ApiChatViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val starterQuestions by viewModel.starterQuestions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val speechError by viewModel.speechError.collectAsState()
    val followUpQuestions by viewModel.followUpQuestions.collectAsState()
    val recognizedText by viewModel.recognizedText.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    // val speechConfidence by viewModel.speechConfidence.collectAsState()
    val speechConfidence = 1.0f // Default confidence
    val currentConversation by viewModel.currentConversation.collectAsState()
    val currentStreamingMessage by viewModel.currentStreamingMessage.collectAsState()
    
    // Get state from ViewModel instead of creating dummy state
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val starterQuestionsLoading by viewModel.starterQuestionsLoading.collectAsState()
    val starterQuestionsError by viewModel.starterQuestionsError.collectAsState()
    
    // Audio recording states
    val audioRecordingState by viewModel.audioRecordingState.collectAsState()
    val audioRecordingDuration by viewModel.audioRecordingDuration.collectAsState()
    val audioPlaybackProgress by viewModel.audioPlaybackProgress.collectAsState()
    val audioLevel by viewModel.audioLevel.collectAsState()
    val isAudioRecording by viewModel.isAudioRecording.collectAsState()
    val isAudioPlaying by viewModel.isAudioPlaying.collectAsState()
    val audioRecordingError by viewModel.audioRecordingError.collectAsState()
    
    val conversationTitle = currentConversation?.title 
        ?: localizedString(StringKey.NEW_CONVERSATION)
    
    var textInput by remember { mutableStateOf("") }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var feedbackMessageId by remember { mutableStateOf<String?>(null) }
    var showVoiceFeedback by remember { mutableStateOf(false) }
    var showAudioRecording by remember { mutableStateOf(false) }
    
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Permission for recording audio
    val recordAudioPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    
    // Auto-scroll to bottom when new message arrives or streaming
    LaunchedEffect(messages.size, currentStreamingMessage) {
        coroutineScope.launch {
            if (messages.isNotEmpty() || currentStreamingMessage.isNotEmpty()) {
                val targetIndex = if (currentStreamingMessage.isNotEmpty()) {
                    messages.size // Scroll to streaming message
                } else {
                    messages.size - 1 // Scroll to last message
                }
                if (targetIndex >= 0) {
                    scrollState.animateScrollToItem(targetIndex)
                }
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
    
    // Handle recognized text from legacy speech recognition
    LaunchedEffect(recognizedText) {
        if (recognizedText.isNotEmpty() && !isRecording) {
            textInput = recognizedText
            viewModel.clearRecognizedText()
        }
    }
    
    // Handle transcribed text from audio recording
    val currentMessage by viewModel.currentMessage.collectAsState()
    LaunchedEffect(currentMessage) {
        if (currentMessage.isNotEmpty() && !showAudioRecording) {
            textInput = currentMessage
        }
    }
    
    // Show voice feedback when recording
    LaunchedEffect(isRecording) {
        showVoiceFeedback = isRecording
        if (!isRecording && showVoiceFeedback) {
            // Keep showing for a brief moment after recording stops
            delay(500)
            showVoiceFeedback = false
        }
    }
    
    // Initialize with conversation ID
    LaunchedEffect(conversationId) {
        viewModel.initializeChat(conversationId)
    }
    
    // Show audio recording error as snackbar
    LaunchedEffect(audioRecordingError) {
        audioRecordingError?.let { error ->
            coroutineScope.launch {
                // Show error briefly
                delay(3000)
                viewModel.clearAudioRecordingError()
            }
        }
    }
    
    Scaffold(
        topBar = {
            FarmerChatAppBar(
                title = conversationTitle,
                onBackClick = onNavigateBack,
                actions = {
                    // New Chat button
                    IconButton(onClick = onNavigateToNewChat) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = localizedString(StringKey.NEW_CONVERSATION),
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { /* TODO: More options */ }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = localizedString(StringKey.MORE),
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
                    verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.xs),
                    contentPadding = PaddingValues(vertical = DesignSystem.Spacing.md)
                ) {
                    // Show starter questions section if no messages and not loading
                    if (messages.isEmpty() && !isLoading) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = DesignSystem.Spacing.md + DesignSystem.Spacing.xs),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.xxxl - DesignSystem.Spacing.xs))
                                
                                Text(
                                    text = localizedString(StringKey.ASK_ME_ANYTHING),
                                    fontSize = DesignSystem.Typography.bodyLarge,
                                    color = secondaryTextColor(),
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.lg))
                                
                                // Only show skeleton on initial load, not when questions are already displayed
                                if (starterQuestionsLoading && starterQuestions.isEmpty()) {
                                    StarterQuestionsSkeleton()
                                } else if (starterQuestionsError != null && starterQuestions.isEmpty()) {
                                    Text(
                                        text = starterQuestionsError ?: "",
                                        fontSize = DesignSystem.Typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
                                }
                            }
                        }
                        
                        if (starterQuestions.isNotEmpty()) {
                            items(starterQuestions) { question ->
                                QuestionChip(
                                    text = question,
                                    category = "General", // Default category for API questions
                                    onClick = {
                                        android.util.Log.d("ChatScreen", "Starter question clicked: $question")
                                        viewModel.sendMessage(question)
                                    },
                                    modifier = Modifier.padding(horizontal = DesignSystem.Spacing.md + DesignSystem.Spacing.xs, vertical = DesignSystem.Spacing.xs)
                                )
                            }
                        }
                    } else if (messages.isNotEmpty() || isLoading) {
                        // Show skeleton when messages are loading
                        if (messages.isEmpty() && isLoading) {
                            items(3) { index ->
                                MessageSkeleton(isUser = false) // All skeletons left-aligned
                            }
                        }
                        
                        // Show chat messages
                        items(messages) { message ->
                        MessageBubbleV2(
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
                            },
                            currentLanguageCode = userProfile?.language ?: "en"
                        )
                    }
                    
                    // Show streaming message if it exists
                    if (currentStreamingMessage.isNotEmpty()) {
                        item {
                            MessageBubbleV2(
                                message = ChatMessage(
                                    content = currentStreamingMessage,
                                    isUser = false,
                                    timestamp = Date()
                                ),
                                onPlayAudio = { },
                                isSpeaking = false,
                                onFeedback = { },
                                currentLanguageCode = userProfile?.language ?: "en",
                                isStreaming = true
                            )
                        }
                    }
                    
                    // Don't show typing indicator as we already have skeletons
                }
            }
        }
            
            // Follow-up questions - always visible when available
            AnimatedVisibility(
                visible = followUpQuestions.isNotEmpty()
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignSystem.Spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
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
            
            // Audio recording view
            AnimatedVisibility(
                visible = showAudioRecording,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                AudioRecordingView(
                    recordingState = audioRecordingState,
                    recordingDuration = audioRecordingDuration,
                    playbackProgress = audioPlaybackProgress,
                    audioLevel = audioLevel,
                    isRecording = isAudioRecording,
                    isPlaying = isAudioPlaying,
                    onStartRecording = {
                        viewModel.startAudioRecording()
                    },
                    onStopRecording = {
                        viewModel.stopAudioRecording()
                    },
                    onPlayPause = {
                        viewModel.playPauseAudioRecording()
                    },
                    onDiscard = {
                        viewModel.discardAudioRecording()
                        showAudioRecording = false
                    },
                    onSendForTranscription = {
                        viewModel.sendAudioForTranscription()
                        showAudioRecording = false
                    }
                )
            }
            
            // Input area - only show when not loading and starter questions are loaded (if first message) and not showing audio recording
            val showInputControls = !isLoading && (messages.isNotEmpty() || (!starterQuestionsLoading && starterQuestions.isNotEmpty())) && !showAudioRecording
            
            AnimatedVisibility(
                visible = showInputControls,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = DesignSystem.Elevation.large,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column {
                        HorizontalDivider()
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DesignSystem.Spacing.md, vertical = DesignSystem.Spacing.sm),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Text input field
                            TextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(DesignSystem.Spacing.lg)),
                                placeholder = { 
                                    Text(
                                        localizedString(StringKey.TYPE_MESSAGE),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DesignSystem.Opacity.medium)
                                    ) 
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true,
                                enabled = !isRecording && !isLoading
                            )
                        
                        Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
                        
                        // Voice recording button
                        VoiceRecordingButton(
                            isRecording = false, // Not using legacy recording
                            onClick = {
                                if (recordAudioPermission.status.isGranted) {
                                    showAudioRecording = true
                                } else {
                                    recordAudioPermission.launchPermissionRequest()
                                }
                            }
                        )
                        
                        // Send button
                        AnimatedVisibility(
                            visible = textInput.isNotBlank() && !isLoading,
                            enter = scaleIn() + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        ) {
                            IconButton(
                                onClick = {
                                    if (textInput.isNotBlank()) {
                                        viewModel.sendMessage(textInput)
                                        textInput = ""
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = localizedString(StringKey.SEND),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        }
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