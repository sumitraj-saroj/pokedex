package com.dexter.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.RectangleShape
import com.dexter.app.ui.theme.LocalDarkTheme

/**
 * Ultra-compact glassmorphic Top Bar component.
 * Seamless edge-to-edge top bar with no box frame or floating border outline.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassmorphicTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    shape: Shape = RectangleShape,
    elevation: Dp = 0.dp
) {
    val isDark = LocalDarkTheme.current

    val glassBg = if (isDark) {
        MaterialTheme.colorScheme.background.copy(alpha = 0.96f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    }

    val bottomDividerColor = if (isDark) {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = glassBg,
        shadowElevation = elevation
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(48.dp)
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Navigation Icon
                    if (navigationIcon != null) {
                        Box(
                            modifier = Modifier.padding(end = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            navigationIcon()
                        }
                    }

                    // Title
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        title()
                    }

                    // Actions
                    if (actions != null) {
                        Row(
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            actions()
                        }
                    }
                }
            }
            HorizontalDivider(
                color = bottomDividerColor,
                thickness = 0.5.dp
            )
        }
    }
}
