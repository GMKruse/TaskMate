package com.example.taskmate.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Configuration for the top app bar
 */
data class TopBarConfig(
    val title: String,
    val showNavigationIcon: Boolean = false,
    val navigationIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    val onNavigationClick: (() -> Unit)? = null,
    val actions: @Composable () -> Unit = {}
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurableTopAppBar(config: TopBarConfig?) {
    if (config != null) {
        TopAppBar(
            title = { Text(config.title) },
            navigationIcon = {
                if (config.showNavigationIcon && config.onNavigationClick != null) {
                    IconButton(onClick = config.onNavigationClick) {
                        Icon(config.navigationIcon, contentDescription = "Navigate")
                    }
                }
            },
            actions = { config.actions() }
        )
    }
}

