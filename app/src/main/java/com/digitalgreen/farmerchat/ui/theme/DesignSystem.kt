package com.digitalgreen.farmerchat.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object DesignSystem {
    
    // Custom Colors
    object Colors {
        val Primary = Color(0xFF4CAF50) // Material Green
        val PrimaryDark = Color(0xFF388E3C)
        val Secondary = Color(0xFF66BB6A)
        val AppBarGreen = Color(0xFF2E7D32) // Darker green for AppBar
        val FloatingActionButton = Color(0xFF4CAF50)
        val Error = Color(0xFFD32F2F)
        val Success = Color(0xFF388E3C)
        val Warning = Color(0xFFF57C00)
        val TextPrimary = Color(0xFF212121)
        val TextSecondary = Color(0xFF757575)
        val BackgroundLight = Color(0xFFF5F5F5)
        val SurfaceLight = Color(0xFFFFFFFF)
        val DividerLight = Color(0xFFE0E0E0)
        
        // Dark theme colors
        val PrimaryDark_Dark = Color(0xFF81C784)
        val AppBarGreen_Dark = Color(0xFF1B5E20)
        val TextPrimary_Dark = Color(0xFFE0E0E0)
        val TextSecondary_Dark = Color(0xFFBDBDBD)
        val BackgroundDark = Color(0xFF121212)
        val SurfaceDark = Color(0xFF1E1E1E)
        val DividerDark = Color(0xFF424242)
    }
    
    // Spacing Scale
    object Spacing {
        val xxs: Dp = 2.dp
        val xs: Dp = 4.dp
        val sm: Dp = 8.dp
        val md: Dp = 16.dp
        val lg: Dp = 24.dp
        val xl: Dp = 32.dp
        val xxl: Dp = 48.dp
        val xxxl: Dp = 64.dp
    }
    
    // Typography Scale
    object Typography {
        // Display
        val displayLarge: TextUnit = 57.sp
        val displayMedium: TextUnit = 45.sp
        val displaySmall: TextUnit = 36.sp
        
        // Headline
        val headlineLarge: TextUnit = 32.sp
        val headlineMedium: TextUnit = 28.sp
        val headlineSmall: TextUnit = 24.sp
        
        // Title
        val titleLarge: TextUnit = 22.sp
        val titleMedium: TextUnit = 20.sp
        val titleSmall: TextUnit = 18.sp
        
        // Body
        val bodyLarge: TextUnit = 16.sp
        val bodyMedium: TextUnit = 14.sp
        val bodySmall: TextUnit = 12.sp
        
        // Label
        val labelLarge: TextUnit = 14.sp
        val labelMedium: TextUnit = 12.sp
        val labelSmall: TextUnit = 11.sp
        
        // Font Weights
        object Weight {
            val Light = FontWeight.Light
            val Normal = FontWeight.Normal
            val Medium = FontWeight.Medium
            val SemiBold = FontWeight.SemiBold
            val Bold = FontWeight.Bold
        }
    }
    
    // Icon Sizes
    object IconSize {
        val small: Dp = 16.dp
        val medium: Dp = 24.dp
        val large: Dp = 32.dp
        val xlarge: Dp = 48.dp
        val xxlarge: Dp = 64.dp
        val xxxlarge: Dp = 80.dp
        val splash: Dp = 120.dp
    }
    
    // Corner Radius
    object CornerRadius {
        val small: Dp = 4.dp
        val medium: Dp = 8.dp
        val large: Dp = 12.dp
        val xlarge: Dp = 16.dp
        val circular: Dp = 50.dp
    }
    
    // Elevation
    object Elevation {
        val none: Dp = 0.dp
        val small: Dp = 2.dp
        val medium: Dp = 4.dp
        val large: Dp = 8.dp
        val xlarge: Dp = 16.dp
    }
    
    // Animation Durations
    object Animation {
        const val fastDuration = 150
        const val mediumDuration = 300
        const val slowDuration = 500
    }
    
    // Opacity Values
    object Opacity {
        const val disabled = 0.38f
        const val medium = 0.60f
        const val high = 0.87f
        const val full = 1.0f
    }
}

// Extension functions for easy access
@Composable
fun appBarColor(): Color {
    // Use Material theme's primaryContainer which is now set to our AppBar green
    return MaterialTheme.colorScheme.primaryContainer
}

@Composable
fun primaryTextColor(): Color {
    return if (MaterialTheme.colorScheme.isLight()) {
        DesignSystem.Colors.TextPrimary
    } else {
        DesignSystem.Colors.TextPrimary_Dark
    }
}

@Composable
fun secondaryTextColor(): Color {
    return if (MaterialTheme.colorScheme.isLight()) {
        DesignSystem.Colors.TextSecondary
    } else {
        DesignSystem.Colors.TextSecondary_Dark
    }
}

// Helper to check if current theme is light
@Composable
private fun androidx.compose.material3.ColorScheme.isLight(): Boolean {
    // Simple check based on the background color's brightness
    val background = this.background
    val red = background.red
    val green = background.green  
    val blue = background.blue
    val brightness = (red * 0.299f + green * 0.587f + blue * 0.114f)
    return brightness > 0.5f
}