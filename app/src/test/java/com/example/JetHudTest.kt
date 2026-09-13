package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.engine.GameHudState
import com.example.engine.MissileThreatLevel
import com.example.engine.PowerUpType
import com.example.model.PrimaryWeaponId
import com.example.model.SecondaryWeaponId
import com.example.model.SpecialWeaponId
import com.example.ui.components.JetHUD
import com.example.ui.components.TacticalTopHud
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class JetHudTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testTacticalHUD_displaysTopHudAndTacticalSidePanels() {
        var secondaryFired = false
        var specialFired = false
        var pauseClicked = false

        val testHudState = GameHudState(
            score = 14500,
            creditsEarned = 850,
            playerHpRatio = 0.85f,
            playerShieldRatio = 0.70f,
            missileCount = 8,
            rocketCount = 4,
            specialCooldownRatio = 1.0f,
            isMissileWarning = true,
            missileThreatLevel = MissileThreatLevel.WARNING,
            missileWarningDistanceMeters = 850,
            missileWarningAngleDeg = 180f,
            activePrimaryWeapon = PrimaryWeaponId.VULCAN_20MM,
            activeSecondaryWeapon = SecondaryWeaponId.HEAT_SEEKING_AIM9,
            activeSpecialWeapon = SpecialWeaponId.FLARE_DISPENSER,
            isTargetLocked = true,
            lockedTargetName = "MIG-29 FULCRUM",
            objectiveDescription = "DESTROY ENEMY SQUADRONS",
            objectiveCurrent = 3,
            objectiveTarget = 5,
            currentWave = 4,
            activePowerUp = PowerUpType.DOUBLE_DAMAGE,
            powerUpRemainingSec = 6.5f
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    TacticalTopHud(
                        objectiveTitle = testHudState.objectiveDescription,
                        objectiveProgress = "${testHudState.objectiveCurrent}/${testHudState.objectiveTarget}",
                        waveNumber = testHudState.currentWave,
                        isBossWave = false,
                        credits = testHudState.creditsEarned,
                        score = testHudState.score,
                        isPaused = false,
                        onPauseClick = { pauseClicked = true },
                        modifier = Modifier.align(Alignment.TopCenter)
                    )

                    JetHUD(
                        hudState = testHudState,
                        onSecondaryClick = { secondaryFired = true },
                        onSpecialClick = { specialFired = true }
                    )
                }
            }
        }

        // Verify Top HUD components
        composeTestRule.onNodeWithTag("tactical_top_hud").assertIsDisplayed()
        composeTestRule.onNodeWithText("DESTROY ENEMY SQUADRONS").assertIsDisplayed()
        composeTestRule.onNodeWithText("PROG: 3/5").assertIsDisplayed()
        composeTestRule.onNodeWithText("WAVE").assertIsDisplayed()
        composeTestRule.onNodeWithText("04").assertIsDisplayed()
        composeTestRule.onNodeWithText("+850 CR").assertIsDisplayed()

        // Verify Left Side Tactical Player HP/Shield Bars
        composeTestRule.onNodeWithTag("hud_player_status_side_panel").assertIsDisplayed()
        composeTestRule.onNodeWithText("85%").assertIsDisplayed()
        composeTestRule.onNodeWithText("70%").assertIsDisplayed()

        // Verify Right Side Weapon Status Panel (Missile ×08, Rocket ×04)
        composeTestRule.onNodeWithTag("hud_weapon_status_side_panel").assertIsDisplayed()
        composeTestRule.onNodeWithText("×08").assertIsDisplayed()
        composeTestRule.onNodeWithText("×04").assertIsDisplayed()

        // Verify Missile Warning System
        composeTestRule.onNodeWithTag("hud_missile_warning_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText("MISSILE WARNING").assertIsDisplayed()

        // Verify Active Power-Up Countdown
        composeTestRule.onNodeWithTag("hud_active_powerup_timer").assertIsDisplayed()
        composeTestRule.onNodeWithText("DOUBLE DAMAGE").assertIsDisplayed()

        // Verify Bottom Controls & Weapon Triggers
        composeTestRule.onNodeWithTag("secondary_weapon_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("special_weapon_button").assertIsDisplayed()

        // Test interaction
        composeTestRule.onNodeWithTag("secondary_weapon_button").performClick()
        assertTrue("Secondary weapon should be triggered", secondaryFired)

        composeTestRule.onNodeWithTag("special_weapon_button").performClick()
        assertTrue("Special weapon should be triggered", specialFired)
    }
}
