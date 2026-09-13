package com.example.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val dao: GameDao) {

    val playerProfile: Flow<PlayerProfile?> = dao.getProfile()
    val allAircraft: Flow<List<AircraftUnlock>> = dao.getAllAircraft()
    val allUpgrades: Flow<List<SystemUpgrade>> = dao.getAllUpgrades()
    val missionProgress: Flow<List<MissionProgress>> = dao.getAllMissionProgress()
    val aces: Flow<List<AceEncounter>> = dao.getAllAces()

    suspend fun getProfileSync(): PlayerProfile? = dao.getProfileSync()

    suspend fun saveProfile(profile: PlayerProfile) {
        dao.insertProfile(profile)
    }

    suspend fun addCredits(amount: Int) {
        dao.addCredits(amount)
    }

    suspend fun updateLoadout(
        aircraft: String,
        wingman: String,
        primary: String,
        secondary: String,
        special: String
    ) {
        val current = dao.getProfileSync() ?: PlayerProfile()
        dao.updateProfile(
            current.copy(
                selectedAircraft = aircraft,
                selectedWingman = wingman,
                selectedPrimary = primary,
                selectedSecondary = secondary,
                selectedSpecial = special
            )
        )
    }

    suspend fun unlockAircraftWithCredits(aircraftId: String, cost: Int): Boolean {
        val current = dao.getProfileSync() ?: return false
        if (current.credits >= cost) {
            dao.deductCredits(cost)
            dao.unlockAircraft(aircraftId)
            return true
        }
        return false
    }

    suspend fun purchaseUpgrade(upgradeKey: String, cost: Int): Boolean {
        val current = dao.getProfileSync() ?: return false
        if (current.credits >= cost) {
            dao.deductCredits(cost)
            dao.incrementUpgrade(upgradeKey)
            return true
        }
        return false
    }

    suspend fun completeMission(
        missionNumber: Int,
        starsEarned: Int,
        score: Int,
        creditsReward: Int
    ) {
        dao.updateMissionResult(missionNumber, starsEarned, score)
        dao.addCredits(creditsReward)

        val current = dao.getProfileSync()
        if (current != null) {
            val nextMission = (missionNumber + 1).coerceAtMost(10)
            val newHighest = maxOf(current.highestUnlockedMission, nextMission)
            dao.updateProfile(current.copy(highestUnlockedMission = newHighest))
        }
    }

    suspend fun recordAceKill(aceId: String) {
        dao.recordAceDefeat(aceId)
        dao.addCredits(1500) // Bonus for taking down an Ace
    }

    suspend fun updateSettings(sfx: Boolean, vibration: Boolean, shake: Boolean, sensitivity: Float) {
        val current = dao.getProfileSync() ?: return
        dao.updateProfile(
            current.copy(
                sfxEnabled = sfx,
                vibrationEnabled = vibration,
                screenShakeEnabled = shake,
                touchSensitivity = sensitivity
            )
        )
    }
}
