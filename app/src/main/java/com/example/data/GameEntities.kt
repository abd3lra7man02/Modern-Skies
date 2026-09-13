package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfile(
    @PrimaryKey val id: Int = 1,
    val credits: Int = 1200,
    val totalStars: Int = 0,
    val selectedAircraft: String = "FA22_PHANTOM",
    val selectedWingman: String = "ATTACK_VIPER",
    val selectedPrimary: String = "VULCAN_20MM",
    val selectedSecondary: String = "HEAT_SEEKING_AIM9",
    val selectedSpecial: String = "FLARE_DISPENSER",
    val highestUnlockedMission: Int = 1,
    val sfxEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val screenShakeEnabled: Boolean = true,
    val touchSensitivity: Float = 1.2f
)

@Entity(tableName = "aircraft_unlocks")
data class AircraftUnlock(
    @PrimaryKey val aircraftId: String,
    val isUnlocked: Boolean,
    val level: Int = 1,
    val camoIndex: Int = 0
)

@Entity(tableName = "system_upgrades")
data class SystemUpgrade(
    @PrimaryKey val upgradeKey: String, // CANNON_DMG, CANNON_RATE, MISSILE_DMG, MISSILE_CAP, ARMOR_HP, SHIELD_CAP
    val level: Int = 1
)

@Entity(tableName = "mission_progress")
data class MissionProgress(
    @PrimaryKey val missionNumber: Int,
    val stars: Int = 0,
    val highScore: Int = 0,
    val isCompleted: Boolean = false,
    val bestTimeSeconds: Int = 0
)

@Entity(tableName = "ace_encounters")
data class AceEncounter(
    @PrimaryKey val aceId: String,
    val defeatedCount: Int = 0,
    val unlockedBlueprint: Boolean = false
)
