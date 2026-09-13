package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.ui.components.JetHUD
import com.example.ui.components.TacticalTopHud
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.GameHudState
import com.example.engine.GameRenderer
import com.example.model.SecondaryWeaponId
import com.example.model.SpecialWeaponId

@Composable
fun CombatGameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val engine = viewModel.gameEngine
    val playerJetSprite = ImageBitmap.imageResource(id = com.example.R.drawable.player_fighter_jet)

    // Category 1: 7 Enemy Aircraft Sprites
    val lightInterceptorSprite = ImageBitmap.imageResource(id = com.example.R.drawable.enemy_light_interceptor)
    val heavyFighterSprite = ImageBitmap.imageResource(id = com.example.R.drawable.enemy_heavy_fighter)
    val strikeAircraftSprite = ImageBitmap.imageResource(id = com.example.R.drawable.enemy_strike_aircraft)
    val bomberSprite = ImageBitmap.imageResource(id = com.example.R.drawable.enemy_bomber)
    val attackHeloSprite = ImageBitmap.imageResource(id = com.example.R.drawable.enemy_attack_helo)
    val stealthAircraftSprite = ImageBitmap.imageResource(id = com.example.R.drawable.enemy_stealth_aircraft)
    val swarmDroneSprite = ImageBitmap.imageResource(id = com.example.R.drawable.enemy_swarm_drone)
    val aceRazorSprite = ImageBitmap.imageResource(id = com.example.R.drawable.enemy_ace_razor)
    val enemyScoutSprite = ImageBitmap.imageResource(id = com.example.R.drawable.enemy_fighter_scout)
    val enemyInterceptorSprite = ImageBitmap.imageResource(id = com.example.R.drawable.enemy_fighter_interceptor)
    val enemyGunshipSprite = ImageBitmap.imageResource(id = com.example.R.drawable.enemy_gunship_heavy)

    // Category 2: 6 Ground Target Sprites
    val groundHangarSprite = ImageBitmap.imageResource(id = com.example.R.drawable.ground_military_hangar)
    val groundRadarSprite = ImageBitmap.imageResource(id = com.example.R.drawable.ground_radar_dish)
    val groundBridgeSprite = ImageBitmap.imageResource(id = com.example.R.drawable.ground_bridge)
    val navalCruiserSprite = ImageBitmap.imageResource(id = com.example.R.drawable.naval_battleship_cruiser)
    val groundMilitaryBaseSprite = ImageBitmap.imageResource(id = com.example.R.drawable.ground_military_base)
    val groundVehicleConvoySprite = ImageBitmap.imageResource(id = com.example.R.drawable.ground_vehicle_convoy)

    // Category 3: 6 Environment Background Textures
    val bgDesertSprite = ImageBitmap.imageResource(id = com.example.R.drawable.bg_desert)
    val bgCoastSprite = ImageBitmap.imageResource(id = com.example.R.drawable.bg_coast)
    val bgMountainSprite = ImageBitmap.imageResource(id = com.example.R.drawable.bg_mountain)
    val bgUrbanSprite = ImageBitmap.imageResource(id = com.example.R.drawable.bg_urban)
    val bgArcticSprite = ImageBitmap.imageResource(id = com.example.R.drawable.bg_arctic)
    val bgMilitaryFacilitySprite = ImageBitmap.imageResource(id = com.example.R.drawable.bg_military_facility)

    // Category 4: Boss 10 Airborne Fortress Sprite
    val bossFortressSprite = ImageBitmap.imageResource(id = com.example.R.drawable.boss_airborne_fortress)

    val enemySprites = remember(
        lightInterceptorSprite, heavyFighterSprite, strikeAircraftSprite, bomberSprite,
        attackHeloSprite, stealthAircraftSprite, swarmDroneSprite, aceRazorSprite,
        groundHangarSprite, groundRadarSprite, groundBridgeSprite, navalCruiserSprite,
        groundMilitaryBaseSprite, groundVehicleConvoySprite, bgDesertSprite, bgCoastSprite,
        bgMountainSprite, bgUrbanSprite, bgArcticSprite, bgMilitaryFacilitySprite, bossFortressSprite
    ) {
        com.example.engine.EnemySpritePack(
            scout = enemyScoutSprite,
            interceptor = enemyInterceptorSprite,
            gunship = enemyGunshipSprite,
            ace = aceRazorSprite,
            lightInterceptor = lightInterceptorSprite,
            heavyFighter = heavyFighterSprite,
            strikeAircraft = strikeAircraftSprite,
            bomber = bomberSprite,
            attackHelo = attackHeloSprite,
            stealthAircraft = stealthAircraftSprite,
            swarmDrone = swarmDroneSprite,
            aceRazor = aceRazorSprite,
            groundHangar = groundHangarSprite,
            groundRadar = groundRadarSprite,
            groundBridge = groundBridgeSprite,
            navalCruiser = navalCruiserSprite,
            groundMilitaryBase = groundMilitaryBaseSprite,
            groundVehicleConvoy = groundVehicleConvoySprite,
            bgDesert = bgDesertSprite,
            bgCoast = bgCoastSprite,
            bgMountain = bgMountainSprite,
            bgUrban = bgUrbanSprite,
            bgArctic = bgArcticSprite,
            bgMilitaryFacility = bgMilitaryFacilitySprite,
            bossAirborneFortress = bossFortressSprite
        )
    }
    var hudState by remember { mutableStateOf(GameHudState()) }
    var isPaused by remember { mutableStateOf(false) }
    var renderTick by remember { mutableLongStateOf(0L) }

    // High performance 60 FPS update loop
    LaunchedEffect(isPaused) {
        if (!isPaused) {
            var lastFrameTimeMs = 0L
            var hudTimer = 0f
            while (true) {
                withFrameMillis { frameTimeMs ->
                    if (lastFrameTimeMs != 0L) {
                        val dt = ((frameTimeMs - lastFrameTimeMs) / 1000f).coerceIn(0.001f, 0.05f)
                        engine.update(dt)
                        renderTick++

                        hudTimer += dt
                        // Throttle Compose HUD tree recomposition to ~12 Hz (every 80ms) for high stability & battery efficiency
                        if (hudTimer >= 0.08f || engine.isGameOver || engine.isVictory) {
                            hudTimer = 0f
                            hudState = engine.getHudState()
                        }
                    }
                    lastFrameTimeMs = frameTimeMs
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .onSizeChanged { size ->
                engine.screenWidth = size.width.toFloat()
                engine.screenHeight = size.height.toFloat()
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        engine.onTouchMove(offset.x, offset.y)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        engine.onTouchMove(change.position.x, change.position.y)
                    }
                )
            }
    ) {
        // Render Canvas (Hardware-accelerated 60 FPS Arcade Visuals)
        Canvas(modifier = Modifier.fillMaxSize().testTag("game_combat_canvas")) {
            renderTick.let { /* Invalidate only canvas on each 60 FPS tick */ }
            GameRenderer.render(this, engine, playerJetSprite, enemySprites)
        }

        // Top Tactical HUD Bar:
        // Top Left: Mission Objective + Progress (e.g. 2/5)
        // Top Center: Wave 04
        // Top Right: Credits + Score + Pause Button
        TacticalTopHud(
            objectiveTitle = hudState.objectiveDescription,
            objectiveProgress = "${hudState.objectiveCurrent}/${hudState.objectiveTarget}",
            waveNumber = hudState.currentWave,
            isBossWave = hudState.bossHpRatio != null,
            credits = hudState.creditsEarned,
            score = hudState.score,
            isPaused = isPaused,
            onPauseClick = {
                isPaused = !isPaused
                engine.isPaused = isPaused
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Tactical Dynamic Event Notification Banner
        AnimatedVisibility(
            visible = hudState.eventMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 54.dp, start = 16.dp, end = 16.dp)
        ) {
            hudState.eventMessage?.let { msg ->
                Card(
                    modifier = Modifier.testTag("event_message_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xEE0F172A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CrisisAlert,
                            contentDescription = "Event Alert",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = msg,
                            color = Color(0xFFF8FAFC),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Jet Tactical Heads-Up Display (Altitude, Speed, Flight Reticle, Active Weapon Status)
        JetHUD(
            hudState = hudState,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            onSecondaryClick = { engine.fireSecondaryWeapon() },
            onSpecialClick = { engine.fireSpecialWeapon() }
        )

        // Pause Menu Dialog
        if (isPaused) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xCC000000)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF38BDF8))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "SORTIE PAUSED",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    isPaused = false
                                    engine.isPaused = false
                                },
                                modifier = Modifier.fillMaxWidth().testTag("resume_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                            ) {
                                Text("RESUME COMBAT", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    viewModel.navigateTo(AppScreen.HANGAR)
                                },
                                modifier = Modifier.fillMaxWidth().testTag("abort_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                            ) {
                                Text("ABORT TO HANGAR", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Victory / Mission Complete Debriefing Screen
        if (hudState.isVictory) {
            MissionResultsScreen(
                missionInfo = engine.missionInfo,
                hudState = hudState,
                onContinue = { totalCredits ->
                    viewModel.finishMission(
                        victory = true,
                        stars = hudState.starsAwarded,
                        score = hudState.score,
                        credits = totalCredits
                    )
                    viewModel.navigateTo(AppScreen.MISSION_SELECT)
                },
                onUpgradeAircraft = { totalCredits ->
                    viewModel.finishMission(
                        victory = true,
                        stars = hudState.starsAwarded,
                        score = hudState.score,
                        credits = totalCredits
                    )
                    viewModel.navigateTo(AppScreen.HANGAR)
                },
                soundManager = viewModel.soundManager
            )
        }

        // Defeat Screen
        if (hudState.isGameOver) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xEE090D16)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Defeat",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "AIRCRAFT DESTROYED",
                                color = Color(0xFFEF4444),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Ejection sequence confirmed. Sortie failed.",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    viewModel.startSortie(engine.missionNumber)
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("retry_sortie_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("RETRY SORTIE", color = Color(0xFF0F172A), fontWeight = FontWeight.Black)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    viewModel.navigateTo(AppScreen.HANGAR)
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("defeat_hangar_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("RETURN TO HANGAR", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
