package com.jovicheer.whisper_voice_notes_wear.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
    isConnected: Boolean = false,
    phoneName: String = "未知設備",
    onSyncClick: () -> Unit,
    onRecordClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 頂部間距
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        // 連接狀態和同步按鈕區域
        item {
            Row(
                modifier = Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 連接狀態指示器 + 手機名稱 - 縮小尺寸
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 狀態燈 - 縮小
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFF44336),
                                shape = CircleShape
                            )
                    )
                    // 手機名稱 - 縮小字體
                    Text(
                        text = phoneName,
                        style = MaterialTheme.typography.body2,
                        color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFF44336),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // 同步按鈕 - 縮小按鈕，放大圖標
                Button(
                    onClick = onSyncClick,
                    modifier = Modifier.size(36.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "⟲", // 更粗體的刷新符號
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onPrimary
                        )
                    }
                }
            }
        }
        
        // 間距
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // 錄音按鈕 - 標準紅色錄音按鈕，更大
        item {
            // 直接使用紅色圓形背景作為錄音按鈕
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = Color(0xFFE53935),
                        shape = CircleShape
                    )
                    .clickable { onRecordClick() },
                contentAlignment = Alignment.Center
            ) {
                // 可以添加小的內部圓點表示錄音狀態
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                )
            }
        }
        
        // 間距
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
        
        // 筆記內容區域
        if (notes.isEmpty() && !isLoading) {
            // 空狀態
            item {
                Column(
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
                        text = "點擊同步按鈕獲取筆記\n或錄音按鈕開始錄音",
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // 筆記列表
            items(notes) { note ->
                NoteItem(note = note)
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
        
        // 底部間距
        item {
            Spacer(modifier = Modifier.height(16.dp))
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