package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.AircraftUnlock
import com.example.data.GameRepository
import com.example.data.MissionProgress
import com.example.data.ModernSkiesDatabase
import com.example.data.PlayerProfile
import com.example.data.SystemUpgrade
import com.example.engine.GameEngine
import com.example.model.AircraftId
import com.example.model.MissionDatabase
import com.example.model.MissionInfo
import com.example.model.PrimaryWeaponId
import com.example.model.SecondaryWeaponId
import com.example.model.SpecialWeaponId
import com.example.model.WingmanId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    MAIN_MENU,
    HANGAR,
    LOADOUT,
    MISSION_SELECT,
    IN_GAME
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ModernSkiesDatabase.getInstance(application)
    val repository = GameRepository(db.gameDao())
    val soundManager = SoundManager(application)

    val gameEngine = GameEngine(soundManager)

    private val _currentScreen = MutableStateFlow(AppScreen.MAIN_MENU)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedMissionNumber = MutableStateFlow(1)
    val selectedMissionNumber: StateFlow<Int> = _selectedMissionNumber.asStateFlow()

    val profile: StateFlow<PlayerProfile?> = repository.playerProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allAircraft: StateFlow<List<AircraftUnlock>> = repository.allAircraft
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUpgrades: StateFlow<List<SystemUpgrade>> = repository.allUpgrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val missionProgress: StateFlow<List<MissionProgress>> = repository.missionProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectMission(number: Int) {
        _selectedMissionNumber.value = number
    }

    fun selectAircraft(id: AircraftId) {
        viewModelScope.launch {
            val current = profile.value ?: return@launch
            repository.updateLoadout(
                aircraft = id.name,
                wingman = current.selectedWingman,
                primary = current.selectedPrimary,
                secondary = current.selectedSecondary,
                special = current.selectedSpecial
            )
        }
    }

    fun selectWingman(id: WingmanId) {
        viewModelScope.launch {
            val current = profile.value ?: return@launch
            repository.updateLoadout(
                aircraft = current.selectedAircraft,
                wingman = id.name,
                primary = current.selectedPrimary,
                secondary = current.selectedSecondary,
                special = current.selectedSpecial
            )
        }
    }

    fun selectPrimary(id: PrimaryWeaponId) {
        viewModelScope.launch {
            val current = profile.value ?: return@launch
            repository.updateLoadout(
                aircraft = current.selectedAircraft,
                wingman = current.selectedWingman,
                primary = id.name,
                secondary = current.selectedSecondary,
                special = current.selectedSpecial
            )
        }
    }

    fun selectSecondary(id: SecondaryWeaponId) {
        viewModelScope.launch {
            val current = profile.value ?: return@launch
            repository.updateLoadout(
                aircraft = current.selectedAircraft,
                wingman = current.selectedWingman,
                primary = current.selectedPrimary,
                secondary = id.name,
                special = current.selectedSpecial
            )
        }
    }

    fun selectSpecial(id: SpecialWeaponId) {
        viewModelScope.launch {
            val current = profile.value ?: return@launch
            repository.updateLoadout(
                aircraft = current.selectedAircraft,
                wingman = current.selectedWingman,
                primary = current.selectedPrimary,
                secondary = current.selectedSecondary,
                special = id.name
            )
        }
    }

    fun unlockAircraft(id: AircraftId) {
        viewModelScope.launch {
            val success = repository.unlockAircraftWithCredits(id.name, id.unlockCost)
            if (success) {
                soundManager.playPowerup()
            }
        }
    }

    fun purchaseUpgrade(key: String, cost: Int) {
        viewModelScope.launch {
            val success = repository.purchaseUpgrade(key, cost)
            if (success) {
                soundManager.playPowerup()
            }
        }
    }

    fun startSortie(missionNum: Int) {
        val prof = profile.value ?: PlayerProfile()
        val upgradesMap = allUpgrades.value.associate { it.upgradeKey to it.level }

        val aircraft = AircraftId.fromString(prof.selectedAircraft)
        val wingman = WingmanId.fromString(prof.selectedWingman)
        val primary = PrimaryWeaponId.fromString(prof.selectedPrimary)
        val secondary = SecondaryWeaponId.fromString(prof.selectedSecondary)
        val special = SpecialWeaponId.fromString(prof.selectedSpecial)

        gameEngine.startMission(
            missionNum = missionNum,
            aircraftId = aircraft,
            wingmanId = wingman,
            primary = primary,
            secondary = secondary,
            special = special,
            upgrades = upgradesMap
        )
        _currentScreen.value = AppScreen.IN_GAME
    }

    fun startSurvivalMode() {
        _selectedMissionNumber.value = 5
        startSortie(5)
    }

    fun startBossRush() {
        _selectedMissionNumber.value = 10
        startSortie(10)
    }

    fun claimEmergencyCredits(amount: Int = 1000) {
        viewModelScope.launch {
            repository.addCredits(amount)
            soundManager.playPowerup()
        }
    }

    fun finishMission(victory: Boolean, stars: Int, score: Int, credits: Int) {
        viewModelScope.launch {
            if (victory) {
                repository.completeMission(
                    missionNumber = _selectedMissionNumber.value,
                    starsEarned = stars,
                    score = score,
                    creditsReward = credits
                )
            }
        }
    }

    fun toggleSfx(enabled: Boolean) {
        soundManager.isSoundEnabled = enabled
        viewModelScope.launch {
            val prof = profile.value ?: return@launch
            repository.updateSettings(
                sfx = enabled,
                vibration = prof.vibrationEnabled,
                shake = prof.screenShakeEnabled,
                sensitivity = prof.touchSensitivity
            )
        }
    }

    fun toggleVibration(enabled: Boolean) {
        soundManager.isVibrationEnabled = enabled
        viewModelScope.launch {
            val prof = profile.value ?: return@launch
            repository.updateSettings(
                sfx = prof.sfxEnabled,
                vibration = enabled,
                shake = prof.screenShakeEnabled,
                sensitivity = prof.touchSensitivity
            )
        }
    }
}
