package com.jovicheer.whisper_voice_notes_wear

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jovicheer.whisper_voice_notes_wear.data.NotesRepository
import com.jovicheer.whisper_voice_notes_wear.data.TranscriptionRecord

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
            
            // 手機端現在直接發送筆記列表的 JSON 字符串
            // 我們使用 TypeToken 來正確解析這個列表
            val listType = object : TypeToken<List<TranscriptionRecord>>() {}.type
            val records: List<TranscriptionRecord> = gson.fromJson(json, listType)

            Log.d("WearDataService", "成功解析 ${records.size} 筆記錄")
            notesRepository.updateNotes(records)
            
        } catch (e: Exception) {
            Log.e("WearDataService", "解析筆記列表 JSON 失敗", e)
        } finally {
            // 無論成功或失敗，都結束載入狀態
            notesRepository.setLoading(false)
        }
    }
} 