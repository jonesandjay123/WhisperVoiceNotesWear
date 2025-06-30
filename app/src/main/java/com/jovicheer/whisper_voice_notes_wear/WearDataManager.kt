package com.jovicheer.whisper_voice_notes_wear

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import com.jovicheer.whisper_voice_notes_wear.data.NotesRepository
import com.jovicheer.whisper_voice_notes_wear.data.SyncRequest
import java.util.*

class WearDataManager(private val context: Context) {
    
    private val messageClient = Wearable.getMessageClient(context)
    private val nodeClient = Wearable.getNodeClient(context) 
    private val notesRepository = NotesRepository.getInstance()
    private val gson = Gson()
    
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
                
                messageClient.sendMessage(
                    phoneNode.id,
                    "/whisper/sync_request",
                    data
                ).addOnFailureListener { e ->
                    Log.e("WearDataManager", "發送消息失敗", e)
                    notesRepository.setLoading(false)
                }
            } else {
                Log.w("WearDataManager", "沒有找到連接的手機節點")
                notesRepository.setLoading(false)
            }
        }.addOnFailureListener { e ->
            Log.e("WearDataManager", "獲取節點失敗", e)
            notesRepository.setLoading(false)
        }
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