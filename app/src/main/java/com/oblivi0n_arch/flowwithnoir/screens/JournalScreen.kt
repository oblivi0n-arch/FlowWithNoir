package com.oblivi0n_arch.flowwithnoir.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oblivi0n_arch.flowwithnoir.data.Feeling
import com.oblivi0n_arch.flowwithnoir.data.JournalTag
import com.oblivi0n_arch.flowwithnoir.ui.theme.FlowWithNoirTheme

enum class JournalMode {
    AFTER_SESSION,
    NEW,
    EDIT
}

@Composable
fun JournalScreen(
    mode: JournalMode = JournalMode.AFTER_SESSION,
    initialFeeling: Feeling? = null,
    initialNote: String = "",
    initialTags: List<JournalTag> = emptyList(),
    onSkip: () -> Unit,
    onSave: (feeling: Feeling?, note: String, tags: List<JournalTag>) -> Unit
) {
    var selectedFeeling by remember { mutableStateOf(initialFeeling) }
    var note by remember { mutableStateOf(initialNote) }
    var selectedTags by remember { mutableStateOf(initialTags.toSet()) }

    val eyebrow = when (mode) {
        JournalMode.AFTER_SESSION -> "Sesja ukończona"
        JournalMode.NEW -> "Nowy wpis"
        JournalMode.EDIT -> "Edycja wpisu"
    }
    val saveLabel = if (mode == JournalMode.EDIT) "Zapisz zmiany" else "Zapisz w dzienniku"
    val skipLabel = if (mode == JournalMode.AFTER_SESSION) "Pomiń" else "Anuluj"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 30.dp)
            .padding(top = 60.dp, bottom = 30.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = eyebrow,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Jak się czujesz?",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Feeling.entries.forEach { feeling ->
                    FeelingOption(
                        feeling = feeling,
                        isSelected = feeling == selectedFeeling,
                        onClick = {
                            selectedFeeling = if (selectedFeeling == feeling) null else feeling
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = selectedFeeling?.label ?: " ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "CZEGO DOTYCZY",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(12.dp))

            TagPicker(
                selectedTags = selectedTags,
                onToggle = { tag ->
                    selectedTags = if (selectedTags.contains(tag)) {
                        selectedTags - tag
                    } else {
                        selectedTags + tag
                    }
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "NOTATKA",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                placeholder = {
                    Text(
                        text = "Co zauważyłeś/aś podczas sesji…",
                        color = MaterialTheme.colorScheme.secondary
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    cursorColor = MaterialTheme.colorScheme.onBackground
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        Button(
            onClick = { onSave(selectedFeeling, note.trim(), selectedTags.toList()) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background
            )
        ) {
            Text(saveLabel)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = skipLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable(onClick = onSkip)
                .padding(8.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagPicker(
    selectedTags: Set<JournalTag>,
    onToggle: (JournalTag) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        JournalTag.entries.forEach { tag ->
            TagChip(
                tag = tag,
                isSelected = selectedTags.contains(tag),
                onClick = { onToggle(tag) }
            )
        }
    }
}

@Composable
fun TagChip(
    tag: JournalTag,
    isSelected: Boolean,
    onClick: (() -> Unit)? = null,
    isCompact: Boolean = false
) {
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 0.dp else 1.dp,
        animationSpec = tween(180),
        label = "tagBorder"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
        animationSpec = tween(180),
        label = "tagContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.background
        else MaterialTheme.colorScheme.secondary,
        animationSpec = tween(180),
        label = "tagContent"
    )

    Row(
        modifier = Modifier
            .background(containerColor, RoundedCornerShape(999.dp))
            .border(borderWidth, MaterialTheme.colorScheme.outline, RoundedCornerShape(999.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(
                horizontal = if (isCompact) 10.dp else 14.dp,
                vertical = if (isCompact) 5.dp else 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = tagIcon(tag),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(if (isCompact) 11.dp else 14.dp)
        )
        Text(
            text = tag.label,
            style = if (isCompact) MaterialTheme.typography.labelSmall
            else MaterialTheme.typography.bodyMedium,
            color = contentColor
        )
    }
}

@Composable
private fun FeelingOption(
    feeling: Feeling,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = tween(200),
        label = "feelingScale"
    )

    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .wrapContentWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.onBackground
                    else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = feelingIcon(feeling),
                contentDescription = feeling.label,
                tint = if (isSelected) MaterialTheme.colorScheme.onBackground
                else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JournalScreenPreview() {
    FlowWithNoirTheme {
        JournalScreen(onSkip = {}, onSave = { _, _, _ -> })
    }
}
