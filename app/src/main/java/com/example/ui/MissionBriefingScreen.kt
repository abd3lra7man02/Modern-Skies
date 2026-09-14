package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.MissionDatabase
import com.example.ui.components.MilitaryPanel
import com.example.ui.components.TacticalButton
import com.example.ui.components.TacticalButtonType
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun MissionBriefingScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val selectedMissionNum by viewModel.selectedMissionNumber.collectAsStateWithLifecycle()
    val mission = MissionDatabase.missions.find { it.number == selectedMissionNum }
        ?: MissionDatabase.missions[0]

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepNavy, DarkSteelVariant, DeepNavy)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // TOP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        viewModel.soundManager.playButtonClick()
                        viewModel.navigateTo(AppScreen.MISSION_SELECT)
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Mission Select",
                        tint = CyanHighlight
                    )
                }
                Text(
                    text = "MISSION BRIEFING",
                    color = CyanHighlight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // CONTENT
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(400)) + slideInHorizontally(tween(400), initialOffsetX = { -100 })
            ) {
                Column {
                    Text(
                        text = mission.codename,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "THEATER: ${mission.theater.uppercase()}",
                        color = CyanHighlight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    MilitaryPanel(title = "INTEL", modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = mission.briefing,
                            color = MutedGray,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    MilitaryPanel(title = "OBJECTIVES", modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                            Icon(Icons.Default.Adjust, contentDescription = "Primary", tint = HealthGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("PRIMARY OBJECTIVE", color = HealthGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(mission.primaryObjective, color = Color.White, fontSize = 14.sp)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.OutlinedFlag, contentDescription = "Secondary", tint = WarmOrange, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("SECONDARY OBJECTIVE", color = WarmOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(mission.secondaryObjective, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (mission.enemyAce != null || mission.hasDynamicEvent) {
                        MilitaryPanel(title = "THREAT ASSESSMENT", modifier = Modifier.fillMaxWidth()) {
                            if (mission.enemyAce != null) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                                    Icon(Icons.Default.Warning, contentDescription = "Ace", tint = DangerRedBright, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("ENEMY ACE PRESENT", color = DangerRedBright, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(mission.enemyAce, color = Color.White, fontSize = 14.sp)
                                    }
                                }
                            }
                            if (mission.hasDynamicEvent) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Bolt, contentDescription = "Event", tint = CreditGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("DYNAMIC EVENT EXPECTED", color = CreditGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(mission.eventDescription, color = Color.White, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // BOTTOM BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TacticalButton(
                onClick = {
                    viewModel.soundManager.playButtonClick()
                    viewModel.startSortie(mission.number)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                type = TacticalButtonType.PRIMARY,
                testTag = "start_mission_button",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.FlightTakeoff,
                        contentDescription = "Start",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            ) {
                Text(
                    text = "COMMENCE OPERATION",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}
