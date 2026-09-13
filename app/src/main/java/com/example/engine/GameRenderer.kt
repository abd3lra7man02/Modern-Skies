package com.example.engine

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.graphics.nativeCanvas
import com.example.model.AircraftId
import com.example.model.WingmanId
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class EnemySpritePack(
    val scout: ImageBitmap? = null,
    val interceptor: ImageBitmap? = null,
    val gunship: ImageBitmap? = null,
    val ace: ImageBitmap? = null,
    // 7 Category 1 enemy aircraft
    val lightInterceptor: ImageBitmap? = null,
    val heavyFighter: ImageBitmap? = null,
    val strikeAircraft: ImageBitmap? = null,
    val bomber: ImageBitmap? = null,
    val attackHelo: ImageBitmap? = null,
    val stealthAircraft: ImageBitmap? = null,
    val swarmDrone: ImageBitmap? = null,
    val aceRazor: ImageBitmap? = null,
    // 6 Category 2 ground targets
    val groundHangar: ImageBitmap? = null,
    val groundRadar: ImageBitmap? = null,
    val groundBridge: ImageBitmap? = null,
    val navalCruiser: ImageBitmap? = null,
    val groundMilitaryBase: ImageBitmap? = null,
    val groundVehicleConvoy: ImageBitmap? = null,
    // 6 Category 3 environment textures
    val bgDesert: ImageBitmap? = null,
    val bgCoast: ImageBitmap? = null,
    val bgMountain: ImageBitmap? = null,
    val bgUrban: ImageBitmap? = null,
    val bgArctic: ImageBitmap? = null,
    val bgMilitaryFacility: ImageBitmap? = null,
    // Category 4 Boss 10 Airborne Fortress
    val bossAirborneFortress: ImageBitmap? = null
)

object GameRenderer {

    private val renderPath = Path()

    private val targetTextPaint = Paint().apply {
        color = android.graphics.Color.argb(200, 0, 229, 255)
        textSize = 20f
        isAntiAlias = true
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }

    private val targetLockTextPaint = Paint().apply {
        color = android.graphics.Color.argb(245, 239, 68, 68)
        textSize = 22f
        isAntiAlias = true
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }

    private val missileWarningTextPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 22f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }

    fun render(
        drawScope: DrawScope,
        engine: GameEngine,
        playerJetSprite: ImageBitmap? = null,
        enemySprites: EnemySpritePack? = null
    ) {
        val width = drawScope.size.width
        val height = drawScope.size.height

        drawScope.apply {
            // Apply screen shake offset if active
            val shakeX = if (engine.screenShakeAmount > 0) (Math.random().toFloat() - 0.5f) * engine.screenShakeAmount else 0f
            val shakeY = if (engine.screenShakeAmount > 0) (Math.random().toFloat() - 0.5f) * engine.screenShakeAmount else 0f

            // 1. Render Terrain / Environment Background (Parallax seamless scrolling)
            renderTerrain(width, height, engine.terrainScrollY, engine.missionNumber, enemySprites)

            // 2. Render Destructible Ground Targets & Friendlies
            for (f in engine.friendlies) {
                renderFriendly(f, shakeX, shakeY)
            }
            for (e in engine.enemies) {
                if (e.isGround) {
                    renderGroundTarget(e, shakeX, shakeY, enemySprites)
                }
            }

            // 3. Render Bombs
            for (b in engine.bombs) {
                renderBomb(b, shakeX, shakeY)
            }

            // 4. Render Power-ups
            for (p in engine.powerUps) {
                renderPowerUp(p, shakeX, shakeY)
            }

            // 5. Render Airborne Enemies & Boss
            for (e in engine.enemies) {
                if (!e.isGround) {
                    renderAirEnemy(e, shakeX, shakeY, enemySprites)
                }
            }

            // 6. Render Wingman
            if (engine.player.isAlive && engine.player.wingmanId != WingmanId.NONE) {
                renderWingman(engine.player, shakeX, shakeY)
            }

            // 7. Render Player Aircraft
            if (engine.player.isAlive) {
                renderPlayer(engine.player, shakeX, shakeY, playerJetSprite)
            }

            // 8. Render Projectiles (Bullets, Missiles, Rockets)
            for (b in engine.bullets) {
                val bulletColor = b.color
                val radius = if (b.isHeavy) 4.5f else 3f
                drawCircle(
                    color = bulletColor,
                    radius = radius,
                    center = Offset(b.x + shakeX, b.y + shakeY)
                )
                // Tracer trail
                drawLine(
                    color = bulletColor.copy(alpha = 0.6f),
                    start = Offset(b.x + shakeX, b.y + shakeY),
                    end = Offset(b.x - b.vx * 0.8f + shakeX, b.y - b.vy * 0.8f + shakeY),
                    strokeWidth = radius * 1.5f
                )
            }

            for (m in engine.missiles) {
                renderMissile(m, shakeX, shakeY)
            }

            for (r in engine.rockets) {
                renderRocket(r, shakeX, shakeY)
            }

            // 9. Render Particles & Shockwaves
            for (p in engine.particles) {
                val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = p.size * alpha,
                    center = Offset(p.x + shakeX, p.y + shakeY)
                )
            }

            for (s in engine.shockwaves) {
                drawCircle(
                    color = s.color.copy(alpha = s.alpha),
                    radius = s.radius,
                    center = Offset(s.x + shakeX, s.y + shakeY),
                    style = Stroke(width = 4f)
                )
            }

            // 10. Render High-Altitude Clouds (Parallax overlay)
            renderClouds(width, height, engine.terrainScrollY * 1.5f)

            // 11. Render Tactical Targeting UI (Thin brackets, distance in meters, target tags, micro HP bars, lock highlight)
            renderTargetingUI(engine, width, height, shakeX, shakeY)

            // 12. Render Directional Missile Warning Indicator (Bearing chevron, threat level, distance)
            renderMissileDirectionalWarning(engine, shakeX, shakeY)
        }
    }

    private fun DrawScope.renderTerrain(
        width: Float,
        height: Float,
        scrollY: Float,
        missionNum: Int,
        sprites: EnemySpritePack? = null
    ) {
        val bgSprite = when {
            missionNum in 1..10 -> sprites?.bgDesert
            missionNum in 11..20 -> sprites?.bgCoast
            missionNum in 21..30 -> sprites?.bgMountain
            missionNum in 31..40 -> sprites?.bgUrban
            missionNum in 41..50 -> sprites?.bgArctic
            else -> sprites?.bgMilitaryFacility
        }

        if (bgSprite != null) {
            // Seamless parallax vertical texture tiling
            val tileH = width // maintain square aspect ratio scaled to screen width
            val normScroll = (scrollY % tileH + tileH) % tileH
            var currentY = -tileH + normScroll
            while (currentY < height + tileH) {
                drawImage(
                    image = bgSprite,
                    dstOffset = IntOffset(0, currentY.toInt()),
                    dstSize = IntSize(width.toInt(), tileH.toInt())
                )
                currentY += tileH
            }
        } else {
            // Procedural tactical satellite terrain fallback
            val (baseColor, accentColor, gridColor) = when (missionNum) {
                in 11..20 -> Triple(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF38BDF8)) // Coastal water
                in 41..50 -> Triple(Color(0xFFE2E8F0), Color(0xFFCBD5E1), Color(0xFF94A3B8)) // Snow tundra
                in 31..40 -> Triple(Color(0xFF1C1917), Color(0xFF292524), Color(0xFF78716C)) // Urban citadel
                else -> Triple(Color(0xFF2B1D0C), Color(0xFF3F2B13), Color(0xFF573E1C)) // Desert military zone
            }

            drawRect(color = baseColor, size = Size(width, height))

            val gridSize = 120f
            val offsetY = scrollY % gridSize
            for (y in -gridSize.toInt()..(height + gridSize).toInt() step gridSize.toInt()) {
                drawLine(
                    color = gridColor.copy(alpha = 0.08f),
                    start = Offset(0f, y.toFloat() + offsetY),
                    end = Offset(width, y.toFloat() + offsetY),
                    strokeWidth = 1f
                )
            }
            for (x in 0..width.toInt() step gridSize.toInt()) {
                drawLine(
                    color = gridColor.copy(alpha = 0.08f),
                    start = Offset(x.toFloat(), 0f),
                    end = Offset(x.toFloat(), height),
                    strokeWidth = 1f
                )
            }
        }

        // Tactical altitude grid overlay
        val overlayGridSize = 180f
        val overlayOffset = (scrollY * 0.5f) % overlayGridSize
        for (y in -overlayGridSize.toInt()..(height + overlayGridSize).toInt() step overlayGridSize.toInt()) {
            drawLine(
                color = Color.White.copy(alpha = 0.03f),
                start = Offset(0f, y.toFloat() + overlayOffset),
                end = Offset(width, y.toFloat() + overlayOffset),
                strokeWidth = 1f
            )
        }
    }

    private fun DrawScope.renderPlayer(
        player: PlayerState,
        sx: Float,
        sy: Float,
        playerSprite: ImageBitmap? = null
    ) {
        val px = player.x + sx
        val py = player.y + sy

        // Soft drop shadow on ground beneath aircraft
        drawCircle(
            color = Color(0x38000000),
            radius = 38f,
            center = Offset(px, py + 46f)
        )

        rotate(degrees = player.bankAngle, pivot = Offset(px, py)) {
            // Twin engine afterburner plume (pulsating supersonic thrust aligned with nozzles)
            val leftEngineX = px - 10f
            val rightEngineX = px + 10f
            val engineY = py + 36f
            val basePulse = (sin(System.currentTimeMillis() * 0.025).toFloat() * 4f + 16f)

            // Category 7: Upgrade Visuals (+Engine)
            val pulse = if (player.engineLevel >= 2) basePulse * 1.35f else basePulse
            val machColor = if (player.engineLevel >= 3) Color(0xFFA855F7) else Color(0x5500E5FF)

            // Outer mach shock glow
            drawOval(
                color = machColor,
                topLeft = Offset(leftEngineX - 7f, engineY),
                size = Size(14f, pulse + 6f)
            )
            drawOval(
                color = machColor,
                topLeft = Offset(rightEngineX - 7f, engineY),
                size = Size(14f, pulse + 6f)
            )
            // Inner hypersonic core
            drawOval(
                color = if (player.engineLevel >= 3) Color(0xFFC084FC) else Color(0xFFF97316),
                topLeft = Offset(leftEngineX - 4f, engineY + 2f),
                size = Size(8f, pulse)
            )
            drawOval(
                color = if (player.engineLevel >= 3) Color(0xFFC084FC) else Color(0xFFF97316),
                topLeft = Offset(rightEngineX - 4f, engineY + 2f),
                size = Size(8f, pulse)
            )
            // White supersonic center
            drawOval(
                color = Color.White,
                topLeft = Offset(leftEngineX - 2f, engineY + 2f),
                size = Size(4f, pulse * 0.5f)
            )
            drawOval(
                color = Color.White,
                topLeft = Offset(rightEngineX - 2f, engineY + 2f),
                size = Size(4f, pulse * 0.5f)
            )

            // Render Player Aircraft Sprite (or procedural fallback)
            if (playerSprite != null) {
                val spriteSize = 92f
                val dstX = (px - spriteSize / 2f).toInt()
                val dstY = (py - spriteSize / 2f).toInt()
                val isFlashing = player.hitFlashTimer > 0f
                val flashFilter = if (isFlashing) ColorFilter.tint(Color.White, BlendMode.SrcAtop) else null

                drawImage(
                    image = playerSprite,
                    dstOffset = IntOffset(dstX, dstY),
                    dstSize = IntSize(spriteSize.toInt(), spriteSize.toInt()),
                    colorFilter = flashFilter
                )
            } else {
                when (player.aircraftId) {
                    AircraftId.X29_GHOST -> {
                        val wingColor = Color(0xFFE2E8F0)
                        val cockpitColor = Color(0xFF00E5FF)
                        renderPath.reset()
                        renderPath.moveTo(px, py - 38f)
                        renderPath.lineTo(px + 8f, py - 10f)
                        renderPath.lineTo(px + 42f, py - 18f)
                        renderPath.lineTo(px + 36f, py + 8f)
                        renderPath.lineTo(px + 14f, py + 12f)
                        renderPath.lineTo(px + 10f, py + 34f)
                        renderPath.lineTo(px - 10f, py + 34f)
                        renderPath.lineTo(px - 14f, py + 12f)
                        renderPath.lineTo(px - 36f, py + 8f)
                        renderPath.lineTo(px - 42f, py - 18f)
                        renderPath.lineTo(px - 8f, py - 10f)
                        renderPath.close()
                        drawPath(renderPath, wingColor, style = Fill)
                        drawPath(renderPath, Color(0xFF94A3B8), style = Stroke(width = 2f))
                        drawOval(cockpitColor, Offset(px - 5f, py - 24f), Size(10f, 18f))
                    }
                    AircraftId.A10_WARHAWK -> {
                        val hullColor = Color(0xFF475569)
                        renderPath.reset()
                        renderPath.moveTo(px, py - 40f)
                        renderPath.lineTo(px + 12f, py - 16f)
                        renderPath.lineTo(px + 46f, py - 16f)
                        renderPath.lineTo(px + 46f, py + 6f)
                        renderPath.lineTo(px + 16f, py + 8f)
                        renderPath.lineTo(px + 14f, py + 32f)
                        renderPath.lineTo(px - 14f, py + 32f)
                        renderPath.lineTo(px - 16f, py + 8f)
                        renderPath.lineTo(px - 46f, py + 6f)
                        renderPath.lineTo(px - 46f, py - 16f)
                        renderPath.lineTo(px - 12f, py - 16f)
                        renderPath.close()
                        drawPath(renderPath, hullColor, style = Fill)
                        drawPath(renderPath, Color(0xFF1E293B), style = Stroke(width = 2.5f))
                        drawCircle(Color(0xFFEF4444), 4f, Offset(px, py - 41f))
                        drawOval(Color(0xFFFBBF24), Offset(px - 6f, py - 26f), Size(12f, 20f))
                    }
                    AircraftId.F35_SHADOW -> {
                        val stealthColor = Color(0xFF1E293B)
                        renderPath.reset()
                        renderPath.moveTo(px, py - 42f)
                        renderPath.lineTo(px + 14f, py - 14f)
                        renderPath.lineTo(px + 44f, py + 12f)
                        renderPath.lineTo(px + 36f, py + 22f)
                        renderPath.lineTo(px + 14f, py + 18f)
                        renderPath.lineTo(px + 12f, py + 34f)
                        renderPath.lineTo(px - 12f, py + 34f)
                        renderPath.lineTo(px - 14f, py + 18f)
                        renderPath.lineTo(px - 36f, py + 22f)
                        renderPath.lineTo(px - 44f, py + 12f)
                        renderPath.lineTo(px - 14f, py - 14f)
                        renderPath.close()
                        drawPath(renderPath, stealthColor, style = Fill)
                        drawPath(renderPath, Color(0xFF00E5FF), style = Stroke(width = 1.8f))
                        drawOval(Color(0xFF00E5FF), Offset(px - 5f, py - 25f), Size(10f, 18f))
                    }
                    else -> {
                        val alloyColor = Color(0xFFCBD5E1)
                        renderPath.reset()
                        renderPath.moveTo(px, py - 40f)
                        renderPath.lineTo(px + 10f, py - 16f)
                        renderPath.lineTo(px + 42f, py + 14f)
                        renderPath.lineTo(px + 34f, py + 22f)
                        renderPath.lineTo(px + 14f, py + 16f)
                        renderPath.lineTo(px + 12f, py + 36f)
                        renderPath.lineTo(px - 12f, py + 36f)
                        renderPath.lineTo(px - 14f, py + 16f)
                        renderPath.lineTo(px - 34f, py + 22f)
                        renderPath.lineTo(px - 42f, py + 14f)
                        renderPath.lineTo(px - 10f, py - 16f)
                        renderPath.close()
                        drawPath(renderPath, alloyColor, style = Fill)
                        drawPath(renderPath, Color(0xFF64748B), style = Stroke(width = 2f))
                        drawOval(Color(0xFF38BDF8), Offset(px - 6f, py - 26f), Size(12f, 20f))
                    }
                }
            }

            // Category 7: Upgrade Visuals (+Armor)
            if (player.armorLevel >= 2) {
                // Reinforced titanium chine armor plates
                drawRoundRect(
                    color = Color(0xFF475569),
                    topLeft = Offset(px - 20f, py - 6f),
                    size = Size(40f, 20f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                drawCircle(Color(0xFF94A3B8), 1.5f, Offset(px - 16f, py - 2f))
                drawCircle(Color(0xFF94A3B8), 1.5f, Offset(px + 16f, py - 2f))
            }
            if (player.armorLevel >= 3) {
                // Heavy ceramic composite hull plating
                drawRoundRect(
                    color = Color(0xFF0F172A),
                    topLeft = Offset(px - 12f, py - 16f),
                    size = Size(24f, 14f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
                drawCircle(Color(0xFFFBBF24), 1.5f, Offset(px - 8f, py - 10f))
                drawCircle(Color(0xFFFBBF24), 1.5f, Offset(px + 8f, py - 10f))
            }

            // Category 7: Upgrade Visuals (+Cannons)
            if (player.cannonLevel >= 2) {
                // Dual external 25mm gun pods under wings
                drawRoundRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(px - 28f, py - 10f),
                    size = Size(5f, 18f),
                    cornerRadius = CornerRadius(2f, 2f)
                )
                drawRoundRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(px + 23f, py - 10f),
                    size = Size(5f, 18f),
                    cornerRadius = CornerRadius(2f, 2f)
                )
                drawRect(Color(0xFF94A3B8), Offset(px - 27f, py - 16f), Size(3f, 6f))
                drawRect(Color(0xFF94A3B8), Offset(px + 24f, py - 16f), Size(3f, 6f))
            }
            if (player.cannonLevel >= 3) {
                // Quad heavy rotary barrels
                drawRect(Color(0xFFFBBF24), Offset(px - 28f, py - 18f), Size(5f, 3f))
                drawRect(Color(0xFFFBBF24), Offset(px + 23f, py - 18f), Size(5f, 3f))
            }

            // Category 7: Upgrade Visuals (+Missiles)
            if (player.secondaryAmmo > 0) {
                // Wingtip missiles (Level 1+)
                drawRect(Color(0xFFEF4444), Offset(px - 36f, py + 4f), Size(4f, 14f))
                drawRect(Color(0xFFEF4444), Offset(px + 32f, py + 4f), Size(4f, 14f))

                // Mid-wing missiles (Level 2+)
                if (player.missileLevel >= 2) {
                    drawRect(Color(0xFFF97316), Offset(px - 22f, py + 8f), Size(4f, 14f))
                    drawRect(Color(0xFFF97316), Offset(px + 18f, py + 8f), Size(4f, 14f))
                }
                // Underwing heavy missiles (Level 3+)
                if (player.missileLevel >= 3) {
                    drawRect(Color(0xFFEF4444), Offset(px - 14f, py + 12f), Size(5f, 16f))
                    drawRect(Color(0xFFEF4444), Offset(px + 9f, py + 12f), Size(5f, 16f))
                    drawCircle(Color.White, 2f, Offset(px - 11.5f, py + 12f))
                    drawCircle(Color.White, 2f, Offset(px + 11.5f, py + 12f))
                }
            }

            // Category 6: Weapon Visuals (Cannon Muzzle Flash)
            if (player.muzzleFlashTimer > 0f) {
                val flashPulse = (player.muzzleFlashTimer / 0.08f).coerceIn(0f, 1f)
                val flashSize = 13f * flashPulse + 5f
                // Twin primary muzzle flashes
                drawCircle(Color(0xFFFEF08A), flashSize, Offset(px - 14f, py - 38f))
                drawCircle(Color.White, flashSize * 0.5f, Offset(px - 14f, py - 38f))
                drawCircle(Color(0xFFFEF08A), flashSize, Offset(px + 14f, py - 38f))
                drawCircle(Color.White, flashSize * 0.5f, Offset(px + 14f, py - 38f))

                if (player.cannonLevel >= 2) {
                    drawCircle(Color(0xFFF97316), flashSize * 0.8f, Offset(px - 25.5f, py - 18f))
                    drawCircle(Color(0xFFF97316), flashSize * 0.8f, Offset(px + 25.5f, py - 18f))
                }
            }

            // Category 5: Visual Damage States for Player
            val hpRatio = (player.hp / player.maxHp).coerceIn(0f, 1f)
            val animTime = System.currentTimeMillis() * 0.006f

            // 75% = Light smoke
            if (hpRatio < 0.75f) {
                val smokeAlpha = ((0.75f - hpRatio) * 1.2f).coerceIn(0.12f, 0.45f)
                drawCircle(Color(0xFF94A3B8).copy(alpha = smokeAlpha), 13f, Offset(px - 6f + sin(animTime) * 6f, py + 38f))
                drawCircle(Color(0xFF94A3B8).copy(alpha = smokeAlpha * 0.7f), 17f, Offset(px - 9f + cos(animTime) * 8f, py + 56f))
            }
            // 50% = Sparks + smoke
            if (hpRatio < 0.50f) {
                drawCircle(Color(0xFF64748B).copy(alpha = 0.55f), 16f, Offset(px + 8f, py + 34f))
                drawCircle(Color(0xFFFBBF24), 3.5f, Offset(px + 10f + sin(animTime * 3f) * 12f, py + 26f + cos(animTime * 3f) * 12f))
                drawCircle(Color(0xFFF97316), 2.5f, Offset(px - 12f + cos(animTime * 4f) * 10f, py + 22f + sin(animTime * 4f) * 10f))
            }
            // 25% = Heavy smoke + engine damage
            if (hpRatio < 0.25f) {
                drawCircle(Color(0xFF1E293B).copy(alpha = 0.8f), 22f, Offset(px + sin(animTime * 2f) * 8f, py + 44f))
                drawCircle(Color(0xFF0F172A).copy(alpha = 0.85f), 26f, Offset(px + cos(animTime * 1.8f) * 12f, py + 68f))
                drawOval(Color(0xFFEA580C), Offset(px - 6f, py + 32f), Size(12f, 16f)) // Sputtering engine flame
            }
            // 10% = Fire + critical damage
            if (hpRatio < 0.10f) {
                drawCircle(Color(0xFFEF4444), 19f, Offset(px, py + 24f))
                drawCircle(Color(0xFFFBBF24), 13f, Offset(px, py + 24f))
                drawCircle(Color.White, 6f, Offset(px, py + 22f))
                drawCircle(Color(0xFF020617).copy(alpha = 0.92f), 30f, Offset(px + sin(animTime * 3f) * 14f, py + 52f))
            }

            // Shield bubble glow if active
            if (player.shield > 0) {
                val shieldAlpha = (player.shield / player.maxShield * 0.35f).coerceIn(0.1f, 0.4f)
                drawCircle(
                    color = Color(0xFF38BDF8).copy(alpha = shieldAlpha),
                    radius = 48f,
                    center = Offset(px, py)
                )
                drawCircle(
                    color = Color(0xFF38BDF8).copy(alpha = shieldAlpha * 1.5f),
                    radius = 48f,
                    center = Offset(px, py),
                    style = Stroke(width = 1.5f)
                )
            }
        }
    }

    private fun DrawScope.renderWingman(player: PlayerState, sx: Float, sy: Float) {
        val wx = player.x + 55f + sx
        val wy = player.y + 20f + sy
        val wingColor = when (player.wingmanId) {
            WingmanId.ATTACK_VIPER -> Color(0xFFEF4444)
            WingmanId.DEFENSIVE_AEGIS -> Color(0xFF38BDF8)
            WingmanId.RECON_HAWK -> Color(0xFFFBBF24)
            WingmanId.DRONE_SWARM -> Color(0xFFA855F7)
            else -> Color.White
        }

        // Mini wingman fighter silhouette
        renderPath.reset()
        renderPath.moveTo(wx, wy - 18f)
        renderPath.lineTo(wx + 16f, wy + 8f)
        renderPath.lineTo(wx + 6f, wy + 6f)
        renderPath.lineTo(wx + 5f, wy + 14f)
        renderPath.lineTo(wx - 5f, wy + 14f)
        renderPath.lineTo(wx - 6f, wy + 6f)
        renderPath.lineTo(wx - 16f, wy + 8f)
        renderPath.close()
        drawPath(renderPath, wingColor, style = Fill)
        drawPath(renderPath, Color.White, style = Stroke(width = 1f))
    }

    private fun DrawScope.renderAirEnemy(
        e: Enemy,
        sx: Float,
        sy: Float,
        enemySprites: EnemySpritePack? = null
    ) {
        val ex = e.x + sx
        val ey = e.y + sy

        val bankAngle = (e.vx * 3.2f).coerceIn(-18f, 18f)
        val isFlashing = e.hitFlashTimer > 0f
        val hitFilter = if (isFlashing) ColorFilter.tint(Color.White, BlendMode.SrcAtop) else null

        when (e.category) {
            EnemyCategory.AIR_LIGHT_INTERCEPTOR, EnemyCategory.AIR_INTERCEPTOR -> {
                val sprite = enemySprites?.lightInterceptor ?: enemySprites?.interceptor
                if (sprite != null) {
                    drawCircle(color = Color(0x35000000), radius = 28f, center = Offset(ex, ey + 34f))
                    rotate(degrees = bankAngle, pivot = Offset(ex, ey)) {
                        // High-speed Mach-3 amber exhaust plume
                        val pulse = (sin(System.currentTimeMillis() * 0.04 + e.id).toFloat() * 4f + 14f)
                        drawOval(Color(0xFFEA580C), Offset(ex - 5f, ey - 36f - pulse), Size(10f, pulse))
                        drawOval(Color(0xFFFEF08A), Offset(ex - 2f, ey - 32f - pulse * 0.6f), Size(4f, pulse * 0.6f))

                        val size = 70f
                        drawImage(
                            image = sprite,
                            dstOffset = IntOffset((ex - size / 2f).toInt(), (ey - size / 2f).toInt()),
                            dstSize = IntSize(size.toInt(), size.toInt()),
                            colorFilter = hitFilter
                        )
                    }
                } else {
                    // Needle jet fallback
                    renderPath.reset()
                    renderPath.moveTo(ex, ey + 30f)
                    renderPath.lineTo(ex + 22f, ey - 12f)
                    renderPath.lineTo(ex + 6f, ey - 8f)
                    renderPath.lineTo(ex + 8f, ey - 22f)
                    renderPath.lineTo(ex - 8f, ey - 22f)
                    renderPath.lineTo(ex - 6f, ey - 8f)
                    renderPath.lineTo(ex - 22f, ey - 12f)
                    renderPath.close()
                    drawPath(renderPath, Color(0xFFEA580C), style = Fill)
                    drawPath(renderPath, Color(0xFF7C2D12), style = Stroke(width = 1.5f))
                }
            }
            EnemyCategory.AIR_HEAVY_FIGHTER, EnemyCategory.AIR_GUNSHIP -> {
                val sprite = enemySprites?.heavyFighter ?: enemySprites?.gunship
                if (sprite != null) {
                    drawCircle(color = Color(0x38000000), radius = 38f, center = Offset(ex, ey + 36f))
                    rotate(degrees = bankAngle * 0.7f, pivot = Offset(ex, ey)) {
                        val pulse = (sin(System.currentTimeMillis() * 0.03 + e.id).toFloat() * 3f + 12f)
                        drawOval(Color(0xFFF97316), Offset(ex - 14f, ey - 42f - pulse), Size(7f, pulse))
                        drawOval(Color(0xFFF97316), Offset(ex + 7f, ey - 42f - pulse), Size(7f, pulse))

                        val size = 88f
                        drawImage(
                            image = sprite,
                            dstOffset = IntOffset((ex - size / 2f).toInt(), (ey - size / 2f).toInt()),
                            dstSize = IntSize(size.toInt(), size.toInt()),
                            colorFilter = hitFilter
                        )
                    }
                } else {
                    drawOval(Color(0xFF334155), Offset(ex - 18f, ey - 32f), Size(36f, 64f))
                    drawRect(Color(0xFF475569), Offset(ex - 48f, ey - 6f), Size(96f, 14f))
                }
            }
            EnemyCategory.AIR_STRIKE_AIRCRAFT, EnemyCategory.AIR_SCOUT -> {
                val sprite = enemySprites?.strikeAircraft ?: enemySprites?.scout
                if (sprite != null) {
                    drawCircle(color = Color(0x35000000), radius = 26f, center = Offset(ex, ey + 32f))
                    rotate(degrees = bankAngle, pivot = Offset(ex, ey)) {
                        val pulse = (sin(System.currentTimeMillis() * 0.03 + e.id).toFloat() * 3f + 10f)
                        drawOval(Color(0xFFF97316), Offset(ex - 8f, ey - 34f - pulse), Size(5f, pulse))
                        drawOval(Color(0xFFF97316), Offset(ex + 3f, ey - 34f - pulse), Size(5f, pulse))

                        val size = 72f
                        drawImage(
                            image = sprite,
                            dstOffset = IntOffset((ex - size / 2f).toInt(), (ey - size / 2f).toInt()),
                            dstSize = IntSize(size.toInt(), size.toInt()),
                            colorFilter = hitFilter
                        )
                    }
                } else {
                    renderPath.reset()
                    renderPath.moveTo(ex, ey + 22f)
                    renderPath.lineTo(ex + 20f, ey - 10f)
                    renderPath.lineTo(ex - 20f, ey - 10f)
                    renderPath.close()
                    drawPath(renderPath, Color(0xFFDC2626), style = Fill)
                }
            }
            EnemyCategory.AIR_BOMBER, EnemyCategory.AIR_STEALTH_BOMBER -> {
                val sprite = enemySprites?.bomber
                if (sprite != null) {
                    drawCircle(color = Color(0x40000000), radius = 46f, center = Offset(ex, ey + 42f))
                    rotate(degrees = bankAngle * 0.5f, pivot = Offset(ex, ey)) {
                        val pulse = (sin(System.currentTimeMillis() * 0.025 + e.id).toFloat() * 3f + 10f)
                        drawOval(Color(0xFFEA580C), Offset(ex - 20f, ey - 48f - pulse), Size(6f, pulse))
                        drawOval(Color(0xFFEA580C), Offset(ex + 14f, ey - 48f - pulse), Size(6f, pulse))

                        val size = 110f
                        drawImage(
                            image = sprite,
                            dstOffset = IntOffset((ex - size / 2f).toInt(), (ey - size / 2f).toInt()),
                            dstSize = IntSize(size.toInt(), size.toInt()),
                            colorFilter = hitFilter
                        )
                    }
                } else {
                    drawOval(Color(0xFF1E293B), Offset(ex - 50f, ey - 25f), Size(100f, 50f))
                }
            }
            EnemyCategory.AIR_ATTACK_HELO -> {
                val sprite = enemySprites?.attackHelo
                if (sprite != null) {
                    drawCircle(color = Color(0x35000000), radius = 28f, center = Offset(ex, ey + 36f))
                    rotate(degrees = bankAngle * 0.8f, pivot = Offset(ex, ey)) {
                        val size = 78f
                        drawImage(
                            image = sprite,
                            dstOffset = IntOffset((ex - size / 2f).toInt(), (ey - size / 2f).toInt()),
                            dstSize = IntSize(size.toInt(), size.toInt()),
                            colorFilter = hitFilter
                        )
                        // Animated spinning rotor disc blur
                        val rotorAngle = (System.currentTimeMillis() * 0.8f) % 360f
                        rotate(degrees = rotorAngle, pivot = Offset(ex, ey - 10f)) {
                            drawLine(Color(0x99CBD5E1), Offset(ex - 42f, ey - 10f), Offset(ex + 42f, ey - 10f), strokeWidth = 3f)
                            drawLine(Color(0x99CBD5E1), Offset(ex, ey - 52f), Offset(ex, ey + 32f), strokeWidth = 3f)
                            drawCircle(Color(0x33CBD5E1), 40f, Offset(ex, ey - 10f), style = Stroke(width = 2f))
                        }
                    }
                } else {
                    drawOval(Color(0xFF1E293B), Offset(ex - 14f, ey - 24f), Size(28f, 48f))
                }
            }
            EnemyCategory.AIR_STEALTH_AIRCRAFT -> {
                val sprite = enemySprites?.stealthAircraft
                if (sprite != null) {
                    drawCircle(color = Color(0x35000000), radius = 30f, center = Offset(ex, ey + 34f))
                    rotate(degrees = bankAngle, pivot = Offset(ex, ey)) {
                        val pulse = (sin(System.currentTimeMillis() * 0.035 + e.id).toFloat() * 3f + 12f)
                        drawOval(Color(0xFF7C3AED), Offset(ex - 4f, ey - 36f - pulse), Size(8f, pulse))

                        val size = 76f
                        drawImage(
                            image = sprite,
                            dstOffset = IntOffset((ex - size / 2f).toInt(), (ey - size / 2f).toInt()),
                            dstSize = IntSize(size.toInt(), size.toInt()),
                            colorFilter = hitFilter
                        )
                    }
                } else {
                    drawOval(Color(0xFF0F172A), Offset(ex - 25f, ey - 25f), Size(50f, 50f))
                }
            }
            EnemyCategory.AIR_SWARM_DRONE, EnemyCategory.AIR_DRONE -> {
                val sprite = enemySprites?.swarmDrone
                if (sprite != null) {
                    drawCircle(color = Color(0x30000000), radius = 16f, center = Offset(ex, ey + 20f))
                    rotate(degrees = bankAngle * 1.2f, pivot = Offset(ex, ey)) {
                        val pulse = (sin(System.currentTimeMillis() * 0.05 + e.id).toFloat() * 2f + 6f)
                        drawOval(Color(0xFFEF4444), Offset(ex - 2f, ey - 20f - pulse), Size(4f, pulse))

                        val size = 44f
                        drawImage(
                            image = sprite,
                            dstOffset = IntOffset((ex - size / 2f).toInt(), (ey - size / 2f).toInt()),
                            dstSize = IntSize(size.toInt(), size.toInt()),
                            colorFilter = hitFilter
                        )
                    }
                } else {
                    drawCircle(Color(0xFFA855F7), 12f, Offset(ex, ey))
                    drawCircle(Color(0xFFEF4444), 4f, Offset(ex, ey))
                }
            }
            EnemyCategory.AIR_ACE_RAZOR -> {
                val sprite = enemySprites?.aceRazor ?: enemySprites?.ace
                if (sprite != null) {
                    drawCircle(color = Color(0x40000000), radius = 36f, center = Offset(ex, ey + 38f))
                    rotate(degrees = bankAngle, pivot = Offset(ex, ey)) {
                        val pulse = (sin(System.currentTimeMillis() * 0.035 + e.id).toFloat() * 4f + 14f)
                        drawOval(Color(0xFFA855F7), Offset(ex - 12f, ey - 38f - pulse), Size(7f, pulse))
                        drawOval(Color(0xFFA855F7), Offset(ex + 5f, ey - 38f - pulse), Size(7f, pulse))

                        val size = 86f
                        drawImage(
                            image = sprite,
                            dstOffset = IntOffset((ex - size / 2f).toInt(), (ey - size / 2f).toInt()),
                            dstSize = IntSize(size.toInt(), size.toInt()),
                            colorFilter = hitFilter
                        )
                    }
                    drawCircle(Color(0xFFFBBF24), 5f, Offset(ex, ey - 44f))
                } else {
                    drawCircle(Color(0xFF991B1B), 25f, Offset(ex, ey))
                }
            }
            EnemyCategory.AIR_ACE_BLACKOUT -> {
                val sprite = enemySprites?.stealthAircraft ?: enemySprites?.ace
                if (sprite != null) {
                    drawCircle(color = Color(0x40000000), radius = 36f, center = Offset(ex, ey + 38f))
                    rotate(degrees = bankAngle, pivot = Offset(ex, ey)) {
                        val pulse = (sin(System.currentTimeMillis() * 0.035 + e.id).toFloat() * 4f + 14f)
                        drawOval(Color(0xFF7C3AED), Offset(ex - 12f, ey - 38f - pulse), Size(7f, pulse))
                        drawOval(Color(0xFF7C3AED), Offset(ex + 5f, ey - 38f - pulse), Size(7f, pulse))

                        val size = 86f
                        val customFilter = if (isFlashing) hitFilter else ColorFilter.tint(Color(0xFF4C1D95), BlendMode.Modulate)
                        drawImage(
                            image = sprite,
                            dstOffset = IntOffset((ex - size / 2f).toInt(), (ey - size / 2f).toInt()),
                            dstSize = IntSize(size.toInt(), size.toInt()),
                            colorFilter = customFilter
                        )
                    }
                    drawCircle(Color(0xFFA855F7), 5f, Offset(ex, ey - 44f))
                }
            }
            EnemyCategory.BOSS_GOLIATH_CORE -> {
                val sprite = enemySprites?.bossAirborneFortress
                if (sprite != null) {
                    // Category 4: Boss 10 Airborne Fortress
                    drawCircle(Color(0x55000000), 130f, Offset(ex, ey + 60f))
                    val size = 320f
                    drawImage(
                        image = sprite,
                        dstOffset = IntOffset((ex - size / 2f).toInt(), (ey - size / 2f).toInt()),
                        dstSize = IntSize(size.toInt(), size.toInt()),
                        colorFilter = hitFilter
                    )
                    // Pulsing central plasma reactor core
                    val reactorPulse = (sin(System.currentTimeMillis() * 0.008f).toFloat() * 6f + 24f)
                    drawCircle(Color(0x889333EA), reactorPulse + 8f, Offset(ex, ey))
                    drawCircle(Color(0xFF9333EA), reactorPulse, Offset(ex, ey))
                    drawCircle(Color(0xFFC084FC), reactorPulse * 0.6f, Offset(ex, ey))
                    drawCircle(Color.White, reactorPulse * 0.3f, Offset(ex, ey))
                } else {
                    drawOval(Color(0xFF1E293B), Offset(ex - 140f, ey - 60f), Size(280f, 120f))
                    drawRect(Color(0xFF334155), Offset(ex - 190f, ey - 20f), Size(380f, 40f))
                    drawCircle(Color(0xFF9333EA), 28f, Offset(ex, ey))
                }
            }
            EnemyCategory.BOSS_TURRET_LEFT, EnemyCategory.BOSS_TURRET_RIGHT -> {
                drawCircle(Color(0xFF475569), 20f, Offset(ex, ey))
                drawCircle(Color(0xFFEF4444), 8f, Offset(ex, ey))
                drawLine(Color(0xFF94A3B8), Offset(ex, ey), Offset(ex, ey + 18f), strokeWidth = 4f)
            }
            EnemyCategory.BOSS_MISSILE_BAY -> {
                drawRect(Color(0xFF475569), Offset(ex - 32f, ey - 16f), Size(64f, 32f))
                drawRect(Color(0xFFEF4444), Offset(ex - 22f, ey - 10f), Size(44f, 20f))
                drawCircle(Color(0xFFFBBF24), 5f, Offset(ex, ey))
            }
            EnemyCategory.BOSS_ENGINE_LEFT, EnemyCategory.BOSS_ENGINE_RIGHT -> {
                drawOval(Color(0xFF334155), Offset(ex - 18f, ey - 26f), Size(36f, 52f))
                val flamePulse = (sin(System.currentTimeMillis() * 0.04f + e.id).toFloat() * 6f + 16f)
                drawOval(Color(0xFFF97316), Offset(ex - 8f, ey - 26f - flamePulse), Size(16f, flamePulse))
                drawOval(Color.White, Offset(ex - 4f, ey - 24f - flamePulse * 0.5f), Size(8f, flamePulse * 0.5f))
            }
            else -> {}
        }

        // Category 5: Visual Damage Feedback for Enemies (trailing smoke / sparks)
        val enemyHpRatio = (e.hp / e.maxHp).coerceIn(0f, 1f)
        if (enemyHpRatio < 0.5f) {
            val smokeAlpha = (0.5f - enemyHpRatio) * 1.5f
            drawCircle(Color(0xFF64748B).copy(alpha = smokeAlpha.coerceIn(0.1f, 0.6f)), 10f, Offset(ex + sin(e.patternTimer * 6f) * 4f, ey - 32f))
        }
        if (enemyHpRatio < 0.25f) {
            drawCircle(Color(0xFF1E293B).copy(alpha = 0.8f), 14f, Offset(ex, ey - 38f))
            drawCircle(Color(0xFFF97316), 3f, Offset(ex + cos(e.patternTimer * 8f) * 8f, ey - 28f))
        }

        // Enemy Health Bar (if damaged or boss)
        if (e.hp < e.maxHp || e.maxHp >= 200f) {
            val barW = if (e.category == EnemyCategory.BOSS_GOLIATH_CORE) 180f else 44f
            val barH = if (e.category == EnemyCategory.BOSS_GOLIATH_CORE) 8f else 5f
            val ratio = (e.hp / e.maxHp).coerceIn(0f, 1f)
            val barY = if (e.category == EnemyCategory.BOSS_GOLIATH_CORE) ey - 140f else ey - 35f
            drawRect(Color(0x99000000), Offset(ex - barW / 2f, barY), Size(barW, barH))
            drawRect(
                if (ratio > 0.4f) Color(0xFF22C55E) else Color(0xFFEF4444),
                Offset(ex - barW / 2f, barY),
                Size(barW * ratio, barH)
            )
        }
    }

    private fun DrawScope.renderGroundTarget(
        e: Enemy,
        sx: Float,
        sy: Float,
        enemySprites: EnemySpritePack? = null
    ) {
        val gx = e.x + sx
        val gy = e.y + sy

        when (e.category) {
            EnemyCategory.GROUND_HANGAR -> {
                val sprite = enemySprites?.groundHangar
                if (sprite != null) {
                    val size = 110f
                    drawImage(
                        image = sprite,
                        dstOffset = IntOffset((gx - size / 2f).toInt(), (gy - size / 2f).toInt()),
                        dstSize = IntSize(size.toInt(), size.toInt())
                    )
                } else {
                    drawRect(Color(0xFF475569), Offset(gx - 45f, gy - 35f), Size(90f, 70f))
                }
            }
            EnemyCategory.GROUND_RADAR -> {
                val sprite = enemySprites?.groundRadar
                if (sprite != null) {
                    val size = 95f
                    drawImage(
                        image = sprite,
                        dstOffset = IntOffset((gx - size / 2f).toInt(), (gy - size / 2f).toInt()),
                        dstSize = IntSize(size.toInt(), size.toInt())
                    )
                    // Rotating radar microwave sweep beam
                    val angle = e.patternTimer * 3.5f
                    drawLine(
                        color = Color(0xFF38BDF8).copy(alpha = 0.7f),
                        start = Offset(gx, gy),
                        end = Offset(gx + cos(angle) * 36f, gy + sin(angle) * 36f),
                        strokeWidth = 2.5f
                    )
                } else {
                    drawCircle(Color(0xFF334155), 24f, Offset(gx, gy), style = Stroke(width = 2f))
                    val angle = e.patternTimer * 4f
                    drawLine(Color(0xFF38BDF8), Offset(gx, gy), Offset(gx + cos(angle) * 22f, gy + sin(angle) * 22f), strokeWidth = 3f)
                }
            }
            EnemyCategory.GROUND_BRIDGE -> {
                val sprite = enemySprites?.groundBridge
                if (sprite != null) {
                    val w = 220f
                    val h = 110f
                    drawImage(
                        image = sprite,
                        dstOffset = IntOffset((gx - w / 2f).toInt(), (gy - h / 2f).toInt()),
                        dstSize = IntSize(w.toInt(), h.toInt())
                    )
                } else {
                    drawRect(Color(0xFF57534E), Offset(gx - 100f, gy - 24f), Size(200f, 48f))
                }
            }
            EnemyCategory.NAVAL_CRUISER -> {
                val sprite = enemySprites?.navalCruiser
                if (sprite != null) {
                    // Foamy water wake trails
                    val wakePulse = (sin(System.currentTimeMillis() * 0.015f).toFloat() * 4f + 16f)
                    drawLine(Color(0x55E0F2FE), Offset(gx - 28f, gy - 80f), Offset(gx - 45f - wakePulse, gy + 100f), strokeWidth = 3f)
                    drawLine(Color(0x55E0F2FE), Offset(gx + 28f, gy - 80f), Offset(gx + 45f + wakePulse, gy + 100f), strokeWidth = 3f)

                    val w = 110f
                    val h = 230f
                    drawImage(
                        image = sprite,
                        dstOffset = IntOffset((gx - w / 2f).toInt(), (gy - h / 2f).toInt()),
                        dstSize = IntSize(w.toInt(), h.toInt())
                    )
                } else {
                    renderPath.reset()
                    renderPath.moveTo(gx, gy - 60f)
                    renderPath.lineTo(gx + 24f, gy - 30f)
                    renderPath.lineTo(gx + 24f, gy + 60f)
                    renderPath.lineTo(gx - 24f, gy + 60f)
                    renderPath.lineTo(gx - 24f, gy - 30f)
                    renderPath.close()
                    drawPath(renderPath, Color(0xFF334155), style = Fill)
                }
            }
            EnemyCategory.GROUND_MILITARY_BASE -> {
                val sprite = enemySprites?.groundMilitaryBase
                if (sprite != null) {
                    val size = 150f
                    drawImage(
                        image = sprite,
                        dstOffset = IntOffset((gx - size / 2f).toInt(), (gy - size / 2f).toInt()),
                        dstSize = IntSize(size.toInt(), size.toInt())
                    )
                } else {
                    drawRect(Color(0xFF334155), Offset(gx - 60f, gy - 60f), Size(120f, 120f))
                }
            }
            EnemyCategory.GROUND_VEHICLE_CONVOY -> {
                val sprite = enemySprites?.groundVehicleConvoy
                if (sprite != null) {
                    val w = 90f
                    val h = 160f
                    drawImage(
                        image = sprite,
                        dstOffset = IntOffset((gx - w / 2f).toInt(), (gy - h / 2f).toInt()),
                        dstSize = IntSize(w.toInt(), h.toInt())
                    )
                } else {
                    drawRect(Color(0xFF15803D), Offset(gx - 20f, gy - 50f), Size(40f, 100f))
                }
            }
            EnemyCategory.GROUND_SAM -> {
                drawRect(Color(0xFF334155), Offset(gx - 20f, gy - 20f), Size(40f, 40f))
                drawRect(Color(0xFFF1F5F9), Offset(gx - 14f, gy - 24f), Size(8f, 32f))
                drawRect(Color(0xFFF1F5F9), Offset(gx + 6f, gy - 24f), Size(8f, 32f))
                drawRect(Color(0xFFEF4444), Offset(gx - 14f, gy - 28f), Size(8f, 6f))
                drawRect(Color(0xFFEF4444), Offset(gx + 6f, gy - 28f), Size(8f, 6f))
            }
            EnemyCategory.GROUND_FLAK -> {
                drawCircle(Color(0xFF475569), 18f, Offset(gx, gy))
                drawLine(Color(0xFF94A3B8), Offset(gx - 6f, gy), Offset(gx - 6f, gy - 24f), strokeWidth = 5f)
                drawLine(Color(0xFF94A3B8), Offset(gx + 6f, gy), Offset(gx + 6f, gy - 24f), strokeWidth = 5f)
            }
            EnemyCategory.GROUND_FUEL_DEPOT -> {
                drawCircle(Color(0xFFB45309), 26f, Offset(gx, gy))
                drawCircle(Color(0xFFD97706), 20f, Offset(gx, gy))
                drawCircle(Color(0xFFEF4444), 6f, Offset(gx, gy))
            }
            else -> {}
        }

        // Ground Target Health Bar
        if (e.hp < e.maxHp) {
            val barW = 50f
            val barH = 5f
            val ratio = (e.hp / e.maxHp).coerceIn(0f, 1f)
            drawRect(Color(0x99000000), Offset(gx - barW / 2f, gy - 45f), Size(barW, barH))
            drawRect(Color(0xFFF97316), Offset(gx - barW / 2f, gy - 45f), Size(barW * ratio, barH))
        }
    }

    private fun DrawScope.renderFriendly(f: FriendlyUnit, sx: Float, sy: Float) {
        val fx = f.x + sx
        val fy = f.y + sy

        if (f.isExtracting) {
            // Allied Rescue Helicopter
            drawOval(Color(0xFF0284C7), Offset(fx - 16f, fy - 26f), Size(32f, 52f))
            drawCircle(Color(0xFF38BDF8), 30f, Offset(fx, fy), style = Stroke(width = 1.5f))
        } else {
            // Supply truck convoy
            drawRect(Color(0xFF15803D), Offset(fx - 14f, fy - 22f), Size(28f, 44f))
            drawCircle(Color(0xFF4ADE80), 4f, Offset(fx, fy - 14f))
        }
    }

    private fun DrawScope.renderMissile(m: Missile, sx: Float, sy: Float) {
        val mx = m.x + sx
        val my = m.y + sy
        val angle = atan2(m.vy, m.vx) * (180f / 3.14159f) + 90f

        // Category 6: Weapon Visuals (Missile Smoke Trail)
        val trailLength = 16f
        val radAngle = (angle - 90f) * (3.14159f / 180f)
        val tailX = mx - cos(radAngle) * trailLength
        val tailY = my - sin(radAngle) * trailLength
        drawLine(
            color = Color.White.copy(alpha = 0.45f),
            start = Offset(mx, my),
            end = Offset(tailX, tailY),
            strokeWidth = 3.5f
        )
        drawLine(
            color = Color(0xFFF97316).copy(alpha = 0.8f),
            start = Offset(mx, my),
            end = Offset(mx - cos(radAngle) * 8f, my - sin(radAngle) * 8f),
            strokeWidth = 2.5f
        )

        rotate(degrees = angle, pivot = Offset(mx, my)) {
            // Sleek aerodynamic missile body with guidance fins
            drawRoundRect(
                color = if (m.isPlayer) Color(0xFFF1F5F9) else Color(0xFFEF4444),
                topLeft = Offset(mx - 2.5f, my - 10f),
                size = Size(5f, 20f),
                cornerRadius = CornerRadius(1.5f, 1.5f)
            )
            // Rear fins
            drawLine(
                color = if (m.isPlayer) Color(0xFF94A3B8) else Color(0xFF991B1B),
                start = Offset(mx - 5f, my + 8f),
                end = Offset(mx + 5f, my + 8f),
                strokeWidth = 2f
            )
            // Rocket motor flare
            drawCircle(Color(0xFFF97316), 4f, Offset(mx, my + 10f))
            drawCircle(Color.White, 2f, Offset(mx, my + 10f))
        }
    }

    private fun DrawScope.renderRocket(r: Rocket, sx: Float, sy: Float) {
        val rx = r.x + sx
        val ry = r.y + sy
        val angle = atan2(r.vy, r.vx) * (180f / 3.14159f) + 90f

        // Category 6: Rocket smoke & fire trail
        val trailLength = 18f
        val radAngle = (angle - 90f) * (3.14159f / 180f)
        drawLine(
            color = Color(0xFFFBBF24).copy(alpha = 0.7f),
            start = Offset(rx, ry),
            end = Offset(rx - cos(radAngle) * trailLength, ry - sin(radAngle) * trailLength),
            strokeWidth = 4f
        )
        rotate(degrees = angle, pivot = Offset(rx, ry)) {
            drawRoundRect(
                color = Color(0xFF38BDF8),
                topLeft = Offset(rx - 3f, ry - 8f),
                size = Size(6f, 16f),
                cornerRadius = CornerRadius(2f, 2f)
            )
            drawCircle(Color(0xFFFEF08A), 3.5f, Offset(rx, ry + 8f))
        }
    }

    private fun DrawScope.renderBomb(b: Bomb, sx: Float, sy: Float) {
        val bx = b.x + sx
        val by = b.y + sy
        val radius = 9f * b.scale

        // Category 6: Ground impact targeting crosshair
        drawCircle(
            color = Color(0xFFEF4444).copy(alpha = 0.35f),
            radius = 24f,
            center = Offset(bx, by),
            style = Stroke(width = 1.5f)
        )
        drawLine(Color(0xFFEF4444).copy(alpha = 0.5f), Offset(bx - 30f, by), Offset(bx + 30f, by), strokeWidth = 1f)
        drawLine(Color(0xFFEF4444).copy(alpha = 0.5f), Offset(bx, by - 30f), Offset(bx, by + 30f), strokeWidth = 1f)

        // Heavy ordnance body
        drawCircle(Color(0xFF1E293B), radius, Offset(bx, by))
        drawCircle(Color(0xFFFBBF24), radius * 0.5f, Offset(bx, by))
    }

    private fun DrawScope.renderPowerUp(p: PowerUp, sx: Float, sy: Float) {
        val px = p.x + sx
        val py = p.y + sy

        // Ambient pulse glow
        val color = p.type.color
        drawCircle(color.copy(alpha = 0.22f), 24f, Offset(px, py))

        // Chamfered military crate / pod body
        val crateSize = 28f
        val halfS = crateSize / 2f
        drawRoundRect(
            color = Color(0xFF0F172A),
            topLeft = Offset(px - halfS, py - halfS),
            size = Size(crateSize, crateSize),
            cornerRadius = CornerRadius(6f, 6f)
        )
        // Glowing tactical border
        drawRoundRect(
            color = color,
            topLeft = Offset(px - halfS, py - halfS),
            size = Size(crateSize, crateSize),
            cornerRadius = CornerRadius(6f, 6f),
            style = Stroke(width = 2f)
        )

        // Crisp tactical vector icons based on all 8 PowerUpTypes
        when (p.type) {
            PowerUpType.DOUBLE_DAMAGE -> {
                // Twin chevron wings
                drawLine(color, Offset(px - 7f, py + 4f), Offset(px, py - 4f), strokeWidth = 2.5f)
                drawLine(color, Offset(px + 7f, py + 4f), Offset(px, py - 4f), strokeWidth = 2.5f)
                drawLine(Color.White, Offset(px - 5f, py + 8f), Offset(px, py + 1f), strokeWidth = 2f)
                drawLine(Color.White, Offset(px + 5f, py + 8f), Offset(px, py + 1f), strokeWidth = 2f)
            }
            PowerUpType.RAPID_FIRE -> {
                // Triple burst fire lightning
                drawLine(color, Offset(px - 6f, py + 5f), Offset(px - 2f, py - 6f), strokeWidth = 2.2f)
                drawLine(Color.White, Offset(px, py + 6f), Offset(px + 2f, py - 7f), strokeWidth = 2.5f)
                drawLine(color, Offset(px + 6f, py + 5f), Offset(px + 4f, py - 6f), strokeWidth = 2.2f)
            }
            PowerUpType.UNLIMITED_MISSILES, PowerUpType.MISSILE_RESTOCK -> {
                // Twin sleek missile silhouettes
                drawLine(color, Offset(px - 4f, py + 7f), Offset(px - 4f, py - 5f), strokeWidth = 2.5f)
                drawLine(Color.White, Offset(px - 4f, py - 5f), Offset(px - 4f, py - 8f), strokeWidth = 3f)
                drawLine(color, Offset(px + 4f, py + 7f), Offset(px + 4f, py - 5f), strokeWidth = 2.5f)
                drawLine(Color.White, Offset(px + 4f, py - 5f), Offset(px + 4f, py - 8f), strokeWidth = 3f)
            }
            PowerUpType.MEGA_BOMB -> {
                // Heavy warhead circle + delta fins
                drawCircle(color, 6f, Offset(px, py - 1f))
                drawCircle(Color.White, 3f, Offset(px, py - 1f))
                drawLine(color, Offset(px - 7f, py + 6f), Offset(px + 7f, py + 6f), strokeWidth = 2.2f)
                drawLine(color, Offset(px, py + 5f), Offset(px, py + 8f), strokeWidth = 2.2f)
            }
            PowerUpType.SHIELD, PowerUpType.SHIELD_BOOST -> {
                // Hexagonal deflector shield
                val shieldPath = Path().apply {
                    moveTo(px, py - 8f)
                    lineTo(px + 7f, py - 4f)
                    lineTo(px + 7f, py + 3f)
                    lineTo(px, py + 8f)
                    lineTo(px - 7f, py + 3f)
                    lineTo(px - 7f, py - 4f)
                    close()
                }
                drawPath(shieldPath, color = color, style = Stroke(width = 2f))
                drawCircle(Color.White, 3f, Offset(px, py))
            }
            PowerUpType.EMP -> {
                // Concentric electromagnetic pulse rings
                drawCircle(color, 7f, Offset(px, py), style = Stroke(width = 1.8f))
                drawCircle(color.copy(alpha = 0.5f), 4f, Offset(px, py), style = Stroke(width = 1.5f))
                drawCircle(Color.White, 2f, Offset(px, py))
            }
            PowerUpType.REPAIR, PowerUpType.REPAIR_HULL -> {
                // Military maintenance / medical cross
                drawRect(color, topLeft = Offset(px - 3.5f, py - 7f), size = Size(7f, 14f))
                drawRect(color, topLeft = Offset(px - 7f, py - 3.5f), size = Size(14f, 7f))
                drawRect(Color.White, topLeft = Offset(px - 1.5f, py - 5f), size = Size(3f, 10f))
                drawRect(Color.White, topLeft = Offset(px - 5f, py - 1.5f), size = Size(10f, 3f))
            }
            PowerUpType.CREDIT_BONUS, PowerUpType.CREDIT_CRATE -> {
                // Gold credit coin medallion
                drawCircle(color, 7f, Offset(px, py))
                drawCircle(Color(0xFF78350F), 5.5f, Offset(px, py), style = Stroke(width = 1.5f))
                drawCircle(Color.White, 2.5f, Offset(px, py))
            }
        }
    }

    private fun DrawScope.renderTargetingUI(
        engine: GameEngine,
        width: Float,
        height: Float,
        sx: Float,
        sy: Float
    ) {
        if (!engine.player.isAlive) return

        for (i in 0 until engine.enemies.size) {
            val e = engine.enemies[i]
            if (e.isDestroyed || e.y < -60f || e.y > height + 60f) continue

            val ex = e.x + sx
            val ey = e.y + sy

            val isLocked = Math.abs(e.x - engine.player.x) < 110f && e.y < engine.player.y && (engine.player.y - e.y) < 750f

            val bw = when (e.category) {
                EnemyCategory.BOSS_GOLIATH_CORE -> 130f
                EnemyCategory.AIR_BOMBER, EnemyCategory.NAVAL_CRUISER, EnemyCategory.GROUND_BRIDGE -> 68f
                else -> 38f
            }
            val bh = bw
            val halfW = bw / 2f
            val halfH = bh / 2f
            val left = ex - halfW
            val top = ey - halfH
            val right = ex + halfW
            val bottom = ey + halfH

            val cornerLen = (bw * 0.26f).coerceIn(7f, 18f)
            val bracketColor = when {
                isLocked -> Color(0xFFEF4444)
                e.isGround -> Color(0xFFF59E0B).copy(alpha = 0.75f)
                else -> Color(0xFF00E5FF).copy(alpha = 0.65f)
            }
            val strokeW = if (isLocked) 2.2f else 1.4f

            // Thin corner brackets
            // Top-Left
            drawLine(bracketColor, Offset(left, top), Offset(left + cornerLen, top), strokeWidth = strokeW)
            drawLine(bracketColor, Offset(left, top), Offset(left, top + cornerLen), strokeWidth = strokeW)
            // Top-Right
            drawLine(bracketColor, Offset(right, top), Offset(right - cornerLen, top), strokeWidth = strokeW)
            drawLine(bracketColor, Offset(right, top), Offset(right, top + cornerLen), strokeWidth = strokeW)
            // Bottom-Left
            drawLine(bracketColor, Offset(left, bottom), Offset(left + cornerLen, bottom), strokeWidth = strokeW)
            drawLine(bracketColor, Offset(left, bottom), Offset(left, bottom - cornerLen), strokeWidth = strokeW)
            // Bottom-Right
            drawLine(bracketColor, Offset(right, bottom), Offset(right - cornerLen, bottom), strokeWidth = strokeW)
            drawLine(bracketColor, Offset(right, bottom), Offset(right, bottom - cornerLen), strokeWidth = strokeW)

            // Locked diamond reticle pip
            if (isLocked) {
                rotate(45f, pivot = Offset(ex, ey)) {
                    drawRect(
                        color = Color(0xFFEF4444),
                        topLeft = Offset(ex - 8f, ey - 8f),
                        size = Size(16f, 16f),
                        style = Stroke(width = 1.8f)
                    )
                }
            }

            // Small distance & enemy type tag
            val distM = ((engine.player.y - e.y).coerceAtLeast(10f) * 1.5f).toInt()
            val tag = when (e.category) {
                EnemyCategory.BOSS_GOLIATH_CORE -> "BOSS"
                EnemyCategory.AIR_ACE_RAZOR, EnemyCategory.AIR_ACE_BLACKOUT -> "ACE"
                EnemyCategory.AIR_BOMBER, EnemyCategory.AIR_STEALTH_BOMBER -> "BMR"
                EnemyCategory.AIR_LIGHT_INTERCEPTOR, EnemyCategory.AIR_INTERCEPTOR -> "INT"
                EnemyCategory.AIR_HEAVY_FIGHTER -> "FTR"
                EnemyCategory.AIR_ATTACK_HELO -> "HELO"
                EnemyCategory.AIR_SWARM_DRONE, EnemyCategory.AIR_DRONE -> "UAV"
                EnemyCategory.GROUND_SAM -> "SAM"
                EnemyCategory.GROUND_FLAK -> "FLAK"
                EnemyCategory.GROUND_RADAR -> "RDR"
                EnemyCategory.GROUND_FUEL_DEPOT -> "FUEL"
                else -> "TGT"
            }

            val labelText = if (isLocked) "LOCK [${distM}M]" else "$tag ${distM}M"
            drawContext.canvas.nativeCanvas.drawText(
                labelText,
                right + 4f,
                top + 14f,
                if (isLocked) targetLockTextPaint else targetTextPaint
            )

            // Subtle micro health bar if damaged
            if (e.hp < e.maxHp && e.hp > 0f) {
                val barH = 2.5f
                val barY = bottom + 4f
                val ratio = (e.hp / e.maxHp).coerceIn(0f, 1f)
                drawRect(
                    color = Color(0x660F172A),
                    topLeft = Offset(left, barY),
                    size = Size(bw, barH)
                )
                drawRect(
                    color = if (ratio > 0.4f) Color(0xFF22C55E) else Color(0xFFEF4444),
                    topLeft = Offset(left, barY),
                    size = Size(bw * ratio, barH)
                )
            }
        }
    }

    private fun DrawScope.renderMissileDirectionalWarning(
        engine: GameEngine,
        sx: Float,
        sy: Float
    ) {
        if (!engine.player.isAlive) return
        var nearestMissile: Missile? = null
        var minSq = Float.MAX_VALUE
        for (i in 0 until engine.missiles.size) {
            val m = engine.missiles[i]
            if (!m.isPlayer && m.active) {
                val dx = m.x - engine.player.x
                val dy = m.y - engine.player.y
                val distSq = dx * dx + dy * dy
                if (distSq < minSq) {
                    minSq = distSq
                    nearestMissile = m
                }
            }
        }
        val missile = nearestMissile ?: return
        val distPx = sqrt(minSq)
        val distM = (distPx * 1.6f).toInt()
        val px = engine.player.x + sx
        val py = engine.player.y + sy

        val threatLevel = when {
            distPx < 320f -> MissileThreatLevel.CRITICAL
            distPx < 650f -> MissileThreatLevel.WARNING
            else -> MissileThreatLevel.NORMAL
        }

        val threatColor = when (threatLevel) {
            MissileThreatLevel.CRITICAL -> Color(0xFFEF4444)
            MissileThreatLevel.WARNING -> Color(0xFFF97316)
            MissileThreatLevel.NORMAL, MissileThreatLevel.NONE -> Color(0xFFF59E0B)
        }

        // Bearing from player to incoming missile
        val angleRad = atan2(missile.y - engine.player.y, missile.x - engine.player.x)
        val orbitR = 85f
        val pointerX = px + cos(angleRad) * orbitR
        val pointerY = py + sin(angleRad) * orbitR

        // Directional hazard chevron pointing toward incoming missile bearing
        rotate(degrees = (angleRad * 180f / 3.14159265f) + 90f, pivot = Offset(pointerX, pointerY)) {
            val chevronPath = Path().apply {
                moveTo(pointerX, pointerY - 14f)
                lineTo(pointerX + 10f, pointerY + 6f)
                lineTo(pointerX, pointerY + 1f)
                lineTo(pointerX - 10f, pointerY + 6f)
                close()
            }
            drawPath(chevronPath, color = threatColor)
        }

        // Threat ring arc around player
        drawCircle(
            color = threatColor.copy(alpha = if (threatLevel == MissileThreatLevel.CRITICAL) 0.5f else 0.25f),
            radius = orbitR,
            center = Offset(px, py),
            style = Stroke(width = if (threatLevel == MissileThreatLevel.CRITICAL) 2.5f else 1.5f)
        )

        // Distance text badge next to pointer
        val textDistOffset = 24f
        val textX = pointerX + cos(angleRad) * textDistOffset
        val textY = pointerY + sin(angleRad) * textDistOffset + 7f
        drawContext.canvas.nativeCanvas.drawText(
            "${distM}M",
            textX,
            textY,
            missileWarningTextPaint
        )
    }

    private fun DrawScope.renderClouds(width: Float, height: Float, scrollY: Float) {
        val cloudColor = Color(0x18FFFFFF)
        val cloudOffset1 = (scrollY * 0.4f) % (height + 200f)
        val cloudOffset2 = (scrollY * 0.6f + 300f) % (height + 200f)

        drawCircle(cloudColor, 120f, Offset(width * 0.25f, cloudOffset1 - 100f))
        drawCircle(cloudColor, 160f, Offset(width * 0.35f, cloudOffset1 - 80f))
        drawCircle(cloudColor, 140f, Offset(width * 0.8f, cloudOffset2 - 120f))
    }
}
