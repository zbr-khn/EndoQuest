package com.example.endoquest.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.*
import kotlin.math.PI
import kotlin.math.sin

/**
 * Zero-dependency real-time audio synthesizer for EndoQuest: The RCT Run.
 * Generates low-latency PCM audio waveforms using Android AudioTrack without requiring external asset files.
 */
class SoundManager(context: Context) {
    var isMuted: Boolean = false
    private val sampleRate = 22050
    private val audioScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var bgmJob: Job? = null
    var isBgmPlaying: Boolean = false
        private set

    /**
     * Plays a synthesized PCM waveform through an ephemeral AudioTrack.
     */
    private fun playPcm(samples: ShortArray) {
        if (isMuted) return
        audioScope.launch {
            try {
                val bufferSize = samples.size * 2
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(samples, 0, samples.size)
                audioTrack.play()

                val durationMs = (samples.size.toDouble() / sampleRate * 1000).toLong() + 50
                delay(durationMs)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                Log.e("SoundManager", "Error playing synthesized sound", e)
            }
        }
    }

    fun playCoin() = playCoinCollection()

    /**
     * Golden $25 coin pickup sound: Bright dual chime (B5 987 Hz -> E6 1318 Hz).
     */
    fun playCoinCollection() {
        if (isMuted) return
        audioScope.launch {
            val totalSamples = (sampleRate * 0.22).toInt()
            val samples = ShortArray(totalSamples)
            val split = (sampleRate * 0.08).toInt()

            for (i in 0 until split) {
                val t = i.toDouble() / sampleRate
                val decay = 1.0 - (i.toDouble() / split) * 0.4
                val wave = sin(2.0 * PI * 987.77 * t) + 0.3 * sin(2.0 * PI * 1975.53 * t)
                samples[i] = (wave * decay * 14000).toInt().coerceIn(-32767, 32767).toShort()
            }
            for (i in split until totalSamples) {
                val t = (i - split).toDouble() / sampleRate
                val decay = 1.0 - ((i - split).toDouble() / (totalSamples - split))
                val wave = sin(2.0 * PI * 1318.51 * t) + 0.3 * sin(2.0 * PI * 2637.0 * t)
                samples[i] = (wave * decay * decay * 16000).toInt().coerceIn(-32767, 32767).toShort()
            }
            playPcm(samples)
        }
    }

    /**
     * Subtle acoustic footstep tap during running animation.
     */
    fun playFootstep() {
        if (isMuted) return
        val totalSamples = (sampleRate * 0.035).toInt()
        val samples = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val decay = 1.0 - (i.toDouble() / totalSamples)
            val freq = 130.0 - 50.0 * (i.toDouble() / totalSamples)
            val wave = sin(2.0 * PI * freq * t)
            samples[i] = (wave * decay * 6000).toInt().toShort()
        }
        playPcm(samples)
    }

    /**
     * Jump whoosh: Fast rising frequency sweep (280 Hz -> 680 Hz).
     */
    fun playJump() {
        if (isMuted) return
        val totalSamples = (sampleRate * 0.16).toInt()
        val samples = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val progress = i.toDouble() / totalSamples
            val freq = 280.0 + 400.0 * progress
            phase += 2.0 * PI * freq / sampleRate
            val envelope = sin(PI * progress) // smooth arch envelope
            val wave = sin(phase)
            samples[i] = (wave * envelope * 12000).toInt().toShort()
        }
        playPcm(samples)
    }

    /**
     * Slide skid: Downward friction swoosh (420 Hz -> 120 Hz).
     */
    fun playSlide() {
        if (isMuted) return
        val totalSamples = (sampleRate * 0.20).toInt()
        val samples = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val progress = i.toDouble() / totalSamples
            val freq = 420.0 - 300.0 * progress
            phase += 2.0 * PI * freq / sampleRate
            val decay = 1.0 - progress
            val noise = (Math.random() * 2.0 - 1.0) * 0.25
            val wave = sin(phase) + noise
            samples[i] = (wave * decay * 11000).toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    /**
     * Heavy obstacle collision punch thud (130 Hz -> 40 Hz decay).
     */
    fun playCollision() {
        if (isMuted) return
        val totalSamples = (sampleRate * 0.28).toInt()
        val samples = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val progress = i.toDouble() / totalSamples
            val freq = 130.0 - 90.0 * progress
            phase += 2.0 * PI * freq / sampleRate
            val decay = (1.0 - progress) * (1.0 - progress)
            val noise = (Math.random() * 2.0 - 1.0) * 0.4
            val wave = sin(phase) + noise
            samples[i] = (wave * decay * 18000).toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    /**
     * Dental Police two-tone European emergency wail siren (750 Hz <-> 940 Hz).
     */
    fun playPoliceSiren() {
        if (isMuted) return
        audioScope.launch {
            val totalSamples = (sampleRate * 0.90).toInt()
            val samples = ShortArray(totalSamples)
            var phase = 0.0
            for (i in 0 until totalSamples) {
                val cyclePos = (i.toDouble() / (sampleRate * 0.22)) % 2.0
                val freq = if (cyclePos < 1.0) 750.0 else 940.0
                phase += 2.0 * PI * freq / sampleRate
                val wave = sin(phase)
                samples[i] = (wave * 12000).toInt().toShort()
            }
            playPcm(samples)
        }
    }

    /**
     * Inspection / Quiz Success fanfare: Joyful C Major triad arpeggio (C5, E5, G5, C6).
     */
    fun playQuizSuccess() {
        if (isMuted) return
        audioScope.launch {
            val freqs = listOf(523.25, 659.25, 783.99, 1046.50)
            val noteDuration = (sampleRate * 0.07).toInt()
            val totalSamples = noteDuration * freqs.size + (sampleRate * 0.15).toInt()
            val samples = ShortArray(totalSamples)

            freqs.forEachIndexed { noteIndex, freq ->
                val start = noteIndex * noteDuration
                for (i in 0 until (noteDuration + (if (noteIndex == freqs.size - 1) (sampleRate * 0.15).toInt() else 0))) {
                    val idx = start + i
                    if (idx < totalSamples) {
                        val t = i.toDouble() / sampleRate
                        val decay = 1.0 - (i.toDouble() / (noteDuration * 1.5))
                        val wave = sin(2.0 * PI * freq * t)
                        samples[idx] = (wave * decay.coerceAtLeast(0.0) * 12000).toInt().toShort()
                    }
                }
            }
            playPcm(samples)
        }
    }

    /**
     * Quiz Incorrect answer: Low buzzer tone.
     */
    fun playQuizError() {
        if (isMuted) return
        val totalSamples = (sampleRate * 0.20).toInt()
        val samples = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val freq = 160.0
            // Harsh buzz harmonic
            val wave = sin(2.0 * PI * freq * t) + 0.5 * sin(2.0 * PI * freq * 2.0 * t) + 0.3 * sin(2.0 * PI * freq * 3.0 * t)
            val decay = 1.0 - (i.toDouble() / totalSamples)
            samples[i] = (wave * decay * 11000).toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    /**
     * Legacy helper method for quiz inspection trigger.
     */
    fun playQuizInspection() {
        playPoliceSiren()
    }

    /**
     * Ambient rhythmic BGM synthesizer loop.
     */
    fun startBgm() {
        if (isBgmPlaying) return
        isBgmPlaying = true
        bgmJob = audioScope.launch {
            // Melodic progression: C4, G4, A4, F4 chords
            val chords = listOf(
                listOf(261.63, 329.63, 392.00), // C Maj
                listOf(196.00, 246.94, 293.66), // G Maj
                listOf(220.00, 261.63, 329.63), // A Min
                listOf(174.61, 220.00, 261.63)  // F Maj
            )
            while (isActive && isBgmPlaying) {
                for (chord in chords) {
                    if (!isActive || !isBgmPlaying) break
                    if (!isMuted) {
                        val duration = (sampleRate * 0.40).toInt()
                        val samples = ShortArray(duration)
                        for (i in 0 until duration) {
                            val t = i.toDouble() / sampleRate
                            val env = sin(PI * (i.toDouble() / duration))
                            var sum = 0.0
                            for (f in chord) {
                                sum += sin(2.0 * PI * f * t)
                            }
                            samples[i] = (sum / chord.size * env * 3500).toInt().toShort()
                        }
                        playPcm(samples)
                    }
                    delay(420)
                }
            }
        }
    }

    fun stopBgm() {
        isBgmPlaying = false
        bgmJob?.cancel()
        bgmJob = null
    }

    fun toggleMute(): Boolean {
        isMuted = !isMuted
        return isMuted
    }

    fun release() {
        stopBgm()
        audioScope.cancel()
    }
}
