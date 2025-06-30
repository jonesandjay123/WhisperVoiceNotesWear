package com.jovicheer.whisper_voice_notes_wear.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotesRepository private constructor() {
    
    private val _notes = MutableStateFlow<List<TranscriptionRecord>>(emptyList())
    val notes: StateFlow<List<TranscriptionRecord>> = _notes.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _lastSyncTime = MutableStateFlow(0L)
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()
    
    fun updateNotes(newNotes: List<TranscriptionRecord>) {
        _notes.value = newNotes.sortedByDescending { it.timestamp }
        _lastSyncTime.value = System.currentTimeMillis()
    }
    
    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
    
    fun getLastSyncTime(): Long {
        return _lastSyncTime.value
    }
    
    companion object {
        @Volatile
        private var INSTANCE: NotesRepository? = null
        
        fun getInstance(): NotesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotesRepository().also { INSTANCE = it }
            }
        }
    }
} 