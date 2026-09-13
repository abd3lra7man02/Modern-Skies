package com.example.engine

import androidx.compose.ui.graphics.Color
import com.example.model.AircraftId
import com.example.model.PrimaryWeaponId
import com.example.model.SecondaryWeaponId
import com.example.model.SpecialWeaponId
import com.example.model.WingmanId

enum class MissileThreatLevel {
    NONE,
    NORMAL,    // > 600px (~1200m)
    WARNING,   // 300px - 600px (~600m - 1200m)
    CRITICAL   // < 300px (< 600m)
}

enum class PowerUpType(val label: String, val color: Color) {
    DOUBLE_DAMAGE("DOUBLE DAMAGE", Color(0xFFF97316)),
    RAPID_FIRE("RAPID FIRE", Color(0xFFEAB308)),
    UNLIMITED_MISSILES("UNLIMITED MISSILES", Color(0xFFEF4444)),
    MEGA_BOMB("MEGA BOMB", Color(0xFFDC2626)),
    SHIELD("DEFLECTOR SHIELD", Color(0xFF00E5FF)),
    EMP("EMP BLAST", Color(0xFF818CF8)),
    REPAIR("HULL REPAIR", Color(0xFF22C55E)),
    CREDIT_BONUS("CREDIT BONUS", Color(0xFFFBBF24)),
    // Backwards-compatible aliases
    REPAIR_HULL("HULL REPAIR", Color(0xFF22C55E)),
    SHIELD_BOOST("DEFLECTOR SHIELD", Color(0xFF00E5FF)),
    MISSILE_RESTOCK("AIM-9 SALVO", Color(0xFFEF4444)),
    CREDIT_CRATE("CREDIT BONUS", Color(0xFFFBBF24))
}

data class PowerUp(
    val id: Long,
    var x: Float,
    var y: Float,
    val type: PowerUpType,
    val vy: Float = 1.6f
)

enum class EnemyCategory {
    // Air enemies (7 distinct requested types)
    AIR_LIGHT_INTERCEPTOR,
    AIR_HEAVY_FIGHTER,
    AIR_STRIKE_AIRCRAFT,
    AIR_BOMBER,
    AIR_ATTACK_HELO,
    AIR_STEALTH_AIRCRAFT,
    AIR_SWARM_DRONE,
    // Aces
    AIR_ACE_RAZOR,
    AIR_ACE_BLACKOUT,
    // Ground & naval targets
    GROUND_SAM,
    GROUND_FLAK,
    GROUND_RADAR,
    GROUND_FUEL_DEPOT,
    GROUND_HANGAR,
    GROUND_BRIDGE,
    GROUND_MILITARY_BASE,
    GROUND_VEHICLE_CONVOY,
    NAVAL_CRUISER,
    // Boss components
    BOSS_GOLIATH_CORE,
    BOSS_TURRET_LEFT,
    BOSS_TURRET_RIGHT,
    BOSS_MISSILE_BAY,
    BOSS_ENGINE_LEFT,
    BOSS_ENGINE_RIGHT,
    // Aliases for compatibility
    AIR_SCOUT,
    AIR_INTERCEPTOR,
    AIR_GUNSHIP,
    AIR_STEALTH_BOMBER,
    AIR_DRONE
}

data class Enemy(
    val id: Long,
    val category: EnemyCategory,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 1.5f,
    var hp: Float,
    val maxHp: Float,
    val isGround: Boolean,
    var shootCooldownMs: Long = 1000L,
    var shootTimerMs: Long = 0L,
    var patternTimer: Float = 0f,
    val points: Int = 100,
    val credits: Int = 50,
    val aceName: String? = null,
    val bossModuleKey: String? = null,
    var isDestroyed: Boolean = false,
    var isEmpDisabledTimer: Float = 0f,
    var hitFlashTimer: Float = 0f
)

data class Bullet(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val isPlayer: Boolean,
    val damage: Float,
    val isHeavy: Boolean = false,
    val color: Color = Color(0xFFFBBF24),
    var active: Boolean = true
)

data class Missile(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var targetEnemyId: Long? = null,
    val isPlayer: Boolean,
    val damage: Float,
    var lifeSec: Float = 4.5f,
    var active: Boolean = true,
    var targetPlayer: Boolean = false
)

data class Rocket(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val damage: Float,
    val blastRadius: Float,
    var active: Boolean = true
)

data class Bomb(
    var x: Float,
    var y: Float,
    var vy: Float = 2.8f,
    var scale: Float = 1.0f,
    var progress: Float = 0f,
    val damage: Float = 150f,
    val blastRadius: Float = 110f,
    var active: Boolean = true
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    val maxLife: Float,
    val size: Float,
    val color: Color,
    val isSparks: Boolean = false
)

data class Shockwave(
    var x: Float,
    var y: Float,
    var radius: Float,
    val maxRadius: Float,
    var alpha: Float,
    val color: Color
)

data class FriendlyUnit(
    val id: Long,
    var x: Float,
    var y: Float,
    val name: String,
    var hp: Float,
    val maxHp: Float,
    var vy: Float = 0.5f,
    var isExtracting: Boolean = false,
    var active: Boolean = true
)

data class PlayerState(
    var x: Float = 0f,
    var y: Float = 0f,
    var targetX: Float = 0f,
    var targetY: Float = 0f,
    var bankAngle: Float = 0f, // tilt when moving left/right (-30 to +30 deg)
    var hp: Float = 150f,
    var maxHp: Float = 150f,
    var shield: Float = 50f,
    var maxShield: Float = 50f,
    var primaryCooldown: Long = 0L,
    var secondaryCooldown: Long = 0L,
    var specialCooldown: Long = 0L,
    var secondaryAmmo: Int = 8,
    var specialActiveTimerSec: Float = 0f,
    var powerUpTimerSec: Float = 0f,
    var activePowerUp: PowerUpType? = null,
    var aircraftId: AircraftId = AircraftId.FA22_PHANTOM,
    var wingmanId: WingmanId = WingmanId.ATTACK_VIPER,
    var primaryWeaponId: PrimaryWeaponId = PrimaryWeaponId.VULCAN_20MM,
    var secondaryWeaponId: SecondaryWeaponId = SecondaryWeaponId.HEAT_SEEKING_AIM9,
    var specialWeaponId: SpecialWeaponId = SpecialWeaponId.FLARE_DISPENSER,
    var cannonLevel: Int = 1,
    var missileLevel: Int = 1,
    var armorLevel: Int = 1,
    var engineLevel: Int = 1,
    var muzzleFlashTimer: Float = 0f,
    var hitFlashTimer: Float = 0f,
    var isAlive: Boolean = true
)
