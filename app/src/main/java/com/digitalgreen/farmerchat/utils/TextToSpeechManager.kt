package com.digitalgreen.farmerchat.utils

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

class TextToSpeechManager(context: Context) {
    private var textToSpeech: TextToSpeech? = null
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking
    
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private var currentLanguageCode: String = "en"
    private val appContext = context.applicationContext
    
    init {
        textToSpeech = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                _isInitialized.value = true
                // Configure TTS for better quality
                configureTTSQuality()
                // Set default language
                setLanguage(Locale.getDefault())
            } else {
                _error.value = "Failed to initialize text-to-speech"
                Log.e("TTS", "Failed to initialize TTS with status: $status")
            }
        }
        
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
                _error.value = null
            }
            
            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }
            
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                _error.value = "Error during speech synthesis"
            }
        })
    }
    
    private fun configureTTSQuality() {
        textToSpeech?.let { tts ->
            // Set speech rate (0.5 to 2.0, default 1.0)
            tts.setSpeechRate(0.9f) // Slightly slower for better clarity
            
            // Set pitch (0.5 to 2.0, default 1.0)
            tts.setPitch(1.0f) // Natural pitch
            
            // Try to use the best available voice
            selectBestVoice()
        }
    }
    
    private fun selectBestVoice() {
        val tts = textToSpeech ?: return
        val locale = getLocaleForLanguage(currentLanguageCode)
        
        // Get available voices
        val voices = tts.voices
        if (voices.isNullOrEmpty()) {
            Log.w("TTS", "No voices available")
            return
        }
        
        // Filter voices by locale
        val localeVoices = voices.filter { voice ->
            voice.locale.language == locale.language &&
            (voice.locale.country.isEmpty() || voice.locale.country == locale.country)
        }
        
        // Sort by quality (prefer network voices, then local enhanced)
        val sortedVoices = localeVoices.sortedWith(compareBy(
            { !it.isNetworkConnectionRequired }, // Network voices first
            { !it.name.contains("enhanced", ignoreCase = true) }, // Enhanced voices next
            { it.quality != Voice.QUALITY_VERY_HIGH }, // Highest quality first
            { it.quality != Voice.QUALITY_HIGH },
            { it.quality != Voice.QUALITY_NORMAL }
        ))
        
        // Select the best voice
        val selectedVoice = sortedVoices.firstOrNull()
        if (selectedVoice != null) {
            tts.voice = selectedVoice
            Log.d("TTS", "Selected voice: ${selectedVoice.name}, Quality: ${selectedVoice.quality}, Network: ${selectedVoice.isNetworkConnectionRequired}")
        } else {
            // Fallback to setting just the locale
            val result = tts.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("TTS", "Language not fully supported: $locale")
                _error.value = "Language not fully supported for speech"
            }
        }
    }
    
    fun setLanguage(locale: Locale) {
        currentLanguageCode = locale.language
        selectBestVoice()
    }
    
    fun setLanguageByCode(languageCode: String) {
        currentLanguageCode = languageCode
        selectBestVoice()
    }
    
    fun speak(text: String, languageCode: String? = null) {
        if (!_isInitialized.value) {
            _error.value = "TTS not initialized"
            return
        }
        
        // Update language if different
        if (languageCode != null && languageCode != currentLanguageCode) {
            setLanguageByCode(languageCode)
        }
        
        // Clean the text for better TTS output
        val cleanedText = cleanTextForTTS(text)
        
        // Speak with optimal parameters
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "msg_${System.currentTimeMillis()}")
            // Add emphasis for better clarity
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }
        
        textToSpeech?.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, params, "msg_${System.currentTimeMillis()}")
    }
    
    private fun cleanTextForTTS(text: String): String {
        return text
            // Remove multiple spaces
            .replace(Regex("\\s+"), " ")
            // Remove markdown formatting
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1") // Bold
            .replace(Regex("\\*(.*?)\\*"), "$1") // Italic
            .replace(Regex("__(.*?)__"), "$1") // Underline
            // Add pauses for better readability
            .replace("•", ",") // Bullet points
            .replace("...", ", ") // Ellipsis
            .trim()
    }
    
    fun stop() {
        textToSpeech?.stop()
        _isSpeaking.value = false
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
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
    
    // Check if a language is well-supported for TTS
    fun isLanguageSupported(languageCode: String): Boolean {
        if (!_isInitialized.value) return false
        
        val locale = getLocaleForLanguage(languageCode)
        val result = textToSpeech?.isLanguageAvailable(locale) ?: TextToSpeech.LANG_NOT_SUPPORTED
        
        return result >= TextToSpeech.LANG_AVAILABLE
    }
}