package com.digitalgreen.farmerchat.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.utils.AudioRecordingManager
import com.digitalgreen.farmerchat.utils.StringsManager
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun AudioRecordingView(
    recordingState: AudioRecordingManager.RecordingState,
    recordingDuration: Int,
    playbackProgress: Float,
    audioLevel: Float,
    isRecording: Boolean,
    isPlaying: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPlayPause: () -> Unit,
    onDiscard: () -> Unit,
    onSendForTranscription: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(DesignSystem.Spacing.md),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(DesignSystem.Spacing.lg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignSystem.Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (recordingState) {
                AudioRecordingManager.RecordingState.IDLE -> {
                    IdleView(onStartRecording = onStartRecording)
                }
                AudioRecordingManager.RecordingState.RECORDING -> {
                    RecordingView(
                        duration = recordingDuration,
                        audioLevel = audioLevel,
                        onStopRecording = onStopRecording
                    )
                }
                AudioRecordingManager.RecordingState.RECORDED,
                AudioRecordingManager.RecordingState.PLAYING,
                AudioRecordingManager.RecordingState.PAUSED -> {
                    PlaybackView(
                        isPlaying = isPlaying,
                        playbackProgress = playbackProgress,
                        recordingDuration = recordingDuration,
                        onPlayPause = onPlayPause,
                        onDiscard = onDiscard,
                        onSend = onSendForTranscription
                    )
                }
            }
        }
    }
}

@Composable
private fun IdleView(
    onStartRecording: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.md)
    ) {
        Text(
            text = StringsManager.getString(StringsManager.StringKey.TAP_TO_RECORD),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Record button
        FilledIconButton(
            onClick = onStartRecording,
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Start recording",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun RecordingView(
    duration: Int,
    audioLevel: Float,
    onStopRecording: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "recording")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.md)
    ) {
        // Recording indicator with pulse effect
        Box(
            modifier = Modifier.size(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = pulseAlpha))
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
            )
        }
        
        // Duration display
        Text(
            text = formatDuration(duration),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Audio level visualization
        AudioLevelVisualizer(
            audioLevel = audioLevel,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = DesignSystem.Spacing.md)
        )
        
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
        
        // Stop button
        FilledIconButton(
            onClick = onStopRecording,
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.Red
            )
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Stop recording",
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun PlaybackView(
    isPlaying: Boolean,
    playbackProgress: Float,
    recordingDuration: Int,
    onPlayPause: () -> Unit,
    onDiscard: () -> Unit,
    onSend: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.md)
    ) {
        // Duration and progress
        Text(
            text = formatDuration((recordingDuration * playbackProgress).toInt()) + " / " + formatDuration(recordingDuration),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Progress bar
        LinearProgressIndicator(
            progress = { playbackProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer
        )
        
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
        
        // Control buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Discard button
            OutlinedIconButton(
                onClick = onDiscard,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.outlinedIconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Discard recording",
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Play/Pause button
            FilledIconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(64.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            // Send button
            FilledIconButton(
                onClick = onSend,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send for transcription",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}

@Composable
private fun AudioLevelVisualizer(
    audioLevel: Float,
    modifier: Modifier = Modifier
) {
    val barCount = 20
    val animatedLevels = remember { mutableStateListOf<Float>().apply {
        repeat(barCount) { add(0f) }
    }}
    
    LaunchedEffect(audioLevel) {
        // Update bars with a wave effect
        for (i in 0 until barCount) {
            val phase = (i.toFloat() / barCount) * 2 * Math.PI
            val waveHeight = audioLevel * sin(System.currentTimeMillis() / 100.0 + phase).toFloat()
            animatedLevels[i] = (0.2f + waveHeight * 0.8f).coerceIn(0.1f, 1f)
        }
    }
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        animatedLevels.forEachIndexed { index, level ->
            val animatedHeight by animateFloatAsState(
                targetValue = level,
                animationSpec = tween(durationMillis = 100),
                label = "bar_$index"
            )
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.3f + animatedHeight * 0.7f
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(animatedHeight)
                        .align(Alignment.BottomCenter)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}