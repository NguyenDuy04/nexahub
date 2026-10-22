package com.beulangthang.nexahub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beulangthang.nexahub.ui.screen.TodoHomeScreen
import com.beulangthang.nexahub.ui.theme.NexahubTheme
import com.beulangthang.nexahub.ui.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val taskViewModel: TaskViewModel = viewModel()
            val uiState by taskViewModel.uiState.collectAsState()

            NexahubTheme(
                themeMode = uiState.themeMode,
                appLanguage = uiState.appLanguage
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TodoHomeScreen(viewModel = taskViewModel)
                }
            }
        }
    }
}
