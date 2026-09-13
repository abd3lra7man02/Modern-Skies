package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.GameHudState
import com.example.engine.MissileThreatLevel
import com.example.engine.PowerUpType
import com.example.model.PrimaryWeaponId
import com.example.model.SecondaryWeaponId
import com.example.model.SpecialWeaponId
import com.example.ui.theme.AfterburnerOrange
import com.example.ui.theme.CrimsonWarning
import com.example.ui.theme.TacticalAmber
import com.example.ui.theme.TacticalCyan
import kotlin.math.roundToInt

/**
 * Tactical Jet HUD (Heads-Up Display) Composable.
 * Displays flight telemetry (altitude, climb rate, airspeed, Mach number),
 * active weapon hardpoint statuses (primary cannon, secondary ordnance, special countermeasure),
 * target acquisition locking, and jet collimator reticle.
 * Uses Material 3 icons for high-contrast tactical feedback.
 */
@Composable
fun JetHUD(
    altitudeFeet: Int,
    speedKnots: Int,
    machNumber: Float = speedKnots / 661.47f,
    climbRateFpm: Int = 0,
    isAfterburner: Boolean = false,
    primaryWeapon: PrimaryWeaponId = PrimaryWeaponId.VULCAN_20MM,
    primaryAmmoStatus: String = "ARMED",
    secondaryWeapon: SecondaryWeaponId = SecondaryWeaponId.HEAT_SEEKING_AIM9,
    secondaryAmmo: Int = 8,
    maxSecondaryAmmo: Int = 8,
    specialWeapon: SpecialWeaponId = SpecialWeaponId.FLARE_DISPENSER,
    specialCooldownRatio: Float = 1.0f,
    isTargetLocked: Boolean = false,
    isMissileWarning: Boolean = false,
    modifier: Modifier = Modifier,
    showFlightReticle: Boolean = true,
    onSecondaryClick: (() -> Unit)? = null,
    onSpecialClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hud_pulse")
    val warningAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "warning_blink"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("jet_hud_container")
    ) {
        // Center Flight Boresight & Collimator Reticle
        if (showFlightReticle) {
            FlightBoresightReticle(
                isTargetLocked = isTargetLocked,
                isWarning = isMissileWarning,
                warningAlpha = warningAlpha,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("jet_hud_reticle")
            )
        }

        // Left Airspeed Flight Tape
        AirspeedTape(
            speedKnots = speedKnots,
            machNumber = machNumber,
            isAfterburner = isAfterburner,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp)
                .testTag("jet_hud_speed_tape")
        )

        // Right Altitude Flight Tape
        AltitudeTape(
            altitudeFeet = altitudeFeet,
            climbRateFpm = climbRateFpm,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
                .testTag("jet_hud_altitude_tape")
        )

        // Bottom Tactical Active Weapon Control Panel
        TacticalWeaponStatusPanel(
            primaryWeapon = primaryWeapon,
            primaryStatus = primaryAmmoStatus,
            secondaryWeapon = secondaryWeapon,
            secondaryAmmo = secondaryAmmo,
            maxSecondaryAmmo = maxSecondaryAmmo,
            specialWeapon = specialWeapon,
            specialCooldownRatio = specialCooldownRatio,
            isTargetLocked = isTargetLocked,
            onSecondaryClick = onSecondaryClick,
            onSpecialClick = onSpecialClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .testTag("jet_hud_weapon_panel")
        )
    }
}

/**
 * Overloaded JetHUD that takes a [GameHudState] directly and renders the full tactical cockpit UI.
 * Structure:
 * - Left Side: Player HP / Deflector Shield tactical vertical bars
 * - Right Side: Weapon Status (Missile ×08, Rocket ×04, Cannon armed)
 * - Bottom: Special Weapon bar (EMP / Flares / Strike) + Secondary Launch trigger
 * - Center: Flight reticle, Target acquisition brackets
 * - Mid-Top: Animated Missile Warning (Normal -> Warning -> Critical with bearing & distance),
 *   Active Power-up countdown widget, and Collection notification banner.
 */
@Composable
fun JetHUD(
    hudState: GameHudState,
    modifier: Modifier = Modifier,
    showFlightReticle: Boolean = true,
    onSecondaryClick: (() -> Unit)? = null,
    onSpecialClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hud_pulse")
    val warningAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "warning_blink"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("jet_hud_container")
    ) {
        // 1. Center Flight Boresight & Collimator Reticle
        if (showFlightReticle) {
            FlightBoresightReticle(
                isTargetLocked = hudState.isTargetLocked,
                isWarning = hudState.isMissileWarning,
                warningAlpha = warningAlpha,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("jet_hud_reticle")
            )
        }

        // 2. Mid-Top Center Alerts: Boss HP, Missile Threat, Power-Up Duration & Notifications
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Boss Health Bar (if active)
            if (hudState.bossHpRatio != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .testTag("boss_health_bar"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xDD7F1D1D)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "GOLIATH AIRBORNE FORTRESS CORE",
                            color = Color(0xFFFECACA),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        LinearProgressIndicator(
                            progress = { (hudState.bossHpRatio ?: 0f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFFEF4444),
                            trackColor = Color(0x88450A0A)
                        )
                    }
                }
            }

            // Animated Missile Warning System (Normal -> Warning -> Critical)
            if (hudState.missileThreatLevel != MissileThreatLevel.NONE) {
                val clockDir = when (((hudState.missileWarningAngleDeg + 22.5f) % 360f / 45f).toInt()) {
                    0 -> "06:00 AFT"
                    1 -> "07:30 BRG"
                    2 -> "09:00 PORT"
                    3 -> "10:30 BRG"
                    4 -> "12:00 NOSE"
                    5 -> "01:30 BRG"
                    6 -> "03:00 STBD"
                    7 -> "04:30 BRG"
                    else -> "06:00 AFT"
                }
                TacticalMissileWarningBanner(
                    threatLevel = hudState.missileThreatLevel,
                    distanceMeters = hudState.missileWarningDistanceMeters,
                    directionText = clockDir
                )
            }

            // Active Power-Up Duration Widget
            if (hudState.powerUpRemainingSec > 0f && hudState.activePowerUp != null) {
                TacticalActivePowerUpWidget(
                    activeType = hudState.activePowerUp,
                    remainingSec = hudState.powerUpRemainingSec
                )
            }

            // Power-Up Notification Popup
            val notificationMsg = if (hudState.powerUpNotificationTimer > 0f && hudState.lastCollectedPowerUp != null) {
                "${hudState.lastCollectedPowerUp.label} ACQUIRED"
            } else null
            if (notificationMsg != null) {
                TacticalPowerUpNotificationPopup(
                    message = notificationMsg,
                    powerUpType = hudState.lastCollectedPowerUp
                )
            }
        }

        // 3. Left Side: Player HP & Shield Tactical Vertical Bars
        TacticalPlayerStatusSidePanel(
            hpRatio = hudState.playerHpRatio,
            shieldRatio = hudState.playerShieldRatio,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp)
        )

        // 4. Right Side: Weapon Status (Missile ×08, Rocket ×04)
        TacticalWeaponStatusSidePanel(
            missileCount = hudState.missileCount,
            rocketCount = hudState.rocketCount,
            isPrimaryArmed = true,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
        )

        // 5. Bottom: Special Weapon bar / EMP / Rocket icon + Secondary trigger
        TacticalBottomControlBar(
            specialWeapon = hudState.activeSpecialWeapon,
            specialCooldownRatio = hudState.specialCooldownRatio,
            secondaryWeapon = hudState.activeSecondaryWeapon,
            secondaryAmmo = hudState.missileCount,
            isTargetLocked = hudState.isTargetLocked,
            lockedTargetName = hudState.lockedTargetName,
            onSecondaryClick = onSecondaryClick,
            onSpecialClick = onSpecialClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

/**
 * Displays calibrated airspeed tape with Material 3 speed and bolt icons,
 * digital Knots readout, Mach number, and Afterburner engagement badge.
 */
@Composable
fun AirspeedTape(
    speedKnots: Int,
    machNumber: Float,
    isAfterburner: Boolean,
    modifier: Modifier = Modifier
) {
    val speedColor by animateColorAsState(
        targetValue = if (isAfterburner) AfterburnerOrange else TacticalCyan,
        label = "speed_color"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xCC090D16))
            .border(1.dp, speedColor.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tactical Airspeed Icon Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = "Airspeed Telemetry",
                tint = speedColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "SPD",
                color = speedColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Digital Airspeed Readout
        Text(
            text = "$speedKnots",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "KTS",
            color = TacticalCyan.copy(alpha = 0.8f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Airspeed Vertical Ladder Graphics
        Canvas(modifier = Modifier.width(36.dp).height(70.dp)) {
            val tapeH = size.height
            val tapeW = size.width
            val tickStep = tapeH / 5f

            // Baseline indicator bar
            drawLine(
                color = speedColor.copy(alpha = 0.35f),
                start = Offset(tapeW - 2.dp.toPx(), 0f),
                end = Offset(tapeW - 2.dp.toPx(), tapeH),
                strokeWidth = 2f
            )

            // Dynamic ladder ticks
            for (i in 0..5) {
                val y = i * tickStep
                val isMajor = i == 2 || i == 3
                val tickW = if (isMajor) 14.dp.toPx() else 8.dp.toPx()
                drawLine(
                    color = if (isMajor) speedColor else speedColor.copy(alpha = 0.4f),
                    start = Offset(tapeW - tickW, y),
                    end = Offset(tapeW, y),
                    strokeWidth = if (isMajor) 2f else 1.5f
                )
            }

            // Current Airspeed Pointer Caret
            val centerY = tapeH / 2f
            drawCircle(
                color = speedColor,
                radius = 3.dp.toPx(),
                center = Offset(tapeW - 12.dp.toPx(), centerY)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Mach Number Readout
        Text(
            text = String.format("M %.2f", machNumber),
            color = if (machNumber >= 1.0f) TacticalAmber else Color(0xFF94A3B8),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        // Afterburner Indicator Badge
        if (isAfterburner) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xCCF97316))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Afterburner Engaged",
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "A/B",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Displays barometric altitude tape with Material 3 height/climb icons,
 * digital Feet readout, Flight Level (FL), and Vertical Speed Indicator (FPM).
 */
@Composable
fun AltitudeTape(
    altitudeFeet: Int,
    climbRateFpm: Int,
    modifier: Modifier = Modifier
) {
    val isClimbing = climbRateFpm > 100
    val isDiving = climbRateFpm < -100

    val altColor = when {
        altitudeFeet < 5000 -> TacticalAmber
        else -> TacticalCyan
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xCC090D16))
            .border(1.dp, altColor.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tactical Altitude Icon Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Height,
                contentDescription = "Altitude Telemetry",
                tint = altColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "ALT",
                color = altColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Digital Altitude Readout (thousands formatted)
        val formattedAlt = "%,d".format(altitudeFeet)
        Text(
            text = formattedAlt,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "FEET",
            color = TacticalCyan.copy(alpha = 0.8f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Altitude Vertical Ladder Graphics
        Canvas(modifier = Modifier.width(36.dp).height(70.dp)) {
            val tapeH = size.height
            val tickStep = tapeH / 5f

            // Baseline indicator bar on left of altitude box
            drawLine(
                color = altColor.copy(alpha = 0.35f),
                start = Offset(2.dp.toPx(), 0f),
                end = Offset(2.dp.toPx(), tapeH),
                strokeWidth = 2f
            )

            // Dynamic ladder ticks
            for (i in 0..5) {
                val y = i * tickStep
                val isMajor = i == 2 || i == 3
                val tickW = if (isMajor) 14.dp.toPx() else 8.dp.toPx()
                drawLine(
                    color = if (isMajor) altColor else altColor.copy(alpha = 0.4f),
                    start = Offset(2.dp.toPx(), y),
                    end = Offset(2.dp.toPx() + tickW, y),
                    strokeWidth = if (isMajor) 2f else 1.5f
                )
            }

            // Current Altitude Pointer Caret
            val centerY = tapeH / 2f
            drawCircle(
                color = altColor,
                radius = 3.dp.toPx(),
                center = Offset(12.dp.toPx(), centerY)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Flight Level (e.g. FL185)
        val flightLevel = (altitudeFeet / 100).coerceAtLeast(1)
        Text(
            text = "FL$flightLevel",
            color = Color(0xFF94A3B8),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        // Vertical Speed Indicator (Climb/Dive with M3 auto-mirrored icons)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(
                    when {
                        isClimbing -> Color(0x3322C55E)
                        isDiving -> Color(0x33EF4444)
                        else -> Color(0x2238BDF8)
                    }
                )
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val climbIcon = when {
                isClimbing -> Icons.AutoMirrored.Filled.TrendingUp
                isDiving -> Icons.AutoMirrored.Filled.TrendingDown
                else -> Icons.Default.Height
            }
            val climbTint = when {
                isClimbing -> Color(0xFF4ADE80)
                isDiving -> CrimsonWarning
                else -> TacticalCyan
            }

            Icon(
                imageVector = climbIcon,
                contentDescription = "Vertical Speed Indicator",
                tint = climbTint,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            val sign = if (climbRateFpm > 0) "+" else ""
            Text(
                text = "$sign$climbRateFpm",
                color = climbTint,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

/**
 * Bottom tactical weapons status bar displaying:
 * 1. Primary Cannon (caliber, cyclic arming state, tactical M3 icon)
 * 2. Secondary Ordnance (ordnance type, missile counter, target lock alert, M3 rocket icon)
 * 3. Special Countermeasure (system type, cooldown gauge, ready badge, M3 shield/flare icon)
 */
@Composable
fun TacticalWeaponStatusPanel(
    primaryWeapon: PrimaryWeaponId,
    primaryStatus: String,
    secondaryWeapon: SecondaryWeaponId,
    secondaryAmmo: Int,
    maxSecondaryAmmo: Int,
    specialWeapon: SpecialWeaponId,
    specialCooldownRatio: Float,
    isTargetLocked: Boolean,
    onSecondaryClick: (() -> Unit)?,
    onSpecialClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Hardpoint 1: Primary Weapon (Rotary / Twin Cannon)
        PrimaryHardpointCard(
            primaryWeapon = primaryWeapon,
            status = primaryStatus,
            modifier = Modifier.weight(1f).testTag("hardpoint_primary")
        )

        // Hardpoint 2: Secondary Ordnance (Missile / Rocket / Bomb)
        SecondaryHardpointCard(
            secondaryWeapon = secondaryWeapon,
            ammo = secondaryAmmo,
            maxAmmo = maxSecondaryAmmo,
            isLocked = isTargetLocked,
            onClick = onSecondaryClick,
            modifier = Modifier
                .weight(1.2f)
                .testTag("secondary_weapon_button")
        )

        // Hardpoint 3: Special Countermeasure (Flares / EMP / Cruise)
        SpecialHardpointCard(
            specialWeapon = specialWeapon,
            cooldownRatio = specialCooldownRatio,
            onClick = onSpecialClick,
            modifier = Modifier
                .weight(1.2f)
                .testTag("special_weapon_button")
        )
    }
}

/**
 * Primary Cannon Hardpoint display card.
 */
@Composable
fun PrimaryHardpointCard(
    primaryWeapon: PrimaryWeaponId,
    status: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xDD0F172A))
            .border(1.dp, Color(0x5538BDF8), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Primary Cannon Hardpoint",
                    tint = TacticalCyan,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "PRI",
                    color = TacticalCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Caliber Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0x3338BDF8))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = primaryWeapon.caliber.take(4).uppercase(),
                    color = TacticalCyan,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = primaryWeapon.displayName,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22C55E))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status,
                color = Color(0xFF4ADE80),
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

/**
 * Secondary Ordnance Hardpoint interactive card.
 */
@Composable
fun SecondaryHardpointCard(
    secondaryWeapon: SecondaryWeaponId,
    ammo: Int,
    maxAmmo: Int,
    isLocked: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val isAvailable = ammo > 0
    val cardBorderColor = when {
        isLocked -> CrimsonWarning
        isAvailable -> Color(0x66EF4444)
        else -> Color(0x33475569)
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isLocked) Color(0xDD3B0707) else Color(0xDD0F172A))
            .border(1.dp, cardBorderColor, RoundedCornerShape(10.dp))
            .then(
                if (onClick != null && isAvailable) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = "Secondary Ordnance Hardpoint",
                    tint = if (isAvailable) CrimsonWarning else Color(0xFF64748B),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "SEC",
                    color = if (isAvailable) CrimsonWarning else Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Target Lock Badge with M3 Lock Icon
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isLocked) Color(0xFFEF4444) else Color(0x2238BDF8))
                    .padding(horizontal = 4.dp, vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = if (isLocked) "Target Locked" else "Radar Search",
                    tint = if (isLocked) Color.White else TacticalCyan,
                    modifier = Modifier.size(9.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = if (isLocked) "LOCK" else "SCAN",
                    color = if (isLocked) Color.White else TacticalCyan,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = secondaryWeapon.displayName,
            color = if (isAvailable) Color.White else Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Ammo Status Pips & Counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$ammo / $maxAmmo",
                color = if (ammo > 2) TacticalAmber else if (ammo > 0) CrimsonWarning else Color(0xFF64748B),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )

            // Visual ammo pip bar
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                val totalPips = maxAmmo.coerceAtMost(8)
                for (p in 1..totalPips) {
                    val filled = p <= ammo
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                if (filled) {
                                    if (isLocked) CrimsonWarning else TacticalAmber
                                } else Color(0x33475569)
                            )
                    )
                }
            }
        }
    }
}

/**
 * Special Countermeasure Hardpoint interactive card (Flares, EMP, Cruise Strike).
 */
@Composable
fun SpecialHardpointCard(
    specialWeapon: SpecialWeaponId,
    cooldownRatio: Float,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val isReady = cooldownRatio >= 1.0f
    val icon = when (specialWeapon) {
        SpecialWeaponId.FLARE_DISPENSER -> Icons.Default.Shield
        SpecialWeaponId.EMP_BLAST -> Icons.Default.Radar
        SpecialWeaponId.AIR_STRIKE_CRUISE -> Icons.Default.RocketLaunch
    }

    val cardBorderColor = if (isReady) Color(0xFF38BDF8) else Color(0x33334155)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isReady) Color(0xDD0C243C) else Color(0xDD0F172A))
            .border(1.dp, cardBorderColor, RoundedCornerShape(10.dp))
            .then(
                if (onClick != null && isReady) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Special Countermeasure Hardpoint",
                    tint = if (isReady) TacticalCyan else Color(0xFF64748B),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "SPEC",
                    color = if (isReady) TacticalCyan else Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Ready Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isReady) Color(0x3322C55E) else Color(0x22475569))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = if (isReady) "READY" else "${(cooldownRatio * 100).toInt()}%",
                    color = if (isReady) Color(0xFF4ADE80) else Color(0xFF94A3B8),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = specialWeapon.displayName,
            color = if (isReady) Color.White else Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Linear Cooldown Progress Gauge
        LinearProgressIndicator(
            progress = { cooldownRatio.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = if (isReady) TacticalCyan else Color(0xFF475569),
            trackColor = Color(0x331E293B)
        )
    }
}

/**
 * Center tactical flight collimator reticle:
 * Boresight crosshairs, pitch lines, and dynamic lock-on brackets.
 */
@Composable
fun FlightBoresightReticle(
    isTargetLocked: Boolean,
    isWarning: Boolean,
    warningAlpha: Float,
    modifier: Modifier = Modifier
) {
    val reticleColor = when {
        isWarning -> CrimsonWarning.copy(alpha = warningAlpha)
        isTargetLocked -> CrimsonWarning
        else -> TacticalCyan.copy(alpha = 0.5f)
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(160.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = 24.dp.toPx()

            // Gun sight collimator ring
            drawCircle(
                color = reticleColor,
                radius = r,
                style = Stroke(
                    width = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )

            // Center Pipper Dot
            drawCircle(
                color = reticleColor,
                radius = 2.5.dp.toPx(),
                center = Offset(cx, cy)
            )

            // Boresight horizon lines
            val lineLength = 28.dp.toPx()
            val gap = 34.dp.toPx()

            // Left pitch bar
            drawLine(
                color = reticleColor,
                start = Offset(cx - gap - lineLength, cy),
                end = Offset(cx - gap, cy),
                strokeWidth = 1.5f
            )
            // Left tick down
            drawLine(
                color = reticleColor,
                start = Offset(cx - gap, cy),
                end = Offset(cx - gap, cy + 8.dp.toPx()),
                strokeWidth = 1.5f
            )

            // Right pitch bar
            drawLine(
                color = reticleColor,
                start = Offset(cx + gap, cy),
                end = Offset(cx + gap + lineLength, cy),
                strokeWidth = 1.5f
            )
            // Right tick down
            drawLine(
                color = reticleColor,
                start = Offset(cx + gap, cy),
                end = Offset(cx + gap, cy + 8.dp.toPx()),
                strokeWidth = 1.5f
            )

            // Upper & lower pitch ladder dashes
            val pitchGapY = 32.dp.toPx()
            drawLine(
                color = reticleColor.copy(alpha = 0.35f),
                start = Offset(cx - 18.dp.toPx(), cy - pitchGapY),
                end = Offset(cx + 18.dp.toPx(), cy - pitchGapY),
                strokeWidth = 1f
            )
            drawLine(
                color = reticleColor.copy(alpha = 0.35f),
                start = Offset(cx - 18.dp.toPx(), cy + pitchGapY),
                end = Offset(cx + 18.dp.toPx(), cy + pitchGapY),
                strokeWidth = 1f
            )
        }

        // Animated target locked brackets box
        if (isTargetLocked) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .border(2.dp, CrimsonWarning, RoundedCornerShape(4.dp))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CrisisAlert,
                    contentDescription = "Target Lock Active",
                    tint = CrimsonWarning,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Tactical Power-Up Icon drawn with high precision Vector Canvas.
 * Supports all 8 collectible ordnance types:
 * Double Damage, Rapid Fire, Unlimited Missiles, Mega Bomb, Shield, EMP, Repair, Credit Bonus.
 */
@Composable
fun TacticalPowerUpIcon(
    type: PowerUpType,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val color = type.color

        when (type) {
            PowerUpType.DOUBLE_DAMAGE -> {
                drawLine(color, Offset(cx - w * 0.28f, cy + h * 0.18f), Offset(cx, cy - h * 0.22f), strokeWidth = 2.5f)
                drawLine(color, Offset(cx + w * 0.28f, cy + h * 0.18f), Offset(cx, cy - h * 0.22f), strokeWidth = 2.5f)
                drawLine(Color.White, Offset(cx - w * 0.2f, cy + h * 0.35f), Offset(cx, cy + h * 0.05f), strokeWidth = 2f)
                drawLine(Color.White, Offset(cx + w * 0.2f, cy + h * 0.35f), Offset(cx, cy + h * 0.05f), strokeWidth = 2f)
            }
            PowerUpType.RAPID_FIRE -> {
                drawLine(color, Offset(cx - w * 0.25f, cy + h * 0.25f), Offset(cx - w * 0.08f, cy - h * 0.3f), strokeWidth = 2.2f)
                drawLine(Color.White, Offset(cx, cy + h * 0.3f), Offset(cx + w * 0.08f, cy - h * 0.35f), strokeWidth = 2.5f)
                drawLine(color, Offset(cx + w * 0.25f, cy + h * 0.25f), Offset(cx + w * 0.17f, cy - h * 0.3f), strokeWidth = 2.2f)
            }
            PowerUpType.UNLIMITED_MISSILES, PowerUpType.MISSILE_RESTOCK -> {
                drawLine(color, Offset(cx - w * 0.18f, cy + h * 0.35f), Offset(cx - w * 0.18f, cy - h * 0.3f), strokeWidth = 2.5f)
                drawLine(Color.White, Offset(cx - w * 0.18f, cy - h * 0.3f), Offset(cx - w * 0.18f, cy - h * 0.45f), strokeWidth = 3f)
                drawLine(color, Offset(cx + w * 0.18f, cy + h * 0.35f), Offset(cx + w * 0.18f, cy - h * 0.3f), strokeWidth = 2.5f)
                drawLine(Color.White, Offset(cx + w * 0.18f, cy - h * 0.3f), Offset(cx + w * 0.18f, cy - h * 0.45f), strokeWidth = 3f)
            }
            PowerUpType.MEGA_BOMB -> {
                drawCircle(color, w * 0.26f, Offset(cx, cy - h * 0.05f))
                drawCircle(Color.White, w * 0.13f, Offset(cx, cy - h * 0.05f))
                drawLine(color, Offset(cx - w * 0.3f, cy + h * 0.3f), Offset(cx + w * 0.3f, cy + h * 0.3f), strokeWidth = 2.2f)
                drawLine(color, Offset(cx, cy + h * 0.25f), Offset(cx, cy + h * 0.4f), strokeWidth = 2.2f)
            }
            PowerUpType.SHIELD, PowerUpType.SHIELD_BOOST -> {
                val shieldPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx, cy - h * 0.38f)
                    lineTo(cx + w * 0.35f, cy - h * 0.18f)
                    lineTo(cx + w * 0.35f, cy + h * 0.15f)
                    lineTo(cx, cy + h * 0.38f)
                    lineTo(cx - w * 0.35f, cy + h * 0.15f)
                    lineTo(cx - w * 0.35f, cy - h * 0.18f)
                    close()
                }
                drawPath(shieldPath, color = color, style = Stroke(width = 2f))
                drawCircle(Color.White, w * 0.14f, Offset(cx, cy))
            }
            PowerUpType.EMP -> {
                drawCircle(color, w * 0.35f, Offset(cx, cy), style = Stroke(width = 1.8f))
                drawCircle(color.copy(alpha = 0.5f), w * 0.2f, Offset(cx, cy), style = Stroke(width = 1.5f))
                drawCircle(Color.White, w * 0.08f, Offset(cx, cy))
            }
            PowerUpType.REPAIR, PowerUpType.REPAIR_HULL -> {
                drawRect(color, topLeft = Offset(cx - w * 0.12f, cy - h * 0.35f), size = Size(w * 0.24f, h * 0.7f))
                drawRect(color, topLeft = Offset(cx - w * 0.35f, cy - h * 0.12f), size = Size(w * 0.7f, h * 0.24f))
                drawRect(Color.White, topLeft = Offset(cx - w * 0.06f, cy - h * 0.26f), size = Size(w * 0.12f, h * 0.52f))
                drawRect(Color.White, topLeft = Offset(cx - w * 0.26f, cy - h * 0.06f), size = Size(w * 0.52f, h * 0.12f))
            }
            PowerUpType.CREDIT_BONUS, PowerUpType.CREDIT_CRATE -> {
                drawCircle(color, w * 0.35f, Offset(cx, cy))
                drawCircle(Color(0xFF78350F), w * 0.28f, Offset(cx, cy), style = Stroke(width = 1.5f))
                drawCircle(Color.White, w * 0.12f, Offset(cx, cy))
            }
        }
    }
}

/**
 * Animated Missile Warning Banner (Normal -> Warning -> Critical).
 * Displays threat level, approximate distance in meters, and directional bearing clock code.
 */
@Composable
fun TacticalMissileWarningBanner(
    threatLevel: MissileThreatLevel,
    distanceMeters: Int,
    directionText: String,
    modifier: Modifier = Modifier
) {
    if (threatLevel == MissileThreatLevel.NONE) return

    val infiniteTransition = rememberInfiniteTransition(label = "missile_warning_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (threatLevel == MissileThreatLevel.CRITICAL) 0.35f else 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (threatLevel == MissileThreatLevel.CRITICAL) 200 else 450,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "missile_alpha"
    )

    val bgColor = when (threatLevel) {
        MissileThreatLevel.CRITICAL -> Color(0xEE7F1D1D).copy(alpha = pulseAlpha)
        MissileThreatLevel.WARNING -> Color(0xDD7C2D12).copy(alpha = pulseAlpha)
        MissileThreatLevel.NORMAL, MissileThreatLevel.NONE -> Color(0xCC78350F)
    }

    val borderColor = when (threatLevel) {
        MissileThreatLevel.CRITICAL -> Color(0xFFEF4444)
        MissileThreatLevel.WARNING -> Color(0xFFF97316)
        MissileThreatLevel.NORMAL, MissileThreatLevel.NONE -> Color(0xFFF59E0B)
    }

    val textColor = when (threatLevel) {
        MissileThreatLevel.CRITICAL -> Color(0xFFFEF2F2)
        MissileThreatLevel.WARNING -> Color(0xFFFFF7ED)
        MissileThreatLevel.NORMAL, MissileThreatLevel.NONE -> Color(0xFFFEF3C7)
    }

    val title = when (threatLevel) {
        MissileThreatLevel.CRITICAL -> "CRITICAL MISSILE LOCK"
        MissileThreatLevel.WARNING -> "WARNING: INCOMING MISSILE"
        MissileThreatLevel.NORMAL, MissileThreatLevel.NONE -> "MISSILE DETECTED"
    }

    Card(
        modifier = modifier
            .padding(horizontal = 14.dp)
            .testTag("missile_warning_banner"),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Missile Threat Hazard",
                    tint = borderColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = title,
                        color = textColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (threatLevel == MissileThreatLevel.CRITICAL) "EVADE IMMEDIATELY / POP FLARES" else "EVASIVE MANEUVER RECOMMENDED",
                        color = textColor.copy(alpha = 0.8f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Bearing & Distance Telemetry
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x99000000))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${distanceMeters}M",
                        color = borderColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "BRG: $directionText",
                    color = textColor,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Left Side: Player HP and Deflector Shield tactical vertical bars.
 */
@Composable
fun TacticalPlayerStatusSidePanel(
    hpRatio: Float,
    shieldRatio: Float,
    modifier: Modifier = Modifier
) {
    val hpColor = when {
        hpRatio > 0.6f -> Color(0xFF22C55E)
        hpRatio > 0.25f -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }
    val shieldColor = Color(0xFF00E5FF)

    Card(
        modifier = modifier
            .width(52.dp)
            .testTag("hud_player_status_panel"),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC090D16)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4438BDF8)),
        shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // HP Section
            Text(
                text = "HULL",
                color = Color(0xFF94A3B8),
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "${(hpRatio * 100).toInt()}%",
                color = hpColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Segmented vertical bar for HP
            Column(
                modifier = Modifier
                    .width(10.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0x331E293B))
                    .padding(1.dp),
                verticalArrangement = Arrangement.spacedBy(1.5.dp, Alignment.Bottom)
            ) {
                val totalBars = 8
                val filledBars = (hpRatio * totalBars).roundToInt()
                for (i in (totalBars - 1) downTo 0) {
                    val active = i < filledBars
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(if (active) hpColor else Color(0x22475569))
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Shield Section
            Text(
                text = "SHD",
                color = Color(0xFF94A3B8),
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "${(shieldRatio * 100).toInt()}%",
                color = shieldColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Segmented vertical bar for Shield
            Column(
                modifier = Modifier
                    .width(10.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0x331E293B))
                    .padding(1.dp),
                verticalArrangement = Arrangement.spacedBy(1.5.dp, Alignment.Bottom)
            ) {
                val totalBars = 8
                val filledBars = (shieldRatio * totalBars).roundToInt()
                for (i in (totalBars - 1) downTo 0) {
                    val active = i < filledBars
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(if (active) shieldColor else Color(0x22475569))
                    )
                }
            }
        }
    }
}

/**
 * Right Side: Weapon Status (Missile ×08, Rocket ×04, Cannon ARMED).
 */
@Composable
fun TacticalWeaponStatusSidePanel(
    missileCount: Int,
    rocketCount: Int,
    isPrimaryArmed: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(76.dp)
            .testTag("hud_weapon_status_side_panel"),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC090D16)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4438BDF8)),
        shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "WEAPONS",
                color = Color(0xFF64748B),
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )

            // Cannon
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "CANNON",
                    color = Color(0xFF94A3B8),
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isPrimaryArmed) Color(0x3322C55E) else Color(0x33EF4444))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = if (isPrimaryArmed) "ARMED" else "OFF",
                        color = if (isPrimaryArmed) Color(0xFF4ADE80) else Color(0xFFEF4444),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Missile ×08
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "MISSILE",
                    color = Color(0xFF94A3B8),
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "×${missileCount.toString().padStart(2, '0')}",
                    color = if (missileCount > 0) Color(0xFFFBBF24) else Color(0xFF64748B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Rocket ×04
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ROCKET",
                    color = Color(0xFF94A3B8),
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "×${rocketCount.toString().padStart(2, '0')}",
                    color = if (rocketCount > 0) Color(0xFF38BDF8) else Color(0xFF64748B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Tactical Active Power-Up countdown timer widget.
 */
@Composable
fun TacticalActivePowerUpWidget(
    activeType: PowerUpType?,
    remainingSec: Float,
    modifier: Modifier = Modifier
) {
    if (activeType == null || remainingSec <= 0f) return

    val totalDuration = 10f
    val ratio = (remainingSec / totalDuration).coerceIn(0f, 1f)

    Card(
        modifier = modifier.testTag("hud_active_powerup_timer"),
        colors = CardDefaults.cardColors(containerColor = Color(0xDD0F172A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, activeType.color),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TacticalPowerUpIcon(type = activeType, size = 18.dp)
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Row(
                    modifier = Modifier.width(110.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activeType.label,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${"%.1f".format(remainingSec)}s",
                        color = activeType.color,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                LinearProgressIndicator(
                    progress = { ratio },
                    modifier = Modifier
                        .width(110.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp)),
                    color = activeType.color,
                    trackColor = Color(0x33334155)
                )
            }
        }
    }
}

/**
 * Power-up notification popup banner when collectible is picked up.
 */
@Composable
fun TacticalPowerUpNotificationPopup(
    message: String?,
    powerUpType: PowerUpType?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn() + slideInVertically { -20 },
        exit = fadeOut() + slideOutVertically { -20 },
        modifier = modifier
    ) {
        if (message == null) return@AnimatedVisibility

        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .testTag("powerup_notification_popup"),
            colors = CardDefaults.cardColors(containerColor = Color(0xEE090D16)),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                powerUpType?.color ?: Color(0xFF38BDF8)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (powerUpType != null) {
                    TacticalPowerUpIcon(type = powerUpType, size = 20.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Powerup Alert",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Bottom Tactical Control Bar:
 * - Special Weapon bar (EMP / Flares / Cruise Strike) with circular cooldown ring
 * - Secondary weapon trigger button (Missile / Rocket launch) with lock indicator
 */
@Composable
fun TacticalBottomControlBar(
    specialWeapon: SpecialWeaponId,
    specialCooldownRatio: Float,
    secondaryWeapon: SecondaryWeaponId,
    secondaryAmmo: Int,
    isTargetLocked: Boolean,
    lockedTargetName: String?,
    onSecondaryClick: (() -> Unit)?,
    onSpecialClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val isSpecialReady = specialCooldownRatio >= 1.0f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Special Weapon Trigger
        Card(
            modifier = Modifier
                .weight(1f)
                .height(54.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(enabled = onSpecialClick != null && isSpecialReady) {
                    onSpecialClick?.invoke()
                }
                .testTag("special_weapon_button"),
            colors = CardDefaults.cardColors(
                containerColor = if (isSpecialReady) Color(0xEE0C243C) else Color(0xCC0F172A)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (isSpecialReady) TacticalCyan else Color(0x33334155)
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isSpecialReady) Color(0x3300E5FF) else Color(0x22334155)),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (specialWeapon) {
                            SpecialWeaponId.FLARE_DISPENSER -> Icons.Default.Shield
                            SpecialWeaponId.EMP_BLAST -> Icons.Default.Radar
                            SpecialWeaponId.AIR_STRIKE_CRUISE -> Icons.Default.RocketLaunch
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Special Weapon Icon",
                            tint = if (isSpecialReady) TacticalCyan else Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = specialWeapon.displayName.uppercase(),
                            color = if (isSpecialReady) Color.White else Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                        Text(
                            text = if (isSpecialReady) "READY // ACTIVATE" else "RECHARGE ${(specialCooldownRatio * 100).toInt()}%",
                            color = if (isSpecialReady) Color(0xFF4ADE80) else Color(0xFF64748B),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Cooldown Ring
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .border(
                            2.dp,
                            if (isSpecialReady) Color(0xFF4ADE80) else Color(0xFF334155),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSpecialReady) "RDY" else "${(specialCooldownRatio * 100).toInt()}%",
                        color = if (isSpecialReady) Color(0xFF4ADE80) else Color(0xFF94A3B8),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Secondary Launch Trigger (Missile / Rocket)
        Card(
            modifier = Modifier
                .weight(1f)
                .height(54.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(enabled = onSecondaryClick != null && secondaryAmmo > 0) {
                    onSecondaryClick?.invoke()
                }
                .testTag("secondary_weapon_button"),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isTargetLocked -> Color(0xEE7F1D1D)
                    secondaryAmmo > 0 -> Color(0xEE1E293B)
                    else -> Color(0xAA0F172A)
                }
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                when {
                    isTargetLocked -> CrimsonWarning
                    secondaryAmmo > 0 -> TacticalAmber
                    else -> Color(0x33475569)
                }
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isTargetLocked -> Color(0x44EF4444)
                                    secondaryAmmo > 0 -> Color(0x33F59E0B)
                                    else -> Color(0x22334155)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isTargetLocked) Icons.Default.CrisisAlert else Icons.Default.GpsFixed,
                            contentDescription = "Fire Secondary Ordnance",
                            tint = when {
                                isTargetLocked -> CrimsonWarning
                                secondaryAmmo > 0 -> TacticalAmber
                                else -> Color(0xFF64748B)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isTargetLocked) "FIRE [${lockedTargetName ?: "LOCKED"}]" else secondaryWeapon.displayName.uppercase(),
                            color = if (isTargetLocked) Color(0xFFFECACA) else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                        Text(
                            text = if (isTargetLocked) "LOCK ACQUIRED" else "$secondaryAmmo ARMED",
                            color = if (isTargetLocked) CrimsonWarning else TacticalAmber,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Ammo Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x99000000))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "×$secondaryAmmo",
                        color = if (secondaryAmmo > 0) TacticalAmber else Color(0xFF64748B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

/**
 * Top Tactical HUD Row:
 * - Top Left: Mission Objective + Progress (e.g. 2/5)
 * - Top Center: Wave 04 (or BOSS ENGAGED)
 * - Top Right: Credits + Score + Pause Button
 */
@Composable
fun TacticalTopHud(
    objectiveTitle: String,
    objectiveProgress: String,
    waveNumber: Int,
    isBossWave: Boolean,
    credits: Int,
    score: Int,
    isPaused: Boolean,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("tactical_top_hud"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Top Left: Mission Objective + Progress 2/5
        Card(
            modifier = Modifier
                .widthIn(max = 160.dp)
                .testTag("hud_top_objective"),
            colors = CardDefaults.cardColors(containerColor = Color(0xCC0F172A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4438BDF8)),
            shape = RoundedCornerShape(6.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Adjust,
                        contentDescription = "Objective Indicator",
                        tint = TacticalCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "OBJECTIVE",
                        color = TacticalCyan,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = objectiveTitle,
                    color = Color(0xFFF1F5F9),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "PROG: $objectiveProgress",
                    color = Color(0xFF38BDF8),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Top Center: Wave 04
        Card(
            modifier = Modifier.testTag("hud_top_wave"),
            colors = CardDefaults.cardColors(
                containerColor = if (isBossWave) Color(0xDD7F1D1D) else Color(0xCC0F172A)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isBossWave) Color(0xFFEF4444) else Color(0xFF38BDF8)
            ),
            shape = RoundedCornerShape(6.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isBossWave) "ALERT" else "WAVE",
                    color = if (isBossWave) Color(0xFFFECACA) else Color(0xFF94A3B8),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (isBossWave) "BOSS" else waveNumber.toString().padStart(2, '0'),
                    color = if (isBossWave) Color(0xFFEF4444) else Color(0xFF00E5FF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Top Right: Credits + Score + Pause
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = "+$credits CR",
                    color = Color(0xFFFBBF24),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "SCR $score",
                    color = Color(0xFF94A3B8),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            IconButton(
                onClick = onPauseClick,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0x880F172A))
                    .border(1.dp, Color(0x3338BDF8), CircleShape)
                    .testTag("pause_button")
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = "Pause Sortie",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
