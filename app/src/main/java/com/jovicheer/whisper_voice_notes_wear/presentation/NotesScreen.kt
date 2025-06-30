package com.jovicheer.whisper_voice_notes_wear.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.jovicheer.whisper_voice_notes_wear.data.TranscriptionRecord
import com.jovicheer.whisper_voice_notes_wear.utils.formatTimestamp

@Composable
fun NotesListScreen(
    notes: List<TranscriptionRecord>,
    isLoading: Boolean,
    onSyncClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 同步按鈕
        Button(
            onClick = onSyncClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("同步筆記")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (notes.isEmpty() && !isLoading) {
            // 空狀態
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "沒有筆記",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "點擊同步按鈕從手機獲取筆記",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // 筆記列表
            LazyColumn {
                items(notes) { note ->
                    NoteItem(note = note)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun NoteItem(note: TranscriptionRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /* 可以在這裡添加點擊事件 */ }
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = note.text,
                style = MaterialTheme.typography.body2,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colors.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTimestamp(note.timestamp),
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray
                )
                
                if (note.isImportant) {
                    Text(
                        text = "★",
                        style = MaterialTheme.typography.body2,
                        color = Color.Yellow
                    )
                }
            }
        }
    }
} 