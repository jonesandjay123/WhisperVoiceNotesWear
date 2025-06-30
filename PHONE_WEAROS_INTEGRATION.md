# 📱⌚️ 手機端 WearOS 整合指南

## 🎯 問題分析
手錶端已成功發送同步請求到手機，但手機端缺少 WearOS 通訊功能來回應手錶的請求。

**日誌證據：**
```
06-30 17:23:20.691 D MainActivity: 開始同步筆記
06-30 17:23:20.692 D WearDataManager: 開始同步請求  
06-30 17:23:20.761 D WearDataManager: 發送同步請求: {"lastSyncTimestamp":0,"requestId":"...","timestamp":1751318600696}
06-30 17:23:24.335 D WearDataManager: 找到手機節點: Pixel 8 Pro
```

✅ **手錶端狀態：** 正常發送請求  
❌ **手機端狀態：** 未實現 WearOS 監聽和回應功能

---

## 🛠️ 手機端需要實現的功能

### 1. 添加 WearOS 依賴

在手機端 `app/build.gradle` 中添加：

```gradle
dependencies {
    // WearOS 通訊
    implementation 'com.google.android.gms:play-services-wearable:18.1.0'
    implementation 'com.google.code.gson:gson:2.10.1'
    
    // 現有依賴...
}
```

### 2. 更新 AndroidManifest.xml

在手機端 `AndroidManifest.xml` 中添加：

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- 現有權限... -->
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    
    <application>
        <!-- 現有活動... -->
        
        <!-- WearOS 數據監聽服務 -->
        <service android:name=".PhoneWearService"
            android:exported="true">
            <intent-filter>
                <action android:name="com.google.android.gms.wearable.MESSAGE_RECEIVED" />
                <action android:name="com.google.android.gms.wearable.DATA_CHANGED" />
                <data android:scheme="wear" android:host="*" />
            </intent-filter>
        </service>
        
    </application>
</manifest>
```

### 3. 創建數據模型（與手錶端保持一致）

```kotlin
// data/WearModels.kt
package com.jovicheer.whisper_voice_notes.data

data class WearSyncRequest(
    val requestId: String,
    val lastSyncTimestamp: Long,
    val timestamp: Long
)

data class WearSyncResponse(
    val success: Boolean,
    val records: List<WearTranscriptionRecord>,
    val requestId: String,
    val timestamp: Long
)

data class WearTranscriptionRecord(
    val id: String,
    val text: String,
    val timestamp: Long,
    val isImportant: Boolean = false,
    val duration: Long = 0L,
    val isSynced: Boolean = true
)
```

### 4. 實現 WearOS 監聽服務

```kotlin
// PhoneWearService.kt
package com.jovicheer.whisper_voice_notes

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.google.gson.Gson
import com.jovicheer.whisper_voice_notes.data.WearSyncRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PhoneWearService : WearableListenerService() {
    
    private val gson = Gson()
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    
    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        
        Log.d("PhoneWearService", "收到來自手錶的訊息: ${messageEvent.path}")
        
        when (messageEvent.path) {
            "/whisper/sync_request" -> {
                handleSyncRequest(messageEvent.sourceNodeId, messageEvent.data)
            }
        }
    }
    
    private fun handleSyncRequest(sourceNodeId: String, data: ByteArray) {
        serviceScope.launch {
            try {
                val json = String(data)
                Log.d("PhoneWearService", "收到同步請求: $json")
                
                val request = gson.fromJson(json, WearSyncRequest::class.java)
                
                // 獲取筆記數據並發送回手錶
                val wearDataManager = PhoneWearDataManager(this@PhoneWearService)
                wearDataManager.sendNotesToWatch(sourceNodeId, request)
                
            } catch (e: Exception) {
                Log.e("PhoneWearService", "處理同步請求失敗", e)
            }
        }
    }
}
```

### 5. 實現數據管理器

```kotlin
// PhoneWearDataManager.kt
package com.jovicheer.whisper_voice_notes

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import com.jovicheer.whisper_voice_notes.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PhoneWearDataManager(private val context: Context) {
    
    private val messageClient = Wearable.getMessageClient(context)
    private val gson = Gson()
    
    suspend fun sendNotesToWatch(watchNodeId: String, request: WearSyncRequest) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("PhoneWearDataManager", "準備發送筆記到手錶")
                
                // 🔥 這裡需要從你的實際數據庫獲取筆記
                val notes = getNotesFromDatabase(request.lastSyncTimestamp)
                
                // 轉換為手錶端格式
                val wearRecords = notes.map { note ->
                    WearTranscriptionRecord(
                        id = note.id,
                        text = note.text,
                        timestamp = note.timestamp,
                        isImportant = note.isImportant,
                        duration = note.duration,
                        isSynced = true
                    )
                }
                
                val response = WearSyncResponse(
                    success = true,
                    records = wearRecords,
                    requestId = request.requestId,
                    timestamp = System.currentTimeMillis()
                )
                
                val json = gson.toJson(response)
                val data = json.toByteArray()
                
                Log.d("PhoneWearDataManager", "發送 ${wearRecords.size} 筆記錄到手錶")
                Log.d("PhoneWearDataManager", "回應數據: $json")
                
                messageClient.sendMessage(
                    watchNodeId,
                    "/whisper/sync_response",
                    data
                ).addOnSuccessListener {
                    Log.d("PhoneWearDataManager", "成功發送數據到手錶")
                }.addOnFailureListener { e ->
                    Log.e("PhoneWearDataManager", "發送數據到手錶失敗", e)
                }
                
            } catch (e: Exception) {
                Log.e("PhoneWearDataManager", "發送筆記到手錶失敗", e)
                
                // 發送錯誤回應
                sendErrorResponse(watchNodeId, request.requestId, e.message ?: "未知錯誤")
            }
        }
    }
    
    // 🔥 重要：這個方法需要根據你的實際數據庫實現來修改
    private suspend fun getNotesFromDatabase(lastSyncTimestamp: Long): List<TranscriptionRecord> {
        // ⚠️ 請替換為你的實際數據庫查詢邏輯
        // 例如：
        // return transcriptionDao.getAllRecordsAfter(lastSyncTimestamp)
        // 或者：
        // return repository.getNotesForWearSync(lastSyncTimestamp)
        
        // 📝 暫時返回空列表，請替換為實際實現
        Log.w("PhoneWearDataManager", "⚠️ 請實現 getNotesFromDatabase() 方法")
        return emptyList()
        
        /* 
        示例實現（請根據你的實際數據結構修改）：
        return withContext(Dispatchers.IO) {
            // 如果使用 Room 數據庫
            database.transcriptionDao().getAllRecords()
            
            // 如果使用 Repository 模式
            repository.getAllNotes().filter { it.timestamp > lastSyncTimestamp }
            
            // 如果使用 SharedPreferences 或其他存儲方式
            // 請相應調整
        }
        */
    }
    
    private fun sendErrorResponse(watchNodeId: String, requestId: String, errorMessage: String) {
        val errorResponse = WearSyncResponse(
            success = false,
            records = emptyList(),
            requestId = requestId,
            timestamp = System.currentTimeMillis()
        )
        
        val json = gson.toJson(errorResponse)
        val data = json.toByteArray()
        
        messageClient.sendMessage(watchNodeId, "/whisper/sync_response", data)
        Log.d("PhoneWearDataManager", "發送錯誤回應: $errorMessage")
    }
}
```

---

## 🔧 整合步驟

### 步驟 1: 添加依賴和權限
- 在 `build.gradle` 中添加 WearOS 依賴
- 在 `AndroidManifest.xml` 中添加服務配置

### 步驟 2: 創建 WearOS 相關檔案
- 創建 `PhoneWearService.kt`
- 創建 `PhoneWearDataManager.kt`  
- 創建數據模型檔案

### 步驟 3: 實現數據庫查詢
- 在 `PhoneWearDataManager.getNotesFromDatabase()` 中實現實際的數據庫查詢
- 確保返回 `List<TranscriptionRecord>` 格式的數據

### 步驟 4: 測試對接
- 重新編譯手機端應用
- 在手錶上點擊同步按鈕
- 查看兩端的日誌確認通訊成功

---

## 🔍 偵錯和測試

### 查看手機端日誌：
```bash
adb -s [手機設備ID] logcat | grep "PhoneWearService\|PhoneWearDataManager"
```

### 預期的成功日誌：
```
PhoneWearService: 收到來自手錶的訊息: /whisper/sync_request
PhoneWearService: 收到同步請求: {"requestId":"...","lastSyncTimestamp":0,"timestamp":...}
PhoneWearDataManager: 準備發送筆記到手錶
PhoneWearDataManager: 發送 X 筆記錄到手錶
PhoneWearDataManager: 成功發送數據到手錶
```

---

## ⚠️ 重要提醒

1. **數據庫整合是關鍵**：請務必在 `getNotesFromDatabase()` 方法中實現實際的數據庫查詢邏輯

2. **數據格式一致性**：確保手機端的 `TranscriptionRecord` 能正確轉換為 `WearTranscriptionRecord`

3. **錯誤處理**：實現了完整的錯誤處理和日誌記錄

4. **權限檢查**：確保手機和手錶已正確配對

---

## 🎯 成功指標

當看到以下情況時，表示整合成功：
- ✅ 手機端日誌顯示收到手錶的同步請求
- ✅ 手機端成功發送數據回手錶  
- ✅ 手錶端載入圈圈停止，顯示筆記列表
- ✅ 手錶上能看到從手機同步過來的筆記內容

實現這些功能後，手錶與手機的數據同步就能正常工作了！🚀 