# 📱⌚️ Whisper 語音筆記 WearOS 應用

這是一個與手機端 Whisper 語音筆記應用程序對接的 WearOS 手錶應用程序，可以在手錶上查看和同步語音筆記。

## 🎯 主要功能

✅ **已實現的功能：**
- ✅ 與手機端應用程序數據同步
- ✅ 在手錶上顯示語音筆記列表
- ✅ 支援重要筆記標記顯示 (⭐)
- ✅ 時間戳格式化顯示
- ✅ 同步狀態指示器
- ✅ 空狀態友好提示
- ✅ WearOS 優化 UI 界面

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
│   ├── NotesScreen.kt            # 筆記列表 UI
│   └── theme/
│       └── Theme.kt              # WearOS 主題
├── utils/
│   └── DateUtils.kt              # 時間格式化工具
├── WearDataService.kt            # WearOS 數據監聽服務
└── WearDataManager.kt            # 數據通訊管理器
```

### 🔧 核心組件

#### 1. **WearDataService** - 數據監聽服務
- 監聽來自手機的數據變化
- 處理 `/whisper/sync_response` 路徑的消息
- 自動更新本地筆記數據

#### 2. **WearDataManager** - 通訊管理器
- 發送同步請求到手機端
- 管理手機與手錶間的連接
- 處理錯誤和超時情況

#### 3. **NotesRepository** - 數據管理
- 使用 StateFlow 管理 UI 狀態
- 提供響應式數據更新
- 管理加載狀態和同步時間

#### 4. **NotesScreen** - UI 界面
- 專為 WearOS 優化的 Compose UI
- 支援圓形和方形手錶螢幕
- 包含同步按鈕和筆記列表

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

### 1. 部署到手錶
```bash
# 連接手錶設備
adb devices

# 安裝應用
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 2. 配對和同步
1. 確保手機和手錶已透過 Wear OS 應用配對
2. 在手錶上啟動「語音筆記手錶」應用
3. 點擊「同步筆記」按鈕
4. 等待從手機端獲取筆記數據

### 3. 查看筆記
- 筆記按時間倒序排列
- 重要筆記會顯示 ⭐ 標記
- 點擊筆記可查看詳細內容

## 🔍 偵錯和日誌

查看應用運行日誌：
```bash
# 查看 WearOS 應用日誌
adb logcat | grep "WearDataService\|WearDataManager\|MainActivity"

# 查看特定標籤
adb logcat -s WearDataService
```

## 📱 與手機端對接

確保手機端應用程序：
- 使用相同的套件名稱前綴：`com.jovicheer.whisper_voice_notes`
- 實現相同的通訊協議
- 正確處理 `/whisper/sync_request` 請求

## 🎨 UI 特色

- **WearOS 原生設計**：遵循 Wear OS 設計準則
- **響應式布局**：適配圓形和方形手錶螢幕
- **Material Design**：使用 Wear Compose Material 組件
- **友好的空狀態**：當沒有筆記時顯示提示
- **加載指示器**：同步過程中顯示進度

## 🔒 權限需求

- `INTERNET` - 網絡通訊
- `WAKE_LOCK` - 保持設備喚醒狀態

## 📋 系stat需求

- **最低 API Level**: 33 (Android 13)
- **目標 API Level**: 36
- **WearOS 版本**: 支援所有現代 WearOS 設備

---

**🎯 成功標準**: 當手錶能夠成功顯示從手機同步過來的語音筆記時，對接就完成了！ 