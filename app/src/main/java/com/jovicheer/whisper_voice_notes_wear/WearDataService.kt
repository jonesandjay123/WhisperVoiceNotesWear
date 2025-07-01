package com.jovicheer.whisper_voice_notes_wear

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jovicheer.whisper_voice_notes_wear.data.NotesRepository
import com.jovicheer.whisper_voice_notes_wear.data.TranscriptionRecord
import java.time.Instant

class WearDataService : WearableListenerService() {
    
    private val notesRepository = NotesRepository.getInstance()
    private val gson = Gson()
    
    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        
        Log.d("WearDataService", "收到訊息: ${messageEvent.path}")
        
        when (messageEvent.path) {
            "/whisper/sync_response" -> {
                handleSyncResponse(messageEvent.data)
            }
        }
    }
    
    private fun handleSyncResponse(data: ByteArray) {
        try {
            val json = String(data)
            Log.d("WearDataService", "收到原始 JSON: $json")
            
            // 手機端發送的是Flutter端的TranscriptionRecord列表
            // 我們需要先解析成通用的Map格式，然後轉換成手錶端的TranscriptionRecord
            val listType = object : TypeToken<List<Map<String, Any>>>() {}.type
            val recordMaps: List<Map<String, Any>> = gson.fromJson(json, listType)

            val records = recordMaps.map { recordMap ->
                TranscriptionRecord(
                    id = recordMap["id"]?.toString() ?: "",
                    text = recordMap["text"]?.toString() ?: "",
                    timestamp = parseTimestamp(recordMap["timestamp"]),
                    isImportant = recordMap["isImportant"] as? Boolean ?: false,
                    duration = when (val duration = recordMap["duration"]) {
                        is Number -> duration.toLong()
                        is String -> duration.toLongOrNull() ?: 0L
                        else -> 0L
                    },
                    isSynced = true
                )
            }

            Log.d("WearDataService", "成功解析 ${records.size} 筆記錄")
            notesRepository.updateNotes(records)
            
        } catch (e: Exception) {
            Log.e("WearDataService", "解析筆記列表 JSON 失敗", e)
        } finally {
            // 無論成功或失敗，都結束載入狀態
            notesRepository.setLoading(false)
        }
    }
    
    private fun parseTimestamp(timestamp: Any?): Long {
        return when (timestamp) {
            is Number -> timestamp.toLong()
            is String -> {
                // 嘗試解析為毫秒數
                timestamp.toLongOrNull() ?: run {
                    // 如果不是數字，嘗試解析為ISO8601格式
                    try {
                        val instant = Instant.parse(timestamp)
                        instant.toEpochMilli()
                    } catch (e: Exception) {
                        Log.w("WearDataService", "無法解析timestamp: $timestamp", e)
                        System.currentTimeMillis()
                    }
                }
            }
            else -> System.currentTimeMillis()
        }
    }
} 