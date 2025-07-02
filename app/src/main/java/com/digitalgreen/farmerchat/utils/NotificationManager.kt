package com.digitalgreen.farmerchat.utils

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.digitalgreen.farmerchat.R

/**
 * Manages haptic feedback and sound notifications for the app
 */
class NotificationManager(private val context: Context) {
    
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .build()
    
    private var messageCompleteSoundId: Int = 0
    private var followUpReadySoundId: Int = 0
    
    init {
        // Load sounds - we'll use system sounds for now
        // In a real app, you'd add custom sound files to res/raw/
        try {
            // For now, we'll use a simple tone generation approach
            // You can add actual sound files later
        } catch (e: Exception) {
            android.util.Log.e("NotificationManager", "Failed to load sounds", e)
        }
    }
    
    /**
     * Play a subtle haptic feedback when AI response is complete
     */
    fun playMessageCompleteHaptic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }
    
    /**
     * Play a double tap haptic feedback when follow-up questions are ready
     */
    fun playFollowUpReadyHaptic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 30, 50, 30)
            val amplitudes = intArrayOf(0, 128, 0, 128)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            val pattern = longArrayOf(0, 30, 50, 30)
            vibrator.vibrate(pattern, -1)
        }
    }
    
    /**
     * Play a notification sound (ting) when message is complete
     */
    fun playMessageCompleteSound() {
        try {
            // Play a system notification sound
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val volume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
            if (volume > 0) {
                // Using ToneGenerator for a simple "ting" sound
                val toneGen = android.media.ToneGenerator(AudioManager.STREAM_NOTIFICATION, 50)
                toneGen.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 150)
                toneGen.release()
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificationManager", "Failed to play sound", e)
        }
    }
    
    /**
     * Play both haptic and sound feedback for message completion
     */
    fun notifyMessageComplete() {
        playMessageCompleteHaptic()
        playMessageCompleteSound()
    }
    
    /**
     * Play haptic feedback for follow-up questions ready
     */
    fun notifyFollowUpQuestionsReady() {
        playFollowUpReadyHaptic()
    }
    
    fun release() {
        soundPool.release()
    }
}