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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.MissionDatabase
import com.example.ui.components.MilitaryPanel
import com.example.ui.components.NotificationSeverity
import com.example.ui.components.TacticalButton
import com.example.ui.components.TacticalButtonType
import com.example.ui.components.TacticalNotification
import com.example.ui.theme.CreditGold
import com.example.ui.theme.CreditGoldDark
import com.example.ui.theme.CreditText
import com.example.ui.theme.CyanHighlight
import com.example.ui.theme.DarkSteel
import com.example.ui.theme.DarkSteelVariant
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.MilitaryGreenBorder
import com.example.ui.theme.MilitaryGreenBright
import com.example.ui.theme.MutedGray
import com.example.ui.theme.SecondarySteel
import com.example.ui.theme.SecondarySteelBorder
import com.example.ui.theme.TacticalBlue
import com.example.ui.theme.WarmOrange
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MainMenuScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val missionProgress by viewModel.missionProgress.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    var showDailyMissionDialog by remember { mutableStateOf(false) }
    var showBonusClaimNotification by remember { mutableStateOf(false) }

    // Hover animation for jet showcase
    val infiniteTransition = rememberInfiniteTransition(label = "jet_hover_transition")
    val hoverOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "jet_hover_offset"
    )

    val creditsFormatted = remember(profile?.credits) {
        NumberFormat.getNumberInstance(Locale.US).format(profile?.credits ?: 2500)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepNavy, DarkSteelVariant, DeepNavy)
                )
            )
    ) {
        // Tactical background grid overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 48.dp.toPx()
            val gridColor = TacticalBlue.copy(alpha = 0.05f)
            var x = 0f
            while (x < size.width) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), 1f)
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1f)
                y += step
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            // =================================================================
            // 1. TOP HEADER BAR: TITLE & PILOT COMMAND STATUS
            // =================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.HANGAR) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSteel)
                            .border(1.dp, TacticalBlue.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Tactical Menu",
                            tint = CyanHighlight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "MODERN SKIES",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "AERIAL COMBAT COMMAND",
                            color = CyanHighlight,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    }
                }

                // Profile Badge & Credits Counter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSteel)
                        .border(1.dp, TacticalBlue.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    // Pilot Rank Avatar
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(TacticalBlue.copy(alpha = 0.3f))
                            .border(1.dp, CyanHighlight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rank",
                            tint = CreditGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "RANK 7",
                            color = CyanHighlight,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CREDITS ",
                                color = MutedGray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = creditsFormatted,
                                color = CreditGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "CR",
                                color = CreditText,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Notification Banner if credits claimed
            AnimatedVisibility(
                visible = showBonusClaimNotification,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TacticalNotification(
                    title = "SUPPLY DROP CONFIRMED",
                    message = "+1,000 Tactical Combat Credits credited to fleet reserves.",
                    severity = NotificationSeverity.SUCCESS,
                    tag = "CREDITED",
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // =================================================================
            // 2. PLAYER AIRCRAFT VISUALIZATION SHOWCASE
            // =================================================================
            MilitaryPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp),
                title = "CURRENT JET: ${profile?.selectedAircraft?.replace('_', ' ') ?: "ARC-WASP"}",
                subTitle = "STAGE II AIR SUPERIORITY INTERCEPTOR",
                headerRightContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(WarmOrange.copy(alpha = 0.2f))
                            .border(1.dp, WarmOrange, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LVL 5",
                            color = WarmOrange,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF1B262F), DeepNavy)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Ground Runway Runway Lines
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val midX = size.width / 2f
                        val midY = size.height / 2f

                        // Radar range rings
                        drawCircle(
                            color = TacticalBlue.copy(alpha = 0.12f),
                            radius = 65.dp.toPx(),
                            center = Offset(midX, midY)
                        )
                        drawCircle(
                            color = TacticalBlue.copy(alpha = 0.08f),
                            radius = 95.dp.toPx(),
                            center = Offset(midX, midY)
                        )

                        // Ground shadow under jet
                        drawOval(
                            color = Color.Black.copy(alpha = 0.45f),
                            topLeft = Offset(midX - 55.dp.toPx(), midY + 38.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(110.dp.toPx(), 22.dp.toPx())
                        )
                    }

                    // Fighter Jet Sprite with hover animation
                    Image(
                        painter = painterResource(id = R.drawable.player_fighter_jet),
                        contentDescription = "Player Aircraft",
                        modifier = Modifier
                            .size(175.dp)
                            .offset(y = hoverOffset.dp)
                            .testTag("player_jet_showcase"),
                        contentScale = ContentScale.Fit
                    )

                    // Hardpoint Callout Tags (from UI Theme Guide)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "◄ CANNON: DUAL 20MM",
                            color = MutedGray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "MISSILES: 4x AIM-9 ►",
                            color = MutedGray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =================================================================
            // 3. TACTICAL NAVIGATION BUTTON STACK
            // =================================================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. PLAY CAMPAIGN (PRIMARY - Military Green with Edge Highlight, STRONGEST ELEMENT)
                TacticalButton(
                    onClick = {
                        viewModel.soundManager.playButtonClick()
                        viewModel.navigateTo(AppScreen.MISSION_SELECT)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    type = TacticalButtonType.PRIMARY,
                    testTag = "play_campaign_button",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = MilitaryGreenBright,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MilitaryGreenBorder,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "PLAY CAMPAIGN",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.8.sp
                        )
                        Text(
                            text = "AIR DEFENSE SORTIES 1-10",
                            color = Color(0xFFA7F3D0),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                // 2. SURVIVAL MODE (SECONDARY)
                TacticalButton(
                    onClick = {
                        viewModel.soundManager.playButtonClick()
                        viewModel.startSurvivalMode()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    type = TacticalButtonType.SECONDARY,
                    testTag = "survival_mode_button",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = WarmOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                ) {
                    Text(
                        text = "SURVIVAL MODE",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }

                // 3. GARAGE & UPGRADES (SECONDARY)
                TacticalButton(
                    onClick = {
                        viewModel.soundManager.playButtonClick()
                        viewModel.navigateTo(AppScreen.HANGAR)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    type = TacticalButtonType.SECONDARY,
                    testTag = "garage_upgrades_button",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = CyanHighlight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                ) {
                    Text(
                        text = "GARAGE & UPGRADES",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }

                // 4. BOSS RUSH (SECONDARY - DANGER/ACCENT)
                TacticalButton(
                    onClick = {
                        viewModel.soundManager.playButtonClick()
                        viewModel.startBossRush()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    type = TacticalButtonType.SECONDARY,
                    testTag = "boss_rush_button",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = CreditGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                ) {
                    Text(
                        text = "BOSS RUSH: GOLIATH FORTRESS",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                }

                // 5. DAILY MISSIONS (SECONDARY)
                TacticalButton(
                    onClick = {
                        viewModel.soundManager.playButtonClick()
                        showDailyMissionDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    type = TacticalButtonType.SECONDARY,
                    testTag = "daily_missions_button",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = CyanHighlight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                ) {
                    Text(
                        text = "DAILY MISSIONS",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // =================================================================
            // 4. CAMPAIGN PROGRESS DRAWER (MISSIONS 1-10)
            // =================================================================
            val highestUnlocked = profile?.highestUnlockedMission ?: 1
            MilitaryPanel(
                modifier = Modifier.fillMaxWidth(),
                title = "CAMPAIGN PROGRESS:",
                subTitle = "MISSIONS 1-10 (SECTORS ALPHA - OMEGA)"
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(MissionDatabase.missions) { mission ->
                        val isUnlocked = mission.number <= highestUnlocked
                        val prog = missionProgress.find { it.missionNumber == mission.number }
                        val stars = prog?.stars ?: 0

                        Box(
                            modifier = Modifier
                                .width(94.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isUnlocked) SecondarySteel else DarkSteelVariant)
                                .border(
                                    1.dp,
                                    if (isUnlocked) CyanHighlight.copy(alpha = 0.6f) else TacticalBlue.copy(alpha = 0.2f),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable(enabled = isUnlocked) {
                                    viewModel.soundManager.playButtonClick()
                                    viewModel.selectMission(mission.number)
                                    viewModel.navigateTo(AppScreen.MISSION_SELECT)
                                }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "M-${mission.number}",
                                    color = if (isUnlocked) CyanHighlight else MutedGray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = mission.theater.take(6).uppercase(),
                                    color = MutedGray,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                // 3 Star Rating
                                Row {
                                    for (i in 1..3) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (i <= stars) CreditGold else Color.DarkGray,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CREDIT REWARDS:",
                        color = MutedGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "1,000 - 3,500 CR PER SORTIE",
                        color = CreditGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =================================================================
            // 5. GET CREDITS / MONETIZATION REQUISITION PANEL
            // =================================================================
            MilitaryPanel(
                modifier = Modifier.fillMaxWidth(),
                title = "GET CREDITS",
                subTitle = "TACTICAL REQUISITION & DEFENSE GRANTS",
                cornerHighlightColor = CreditGold
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E271B))
                        .border(1.dp, MilitaryGreenBorder.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "EMERGENCY SUPPLY DROP",
                            color = MilitaryGreenBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Claim daily +1,000 Combat Credits to finance jet upgrades.",
                            color = MutedGray,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    TacticalButton(
                        onClick = {
                            viewModel.claimEmergencyCredits(1000)
                            showBonusClaimNotification = true
                        },
                        type = TacticalButtonType.GOLD,
                        modifier = Modifier.height(38.dp),
                        testTag = "claim_credits_button"
                    ) {
                        Text(
                            text = "+1,000 CR",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    // DAILY MISSIONS MODAL DIALOG
    // =========================================================================
    if (showDailyMissionDialog) {
        AlertDialog(
            onDismissRequest = { showDailyMissionDialog = false },
            confirmButton = {
                TacticalButton(
                    onClick = {
                        showDailyMissionDialog = false
                        viewModel.startSortie(3)
                    },
                    type = TacticalButtonType.PRIMARY,
                    modifier = Modifier.height(42.dp)
                ) {
                    Text(
                        text = "ACCEPT & SCRAMBLE",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TacticalButton(
                    onClick = { showDailyMissionDialog = false },
                    type = TacticalButtonType.SECONDARY,
                    modifier = Modifier.height(42.dp)
                ) {
                    Text(
                        text = "STAND DOWN",
                        color = MutedGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            title = {
                Text(
                    text = "DAILY TACTICAL BRIEFING",
                    color = CyanHighlight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "OPERATION: DESERT VIPER",
                        color = WarmOrange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Enemy heavy bombers and radar installations detected in Sector 3. Intercept strike force before they penetrate frontline airspace.",
                        color = MutedGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkSteelVariant)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "BOUNTY REWARD:", color = MutedGray, fontSize = 11.sp)
                        Text(text = "+2,500 CR", color = CreditGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            containerColor = DarkSteel,
            shape = RoundedCornerShape(8.dp)
        )
    }
}
