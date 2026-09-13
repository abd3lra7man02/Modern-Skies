package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PlayerProfile::class,
        AircraftUnlock::class,
        SystemUpgrade::class,
        MissionProgress::class,
        AceEncounter::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ModernSkiesDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: ModernSkiesDatabase? = null

        fun getInstance(context: Context): ModernSkiesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ModernSkiesDatabase::class.java,
                    "modern_skies.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getInstance(context).gameDao()
                            // Seed default player profile
                            dao.insertProfile(
                                PlayerProfile(
                                    id = 1,
                                    credits = 2500,
                                    totalStars = 0,
                                    selectedAircraft = "FA22_PHANTOM",
                                    selectedWingman = "ATTACK_VIPER",
                                    selectedPrimary = "VULCAN_20MM",
                                    selectedSecondary = "HEAT_SEEKING_AIM9",
                                    selectedSpecial = "FLARE_DISPENSER",
                                    highestUnlockedMission = 1
                                )
                            )
                            // Seed aircraft
                            dao.insertAircraft(
                                listOf(
                                    AircraftUnlock("FA22_PHANTOM", isUnlocked = true, level = 1),
                                    AircraftUnlock("X29_GHOST", isUnlocked = false, level = 1),
                                    AircraftUnlock("A10_WARHAWK", isUnlocked = false, level = 1),
                                    AircraftUnlock("F35_SHADOW", isUnlocked = false, level = 1)
                                )
                            )
                            // Seed upgrades
                            dao.insertUpgrades(
                                listOf(
                                    SystemUpgrade("CANNON_DMG", 1),
                                    SystemUpgrade("CANNON_RATE", 1),
                                    SystemUpgrade("MISSILE_DMG", 1),
                                    SystemUpgrade("MISSILE_CAP", 1),
                                    SystemUpgrade("ARMOR_HP", 1),
                                    SystemUpgrade("SHIELD_CAP", 1)
                                )
                            )
                            // Seed 10 Missions
                            val missions = (1..10).map { num ->
                                MissionProgress(
                                    missionNumber = num,
                                    stars = 0,
                                    highScore = 0,
                                    isCompleted = false
                                )
                            }
                            dao.insertMissionProgress(missions)

                            // Seed Aces
                            dao.insertAces(
                                listOf(
                                    AceEncounter("ACE_RAZOR", 0, false),
                                    AceEncounter("ACE_BLACKOUT", 0, false),
                                    AceEncounter("ACE_IRONCLAD", 0, false)
                                )
                            )
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
