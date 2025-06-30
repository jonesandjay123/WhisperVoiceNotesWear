/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.jovicheer.whisper_voice_notes_wear.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.TimeText
import com.jovicheer.whisper_voice_notes_wear.WearDataManager
import com.jovicheer.whisper_voice_notes_wear.data.NotesRepository
import com.jovicheer.whisper_voice_notes_wear.presentation.theme.WhisperVoiceNotesWearTheme

class MainActivity : ComponentActivity() {
    
    private lateinit var dataManager: WearDataManager
    private val notesRepository = NotesRepository.getInstance()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        setTheme(android.R.style.Theme_DeviceDefault)
        
        dataManager = WearDataManager(this)
        
        setContent {
            WearApp()
        }
    }
    
    @Composable
    fun WearApp() {
        WhisperVoiceNotesWearTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background),
                contentAlignment = Alignment.TopCenter
            ) {
                TimeText()
                
                val notes = notesRepository.notes.collectAsState().value
                val isLoading = notesRepository.isLoading.collectAsState().value
                
                NotesListScreen(
                    notes = notes,
                    isLoading = isLoading,
                    onSyncClick = { syncNotes() }
                )
            }
        }
    }
    
    private fun syncNotes() {
        Log.d("MainActivity", "開始同步筆記")
        dataManager.requestNotesSync()
    }
}