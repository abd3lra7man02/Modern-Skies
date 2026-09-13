package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM player_profile WHERE id = 1")
    fun getProfile(): Flow<PlayerProfile?>

    @Query("SELECT * FROM player_profile WHERE id = 1")
    suspend fun getProfileSync(): PlayerProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: PlayerProfile)

    @Update
    suspend fun updateProfile(profile: PlayerProfile)

    @Query("SELECT * FROM aircraft_unlocks")
    fun getAllAircraft(): Flow<List<AircraftUnlock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAircraft(aircraft: List<AircraftUnlock>)

    @Query("UPDATE aircraft_unlocks SET isUnlocked = 1 WHERE aircraftId = :id")
    suspend fun unlockAircraft(id: String)

    @Query("SELECT * FROM system_upgrades")
    fun getAllUpgrades(): Flow<List<SystemUpgrade>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpgrades(upgrades: List<SystemUpgrade>)

    @Query("UPDATE system_upgrades SET level = level + 1 WHERE upgradeKey = :key")
    suspend fun incrementUpgrade(key: String)

    @Query("SELECT * FROM mission_progress ORDER BY missionNumber ASC")
    fun getAllMissionProgress(): Flow<List<MissionProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissionProgress(missions: List<MissionProgress>)

    @Query("UPDATE mission_progress SET stars = :stars, highScore = :score, isCompleted = 1 WHERE missionNumber = :missionNumber")
    suspend fun updateMissionResult(missionNumber: Int, stars: Int, score: Int)

    @Query("UPDATE player_profile SET credits = credits + :amount WHERE id = 1")
    suspend fun addCredits(amount: Int)

    @Query("UPDATE player_profile SET credits = credits - :amount WHERE id = 1 AND credits >= :amount")
    suspend fun deductCredits(amount: Int): Int

    @Query("SELECT * FROM ace_encounters")
    fun getAllAces(): Flow<List<AceEncounter>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAces(aces: List<AceEncounter>)

    @Query("UPDATE ace_encounters SET defeatedCount = defeatedCount + 1, unlockedBlueprint = 1 WHERE aceId = :aceId")
    suspend fun recordAceDefeat(aceId: String)
}
