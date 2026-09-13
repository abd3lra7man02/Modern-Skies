package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.GameHudState
import com.example.model.MissionInfo
import com.example.ui.components.MilitaryPanel
import com.example.ui.components.TacticalButton
import com.example.ui.components.TacticalButtonType
import com.example.ui.theme.CreditGold
import com.example.ui.theme.CreditText
import com.example.ui.theme.CyanHighlight
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
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MissionResultsScreen(
    missionInfo: MissionInfo,
    hudState: GameHudState,
    onContinue: (totalCredits: Int) -> Unit,
    onUpgradeAircraft: (totalCredits: Int) -> Unit,
    soundManager: com.example.audio.SoundManager,
    modifier: Modifier = Modifier
) {
    // -------------------------------------------------------------------------
    // Sequential Animation Stages
    // 0: Header & Mission Accomplished
    // 1: Checklist & Objectives
    // 2: Combat Performance Telemetry
    // 3: Rating Stars Awarded
    // 4: Financial Rewards Breakdown
    // 5: Credit Counter Count-Up
    // 6: Action Buttons Ready
    // -------------------------------------------------------------------------
    var animStage by remember { mutableIntStateOf(0) }

    val baseReward = missionInfo.rewardCredits
    val combatBonus = hudState.creditsEarned
    val objectiveBonus = 500
    val starBonus = when {
        hudState.starsAwarded >= 3 -> 1000
        hudState.starsAwarded == 2 -> 500
        else -> 200
    }
    val totalCreditsSecured = baseReward + combatBonus + objectiveBonus + starBonus

    // Animated Credit Counter
    val animatedCredits by animateIntAsState(
        targetValue = if (animStage >= 5) totalCreditsSecured else 0,
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "credits_count_up"
    )

    // Star scales for bouncy pop-in
    val star1Scale = remember { Animatable(0f) }
    val star2Scale = remember { Animatable(0f) }
    val star3Scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        soundManager.playPowerup()
        delay(350)
        animStage = 1 // Checklist
        soundManager.playButtonClick()

        delay(400)
        animStage = 2 // Performance
        soundManager.playButtonClick()

        delay(450)
        animStage = 3 // Stars
        if (hudState.starsAwarded >= 1) {
            star1Scale.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 400f))
            soundManager.playPowerup()
        }
        if (hudState.starsAwarded >= 2) {
            delay(200)
            star2Scale.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 400f))
            soundManager.playPowerup()
        }
        if (hudState.starsAwarded >= 3) {
            delay(200)
            star3Scale.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 400f))
            soundManager.playPowerup()
        }

        delay(450)
        animStage = 4 // Rewards breakdown
        soundManager.playButtonClick()

        delay(350)
        animStage = 5 // Count up credits
        soundManager.playPowerup()

        delay(1200)
        animStage = 6 // Action buttons active
    }

    val infiniteTransition = rememberInfiniteTransition(label = "results_glow")
    val bannerGlow by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "banner_glow"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF080D14),
                        Color(0xFF0F1B25),
                        Color(0xFF080D14)
                    )
                )
            )
            .statusBarsPadding()
            .testTag("results_debrief_panel")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // =================================================================
            // STAGE 0: HEADER & VICTORY BANNER
            // =================================================================
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CreditGold.copy(alpha = 0.15f * bannerGlow))
                            .border(2.dp, CreditGold.copy(alpha = bannerGlow), CircleShape)
                            .padding(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = "Mission Accomplished",
                            tint = CreditGold,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "MISSION ACCOMPLISHED",
                        color = CreditGold,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "${missionInfo.codename} • SORTIE DEBRIEFING",
                        color = CyanHighlight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    Text(
                        text = "THEATER: ${missionInfo.theater.uppercase()}",
                        color = MutedGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // =================================================================
            // STAGE 1: CHECKLIST & OBJECTIVES
            // =================================================================
            item {
                AnimatedVisibility(
                    visible = animStage >= 1,
                    enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { 40 })
                ) {
                    MilitaryPanel(
                        title = "SORTIE OBJECTIVE CHECKLIST",
                        subTitle = "RECONNAISSANCE STATUS VERIFIED",
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Primary Objective
                            ChecklistItem(
                                title = "PRIMARY OBJECTIVE",
                                detail = missionInfo.primaryObjective,
                                isAchieved = true,
                                tint = HealthGreen
                            )

                            // Secondary Objective
                            ChecklistItem(
                                title = "SECONDARY OBJECTIVE",
                                detail = missionInfo.secondaryObjective,
                                isAchieved = hudState.starsAwarded >= 2,
                                tint = if (hudState.starsAwarded >= 2) HealthGreen else WarmOrange
                            )

                            // Airframe Recovery
                            ChecklistItem(
                                title = "AIRFRAME RECOVERY",
                                detail = "Fighter jet recovered intact with zero catastrophic damage",
                                isAchieved = true,
                                tint = CyanHighlight
                            )
                        }
                    }
                }
            }

            // =================================================================
            // STAGE 2: COMBAT PERFORMANCE TELEMETRY
            // =================================================================
            item {
                AnimatedVisibility(
                    visible = animStage >= 2,
                    enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { 40 })
                ) {
                    val minutes = (hudState.missionTimeSec / 60).toInt()
                    val seconds = (hudState.missionTimeSec % 60).toInt()
                    val timeFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)
                    val damagePct = ((hudState.damageTaken / 3.5f).toInt()).coerceIn(0, 100)

                    MilitaryPanel(
                        title = "COMBAT PERFORMANCE TELEMETRY",
                        subTitle = "TACTICAL ENGAGEMENT METRICS",
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                PerformanceMetricItem(
                                    label = "AIR TARGETS",
                                    value = "${hudState.airTargetsDestroyed}",
                                    icon = Icons.Default.AirplanemodeActive,
                                    tint = CyanHighlight,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                PerformanceMetricItem(
                                    label = "GROUND TARGETS",
                                    value = "${hudState.groundTargetsDestroyed}",
                                    icon = Icons.Default.CrisisAlert,
                                    tint = WarmOrange,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                PerformanceMetricItem(
                                    label = "DAMAGE TAKEN",
                                    value = if (damagePct == 0) "0% (CLEAN)" else "$damagePct%",
                                    icon = Icons.Default.Shield,
                                    tint = if (damagePct < 30) HealthGreen else WarmOrange,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                PerformanceMetricItem(
                                    label = "SORTIE TIME",
                                    value = timeFormatted,
                                    icon = Icons.Default.Timer,
                                    tint = TacticalBlue,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Total Combat Score Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkSteelVariant)
                                    .border(1.dp, TacticalBlue.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "COMBAT MERIT SCORE",
                                    color = TacticalBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${NumberFormat.getNumberInstance(Locale.US).format(hudState.score)} PTS",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // =================================================================
            // STAGE 3: MISSION RATING (STARS ★★★)
            // =================================================================
            item {
                AnimatedVisibility(
                    visible = animStage >= 3,
                    enter = fadeIn(tween(400)) + scaleIn(initialScale = 0.8f)
                ) {
                    MilitaryPanel(
                        title = "MISSION RATING & MASTERY",
                        subTitle = when (hudState.starsAwarded) {
                            3 -> "EXEMPLARY / ACE PERFORMANCE"
                            2 -> "SUPERIOR / TACTICAL SUCCESS"
                            else -> "SORTIE COMPLETED"
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Star 1
                                Box(modifier = Modifier.scale(star1Scale.value)) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Star 1",
                                        tint = if (hudState.starsAwarded >= 1) CreditGold else Color(0xFF26343D),
                                        modifier = Modifier.size(42.dp)
                                    )
                                }
                                // Star 2
                                Box(modifier = Modifier.scale(star2Scale.value)) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Star 2",
                                        tint = if (hudState.starsAwarded >= 2) CreditGold else Color(0xFF26343D),
                                        modifier = Modifier.size(52.dp)
                                    )
                                }
                                // Star 3
                                Box(modifier = Modifier.scale(star3Scale.value)) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Star 3",
                                        tint = if (hudState.starsAwarded >= 3) CreditGold else Color(0xFF26343D),
                                        modifier = Modifier.size(42.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = when (hudState.starsAwarded) {
                                    3 -> "PERFECT OPERATION • ALL OBJECTIVES SECURED"
                                    2 -> "COMMENDED • SECONDARY OBJECTIVE MET"
                                    else -> "QUALIFIED • PRIMARY SECTOR DEFENSE SECURED"
                                },
                                color = if (hudState.starsAwarded >= 3) CreditGold else CyanHighlight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            // =================================================================
            // STAGE 4: REWARDS BREAKDOWN
            // =================================================================
            item {
                AnimatedVisibility(
                    visible = animStage >= 4,
                    enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { 40 })
                ) {
                    MilitaryPanel(
                        title = "REQUISITION & BOUNTY BREAKDOWN",
                        subTitle = "FINANCIAL ALLOCATION LEDGER",
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            RewardBreakdownRow(
                                title = "BASE SORTIE PAY",
                                credits = baseReward,
                                tint = Color.White
                            )
                            RewardBreakdownRow(
                                title = "COMBAT ELIMINATION BONUS",
                                credits = combatBonus,
                                tint = CyanHighlight
                            )
                            RewardBreakdownRow(
                                title = "OBJECTIVE SECURED BONUS",
                                credits = objectiveBonus,
                                tint = HealthGreen
                            )
                            if (hudState.starsAwarded >= 2) {
                                RewardBreakdownRow(
                                    title = if (hudState.starsAwarded >= 3) "3-STAR FLAWLESS BONUS" else "2-STAR MASTERY BONUS",
                                    credits = starBonus,
                                    tint = CreditGold
                                )
                            }
                        }
                    }
                }
            }

            // =================================================================
            // STAGE 5: TOTAL CREDITS EARNED (ANIMATED COUNT-UP)
            // =================================================================
            item {
                AnimatedVisibility(
                    visible = animStage >= 4,
                    enter = fadeIn(tween(400)) + scaleIn(initialScale = 0.9f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        DarkSteel,
                                        Color(0xFF282010),
                                        DarkSteel
                                    )
                                )
                            )
                            .border(
                                1.5.dp,
                                CreditGold.copy(alpha = if (animStage >= 5) bannerGlow else 0.4f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL SORTIE BOUNTY",
                                    color = MutedGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "CREDITS DEPOSITED TO TREASURY",
                                    color = CreditText,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = CreditGold,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "+${NumberFormat.getNumberInstance(Locale.US).format(animatedCredits)}",
                                    color = CreditGold,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "CR",
                                    color = CreditText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // STAGE 6: ACTION BUTTONS (BOTTOM FIXED BAR)
        // =====================================================================
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
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
                // Secondary: Jump to Hangar to spend new credits immediately!
                TacticalButton(
                    onClick = {
                        soundManager.playButtonClick()
                        onUpgradeAircraft(totalCreditsSecured)
                    },
                    modifier = Modifier
                        .weight(0.45f)
                        .height(52.dp),
                    type = TacticalButtonType.SECONDARY,
                    testTag = "results_upgrade_button",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Upgrade Aircraft",
                            tint = CyanHighlight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                ) {
                    Text(
                        text = "UPGRADE AIRCRAFT",
                        color = CyanHighlight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                // Primary: Continue / Advance to Campaign
                TacticalButton(
                    onClick = {
                        soundManager.playButtonClick()
                        onContinue(totalCreditsSecured)
                    },
                    modifier = Modifier
                        .weight(0.55f)
                        .height(52.dp),
                    type = TacticalButtonType.PRIMARY,
                    testTag = "results_continue_button",
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Continue",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                ) {
                    Text(
                        text = "CONTINUE",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.4.sp
                    )
                }
            }
        }
    }
}

// =============================================================================
// HELPER COMPOSABLES FOR MISSION RESULTS
// =============================================================================

@Composable
private fun ChecklistItem(
    title: String,
    detail: String,
    isAchieved: Boolean,
    tint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(DarkSteelVariant)
            .border(1.dp, tint.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (isAchieved) tint.copy(alpha = 0.25f) else Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isAchieved) Icons.Default.Check else Icons.Default.Check,
                contentDescription = null,
                tint = if (isAchieved) tint else MutedGray,
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = tint,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.8.sp
            )
            Text(
                text = detail,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun PerformanceMetricItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(DarkSteelVariant)
            .border(1.dp, TacticalBlue.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(tint.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = label,
                color = MutedGray,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun RewardBreakdownRow(
    title: String,
    credits: Int,
    tint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(DarkSteelVariant)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = tint,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "+${NumberFormat.getNumberInstance(Locale.US).format(credits)}",
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
