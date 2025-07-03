package com.digitalgreen.farmerchat.ui.components

import androidx.compose.animation.*
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.utils.AudioRecordingManager
import com.digitalgreen.farmerchat.ui.components.localizedString
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun WhatsAppVoiceRecorder(
    recordingState: AudioRecordingManager.RecordingState,
    recordingDuration: Int,
    audioLevel: Float,
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    onSendTranscription: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    when (recordingState) {
        AudioRecordingManager.RecordingState.IDLE -> {
            // Simple mic button like WhatsApp
            IconButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onStartRecording()
                },
                modifier = modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = localizedString(StringKey.SPEAK_MESSAGE),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        AudioRecordingManager.RecordingState.RECORDING -> {
            // WhatsApp-style recording UI - cleaner and more compact
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(28.dp)
                    )
                    .padding(horizontal = DesignSystem.Spacing.xs)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left side - Cancel with swipe hint
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = DesignSystem.Spacing.xs)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = localizedString(StringKey.SWIPE_TO_CANCEL),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        
                        IconButton(
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onCancelRecording()
                            },
                            modifier = Modifier
                            .testTag("hapticFeedbackButton").size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = localizedString(StringKey.CANCEL),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    // Center - Clean recording info
                    Row(
                        modifier = Modifier
                            .testTag("whatsappvoicerecorderRow3")
                            .weight(1f)
                            .padding(horizontal = DesignSystem.Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Recording indicator
                        RecordingIndicator()
                        
                        Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
                        
                        // Duration
                        Text(
                        modifier = Modifier.testTag("whatsappvoicerecorderText1Text"),
                            text = formatDuration(recordingDuration),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.width(DesignSystem.Spacing.md))
                        
                        // Compact waveform
                        WhatsAppWaveform(
                            audioLevel = audioLevel,
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                        )
                    }
                    
                    // Right side - Check/Confirm button
                    FilledIconButton(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onStopRecording()
                            onSendTranscription()
                        },
                        modifier = Modifier
                            .testTag("sendButton")
                            .padding(end = DesignSystem.Spacing.xs)
                            .size(40.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = localizedString(StringKey.SEND),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        
        // These states shouldn't show in WhatsApp-style - it auto-sends
        AudioRecordingManager.RecordingState.RECORDED,
        AudioRecordingManager.RecordingState.PLAYING,
        AudioRecordingManager.RecordingState.PAUSED -> {
            // Should not reach here in WhatsApp flow
            // But if it does, just show mic button again
            IconButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onStartRecording()
                },
                modifier = modifier
                    .testTag("startRecordingButton")
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = localizedString(StringKey.SPEAK_MESSAGE),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun RecordingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "recording")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )
    
    Box(
        modifier = Modifier
            .testTag("recordingIndicator")
            .size(12.dp)
            .clip(CircleShape)
            .background(Color.Red.copy(alpha = alpha))
    )
}

@Composable
private fun WhatsAppWaveform(
    audioLevel: Float,
    modifier: Modifier = Modifier
) {
    val barCount = 25 // Reduced for cleaner look
    val waveformBars = remember { mutableStateListOf<Float>().apply {
        repeat(barCount) { add(0.15f) }
    }}
    
    // Update waveform based on audio level with more dynamic movement
    LaunchedEffect(audioLevel) {
        // Shift existing values to the left (scrolling effect)
        for (i in 0 until barCount - 1) {
            waveformBars[i] = waveformBars[i + 1]
        }
        
        // Generate new bar height based on audio level
        val baseHeight = if (audioLevel > 0.1f) {
            // Active speaking - create wave pattern
            val wavePosition = System.currentTimeMillis() / 50.0
            val sineWave = (sin(wavePosition) * 0.3 + 0.5).toFloat()
            (audioLevel * sineWave).coerceIn(0.2f, 1f)
        } else {
            // Silence - minimal height with test animation
            // Add a subtle test wave to verify waveform is working
            val testWave = System.currentTimeMillis() / 200.0
            (0.1f + sin(testWave) * 0.05f).toFloat().coerceIn(0.05f, 0.15f)
        }
        
        // Add some natural variation
        val variation = (Math.random() * 0.2 - 0.1).toFloat()
        waveformBars[barCount - 1] = (baseHeight + variation).coerceIn(0.1f, 1f)
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            waveformBars.forEachIndexed { index, height ->
                val animatedHeight by animateFloatAsState(
                    targetValue = height,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "wave_$index"
                )
                
                // Create center-aligned bars like WhatsApp
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(animatedHeight)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(
                                MaterialTheme.colorScheme.primary.copy(
                                    alpha = if (index > barCount - 10) {
                                        // Make recent bars more opaque
                                        0.8f + animatedHeight * 0.2f
                                    } else {
                                        0.5f + animatedHeight * 0.3f
                                    }
                                )
                            )
                    )
                }
            }
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}