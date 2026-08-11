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
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oblivi0n_arch.flowwithnoir.data.Feeling
import com.oblivi0n_arch.flowwithnoir.data.JournalTag
import com.oblivi0n_arch.flowwithnoir.ui.theme.FlowWithNoirTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class JournalMode {
    AFTER_SESSION,
    NEW,
    EDIT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    mode: JournalMode = JournalMode.AFTER_SESSION,
    sessionLabel: String = "",
    timestampMillis: Long = System.currentTimeMillis(),
    initialTitle: String = "",
    initialFeeling: Feeling? = null,
    initialNote: String = "",
    initialTags: List<JournalTag> = emptyList(),
    onSkip: () -> Unit,
    onSave: (title: String, feeling: Feeling?, note: String, tags: List<JournalTag>) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var note by remember { mutableStateOf(initialNote) }
    var selectedFeeling by remember { mutableStateOf(initialFeeling) }
    var selectedTags by remember { mutableStateOf(initialTags.toSet()) }

    var showFeelingSheet by remember { mutableStateOf(false) }
    var showTagSheet by remember { mutableStateOf(false) }
    val feelingSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val tagSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val leaveLabel = if (mode == JournalMode.AFTER_SESSION) "Pomiń" else "Anuluj"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .padding(horizontal = 30.dp)
            .padding(top = 52.dp, bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = leaveLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .clickable(onClick = onSkip)
                    .padding(vertical = 4.dp)
            )
            Text(
                text = "Zapisz",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .clickable {
                        onSave(title.trim(), selectedFeeling, note.trim(), selectedTags.toList())
                    }
                    .padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = headerLine(mode, sessionLabel, timestampMillis),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        PlainTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = "Tytuł wpisu",
            textStyle = MaterialTheme.typography.headlineLarge.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            placeholderColor = MaterialTheme.colorScheme.outline,
            singleLine = true,
            imeAction = ImeAction.Next
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, bottom = 16.dp)
        ) {
            PlainTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = "Co zauważyłeś/aś podczas sesji…",
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                placeholderColor = MaterialTheme.colorScheme.secondary,
                singleLine = false,
                imeAction = ImeAction.Default,
                modifier = Modifier.fillMaxWidth()
            )
        }

        MetadataBar(
            feeling = selectedFeeling,
            tagCount = selectedTags.size,
            onFeelingClick = { showFeelingSheet = true },
            onTagsClick = { showTagSheet = true }
        )
    }

    if (showFeelingSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFeelingSheet = false },
            sheetState = feelingSheetState,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            SheetContent(label = "JAK SIĘ CZUJESZ?") {
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
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = selectedFeeling?.label ?: "Nic nie wybrano",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedFeeling == null) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showTagSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTagSheet = false },
            sheetState = tagSheetState,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            SheetContent(label = "CZEGO DOTYCZY") {
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
            }
        }
    }
}

@Composable
private fun SheetContent(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp)
            .padding(bottom = 44.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(18.dp))
        content()
    }
}

@Composable
private fun MetadataBar(
    feeling: Feeling?,
    tagCount: Int,
    onFeelingClick: () -> Unit,
    onTagsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(999.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(999.dp))
                .clickable(onClick = onFeelingClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .border(
                        width = if (feeling != null) 1.5.dp else 1.dp,
                        color = if (feeling != null) MaterialTheme.colorScheme.onBackground
                        else MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                feeling?.let {
                    Icon(
                        imageVector = feelingIcon(it),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
            Text(
                text = feeling?.label ?: "Nastrój",
                style = MaterialTheme.typography.bodyMedium,
                color = if (feeling != null) MaterialTheme.colorScheme.onBackground
                else MaterialTheme.colorScheme.secondary
            )
        }

        Box(
            modifier = Modifier
                .width(1.dp)
                .height(20.dp)
                .background(MaterialTheme.colorScheme.outline)
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(999.dp))
                .clickable(onClick = onTagsClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = when (tagCount) {
                    0 -> "Tagi"
                    1 -> "1 tag"
                    in 2..4 -> "$tagCount tagi"
                    else -> "$tagCount tagów"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (tagCount > 0) MaterialTheme.colorScheme.onBackground
                else MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun PlainTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    textStyle: TextStyle,
    placeholderColor: Color,
    singleLine: Boolean,
    imeAction: ImeAction,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                style = textStyle,
                color = placeholderColor
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = textStyle,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            modifier = Modifier.fillMaxWidth()
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

private fun headerLine(mode: JournalMode, sessionLabel: String, timestampMillis: Long): String {
    val date = SimpleDateFormat("EEEE, d MMMM · HH:mm", Locale("pl"))
        .format(Date(timestampMillis))
        .uppercase()
    return if (mode == JournalMode.AFTER_SESSION && sessionLabel.isNotBlank()) {
        "$date · ${sessionLabel.uppercase()}"
    } else {
        date
    }
}

@Preview(showBackground = true)
@Composable
fun JournalScreenPreview() {
    FlowWithNoirTheme {
        JournalScreen(onSkip = {}, onSave = { _, _, _, _ -> })
    }
}
