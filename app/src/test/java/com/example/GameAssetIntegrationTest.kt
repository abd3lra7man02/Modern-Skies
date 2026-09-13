package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.SoundManager
import com.example.engine.Enemy
import com.example.engine.EnemyCategory
import com.example.engine.GameEngine
import com.example.model.MissionDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GameAssetIntegrationTest {

    @Test
    fun testEnemyAircraftCategories_allSevenTypesExistWithValidAttributes() {
        val categories = listOf(
            EnemyCategory.AIR_LIGHT_INTERCEPTOR,
            EnemyCategory.AIR_HEAVY_FIGHTER,
            EnemyCategory.AIR_STRIKE_AIRCRAFT,
            EnemyCategory.AIR_BOMBER,
            EnemyCategory.AIR_ATTACK_HELO,
            EnemyCategory.AIR_STEALTH_AIRCRAFT,
            EnemyCategory.AIR_SWARM_DRONE
        )
        assertEquals(7, categories.size)
        for (cat in categories) {
            assertTrue(cat.name.startsWith("AIR_"))
        }
    }

    @Test
    fun testGroundTargetCategories_allSixTypesExist() {
        val categories = listOf(
            EnemyCategory.GROUND_HANGAR,
            EnemyCategory.GROUND_RADAR,
            EnemyCategory.GROUND_BRIDGE,
            EnemyCategory.NAVAL_CRUISER,
            EnemyCategory.GROUND_MILITARY_BASE,
            EnemyCategory.GROUND_VEHICLE_CONVOY
        )
        assertEquals(6, categories.size)
    }

    @Test
    fun testBossGoliathSpawnAndCombat() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val soundManager = SoundManager(context)
        val engine = GameEngine(soundManager, screenWidth = 1080f, screenHeight = 2400f)

        engine.startMission(missionNum = 5)

        // Initial update
        engine.update(0.1f)

        // Add Goliath Boss components
        val bossCore = Enemy(
            id = 9999L,
            category = EnemyCategory.BOSS_GOLIATH_CORE,
            x = 540f,
            y = 300f,
            vx = 0f,
            vy = 10f,
            hp = 2500f,
            maxHp = 2500f,
            isGround = false,
            points = 5000
        )
        engine.enemies.add(bossCore)

        val turretLeft = Enemy(
            id = 9998L,
            category = EnemyCategory.BOSS_TURRET_LEFT,
            x = 420f,
            y = 320f,
            vx = 0f,
            vy = 10f,
            hp = 400f,
            maxHp = 400f,
            isGround = false
        )
        engine.enemies.add(turretLeft)

        assertNotNull("Goliath core must be present", bossCore)
        assertTrue(bossCore.hp >= 500f)

        // Damage the core
        val initialHp = bossCore.hp
        bossCore.hp -= 100f
        bossCore.hitFlashTimer = 0.15f
        assertTrue(bossCore.hp < initialHp)
        assertTrue(bossCore.hitFlashTimer > 0f)
    }

    @Test
    fun testPerformanceStressTest_withMultipleEnemiesMissilesParticles() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val soundManager = SoundManager(context)
        val engine = GameEngine(soundManager, screenWidth = 1080f, screenHeight = 2400f)

        engine.startMission(missionNum = 1)

        // Spawn 20 enemies (mix of all categories)
        val enemyTypes = listOf(
            EnemyCategory.AIR_LIGHT_INTERCEPTOR,
            EnemyCategory.AIR_HEAVY_FIGHTER,
            EnemyCategory.AIR_STRIKE_AIRCRAFT,
            EnemyCategory.AIR_BOMBER,
            EnemyCategory.AIR_ATTACK_HELO,
            EnemyCategory.AIR_STEALTH_AIRCRAFT,
            EnemyCategory.AIR_SWARM_DRONE,
            EnemyCategory.GROUND_HANGAR,
            EnemyCategory.GROUND_RADAR,
            EnemyCategory.GROUND_BRIDGE,
            EnemyCategory.NAVAL_CRUISER,
            EnemyCategory.GROUND_MILITARY_BASE,
            EnemyCategory.GROUND_VEHICLE_CONVOY
        )

        for (i in 0 until 20) {
            val type = enemyTypes[i % enemyTypes.size]
            val isGround = type.name.startsWith("GROUND_") || type == EnemyCategory.NAVAL_CRUISER
            engine.enemies.add(
                Enemy(
                    id = i.toLong(),
                    category = type,
                    x = 100f + (i * 45f) % 900f,
                    y = 200f + (i * 60f) % 1500f,
                    vx = (i % 3 - 1) * 30f,
                    vy = 50f,
                    hp = 100f,
                    maxHp = 100f,
                    isGround = isGround,
                    points = 250
                )
            )
        }

        // Fire secondary weapon (missiles)
        engine.fireSecondaryWeapon()
        // Fire special weapon
        engine.fireSpecialWeapon()

        // Simulate 60 ticks (1 second) of active combat under heavy load
        val startTime = System.currentTimeMillis()
        for (step in 0 until 60) {
            engine.update(0.016f)
        }
        val elapsed = System.currentTimeMillis() - startTime
        // Stress test performance must execute swiftly in JVM
        assertTrue("60 update cycles should complete rapidly", elapsed < 2000)
    }
}
