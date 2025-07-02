import { config } from '../config';
import { logger } from '../utils/logger';
import { BaseAIProvider, AIMessage, AIResponse, StreamChunk } from './ai/base.provider';
import { GeminiProvider } from './ai/gemini.provider';
import { OpenAIProvider } from './ai/openai.provider';
import { AnthropicProvider } from './ai/anthropic.provider';
import { query } from '../database';

export interface ChatRequest {
  message: string;
  conversationId: string;
  userId: string;
  userProfile?: any;
  stream?: boolean;
}

export interface FollowUpQuestion {
  question: string;
  id: string;
}

export class AIService {
  private providers: Map<string, BaseAIProvider> = new Map();
  private defaultProvider: string;
  
  constructor() {
    this.defaultProvider = config.ai.defaultProvider;
    this.initializeProviders();
  }
  
  private initializeProviders(): void {
    // Initialize Gemini if API key is available
    if (config.ai.providers.gemini.apiKey) {
      this.providers.set('gemini', new GeminiProvider({
        apiKey: config.ai.providers.gemini.apiKey,
        model: config.ai.providers.gemini.model,
      }));
    }
    
    // Initialize OpenAI if API key is available
    if (config.ai.providers.openai.apiKey) {
      this.providers.set('openai', new OpenAIProvider({
        apiKey: config.ai.providers.openai.apiKey,
        model: config.ai.providers.openai.model,
      }));
    }
    
    // Initialize Anthropic if API key is available
    if (config.ai.providers.anthropic.apiKey) {
      this.providers.set('anthropic', new AnthropicProvider({
        apiKey: config.ai.providers.anthropic.apiKey,
        model: config.ai.providers.anthropic.model,
      }));
    }
    
    logger.info(`AI Service initialized with providers: ${Array.from(this.providers.keys()).join(', ')}`);
  }
  
  getProvider(providerName?: string): BaseAIProvider {
    const name = providerName || this.defaultProvider;
    const provider = this.providers.get(name);
    
    if (!provider) {
      throw new Error(`AI provider '${name}' not available. Check API key configuration.`);
    }
    
    return provider;
  }
  
  async generateResponse(request: ChatRequest): Promise<AIResponse> {
    const provider = this.getProvider();
    
    // Get conversation history
    const messages = await this.getConversationHistory(request.conversationId);
    
    // Add system message based on user profile using PromptService
    if (request.userProfile) {
      const { PromptService } = await import('./prompt.service');
      const promptService = new PromptService();
      const languageCode = request.userProfile.language || 'en';
      const systemMessage = await promptService.getSystemPrompt(request.userProfile, languageCode);
      messages.unshift({ role: 'system', content: systemMessage });
    }
    
    // Add current message
    messages.push({ role: 'user', content: request.message });
    
    // Generate response
    const response = await provider.generateResponse(messages);
    
    // Log usage
    await provider.logUsage(request.userId, response.model || provider.getDefaultModel(), response.usage);
    
    // Extract follow-up questions before saving
    const followUpQuestions = await this.extractFollowUpQuestions(
      response.content,
      request.userProfile?.language || 'en',
      request.userProfile
    );
    
    // Save messages to database with follow-up questions as string array
    await this.saveMessages(
      request.conversationId, 
      request.message, 
      response.content,
      followUpQuestions.map(fq => fq.question)
    );
    
    return response;
  }
  
  async generateStreamResponse(
    request: ChatRequest,
    onChunk: (chunk: StreamChunk) => void
  ): Promise<AIResponse> {
    const provider = this.getProvider();
    
    // Get conversation history
    const messages = await this.getConversationHistory(request.conversationId);
    
    // Add system message based on user profile using PromptService
    if (request.userProfile) {
      const { PromptService } = await import('./prompt.service');
      const promptService = new PromptService();
      const languageCode = request.userProfile.language || 'en';
      const systemMessage = await promptService.getSystemPrompt(request.userProfile, languageCode);
      messages.unshift({ role: 'system', content: systemMessage });
    }
    
    // Add current message
    messages.push({ role: 'user', content: request.message });
    
    // Generate streaming response
    const response = await provider.generateStreamResponse(messages, onChunk);
    
    // Log usage
    await provider.logUsage(request.userId, response.model || provider.getDefaultModel(), response.usage);
    
    // Extract follow-up questions before saving
    const followUpQuestions = await this.extractFollowUpQuestions(
      response.content,
      request.userProfile?.language || 'en',
      request.userProfile
    );
    
    // Save messages to database with follow-up questions as string array
    await this.saveMessages(
      request.conversationId, 
      request.message, 
      response.content,
      followUpQuestions.map(fq => fq.question)
    );
    
    return response;
  }
  
  async extractFollowUpQuestions(
    response: string,
    userLanguage: string,
    userProfile?: any
  ): Promise<FollowUpQuestion[]> {
    const provider = this.getProvider();
    
    // Use PromptService for consistent prompt generation
    const { PromptService } = await import('./prompt.service');
    const promptService = new PromptService();
    const prompt = await promptService.getFollowUpPrompt(response, userProfile, userLanguage);
    
    const messages: AIMessage[] = [
      { role: 'user', content: prompt }
    ];
    
    try {
      const result = await provider.generateResponse(messages);
      const questions = result.content
        .split('\n')
        .filter(q => q.trim())
        .slice(0, 3)
        .map((question, index) => ({
          question: question.trim(),
          id: `follow-up-${index + 1}`,
        }));
      
      return questions;
    } catch (error) {
      logger.error('Failed to extract follow-up questions:', error);
      return [];
    }
  }
  
  async generateConversationTitle(
    firstMessage: string,
    firstResponse: string,
    userLanguage: string
  ): Promise<string> {
    const provider = this.getProvider();
    const languageName = this.getLanguageName(userLanguage);
    
    const prompt = `Generate a short, descriptive title for this conversation in ${languageName} language.

User: ${firstMessage}
Assistant: ${firstResponse.substring(0, 200)}...

Requirements:
1. Title must be in ${languageName} language
2. Maximum 50 characters
3. Should summarize the main topic
4. Be specific and descriptive

Output: Return only the title, nothing else.`;
    
    const messages: AIMessage[] = [
      { role: 'user', content: prompt }
    ];
    
    try {
      const result = await provider.generateResponse(messages);
      return result.content.trim().substring(0, 50);
    } catch (error) {
      logger.error('Failed to generate conversation title:', error);
      // Fallback to first few words of the message
      return firstMessage.substring(0, 30) + '...';
    }
  }
  
  private async getConversationHistory(conversationId: string): Promise<AIMessage[]> {
    const result = await query<{ content: string; is_user: boolean }>(
      `SELECT content, is_user FROM messages 
       WHERE conversation_id = $1 
       ORDER BY created_at ASC 
       LIMIT 20`,
      [conversationId]
    );
    
    return result.rows.map(row => ({
      role: row.is_user ? 'user' : 'assistant',
      content: row.content,
    }));
  }
  
  private async saveMessages(
    conversationId: string,
    userMessage: string,
    assistantMessage: string,
    followUpQuestions?: string[]
  ): Promise<void> {
    // Insert user message
    await query(
      `INSERT INTO messages (conversation_id, content, is_user, created_at)
       VALUES ($1, $2, true, NOW())`,
      [conversationId, userMessage]
    );
    
    // Insert assistant message with follow-up questions
    await query(
      `INSERT INTO messages (conversation_id, content, is_user, follow_up_questions, created_at)
       VALUES ($1, $2, false, $3, NOW())`,
      [conversationId, assistantMessage, followUpQuestions || []]
    );
    
    // Update conversation last message
    await query(
      `UPDATE conversations 
       SET last_message = $1, 
           last_message_time = NOW(),
           last_message_is_user = false,
           updated_at = NOW()
       WHERE id = $2`,
      [assistantMessage, conversationId]
    );
  }
  
  private buildSystemMessage(userProfile: any): string {
    const crops = userProfile.crops?.join(', ') || 'various crops';
    const livestock = userProfile.livestock?.join(', ') || 'various livestock';
    const location = userProfile.location || 'unspecified location';
    const language = userProfile.language || 'en';
    const languageName = this.getLanguageName(language);
    
    return `You are an expert agricultural advisor helping farmers make better decisions. 
The farmer you're speaking with is located in ${location}, grows ${crops}, and raises ${livestock}.
Please provide practical, actionable advice tailored to their specific context.
IMPORTANT: Always respond in ${languageName} language (code: ${language}).
Keep responses concise but comprehensive, using bullet points where appropriate.
Focus on sustainable and locally appropriate farming practices.`;
  }
  
  private getLanguageName(code: string): string {
    const languages: Record<string, string> = {
      en: 'English',
      hi: 'Hindi',
      sw: 'Swahili',
      es: 'Spanish',
      fr: 'French',
      pt: 'Portuguese',
      ar: 'Arabic',
      bn: 'Bengali',
      zh: 'Chinese',
      // Add more as needed
    };
    
    return languages[code] || 'English';
  }
  
  getAvailableProviders(): string[] {
    return Array.from(this.providers.keys());
  }
  
  getProviderModels(providerName: string): string[] {
    const provider = this.providers.get(providerName);
    return provider ? provider.getAvailableModels() : [];
  }

  async extractConversationTags(
    conversationHistory: string,
    userLanguage: string,
    userProfile?: any
  ): Promise<string[]> {
    const provider = this.getProvider();
    
    const contextInfo = userProfile ? `
User Context:
- Location: ${userProfile.location || 'Not specified'}
- Crops: ${userProfile.crops?.join(', ') || 'None specified'}
- Livestock: ${userProfile.livestock?.join(', ') || 'None specified'}` : '';

    const prompt = `Analyze this agricultural conversation and extract 3-5 key facts/tags that represent the main topics discussed.

${contextInfo}

Conversation:
${conversationHistory}

Requirements:
1. Extract factual tags about agricultural topics (crops, diseases, techniques, seasons, etc.)
2. Tags should be in ${userLanguage} language
3. Focus on actionable agricultural knowledge
4. Each tag should be 2-4 words maximum
5. Return ONLY the tags, one per line, no numbering or bullets

Example tags for reference:
- Tomato leaf curl
- Organic fertilizer
- Monsoon planting
- Pest control
- Soil testing`;

    const messages: AIMessage[] = [
      { role: 'user', content: prompt }
    ];

    try {
      const result = await provider.generateResponse(messages);
      const tags = result.content
        .split('\n')
        .filter(tag => tag.trim())
        .map(tag => tag.trim())
        .slice(0, 5); // Limit to 5 tags
      
      return tags;
    } catch (error) {
      logger.error('Failed to extract conversation tags:', error);
      return [];
    }
  }

  async generateConversationSummary(
    conversationHistory: string,
    userLanguage: string,
    userProfile?: any
  ): Promise<string> {
    const provider = this.getProvider();
    
    const contextInfo = userProfile ? `
User Context:
- Location: ${userProfile.location || 'Not specified'}
- Crops: ${userProfile.crops?.join(', ') || 'None specified'}
- Livestock: ${userProfile.livestock?.join(', ') || 'None specified'}` : '';

    const prompt = `Create a concise summary of this agricultural conversation in ${userLanguage} language.

${contextInfo}

Conversation:
${conversationHistory}

Requirements:
1. Summarize in ${userLanguage} language ONLY
2. Maximum 100 words
3. Focus on key agricultural advice given
4. Include main problems discussed and solutions suggested
5. Highlight any specific recommendations made

Output: Return only the summary, nothing else.`;

    const messages: AIMessage[] = [
      { role: 'user', content: prompt }
    ];

    try {
      const result = await provider.generateResponse(messages);
      return result.content.trim().substring(0, 500); // Limit to 500 chars
    } catch (error) {
      logger.error('Failed to generate conversation summary:', error);
      return conversationHistory.substring(0, 100) + '...'; // Fallback
    }
  }

  async translateTagsToEnglish(
    tags: string[],
    sourceLanguage: string
  ): Promise<string[]> {
    if (sourceLanguage === 'en') return tags; // Already in English
    
    const provider = this.getProvider();
    
    const prompt = `Translate these agricultural tags from ${sourceLanguage} to English. Maintain their agricultural context and meaning.

Tags to translate:
${tags.map((tag, i) => `${i + 1}. ${tag}`).join('\n')}

Requirements:
1. Translate to English while preserving agricultural meaning
2. Keep tags concise (2-4 words each)
3. Use standard agricultural terminology
4. Return only the translated tags, one per line, no numbering

Example:
If input is "टमाटर का रोग" → output "Tomato disease"
If input is "जैविक खाद" → output "Organic fertilizer"`;

    const messages: AIMessage[] = [
      { role: 'user', content: prompt }
    ];

    try {
      const result = await provider.generateResponse(messages);
      const translatedTags = result.content
        .split('\n')
        .filter(tag => tag.trim())
        .map(tag => tag.trim())
        .slice(0, tags.length); // Same number as input
      
      return translatedTags;
    } catch (error) {
      logger.error('Failed to translate tags to English:', error);
      return tags; // Return original if translation fails
    }
  }

  private async getConversationHistoryText(conversationId: string): Promise<string> {
    const result = await query<{ content: string; is_user: boolean }>(
      `SELECT content, is_user FROM messages 
       WHERE conversation_id = $1 
       ORDER BY created_at ASC`,
      [conversationId]
    );
    
    return result.rows
      .map(row => `${row.is_user ? 'User' : 'Assistant'}: ${row.content}`)
      .join('\n\n');
  }

  async processConversationAnalytics(
    conversationId: string,
    userLanguage: string,
    userProfile?: any
  ): Promise<{
    tags: string[],
    englishTags: string[],
    summary: string
  }> {
    try {
      const conversationHistory = await this.getConversationHistoryText(conversationId);
      
      // Extract tags in user's language
      const tags = await this.extractConversationTags(
        conversationHistory,
        userLanguage,
        userProfile
      );
      
      // Translate tags to English for storage
      const englishTags = await this.translateTagsToEnglish(tags, userLanguage);
      
      // Generate summary in user's language
      const summary = await this.generateConversationSummary(
        conversationHistory,
        userLanguage,
        userProfile
      );
      
      // Store analytics in database
      await this.storeConversationAnalytics(conversationId, tags, englishTags, summary);
      
      return { tags, englishTags, summary };
    } catch (error) {
      logger.error('Failed to process conversation analytics:', error);
      return { tags: [], englishTags: [], summary: '' };
    }
  }

  private async storeConversationAnalytics(
    conversationId: string,
    tags: string[],
    englishTags: string[],
    summary: string
  ): Promise<void> {
    try {
      // Update conversation with tags and summary
      await query(
        `UPDATE conversations 
         SET tags = $1, 
             english_tags = $2, 
             summary = $3,
             updated_at = NOW()
         WHERE id = $4`,
        [JSON.stringify(tags), JSON.stringify(englishTags), summary, conversationId]
      );
    } catch (error) {
      logger.error('Failed to store conversation analytics:', error);
    }
  }
  
  async transcribeAudio(audioBuffer: Buffer, language?: string): Promise<string> {
    logger.info('=== AI SERVICE TRANSCRIBE START ===');
    logger.info('Audio buffer size:', audioBuffer.length);
    logger.info('Language:', language);
    
    // Currently only OpenAI supports audio transcription
    const openaiProvider = this.providers.get('openai') as any;
    
    logger.info('OpenAI provider exists:', !!openaiProvider);
    logger.info('OpenAI provider has transcribeAudio:', !!(openaiProvider && openaiProvider.transcribeAudio));
    
    if (!openaiProvider || !openaiProvider.transcribeAudio) {
      throw new Error('Audio transcription is not available. OpenAI provider is required.');
    }
    
    try {
      logger.info('Calling OpenAI provider transcribeAudio...');
      const transcription = await openaiProvider.transcribeAudio(audioBuffer, language);
      logger.info('=== AI SERVICE TRANSCRIBE SUCCESS ===');
      logger.info('Audio transcription successful', { 
        language, 
        transcriptionLength: transcription.length,
        transcriptionPreview: transcription.substring(0, 100) + '...'
      });
      return transcription;
    } catch (error: any) {
      logger.error('=== AI SERVICE TRANSCRIBE ERROR ===');
      logger.error('Failed to transcribe audio:', {
        errorMessage: error.message,
        errorType: error.constructor.name,
        errorStack: error.stack
      });
      throw error;
    }
  }
}