package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppScreen
import com.example.ui.CombatGameScreen
import com.example.ui.GameViewModel
import com.example.ui.HangarScreen
import com.example.ui.MainMenuScreen
import com.example.ui.MissionSelectScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme

import com.example.ui.MissionBriefingScreen

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

                BackHandler(enabled = currentScreen != AppScreen.MAIN_MENU) {
                    when (currentScreen) {
                        AppScreen.IN_GAME -> viewModel.navigateTo(AppScreen.MAIN_MENU)
                        AppScreen.MISSION_BRIEFING -> viewModel.navigateTo(AppScreen.MISSION_SELECT)
                        AppScreen.MISSION_SELECT -> viewModel.navigateTo(AppScreen.MAIN_MENU)
                        AppScreen.HANGAR -> viewModel.navigateTo(AppScreen.MAIN_MENU)
                        else -> {}
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    when (currentScreen) {
                        AppScreen.MAIN_MENU -> MainMenuScreen(viewModel)
                        AppScreen.HANGAR -> HangarScreen(viewModel)
                        AppScreen.MISSION_SELECT -> MissionSelectScreen(viewModel)
                        AppScreen.MISSION_BRIEFING -> MissionBriefingScreen(viewModel)
                        AppScreen.IN_GAME -> CombatGameScreen(viewModel)
                        else -> MainMenuScreen(viewModel)
                    }
                }
            }
        }
    }
}

