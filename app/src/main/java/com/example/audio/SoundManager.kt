package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.sin
import kotlin.random.Random

class SoundManager(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var isSoundEnabled: Boolean = true
    var isVibrationEnabled: Boolean = true

    private val sampleRate = 22050
    private val scope = CoroutineScope(Dispatchers.Default)

    // Hardware-accelerated native SoundPool for instant, zero-latency, multi-channel game audio
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(12)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundIds = ConcurrentHashMap<String, Int>()
    private var lastVibrateTime = 0L

    init {
        scope.launch {
            try {
                val soundsDir = File(context.cacheDir, "sounds").apply { mkdirs() }
                
                val soundGenerators = mapOf(
                    "cannon" to generateCannonPcm(false),
                    "heavy_cannon" to generateCannonPcm(true),
                    "missile_launch" to generateMissileLaunchPcm(),
                    "rocket_launch" to generateRocketLaunchPcm(),
                    "explosion" to generateExplosionPcm(0.35f, 0.7f),
                    "big_explosion" to generateExplosionPcm(0.65f, 1.0f),
                    "missile_warning" to generateWarningBeepPcm(),
                    "lock_on" to generateLockOnPcm(),
                    "flare" to generateFlarePcm(),
                    "emp" to generateEmpPcm(),
                    "powerup" to generatePowerupPcm(),
                    "hit" to generateHitPcm()
                )

                for ((key, pcm) in soundGenerators) {
                    val file = File(soundsDir, "$key.wav")
                    if (!file.exists() || file.length() == 0L) {
                        val wavBytes = pcmToWav(pcm)
                        FileOutputStream(file).use { it.write(wavBytes) }
                    }
                    val id = soundPool.load(file.absolutePath, 1)
                    if (id != 0) {
                        soundIds[key] = id
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun playSound(key: String, volume: Float = 0.8f) {
        if (!isSoundEnabled) return
        val soundId = soundIds[key] ?: return
        try {
            val v = volume.coerceIn(0f, 1f)
            soundPool.play(soundId, v, v, 1, 0, 1.0f)
        } catch (_: Exception) {}
    }

    fun playCannon() = playSound("cannon", 0.45f)
    fun playHeavyCannon() = playSound("heavy_cannon", 0.65f)
    fun playMissileLaunch() = playSound("missile_launch", 0.7f)
    fun playRocketLaunch() = playSound("rocket_launch", 0.7f)
    fun playExplosion() {
        playSound("explosion", 0.75f)
        vibrate(30)
    }
    fun playBigExplosion() {
        playSound("big_explosion", 0.95f)
        vibrate(55)
    }
    fun playMissileWarning() = playSound("missile_warning", 0.85f)
    fun playLockOn() = playSound("lock_on", 0.55f)
    fun playFlare() = playSound("flare", 0.65f)
    fun playEmp() = playSound("emp", 0.8f)
    fun playPowerup() = playSound("powerup", 0.75f)
    fun playButtonClick() {
        playSound("lock_on", 0.4f)
        vibrate(10)
    }
    fun playHit() {
        playSound("hit", 0.35f)
        vibrate(12)
    }

    fun vibrate(durationMs: Long) {
        if (!isVibrationEnabled || vibrator == null || !vibrator.hasVibrator()) return
        val now = System.currentTimeMillis()
        if (now - lastVibrateTime < 100L) return // Throttle vibrations to prevent driver queue stall
        lastVibrateTime = now
        try {
            val safeDuration = durationMs.coerceIn(5L, 40L)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(safeDuration, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(safeDuration)
            }
        } catch (_: Exception) {}
    }

    private fun pcmToWav(pcmData: ByteArray): ByteArray {
        val totalDataLen = pcmData.size + 36
        val channels = 1
        val byteRate = sampleRate * channels * 2
        val header = ByteArray(44)

        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // PCM
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * 2).toByte()
        header[33] = 0
        header[34] = 16
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (pcmData.size and 0xff).toByte()
        header[41] = ((pcmData.size shr 8) and 0xff).toByte()
        header[42] = ((pcmData.size shr 16) and 0xff).toByte()
        header[43] = ((pcmData.size shr 24) and 0xff).toByte()

        val wavBytes = ByteArray(44 + pcmData.size)
        System.arraycopy(header, 0, wavBytes, 0, 44)
        System.arraycopy(pcmData, 0, wavBytes, 44, pcmData.size)
        return wavBytes
    }

    // --- Waveform Generators ---

    private fun generateCannonPcm(heavy: Boolean): ByteArray {
        val durationMs = if (heavy) 120 else 70
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        val baseFreq = if (heavy) 90.0 else 180.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val decay = (1.0 - progress) * (1.0 - progress)

            val sine = sin(2.0 * Math.PI * baseFreq * t)
            val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.45
            val sample = (sine * 0.55 + noise) * decay * 32767.0
            buffer[i] = sample.toInt().coerceIn(-32768, 32767).toShort()
        }
        return shortArrayToByteArray(buffer)
    }

    private fun generateMissileLaunchPcm(): ByteArray {
        val durationMs = 280
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val freq = 220.0 + progress * 880.0
            val t = i.toDouble() / sampleRate
            val sine = sin(2.0 * Math.PI * freq * t)
            val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.4
            val decay = if (progress < 0.2) progress / 0.2 else (1.0 - progress) / 0.8
            val sample = (sine * 0.6 + noise) * decay * 30000.0
            buffer[i] = sample.toInt().coerceIn(-32768, 32767).toShort()
        }
        return shortArrayToByteArray(buffer)
    }

    private fun generateRocketLaunchPcm(): ByteArray {
        val durationMs = 220
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val freq = 160.0 + progress * 500.0
            val t = i.toDouble() / sampleRate
            val sine = sin(2.0 * Math.PI * freq * t)
            val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.55
            val decay = (1.0 - progress)
            val sample = (sine * 0.45 + noise) * decay * 29000.0
            buffer[i] = sample.toInt().coerceIn(-32768, 32767).toShort()
        }
        return shortArrayToByteArray(buffer)
    }

    private fun generateExplosionPcm(durationSec: Float, intensity: Float): ByteArray {
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        var lastVal = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val decay = (1.0 - progress) * (1.0 - progress)
            val whiteNoise = (Random.nextDouble() * 2.0 - 1.0)
            // Low-pass filter for heavy thud
            val alpha = 0.15
            lastVal = lastVal + alpha * (whiteNoise - lastVal)
            val subBass = sin(2.0 * Math.PI * 45.0 * (i.toDouble() / sampleRate)) * 0.4
            val sample = (lastVal * 0.7 + subBass) * decay * intensity * 32767.0
            buffer[i] = sample.toInt().coerceIn(-32768, 32767).toShort()
        }
        return shortArrayToByteArray(buffer)
    }

    private fun generateWarningBeepPcm(): ByteArray {
        val durationMs = 180
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val freq = if (progress < 0.5) 900.0 else 1250.0
            val t = i.toDouble() / sampleRate
            val sine = sin(2.0 * Math.PI * freq * t)
            val sample = sine * 28000.0
            buffer[i] = sample.toInt().coerceIn(-32768, 32767).toShort()
        }
        return shortArrayToByteArray(buffer)
    }

    private fun generateLockOnPcm(): ByteArray {
        val durationMs = 120
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val sine = sin(2.0 * Math.PI * 1100.0 * t)
            val decay = 1.0 - (i.toDouble() / numSamples)
            val sample = sine * decay * 26000.0
            buffer[i] = sample.toInt().coerceIn(-32768, 32767).toShort()
        }
        return shortArrayToByteArray(buffer)
    }

    private fun generateFlarePcm(): ByteArray {
        val durationMs = 260
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val noise = (Random.nextDouble() * 2.0 - 1.0)
            val decay = (1.0 - progress) * (1.0 - progress)
            val sample = noise * decay * 26000.0
            buffer[i] = sample.toInt().coerceIn(-32768, 32767).toShort()
        }
        return shortArrayToByteArray(buffer)
    }

    private fun generateEmpPcm(): ByteArray {
        val durationMs = 380
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val freq = 1400.0 * (1.0 - progress * 0.85)
            val t = i.toDouble() / sampleRate
            val sine = sin(2.0 * Math.PI * freq * t)
            val sample = sine * (1.0 - progress) * 30000.0
            buffer[i] = sample.toInt().coerceIn(-32768, 32767).toShort()
        }
        return shortArrayToByteArray(buffer)
    }

    private fun generatePowerupPcm(): ByteArray {
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
        val totalMs = 280
        val numSamples = (sampleRate * totalMs) / 1000
        val buffer = ShortArray(numSamples)
        val noteLength = numSamples / notes.size

        for (i in 0 until numSamples) {
            val noteIndex = (i / noteLength).coerceAtMost(notes.size - 1)
            val freq = notes[noteIndex]
            val t = i.toDouble() / sampleRate
            val progressInNote = (i % noteLength).toDouble() / noteLength
            val decay = 1.0 - progressInNote * 0.4
            val sine = sin(2.0 * Math.PI * freq * t)
            val sample = sine * decay * 27000.0
            buffer[i] = sample.toInt().coerceIn(-32768, 32767).toShort()
        }
        return shortArrayToByteArray(buffer)
    }

    private fun generateHitPcm(): ByteArray {
        val durationMs = 45
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val noise = (Random.nextDouble() * 2.0 - 1.0)
            val decay = 1.0 - progress
            val sample = noise * decay * 22000.0
            buffer[i] = sample.toInt().coerceIn(-32768, 32767).toShort()
        }
        return shortArrayToByteArray(buffer)
    }

    private fun shortArrayToByteArray(shortArray: ShortArray): ByteArray {
        val byteArray = ByteArray(shortArray.size * 2)
        for (i in shortArray.indices) {
            val s = shortArray[i].toInt()
            byteArray[i * 2] = (s and 0xFF).toByte()
            byteArray[i * 2 + 1] = ((s shr 8) and 0xFF).toByte()
        }
        return byteArray
    }
}
