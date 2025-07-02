package com.digitalgreen.farmerchat.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import com.digitalgreen.farmerchat.ui.theme.DesignSystem

/**
 * Skeleton loader for follow-up questions
 */
@Composable
fun FollowUpQuestionsSkeleton() {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )
    
    Row(
        modifier = Modifier
                            .testTag("chatskeletonsRow1")
            .fillMaxWidth()
            .padding(DesignSystem.Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                            .testTag("chatskeletonsBox1")
                    .width(if (index == 0) 120.dp else if (index == 1) 140.dp else 100.dp)
                    .height(36.dp) // Slightly reduced to match chips better
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = shimmerAlpha)
                    )
            )
        }
    }
}


