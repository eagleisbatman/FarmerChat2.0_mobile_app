package com.digitalgreen.farmerchat.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digitalgreen.farmerchat.BuildConfig
import com.digitalgreen.farmerchat.data.ChatMessage
import com.digitalgreen.farmerchat.data.LanguageManager
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import java.text.SimpleDateFormat
import java.util.*

/**
 * Detects if a message is a simple greeting that doesn't need feedback
 */
private fun isSimpleGreeting(message: String): Boolean {
    val lowerMessage = message.lowercase().trim()
    val greetings = listOf(
        "hi", "hello", "hey", "good morning", "good afternoon", "good evening",
        "thanks", "thank you", "bye", "goodbye", "ok", "okay", "yes", "no",
        "sure", "alright", "got it", "understood", "nice", "great", "awesome",
        "cool", "perfect", "excellent", "wonderful", "amazing", "fantastic"
    )
    
    // Check if the message is exactly one of these greetings or very short
    return greetings.any { greeting ->
        lowerMessage == greeting || 
        lowerMessage == "$greeting!" ||
        lowerMessage == "$greeting." ||
        lowerMessage == "$greeting?"
    } || lowerMessage.length <= 3 // Very short messages like "ok", "yes", "no"
}

/**
 * Improved message bubble with:
 * - Consistent left/right alignment based on language direction
 * - Reduced width (80% max)
 * - Better spacing
 * - RTL support
 */
@Composable
fun MessageBubbleV2(
    message: ChatMessage,
    onPlayAudio: () -> Unit,
    isSpeaking: Boolean,
    onFeedback: () -> Unit,
    onShare: () -> Unit = {},
    isStreaming: Boolean = false,
    currentLanguageCode: String = "en"
) {
    val isUser = message.isUser
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    // Debug logging
    android.util.Log.d("MessageBubbleV2", "Message: ${message.content.take(50)}, isUser: $isUser, message.user: ${message.user}")
    
    // Check if current language is RTL
    val isRtlLanguage = LanguageManager.getLanguageByCode(currentLanguageCode)?.isRTL ?: false
    
    // Set layout direction
    CompositionLocalProvider(
        LocalLayoutDirection provides if (isRtlLanguage) LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignSystem.Spacing.md, vertical = DesignSystem.Spacing.xs),
            horizontalAlignment = Alignment.Start // Always align to start (left)
        ) {
            // Sender name and timestamp in same row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = DesignSystem.Spacing.xxs)
            ) {
                Text(
                    text = if (isUser) "You" else "FarmerChat",
                    fontSize = DesignSystem.Typography.labelSmall,
                    fontWeight = DesignSystem.Typography.Weight.Medium,
                    color = if (isUser) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
                Text(
                    text = "• ${dateFormat.format(message.timestamp)}",
                    fontSize = DesignSystem.Typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DesignSystem.Opacity.medium)
                )
            }
            
            // Message bubble with better visual distinction
            Surface(
                shape = RoundedCornerShape(
                    topStart = if (isUser) 16.dp else 4.dp,
                    topEnd = if (isUser) 4.dp else 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                color = if (isUser) {
                    // Use the green primary color for user messages
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                },
                shadowElevation = if (isUser) DesignSystem.Elevation.small else DesignSystem.Elevation.none,
                modifier = Modifier
                    .fillMaxWidth() // Use full width as requested
                    .animateContentSize()
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = DesignSystem.Spacing.md,
                        vertical = DesignSystem.Spacing.sm
                    )
                ) {
                    // Message content with improved typography
                    if (isUser) {
                        Text(
                            text = if (isStreaming && message.content.isEmpty()) "..." else message.content,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 16.sp, // Bigger, more readable font
                            lineHeight = 24.sp, // Better line spacing
                            fontWeight = DesignSystem.Typography.Weight.Normal,
                            modifier = Modifier.animateContentSize()
                        )
                    } else {
                        MarkdownText(
                            text = if (isStreaming && message.content.isEmpty()) "..." else message.content,
                            modifier = Modifier.animateContentSize(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16, // Consistent with user messages (Int parameter)
                            lineHeight = 24, // Better line spacing
                            isStreaming = isStreaming // Pass streaming flag
                        )
                    }
                    
                    // Debug for troubleshooting
                    if (BuildConfig.DEBUG) {
                        android.util.Log.d("MessageBubbleV2", "Rendering message - isUser: $isUser, background: ${if (isUser) "green/primary" else "surface"}")
                    }
                    
                    // Action buttons (only for AI messages, no timestamp here)
                    if (!isUser && !isStreaming && message.content.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.xs)
                        ) {
                            // Voice button with larger touch target
                            IconButton(
                                onClick = onPlayAudio,
                                modifier = Modifier.size(36.dp) // Increased from 28dp for better touch target
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = if (isSpeaking) localizedString(StringKey.STOP) else localizedString(StringKey.PLAY),
                                    modifier = Modifier.size(DesignSystem.IconSize.medium), // Increased from small to medium (24dp)
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DesignSystem.Opacity.high) // More prominent
                                )
                            }
                            
                            // Share button - always show for AI responses
                            IconButton(
                                onClick = onShare,
                                modifier = Modifier.size(36.dp) // Same size as other buttons
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = localizedString(StringKey.SHARE_ANSWER),
                                    modifier = Modifier.size(DesignSystem.IconSize.medium),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DesignSystem.Opacity.high)
                                )
                            }
                            
                            // Feedback button with larger touch target - only show for substantial AI responses, not greetings
                            if (!isSimpleGreeting(message.content)) {
                                IconButton(
                                    onClick = onFeedback,
                                    modifier = Modifier.size(36.dp) // Increased from 28dp for better touch target
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ThumbsUpDown,
                                        contentDescription = localizedString(StringKey.RATE),
                                        modifier = Modifier.size(DesignSystem.IconSize.medium), // Increased from small to medium (24dp)
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DesignSystem.Opacity.high) // More prominent
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