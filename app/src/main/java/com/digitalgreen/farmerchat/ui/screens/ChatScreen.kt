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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ChatScreen(
    conversationId: String,
    onNavigateBack: () -> Unit,
    viewModel: ApiChatViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val starterQuestions by viewModel.starterQuestions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val speechError by viewModel.speechError.collectAsState()
    val followUpQuestions by viewModel.followUpQuestions.collectAsState()
    val recognizedText by viewModel.recognizedText.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val voiceConfidenceScore by viewModel.voiceConfidenceScore.collectAsState()
    val voiceConfidenceLevel = viewModel.voiceConfidenceLevel
    val starterQuestionsLoading by viewModel.starterQuestionsLoading.collectAsState()
    val starterQuestionsError by viewModel.starterQuestionsError.collectAsState()
    val currentConversation by viewModel.currentConversation.collectAsState()
    
    val conversationTitle = currentConversation?.getLocalizedTitle(userProfile?.language ?: "en") 
        ?: localizedString(StringKey.NEW_CONVERSATION)
    
    var textInput by remember { mutableStateOf("") }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var feedbackMessageId by remember { mutableStateOf<String?>(null) }
    var showVoiceFeedback by remember { mutableStateOf(false) }
    
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
    
    // Handle recognized text
    LaunchedEffect(recognizedText) {
        if (recognizedText.isNotEmpty() && !isRecording) {
            textInput = recognizedText
            viewModel.clearRecognizedText()
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
    
    Scaffold(
        topBar = {
            FarmerChatAppBar(
                title = conversationTitle,
                onBackClick = onNavigateBack,
                actions = {
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
                    // Show starter questions section if no messages
                    if (messages.isEmpty()) {
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
                                
                                // Show loading indicator for starter questions
                                if (starterQuestionsLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(DesignSystem.IconSize.medium),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
                                } else if (starterQuestionsError != null) {
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
                        
                        if (!starterQuestionsLoading && starterQuestions.isNotEmpty()) {
                            items(starterQuestions) { question ->
                                QuestionChip(
                                    text = question,
                                    category = "General", // Default category for API questions
                                    onClick = {
                                        viewModel.sendMessage(question)
                                    },
                                    modifier = Modifier.padding(horizontal = DesignSystem.Spacing.md + DesignSystem.Spacing.xs, vertical = DesignSystem.Spacing.xs)
                                )
                            }
                        }
                    } else {
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
                    
                    // Show typing indicator when loading
                    if (isLoading) {
                        item {
                            Row(
                                modifier = Modifier.padding(DesignSystem.Spacing.sm),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(DesignSystem.Spacing.sm + DesignSystem.Spacing.xs),
                                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.xs)
                                    ) {
                                        repeat(3) {
                                            Box(
                                                modifier = Modifier
                                                    .size(DesignSystem.Spacing.sm)
                                                    .background(
                                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DesignSystem.Opacity.medium),
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
            
            // Voice recording feedback
            AnimatedVisibility(
                visible = showVoiceFeedback,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = DesignSystem.Elevation.medium
                ) {
                    Column(
                        modifier = Modifier.padding(DesignSystem.Spacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(DesignSystem.IconSize.medium)
                            )
                            Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
                            Text(
                                text = if (isRecording) {
                                    localizedString(StringKey.LISTENING)
                                } else {
                                    localizedString(StringKey.PROCESSING)
                                },
                                fontWeight = DesignSystem.Typography.Weight.Medium
                            )
                        }
                        
                        if (recognizedText.isNotEmpty() && isRecording) {
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
                            Text(
                                text = recognizedText,
                                fontSize = DesignSystem.Typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = DesignSystem.Opacity.high - 0.17f),
                                textAlign = TextAlign.Center
                            )
                            
                            // Confidence indicator
                            if (voiceConfidenceScore > 0) {
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    LinearProgressIndicator(
                                        progress = voiceConfidenceScore,
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(DesignSystem.Spacing.xs)
                                            .clip(RoundedCornerShape(DesignSystem.Spacing.xxs)),
                                        color = when (voiceConfidenceLevel) {
                                            SpeechRecognitionManager.ConfidenceLevel.HIGH -> DesignSystem.Colors.Success
                                            SpeechRecognitionManager.ConfidenceLevel.MEDIUM -> DesignSystem.Colors.Warning
                                            SpeechRecognitionManager.ConfidenceLevel.LOW -> DesignSystem.Colors.Error
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
                                    Text(
                                        text = when (voiceConfidenceLevel) {
                                            SpeechRecognitionManager.ConfidenceLevel.HIGH -> localizedString(StringKey.CONFIDENCE_HIGH)
                                            SpeechRecognitionManager.ConfidenceLevel.MEDIUM -> localizedString(StringKey.CONFIDENCE_MEDIUM)
                                            SpeechRecognitionManager.ConfidenceLevel.LOW -> localizedString(StringKey.CONFIDENCE_LOW)
                                        },
                                        fontSize = DesignSystem.Typography.bodySmall,
                                        color = when (voiceConfidenceLevel) {
                                            SpeechRecognitionManager.ConfidenceLevel.HIGH -> DesignSystem.Colors.Success
                                            SpeechRecognitionManager.ConfidenceLevel.MEDIUM -> DesignSystem.Colors.Warning
                                            SpeechRecognitionManager.ConfidenceLevel.LOW -> DesignSystem.Colors.Error
                                        },
                                        fontWeight = DesignSystem.Typography.Weight.Medium
                                    )
                                }
                            }
                        }
                        
                        speechError?.let { error ->
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
                            Text(
                                text = error,
                                fontSize = DesignSystem.Typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            // Input area
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
                            enabled = !isRecording
                        )
                        
                        Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
                        
                        // Voice recording button
                        VoiceRecordingButton(
                            isRecording = isRecording,
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