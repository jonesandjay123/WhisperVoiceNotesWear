package com.jovicheer.whisper_voice_notes_wear

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.google.gson.Gson
import com.jovicheer.whisper_voice_notes_wear.data.NotesRepository
import com.jovicheer.whisper_voice_notes_wear.data.SyncResponse

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
            Log.d("WearDataService", "收到JSON: $json")
            
            val response = gson.fromJson(json, SyncResponse::class.java)
            
            if (response.success) {
                Log.d("WearDataService", "收到 ${response.records.size} 筆記錄")
                notesRepository.updateNotes(response.records)
            } else {
                Log.w("WearDataService", "同步失敗")
            }
            
            notesRepository.setLoading(false)
            
        } catch (e: Exception) {
            Log.e("WearDataService", "解析數據失敗", e)
            notesRepository.setLoading(false)
        }
    }
} 