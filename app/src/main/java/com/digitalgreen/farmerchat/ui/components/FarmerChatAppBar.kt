package com.digitalgreen.farmerchat.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.digitalgreen.farmerchat.ui.theme.DesignSystem
import com.digitalgreen.farmerchat.ui.theme.appBarColor

/**
 * Consistent AppBar component for all screens in FarmerChat
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerChatAppBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
    colors: TopAppBarColors? = null,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = DesignSystem.Typography.titleMedium,
                fontWeight = DesignSystem.Typography.Weight.SemiBold,
                color = Color.White
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
        },
        actions = { actions() },
        colors = colors ?: TopAppBarDefaults.topAppBarColors(
            containerColor = appBarColor(),
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        modifier = modifier
    )
}

/**
 * Large AppBar variant for main screens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerChatLargeAppBar(
    title: String,
    actions: @Composable () -> Unit = {},
    colors: TopAppBarColors? = null,
    modifier: Modifier = Modifier
) {
    LargeTopAppBar(
        title = {
            Text(
                text = title,
                fontSize = DesignSystem.Typography.headlineMedium,
                fontWeight = DesignSystem.Typography.Weight.Bold,
                color = Color.White
            )
        },
        actions = { actions() },
        colors = colors ?: TopAppBarDefaults.largeTopAppBarColors(
            containerColor = appBarColor(),
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        modifier = modifier
    )
}

/**
 * Specialized chat screen AppBar with user info
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenAppBar(
    title: String,
    subtitle: String? = null,
    onBackClick: () -> Unit,
    actions: @Composable () -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            androidx.compose.foundation.layout.Column {
                Text(
                    text = title,
                    fontSize = DesignSystem.Typography.titleMedium,
                    fontWeight = DesignSystem.Typography.Weight.Medium,
                    color = Color.White
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = DesignSystem.Typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = appBarColor(),
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        modifier = modifier
    )
}