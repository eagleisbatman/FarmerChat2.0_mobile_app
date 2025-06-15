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
import com.digitalgreen.farmerchat.data.ChatMessage
import com.digitalgreen.farmerchat.data.LanguageManager
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.utils.StringsManager.StringKey
import java.text.SimpleDateFormat
import java.util.*

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
    isStreaming: Boolean = false,
    currentLanguageCode: String = "en"
) {
    val isUser = message.isUser
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    // Check if current language is RTL
    val isRtlLanguage = LanguageManager.getLanguageByCode(currentLanguageCode)?.isRTL ?: false
    
    // Set layout direction
    CompositionLocalProvider(
        LocalLayoutDirection provides if (isRtlLanguage) LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignSystem.Spacing.md, vertical = DesignSystem.Spacing.xs),
            horizontalArrangement = if (isRtlLanguage) Arrangement.End else Arrangement.Start
        ) {
            // Avatar (AI for bot messages)
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Agriculture,
                        contentDescription = "AI Assistant",
                        modifier = Modifier.size(DesignSystem.IconSize.small),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 320.dp), // 80% of typical phone width
                horizontalAlignment = if (isRtlLanguage) Alignment.End else Alignment.Start
            ) {
                // Message bubble
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (!isUser && !isRtlLanguage || isUser && isRtlLanguage) 4.dp else 16.dp,
                        bottomEnd = if (isUser && !isRtlLanguage || !isUser && isRtlLanguage) 4.dp else 16.dp
                    ),
                    color = if (isUser) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    shadowElevation = DesignSystem.Elevation.small,
                    modifier = Modifier.animateContentSize()
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = DesignSystem.Spacing.md,
                            vertical = DesignSystem.Spacing.sm
                        )
                    ) {
                        // Message content
                        if (isUser) {
                            Text(
                                text = if (isStreaming && message.content.isEmpty()) "..." else message.content,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = DesignSystem.Typography.bodyLarge,
                                lineHeight = DesignSystem.Typography.titleMedium,
                                modifier = Modifier.animateContentSize()
                            )
                        } else {
                            MarkdownText(
                                text = if (isStreaming && message.content.isEmpty()) "..." else message.content,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 15,
                                lineHeight = 22,
                                modifier = Modifier.animateContentSize()
                            )
                        }
                        
                        // Timestamp and actions
                        if (!isStreaming || message.content.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Timestamp
                                Text(
                                    text = dateFormat.format(message.timestamp),
                                    fontSize = DesignSystem.Typography.labelSmall,
                                    color = if (isUser) {
                                        MaterialTheme.colorScheme.onPrimary.copy(alpha = DesignSystem.Opacity.high)
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DesignSystem.Opacity.medium)
                                    }
                                )
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                // Action buttons (only for AI messages)
                                if (!isUser) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.xs)
                                    ) {
                                        // Voice button
                                        IconButton(
                                            onClick = onPlayAudio,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                                contentDescription = if (isSpeaking) localizedString(StringKey.STOP) else localizedString(StringKey.PLAY),
                                                modifier = Modifier.size(DesignSystem.IconSize.small),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DesignSystem.Opacity.medium)
                                            )
                                        }
                                        
                                        // Feedback button
                                        IconButton(
                                            onClick = onFeedback,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ThumbsUpDown,
                                                contentDescription = localizedString(StringKey.RATE),
                                                modifier = Modifier.size(DesignSystem.IconSize.small),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DesignSystem.Opacity.medium)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // User name label (optional, for user messages)
                if (isUser) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = localizedString(StringKey.YOU),
                        fontSize = DesignSystem.Typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DesignSystem.Opacity.medium)
                    )
                }
            }
            
            // Avatar (User for user messages) - only in LTR mode
            if (isUser && !isRtlLanguage) {
                Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        modifier = Modifier.size(DesignSystem.IconSize.small),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            // Add spacer to limit bubble width
            if (!isUser) {
                Spacer(modifier = Modifier.weight(0.2f))
            }
        }
    }
}