package com.oblivi0n_arch.flowwithnoir.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oblivi0n_arch.flowwithnoir.data.SessionCatalog
import com.oblivi0n_arch.flowwithnoir.data.SessionCategory
import com.oblivi0n_arch.flowwithnoir.data.SessionTemplate
import com.oblivi0n_arch.flowwithnoir.ui.theme.FlowWithNoirTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionCatalogScreen(
    onBack: () -> Unit,
    onStart: (templateId: String, durationSeconds: Int) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<SessionCategory?>(null) }
    var sheetTemplate by remember { mutableStateOf<SessionTemplate?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val suggested = remember { SessionCatalog.suggestedFor() }
    val visible = remember(selectedCategory) {
        selectedCategory?.let { SessionCatalog.byCategory(it) } ?: SessionCatalog.templates
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .screenInsets()
            .padding(horizontal = screenPaddingHorizontal)
            .padding(top = screenPaddingTop, bottom = screenPaddingBottom)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Wstecz",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .size(20.dp)
                    .clickable(onClick = onBack)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Wszystkie sesje",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(18.dp))

        CategoryFilter(
            selected = selectedCategory,
            onSelect = { selectedCategory = if (selectedCategory == it) null else it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(visible, key = { it.id }) { template ->
                CatalogRow(
                    template = template,
                    isSuggested = template.id == suggested.id,
                    onClick = { sheetTemplate = template }
                )
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Wybierz sesję, żeby zobaczyć opis i ustawić długość.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }

    sheetTemplate?.let { template ->
        SessionStartSheet(
            template = template,
            sheetState = sheetState,
            onDismiss = { sheetTemplate = null },
            onStart = { id, duration ->
                sheetTemplate = null
                onStart(id, duration)
            }
        )
    }
}

@Composable
private fun CategoryFilter(
    selected: SessionCategory?,
    onSelect: (SessionCategory) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SessionCategory.entries.forEach { category ->
            val isSelected = selected == category
            val alpha by animateFloatAsState(
                targetValue = if (selected == null || isSelected) 1f else 0.45f,
                animationSpec = tween(200),
                label = "categoryAlpha"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .alpha(alpha)
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.onBackground
                        else androidx.compose.ui.graphics.Color.Transparent,
                        shape = RoundedCornerShape(999.dp)
                    )
                    .border(
                        width = if (isSelected) 0.dp else 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(999.dp)
                    )
                    .clickable { onSelect(category) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.background
                    else MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun CatalogRow(
    template: SessionTemplate,
    isSuggested: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = template.meta(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            StatusDot(isActive = isSuggested)
        }

        AnimatedVisibility(
            visible = isSuggested,
            enter = fadeIn(tween(220)) + expandVertically(tween(220)),
            exit = fadeOut(tween(160)) + shrinkVertically(tween(160))
        ) {
            Text(
                text = "Propozycja na teraz",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SessionCatalogScreenPreview() {
    FlowWithNoirTheme {
        SessionCatalogScreen(onBack = {}, onStart = { _, _ -> })
    }
}
