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
    
    private val _confidenceScore = MutableStateFlow(0f)
    val confidenceScore: StateFlow<Float> = _confidenceScore
    
    private val _alternativeResults = MutableStateFlow<List<Pair<String, Float>>>(emptyList())
    val alternativeResults: StateFlow<List<Pair<String, Float>>> = _alternativeResults
    
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
                val confidenceScores = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    _recognizedText.value = text
                    
                    // Set confidence score for the top result
                    if (confidenceScores != null && confidenceScores.isNotEmpty()) {
                        _confidenceScore.value = confidenceScores[0]
                        
                        // Store alternative results with confidence scores
                        val alternatives = mutableListOf<Pair<String, Float>>()
                        for (i in matches.indices) {
                            if (i < confidenceScores.size) {
                                alternatives.add(matches[i] to confidenceScores[i])
                            }
                        }
                        _alternativeResults.value = alternatives
                        
                        Log.d("SpeechRecognition", "Result: $text (confidence: ${confidenceScores[0]})")
                        Log.d("SpeechRecognition", "Alternatives: $alternatives")
                    } else {
                        // No confidence scores available, use default
                        _confidenceScore.value = 0.5f
                        _alternativeResults.value = listOf(text to 0.5f)
                    }
                    
                    onResultCallback?.invoke(text)
                }
            }
            
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val confidenceScores = partialResults?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                
                if (!matches.isNullOrEmpty()) {
                    _recognizedText.value = matches[0]
                    
                    // Update confidence for partial results
                    if (confidenceScores != null && confidenceScores.isNotEmpty()) {
                        _confidenceScore.value = confidenceScores[0]
                    }
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
        
        // Get the appropriate locale for the language
        val locale = getLocaleForLanguage(languageCode)
        
        // Create a new intent with updated language settings
        recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toString())
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, locale.toString())
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, locale.toString())
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            // Add extra hints for better recognition
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1500)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500)
        }
        
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
    
    fun clearRecognizedText() {
        _recognizedText.value = ""
        _confidenceScore.value = 0f
        _alternativeResults.value = emptyList()
    }
    
    fun getConfidenceLevel(): ConfidenceLevel {
        return when (_confidenceScore.value) {
            in 0.8f..1.0f -> ConfidenceLevel.HIGH
            in 0.5f..0.8f -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }
    }
    
    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
    
    enum class ConfidenceLevel {
        HIGH,
        MEDIUM,
        LOW
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
    
    private fun getLocaleForLanguage(languageCode: String): Locale {
        return when (languageCode) {
            // Indian languages
            "hi" -> Locale("hi", "IN")
            "bn" -> Locale("bn", "IN")
            "te" -> Locale("te", "IN")
            "mr" -> Locale("mr", "IN")
            "ta" -> Locale("ta", "IN")
            "gu" -> Locale("gu", "IN")
            "kn" -> Locale("kn", "IN")
            "ml" -> Locale("ml", "IN")
            "pa" -> Locale("pa", "IN")
            "or" -> Locale("or", "IN")
            "as" -> Locale("as", "IN")
            "ur" -> Locale("ur", "IN")
            
            // African languages
            "sw" -> Locale("sw", "KE")
            "am" -> Locale("am", "ET")
            "ha" -> Locale("ha", "NG")
            "yo" -> Locale("yo", "NG")
            "ig" -> Locale("ig", "NG")
            "zu" -> Locale("zu", "ZA")
            "xh" -> Locale("xh", "ZA")
            "af" -> Locale("af", "ZA")
            
            // Global languages
            "es" -> Locale("es", "ES")
            "fr" -> Locale("fr", "FR")
            "pt" -> Locale("pt", "BR")
            "ar" -> Locale("ar", "SA")
            "zh" -> Locale("zh", "CN")
            "ru" -> Locale("ru", "RU")
            "de" -> Locale("de", "DE")
            "ja" -> Locale("ja", "JP")
            "ko" -> Locale("ko", "KR")
            "it" -> Locale("it", "IT")
            "nl" -> Locale("nl", "NL")
            "pl" -> Locale("pl", "PL")
            "tr" -> Locale("tr", "TR")
            
            // Southeast Asian
            "id" -> Locale("id", "ID")
            "ms" -> Locale("ms", "MY")
            "th" -> Locale("th", "TH")
            "vi" -> Locale("vi", "VN")
            "fil" -> Locale("fil", "PH")
            
            // Default
            else -> Locale("en", "US")
        }
    }
    
    // Check if a language is supported for speech recognition
    fun isLanguageSupported(languageCode: String): Boolean {
        // These languages generally have good speech recognition support
        val wellSupportedLanguages = setOf(
            "en", "es", "fr", "de", "it", "pt", "nl", "pl", "ru",
            "zh", "ja", "ko", "hi", "ar", "id", "tr", "th", "vi"
        )
        
        // These have partial or developing support
        val partiallySupportedLanguages = setOf(
            "bn", "te", "mr", "ta", "gu", "kn", "ml", "sw", "fil"
        )
        
        return languageCode in wellSupportedLanguages || languageCode in partiallySupportedLanguages
    }
}