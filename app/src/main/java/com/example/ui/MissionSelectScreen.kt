package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.MissionDatabase
import com.example.model.MissionInfo
import com.example.model.MissionType
import com.example.ui.components.MilitaryPanel
import com.example.ui.components.TacticalButton
import com.example.ui.components.TacticalButtonType
import com.example.ui.theme.CreditGold
import com.example.ui.theme.CreditText
import com.example.ui.theme.CyanHighlight
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedBright
import com.example.ui.theme.DarkSteel
import com.example.ui.theme.DarkSteelVariant
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.HealthGreen
import com.example.ui.theme.MilitaryGreen
import com.example.ui.theme.MilitaryGreenBorder
import com.example.ui.theme.MutedGray
import com.example.ui.theme.SecondarySteel
import com.example.ui.theme.TacticalBlue
import com.example.ui.theme.WarmOrange
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MissionSelectScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val missionProgressList by viewModel.missionProgress.collectAsStateWithLifecycle()
    val selectedMissionNum by viewModel.selectedMissionNumber.collectAsStateWithLifecycle()

    val highestUnlocked = profile?.highestUnlockedMission ?: 1
    val selectedMission = MissionDatabase.missions.find { it.number == selectedMissionNum }
        ?: MissionDatabase.missions[0]

    val currentCredits = profile?.credits ?: 0
    val totalStarsEarned = remember(missionProgressList) {
        missionProgressList.sumOf { it.stars }
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
    ) {
        // =====================================================================
        // 1. TOP TACTICAL HEADER
        // =====================================================================
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
                        viewModel.navigateTo(AppScreen.MAIN_MENU)
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSteel)
                        .border(1.dp, TacticalBlue.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .testTag("mission_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Return to Command Center",
                        tint = CyanHighlight,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "THEATER CAMPAIGN",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.6.sp
                    )
                    Text(
                        text = "STRATEGIC THEATER MAP • SECTORS 1-10",
                        color = CyanHighlight,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            // Right Status Badges: Stars and Credits
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Stars Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSteel)
                        .border(1.dp, TacticalBlue.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = CreditGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$totalStarsEarned/30",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Credits Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSteel)
                        .border(1.dp, CreditGold.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = NumberFormat.getNumberInstance(Locale.US).format(currentCredits),
                        color = CreditGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "CR",
                        color = CreditText,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // =====================================================================
        // 2. OPERATION STRATEGIC MAP CANVAS & NODES (MISSIONS 1 - 10)
        // =====================================================================
        MilitaryPanel(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            title = "CAMPAIGN PROGRESSION FLIGHT PATH",
            subTitle = "ACTIVE REGIONS & HIGHWAY CORRIDORS",
            shape = RoundedCornerShape(8.dp)
        ) {
            CampaignFlightPathMap(
                missions = MissionDatabase.missions,
                highestUnlocked = highestUnlocked,
                selectedMissionNum = selectedMissionNum,
                missionProgressList = missionProgressList,
                onSelectMission = { missionNum ->
                    viewModel.soundManager.playButtonClick()
                    viewModel.selectMission(missionNum)
                }
            )
        }

        // =====================================================================
        // 3. SELECTED MISSION DOSSIER / TACTICAL MISSION CARD
        // =====================================================================
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                val isUnlocked = selectedMission.number <= highestUnlocked
                val progress = missionProgressList.find { it.missionNumber == selectedMission.number }
                val starsEarned = progress?.stars ?: 0
                val bestScore = progress?.highScore ?: 0

                TacticalMissionDossierCard(
                    mission = selectedMission,
                    isUnlocked = isUnlocked,
                    starsEarned = starsEarned,
                    bestScore = bestScore
                )
            }
        }

        // =====================================================================
        // 4. BOTTOM ACTION LAUNCH BANNER
        // =====================================================================
        val isSelectedUnlocked = selectedMission.number <= highestUnlocked

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSteel)
                .border(1.dp, TacticalBlue.copy(alpha = 0.4f))
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary: Jump to Hangar to modify loadout
                TacticalButton(
                    onClick = {
                        viewModel.soundManager.playButtonClick()
                        viewModel.navigateTo(AppScreen.HANGAR)
                    },
                    modifier = Modifier
                        .weight(0.35f)
                        .height(52.dp),
                    type = TacticalButtonType.SECONDARY,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Garage",
                            tint = CyanHighlight,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                ) {
                    Text(
                        text = "GARAGE",
                        color = CyanHighlight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Primary Launch Sortie Button
                TacticalButton(
                    onClick = {
                        viewModel.soundManager.playButtonClick()
                        viewModel.startSortie(selectedMission.number)
                    },
                    enabled = isSelectedUnlocked,
                    modifier = Modifier
                        .weight(0.65f)
                        .height(52.dp),
                    type = when {
                        !isSelectedUnlocked -> TacticalButtonType.SECONDARY
                        selectedMission.isBossMission -> TacticalButtonType.DANGER
                        else -> TacticalButtonType.PRIMARY
                    },
                    testTag = "launch_sortie_button",
                    leadingIcon = {
                        Icon(
                            imageVector = if (selectedMission.isBossMission) Icons.Default.Warning else Icons.Default.FlightTakeoff,
                            contentDescription = "Launch",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                ) {
                    Text(
                        text = when {
                            !isSelectedUnlocked -> "SECTOR LOCKED"
                            selectedMission.isBossMission -> "ENGAGE GOLIATH"
                            else -> "LAUNCH SORTIE ${selectedMission.number}"
                        },
                        color = if (isSelectedUnlocked) Color.White else MutedGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                }
            }
        }
    }
}

// =============================================================================
// COMPONENT: STRATEGIC FLIGHT PATH MAP (NODES & CONNECTING LINES)
// =============================================================================

@Composable
private fun CampaignFlightPathMap(
    missions: List<MissionInfo>,
    highestUnlocked: Int,
    selectedMissionNum: Int,
    missionProgressList: List<com.example.data.MissionProgress>,
    onSelectMission: (Int) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flight_map_anim")
    val beaconPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beacon_pulse"
    )

    val scrollState = rememberScrollState()

    // Horizontal Operation Ribbon with canvas background lines
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF131D24))
    ) {
        // Background Tactical Map Grid & Topographic Vector Contour Lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Horizontal & Vertical Grid Lines
            val gridSpacing = 24.dp.toPx()
            var x = 0f
            while (x < w) {
                drawLine(
                    TacticalBlue.copy(alpha = 0.08f),
                    Offset(x, 0f),
                    Offset(x, h),
                    strokeWidth = 1f
                )
                x += gridSpacing
            }
            var y = 0f
            while (y < h) {
                drawLine(
                    TacticalBlue.copy(alpha = 0.08f),
                    Offset(0f, y),
                    Offset(w, y),
                    strokeWidth = 1f
                )
                y += gridSpacing
            }
        }

        // Scrollable Nodes Ribbon with Connecting Vector Lines
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            missions.forEachIndexed { index, mission ->
                val isUnlocked = mission.number <= highestUnlocked
                val isSelected = mission.number == selectedMissionNum
                val progress = missionProgressList.find { it.missionNumber == mission.number }
                val stars = progress?.stars ?: 0

                // 1. Mission Node Element
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable(enabled = isUnlocked) { onSelectMission(mission.number) }
                        .padding(vertical = 8.dp)
                ) {
                    // Region / Sector tag
                    Text(
                        text = "S-${mission.number}",
                        color = when {
                            isSelected -> CyanHighlight
                            isUnlocked -> TacticalBlue
                            else -> MutedGray.copy(alpha = 0.6f)
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Tactical Node Blip
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected && mission.isBossMission -> DangerRedBright.copy(alpha = 0.3f)
                                    isSelected -> CyanHighlight.copy(alpha = 0.25f)
                                    isUnlocked && mission.isBossMission -> DangerRed.copy(alpha = 0.2f)
                                    isUnlocked -> SecondarySteel
                                    else -> DarkSteelVariant
                                }
                            )
                            .border(
                                width = if (isSelected) 2.5.dp else 1.5.dp,
                                color = when {
                                    mission.isBossMission && isUnlocked -> DangerRedBright
                                    isSelected -> CyanHighlight
                                    isUnlocked -> TacticalBlue
                                    else -> MutedGray.copy(alpha = 0.3f)
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Pulsing outer beacon for current/selected node
                        if (isSelected) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = if (mission.isBossMission) DangerRedBright.copy(alpha = beaconPulse * 0.4f)
                                    else CyanHighlight.copy(alpha = beaconPulse * 0.4f),
                                    radius = (size.minDimension / 2f) + 4.dp.toPx() * beaconPulse,
                                    style = Stroke(width = 1.5f)
                                )
                            }
                        }

                        if (!isUnlocked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = MutedGray,
                                modifier = Modifier.size(16.dp)
                            )
                        } else if (mission.isBossMission) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Boss",
                                tint = DangerRedBright,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "${mission.number}",
                                color = if (isSelected) Color.White else CyanHighlight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Star Rating for Node
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..3) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (i <= stars) CreditGold else Color(0xFF26343D),
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }

                // 2. Connecting Vector Line to Next Node
                if (index < missions.size - 1) {
                    val nextMission = missions[index + 1]
                    val isPathUnlocked = nextMission.number <= highestUnlocked

                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(2.dp)
                            .offset(y = (-6).dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawLine(
                                color = if (isPathUnlocked) CyanHighlight.copy(alpha = 0.8f) else TacticalBlue.copy(alpha = 0.25f),
                                start = Offset(0f, size.height / 2f),
                                end = Offset(size.width, size.height / 2f),
                                strokeWidth = if (isPathUnlocked) 2f else 1.5f,
                                pathEffect = if (!isPathUnlocked) PathEffect.dashPathEffect(floatArrayOf(6f, 4f)) else null
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// COMPONENT: REDESIGNED TACTICAL MISSION DOSSIER CARD
// =============================================================================

@Composable
private fun TacticalMissionDossierCard(
    mission: MissionInfo,
    isUnlocked: Boolean,
    starsEarned: Int,
    bestScore: Int
) {
    MilitaryPanel(
        modifier = Modifier.fillMaxWidth(),
        title = if (isUnlocked) "SORTIE BRIEFING DOSSIER" else "CLASSIFIED SECTOR",
        subTitle = "${mission.codename} • REGION: ${mission.theater.uppercase()}",
        shape = RoundedCornerShape(8.dp),
        headerRightContent = {
            // Mission Type Badge (From image_0.png icon sets)
            MissionTypePill(missionType = mission.missionType)
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // A. Title & Classification Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MISSION ${mission.number}: ${mission.title.uppercase()}",
                        color = if (mission.isBossMission) DangerRedBright else Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "THEATER OF OPERATIONS: ${mission.theater}",
                        color = CyanHighlight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Difficulty Stars (1 to 3)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "THREAT LEVEL",
                        color = MutedGray,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        for (i in 1..3) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (i <= mission.difficultyStars) {
                                    if (mission.isBossMission) DangerRedBright else WarmOrange
                                } else MutedGray.copy(alpha = 0.3f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            // B. Intelligence Briefing Paragraph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF152129))
                    .border(1.dp, TacticalBlue.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = if (isUnlocked) mission.briefing else "INTELLIGENCE REDACTED. Complete preceding theater sorties to decrypt satellite reconnaissance data.",
                    color = if (isUnlocked) Color(0xFFD3DFE6) else MutedGray,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }

            // C. Dynamic Battlefield Alert (If Present)
            if (mission.hasDynamicEvent && isUnlocked) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(WarmOrange.copy(alpha = 0.15f))
                        .border(1.dp, WarmOrange.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CrisisAlert,
                        contentDescription = null,
                        tint = WarmOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mission.eventDescription,
                        color = WarmOrange,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 14.sp
                    )
                }
            }

            // D. Sortie Objectives
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "SORTIE OBJECTIVES:",
                    color = TacticalBlue,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                // Primary
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = CyanHighlight,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PRIMARY: ",
                        color = CyanHighlight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = mission.primaryObjective,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Secondary
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = WarmOrange,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SECONDARY: ",
                        color = WarmOrange,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = mission.secondaryObjective,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // E. Performance Records & Credit Reward Bounty
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkSteelVariant)
                    .border(1.dp, TacticalBlue.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Best Record
                Column {
                    Text(
                        text = "BEST RECORD:",
                        color = MutedGray,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (bestScore > 0) "${NumberFormat.getNumberInstance(Locale.US).format(bestScore)} PTS" else "NO SORTIE DATA",
                        color = if (bestScore > 0) Color.White else MutedGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Stars rating
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SECTOR STARS:",
                        color = MutedGray,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        for (i in 1..3) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (i <= starsEarned) CreditGold else Color(0xFF334155),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Bounty Reward
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "SORTIE REWARD:",
                        color = MutedGray,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "+${NumberFormat.getNumberInstance(Locale.US).format(mission.rewardCredits)}",
                            color = CreditGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "CR",
                            color = CreditText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// COMPONENT: MISSION TYPE PILL (FROM IMAGE_0.PNG ICON SETS)
// =============================================================================

@Composable
fun MissionTypePill(
    missionType: MissionType,
    modifier: Modifier = Modifier
) {
    val (icon, tint) = when (missionType) {
        MissionType.AIR_SUPERIORITY -> Icons.Default.AirplanemodeActive to CyanHighlight
        MissionType.ESCORT -> Icons.Default.Security to HealthGreen
        MissionType.RECON -> Icons.Default.Radar to TacticalBlue
        MissionType.STRIKE -> Icons.Default.Rocket to WarmOrange
        MissionType.RESCUE -> Icons.Default.LocalHospital to CreditGold
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(tint.copy(alpha = 0.18f))
            .border(1.dp, tint.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = missionType.displayName.uppercase(),
            color = tint,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
        )
    }
}
