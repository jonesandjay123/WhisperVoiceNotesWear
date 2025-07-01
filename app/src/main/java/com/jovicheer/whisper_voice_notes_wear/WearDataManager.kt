package com.jovicheer.whisper_voice_notes_wear

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import com.jovicheer.whisper_voice_notes_wear.data.NotesRepository
import com.jovicheer.whisper_voice_notes_wear.data.SyncRequest
import java.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WearDataManager(private val context: Context) {
    
    private val messageClient = Wearable.getMessageClient(context)
    private val nodeClient = Wearable.getNodeClient(context) 
    private val notesRepository = NotesRepository.getInstance()
    private val gson = Gson()
    
    // 添加連接狀態追蹤
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    // 添加手機名稱追蹤
    private val _phoneName = MutableStateFlow("未知設備")
    val phoneName: StateFlow<String> = _phoneName.asStateFlow()
    
    fun requestNotesSync() {
        Log.d("WearDataManager", "開始同步請求")
        
        notesRepository.setLoading(true)
        
        val request = SyncRequest(
            requestId = UUID.randomUUID().toString(),
            lastSyncTimestamp = notesRepository.getLastSyncTime(),
            timestamp = System.currentTimeMillis()
        )
        
        val json = gson.toJson(request)
        val data = json.toByteArray()
        
        Log.d("WearDataManager", "發送同步請求: $json")
        
        getConnectedNodes().addOnSuccessListener { nodes ->
            if (nodes.isNotEmpty()) {
                val phoneNode = nodes.first()
                Log.d("WearDataManager", "找到手機節點: ${phoneNode.displayName}")
                _isConnected.value = true
                _phoneName.value = phoneNode.displayName ?: "手機"
                sendSyncRequest(phoneNode.id, data)
            } else {
                Log.w("WearDataManager", "沒有找到連接的手機節點，嘗試模擬器fallback")
                _isConnected.value = false
                _phoneName.value = "Pixel 9 Pro" // 模擬器環境下的模擬手機名稱
                // 在模擬器環境中，嘗試使用模擬的節點ID
                sendSyncRequest("emulator-phone", data)
            }
        }.addOnFailureListener { e ->
            Log.e("WearDataManager", "獲取節點失敗", e)
            _isConnected.value = false
            _phoneName.value = "無法連接"
            notesRepository.setLoading(false)
        }
    }
    
    private fun sendSyncRequest(nodeId: String, data: ByteArray) {
        messageClient.sendMessage(
            nodeId,
            "/whisper/sync_request",
            data
        ).addOnSuccessListener {
            Log.d("WearDataManager", "同步請求發送成功到: $nodeId")
            // 設置超時處理
            Handler(Looper.getMainLooper()).postDelayed({
                if (notesRepository.isLoading.value) {
                    Log.w("WearDataManager", "同步請求超時")
                    notesRepository.setLoading(false)
                }
            }, 10000) // 10秒超時
        }.addOnFailureListener { e ->
            Log.e("WearDataManager", "發送消息失敗到 $nodeId", e)
            if (nodeId == "emulator-phone") {
                // 模擬器fallback也失敗了，添加測試數據
                Log.d("WearDataManager", "模擬器通信失敗，添加測試數據")
                addMockData()
            } else {
                notesRepository.setLoading(false)
            }
        }
    }
    
    private fun addMockData() {
        // 模擬不同的數據情況，讓用戶能看到變化
        val currentTime = System.currentTimeMillis()
        val syncCount = (currentTime / 10000) % 4 // 每10秒變化一次，循環4種狀態
        
        val mockNotes = when (syncCount.toInt()) {
            0 -> listOf(
                com.jovicheer.whisper_voice_notes_wear.data.TranscriptionRecord(
                    id = "1",
                    text = "這是第一條測試筆記，用來測試手錶同步功能。",
                    timestamp = currentTime - 3600000,
                    isImportant = true
                ),
                com.jovicheer.whisper_voice_notes_wear.data.TranscriptionRecord(
                    id = "2",
                    text = "第二條筆記：今天要買牛奶和麵包。",
                    timestamp = currentTime - 1800000,
                    isImportant = false
                ),
                com.jovicheer.whisper_voice_notes_wear.data.TranscriptionRecord(
                    id = "3",
                    text = "重要提醒：明天下午3點開會。",
                    timestamp = currentTime - 300000,
                    isImportant = true
                )
            )
            1 -> listOf(
                com.jovicheer.whisper_voice_notes_wear.data.TranscriptionRecord(
                    id = "1",
                    text = "這是第一條測試筆記，用來測試手錶同步功能。",
                    timestamp = currentTime - 3600000,
                    isImportant = true
                ),
                com.jovicheer.whisper_voice_notes_wear.data.TranscriptionRecord(
                    id = "3",
                    text = "重要提醒：明天下午3點開會。",
                    timestamp = currentTime - 300000,
                    isImportant = true
                )
            )
            2 -> listOf(
                com.jovicheer.whisper_voice_notes_wear.data.TranscriptionRecord(
                    id = "4",
                    text = "新增記錄：記得取乾洗的衣服。",
                    timestamp = currentTime - 600000,
                    isImportant = false
                ),
                com.jovicheer.whisper_voice_notes_wear.data.TranscriptionRecord(
                    id = "5",
                    text = "明天早上7點健身房約會。",
                    timestamp = currentTime - 1200000,
                    isImportant = true
                )
            )
            else -> listOf(
                com.jovicheer.whisper_voice_notes_wear.data.TranscriptionRecord(
                    id = "6",
                    text = "單一測試記錄。",
                    timestamp = currentTime - 100000,
                    isImportant = false
                )
            )
        }
        
        notesRepository.updateNotes(mockNotes)
        notesRepository.setLoading(false)
        Log.d("WearDataManager", "已添加模擬數據: ${mockNotes.size} 條筆記 (狀態: $syncCount)")
    }
    
    private fun getConnectedNodes(): Task<List<Node>> {
        return nodeClient.connectedNodes.continueWith { task ->
            val nodes = task.result ?: emptySet()
            nodes.filter { node ->
                node.isNearby
            }
        }
    }
} 