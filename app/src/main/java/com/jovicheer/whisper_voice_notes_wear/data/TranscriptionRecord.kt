package com.jovicheer.whisper_voice_notes_wear.data

data class TranscriptionRecord(
    val id: String,
    val text: String,
    val timestamp: Long,
    val isImportant: Boolean = false,
    val duration: Long = 0L,
    val isSynced: Boolean = true
) 