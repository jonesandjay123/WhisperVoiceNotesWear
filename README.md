# 📱⌚️ Whisper 語音筆記 WearOS 應用

**與手機端 Whisper 語音筆記應用程序對接的 WearOS 手錶應用程序，實現穩定的雙向同步功能**

## 🎉 最新更新 (v2.2.0) - 全新UI設計

✅ **界面全面重新設計，完美的手錶體驗！**

### 🎨 UI/UX 重大改進

- 🔴🟢 **智能連接狀態**: 即時顯示配對手機名稱（如 "Pixel 9 Pro"）和連接狀態
- ⟲ **優化同步按鈕**: 使用標準的刷新符號，按鈕尺寸縮小（36dp），符號比例更大
- 🎙️ **標準錄音按鈕**: 經典的紅色圓形錄音按鈕設計（80dp），符合業界標準
- 📜 **完整滾動體驗**: 整個頁面可滾動，不再固定按鈕區域，充分利用螢幕空間
- 🎯 **圓形螢幕優化**: 專為 WearOS 圓形螢幕設計的居中布局，避免邊緣被切掉
- 📏 **精確間距設計**: 32dp 頂部間距，避免按鈕頂到螢幕邊緣

### 🛠️ 功能增強

- 📱 **手機名稱顯示**: 自動識別並顯示配對手機的實際名稱
- 🔄 **模擬器友好**: 開發環境下自動使用 fallback 測試數據
- ⚡ **響應式設計**: 在不同手錶尺寸上都有最佳體驗
- 🎮 **觸控優化**: 所有按鈕都有合適的點擊區域，避免誤觸

## 🎯 主要功能

✅ **已實現並穩定運行的功能：**

- ✅ **穩定同步**: 與手機端應用程序數據雙向同步 _(已修正)_
- ✅ **筆記顯示**: 在手錶上完整顯示語音筆記列表
- ✅ **重要標記**: 支援重要筆記標記顯示 (⭐)
- ✅ **時間排序**: 按最新時間戳格式化顯示
- ✅ **同步狀態**: 實時同步狀態指示器和進度顯示
- ✅ **空狀態**: 友好的空狀態提示和操作指引
- ✅ **WearOS 優化**: 專為 WearOS 優化的 Compose UI 界面
- ✅ **整頁滾動**: 支援完整的頁面滾動，最大化內容顯示空間
- ✅ **設備識別**: 自動顯示配對手機的實際名稱和連接狀態

## 🛠️ 最新技術修正

### 🔧 關鍵問題修正 (v2.1.0)

1. **SharedPreferences Key 統一**
   - 修正手機端讀取正確的 `flutter.transcription_records` key
   - 支援多種 key 格式的 fallback 機制

2. **資料格式相容性**
   - 支援 timestamp 的毫秒數和 ISO8601 格式解析
   - 強化 JSON 資料類型轉換邏輯

3. **錯誤處理機制**
   - 添加 10 秒同步超時保護
   - 改進節點連接和錯誤恢復

4. **應用配置優化**
   - 使用獨立應用 ID: `com.jovicheer.whisper_voice_notes.wear`
   - 避免與手機端應用安裝衝突

### 🎨 UI/UX 全面重構 (v2.2.0)

1. **LazyColumn 架構**
   ```kotlin
   // 從固定 Column 改為 LazyColumn，支援整頁滾動
   LazyColumn(
       modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
       horizontalAlignment = Alignment.CenterHorizontally
   ) {
       item { Spacer(modifier = Modifier.height(32.dp)) }
       item { 連接狀態和同步按鈕區域 }
       item { 錄音按鈕 }
       items(notes) { NoteItem(note = it) }
   }
   ```

2. **智能連接狀態顯示**
   ```kotlin
   // 動態手機名稱追蹤
   private val _phoneName = MutableStateFlow("未知設備")
   val phoneName: StateFlow<String> = _phoneName.asStateFlow()
   
   // 實際設備: phoneNode.displayName ?: "手機"
   // 模擬器環境: "Pixel 9 Pro"
   ```

3. **標準錄音按鈕設計**
   ```kotlin
   // 經典紅色圓形錄音按鈕
   Box(
       modifier = Modifier
           .size(80.dp)  // 用戶調整後的最佳尺寸
           .background(color = Color(0xFFE53935), shape = CircleShape)
           .clickable { onRecordClick() }
   ) {
       // 內部白色圓點表示錄音狀態
       Box(modifier = Modifier.size(16.dp)
           .background(color = Color.White.copy(alpha = 0.3f)))
   }
   ```

4. **優化的同步按鈕**
   ```kotlin
   Button(
       onClick = onSyncClick,
       modifier = Modifier.size(36.dp),  // 縮小的按鈕尺寸
       enabled = !isLoading
   ) {
       Text(
           text = "⟲",  // 更粗體的Unicode刷新符號
           style = MaterialTheme.typography.body1
       )
   }
   ```

## 📋 技術實現

### 🏗️ 專案結構

```
com.jovicheer.whisper_voice_notes_wear/
├── data/
│   ├── TranscriptionRecord.kt      # 語音記錄數據模型
│   ├── SyncRequest.kt             # 同步請求數據模型
│   ├── SyncResponse.kt            # 同步響應數據模型
│   └── NotesRepository.kt         # 筆記數據儲存庫
├── presentation/
│   ├── MainActivity.kt            # 主要活動
│   ├── NotesScreen.kt            # 筆記列表 UI (v2.2.0 重新設計)
│   └── theme/
│       └── Theme.kt              # WearOS 主題
├── utils/
│   └── DateUtils.kt              # 時間格式化工具
├── WearDataService.kt            # WearOS 數據監聽服務
└── WearDataManager.kt            # 數據通訊管理器 (v2.2.0 增強)
```

### 🔧 核心組件

#### 1. **WearDataService** - 數據監聽服務
- 監聽來自手機的數據變化
- 處理 `/whisper/sync_response` 路徑的消息
- 自動更新本地筆記數據

#### 2. **WearDataManager** - 通訊管理器 (v2.2.0 增強)
- 發送同步請求到手機端
- 管理手機與手錶間的連接
- 處理錯誤和超時情況
- **新功能**: 追蹤手機名稱和連接狀態

#### 3. **NotesRepository** - 數據管理
- 使用 StateFlow 管理 UI 狀態
- 提供響應式數據更新
- 管理加載狀態和同步時間

#### 4. **NotesScreen** - UI 界面 (v2.2.0 完全重新設計)
- 專為 WearOS 優化的 Compose UI
- 支援圓形和方形手錶螢幕
- 包含同步按鈕和筆記列表
- **新特色**: 整頁滾動、智能布局、標準錄音按鈕

## 📡 通訊協議

### 手錶 ➡️ 手機

- **路徑**: `/whisper/sync_request`
- **數據格式**: JSON

```json
{
  "requestId": "uuid",
  "lastSyncTimestamp": 1234567890,
  "timestamp": 1234567890
}
```

### 手機 ➡️ 手錶

- **路徑**: `/whisper/sync_response`
- **數據格式**: JSON

```json
{
  "success": true,
  "records": [
    {
      "id": "uuid",
      "text": "語音轉文字內容",
      "timestamp": 1234567890,
      "isImportant": false,
      "duration": 5000,
      "isSynced": true
    }
  ],
  "requestId": "uuid",
  "timestamp": 1234567890
}
```

## 🚀 使用方法

### 1. 環境準備

```bash
# 確保手機和手錶設備連接
adb devices

# 應該看到類似輸出：
# List of devices attached
# 1A2B3C4D5E	device          # 手機
# emulator-5554	device          # 手錶 (或實體手錶設備ID)
```

### 2. 應用安裝

```bash
# 編譯手錶應用
./gradlew assembleDebug

# 安裝到手錶設備
adb -s [手錶設備ID] install -r app/build/outputs/apk/debug/app-debug.apk
```

### 3. 配對和同步 _(已修正，現在穩定工作)_

1. **設備配對**: 確保手機和手錶已透過 Wear OS 應用正確配對
2. **手機端準備**: 在手機上錄製一些語音筆記作為測試資料
3. **手錶端同步**:
   - 在手錶上啟動「語音筆記手錶」應用
   - 觀察頂部的連接狀態：🔴/🟢 + 手機名稱
   - 點擊「⟲」同步按鈕
   - 等待同步完成（通常 < 3 秒）
4. **驗證結果**: 手錶螢幕應顯示從手機同步的筆記列表

### 4. 查看和管理筆記 (v2.2.0 增強體驗)

- **整頁滾動**: 向上滾動可隱藏按鈕區域，全螢幕查看筆記
- **時間排序**: 筆記按最新時間倒序排列
- **重要標記**: 重要筆記會顯示 ⭐ 標記
- **詳細查看**: 點擊筆記查看完整內容
- **重新同步**: 隨時點擊 ⟲ 按鈕獲取最新筆記
- **錄音準備**: 紅色錄音按鈕已準備好（功能即將推出）

## 🔍 測試和除錯

### 📋 快速測試指南

詳細的測試步驟和問題診斷，請參考：
**📄 [完整測試指南](TESTING_GUIDE.md)**

### 🛠️ 基本除錯

```bash
# 手錶端應用日誌
adb -s [手錶設備ID] logcat -s WearDataManager:* WearDataService:* MainActivity:*

# 成功同步的關鍵日誌：
# ✅ "找到手機節點: [手機名稱]"
# ✅ "同步請求發送成功"
# ✅ "成功解析 X 筆記錄"
# ✅ 連接狀態從 🔴 變為 🟢
```

### 🚨 常見問題快速修復

| 問題                 | 症狀                     | v2.2.0 解決方法                           |
| -------------------- | ------------------------ | ----------------------------------------- |
| **無法找到手機節點** | "沒有找到連接的手機節點" | 檢查 Wear OS 配對狀態，觀察連接狀態指示器 |

## 🔧 開發者資訊

### 💻 同步流程 _(已修正)_

```
1. 手錶端發送同步請求 (/whisper/sync_request)
2. 手機端接收請求並讀取 SharedPreferences
   ├── Key: "flutter.transcription_records" ✅ (已修正)
   ├── 解析 JSON 筆記資料
   └── 發送回應 (/whisper/sync_response)
3. 手錶端接收資料並解析顯示
   ├── 支援多種 timestamp 格式 ✅ (已修正)
   ├── 強化錯誤處理 ✅ (已修正)
   └── 更新 UI 顯示
```

### 🗂️ 修正版本架構

```kotlin
// 手錶端應用 ID (已修正避免衝突)
applicationId = "com.jovicheer.whisper_voice_notes.wear"

// 資料解析 (已修正支援多格式)
private fun parseTimestamp(timestamp: Any?): Long {
    // 支援毫秒數和 ISO8601 格式
}

// 錯誤處理 (已修正添加超時)
Handler(Looper.getMainLooper()).postDelayed({
    if (notesRepository.isLoading.value) {
        notesRepository.setLoading(false)
    }
}, 10000) // 10 秒超時保護
```

## 🎨 UI 特色

- **WearOS 原生設計**：遵循 Wear OS 設計準則
- **響應式布局**：適配圓形和方形手錶螢幕
- **Material Design**：使用 Wear Compose Material 組件
- **友好的空狀態**：當沒有筆記時顯示提示
- **加載指示器**：同步過程中顯示進度

## 📱 與手機端對接

### ✅ 對接狀態: **完全正常工作**

確保手機端應用程序配置：

- **套件名稱**: `com.jovicheer.whisper_voice_notes` (手機端)
- **通訊協議**: WearOS MessageAPI 標準協議
- **資料格式**: 統一的 JSON 格式和 SharedPreferences key
- **請求處理**: 正確處理 `/whisper/sync_request` 請求

### 🔄 通訊協議 _(已驗證可用)_

- **手錶 → 手機**: `/whisper/sync_request` (同步請求)
- **手機 → 手錶**: `/whisper/sync_response` (筆記資料)
- **資料格式**: JSON with TranscriptionRecord 陣列
- **錯誤處理**: 10 秒超時 + 自動重試機制

## 📊 版本歷史

### v2.1.0 (2024-12-27) - 同步修正版本

**🎯 主要成就: 完全解決與手機端的同步問題**

#### ✅ 修正項目

- **資料解析**: 修正 JSON 解析邏輯，支援多種 timestamp 格式
- **錯誤處理**: 添加 10 秒超時機制和完整錯誤恢復
- **應用配置**: 修正 applicationId 避免與手機端衝突
- **日誌輸出**: 添加詳細除錯日誌便於問題診斷

#### 🛠️ 技術改進

```kotlin
// parseTimestamp() 方法支援多格式
private fun parseTimestamp(timestamp: Any?): Long {
    // 支援 Long、String(毫秒數)、ISO8601 格式
}

// 添加超時保護機制
Handler(Looper.getMainLooper()).postDelayed({
    if (notesRepository.isLoading.value) {
        notesRepository.setLoading(false)
    }
}, 10000)
```

#### 📋 測試驗證

- 創建完整測試指南 ([TESTING_GUIDE.md](TESTING_GUIDE.md))
- 提供並行日誌監控方法
- 包含常見問題快速解決方案

### v1.0.0 - 初始版本

- 基本 WearOS 應用框架
- UI 界面和資料模型
- WearOS MessageAPI 通訊基礎

## 🔒 權限需求

- `INTERNET` - 網絡通訊 (WearOS MessageAPI)
- `WAKE_LOCK` - 保持設備喚醒狀態

## 📋 系統需求

- **最低 API Level**: 33 (Android 13)
- **目標 API Level**: 36
- **WearOS 版本**: 支援所有現代 WearOS 設備
- **記憶體需求**: 最低 512MB RAM
- **存儲需求**: 10MB 可用空間

---

## 🎯 成功標準

✅ **同步成功標準** _(已達成)_:

1. 手錶能找到並連接到已配對的手機
2. 點擊「同步筆記」後 3 秒內完成同步
3. 手錶螢幕正確顯示手機端的語音筆記
4. 重要筆記正確顯示 ⭐ 標記
5. 時間戳正確格式化顯示

**🎉 對接完成！手錶現在可以穩定同步並顯示手機端的語音筆記！**
