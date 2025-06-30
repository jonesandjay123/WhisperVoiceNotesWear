package com.jovicheer.whisper_voice_notes_wear.data

data class SyncResponse(
    val success: Boolean,
    val records: List<TranscriptionRecord>,
    val requestId: String,
    val timestamp: Long
) 