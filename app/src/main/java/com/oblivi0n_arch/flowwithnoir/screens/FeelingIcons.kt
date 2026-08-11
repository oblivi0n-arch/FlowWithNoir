package com.oblivi0n_arch.flowwithnoir.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.ui.graphics.vector.ImageVector
import com.oblivi0n_arch.flowwithnoir.data.Feeling
import com.oblivi0n_arch.flowwithnoir.data.JournalTag

fun feelingIcon(feeling: Feeling): ImageVector = when (feeling) {
    Feeling.CALM -> Icons.Filled.Cloud
    Feeling.NEUTRAL -> Icons.Filled.CloudQueue
    Feeling.STRESSED -> Icons.Filled.Thunderstorm
    Feeling.TIRED -> Icons.Filled.FilterDrama
}

fun tagIcon(tag: JournalTag): ImageVector = when (tag) {
    JournalTag.WORK -> Icons.Filled.WorkOutline
    JournalTag.SLEEP -> Icons.Filled.Bedtime
    JournalTag.RELATIONS -> Icons.Filled.People
    JournalTag.HEALTH -> Icons.Filled.Favorite
    JournalTag.BODY -> Icons.Filled.Accessibility
    JournalTag.ANXIETY -> Icons.Filled.Bolt
    JournalTag.GRATITUDE -> Icons.Filled.Spa
    JournalTag.REST -> Icons.Filled.Weekend
}
