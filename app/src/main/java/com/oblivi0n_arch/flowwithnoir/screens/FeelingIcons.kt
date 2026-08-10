package com.oblivi0n_arch.flowwithnoir.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.ui.graphics.vector.ImageVector
import com.oblivi0n_arch.flowwithnoir.data.Feeling

fun feelingIcon(feeling: Feeling): ImageVector = when (feeling) {
    Feeling.CALM -> Icons.Filled.Cloud
    Feeling.NEUTRAL -> Icons.Filled.CloudQueue
    Feeling.STRESSED -> Icons.Filled.Thunderstorm
    Feeling.TIRED -> Icons.Filled.FilterDrama
}
