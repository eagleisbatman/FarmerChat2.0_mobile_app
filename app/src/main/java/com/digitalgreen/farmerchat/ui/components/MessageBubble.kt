package com.digitalgreen.farmerchat.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digitalgreen.farmerchat.data.ChatMessage
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageBubble(
    message: ChatMessage,
    onPlayAudio: () -> Unit,
    isSpeaking: Boolean,
    onFeedback: () -> Unit,
    isStreaming: Boolean = false
) {
    val isUser = message.isUser
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        // AI Avatar
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Agriculture,
                    contentDescription = "AI Assistant",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = if (isUser) 20.dp else 4.dp,
                    topEnd = if (isUser) 4.dp else 20.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                ),
                color = if (isUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                shadowElevation = 1.dp,
                modifier = Modifier.animateContentSize()
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 10.dp
                    )
                ) {
                    if (isUser) {
                        Text(
                            text = if (isStreaming && message.content.isEmpty()) "..." else message.content,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
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
                    
                    if (!isStreaming || message.content.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = dateFormat.format(message.timestamp),
                                fontSize = 12.sp,
                                color = if (isUser) {
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                }
                            )
                            
                            if (!isUser) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = onPlayAudio,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = if (isSpeaking) "Stop" else "Play",
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                    
                                    IconButton(
                                        onClick = onFeedback,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.StarOutline,
                                            contentDescription = "Rate",
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // User Avatar
        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}