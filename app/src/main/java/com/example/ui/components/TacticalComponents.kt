package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CreditGold
import com.example.ui.theme.CyanHighlight
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedBorder
import com.example.ui.theme.DangerRedBright
import com.example.ui.theme.DarkSteel
import com.example.ui.theme.DarkSteelVariant
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.HealthGreen
import com.example.ui.theme.HealthGreenDim
import com.example.ui.theme.MilitaryGreen
import com.example.ui.theme.MilitaryGreenBorder
import com.example.ui.theme.MilitaryGreenBright
import com.example.ui.theme.MilitaryGreenPressed
import com.example.ui.theme.MutedGray
import com.example.ui.theme.SecondarySteel
import com.example.ui.theme.SecondarySteelBorder
import com.example.ui.theme.SecondarySteelPressed
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.ShieldCyanDim
import com.example.ui.theme.TacticalBlue
import com.example.ui.theme.WarmOrange

// =========================================================================
// 1. MILITARY PANEL
// =========================================================================

/**
 * Tactical aviation-grade panel with beveled look, subtle inner gradient,
 * tactical border, and optional header tab / corner indicators.
 */
@Composable
fun MilitaryPanel(
    modifier: Modifier = Modifier,
    title: String? = null,
    subTitle: String? = null,
    headerRightContent: (@Composable () -> Unit)? = null,
    borderColor: Color = TacticalBlue.copy(alpha = 0.6f),
    cornerHighlightColor: Color = CyanHighlight,
    backgroundColor: Color = DarkSteel,
    shape: Shape = RoundedCornerShape(8.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.95f),
                        DeepNavy
                    )
                )
            )
            .border(
                border = BorderStroke(1.5.dp, borderColor),
                shape = shape
            )
    ) {
        // Corner bracket tactical accents
        Canvas(modifier = Modifier.matchParentSize()) {
            val bracketLen = 14f
            val strokeW = 2.5f
            // Top-Left corner
            drawLine(cornerHighlightColor, Offset(4f, 4f), Offset(4f + bracketLen, 4f), strokeW)
            drawLine(cornerHighlightColor, Offset(4f, 4f), Offset(4f, 4f + bracketLen), strokeW)
            // Top-Right corner
            drawLine(cornerHighlightColor, Offset(size.width - 4f, 4f), Offset(size.width - 4f - bracketLen, 4f), strokeW)
            drawLine(cornerHighlightColor, Offset(size.width - 4f, 4f), Offset(size.width - 4f, 4f + bracketLen), strokeW)
            // Bottom-Left corner
            drawLine(cornerHighlightColor, Offset(4f, size.height - 4f), Offset(4f + bracketLen, size.height - 4f), strokeW)
            drawLine(cornerHighlightColor, Offset(4f, size.height - 4f), Offset(4f, size.height - 4f - bracketLen), strokeW)
            // Bottom-Right corner
            drawLine(cornerHighlightColor, Offset(size.width - 4f, size.height - 4f), Offset(size.width - 4f - bracketLen, size.height - 4f), strokeW)
            drawLine(cornerHighlightColor, Offset(size.width - 4f, size.height - 4f), Offset(size.width - 4f, size.height - 4f - bracketLen), strokeW)
        }

        Column(modifier = Modifier.padding(12.dp)) {
            if (title != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = title.uppercase(),
                            color = CyanHighlight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        if (subTitle != null) {
                            Text(
                                text = subTitle,
                                color = MutedGray,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                    if (headerRightContent != null) {
                        headerRightContent()
                    }
                }
            }
            content()
        }
    }
}

// =========================================================================
// 2. TACTICAL BUTTON SYSTEM
// =========================================================================

enum class TacticalButtonType {
    PRIMARY,    // Military Green with edge highlight (strongest visual priority)
    SECONDARY,  // Dark Blue Steel with cyan border
    DANGER,     // Dark Red with warning border
    GOLD        // Currency / Upgrade accent
}

@Composable
fun TacticalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: TacticalButtonType = TacticalButtonType.SECONDARY,
    enabled: Boolean = true,
    testTag: String = "tactical_button",
    onPlaySound: (() -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Tactile press scaling
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1.0f,
        label = "tactical_button_scale"
    )

    // Trigger audio feedback on press
    LaunchedEffect(isPressed) {
        if (isPressed && enabled) {
            onPlaySound?.invoke()
        }
    }

    val (bgBrush, borderColor, textColor) = when (type) {
        TacticalButtonType.PRIMARY -> {
            if (!enabled) {
                Triple(
                    Brush.verticalGradient(listOf(Color(0xFF16251E), Color(0xFF0F1A14))),
                    Color(0xFF2A4235),
                    MutedGray
                )
            } else if (isPressed) {
                Triple(
                    Brush.verticalGradient(listOf(MilitaryGreenPressed, Color(0xFF10281D))),
                    MilitaryGreenBorder,
                    Color.White
                )
            } else {
                Triple(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF256143),
                            MilitaryGreen,
                            Color(0xFF143525)
                        )
                    ),
                    MilitaryGreenBorder,
                    Color(0xFFE6FFFA)
                )
            }
        }
        TacticalButtonType.SECONDARY -> {
            if (!enabled) {
                Triple(
                    Brush.verticalGradient(listOf(Color(0xFF161F25), Color(0xFF10171C))),
                    Color(0xFF2A3A45),
                    MutedGray
                )
            } else if (isPressed) {
                Triple(
                    Brush.verticalGradient(listOf(SecondarySteelPressed, Color(0xFF121B21))),
                    CyanHighlight,
                    CyanHighlight
                )
            } else {
                Triple(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF2A3F4E),
                            SecondarySteel,
                            Color(0xFF141F26)
                        )
                    ),
                    SecondarySteelBorder,
                    Color(0xFFE2E8F0)
                )
            }
        }
        TacticalButtonType.DANGER -> {
            if (!enabled) {
                Triple(
                    Brush.verticalGradient(listOf(Color(0xFF201313), Color(0xFF170C0C))),
                    Color(0xFF3B1D1D),
                    MutedGray
                )
            } else if (isPressed) {
                Triple(
                    Brush.verticalGradient(listOf(Color(0xFF4A1212), Color(0xFF330B0B))),
                    DangerRedBright,
                    Color.White
                )
            } else {
                Triple(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF8F1D1D),
                            DangerRed,
                            Color(0xFF400E0E)
                        )
                    ),
                    DangerRedBorder,
                    Color(0xFFFFE4E6)
                )
            }
        }
        TacticalButtonType.GOLD -> {
            if (!enabled) {
                Triple(
                    Brush.verticalGradient(listOf(Color(0xFF261E14), Color(0xFF1B140C))),
                    Color(0xFF4A3B22),
                    MutedGray
                )
            } else if (isPressed) {
                Triple(
                    Brush.verticalGradient(listOf(Color(0xFF92400E), Color(0xFF78350F))),
                    CreditGold,
                    Color.White
                )
            } else {
                Triple(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFB45309),
                            Color(0xFF92400E),
                            Color(0xFF78350F)
                        )
                    ),
                    CreditGold,
                    Color(0xFFFEF3C7)
                )
            }
        }
    }

    Box(
        modifier = modifier
            .scale(scale)
            .testTag(testTag)
            .clip(RoundedCornerShape(6.dp))
            .background(bgBrush)
            .border(
                border = BorderStroke(if (type == TacticalButtonType.PRIMARY) 2.dp else 1.5.dp, borderColor),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            content()
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailingIcon()
            }
        }
    }
}

// =========================================================================
// 3. STAT BAR
// =========================================================================

/**
 * Tactical Stat Meter (Armor, Cannon Damage, Missile Capacity, Speed, Maneuverability)
 * with segmented steps or smooth ratio.
 */
@Composable
fun StatBar(
    label: String,
    value: Float, // 0.0f to 1.0f
    modifier: Modifier = Modifier,
    valueText: String? = null,
    barColor: Color = WarmOrange,
    trackColor: Color = DarkSteelVariant,
    segments: Int = 10,
    height: Dp = 10.dp
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label.uppercase(),
                color = MutedGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            )
            Text(
                text = valueText ?: "${(value * 100).toInt()}%",
                color = barColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(2.dp))
                .background(trackColor),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val filledSegments = (value * segments).toInt().coerceIn(0, segments)
            for (i in 0 until segments) {
                val isFilled = i < filledSegments
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(height)
                        .background(if (isFilled) barColor else Color.Transparent)
                )
            }
        }
    }
}

// =========================================================================
// 4. HEALTH & SHIELD GAUGE BARS
// =========================================================================

@Composable
fun HealthBar(
    hpRatio: Float,
    shieldRatio: Float,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(DarkSteel.copy(alpha = 0.9f))
            .border(1.dp, TacticalBlue.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        // Hull HP Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 18.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(HealthGreenDim),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "HP",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(DarkSteelVariant)
                    .border(1.dp, Color(0xFF1E3A2F), RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(hpRatio.coerceIn(0f, 1f))
                        .height(14.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    HealthGreenDim,
                                    if (hpRatio > 0.3f) HealthGreen else DangerRedBright
                                )
                            )
                        )
                )
            }
            if (showLabels) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${(hpRatio * 100).toInt()}%",
                    color = if (hpRatio > 0.3f) HealthGreen else DangerRedBright,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.End
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Deflector Shield Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 18.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(ShieldCyanDim),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SHIELD",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(DarkSteelVariant)
                    .border(1.dp, Color(0xFF0C4A6E), RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(shieldRatio.coerceIn(0f, 1f))
                        .height(14.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(ShieldCyanDim, ShieldCyan)
                            )
                        )
                )
            }
            if (showLabels) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${(shieldRatio * 100).toInt()}%",
                    color = ShieldCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

// =========================================================================
// 5. TACTICAL NOTIFICATION / ALERT BANNER
// =========================================================================

enum class NotificationSeverity {
    INFO,
    WARNING,
    CRITICAL,
    SUCCESS
}

@Composable
fun TacticalNotification(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    severity: NotificationSeverity = NotificationSeverity.INFO,
    tag: String? = null
) {
    val (bgColor, borderColor, accentColor) = when (severity) {
        NotificationSeverity.INFO -> Triple(DarkSteel, TacticalBlue, CyanHighlight)
        NotificationSeverity.WARNING -> Triple(Color(0xFF261D12), WarmOrange, CreditGold)
        NotificationSeverity.CRITICAL -> Triple(Color(0xFF261214), DangerRedBorder, DangerRedBright)
        NotificationSeverity.SUCCESS -> Triple(Color(0xFF14241B), MilitaryGreenBorder, MilitaryGreenBright)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(6.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left accent stripe
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(36.dp)
                .background(accentColor, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title.uppercase(),
                    color = accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.0.sp
                )
                if (tag != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(accentColor.copy(alpha = 0.2f))
                            .border(0.5.dp, accentColor, RoundedCornerShape(3.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tag.uppercase(),
                            color = accentColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = message,
                color = MutedGray,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
