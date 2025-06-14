package com.digitalgreen.farmerchat.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class SpeechRecognitionManager(private val context: Context) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var recognitionIntent: Intent? = null
    
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening
    
    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private var onResultCallback: ((String) -> Unit)? = null
    
    init {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            setupRecognitionIntent()
            setupRecognitionListener()
        } else {
            _error.value = "Speech recognition is not available on this device"
        }
    }
    
    private fun setupRecognitionIntent() {
        recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
    }
    
    private fun setupRecognitionListener() {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
                _error.value = null
                Log.d("SpeechRecognition", "Ready for speech")
            }
            
            override fun onBeginningOfSpeech() {
                Log.d("SpeechRecognition", "Beginning of speech detected")
            }
            
            override fun onRmsChanged(rmsdB: Float) {
                // Volume level changed
            }
            
            override fun onBufferReceived(buffer: ByteArray?) {
                // Buffer received
            }
            
            override fun onEndOfSpeech() {
                _isListening.value = false
                Log.d("SpeechRecognition", "End of speech")
            }
            
            override fun onError(error: Int) {
                _isListening.value = false
                _error.value = getErrorMessage(error)
                Log.e("SpeechRecognition", "Error: ${getErrorMessage(error)}")
            }
            
            override fun onResults(results: Bundle?) {
                _isListening.value = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    _recognizedText.value = text
                    onResultCallback?.invoke(text)
                    Log.d("SpeechRecognition", "Result: $text")
                }
            }
            
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    _recognizedText.value = matches[0]
                }
            }
            
            override fun onEvent(eventType: Int, params: Bundle?) {
                // Handle events
            }
        })
    }
    
    fun startListening(languageCode: String = "en", onResult: (String) -> Unit) {
        if (speechRecognizer == null) {
            _error.value = "Speech recognition not available"
            return
        }
        
        if (_isListening.value) {
            return
        }
        
        onResultCallback = onResult
        _recognizedText.value = ""
        _error.value = null
        
        // Update language if needed
        val locale = when (languageCode) {
            "hi" -> Locale("hi", "IN")
            "bn" -> Locale("bn", "IN")
            "te" -> Locale("te", "IN")
            "mr" -> Locale("mr", "IN")
            "ta" -> Locale("ta", "IN")
            "gu" -> Locale("gu", "IN")
            "kn" -> Locale("kn", "IN")
            else -> Locale("en", "IN")
        }
        
        recognitionIntent?.putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale)
        recognitionIntent?.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, locale)
        
        try {
            speechRecognizer?.startListening(recognitionIntent)
        } catch (e: Exception) {
            _error.value = "Failed to start speech recognition: ${e.message}"
            _isListening.value = false
        }
    }
    
    fun stopListening() {
        if (_isListening.value) {
            speechRecognizer?.stopListening()
            _isListening.value = false
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
    
    private fun getErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error"
        }
    }
}