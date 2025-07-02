import OpenAI from 'openai';
import { BaseAIProvider, AIMessage, AIResponse, StreamChunk, AIProviderConfig } from './base.provider';
import { logger } from '../../utils/logger';

export class OpenAIProvider extends BaseAIProvider {
  private openai: OpenAI;
  
  constructor(config: AIProviderConfig) {
    super(config);
    this.openai = new OpenAI({
      apiKey: this.apiKey,
    });
  }
  
  getProviderName(): string {
    return 'openai';
  }
  
  getDefaultModel(): string {
    return 'gpt-4-turbo-preview';
  }
  
  getAvailableModels(): string[] {
    return [
      'gpt-4-turbo-preview',
      'gpt-4-turbo',
      'gpt-4',
      'gpt-4-32k',
      'gpt-3.5-turbo',
      'gpt-3.5-turbo-16k',
    ];
  }
  
  async transcribeAudio(audioBuffer: Buffer, language?: string): Promise<string> {
    logger.info('=== OPENAI PROVIDER TRANSCRIBE START ===');
    logger.info('Audio buffer size:', audioBuffer.length);
    logger.info('Language parameter:', language);
    logger.info('API Key present:', !!this.apiKey);
    logger.info('API Key prefix:', this.apiKey?.substring(0, 10) + '...');
    
    try {
      // Create a File object from the buffer
      const file = new File([audioBuffer], 'audio.m4a', { type: 'audio/m4a' });
      logger.info('Created File object:', { name: file.name, size: file.size, type: file.type });
      
      // If language is specified, use it to guide transcription
      // This helps Whisper transcribe in the expected language
      const transcriptionOptions: any = {
        file: file,
        model: 'whisper-1',
        response_format: 'text', // Simple text format for now
      };
      
      // Add language parameter if specified
      if (language) {
        // OpenAI Whisper expects ISO-639-1 language codes (2-letter codes)
        // We already receive them in that format, so just pass them through
        transcriptionOptions.language = language;
        logger.info('Using ISO-639-1 language code:', language);
      }
      
      logger.info('Transcription options:', transcriptionOptions);
      logger.info('Calling OpenAI Whisper API...');
      
      const response = await this.openai.audio.transcriptions.create(transcriptionOptions);
      
      logger.info('OpenAI API response type:', typeof response);
      logger.info('OpenAI API response:', response);
      
      // For verbose_json format, response is an object with text, language, etc.
      // For text format, response is just a string
      let transcriptionText: string;
      let detectedLanguage: string | undefined;
      
      if (typeof response === 'string') {
        transcriptionText = response;
      } else {
        transcriptionText = response.text || '';
        detectedLanguage = (response as any).language; // Language info if available
      }
      
      // Check if the detected language matches expected language (if available)
      if (language && detectedLanguage) {
        const detectedLang = detectedLanguage.toLowerCase();
        const expectedLang = language.toLowerCase();
        
        // Map detected language names to ISO codes for comparison
        const languageNameToCode: { [key: string]: string } = {
          'english': 'en',
          'hindi': 'hi',
          'spanish': 'es',
          'french': 'fr',
          'portuguese': 'pt',
          'arabic': 'ar',
          'chinese': 'zh',
          'japanese': 'ja',
          'korean': 'ko',
          'german': 'de',
          'italian': 'it',
          'russian': 'ru',
          'bengali': 'bn',
          'punjabi': 'pa',
          'tamil': 'ta',
          'telugu': 'te',
          'marathi': 'mr',
          'gujarati': 'gu',
          'urdu': 'ur',
        };
        
        // Convert detected language name to ISO code if needed
        const detectedCode = languageNameToCode[detectedLang] || detectedLang;
        
        if (detectedCode !== expectedLang) {
          logger.warn(`Language mismatch: expected ${expectedLang}, detected ${detectedCode} (${detectedLang})`);
          
          // Get language display name for error message
          const languageDisplayNames: { [key: string]: string } = {
            'hi': 'Hindi',
            'en': 'English',
            'sw': 'Swahili',
            'es': 'Spanish',
            'fr': 'French',
            'pt': 'Portuguese',
            'ar': 'Arabic',
          };
          
          const expectedName = languageDisplayNames[expectedLang] || expectedLang;
          throw new Error(`Please speak in ${expectedName}`);
        }
      }
      
      // Check for low confidence or empty transcription
      if (!transcriptionText || transcriptionText.trim().length === 0) {
        throw new Error('No speech detected. Please speak clearly into the microphone.');
      }
      
      // Filter out known problematic transcriptions (Osho copyright message)
      const lowerText = transcriptionText.toLowerCase();
      if (lowerText.includes('osho.com') || 
          lowerText.includes('osho international') || 
          lowerText.includes('registered trademark')) {
        logger.warn('Detected Osho copyright message in transcription, rejecting');
        throw new Error('Could not understand the audio. Please try speaking again.');
      }
      
      // Check for very short transcriptions that might indicate noise
      if (transcriptionText.trim().split(' ').length < 2) {
        throw new Error('Message too short. Please speak a complete sentence.');
      }
      
      logger.info('Transcription successful', { 
        language: detectedLanguage || language,
        textLength: transcriptionText.length
      });
      
      return transcriptionText.trim();
    } catch (error: any) {
      logger.error('OpenAI audio transcription error:', error);
      
      // Pass through custom error messages
      if (error.message?.includes('LANGUAGE_MISMATCH:') || 
          error.message?.includes('EMPTY_TRANSCRIPTION:') ||
          error.message?.includes('TOO_SHORT:')) {
        throw error;
      }
      
      // Handle specific OpenAI errors
      if (error.response?.status === 413) {
        throw new Error('Recording is too long. Please record a shorter message.');
      }
      
      if (error.response?.status === 400) {
        throw new Error('Could not process the audio. Please try recording again.');
      }
      
      throw new Error('Unable to transcribe. Please check your internet connection and try again.');
    }
  }
  
  async generateResponse(messages: AIMessage[]): Promise<AIResponse> {
    try {
      const completion = await this.openai.chat.completions.create({
        model: this.model,
        messages: messages as any,
        temperature: this.temperature,
        max_tokens: this.maxTokens,
        top_p: this.topP,
      });
      
      const choice = completion.choices[0];
      
      return {
        content: choice.message.content || '',
        usage: completion.usage ? {
          promptTokens: completion.usage.prompt_tokens,
          completionTokens: completion.usage.completion_tokens,
          totalTokens: completion.usage.total_tokens,
        } : undefined,
        model: completion.model,
        finishReason: choice.finish_reason,
      };
    } catch (error) {
      logger.error('OpenAI generation error:', error);
      throw error;
    }
  }
  
  async generateStreamResponse(
    messages: AIMessage[],
    onChunk: (chunk: StreamChunk) => void
  ): Promise<AIResponse> {
    try {
      const stream = await this.openai.chat.completions.create({
        model: this.model,
        messages: messages as any,
        temperature: this.temperature,
        max_tokens: this.maxTokens,
        top_p: this.topP,
        stream: true,
      });
      
      let fullContent = '';
      let model = '';
      
      for await (const chunk of stream) {
        const content = chunk.choices[0]?.delta?.content || '';
        fullContent += content;
        model = chunk.model || model;
        
        if (content) {
          onChunk({
            content,
            isComplete: false,
          });
        }
      }
      
      onChunk({
        content: '',
        isComplete: true,
      });
      
      // Note: OpenAI doesn't provide usage data in streaming mode
      return {
        content: fullContent,
        model,
      };
    } catch (error) {
      logger.error('OpenAI streaming error:', error);
      throw error;
    }
  }
}