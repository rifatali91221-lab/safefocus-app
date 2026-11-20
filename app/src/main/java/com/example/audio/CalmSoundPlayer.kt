package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * ১৫ সেকেন্ডের বিরতির সময় সম্পূর্ণ অফলাইনে মন শান্ত করার জন্য
 * মৃদু হারমোনিক অ্যাম্বিয়েন্ট সাউন্ড জেনারেট করে (কোনো ইন্টারনেট বা এক্সটার্নাল ফাইলের প্রয়োজন নেই)।
 */
class CalmSoundPlayer {
    private var playJob: Job? = null
    private var audioTrack: AudioTrack? = null

    fun start(scope: CoroutineScope) {
        stop()
        playJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 22050
            val bufferSamples = sampleRate / 10 // 100ms chunks
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(bufferSamples * 2)

            val track = runCatching {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            }.getOrNull() ?: return@launch

            audioTrack = track
            runCatching { track.play() }

            val pcmBuffer = ShortArray(bufferSamples)
            var sampleIndex = 0L

            while (isActive) {
                for (i in pcmBuffer.indices) {
                    val t = sampleIndex.toDouble() / sampleRate.toDouble()
                    // 432Hz + 216Hz + 324Hz মৃদু শ্বাস-প্রশ্বাস লয়ের সুর
                    val breathEnvelope = 0.45 + 0.55 * sin(2.0 * PI * 0.12 * t)
                    val fadeIn = (t / 1.8).coerceIn(0.0, 1.0)
                    val wave = (
                        0.50 * sin(2.0 * PI * 216.0 * t) +
                        0.32 * sin(2.0 * PI * 324.0 * t) +
                        0.18 * sin(2.0 * PI * 432.0 * t)
                    ) * breathEnvelope * fadeIn * 0.16

                    pcmBuffer[i] = (wave * Short.MAX_VALUE).toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        .toShort()
                    sampleIndex++
                }
                val written = runCatching {
                    track.write(pcmBuffer, 0, pcmBuffer.size)
                }.getOrDefault(-1)
                if (written < 0) break
            }
        }
    }

    fun stop() {
        playJob?.cancel()
        playJob = null
        audioTrack?.let { track ->
            runCatching {
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.pause()
                    track.flush()
                }
                track.stop()
            }
            runCatching { track.release() }
        }
        audioTrack = null
    }
}
