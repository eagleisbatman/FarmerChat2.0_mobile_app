package com.digitalgreen.farmerchat.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.digitalgreen.farmerchat.ui.theme.DesignSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    question: String,
    answer: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignSystem.Spacing.lg)
                .padding(bottom = DesignSystem.Spacing.lg)
        ) {
            // Title
            Text(
                text = "Share Answer",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.lg))
            
            // Preview of what will be shared
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier.padding(DesignSystem.Spacing.md)
                ) {
                    Text(
                        text = "Q: $question",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
                    
                    Text(
                        text = "A: ${answer.take(100)}${if (answer.length > 100) "..." else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
                    
                    Text(
                        text = "\nGet more farming advice with FarmerChat: https://play.google.com/store/apps/details?id=com.digitalgreen.farmerchat",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.lg))
            
            // Share options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ShareOption(
                    icon = Icons.Default.Share,
                    label = "WhatsApp",
                    color = MaterialTheme.colorScheme.primary
                ) {
                    shareToWhatsApp(context, question, answer)
                    onDismiss()
                }
                
                ShareOption(
                    icon = Icons.Default.ContentCopy,
                    label = "Copy Text",
                    color = MaterialTheme.colorScheme.secondary
                ) {
                    copyToClipboard(context, question, answer)
                    onDismiss()
                }
                
                ShareOption(
                    icon = Icons.Default.MoreHoriz,
                    label = "More Apps",
                    color = MaterialTheme.colorScheme.tertiary
                ) {
                    shareToOtherApps(context, question, answer)
                    onDismiss()
                }
            }
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.lg))
            
            // Cancel button
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun ShareOption(
    icon: ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.1f)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(24.dp),
                    tint = color
                )
            }
        }
        
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun shareToWhatsApp(context: Context, question: String, answer: String) {
    val shareText = buildShareText(question, answer)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        setPackage("com.whatsapp")
    }
    
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // WhatsApp not installed, fallback to general share
        shareToOtherApps(context, question, answer)
    }
}

private fun copyToClipboard(context: Context, question: String, answer: String) {
    val shareText = buildShareText(question, answer)
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("FarmerChat Answer", shareText)
    clipboard.setPrimaryClip(clip)
    
    // Show a toast
    android.widget.Toast.makeText(context, localizedString(StringKey.COPIED_TO_CLIPBOARD), android.widget.Toast.LENGTH_SHORT).show()
}

private fun shareToOtherApps(context: Context, question: String, answer: String) {
    val shareText = buildShareText(question, answer)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "FarmerChat Answer")
    }
    
    context.startActivity(Intent.createChooser(intent, "Share via"))
}

private fun buildShareText(question: String, answer: String): String {
    return """
Q: $question

A: $answer

Get more farming advice with FarmerChat: https://play.google.com/store/apps/details?id=com.digitalgreen.farmerchat
    """.trimIndent()
}