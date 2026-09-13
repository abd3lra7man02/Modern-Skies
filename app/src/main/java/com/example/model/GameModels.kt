package com.example.model

enum class AircraftId(
    val displayName: String,
    val role: String,
    val speedMultiplier: Float,
    val baseHealth: Float,
    val baseShield: Float,
    val damageMultiplier: Float,
    val description: String,
    val unlockCost: Int
) {
    FA22_PHANTOM(
        displayName = "F/A-22 Phantom",
        role = "Multirole Air Superiority",
        speedMultiplier = 1.0f,
        baseHealth = 150f,
        baseShield = 50f,
        damageMultiplier = 1.0f,
        description = "Balanced twin-engine stealth fighter. Excellent hull integrity and versatile weapon hardpoints.",
        unlockCost = 0
    ),
    X29_GHOST(
        displayName = "X-29 Ghost",
        role = "Fast Interceptor",
        speedMultiplier = 1.35f,
        baseHealth = 100f,
        baseShield = 60f,
        damageMultiplier = 1.25f,
        description = "Forward-swept delta agility. High evasive maneuverability, fast missile recharge, but lighter armor.",
        unlockCost = 3500
    ),
    A10_WARHAWK(
        displayName = "A-10 Warhawk",
        role = "Heavy Strike Fighter",
        speedMultiplier = 0.82f,
        baseHealth = 240f,
        baseShield = 40f,
        damageMultiplier = 1.4f,
        description = "Titanium-armored flying tank. Devastating heavy rotary firepower and amplified bomb/rocket yield.",
        unlockCost = 4500
    ),
    ARC_WASP(
        displayName = "ARC-Wasp Mk.V",
        role = "Multi-Role Tactical Interceptor",
        speedMultiplier = 1.15f,
        baseHealth = 160f,
        baseShield = 70f,
        damageMultiplier = 1.25f,
        description = "Advanced tactical delta-wing fighter featuring dual cannon cowlings, enhanced missile bays, and high-G agility.",
        unlockCost = 2000
    ),
    F35_SHADOW(
        displayName = "F-35C Shadow",
        role = "5th-Gen Stealth Strike",
        speedMultiplier = 1.12f,
        baseHealth = 130f,
        baseShield = 90f,
        damageMultiplier = 1.2f,
        description = "Advanced low-observable stealth. Instant missile target acquisition and reinforced active energy shielding.",
        unlockCost = 6000
    );

    companion object {
        fun fromString(id: String): AircraftId = entries.firstOrNull { it.name == id } ?: FA22_PHANTOM
    }
}

enum class WingmanId(
    val displayName: String,
    val role: String,
    val description: String,
    val unlockCost: Int
) {
    NONE("Solo Flight", "No Escort", "Fly with no AI wingman support.", 0),
    ATTACK_VIPER("Viper", "Attack Wingman", "Twin 20mm cannons that track and fire upon enemy fighters.", 1500),
    DEFENSIVE_AEGIS("Aegis", "Defensive Wingman", "Point-defense laser system that automatically shoots down incoming missiles.", 2200),
    RECON_HAWK("Hawk", "Recon Wingman", "Targeting telemetry providing +35% critical strike damage and early warning.", 2800),
    DRONE_SWARM("Specter", "Autonomous Drone", "Deployable orbiting combat drones that perform kamikaze dive attacks.", 3500);

    companion object {
        fun fromString(id: String): WingmanId = entries.firstOrNull { it.name == id } ?: ATTACK_VIPER
    }
}

enum class PrimaryWeaponId(
    val displayName: String,
    val caliber: String,
    val fireRateMs: Long,
    val baseDamage: Float,
    val description: String
) {
    VULCAN_20MM("M61 Vulcan", "20mm Twin Cannon", 110L, 18f, "Rapid cycle twin cannon with high velocity and balanced spread."),
    ROTARY_30MM("GAU-8 Avenger", "30mm Heavy Gatling", 155L, 34f, "Devastating armor-piercing depleted uranium rounds. Destroys armored targets."),
    PLASMA_REPEATER("Pulse Repeater", "Kinetic Energy Pulse", 130L, 26f, "Experimental focused energy rounds with impact shockwaves.");

    companion object {
        fun fromString(id: String): PrimaryWeaponId = entries.firstOrNull { it.name == id } ?: VULCAN_20MM
    }
}

enum class SecondaryWeaponId(
    val displayName: String,
    val typeName: String,
    val maxAmmo: Int,
    val cooldownMs: Long,
    val baseDamage: Float,
    val description: String
) {
    HEAT_SEEKING_AIM9("AIM-9X Sidewinder", "Homing Missiles", 8, 1400L, 75f, "Infrared guided missiles that track enemy aircraft with smoke trails."),
    HYDRA_ROCKET_PODS("Hydra 70 Pod", "Unguided Rocket Volley", 12, 1100L, 55f, "Fires a rapid 4-rocket fan spread. Ideal for clearing formations and ground bunkers."),
    BUNKER_BUSTER_BOMBS("GBU-28 Penetrator", "Ground Bombs", 6, 2000L, 140f, "Heavy guided bomb causing massive seismic ground explosions.");

    companion object {
        fun fromString(id: String): SecondaryWeaponId = entries.firstOrNull { it.name == id } ?: HEAT_SEEKING_AIM9
    }
}

enum class SpecialWeaponId(
    val displayName: String,
    val typeName: String,
    val cooldownMs: Long,
    val durationMs: Long,
    val description: String
) {
    FLARE_DISPENSER("Defensive Flares", "Countermeasure", 6500L, 2000L, "Ejects hot decoy flares, instantly diverting and destroying all incoming missiles."),
    EMP_BLAST("EMP Shockwave", "Area Disable", 9000L, 4000L, "Emits an electromagnetic pulse that neutralizes all enemy weapons and drones."),
    AIR_STRIKE_CRUISE("Cruise Missile", "Support Strike", 12000L, 1000L, "Calls an allied supersonic cruise missile that wipes out the forward sector.");

    companion object {
        fun fromString(id: String): SpecialWeaponId = entries.firstOrNull { it.name == id } ?: FLARE_DISPENSER
    }
}

enum class MissionType(val displayName: String) {
    AIR_SUPERIORITY("Air Superiority"),
    ESCORT("Escort"),
    RECON("Recon"),
    STRIKE("Strike"),
    RESCUE("Rescue")
}

data class MissionInfo(
    val number: Int,
    val title: String,
    val codename: String,
    val theater: String,
    val briefing: String,
    val primaryObjective: String,
    val secondaryObjective: String,
    val hasDynamicEvent: Boolean,
    val eventDescription: String,
    val isBossMission: Boolean,
    val enemyAce: String? = null,
    val missionType: MissionType = MissionType.AIR_SUPERIORITY,
    val difficultyStars: Int = 1,
    val rewardCredits: Int = 1500
)

object MissionDatabase {
    val missions = listOf(
        MissionInfo(
            number = 1,
            title = "Desert Vanguard",
            codename = "OPERATION DUST DEVIL",
            theater = "Southern Arid Desert",
            briefing = "Hostile insurgent jets have established a forward outpost. Neutralize air patrols and eliminate the forward radar outpost.",
            primaryObjective = "Destroy 15 Hostile Aircraft",
            secondaryObjective = "Destroy 4 Ground SAM Towers",
            hasDynamicEvent = false,
            eventDescription = "",
            isBossMission = false,
            missionType = MissionType.STRIKE,
            difficultyStars = 1,
            rewardCredits = 1500
        ),
        MissionInfo(
            number = 2,
            title = "SAM Valley",
            codename = "OPERATION IRON SHIELD",
            theater = "Rocky Canyons",
            briefing = "Enemy air defenses are dense. Radar-guided SAM batteries are firing from the ravines. Evade missiles and bomb the missile silos.",
            primaryObjective = "Neutralize 6 SAM Sites",
            secondaryObjective = "Take less than 40% Hull Damage",
            hasDynamicEvent = true,
            eventDescription = "Hostile Runway Scramble: Interceptors taking off!",
            isBossMission = false,
            missionType = MissionType.RECON,
            difficultyStars = 1,
            rewardCredits = 2000
        ),
        MissionInfo(
            number = 3,
            title = "Convoy Defense",
            codename = "OPERATION GUARDIAN",
            theater = "Highway Delta",
            briefing = "Allied logistical convoy 'Bravo-7' is ambushed on the main artery. Provide close air support and destroy attacking attack gunships.",
            primaryObjective = "Protect Allied Convoy to Safety",
            secondaryObjective = "Destroy 8 Enemy Gunships",
            hasDynamicEvent = true,
            eventDescription = "Allied Convoy under heavy ground and air assault!",
            isBossMission = false,
            missionType = MissionType.ESCORT,
            difficultyStars = 2,
            rewardCredits = 2500
        ),
        MissionInfo(
            number = 4,
            title = "Runway Scramble",
            codename = "OPERATION CLOUD DIVE",
            theater = "Al-Zuhar Airbase",
            briefing = "Strike enemy military runway before their supersonic interceptor squadrons take flight. Destroy hangars and fuel depots.",
            primaryObjective = "Demolish 3 Hangars & 4 Fuel Depots",
            secondaryObjective = "Achieve 85%+ Accuracy",
            hasDynamicEvent = true,
            eventDescription = "Chain Reaction: Fuel depot detonation causes massive blasts!",
            isBossMission = false,
            missionType = MissionType.STRIKE,
            difficultyStars = 2,
            rewardCredits = 3000
        ),
        MissionInfo(
            number = 5,
            title = "Duel in the Clouds",
            codename = "OPERATION RED RAZOR",
            theater = "High Sierra Ridge",
            briefing = "The notorious enemy ace 'Razor' has intercepted your squadron in a modified MiG-35. Engage in high-G dogfight and shoot him down.",
            primaryObjective = "Defeat Enemy Ace 'Razor'",
            secondaryObjective = "Complete within 90 Seconds",
            hasDynamicEvent = true,
            eventDescription = "Warning: Ace Pilot Razor has locked onto your signature!",
            isBossMission = false,
            enemyAce = "Ace Razor",
            missionType = MissionType.AIR_SUPERIORITY,
            difficultyStars = 2,
            rewardCredits = 3500
        ),
        MissionInfo(
            number = 6,
            title = "Coastal Infiltration",
            codename = "OPERATION TIDAL HAMMER",
            theater = "Mediterranean Littoral",
            briefing = "Infiltrate enemy naval staging waters. Heavy missile cruisers are launching anti-ship missiles. Sink the warships and flak platforms.",
            primaryObjective = "Sink 2 Missile Cruisers",
            secondaryObjective = "Destroy 5 Flak Towers",
            hasDynamicEvent = true,
            eventDescription = "Allied Cruiser firing supporting Tomahawk barrage!",
            isBossMission = false,
            missionType = MissionType.STRIKE,
            difficultyStars = 2,
            rewardCredits = 4000
        ),
        MissionInfo(
            number = 7,
            title = "Extraction Overwatch",
            codename = "OPERATION VALKYRIE",
            theater = "Mountain Stronghold",
            briefing = "Allied 'Dustoff 1' helicopter is extracting downed pilots behind enemy lines. Defend the helicopter from swarming attack drones.",
            primaryObjective = "Extract Allied Aircrew Safely",
            secondaryObjective = "Destroy 20 Drone Attackers",
            hasDynamicEvent = true,
            eventDescription = "Rescue Helo 'Dustoff 1' under fire! Clear LZ!",
            isBossMission = false,
            missionType = MissionType.RESCUE,
            difficultyStars = 3,
            rewardCredits = 4500
        ),
        MissionInfo(
            number = 8,
            title = "Ghost of the Grid",
            codename = "OPERATION BLACKOUT",
            theater = "Sub-Zero Tundra",
            briefing = "Ace pilot 'Blackout' is using advanced electronic warfare to scramble radar. Counter his stealth attacks and destroy his stealth fighter.",
            primaryObjective = "Defeat Stealth Ace 'Blackout'",
            secondaryObjective = "Deploy Countermeasures 4 times",
            hasDynamicEvent = true,
            eventDescription = "Radar Jamming active: Hostile cloaking signature detected!",
            isBossMission = false,
            enemyAce = "Ace Blackout",
            missionType = MissionType.RECON,
            difficultyStars = 3,
            rewardCredits = 5000
        ),
        MissionInfo(
            number = 9,
            title = "Iron Fortress Breach",
            codename = "OPERATION CITADEL",
            theater = "Industrial River Valley",
            briefing = "The main enemy defense network. Heavily fortified bridges, radar complexes, and gunship squadrons stand between us and the command post.",
            primaryObjective = "Destroy 2 Reinforced River Bridges",
            secondaryObjective = "Score over 8,000 Points",
            hasDynamicEvent = true,
            eventDescription = "Bridge collapse blocks hostile tank reinforcements!",
            isBossMission = false,
            missionType = MissionType.STRIKE,
            difficultyStars = 3,
            rewardCredits = 6000
        ),
        MissionInfo(
            number = 10,
            title = "OPERATION GOLIATH",
            codename = "FALL OF THE TITAN",
            theater = "Stratosphere Over Fortress Zero",
            briefing = "The ultimate aerial threat: The 'Goliath' Heavy Airborne Dreadnought. Destroy its flak turrets, missile bays, engines, and exposed core!",
            primaryObjective = "Destroy the Goliath Aerial Fortress",
            secondaryObjective = "Destroy All 4 Fortress Sub-Systems",
            hasDynamicEvent = true,
            eventDescription = "BOSS BATTLE: Goliath Airborne Fortress engaged!",
            isBossMission = true,
            missionType = MissionType.AIR_SUPERIORITY,
            difficultyStars = 3,
            rewardCredits = 10000
        )
    )
}

