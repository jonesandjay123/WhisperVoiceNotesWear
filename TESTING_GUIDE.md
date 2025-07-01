# 🧪 手錶與手機同步測試指南

## 📋 修正內容摘要

我們修正了以下關鍵問題：

### 1. **SharedPreferences Key 不匹配**

- ❌ 之前：手機端讀取 `"notes"` key
- ✅ 現在：手機端讀取 `"flutter.transcription_records"` key

### 2. **資料格式轉換問題**

- ✅ 改進 JSON 解析邏輯，支援毫秒數和 ISO8601 格式
- ✅ 強化資料類型轉換

### 3. **超時和錯誤處理**

- ✅ 添加 10 秒超時機制
- ✅ 改進錯誤處理和日誌輸出

### 4. **應用 ID 衝突**

- ✅ 手錶端改為 `com.jovicheer.whisper_voice_notes.wear`

## 🚀 測試步驟

### 準備工作

1. **確保設備配對**：

   ```bash
   # 檢查設備連接
   adb devices
   ```

2. **編譯並安裝手機端**：

   ```bash
   cd flutter-whisper-voice-notes
   flutter build apk --debug
   flutter install
   ```

3. **編譯並安裝手錶端**：
   ```bash
   cd WhisperVoiceNotesWear
   ./gradlew assembleDebug
   adb -s [手錶設備ID] install -r app/build/outputs/apk/debug/app-debug.apk
   ```

### 測試流程

#### 第一步：準備測試資料

1. 打開手機上的 Whisper 語音筆記應用
2. 錄製一些測試語音並轉錄成文字
3. 確保有至少 2-3 筆記錄

#### 第二步：查看日誌

開啟兩個終端視窗來監控日誌：

**手機端日誌**：

```bash
adb logcat -s PhoneWearComm
```

**手錶端日誌**：

```bash
adb -s [手錶設備ID] logcat -s WearDataManager:* WearDataService:* MainActivity:*
```

#### 第三步：執行同步測試

1. 在手錶上打開「語音筆記手錶」應用
2. 點擊「同步筆記」按鈕
3. 觀察日誌輸出

#### 第四步：驗證結果

**✅ 成功指標**：

- 手機端日誌顯示：`"Message sent to [節點ID] successfully"`
- 手錶端日誌顯示：`"成功解析 X 筆記錄"`
- 手錶螢幕顯示筆記列表

**❌ 失敗指標**：

- 日誌顯示 `"沒有找到連接的手機節點"`
- 日誌顯示 `"No notes key found. Returning empty list"`
- 手錶顯示「沒有筆記」且不是 loading 狀態

## 🔍 除錯技巧

### 1. 檢查 SharedPreferences 內容

在手機端日誌中查找：

```
所有可用的SharedPreferences keys: [...]
```

應該看到包含 `flutter.transcription_records` 的 key。

### 2. 檢查 JSON 格式

在手機端日誌中查找：

```
Service: Got notes from SharedPreferences: [{"id":"...","text":"..."}]
```

### 3. 檢查手錶端解析

在手錶端日誌中查找：

```
收到原始 JSON: [...]
成功解析 X 筆記錄
```

### 4. 檢查設備連接

```bash
# 列出連接的設備
adb devices

# 檢查Wear OS配對狀態
adb shell dumpsys activity service GmsWearableListenerService
```

## 🐛 常見問題解決

### 問題 1：「沒有找到連接的手機節點」

**解決方案**：

1. 確認手機和手錶已通過 Wear OS 應用配對
2. 確認兩個設備都連接到 adb
3. 重啟 Wear OS 應用

### 問題 2：「No notes key found」

**解決方案**：

1. 確認手機端有記錄筆記
2. 檢查 SharedPreferences keys 日誌
3. 嘗試重新安裝手機端應用

### 問題 3：JSON 解析失敗

**解決方案**：

1. 檢查手機端發送的 JSON 格式
2. 確認 timestamp 格式是否正確
3. 查看詳細的錯誤堆疊

## 📊 測試報告範本

```
測試日期：____
設備型號：手機[____] 手錶[____]

✅/❌ 應用安裝成功
✅/❌ 設備配對正常
✅/❌ 同步按鈕響應
✅/❌ 手機端接收請求
✅/❌ 手機端發送資料
✅/❌ 手錶端接收資料
✅/❌ 手錶端顯示筆記

備註：
___________________
```

---

如果測試失敗，請提供完整的日誌輸出以進行進一步診斷。
