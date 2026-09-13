package com.example.engine

import androidx.compose.ui.graphics.Color
import com.example.audio.SoundManager
import com.example.model.AircraftId
import com.example.model.MissionDatabase
import com.example.model.MissionInfo
import com.example.model.PrimaryWeaponId
import com.example.model.SecondaryWeaponId
import com.example.model.SpecialWeaponId
import com.example.model.WingmanId
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

data class GameHudState(
    val score: Int = 0,
    val creditsEarned: Int = 0,
    val playerHpRatio: Float = 1.0f,
    val playerShieldRatio: Float = 1.0f,
    val missileCount: Int = 8,
    val rocketCount: Int = 4,
    val specialCooldownRatio: Float = 1.0f, // 1.0 = ready
    val isMissileWarning: Boolean = false,
    val missileWarningDistance: Float = 0f,
    val missileWarningAngleDeg: Float = 180f,
    val missileThreatLevel: MissileThreatLevel = MissileThreatLevel.NONE,
    val missileWarningDistanceMeters: Int = 0,
    val eventMessage: String? = null,
    val bossHpRatio: Float? = null,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val starsAwarded: Int = 0,
    val altitudeFeet: Int = 18500,
    val climbRateFpm: Int = 0,
    val speedKnots: Int = 680,
    val machNumber: Float = 1.03f,
    val isAfterburnerActive: Boolean = false,
    val activePrimaryWeapon: PrimaryWeaponId = PrimaryWeaponId.VULCAN_20MM,
    val activeSecondaryWeapon: SecondaryWeaponId = SecondaryWeaponId.HEAT_SEEKING_AIM9,
    val activeSpecialWeapon: SpecialWeaponId = SpecialWeaponId.FLARE_DISPENSER,
    val isTargetLocked: Boolean = false,
    val lockedTargetName: String? = null,
    val lockedTargetDistanceMeters: Int = 0,
    val airTargetsDestroyed: Int = 0,
    val groundTargetsDestroyed: Int = 0,
    val damageTaken: Float = 0f,
    val missionTimeSec: Float = 0f,
    val baseRewardCredits: Int = 1500,
    // Objective and Wave telemetry
    val objectiveDescription: String = "DESTROY ENEMY FORCES",
    val objectiveCurrent: Int = 0,
    val objectiveTarget: Int = 15,
    val currentWave: Int = 1,
    val totalWaves: Int = 5,
    // Active power-up telemetry
    val activePowerUp: PowerUpType? = null,
    val powerUpRemainingSec: Float = 0f,
    val powerUpMaxSec: Float = 8.0f,
    val lastCollectedPowerUp: PowerUpType? = null,
    val powerUpNotificationTimer: Float = 0f
)

class GameEngine(
    private val soundManager: SoundManager,
    var screenWidth: Float = 1080f,
    var screenHeight: Float = 1920f
) {
    var missionInfo: MissionInfo = MissionDatabase.missions[0]
    var missionNumber: Int = 1

    // Game state
    var isRunning = false
    var isPaused = false
    var isGameOver = false
    var isVictory = false

    // Entities
    val player = PlayerState()
    val bullets = mutableListOf<Bullet>()
    val missiles = mutableListOf<Missile>()
    val rockets = mutableListOf<Rocket>()
    val bombs = mutableListOf<Bomb>()
    val enemies = mutableListOf<Enemy>()
    val powerUps = mutableListOf<PowerUp>()
    val particles = mutableListOf<Particle>()
    val shockwaves = mutableListOf<Shockwave>()
    val friendlies = mutableListOf<FriendlyUnit>()

    // Mission Progression
    var score = 0
    var creditsCollected = 0
    var enemiesDestroyed = 0
    var groundTargetsDestroyed = 0
    var totalDamageTaken = 0f
    var missionTimeSec = 0f
    var missionDurationSec = 65f // Each mission ~1 to 1.5 minutes intense arcade action
    var terrainScrollY = 0f

    // Dynamic Events
    var eventMessage: String? = null
    var eventMessageTimer: Float = 0f
    var dynamicEventTriggered = false
    var convoyProtected = true
    var bossSpawned = false
    var lastCollectedPowerUp: PowerUpType? = null
    var powerUpNotificationTimer: Float = 0f

    // Upgrades
    var cannonDmgMult = 1.0f
    var cannonRateMult = 1.0f
    var missileDmgMult = 1.0f
    var armorHpMult = 1.0f
    var shieldCapMult = 1.0f

    // IDs
    private var nextEntityId = 1L

    // Screen Shake
    var screenShakeAmount = 0f
    private var engineParticleTick = 0

    fun startMission(
        missionNum: Int = 1,
        aircraftId: AircraftId = AircraftId.FA22_PHANTOM,
        wingmanId: WingmanId = WingmanId.ATTACK_VIPER,
        primary: PrimaryWeaponId = PrimaryWeaponId.VULCAN_20MM,
        secondary: SecondaryWeaponId = SecondaryWeaponId.HEAT_SEEKING_AIM9,
        special: SpecialWeaponId = SpecialWeaponId.FLARE_DISPENSER,
        upgrades: Map<String, Int> = emptyMap()
    ) {
        missionNumber = missionNum
        missionInfo = MissionDatabase.missions.find { it.number == missionNum } ?: MissionDatabase.missions[0]

        bullets.clear()
        missiles.clear()
        rockets.clear()
        bombs.clear()
        enemies.clear()
        powerUps.clear()
        particles.clear()
        shockwaves.clear()
        friendlies.clear()

        score = 0
        creditsCollected = 0
        enemiesDestroyed = 0
        groundTargetsDestroyed = 0
        totalDamageTaken = 0f
        missionTimeSec = 0f
        terrainScrollY = 0f
        dynamicEventTriggered = false
        convoyProtected = true
        bossSpawned = false
        isGameOver = false
        isVictory = false
        isPaused = false
        eventMessage = null
        eventMessageTimer = 0f
        lastCollectedPowerUp = null
        powerUpNotificationTimer = 0f
        screenShakeAmount = 0f

        // Upgrades calculation
        val cannonDmgLevel = upgrades["CANNON_DMG"] ?: 1
        val cannonRateLevel = upgrades["CANNON_RATE"] ?: 1
        val missileDmgLevel = upgrades["MISSILE_DMG"] ?: 1
        val missileCapLevel = upgrades["MISSILE_CAP"] ?: 1
        val armorHpLevel = upgrades["ARMOR_HP"] ?: 1
        val shieldCapLevel = upgrades["SHIELD_CAP"] ?: 1

        cannonDmgMult = 1.0f + (cannonDmgLevel - 1) * 0.15f
        cannonRateMult = 1.0f + (cannonRateLevel - 1) * 0.10f
        missileDmgMult = 1.0f + (missileDmgLevel - 1) * 0.20f
        armorHpMult = 1.0f + (armorHpLevel - 1) * 0.20f
        shieldCapMult = 1.0f + (shieldCapLevel - 1) * 0.25f

        // Configure player
        player.aircraftId = aircraftId
        player.wingmanId = wingmanId
        player.primaryWeaponId = primary
        player.secondaryWeaponId = secondary
        player.specialWeaponId = special
        player.maxHp = aircraftId.baseHealth * armorHpMult
        player.hp = player.maxHp
        player.maxShield = aircraftId.baseShield * shieldCapMult
        player.shield = player.maxShield
        player.secondaryAmmo = secondary.maxAmmo + (missileCapLevel - 1) * 2
        player.x = screenWidth / 2f
        player.y = screenHeight * 0.8f
        player.targetX = player.x
        player.targetY = player.y
        player.bankAngle = 0f
        player.primaryCooldown = 0L
        player.secondaryCooldown = 0L
        player.specialCooldown = 0L
        player.specialActiveTimerSec = 0f
        player.powerUpTimerSec = 0f
        player.activePowerUp = null
        player.isAlive = true

        missionDurationSec = if (missionInfo.isBossMission) 90f else 55f

        isRunning = true
        showEventMessage("${missionInfo.codename}: SORTIE COMMENCED", 3.5f)
    }

    fun onTouchMove(targetX: Float, targetY: Float) {
        if (!player.isAlive || isPaused) return
        val clampedX = targetX.coerceIn(50f, screenWidth - 50f)
        val clampedY = targetY.coerceIn(screenHeight * 0.15f, screenHeight - 60f)
        player.targetX = clampedX
        player.targetY = clampedY
    }

    fun fireSecondaryWeapon() {
        if (!player.isAlive || isPaused || player.secondaryAmmo <= 0 || player.secondaryCooldown > 0) return

        when (player.secondaryWeaponId) {
            SecondaryWeaponId.HEAT_SEEKING_AIM9 -> {
                // Launch heat-seeking missile from left and right pylons
                val target = enemies.filter { !it.isDestroyed && it.y < player.y }.minByOrNull {
                    val dx = it.x - player.x
                    val dy = it.y - player.y
                    dx * dx + dy * dy
                }
                val dmg = player.secondaryWeaponId.baseDamage * missileDmgMult * player.aircraftId.damageMultiplier

                missiles.add(
                    Missile(
                        id = nextEntityId++,
                        x = player.x - 28f,
                        y = player.y - 10f,
                        vx = -1.5f,
                        vy = -14f,
                        targetEnemyId = target?.id,
                        isPlayer = true,
                        damage = dmg
                    )
                )
                missiles.add(
                    Missile(
                        id = nextEntityId++,
                        x = player.x + 28f,
                        y = player.y - 10f,
                        vx = 1.5f,
                        vy = -14f,
                        targetEnemyId = target?.id,
                        isPlayer = true,
                        damage = dmg
                    )
                )
                soundManager.playMissileLaunch()
            }
            SecondaryWeaponId.HYDRA_ROCKET_PODS -> {
                val dmg = player.secondaryWeaponId.baseDamage * missileDmgMult * player.aircraftId.damageMultiplier
                // 4-rocket fan spread
                for (i in -2..1) {
                    val angleOffset = i * 0.12f
                    rockets.add(
                        Rocket(
                            x = player.x + i * 14f,
                            y = player.y - 15f,
                            vx = sin(angleOffset) * 16f,
                            vy = -cos(angleOffset) * 16f,
                            damage = dmg,
                            blastRadius = 65f
                        )
                    )
                }
                soundManager.playRocketLaunch()
            }
            SecondaryWeaponId.BUNKER_BUSTER_BOMBS -> {
                val dmg = player.secondaryWeaponId.baseDamage * missileDmgMult * player.aircraftId.damageMultiplier
                bombs.add(
                    Bomb(
                        x = player.x,
                        y = player.y + 10f,
                        vy = 3.5f,
                        damage = dmg,
                        blastRadius = 140f
                    )
                )
                soundManager.playRocketLaunch()
            }
        }
        if (player.activePowerUp != PowerUpType.UNLIMITED_MISSILES) {
            player.secondaryAmmo--
        }
        player.secondaryCooldown = (player.secondaryWeaponId.cooldownMs / player.aircraftId.speedMultiplier).toLong()
    }

    fun fireSpecialWeapon() {
        if (!player.isAlive || isPaused || player.specialCooldown > 0) return

        when (player.specialWeaponId) {
            SpecialWeaponId.FLARE_DISPENSER -> {
                player.specialActiveTimerSec = player.specialWeaponId.durationMs / 1000f
                // Divert and detonate all incoming hostile missiles!
                for (m in missiles) {
                    if (!m.isPlayer) {
                        m.active = false
                        createExplosion(m.x, m.y, 0.6f, Color(0xFFF97316))
                    }
                }
                // Spawn sparkling magnesium flare particles
                for (i in 0 until 18) {
                    val angle = Random.nextFloat() * 6.28f
                    val speed = Random.nextFloat() * 8f + 2f
                    particles.add(
                        Particle(
                            x = player.x + (Random.nextFloat() - 0.5f) * 30f,
                            y = player.y + 20f,
                            vx = cos(angle) * speed,
                            vy = sin(angle) * speed + 3f,
                            life = 1.2f,
                            maxLife = 1.2f,
                            size = 7f,
                            color = Color(0xFFFFFBEB),
                            isSparks = true
                        )
                    )
                }
                soundManager.playFlare()
                showEventMessage("DEFENSIVE FLARES DEPLOYED — MISSILES DIVERTED", 2.0f)
            }
            SpecialWeaponId.EMP_BLAST -> {
                player.specialActiveTimerSec = player.specialWeaponId.durationMs / 1000f
                // Disable all enemy fire and freeze electronics
                for (e in enemies) {
                    e.isEmpDisabledTimer = 4.0f
                }
                // Clear hostile bullets
                bullets.removeAll { !it.isPlayer }
                shockwaves.add(
                    Shockwave(
                        x = player.x,
                        y = player.y,
                        radius = 20f,
                        maxRadius = screenWidth * 0.9f,
                        alpha = 1.0f,
                        color = Color(0xFF00E5FF)
                    )
                )
                soundManager.playEmp()
                showEventMessage("EMP SHOCKWAVE DISCHARGED — ENEMY SYSTEMS OFFLINE", 2.5f)
            }
            SpecialWeaponId.AIR_STRIKE_CRUISE -> {
                // Allied supersonic cruise missile strike
                soundManager.playMissileLaunch()
                showEventMessage("ALLIED CRUISE MISSILE INBOUND", 2.0f)
                shockwaves.add(
                    Shockwave(
                        x = screenWidth / 2f,
                        y = screenHeight * 0.35f,
                        radius = 10f,
                        maxRadius = screenWidth * 0.8f,
                        alpha = 1.0f,
                        color = Color(0xFFFF5722)
                    )
                )
                // Devastate all forward enemies
                for (e in enemies) {
                    if (e.y < screenHeight * 0.7f) {
                        e.hp -= 300f
                        e.hitFlashTimer = 0.18f
                    }
                }
                soundManager.playBigExplosion()
                triggerScreenShake(20f)
            }
        }
        player.specialCooldown = player.specialWeaponId.cooldownMs
    }

    fun update(dtSec: Float) {
        if (!isRunning || isPaused) return

        missionTimeSec += dtSec
        terrainScrollY += 160f * dtSec

        if (screenShakeAmount > 0f) {
            screenShakeAmount = (screenShakeAmount - dtSec * 30f).coerceAtLeast(0f)
        }

        if (eventMessageTimer > 0f) {
            eventMessageTimer -= dtSec
            if (eventMessageTimer <= 0f) eventMessage = null
        }

        if (powerUpNotificationTimer > 0f) {
            powerUpNotificationTimer -= dtSec
            if (powerUpNotificationTimer <= 0f) lastCollectedPowerUp = null
        }

        if (player.isAlive) {
            updatePlayer(dtSec)
            updateWingman(dtSec)
        }

        updateProjectiles(dtSec)
        updateEnemies(dtSec)
        updateDynamicEvents(dtSec)
        updateSpawnWave(dtSec)
        updatePowerUps(dtSec)
        updateParticles(dtSec)

        // Check mission victory conditions
        checkMissionStatus()
    }

    private fun updatePlayer(dtSec: Float) {
        // Smooth player movement towards target
        val dx = player.targetX - player.x
        val dy = player.targetY - player.y
        val moveSpeed = 14f * player.aircraftId.speedMultiplier
        player.x += dx * (moveSpeed * dtSec).coerceAtMost(1f)
        player.y += dy * (moveSpeed * dtSec).coerceAtMost(1f)

        // Banking angle calculation based on horizontal movement
        val targetBank = (dx * 0.25f).coerceIn(-28f, 28f)
        player.bankAngle += (targetBank - player.bankAngle) * (12f * dtSec)

        // Shield regeneration (recharge after 4s without damage)
        if (player.shield < player.maxShield) {
            player.shield = (player.shield + 6f * dtSec).coerceAtMost(player.maxShield)
        }

        // Weapon cooldowns
        val dtMs = (dtSec * 1000f).toLong()
        if (player.primaryCooldown > 0) player.primaryCooldown -= dtMs
        if (player.secondaryCooldown > 0) player.secondaryCooldown -= dtMs
        if (player.specialCooldown > 0) player.specialCooldown -= dtMs
        if (player.specialActiveTimerSec > 0f) player.specialActiveTimerSec -= dtSec
        if (player.powerUpTimerSec > 0f) {
            player.powerUpTimerSec -= dtSec
            if (player.powerUpTimerSec <= 0f) player.activePowerUp = null
        }
        if (player.hitFlashTimer > 0f) {
            player.hitFlashTimer = (player.hitFlashTimer - dtSec).coerceAtLeast(0f)
        }

        // Automatic Primary Cannon Continuous Fire
        if (player.primaryCooldown <= 0) {
            firePrimaryWeapon()
        }

        // Jet engine particle contrails & afterburner (throttled for performance)
        engineParticleTick++
        if (engineParticleTick % 2 == 0 && particles.size < 70) {
            spawnEngineParticles()
        }

        // Damage visual effects: smoke plumes & fire if hull is damaged
        val hpRatio = player.hp / player.maxHp
        if (hpRatio < 0.75f && Random.nextFloat() < 0.35f) {
            // Light smoke
            particles.add(
                Particle(
                    x = player.x + (Random.nextFloat() - 0.5f) * 16f,
                    y = player.y + 36f,
                    vx = (Random.nextFloat() - 0.5f) * 1.5f,
                    vy = Random.nextFloat() * 2f + 3f,
                    life = 0.6f,
                    maxLife = 0.6f,
                    size = 6f,
                    color = Color(0x66475569)
                )
            )
        }
        if (hpRatio < 0.50f && Random.nextFloat() < 0.55f) {
            // Heavy smoke plume
            particles.add(
                Particle(
                    x = player.x + (Random.nextFloat() - 0.5f) * 20f,
                    y = player.y + 38f,
                    vx = (Random.nextFloat() - 0.5f) * 2.5f,
                    vy = Random.nextFloat() * 3f + 4f,
                    life = 0.8f,
                    maxLife = 0.8f,
                    size = 11f,
                    color = Color(0x991E293B)
                )
            )
        }
        if (hpRatio < 0.25f && Random.nextFloat() < 0.45f) {
            // Flame sparks
            particles.add(
                Particle(
                    x = player.x + (Random.nextFloat() - 0.5f) * 14f,
                    y = player.y + 36f,
                    vx = (Random.nextFloat() - 0.5f) * 4f,
                    vy = Random.nextFloat() * 4f + 3f,
                    life = 0.4f,
                    maxLife = 0.4f,
                    size = 5f,
                    color = Color(0xFFEF4444),
                    isSparks = true
                )
            )
        }
    }

    private fun firePrimaryWeapon() {
        val mult = player.aircraftId.damageMultiplier * cannonDmgMult
        val isDouble = player.activePowerUp == PowerUpType.DOUBLE_DAMAGE
        val isRapid = player.activePowerUp == PowerUpType.RAPID_FIRE
        val rateDivider = if (isRapid) 1.6f else 1.0f

        when (player.primaryWeaponId) {
            PrimaryWeaponId.VULCAN_20MM -> {
                val dmg = player.primaryWeaponId.baseDamage * mult
                // Twin synchronized cannons
                bullets.add(Bullet(player.x - 14f, player.y - 30f, 0f, -24f, true, dmg, false, Color(0xFFFBBF24)))
                bullets.add(Bullet(player.x + 14f, player.y - 30f, 0f, -24f, true, dmg, false, Color(0xFFFBBF24)))
                if (isDouble) {
                    bullets.add(Bullet(player.x - 26f, player.y - 25f, -1.8f, -23f, true, dmg, false, Color(0xFFF97316)))
                    bullets.add(Bullet(player.x + 26f, player.y - 25f, 1.8f, -23f, true, dmg, false, Color(0xFFF97316)))
                }
                player.primaryCooldown = ((player.primaryWeaponId.fireRateMs / cannonRateMult) / rateDivider).toLong()
                soundManager.playCannon()
            }
            PrimaryWeaponId.ROTARY_30MM -> {
                val dmg = player.primaryWeaponId.baseDamage * mult
                // Heavy center gatling cannon
                bullets.add(Bullet(player.x, player.y - 34f, 0f, -26f, true, dmg, true, Color(0xFFEF4444)))
                if (isDouble) {
                    bullets.add(Bullet(player.x - 16f, player.y - 30f, -1f, -25f, true, dmg * 0.8f, true, Color(0xFFF97316)))
                    bullets.add(Bullet(player.x + 16f, player.y - 30f, 1f, -25f, true, dmg * 0.8f, true, Color(0xFFF97316)))
                }
                player.primaryCooldown = ((player.primaryWeaponId.fireRateMs / cannonRateMult) / rateDivider).toLong()
                soundManager.playHeavyCannon()
            }
            PrimaryWeaponId.PLASMA_REPEATER -> {
                val dmg = player.primaryWeaponId.baseDamage * mult
                // Dual kinetic energy pulses
                bullets.add(Bullet(player.x - 12f, player.y - 32f, -0.6f, -25f, true, dmg, false, Color(0xFF00E5FF)))
                bullets.add(Bullet(player.x + 12f, player.y - 32f, 0.6f, -25f, true, dmg, false, Color(0xFF00E5FF)))
                if (isDouble) {
                    bullets.add(Bullet(player.x, player.y - 38f, 0f, -26f, true, dmg * 1.2f, true, Color(0xFF38BDF8)))
                }
                player.primaryCooldown = ((player.primaryWeaponId.fireRateMs / cannonRateMult) / rateDivider).toLong()
                soundManager.playCannon()
            }
        }
    }

    private fun spawnEngineParticles() {
        // Twin jet engine flames
        val leftEngineX = player.x - 10f
        val rightEngineX = player.x + 10f
        val engineY = player.y + 36f

        particles.add(
            Particle(
                x = leftEngineX,
                y = engineY,
                vx = (Random.nextFloat() - 0.5f) * 1f,
                vy = Random.nextFloat() * 4f + 6f,
                life = 0.25f,
                maxLife = 0.25f,
                size = 6f,
                color = Color(0xFF00E5FF)
            )
        )
        particles.add(
            Particle(
                x = rightEngineX,
                y = engineY,
                vx = (Random.nextFloat() - 0.5f) * 1f,
                vy = Random.nextFloat() * 4f + 6f,
                life = 0.25f,
                maxLife = 0.25f,
                size = 6f,
                color = Color(0xFFF97316)
            )
        )
    }

    private fun updateWingman(dtSec: Float) {
        val wingman = player.wingmanId
        if (wingman == WingmanId.NONE) return

        val wingmanX = player.x + 55f
        val wingmanY = player.y + 20f

        when (wingman) {
            WingmanId.ATTACK_VIPER -> {
                // Fires synchronized cannon burst periodically
                if (Random.nextFloat() < 0.22f) {
                    bullets.add(Bullet(wingmanX, wingmanY - 15f, 0f, -22f, true, 16f, false, Color(0xFF38BDF8)))
                }
            }
            WingmanId.DEFENSIVE_AEGIS -> {
                // Scans for hostile missiles near player and shoots them down!
                for (m in missiles) {
                    if (!m.isPlayer && m.active) {
                        val dist = sqrt((m.x - player.x) * (m.x - player.x) + (m.y - player.y) * (m.y - player.y))
                        if (dist < 260f) {
                            // Intercept missile
                            m.active = false
                            createExplosion(m.x, m.y, 0.4f, Color(0xFF38BDF8))
                            soundManager.playHit()
                            // Laser beam particle
                            particles.add(
                                Particle(
                                    x = (wingmanX + m.x) / 2f,
                                    y = (wingmanY + m.y) / 2f,
                                    vx = 0f,
                                    vy = 0f,
                                    life = 0.15f,
                                    maxLife = 0.15f,
                                    size = 4f,
                                    color = Color(0xFF00E5FF)
                                )
                            )
                            break
                        }
                    }
                }
            }
            WingmanId.RECON_HAWK -> {
                // Grants critical targeting telemetry
                // Handled in damage calculations (+35% damage)
            }
            WingmanId.DRONE_SWARM -> {
                // Orbiting drone swarm that attacks closest enemies
                val nearest = enemies.filter { !it.isDestroyed && it.y < screenHeight * 0.7f }.minByOrNull {
                    val dx = it.x - player.x
                    val dy = it.y - player.y
                    dx * dx + dy * dy
                }
                if (nearest != null && Random.nextFloat() < 0.18f) {
                    val angle = atan2(nearest.y - wingmanY, nearest.x - wingmanX)
                    bullets.add(
                        Bullet(
                            x = wingmanX,
                            y = wingmanY,
                            vx = cos(angle) * 18f,
                            vy = sin(angle) * 18f,
                            isPlayer = true,
                            damage = 18f,
                            color = Color(0xFFA855F7)
                        )
                    )
                }
            }
            WingmanId.NONE -> {}
        }
    }

    private fun updateProjectiles(dtSec: Float) {
        // Bullets
        val bulletIterator = bullets.iterator()
        while (bulletIterator.hasNext()) {
            val b = bulletIterator.next()
            b.x += b.vx
            b.y += b.vy

            if (b.y < -50f || b.y > screenHeight + 50f || b.x < -50f || b.x > screenWidth + 50f) {
                bulletIterator.remove()
                continue
            }

            if (b.isPlayer) {
                // Check hit against enemies
                var hit = false
                val critMult = if (player.wingmanId == WingmanId.RECON_HAWK) 1.35f else 1.0f
                for (e in enemies) {
                    if (e.isDestroyed) continue
                    val hitRadius = if (e.isGround) 42f else 32f
                    val dx = b.x - e.x
                    val dy = b.y - e.y
                    if (dx * dx + dy * dy < hitRadius * hitRadius) {
                        e.hp -= b.damage * critMult
                        e.hitFlashTimer = 0.12f
                        hit = true
                        soundManager.playHit()
                        // Sparks
                        for (i in 0 until 3) {
                            particles.add(
                                Particle(
                                    b.x, b.y,
                                    (Random.nextFloat() - 0.5f) * 6f,
                                    (Random.nextFloat() - 0.5f) * 6f,
                                    0.2f, 0.2f, 4f, Color(0xFFFBBF24), true
                                )
                            )
                        }
                        if (e.hp <= 0f) {
                            destroyEnemy(e)
                        }
                        break
                    }
                }
                if (hit) bulletIterator.remove()
            } else {
                // Hostile bullet hitting player
                if (player.isAlive) {
                    val dx = b.x - player.x
                    val dy = b.y - player.y
                    if (dx * dx + dy * dy < 28f * 28f) {
                        damagePlayer(b.damage)
                        bulletIterator.remove()
                    }
                }
            }
        }

        // Missiles
        val missileIterator = missiles.iterator()
        while (missileIterator.hasNext()) {
            val m = missileIterator.next()
            m.lifeSec -= dtSec

            // Smoke trail behind missile
            if (Random.nextFloat() < 0.6f) {
                particles.add(
                    Particle(
                        x = m.x,
                        y = m.y + (if (m.isPlayer) 10f else -10f),
                        vx = (Random.nextFloat() - 0.5f) * 1.5f,
                        vy = (Random.nextFloat() - 0.5f) * 1.5f + (if (m.isPlayer) 2f else -2f),
                        life = 0.5f,
                        maxLife = 0.5f,
                        size = 5f,
                        color = Color(0x8894A3B8)
                    )
                )
            }

            if (m.isPlayer) {
                // Home towards target
                val target = enemies.find { it.id == m.targetEnemyId && !it.isDestroyed }
                    ?: enemies.filter { !it.isDestroyed && it.y < m.y }.minByOrNull {
                        val dx = it.x - m.x
                        val dy = it.y - m.y
                        dx * dx + dy * dy
                    }

                if (target != null) {
                    val angle = atan2(target.y - m.y, target.x - m.x)
                    val speed = 15f
                    m.vx += (cos(angle) * speed - m.vx) * 0.12f
                    m.vy += (sin(angle) * speed - m.vy) * 0.12f
                }
                m.x += m.vx
                m.y += m.vy

                // Collision with enemies
                var hit = false
                for (e in enemies) {
                    if (e.isDestroyed) continue
                    val dx = m.x - e.x
                    val dy = m.y - e.y
                    if (dx * dx + dy * dy < 38f * 38f) {
                        e.hp -= m.damage
                        e.hitFlashTimer = 0.16f
                        hit = true
                        createExplosion(m.x, m.y, 0.7f, Color(0xFFF97316))
                        soundManager.playExplosion()
                        if (e.hp <= 0f) destroyEnemy(e)
                        break
                    }
                }
                if (hit || m.lifeSec <= 0f || m.y < -80f) {
                    missileIterator.remove()
                }
            } else {
                // Hostile missile tracking player
                if (player.isAlive) {
                    val angle = atan2(player.y - m.y, player.x - m.x)
                    val speed = 11f
                    m.vx += (cos(angle) * speed - m.vx) * 0.08f
                    m.vy += (sin(angle) * speed - m.vy) * 0.08f
                    m.x += m.vx
                    m.y += m.vy

                    val dx = m.x - player.x
                    val dy = m.y - player.y
                    if (dx * dx + dy * dy < 32f * 32f) {
                        damagePlayer(m.damage)
                        createExplosion(m.x, m.y, 0.8f, Color(0xFFEF4444))
                        soundManager.playExplosion()
                        missileIterator.remove()
                    } else if (m.lifeSec <= 0f || m.y > screenHeight + 80f) {
                        missileIterator.remove()
                    }
                } else {
                    m.y += m.vy
                    if (m.lifeSec <= 0f) missileIterator.remove()
                }
            }
        }

        // Rockets
        val rocketIterator = rockets.iterator()
        while (rocketIterator.hasNext()) {
            val r = rocketIterator.next()
            r.x += r.vx
            r.y += r.vy

            // Rocket smoke
            particles.add(
                Particle(r.x, r.y + 8f, 0f, 2f, 0.3f, 0.3f, 5f, Color(0xFFF97316))
            )

            // Check hit
            var exploded = false
            for (e in enemies) {
                if (e.isDestroyed) continue
                val dx = r.x - e.x
                val dy = r.y - e.y
                if (dx * dx + dy * dy < (r.blastRadius * 0.6f) * (r.blastRadius * 0.6f)) {
                    exploded = true
                    break
                }
            }
            if (exploded || r.y < -50f) {
                if (exploded) {
                    // Area damage
                    for (e in enemies) {
                        if (e.isDestroyed) continue
                        val dist = sqrt((r.x - e.x) * (r.x - e.x) + (r.y - e.y) * (r.y - e.y))
                        if (dist < r.blastRadius) {
                            val falloff = (1f - dist / r.blastRadius).coerceIn(0.2f, 1f)
                            e.hp -= r.damage * falloff
                            e.hitFlashTimer = 0.16f
                            if (e.hp <= 0f) destroyEnemy(e)
                        }
                    }
                    createExplosion(r.x, r.y, 0.8f, Color(0xFFEAB308))
                    soundManager.playExplosion()
                }
                rocketIterator.remove()
            }
        }

        // Bombs
        val bombIterator = bombs.iterator()
        while (bombIterator.hasNext()) {
            val b = bombIterator.next()
            b.y += b.vy
            b.progress += dtSec * 0.7f
            b.scale = 1.0f - b.progress * 0.35f // Falling perspective

            if (b.progress >= 1.0f) {
                // Detonates on the ground with massive shockwave
                for (e in enemies) {
                    if (e.isDestroyed) continue
                    val dist = sqrt((b.x - e.x) * (b.x - e.x) + (b.y - e.y) * (b.y - e.y))
                    if (dist < b.blastRadius) {
                        val falloff = (1f - dist / b.blastRadius).coerceIn(0.3f, 1f)
                        e.hp -= b.damage * falloff
                        e.hitFlashTimer = 0.2f
                        if (e.hp <= 0f) destroyEnemy(e)
                    }
                }
                shockwaves.add(
                    Shockwave(b.x, b.y, 15f, b.blastRadius * 1.2f, 1.0f, Color(0xFFFF5722))
                )
                createExplosion(b.x, b.y, 1.2f, Color(0xFFF97316))
                soundManager.playBigExplosion()
                triggerScreenShake(14f)
                bombIterator.remove()
            }
        }
    }

    private fun updateEnemies(dtSec: Float) {
        val iterator = enemies.iterator()
        while (iterator.hasNext()) {
            val e = iterator.next()
            if (e.isDestroyed) {
                iterator.remove()
                continue
            }

            e.patternTimer += dtSec

            if (e.hitFlashTimer > 0f) {
                e.hitFlashTimer = (e.hitFlashTimer - dtSec).coerceAtLeast(0f)
            }

            if (e.isEmpDisabledTimer > 0f) {
                e.isEmpDisabledTimer -= dtSec
            }

            // Movement patterns based on enemy type
            when (e.category) {
                EnemyCategory.AIR_SCOUT -> {
                    e.y += e.vy
                    e.x += sin(e.patternTimer * 3f) * 2.5f
                }
                EnemyCategory.AIR_INTERCEPTOR -> {
                    e.y += e.vy * 1.3f
                    // Flanks towards player
                    val dir = if (player.x > e.x) 1.2f else -1.2f
                    e.x += dir
                }
                EnemyCategory.AIR_GUNSHIP -> {
                    e.y += e.vy * 0.6f
                    e.x += sin(e.patternTimer * 1.5f) * 1.0f
                }
                EnemyCategory.AIR_ATTACK_HELO -> {
                    e.y += e.vy * 0.5f
                    e.x += sin(e.patternTimer * 2f) * 1.8f
                }
                EnemyCategory.AIR_DRONE -> {
                    // Kamikaze swoop towards player
                    val angle = atan2(player.y - e.y, player.x - e.x)
                    e.vx += (cos(angle) * 4f - e.vx) * 0.05f
                    e.vy += (sin(angle) * 4f - e.vy) * 0.05f
                    e.x += e.vx
                    e.y += e.vy
                }
                EnemyCategory.AIR_ACE_RAZOR -> {
                    // High-G aggressive dogfighting maneuvers
                    e.y += sin(e.patternTimer * 2f) * 2f + 0.8f
                    e.x = screenWidth / 2f + sin(e.patternTimer * 3.5f) * (screenWidth * 0.35f)
                }
                EnemyCategory.AIR_ACE_BLACKOUT -> {
                    // Stealth darting
                    e.y += sin(e.patternTimer * 1.5f) * 1.5f + 0.6f
                    e.x += cos(e.patternTimer * 2.8f) * 3f
                }
                EnemyCategory.BOSS_GOLIATH_CORE -> {
                    // Heavy airborne fortress descends and hovers at top
                    if (e.y < screenHeight * 0.22f) {
                        e.y += 0.8f
                    } else {
                        e.x = screenWidth / 2f + sin(e.patternTimer * 0.8f) * 60f
                    }
                }
                EnemyCategory.BOSS_TURRET_LEFT -> {
                    val core = enemies.find { it.category == EnemyCategory.BOSS_GOLIATH_CORE }
                    if (core != null) {
                        e.x = core.x - 120f
                        e.y = core.y + 20f
                    }
                }
                EnemyCategory.BOSS_TURRET_RIGHT -> {
                    val core = enemies.find { it.category == EnemyCategory.BOSS_GOLIATH_CORE }
                    if (core != null) {
                        e.x = core.x + 120f
                        e.y = core.y + 20f
                    }
                }
                EnemyCategory.BOSS_MISSILE_BAY -> {
                    val core = enemies.find { it.category == EnemyCategory.BOSS_GOLIATH_CORE }
                    if (core != null) {
                        e.x = core.x
                        e.y = core.y - 40f
                    }
                }
                EnemyCategory.BOSS_ENGINE_LEFT -> {
                    val core = enemies.find { it.category == EnemyCategory.BOSS_GOLIATH_CORE }
                    if (core != null) {
                        e.x = core.x - 70f
                        e.y = core.y + 60f
                    }
                }
                EnemyCategory.BOSS_ENGINE_RIGHT -> {
                    val core = enemies.find { it.category == EnemyCategory.BOSS_GOLIATH_CORE }
                    if (core != null) {
                        e.x = core.x + 70f
                        e.y = core.y + 60f
                    }
                }
                else -> {
                    // Ground targets scroll down with terrain
                    e.y += 160f * dtSec
                }
            }

            // Enemy weapon firing
            if (e.isEmpDisabledTimer <= 0f && e.y > 0f && e.y < screenHeight * 0.85f) {
                e.shootTimerMs += (dtSec * 1000f).toLong()
                if (e.shootTimerMs >= e.shootCooldownMs) {
                    e.shootTimerMs = 0L
                    fireEnemyWeapon(e)
                }
            }

            // Clean up off-screen enemies
            if (e.y > screenHeight + 120f) {
                iterator.remove()
            }
        }
    }

    private fun fireEnemyWeapon(e: Enemy) {
        when (e.category) {
            EnemyCategory.AIR_SCOUT -> {
                bullets.add(Bullet(e.x, e.y + 20f, 0f, 9f, false, 12f, false, Color(0xFFEF4444)))
            }
            EnemyCategory.AIR_INTERCEPTOR -> {
                bullets.add(Bullet(e.x - 10f, e.y + 20f, -0.8f, 11f, false, 14f, false, Color(0xFFF97316)))
                bullets.add(Bullet(e.x + 10f, e.y + 20f, 0.8f, 11f, false, 14f, false, Color(0xFFF97316)))
            }
            EnemyCategory.AIR_GUNSHIP -> {
                // Triple burst
                for (i in -1..1) {
                    bullets.add(Bullet(e.x + i * 18f, e.y + 25f, i * 1.5f, 9f, false, 16f, true, Color(0xFFDC2626)))
                }
            }
            EnemyCategory.GROUND_SAM -> {
                // Fires surface-to-air missile targeting player!
                missiles.add(
                    Missile(
                        id = nextEntityId++,
                        x = e.x,
                        y = e.y - 15f,
                        vx = (player.x - e.x) * 0.015f,
                        vy = -6f,
                        isPlayer = false,
                        damage = 38f,
                        targetPlayer = true
                    )
                )
                soundManager.playMissileWarning()
            }
            EnemyCategory.GROUND_FLAK -> {
                // Flak shell burst near player
                bullets.add(Bullet(e.x, e.y - 20f, (player.x - e.x) * 0.012f, -12f, false, 20f, true, Color(0xFFF59E0B)))
            }
            EnemyCategory.AIR_ACE_RAZOR -> {
                // Rapid spread plus heat seeking missile
                for (i in -2..2) {
                    bullets.add(Bullet(e.x + i * 8f, e.y + 25f, i * 1.2f, 12f, false, 16f, false, Color(0xFFEF4444)))
                }
                if (Random.nextFloat() < 0.45f) {
                    missiles.add(
                        Missile(
                            id = nextEntityId++,
                            x = e.x,
                            y = e.y + 25f,
                            vx = 0f,
                            vy = 8f,
                            isPlayer = false,
                            damage = 35f,
                            targetPlayer = true
                        )
                    )
                    soundManager.playMissileWarning()
                }
            }
            EnemyCategory.AIR_ACE_BLACKOUT -> {
                // Missile volley
                missiles.add(
                    Missile(
                        id = nextEntityId++,
                        x = e.x - 20f,
                        y = e.y + 20f,
                        vx = -2f,
                        vy = 9f,
                        isPlayer = false,
                        damage = 32f,
                        targetPlayer = true
                    )
                )
                missiles.add(
                    Missile(
                        id = nextEntityId++,
                        x = e.x + 20f,
                        y = e.y + 20f,
                        vx = 2f,
                        vy = 9f,
                        isPlayer = false,
                        damage = 32f,
                        targetPlayer = true
                    )
                )
                soundManager.playMissileWarning()
            }
            EnemyCategory.BOSS_TURRET_LEFT, EnemyCategory.BOSS_TURRET_RIGHT -> {
                // Radial flak ring
                for (i in -2..2) {
                    val angle = 1.57f + i * 0.35f
                    bullets.add(
                        Bullet(e.x, e.y, cos(angle) * 8f, sin(angle) * 8f, false, 18f, true, Color(0xFFDC2626))
                    )
                }
            }
            EnemyCategory.BOSS_MISSILE_BAY -> {
                // Double missile launch
                missiles.add(
                    Missile(
                        id = nextEntityId++,
                        x = e.x - 24f,
                        y = e.y,
                        vx = -3f,
                        vy = 6f,
                        isPlayer = false,
                        damage = 40f,
                        targetPlayer = true
                    )
                )
                missiles.add(
                    Missile(
                        id = nextEntityId++,
                        x = e.x + 24f,
                        y = e.y,
                        vx = 3f,
                        vy = 6f,
                        isPlayer = false,
                        damage = 40f,
                        targetPlayer = true
                    )
                )
                soundManager.playMissileWarning()
            }
            EnemyCategory.BOSS_GOLIATH_CORE -> {
                // Desperation bullet spiral
                for (i in 0 until 8) {
                    val angle = e.patternTimer * 4f + i * (6.28f / 8f)
                    bullets.add(
                        Bullet(e.x, e.y, cos(angle) * 9f, sin(angle) * 9f, false, 15f, false, Color(0xFF9333EA))
                    )
                }
            }
            else -> {}
        }
    }

    private fun destroyEnemy(e: Enemy) {
        e.isDestroyed = true
        score += e.points
        creditsCollected += e.credits
        enemiesDestroyed++

        if (e.isGround) groundTargetsDestroyed++

        // Check if fuel depot: creates massive secondary chain explosion damaging other nearby enemies!
        if (e.category == EnemyCategory.GROUND_FUEL_DEPOT) {
            shockwaves.add(Shockwave(e.x, e.y, 20f, 180f, 1.0f, Color(0xFFFF5722)))
            createExplosion(e.x, e.y, 1.5f, Color(0xFFF97316))
            soundManager.playBigExplosion()
            triggerScreenShake(18f)
            showEventMessage("FUEL DEPOT CRITICAL DETONATION!", 2.0f)
            for (other in enemies) {
                if (!other.isDestroyed && other.id != e.id) {
                    val dist = sqrt((e.x - other.x) * (e.x - other.x) + (e.y - other.y) * (e.y - other.y))
                    if (dist < 220f) {
                        other.hp -= 220f
                        if (other.hp <= 0f) destroyEnemy(other)
                    }
                }
            }
        } else if (e.category == EnemyCategory.GROUND_BRIDGE) {
            shockwaves.add(Shockwave(e.x, e.y, 25f, 200f, 1.0f, Color(0xFF78350F)))
            createExplosion(e.x, e.y, 1.4f, Color(0xFFF97316))
            soundManager.playBigExplosion()
            showEventMessage("BRIDGE COLLAPSE — SUPPLY ROUTE CUT!", 2.0f)
        } else if (e.category == EnemyCategory.BOSS_GOLIATH_CORE) {
            // Boss defeated!
            createExplosion(e.x, e.y, 2.0f, Color(0xFFF97316))
            soundManager.playBigExplosion()
            triggerScreenShake(25f)
            showEventMessage("GOLIATH AIRBORNE FORTRESS DESTROYED!", 4.0f)
            isVictory = true
        } else {
            createExplosion(e.x, e.y, if (e.isGround) 1.0f else 0.8f, Color(0xFFF97316))
            soundManager.playExplosion()
        }

        // Power-up drops
        if (Random.nextFloat() < 0.22f) {
            val type = listOf(
                PowerUpType.DOUBLE_DAMAGE,
                PowerUpType.RAPID_FIRE,
                PowerUpType.UNLIMITED_MISSILES,
                PowerUpType.MEGA_BOMB,
                PowerUpType.SHIELD,
                PowerUpType.EMP,
                PowerUpType.REPAIR,
                PowerUpType.CREDIT_BONUS
            ).random()
            powerUps.add(PowerUp(nextEntityId++, e.x, e.y, type))
        }
    }

    private fun damagePlayer(amount: Float) {
        if (!player.isAlive) return

        totalDamageTaken += amount
        triggerScreenShake(8f)
        soundManager.playHit()
        player.hitFlashTimer = 0.22f

        // Shield absorbs first
        if (player.shield > 0) {
            if (player.shield >= amount) {
                player.shield -= amount
                return
            } else {
                val remaining = amount - player.shield
                player.shield = 0f
                player.hp -= remaining
            }
        } else {
            player.hp -= amount
        }

        if (player.hp <= 0f) {
            player.hp = 0f
            player.isAlive = false
            isGameOver = true
            createExplosion(player.x, player.y, 1.8f, Color(0xFFEF4444))
            soundManager.playBigExplosion()
            triggerScreenShake(22f)
            showEventMessage("AIRCRAFT DESTROYED — MISSION FAILED", 4.0f)
        }
    }

    private fun updatePowerUps(dtSec: Float) {
        val iterator = powerUps.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.y += p.vy

            // Magnetic collection towards player
            if (player.isAlive) {
                val dx = player.x - p.x
                val dy = player.y - p.y
                val distSq = dx * dx + dy * dy
                if (distSq < 160f * 160f) {
                    p.x += dx * 0.08f
                    p.y += dy * 0.08f
                }
                if (distSq < 48f * 48f) {
                    applyPowerUp(p.type)
                    soundManager.playPowerup()
                    iterator.remove()
                    continue
                }
            }

            if (p.y > screenHeight + 40f) iterator.remove()
        }
    }

    private fun applyPowerUp(type: PowerUpType) {
        lastCollectedPowerUp = type
        powerUpNotificationTimer = 2.5f

        when (type) {
            PowerUpType.REPAIR, PowerUpType.REPAIR_HULL -> {
                player.hp = (player.hp + player.maxHp * 0.45f).coerceAtMost(player.maxHp)
                showEventMessage("HULL REPAIR: +45% INTEGRITY", 2.0f)
            }
            PowerUpType.SHIELD, PowerUpType.SHIELD_BOOST -> {
                player.shield = player.maxShield
                player.activePowerUp = PowerUpType.SHIELD
                player.powerUpTimerSec = 8.0f
                showEventMessage("DEFLECTOR SHIELD MAX OVERCHARGE", 2.0f)
            }
            PowerUpType.DOUBLE_DAMAGE -> {
                player.activePowerUp = PowerUpType.DOUBLE_DAMAGE
                player.powerUpTimerSec = 9.0f
                showEventMessage("DOUBLE DAMAGE ENGAGED (2X FIREPOWER)", 2.0f)
            }
            PowerUpType.RAPID_FIRE -> {
                player.activePowerUp = PowerUpType.RAPID_FIRE
                player.powerUpTimerSec = 9.0f
                showEventMessage("RAPID FIRE OVERDRIVE ENGAGED", 2.0f)
            }
            PowerUpType.UNLIMITED_MISSILES, PowerUpType.MISSILE_RESTOCK -> {
                player.activePowerUp = PowerUpType.UNLIMITED_MISSILES
                player.powerUpTimerSec = 8.0f
                player.secondaryAmmo = (player.secondaryAmmo + 8).coerceAtMost(16)
                showEventMessage("UNLIMITED MISSILES ACTIVE", 2.0f)
            }
            PowerUpType.MEGA_BOMB -> {
                // Clear hostile projectiles and deal heavy damage across the screen
                bullets.removeAll { !it.isPlayer }
                for (m in missiles) {
                    if (!m.isPlayer) {
                        m.active = false
                        createExplosion(m.x, m.y, 0.8f, Color(0xFFF97316))
                    }
                }
                for (e in enemies) {
                    if (e.y in 0f..screenHeight) {
                        e.hp -= 260f
                        e.hitFlashTimer = 0.25f
                    }
                }
                shockwaves.add(
                    Shockwave(
                        x = player.x,
                        y = player.y,
                        radius = 20f,
                        maxRadius = screenWidth * 1.1f,
                        alpha = 1.0f,
                        color = Color(0xFFEF4444)
                    )
                )
                triggerScreenShake(20f)
                soundManager.playBigExplosion()
                showEventMessage("MEGA BOMB DETONATED — AIRSPACE CLEARED", 2.5f)
            }
            PowerUpType.EMP -> {
                for (e in enemies) {
                    e.isEmpDisabledTimer = 5.0f
                }
                bullets.removeAll { !it.isPlayer }
                shockwaves.add(
                    Shockwave(
                        x = player.x,
                        y = player.y,
                        radius = 15f,
                        maxRadius = screenWidth,
                        alpha = 1.0f,
                        color = Color(0xFF818CF8)
                    )
                )
                soundManager.playEmp()
                showEventMessage("EMP SHOCKWAVE DISCHARGED — WEAPONS OFFLINE", 2.5f)
            }
            PowerUpType.CREDIT_BONUS, PowerUpType.CREDIT_CRATE -> {
                creditsCollected += 500
                score += 1500
                showEventMessage("+500 CREDITS BONUS SECURED", 2.0f)
            }
        }
    }

    private fun updateDynamicEvents(dtSec: Float) {
        if (!dynamicEventTriggered && missionTimeSec > 15f && missionInfo.hasDynamicEvent) {
            dynamicEventTriggered = true
            showEventMessage("DYNAMIC EVENT: ${missionInfo.eventDescription}", 4.0f)

            when (missionNumber) {
                3 -> {
                    // Convoy escort: spawn 3 friendly transport trucks traveling down highway
                    for (i in 0..2) {
                        friendlies.add(
                            FriendlyUnit(
                                id = nextEntityId++,
                                x = screenWidth * 0.35f + i * 70f,
                                y = screenHeight + 20f + i * 50f,
                                name = "Bravo-${i + 1}",
                                hp = 80f,
                                maxHp = 80f,
                                vy = -1.2f
                            )
                        )
                    }
                    // Spawn gunships attacking them
                    enemies.add(Enemy(nextEntityId++, EnemyCategory.AIR_GUNSHIP, screenWidth * 0.3f, -50f, 0f, 1.4f, 120f, 120f, false, 1200L, 0L))
                    enemies.add(Enemy(nextEntityId++, EnemyCategory.AIR_GUNSHIP, screenWidth * 0.7f, -70f, 0f, 1.4f, 120f, 120f, false, 1200L, 0L))
                }
                5 -> {
                    // Ace Razor arrival!
                    showEventMessage("WARNING: ACE 'RAZOR' DETECTED IN SECTOR", 4.0f)
                    soundManager.playMissileWarning()
                    enemies.add(
                        Enemy(
                            id = nextEntityId++,
                            category = EnemyCategory.AIR_ACE_RAZOR,
                            x = screenWidth / 2f,
                            y = -60f,
                            vx = 0f,
                            vy = 1.0f,
                            hp = 450f,
                            maxHp = 450f,
                            isGround = false,
                            shootCooldownMs = 800L,
                            points = 2500,
                            credits = 800,
                            aceName = "Ace Razor"
                        )
                    )
                }
                7 -> {
                    // Rescue Helo extraction
                    friendlies.add(
                        FriendlyUnit(
                            id = nextEntityId++,
                            x = screenWidth / 2f,
                            y = screenHeight * 0.5f,
                            name = "Dustoff-1 (Rescue Helo)",
                            hp = 120f,
                            maxHp = 120f,
                            vy = 0f,
                            isExtracting = true
                        )
                    )
                    // Drone swarm attacking helo
                    for (i in 0..4) {
                        enemies.add(Enemy(nextEntityId++, EnemyCategory.AIR_DRONE, screenWidth * (0.15f + i * 0.18f), -40f, 0f, 2.5f, 35f, 35f, false, 2000L))
                    }
                }
                8 -> {
                    // Ace Blackout
                    showEventMessage("WARNING: STEALTH ACE 'BLACKOUT' DETECTED", 4.0f)
                    soundManager.playMissileWarning()
                    enemies.add(
                        Enemy(
                            id = nextEntityId++,
                            category = EnemyCategory.AIR_ACE_BLACKOUT,
                            x = screenWidth / 2f,
                            y = -60f,
                            vx = 0f,
                            vy = 0.8f,
                            hp = 550f,
                            maxHp = 550f,
                            isGround = false,
                            shootCooldownMs = 900L,
                            points = 3500,
                            credits = 1000,
                            aceName = "Ace Blackout"
                        )
                    )
                }
                10 -> {
                    // Mission 10 Boss: Goliath Aerial Fortress
                    spawnGoliathBoss()
                }
            }
        }

        // Update friendly units
        val fIterator = friendlies.iterator()
        while (fIterator.hasNext()) {
            val f = fIterator.next()
            f.y += f.vy
            if (f.hp <= 0f) {
                convoyProtected = false
                createExplosion(f.x, f.y, 1.2f, Color(0xFFEF4444))
                soundManager.playExplosion()
                fIterator.remove()
                showEventMessage("ALLIED ASSET LOST!", 2.0f)
            } else if (f.y < -50f) {
                // Escaped to safety
                score += 1500
                creditsCollected += 300
                fIterator.remove()
                showEventMessage("ALLIED CONVOY REACHED FRIENDLY LINES!", 2.5f)
            }
        }
    }

    private fun spawnGoliathBoss() {
        if (bossSpawned) return
        bossSpawned = true
        showEventMessage("WARNING: GOLIATH AIRBORNE FORTRESS ENGAGED!", 4.0f)
        soundManager.playMissileWarning()

        val coreX = screenWidth / 2f
        val coreY = -180f

        // Goliath core
        enemies.add(
            Enemy(
                id = nextEntityId++,
                category = EnemyCategory.BOSS_GOLIATH_CORE,
                x = coreX,
                y = coreY,
                hp = 950f,
                maxHp = 950f,
                isGround = false,
                shootCooldownMs = 1200L,
                points = 10000,
                credits = 2500,
                bossModuleKey = "CORE"
            )
        )
        // Left Flak Turret
        enemies.add(
            Enemy(
                id = nextEntityId++,
                category = EnemyCategory.BOSS_TURRET_LEFT,
                x = coreX - 120f,
                y = coreY + 20f,
                hp = 220f,
                maxHp = 220f,
                isGround = false,
                shootCooldownMs = 1500L,
                points = 1500,
                credits = 300,
                bossModuleKey = "TURRET_LEFT"
            )
        )
        // Right Flak Turret
        enemies.add(
            Enemy(
                id = nextEntityId++,
                category = EnemyCategory.BOSS_TURRET_RIGHT,
                x = coreX + 120f,
                y = coreY + 20f,
                hp = 220f,
                maxHp = 220f,
                isGround = false,
                shootCooldownMs = 1500L,
                points = 1500,
                credits = 300,
                bossModuleKey = "TURRET_RIGHT"
            )
        )
        // Missile Salvo Bay
        enemies.add(
            Enemy(
                id = nextEntityId++,
                category = EnemyCategory.BOSS_MISSILE_BAY,
                x = coreX,
                y = coreY - 40f,
                hp = 280f,
                maxHp = 280f,
                isGround = false,
                shootCooldownMs = 2800L,
                points = 2000,
                credits = 500,
                bossModuleKey = "MISSILE_BAY"
            )
        )
        // Left Jet Engine
        enemies.add(
            Enemy(
                id = nextEntityId++,
                category = EnemyCategory.BOSS_ENGINE_LEFT,
                x = coreX - 70f,
                y = coreY + 60f,
                hp = 180f,
                maxHp = 180f,
                isGround = false,
                shootCooldownMs = 2000L,
                points = 1000,
                credits = 250,
                bossModuleKey = "ENGINE_LEFT"
            )
        )
        // Right Jet Engine
        enemies.add(
            Enemy(
                id = nextEntityId++,
                category = EnemyCategory.BOSS_ENGINE_RIGHT,
                x = coreX + 70f,
                y = coreY + 60f,
                hp = 180f,
                maxHp = 180f,
                isGround = false,
                shootCooldownMs = 2000L,
                points = 1000,
                credits = 250,
                bossModuleKey = "ENGINE_RIGHT"
            )
        )
    }

    private var spawnTimer = 0f
    private fun updateSpawnWave(dtSec: Float) {
        spawnTimer += dtSec
        val spawnInterval = if (missionInfo.isBossMission) 3.5f else 1.8f

        if (spawnTimer >= spawnInterval && missionTimeSec < missionDurationSec) {
            spawnTimer = 0f
            spawnMissionWave()
        }
    }

    private fun spawnMissionWave() {
        val spawnX = Random.nextFloat() * (screenWidth - 200f) + 100f
        val isAir = Random.nextFloat() < 0.65f

        if (isAir) {
            when (Random.nextInt(4)) {
                0 -> {
                    // Scout formation (3 in chevron)
                    enemies.add(Enemy(nextEntityId++, EnemyCategory.AIR_SCOUT, spawnX, -40f, 0f, 2.2f, 40f, 40f, false, 1400L))
                    enemies.add(Enemy(nextEntityId++, EnemyCategory.AIR_SCOUT, spawnX - 45f, -70f, 0f, 2.2f, 40f, 40f, false, 1400L))
                    enemies.add(Enemy(nextEntityId++, EnemyCategory.AIR_SCOUT, spawnX + 45f, -70f, 0f, 2.2f, 40f, 40f, false, 1400L))
                }
                1 -> {
                    // Fast Interceptor
                    enemies.add(Enemy(nextEntityId++, EnemyCategory.AIR_INTERCEPTOR, spawnX, -40f, 0f, 3.2f, 65f, 65f, false, 1100L))
                }
                2 -> {
                    // Heavy Gunship
                    enemies.add(Enemy(nextEntityId++, EnemyCategory.AIR_GUNSHIP, spawnX, -70f, 0f, 1.2f, 140f, 140f, false, 1600L))
                }
                3 -> {
                    // Attack Helo
                    enemies.add(Enemy(nextEntityId++, EnemyCategory.AIR_ATTACK_HELO, spawnX, -50f, 0f, 1.5f, 75f, 75f, false, 1300L))
                }
            }
        } else {
            // Ground targets
            when (Random.nextInt(4)) {
                0 -> {
                    // SAM launcher
                    enemies.add(Enemy(nextEntityId++, EnemyCategory.GROUND_SAM, spawnX, -60f, 0f, 0f, 85f, 85f, true, 2600L, credits = 80))
                }
                1 -> {
                    // Flak cannon
                    enemies.add(Enemy(nextEntityId++, EnemyCategory.GROUND_FLAK, spawnX, -60f, 0f, 0f, 70f, 70f, true, 1800L, credits = 70))
                }
                2 -> {
                    // Fuel depot (destructible chain reaction)
                    enemies.add(Enemy(nextEntityId++, EnemyCategory.GROUND_FUEL_DEPOT, spawnX, -60f, 0f, 0f, 50f, 50f, true, 99999L, points = 300, credits = 150))
                }
                3 -> {
                    // Naval cruiser (if coastal/water) or radar installation
                    if (missionNumber == 6) {
                        enemies.add(Enemy(nextEntityId++, EnemyCategory.NAVAL_CRUISER, spawnX, -100f, 0f, 0.4f, 260f, 260f, true, 2200L, points = 800, credits = 300))
                    } else if (missionNumber == 9) {
                        enemies.add(Enemy(nextEntityId++, EnemyCategory.GROUND_BRIDGE, screenWidth / 2f, -80f, 0f, 0f, 220f, 220f, true, 99999L, points = 1000, credits = 400))
                    } else {
                        enemies.add(Enemy(nextEntityId++, EnemyCategory.GROUND_RADAR, spawnX, -60f, 0f, 0f, 60f, 60f, true, 99999L, credits = 90))
                    }
                }
            }
        }
    }

    private fun checkMissionStatus() {
        if (!player.isAlive || isGameOver || isVictory) return

        if (missionInfo.isBossMission) {
            // Victory triggered when Goliath core is destroyed
            // Handled in destroyEnemy
        } else {
            if (missionTimeSec >= missionDurationSec && enemies.none { !it.isGround && !it.isDestroyed }) {
                isVictory = true
                showEventMessage("MISSION OBJECTIVES ACCOMPLISHED!", 4.0f)
            }
        }
    }

    fun calculateStars(): Int {
        if (!isVictory) return 0
        var stars = 1 // Completed sortie
        val hpRatio = player.hp / player.maxHp
        if (hpRatio >= 0.60f) stars++ // Good hull integrity
        if (groundTargetsDestroyed >= 2 || convoyProtected) stars++ // Secondary completed
        return stars.coerceIn(1, 3)
    }

    fun createExplosion(x: Float, y: Float, scale: Float = 1.0f, color: Color = Color(0xFFF97316)) {
        if (particles.size > 80) return
        val count = (12 * scale).toInt().coerceAtMost(16)
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 6.28f
            val speed = (Random.nextFloat() * 5f + 2f) * scale
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    life = 0.4f * scale,
                    maxLife = 0.4f * scale,
                    size = (Random.nextFloat() * 6f + 3f) * scale,
                    color = if (Random.nextBoolean()) color else Color(0xFFFFFBEB)
                )
            )
        }
        // Lingering smoke
        val smokeCount = (4 * scale).toInt().coerceAtMost(6)
        for (i in 0 until smokeCount) {
            particles.add(
                Particle(
                    x = x + (Random.nextFloat() - 0.5f) * 16f * scale,
                    y = y + (Random.nextFloat() - 0.5f) * 16f * scale,
                    vx = (Random.nextFloat() - 0.5f) * 1.2f,
                    vy = -Random.nextFloat() * 2f - 1f,
                    life = 0.7f * scale,
                    maxLife = 0.7f * scale,
                    size = (Random.nextFloat() * 10f + 6f) * scale,
                    color = Color(0x66334155)
                )
            )
        }
    }

    private fun updateParticles(dtSec: Float) {
        val pIterator = particles.iterator()
        while (pIterator.hasNext()) {
            val p = pIterator.next()
            p.life -= dtSec
            p.x += p.vx
            p.y += p.vy
            if (p.life <= 0f) pIterator.remove()
        }

        val sIterator = shockwaves.iterator()
        while (sIterator.hasNext()) {
            val s = sIterator.next()
            s.radius += (s.maxRadius - s.radius) * (8f * dtSec)
            s.alpha -= dtSec * 1.5f
            if (s.alpha <= 0f) sIterator.remove()
        }
    }

    fun triggerScreenShake(amount: Float) {
        screenShakeAmount = amount.coerceAtMost(25f)
    }

    fun showEventMessage(msg: String, durationSec: Float = 3.0f) {
        eventMessage = msg
        eventMessageTimer = durationSec
    }

    fun getHudState(): GameHudState {
        // Find nearest incoming hostile missile targeting player
        var nearestMissileDistSq = Float.MAX_VALUE
        var nearestMissileAngleDeg = 180f
        var hasHostileMissile = false
        for (i in 0 until missiles.size) {
            val m = missiles[i]
            if (!m.isPlayer && m.active) {
                val dx = m.x - player.x
                val dy = m.y - player.y
                val distSq = dx * dx + dy * dy
                if (distSq < nearestMissileDistSq) {
                    nearestMissileDistSq = distSq
                    hasHostileMissile = true
                    // Angle in degrees from player towards missile (90 = 6 o'clock rear)
                    nearestMissileAngleDeg = (atan2(dy, dx) * (180f / 3.14159265f) + 360f) % 360f
                }
            }
        }
        val isWarning = hasHostileMissile
        val distPx = if (hasHostileMissile) sqrt(nearestMissileDistSq) else 0f
        val distMeters = (distPx * 1.6f).toInt()
        val threatLevel = when {
            !hasHostileMissile -> MissileThreatLevel.NONE
            distPx < 320f -> MissileThreatLevel.CRITICAL
            distPx < 650f -> MissileThreatLevel.WARNING
            else -> MissileThreatLevel.NORMAL
        }

        var bossRatio: Float? = null
        for (i in 0 until enemies.size) {
            val e = enemies[i]
            if (e.category == EnemyCategory.BOSS_GOLIATH_CORE && !e.isDestroyed) {
                bossRatio = e.hp / e.maxHp
                break
            }
        }

        val specCooldownRatio = if (player.specialWeaponId.cooldownMs > 0) {
            1.0f - (player.specialCooldown.toFloat() / player.specialWeaponId.cooldownMs.toFloat()).coerceIn(0f, 1f)
        } else 1.0f

        val safeHeight = if (screenHeight > 0f) screenHeight else 1920f
        val normY = (1.0f - (player.y / safeHeight)).coerceIn(0f, 1f)
        val altitudeBase = (12000 + normY * 16000).toInt()
        val deltaY = player.y - player.targetY
        val climbRate = (deltaY * 40f).toInt().coerceIn(-6000, 6000)

        val baseSpeed = when (player.aircraftId) {
            AircraftId.X29_GHOST -> 780
            AircraftId.A10_WARHAWK -> 410
            AircraftId.F35_SHADOW -> 640
            AircraftId.FA22_PHANTOM -> 720
            AircraftId.ARC_WASP -> 700
        }
        val isAfterburn = deltaY > 15f || (player.powerUpTimerSec > 0f && player.activePowerUp == PowerUpType.RAPID_FIRE)
        val speedDynamic = baseSpeed + (if (isAfterburn) 140 else 0) + (climbRate / 120).coerceIn(-50, 80)
        val mach = speedDynamic / 661.47f

        // Target locking logic
        var hasLock = false
        var lockedEnemy: Enemy? = null
        var minLockDistSq = Float.MAX_VALUE
        for (i in 0 until enemies.size) {
            val e = enemies[i]
            if (!e.isDestroyed && Math.abs(e.x - player.x) < 110f && e.y < player.y && (player.y - e.y) < 750f) {
                val distSq = (e.x - player.x) * (e.x - player.x) + (e.y - player.y) * (e.y - player.y)
                if (distSq < minLockDistSq) {
                    minLockDistSq = distSq
                    lockedEnemy = e
                    hasLock = true
                }
            }
        }
        val lockedName = lockedEnemy?.let {
            when (it.category) {
                EnemyCategory.AIR_LIGHT_INTERCEPTOR, EnemyCategory.AIR_INTERCEPTOR -> "SU-35 FLANKER"
                EnemyCategory.AIR_HEAVY_FIGHTER -> "SU-57 FELON"
                EnemyCategory.AIR_STRIKE_AIRCRAFT -> "SU-34 FULLBACK"
                EnemyCategory.AIR_BOMBER, EnemyCategory.AIR_STEALTH_BOMBER -> "TU-160 BLACKJACK"
                EnemyCategory.AIR_ATTACK_HELO -> "KA-52 HOKUM"
                EnemyCategory.AIR_STEALTH_AIRCRAFT -> "J-20 DRAGON"
                EnemyCategory.AIR_SWARM_DRONE, EnemyCategory.AIR_DRONE -> "UAV DRONE"
                EnemyCategory.AIR_ACE_RAZOR, EnemyCategory.AIR_ACE_BLACKOUT -> it.aceName ?: "HOSTILE ACE"
                EnemyCategory.GROUND_SAM -> "SA-10 SAM"
                EnemyCategory.GROUND_FLAK -> "ZSU-23 FLAK"
                EnemyCategory.GROUND_RADAR -> "RADAR TOWER"
                EnemyCategory.GROUND_FUEL_DEPOT -> "FUEL DEPOT"
                EnemyCategory.BOSS_GOLIATH_CORE -> "GOLIATH CORE"
                else -> "TARGET"
            }
        }
        val lockedDist = if (hasLock) (sqrt(minLockDistSq) * 1.5f).toInt() else 0

        // Wave & Objective progression
        val waveNumber = ((missionTimeSec / (missionDurationSec / 5f)).toInt() + 1).coerceIn(1, 5)
        val isSamMission = missionNumber in listOf(2, 4, 7)
        val objTarget = when {
            missionInfo.isBossMission -> 1
            isSamMission -> 5
            else -> 15
        }
        val objCurrent = when {
            missionInfo.isBossMission -> if (bossSpawned && enemies.none { it.category == EnemyCategory.BOSS_GOLIATH_CORE && !it.isDestroyed }) 1 else 0
            isSamMission -> groundTargetsDestroyed.coerceAtMost(objTarget)
            else -> enemiesDestroyed.coerceAtMost(objTarget)
        }
        val objDesc = when {
            missionInfo.isBossMission -> "DESTROY GOLIATH FORTRESS"
            isSamMission -> "DESTROY SAM BATTERY"
            else -> "NEUTRALIZE AIR SQUADRONS"
        }

        val rocketAmmo = if (player.secondaryWeaponId == SecondaryWeaponId.HYDRA_ROCKET_PODS) player.secondaryAmmo else 4

        return GameHudState(
            score = score,
            creditsEarned = creditsCollected,
            playerHpRatio = (player.hp / player.maxHp).coerceIn(0f, 1f),
            playerShieldRatio = if (player.maxShield > 0) (player.shield / player.maxShield).coerceIn(0f, 1f) else 0f,
            missileCount = player.secondaryAmmo,
            rocketCount = rocketAmmo,
            specialCooldownRatio = specCooldownRatio,
            isMissileWarning = isWarning,
            missileWarningDistance = distPx,
            missileWarningAngleDeg = nearestMissileAngleDeg,
            missileThreatLevel = threatLevel,
            missileWarningDistanceMeters = distMeters,
            eventMessage = eventMessage,
            bossHpRatio = bossRatio,
            isGameOver = isGameOver,
            isVictory = isVictory,
            starsAwarded = if (isVictory) calculateStars() else 0,
            altitudeFeet = altitudeBase,
            climbRateFpm = climbRate,
            speedKnots = speedDynamic,
            machNumber = mach,
            isAfterburnerActive = isAfterburn,
            activePrimaryWeapon = player.primaryWeaponId,
            activeSecondaryWeapon = player.secondaryWeaponId,
            activeSpecialWeapon = player.specialWeaponId,
            isTargetLocked = hasLock,
            lockedTargetName = lockedName,
            lockedTargetDistanceMeters = lockedDist,
            airTargetsDestroyed = (enemiesDestroyed - groundTargetsDestroyed).coerceAtLeast(0),
            groundTargetsDestroyed = groundTargetsDestroyed,
            damageTaken = totalDamageTaken,
            missionTimeSec = missionTimeSec,
            baseRewardCredits = missionInfo.rewardCredits,
            objectiveDescription = objDesc,
            objectiveCurrent = objCurrent,
            objectiveTarget = objTarget,
            currentWave = waveNumber,
            totalWaves = 5,
            activePowerUp = player.activePowerUp,
            powerUpRemainingSec = player.powerUpTimerSec,
            powerUpMaxSec = 9.0f,
            lastCollectedPowerUp = lastCollectedPowerUp,
            powerUpNotificationTimer = powerUpNotificationTimer
        )
    }
}
