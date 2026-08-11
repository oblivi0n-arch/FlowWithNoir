package com.oblivi0n_arch.flowwithnoir.screens

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class NavTab {
    SESSIONS,
    JOURNAL,
    PROFILE
}

@Composable
fun BottomNavBar(
    selected: NavTab,
    onSessionsClick: () -> Unit = {},
    onJournalClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        NavIcon(
            icon = Icons.Filled.SelfImprovement,
            isActive = selected == NavTab.SESSIONS,
            onClick = { if (selected != NavTab.SESSIONS) onSessionsClick() }
        )
        NavIcon(
            icon = Icons.Filled.MenuBook,
            isActive = selected == NavTab.JOURNAL,
            onClick = { if (selected != NavTab.JOURNAL) onJournalClick() }
        )
        NavIcon(
            icon = Icons.Filled.AccountCircle,
            isActive = selected == NavTab.PROFILE,
            onClick = { if (selected != NavTab.PROFILE) onProfileClick() }
        )
    }
}

val screenPaddingTop = 24.dp

val screenPaddingBottom = 12.dp

val screenPaddingHorizontal = 26.dp

@Composable
fun Modifier.screenInsets(): Modifier = this.windowInsetsPadding(
    WindowInsets.systemBars.union(WindowInsets.displayCutout)
)

internal fun Context.findActivity(): ComponentActivity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is ComponentActivity) return current
        current = current.baseContext
    }
    return null
}

@Composable
fun NavIcon(icon: ImageVector, isActive: Boolean, onClick: () -> Unit = {}) {
    val tint by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.onBackground
        else MaterialTheme.colorScheme.secondary,
        animationSpec = tween(220),
        label = "navTint"
    )
    val dotScale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(220),
        label = "navDot"
    )
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Box(
            modifier = Modifier
                .size(4.dp)
                .graphicsLayer {
                    scaleX = dotScale
                    scaleY = dotScale
                }
                .background(
                    color = if (dotScale > 0f) MaterialTheme.colorScheme.onBackground
                    else Color.Transparent,
                    shape = CircleShape
                )
        )
    }
}
