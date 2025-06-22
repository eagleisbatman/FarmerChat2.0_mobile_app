package com.digitalgreen.farmerchat.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.digitalgreen.farmerchat.ui.theme.DesignSystem

@Composable
fun shimmerBrush(showShimmer: Boolean = true): Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.LightGray.copy(alpha = 0.2f),
            Color.LightGray.copy(alpha = 0.6f),
        )

        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnim = transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer_animation"
        )

        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnim.value, y = translateAnim.value)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent),
            start = Offset.Zero,
            end = Offset.Zero
        )
    }
}

@Composable
fun ConversationItemSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignSystem.Spacing.md, vertical = DesignSystem.Spacing.sm + DesignSystem.Spacing.xs)
    ) {
        // First row: Title + Timestamp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Title skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush())
            )
            
            // Time skeleton
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush())
            )
        }
        
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
        
        // Tags section skeleton (randomly show for some items to match real variety)
        if ((0..2).random() == 0) { // Show tags for about 1/3 of skeleton items
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                repeat((1..3).random()) { index ->
                    Box(
                        modifier = Modifier
                            .width((60..80).random().dp)
                            .height(DesignSystem.Spacing.lg)
                            .clip(RoundedCornerShape(DesignSystem.Spacing.sm))
                            .background(shimmerBrush())
                    )
                    if (index < 2) {
                        Spacer(modifier = Modifier.width(DesignSystem.Spacing.xs))
                    }
                }
            }
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
        }
        
        // Last row: Last message + Unread count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Last message with optional AI icon
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // AI icon skeleton (randomly show for assistant messages)
                if ((0..1).random() == 0) { // Show AI icon for about 50% of skeleton items
                    Box(
                        modifier = Modifier
                            .size(DesignSystem.IconSize.small)
                            .clip(CircleShape)
                            .background(shimmerBrush())
                    )
                    Spacer(modifier = Modifier.width(DesignSystem.Spacing.xs))
                }
                
                // Last message skeleton
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush())
                )
            }
            
            // Unread count skeleton (randomly show to match real variety)
            if ((0..3).random() == 0) { // Show unread badge for about 25% of skeleton items
                Box(
                    modifier = Modifier
                        .size(DesignSystem.Spacing.lg)
                        .clip(CircleShape)
                        .background(shimmerBrush())
                )
            }
        }
    }
}

@Composable
fun MessageSkeleton(isUser: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignSystem.Spacing.md, vertical = DesignSystem.Spacing.xs),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 100.dp, max = 280.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(shimmerBrush())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .background(Color.Transparent)
                    )
                    if (it == 0) Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun StarterQuestionsSkeleton() {
    Column(
        modifier = Modifier.padding(DesignSystem.Spacing.md)
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(shimmerBrush())
            )
            if (index < 2) {
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
            }
        }
    }
}

@Composable
fun SettingsItemSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DesignSystem.Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon skeleton
        Box(
            modifier = Modifier
                .size(DesignSystem.IconSize.medium)
                .clip(CircleShape)
                .background(shimmerBrush())
        )
        
        Spacer(modifier = Modifier.width(DesignSystem.Spacing.md))
        
        Column(modifier = Modifier.weight(1f)) {
            // Title skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush())
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Subtitle skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush())
            )
        }
        
        // Chevron/action skeleton
        Box(
            modifier = Modifier
                .size(DesignSystem.IconSize.small)
                .clip(CircleShape)
                .background(shimmerBrush())
        )
    }
}

@Composable
fun SettingsSectionSkeleton(itemCount: Int = 4) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Section title skeleton
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(18.dp)
                .padding(
                    horizontal = DesignSystem.Spacing.md, 
                    vertical = DesignSystem.Spacing.sm
                )
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerBrush())
        )
        
        // Section card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = DesignSystem.Spacing.md, 
                    vertical = DesignSystem.Spacing.xs
                ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column {
                repeat(itemCount) { index ->
                    SettingsItemSkeleton()
                    if (index < itemCount - 1) {
                        androidx.compose.material3.HorizontalDivider(
                            modifier = Modifier.padding(start = 56.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier
            .padding(horizontal = DesignSystem.Spacing.md, vertical = DesignSystem.Spacing.xs),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val infiniteTransition = rememberInfiniteTransition(label = "typing")
                    val alpha = infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = index * 100),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_alpha"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha.value)
                            )
                    )
                }
            }
        }
    }
}