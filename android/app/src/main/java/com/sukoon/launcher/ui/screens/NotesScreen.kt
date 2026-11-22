package com.sukoon.launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sukoon.launcher.data.LauncherState
import com.sukoon.launcher.ui.theme.LocalAppFont
import com.sukoon.launcher.ui.theme.LocalAppStrings
import com.sukoon.launcher.ui.theme.LocalSukoonColors

@Composable
fun NotesScreen(
    state: LauncherState,
    onCreateNote: () -> Long,
    onUpdateNote: (Long, String, String) -> Unit,
    onTogglePin: (Long) -> Unit,
    onDeleteNote: (Long) -> Unit
) {
    val colors = LocalSukoonColors.current
    val appFont = LocalAppFont.current
    val strings = LocalAppStrings.current
    var activeNoteId by remember { mutableStateOf<Long?>(null) }
    val activeNote = state.notes.find { it.id == activeNoteId }

    if (activeNote != null) {
        var title by remember(activeNote.id) { mutableStateOf(activeNote.title) }
        var body by remember(activeNote.id) { mutableStateOf(activeNote.body) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallChip(strings.backArrow) { activeNoteId = null }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SmallChip(if (activeNote.pinned) strings.pinnedStar else strings.pin, active = activeNote.pinned) {
                        onTogglePin(activeNote.id)
                    }
                    SmallChip(strings.delete, danger = true) {
                        val id = activeNote.id
                        activeNoteId = null
                        onDeleteNote(id)
                    }
                }
            }

            BasicTextField(
                value = title,
                onValueChange = {
                    title = it
                    onUpdateNote(activeNote.id, it, body)
                },
                textStyle = TextStyle(
                    fontFamily = appFont,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                ),
                cursorBrush = SolidColor(colors.accentWarm),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                decorationBox = { inner ->
                    Box {
                        if (title.isEmpty()) {
                            Text(strings.noteTitleHint, fontFamily = appFont, fontSize = 20.sp, color = colors.textMuted)
                        }
                        inner()
                    }
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.borderSubtle)
            )

            BasicTextField(
                value = body,
                onValueChange = {
                    body = it
                    onUpdateNote(activeNote.id, title, it)
                },
                textStyle = TextStyle(
                    fontFamily = appFont,
                    fontSize = 15.sp,
                    lineHeight = 26.sp,
                    color = colors.textPrimary
                ),
                cursorBrush = SolidColor(colors.accentWarm),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                decorationBox = { inner ->
                    Box {
                        if (body.isEmpty()) {
                            Text(strings.noteBodyHint, fontFamily = appFont, fontSize = 15.sp, color = colors.textMuted)
                        }
                        inner()
                    }
                }
            )

            Text(
                text = strings.autoSaved,
                fontFamily = appFont,
                fontSize = 11.sp,
                color = colors.textMuted
            )
        }
    } else {
        val sorted = remember(state.notes) {
            state.notes.sortedWith(compareByDescending<com.sukoon.launcher.data.NoteItem> { it.pinned }.thenByDescending { it.id })
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(strings.quickThoughts, fontFamily = appFont, fontSize = 12.sp, color = colors.textMuted)
                    Text(strings.notesTitle, fontFamily = appFont, fontSize = 22.sp, color = colors.textPrimary)
                }
                SmallChip(strings.newNoteBtn, active = true) {
                    activeNoteId = onCreateNote()
                }
            }

            if (sorted.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(strings.noNotesYet, fontFamily = appFont, color = colors.textMuted)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sorted, key = { it.id }) { note ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.surface)
                                .border(
                                    1.dp,
                                    if (note.pinned) colors.accentWarm.copy(alpha = 0.4f) else colors.borderSubtle,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { activeNoteId = note.id }
                                .padding(15.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = note.title.ifBlank { strings.untitledNote },
                                    fontFamily = appFont,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${if (note.pinned) strings.pinnedDot else ""}${note.updatedAt}",
                                    fontFamily = appFont,
                                    fontSize = 11.sp,
                                    color = colors.accentWarm
                                )
                            }
                            Text(
                                text = note.body.ifBlank { strings.tapToWrite },
                                fontFamily = appFont,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = colors.textSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
