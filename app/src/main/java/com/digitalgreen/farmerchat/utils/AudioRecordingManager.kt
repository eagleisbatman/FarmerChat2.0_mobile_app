package com.digitalgreen.farmerchat.utils

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AudioRecordingManager(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentRecordingFile: File? = null
    
    // Recording state
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording
    
    // Playback state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying
    
    // Recording duration in seconds
    private val _recordingDuration = MutableStateFlow(0)
    val recordingDuration: StateFlow<Int> = _recordingDuration
    
    // Playback progress (0.0 to 1.0)
    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress
    
    // Audio levels for visualization (0.0 to 1.0)
    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    // Recording state
    private val _recordingState = MutableStateFlow(RecordingState.IDLE)
    val recordingState: StateFlow<RecordingState> = _recordingState
    
    private var durationTimer: Timer? = null
    private var audioLevelTimer: Timer? = null
    
    enum class RecordingState {
        IDLE,           // No recording or playback
        RECORDING,      // Currently recording
        RECORDED,       // Recording complete, ready for playback
        PLAYING,        // Playing back recording
        PAUSED          // Playback paused
    }
    
    fun startRecording(): Boolean {
        if (_isRecording.value) {
            Log.w(TAG, "Already recording")
            return false
        }
        
        try {
            // Create output file
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val recordingsDir = File(context.filesDir, "recordings").apply { mkdirs() }
            currentRecordingFile = File(recordingsDir, "recording_$timestamp.m4a")
            
            // Initialize MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(currentRecordingFile?.absolutePath)
                
                prepare()
                start()
            }
            
            _isRecording.value = true
            _recordingState.value = RecordingState.RECORDING
            _recordingDuration.value = 0
            _error.value = null
            
            // Start duration timer
            startDurationTimer()
            
            // Start audio level monitoring
            startAudioLevelMonitoring()
            
            Log.d(TAG, "Recording started: ${currentRecordingFile?.name}")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            _error.value = "Failed to start recording: ${e.message}"
            _isRecording.value = false
            return false
        }
    }
    
    fun stopRecording(): File? {
        if (!_isRecording.value) {
            Log.w(TAG, "Not recording")
            return null
        }
        
        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
            mediaRecorder = null
            
            _isRecording.value = false
            _recordingState.value = RecordingState.RECORDED
            
            // Stop timers
            durationTimer?.cancel()
            durationTimer = null
            audioLevelTimer?.cancel()
            audioLevelTimer = null
            _audioLevel.value = 0f
            
            Log.d(TAG, "Recording stopped: ${currentRecordingFile?.name}")
            return currentRecordingFile
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
            _error.value = "Failed to stop recording: ${e.message}"
            _isRecording.value = false
            return null
        }
    }
    
    fun startPlayback() {
        if (currentRecordingFile == null || !currentRecordingFile!!.exists()) {
            _error.value = "No recording available"
            return
        }
        
        if (_isPlaying.value) {
            pausePlayback()
            return
        }
        
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(currentRecordingFile!!.absolutePath)
                prepare()
                
                setOnCompletionListener {
                    stopPlayback()
                }
                
                start()
            }
            
            _isPlaying.value = true
            _recordingState.value = RecordingState.PLAYING
            
            // Start playback progress monitoring
            startPlaybackProgressMonitoring()
            
            Log.d(TAG, "Playback started")
            
        } catch (e: IOException) {
            Log.e(TAG, "Failed to start playback", e)
            _error.value = "Failed to play recording: ${e.message}"
        }
    }
    
    fun pausePlayback() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
                _recordingState.value = RecordingState.PAUSED
                Log.d(TAG, "Playback paused")
            }
        }
    }
    
    fun resumePlayback() {
        mediaPlayer?.let {
            it.start()
            _isPlaying.value = true
            _recordingState.value = RecordingState.PLAYING
            startPlaybackProgressMonitoring()
            Log.d(TAG, "Playback resumed")
        }
    }
    
    fun stopPlayback() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            reset()
            release()
        }
        mediaPlayer = null
        
        _isPlaying.value = false
        _playbackProgress.value = 0f
        _recordingState.value = RecordingState.RECORDED
        
        Log.d(TAG, "Playback stopped")
    }
    
    fun discardRecording() {
        // Stop any ongoing recording or playback
        if (_isRecording.value) {
            stopRecording()
        }
        if (_isPlaying.value) {
            stopPlayback()
        }
        
        // Delete the recording file
        currentRecordingFile?.let {
            if (it.exists()) {
                it.delete()
                Log.d(TAG, "Recording discarded: ${it.name}")
            }
        }
        
        // Reset state
        currentRecordingFile = null
        _recordingState.value = RecordingState.IDLE
        _recordingDuration.value = 0
        _playbackProgress.value = 0f
        _error.value = null
    }
    
    fun getCurrentRecordingFile(): File? = currentRecordingFile
    
    fun getRecordingDurationSeconds(): Int = _recordingDuration.value
    
    private fun startDurationTimer() {
        durationTimer = Timer()
        durationTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                _recordingDuration.value += 1
            }
        }, 1000, 1000)
    }
    
    private fun startAudioLevelMonitoring() {
        audioLevelTimer = Timer()
        audioLevelTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                mediaRecorder?.let {
                    val maxAmplitude = it.maxAmplitude
                    // Normalize to 0.0 - 1.0 range
                    val normalizedLevel = if (maxAmplitude > 0) {
                        (maxAmplitude.toFloat() / 32767f).coerceIn(0f, 1f)
                    } else {
                        0f
                    }
                    _audioLevel.value = normalizedLevel
                }
            }
        }, 0, 100) // Update every 100ms
    }
    
    private fun startPlaybackProgressMonitoring() {
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        val progress = player.currentPosition.toFloat() / player.duration.toFloat()
                        _playbackProgress.value = progress.coerceIn(0f, 1f)
                    }
                }
            }
        }, 0, 100) // Update every 100ms
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun release() {
        if (_isRecording.value) {
            stopRecording()
        }
        if (_isPlaying.value) {
            stopPlayback()
        }
        
        durationTimer?.cancel()
        audioLevelTimer?.cancel()
        
        mediaRecorder?.release()
        mediaPlayer?.release()
        
        mediaRecorder = null
        mediaPlayer = null
        durationTimer = null
        audioLevelTimer = null
    }
    
    companion object {
        private const val TAG = "AudioRecordingManager"
    }
}