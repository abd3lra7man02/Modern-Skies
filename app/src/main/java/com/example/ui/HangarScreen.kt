package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.model.AircraftId
import com.example.model.PrimaryWeaponId
import com.example.model.SecondaryWeaponId
import com.example.model.SpecialWeaponId
import com.example.model.WingmanId
import com.example.ui.components.MilitaryPanel
import com.example.ui.components.NotificationSeverity
import com.example.ui.components.StatBar
import com.example.ui.components.TacticalButton
import com.example.ui.components.TacticalButtonType
import com.example.ui.components.TacticalNotification
import com.example.ui.theme.CreditGold
import com.example.ui.theme.CreditGoldDark
import com.example.ui.theme.CreditText
import com.example.ui.theme.CyanHighlight
import com.example.ui.theme.DangerRedBright
import com.example.ui.theme.DarkSteel
import com.example.ui.theme.DarkSteelVariant
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.HealthGreen
import com.example.ui.theme.MilitaryGreen
import com.example.ui.theme.MilitaryGreenBorder
import com.example.ui.theme.MilitaryGreenBright
import com.example.ui.theme.MutedGray
import com.example.ui.theme.SecondarySteel
import com.example.ui.theme.SecondarySteelBorder
import com.example.ui.theme.TacticalBlue
import com.example.ui.theme.WarmOrange
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HangarScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val allAircraft by viewModel.allAircraft.collectAsStateWithLifecycle()
    val allUpgrades by viewModel.allUpgrades.collectAsStateWithLifecycle()

    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: WEAPONS, 1: ARMOR, 2: ENGINES, 3: SPECIAL
    var showSettingsDialog by remember { mutableStateOf(false) }
    var recentUpgradedKey by remember { mutableStateOf<String?>(null) }

    val currentCredits = profile?.credits ?: 0
    val selectedAircraftId = AircraftId.fromString(profile?.selectedAircraft ?: "FA22_PHANTOM")
    val allAircraftEntries = AircraftId.entries
    var currentAircraftIndex by remember {
        mutableIntStateOf(allAircraftEntries.indexOf(selectedAircraftId).coerceAtLeast(0))
    }
    val inspectedAircraft = allAircraftEntries[currentAircraftIndex.coerceIn(0, allAircraftEntries.size - 1)]

    val isCurrentInspectedEquipped = inspectedAircraft.name == (profile?.selectedAircraft ?: "FA22_PHANTOM")
    val isCurrentInspectedUnlocked = inspectedAircraft == AircraftId.FA22_PHANTOM ||
            allAircraft.any { it.aircraftId == inspectedAircraft.name && it.isUnlocked }

    val upgradesMap = remember(allUpgrades) { allUpgrades.associate { it.upgradeKey to it.level } }

    // Overall fleet tactical level calculation
    val overallLevel = remember(upgradesMap) {
        val totalLevels = upgradesMap.values.sum()
        (totalLevels / (upgradesMap.size.coerceAtLeast(1))).coerceAtLeast(1)
    }

    // Credits formatted
    val creditsFormatted = remember(currentCredits) {
        NumberFormat.getNumberInstance(Locale.US).format(currentCredits)
    }

    // Reset recent upgraded notification
    LaunchedEffect(recentUpgradedKey) {
        if (recentUpgradedKey != null) {
            delay(2500)
            recentUpgradedKey = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepNavy, DarkSteelVariant, DeepNavy)
                )
            )
            .statusBarsPadding()
    ) {
        // =====================================================================
        // 1. TOP TACTICAL HEADER (MATCHING REFERENCE GUIDE)
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
                        .testTag("hangar_back_button")
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
                        text = "THE GARAGE",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.8.sp
                    )
                    Text(
                        text = "${inspectedAircraft.displayName.uppercase()} • LVL $overallLevel",
                        color = CyanHighlight,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Credits Pill with Coin Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSteel)
                        .border(1.dp, CreditGold.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = CreditGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = creditsFormatted,
                        color = CreditGold,
                        fontSize = 12.sp,
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

                // Settings Trigger
                IconButton(
                    onClick = {
                        viewModel.soundManager.playButtonClick()
                        showSettingsDialog = true
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSteel)
                        .border(1.dp, TacticalBlue.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .testTag("hangar_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MutedGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // =====================================================================
        // 2. SUB-TABS: WEAPONS, ARMOR, ENGINES, SPECIAL (FROM IMAGE_0.PNG)
        // =====================================================================
        val subTabs = listOf("WEAPONS", "ARMOR", "ENGINES", "SPECIAL")
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = DarkSteel,
            contentColor = CyanHighlight,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                    color = CyanHighlight,
                    height = 2.5.dp
                )
            }
        ) {
            subTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = {
                        viewModel.soundManager.playButtonClick()
                        selectedSubTab = index
                    },
                    text = {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (selectedSubTab == index) FontWeight.Black else FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = if (selectedSubTab == index) CyanHighlight else MutedGray
                        )
                    }
                )
            }
        }

        // =====================================================================
        // 3. CENTRAL AIRCRAFT MAINTENANCE SCHEMATIC WITH VISUAL PROGRESSION
        // =====================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            AircraftSchematicBay(
                aircraft = inspectedAircraft,
                overallLevel = overallLevel,
                isEquipped = isCurrentInspectedEquipped,
                isUnlocked = isCurrentInspectedUnlocked,
                onPreviousJet = {
                    viewModel.soundManager.playButtonClick()
                    if (currentAircraftIndex > 0) currentAircraftIndex--
                },
                onNextJet = {
                    viewModel.soundManager.playButtonClick()
                    if (currentAircraftIndex < allAircraftEntries.size - 1) currentAircraftIndex++
                },
                onEquipOrUnlock = {
                    viewModel.soundManager.playButtonClick()
                    if (!isCurrentInspectedUnlocked) {
                        viewModel.unlockAircraft(inspectedAircraft)
                    } else {
                        viewModel.selectAircraft(inspectedAircraft)
                    }
                },
                cannonLevel = upgradesMap["CANNON_DMG"] ?: 1,
                missileLevel = upgradesMap["MISSILE_CAP"] ?: 1
            )
        }

        // =====================================================================
        // 4. SATISFYING UPGRADE NOTIFICATION POPUP
        // =====================================================================
        AnimatedVisibility(
            visible = recentUpgradedKey != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            TacticalNotification(
                title = "SYSTEM UPGRADE INSTALLED",
                message = "${recentUpgradedKey?.replace('_', ' ')} upgraded to next performance tier.",
                severity = NotificationSeverity.SUCCESS,
                tag = "MK UPGRADE",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // =====================================================================
        // 5. MAIN CONTENT SCROLLER: UPGRADE CARDS & HORIZONTAL STAT BARS
        // =====================================================================
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // A. STATS OVERVIEW SECTION (Visual Horizontal Tactical Bars)
            item {
                MilitaryPanel(
                    modifier = Modifier.fillMaxWidth(),
                    title = "AIRCRAFT SPECIFICATIONS",
                    subTitle = "DYNAMIC TELEMETRY & MULTIPLIERS",
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val cannonDmgBonus = ((upgradesMap["CANNON_DMG"] ?: 1) - 1) * 0.15f
                        val armorBonus = ((upgradesMap["ARMOR_HP"] ?: 1) - 1) * 0.15f
                        val shieldBonus = ((upgradesMap["SHIELD_CAP"] ?: 1) - 1) * 0.20f
                        val missileBonus = (upgradesMap["MISSILE_CAP"] ?: 1) * 2

                        StatBar(
                            label = "ARMOR / HULL INTEGRITY",
                            value = ((inspectedAircraft.baseHealth * (1f + armorBonus)) / 350f).coerceIn(0f, 1f),
                            valueText = "${(inspectedAircraft.baseHealth * (1f + armorBonus)).toInt()} HP",
                            barColor = HealthGreen
                        )
                        StatBar(
                            label = "CANNON DAMAGE",
                            value = ((inspectedAircraft.damageMultiplier * (1f + cannonDmgBonus)) / 2.2f).coerceIn(0f, 1f),
                            valueText = "${((1f + cannonDmgBonus) * 100).toInt()}% DPS",
                            barColor = WarmOrange
                        )
                        StatBar(
                            label = "MISSILE CAPACITY",
                            value = ((8 + missileBonus) / 24f).coerceIn(0f, 1f),
                            valueText = "${8 + missileBonus} ROCKETS",
                            barColor = CreditGold
                        )
                        StatBar(
                            label = "SPEED",
                            value = (inspectedAircraft.speedMultiplier / 1.5f).coerceIn(0f, 1f),
                            valueText = "MACH ${(inspectedAircraft.speedMultiplier * 2.1f).format(1)}",
                            barColor = CyanHighlight
                        )
                        StatBar(
                            label = "MANEUVERABILITY",
                            value = ((inspectedAircraft.speedMultiplier * 1.1f) / 1.6f).coerceIn(0f, 1f),
                            valueText = "${(inspectedAircraft.speedMultiplier * 8.5f).format(1)} G",
                            barColor = TacticalBlue
                        )
                        StatBar(
                            label = "DEFLECTOR SHIELD",
                            value = ((inspectedAircraft.baseShield * (1f + shieldBonus)) / 150f).coerceIn(0f, 1f),
                            valueText = "${(inspectedAircraft.baseShield * (1f + shieldBonus)).toInt()} SHIELD",
                            barColor = CyanHighlight
                        )
                    }
                }
            }

            // B. UPGRADE CARDS (FILTERED BY SUB-TAB)
            val upgradeCardDefinitions = getUpgradesForTab(selectedSubTab)
            items(upgradeCardDefinitions) { def ->
                val currentLevel = upgradesMap[def.key] ?: 1
                val cost = currentLevel * def.baseCostMultiplier
                val canAfford = currentCredits >= cost

                val currentValueStr = def.valueCalculation(currentLevel)
                val nextValueStr = def.valueCalculation(currentLevel + 1)

                MilitaryPanel(
                    modifier = Modifier.fillMaxWidth(),
                    title = def.title,
                    subTitle = def.description,
                    headerRightContent = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyanHighlight.copy(alpha = 0.2f))
                                .border(1.dp, CyanHighlight, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LVL $currentLevel",
                                color = CyanHighlight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "CURRENT: ",
                                    color = MutedGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = currentValueStr,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = " ➔ ",
                                    color = CyanHighlight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = nextValueStr,
                                    color = CyanHighlight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "COST: ",
                                    color = MutedGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = NumberFormat.getNumberInstance(Locale.US).format(cost),
                                    color = CreditGold,
                                    fontSize = 11.sp,
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

                        Spacer(modifier = Modifier.width(12.dp))

                        TacticalButton(
                            onClick = {
                                viewModel.soundManager.playButtonClick()
                                viewModel.purchaseUpgrade(def.key, cost)
                                recentUpgradedKey = def.title
                            },
                            enabled = canAfford,
                            type = if (canAfford) TacticalButtonType.PRIMARY else TacticalButtonType.SECONDARY,
                            modifier = Modifier
                                .height(40.dp)
                                .width(120.dp),
                            testTag = "upgrade_button_${def.key}"
                        ) {
                            Text(
                                text = if (canAfford) "UPGRADE" else "LOCKED",
                                color = if (canAfford) Color.White else MutedGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }
            }

            // Extra space at bottom for comfortable scrolling
            item {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // =====================================================================
        // 6. BOTTOM DEPLOYMENT ACTION BANNER
        // =====================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSteel)
                .border(1.dp, TacticalBlue.copy(alpha = 0.4f))
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            TacticalButton(
                onClick = {
                    viewModel.soundManager.playButtonClick()
                    viewModel.navigateTo(AppScreen.MISSION_SELECT)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                type = TacticalButtonType.PRIMARY,
                testTag = "deploy_sortie_button",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Flight,
                        contentDescription = "Deploy",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MilitaryGreenBorder,
                        modifier = Modifier.size(18.dp)
                    )
                }
            ) {
                Text(
                    text = "PROCEED TO MISSION BRIEFING",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.4.sp
                )
            }
        }
    }

    // =========================================================================
    // SETTINGS MODAL BOTTOM SHEET
    // =========================================================================
    if (showSettingsDialog) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsDialog = false },
            containerColor = DarkSteel
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "AERIAL FLIGHT SETTINGS",
                    color = CyanHighlight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )

                // SFX Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sound Effects & Audio", color = Color.White, fontSize = 13.sp)
                    TacticalButton(
                        onClick = { viewModel.toggleSfx(!(profile?.sfxEnabled ?: true)) },
                        type = if (profile?.sfxEnabled != false) TacticalButtonType.PRIMARY else TacticalButtonType.SECONDARY,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(if (profile?.sfxEnabled != false) "ENABLED" else "MUTED", fontSize = 11.sp)
                    }
                }

                // Vibration Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tactical Haptic Vibration", color = Color.White, fontSize = 13.sp)
                    TacticalButton(
                        onClick = { viewModel.toggleVibration(!(profile?.vibrationEnabled ?: true)) },
                        type = if (profile?.vibrationEnabled != false) TacticalButtonType.PRIMARY else TacticalButtonType.SECONDARY,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(if (profile?.vibrationEnabled != false) "ENABLED" else "MUTED", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// =============================================================================
// COMPONENT: AIRCRAFT SCHEMATIC MAINTENANCE BAY (WITH VISUAL LEVEL PROGRESSION)
// =============================================================================

@Composable
private fun AircraftSchematicBay(
    aircraft: AircraftId,
    overallLevel: Int,
    isEquipped: Boolean,
    isUnlocked: Boolean,
    onPreviousJet: () -> Unit,
    onNextJet: () -> Unit,
    onEquipOrUnlock: () -> Unit,
    cannonLevel: Int,
    missileLevel: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hangar_jet_anim")
    val hoverY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hover_y"
    )
    val afterburnerPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "afterburner_pulse"
    )

    MilitaryPanel(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        title = aircraft.displayName,
        subTitle = aircraft.role.uppercase(),
        headerRightContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Equip/Unlock mini button
                if (isEquipped) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MilitaryGreen)
                            .border(1.dp, MilitaryGreenBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "EQUIPPED",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (!isUnlocked) {
                    TacticalButton(
                        onClick = onEquipOrUnlock,
                        type = TacticalButtonType.GOLD,
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "UNLOCK ${aircraft.unlockCost} CR",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    TacticalButton(
                        onClick = onEquipOrUnlock,
                        type = TacticalButtonType.SECONDARY,
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "EQUIP",
                            color = CyanHighlight,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(6.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1B2730), DeepNavy)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Background maintenance grid & radar rings
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f

                // Central maintenance circle
                drawCircle(
                    color = TacticalBlue.copy(alpha = 0.15f),
                    radius = 80.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.5f)
                )
                drawCircle(
                    color = TacticalBlue.copy(alpha = 0.08f),
                    radius = 110.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(width = 1f)
                )

                // Tactical Leader Lines (pointing to Cannon and Missiles as in Image_0.png)
                // Left leader line to cannon
                val leftLinePath = Path().apply {
                    moveTo(cx - 28.dp.toPx(), cy - 20.dp.toPx())
                    lineTo(cx - 70.dp.toPx(), cy - 35.dp.toPx())
                    lineTo(cx - 100.dp.toPx(), cy - 35.dp.toPx())
                }
                drawPath(leftLinePath, CyanHighlight.copy(alpha = 0.6f), style = Stroke(width = 1.5f))
                drawCircle(CyanHighlight, radius = 3.dp.toPx(), center = Offset(cx - 28.dp.toPx(), cy - 20.dp.toPx()))

                // Right leader line to missile hardpoint
                val rightLinePath = Path().apply {
                    moveTo(cx + 34.dp.toPx(), cy + 10.dp.toPx())
                    lineTo(cx + 70.dp.toPx(), cy - 5.dp.toPx())
                    lineTo(cx + 100.dp.toPx(), cy - 5.dp.toPx())
                }
                drawPath(rightLinePath, WarmOrange.copy(alpha = 0.6f), style = Stroke(width = 1.5f))
                drawCircle(WarmOrange, radius = 3.dp.toPx(), center = Offset(cx + 34.dp.toPx(), cy + 10.dp.toPx()))

                // Ground shadow under jet
                drawOval(
                    color = Color.Black.copy(alpha = 0.5f),
                    topLeft = Offset(cx - 50.dp.toPx(), cy + 42.dp.toPx()),
                    size = Size(100.dp.toPx(), 20.dp.toPx())
                )
            }

            // Aircraft Sprite with level-based visual progression
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .offset(y = hoverY.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.player_fighter_jet),
                    contentDescription = "Aircraft Blueprint",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // Visual progression overlay:
                // Level 3+: wing armor decals & cannon flash
                // Level 5+: wingtip missile pods and twin afterburner plumes
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f

                    // Afterburner exhaust glow (increases with overallLevel)
                    val burnerRadius = (6f + overallLevel * 1.5f) * afterburnerPulse
                    val burnerColor = if (overallLevel >= 5) WarmOrange else CyanHighlight
                    drawCircle(burnerColor, radius = burnerRadius, center = Offset(cx - 8.dp.toPx(), cy + 62.dp.toPx()))
                    drawCircle(burnerColor, radius = burnerRadius, center = Offset(cx + 8.dp.toPx(), cy + 62.dp.toPx()))
                    drawCircle(Color.White, radius = burnerRadius * 0.45f, center = Offset(cx - 8.dp.toPx(), cy + 62.dp.toPx()))
                    drawCircle(Color.White, radius = burnerRadius * 0.45f, center = Offset(cx + 8.dp.toPx(), cy + 62.dp.toPx()))

                    if (overallLevel >= 3) {
                        // Wing armor bracket markers
                        drawLine(
                            CyanHighlight.copy(alpha = 0.8f),
                            Offset(cx - 45.dp.toPx(), cy + 15.dp.toPx()),
                            Offset(cx - 30.dp.toPx(), cy + 15.dp.toPx()),
                            strokeWidth = 2f
                        )
                        drawLine(
                            CyanHighlight.copy(alpha = 0.8f),
                            Offset(cx + 45.dp.toPx(), cy + 15.dp.toPx()),
                            Offset(cx + 30.dp.toPx(), cy + 15.dp.toPx()),
                            strokeWidth = 2f
                        )
                    }

                    if (overallLevel >= 5) {
                        // Wingtip missile hardpoint lights
                        drawCircle(CreditGold, radius = 3.5f, center = Offset(cx - 56.dp.toPx(), cy + 24.dp.toPx()))
                        drawCircle(CreditGold, radius = 3.5f, center = Offset(cx + 56.dp.toPx(), cy + 24.dp.toPx()))
                    }
                }
            }

            // Left Leader Line Callout Tag
            Text(
                text = "CANNON DMG\nLVL $cannonLevel",
                color = CyanHighlight,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                lineHeight = 11.sp,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
            )

            // Right Leader Line Callout Tag
            Text(
                text = "MISSILE BAY\nLVL $missileLevel",
                color = WarmOrange,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                lineHeight = 11.sp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp),
                textAlign = TextAlign.End
            )

            // Jet Carousel Left Arrow
            IconButton(
                onClick = onPreviousJet,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-4).dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(DarkSteel.copy(alpha = 0.8f))
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Previous Aircraft",
                    tint = CyanHighlight
                )
            }

            // Jet Carousel Right Arrow
            IconButton(
                onClick = onNextJet,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 4.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(DarkSteel.copy(alpha = 0.8f))
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Next Aircraft",
                    tint = CyanHighlight
                )
            }
        }
    }
}

// =============================================================================
// UPGRADE CARD DEFINITIONS PER SUB-TAB
// =============================================================================

private data class UpgradeCardDefinition(
    val key: String,
    val title: String,
    val description: String,
    val baseCostMultiplier: Int,
    val valueCalculation: (level: Int) -> String
)

private fun getUpgradesForTab(tabIndex: Int): List<UpgradeCardDefinition> {
    return when (tabIndex) {
        0 -> listOf( // WEAPONS
            UpgradeCardDefinition(
                key = "CANNON_DMG",
                title = "CANNON FIREPOWER",
                description = "Increases 20mm kinetic shell caliber and armor penetration yield.",
                baseCostMultiplier = 850,
                valueCalculation = { lvl -> "${((1f + (lvl - 1) * 0.15f) * 100).toInt()}% DPS" }
            ),
            UpgradeCardDefinition(
                key = "CANNON_RATE",
                title = "CANNON CYCLE RATE",
                description = "Accelerates pneumatic cycling speed of the primary autocannon.",
                baseCostMultiplier = 950,
                valueCalculation = { lvl -> "${(100 - (lvl - 1) * 6).coerceAtLeast(40)}ms DELAY" }
            ),
            UpgradeCardDefinition(
                key = "MISSILE_DMG",
                title = "MISSILE WARHEAD",
                description = "Enhances blast radius and payload yield of AIM-9 homing missiles.",
                baseCostMultiplier = 1100,
                valueCalculation = { lvl -> "${75 + (lvl - 1) * 20} BLAST" }
            ),
            UpgradeCardDefinition(
                key = "MISSILE_CAP",
                title = "MISSILE CAPACITY",
                description = "Expands internal weapons bay with +2 additional ordnance racks per rank.",
                baseCostMultiplier = 1200,
                valueCalculation = { lvl -> "${8 + (lvl - 1) * 2} MISSILES" }
            )
        )
        1 -> listOf( // ARMOR
            UpgradeCardDefinition(
                key = "ARMOR_HP",
                title = "TITANIUM HULL PLATING",
                description = "Reinforces structural airframe against ballistic flak and collisions.",
                baseCostMultiplier = 1000,
                valueCalculation = { lvl -> "+${((lvl - 1) * 20)}% HP BUFFER" }
            ),
            UpgradeCardDefinition(
                key = "SHIELD_CAP",
                title = "DEFLECTOR GENERATOR",
                description = "Amplifies energy shield buffer and emergency recharge rate.",
                baseCostMultiplier = 1150,
                valueCalculation = { lvl -> "+${((lvl - 1) * 25)}% RECHARGE" }
            )
        )
        2 -> listOf( // ENGINES
            UpgradeCardDefinition(
                key = "CANNON_RATE",
                title = "AFTERBURNER TURBOFANS",
                description = "Optimizes twin turbofan thrust for supersonic evasive maneuvers.",
                baseCostMultiplier = 950,
                valueCalculation = { lvl -> "MACH ${(1.8f + lvl * 0.12f).format(2)}" }
            ),
            UpgradeCardDefinition(
                key = "SHIELD_CAP",
                title = "VECTORING NOZZLES",
                description = "Increases pitch and roll response for high-G evasion.",
                baseCostMultiplier = 1100,
                valueCalculation = { lvl -> "${(7.5f + lvl * 0.4f).format(1)} G TURN" }
            )
        )
        else -> listOf( // SPECIAL
            UpgradeCardDefinition(
                key = "MISSILE_CAP",
                title = "COUNTERMEASURE DECOYS",
                description = "Expands flare dispenser capacity to decoy incoming enemy SAMs.",
                baseCostMultiplier = 1300,
                valueCalculation = { lvl -> "${3 + lvl} CHARGES" }
            ),
            UpgradeCardDefinition(
                key = "MISSILE_DMG",
                title = "TACTICAL EMP BLAST",
                description = "Increases electromagnetic shockwave radius to disable enemy waves.",
                baseCostMultiplier = 1400,
                valueCalculation = { lvl -> "${150 + lvl * 25}m RADIUS" }
            )
        )
    }
}

private fun Float.format(digits: Int): String = String.format(Locale.US, "%.${digits}f", this)
