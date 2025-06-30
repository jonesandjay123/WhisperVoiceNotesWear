package com.jovicheer.whisper_voice_notes_wear.data

data class SyncRequest(
    val requestId: String,
    val lastSyncTimestamp: Long,
    val timestamp: Long
) 