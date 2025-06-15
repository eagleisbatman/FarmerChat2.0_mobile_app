import { GoogleGenerativeAI, GenerativeModel, ChatSession } from '@google/generative-ai';
import { BaseAIProvider, AIMessage, AIResponse, StreamChunk, AIProviderConfig } from './base.provider';
import { logger } from '../../utils/logger';

export class GeminiProvider extends BaseAIProvider {
  private genAI: GoogleGenerativeAI;
  private generativeModel: GenerativeModel;
  
  constructor(config: AIProviderConfig) {
    super(config);
    this.genAI = new GoogleGenerativeAI(this.apiKey);
    this.generativeModel = this.genAI.getGenerativeModel({
      model: this.model,
      generationConfig: {
        temperature: this.temperature,
        maxOutputTokens: this.maxTokens,
        topP: this.topP,
        topK: this.topK,
      },
    });
  }
  
  getProviderName(): string {
    return 'gemini';
  }
  
  getDefaultModel(): string {
    return 'gemini-1.5-pro-latest';
  }
  
  getAvailableModels(): string[] {
    return [
      'gemini-1.5-pro-latest',
      'gemini-1.5-pro',
      'gemini-1.5-flash',
      'gemini-1.5-flash-8b',
      'gemini-pro',
      'gemini-pro-vision',
    ];
  }
  
  private convertToGeminiMessages(messages: AIMessage[]): Array<{ role: string; parts: Array<{ text: string }> }> {
    return messages.map(msg => ({
      role: msg.role === 'assistant' ? 'model' : 'user',
      parts: [{ text: msg.content }],
    }));
  }
  
  async generateResponse(messages: AIMessage[]): Promise<AIResponse> {
    try {
      const chat = this.generativeModel.startChat({
        history: this.convertToGeminiMessages(messages.slice(0, -1)),
      });
      
      const lastMessage = messages[messages.length - 1];
      const result = await chat.sendMessage(lastMessage.content);
      const response = result.response;
      
      return {
        content: response.text(),
        usage: {
          promptTokens: response.usageMetadata?.promptTokenCount || 0,
          completionTokens: response.usageMetadata?.candidatesTokenCount || 0,
          totalTokens: response.usageMetadata?.totalTokenCount || 0,
        },
        model: this.model,
        finishReason: response.candidates?.[0]?.finishReason,
      };
    } catch (error) {
      logger.error('Gemini generation error:', error);
      throw error;
    }
  }
  
  async generateStreamResponse(
    messages: AIMessage[],
    onChunk: (chunk: StreamChunk) => void
  ): Promise<AIResponse> {
    try {
      const chat = this.generativeModel.startChat({
        history: this.convertToGeminiMessages(messages.slice(0, -1)),
      });
      
      const lastMessage = messages[messages.length - 1];
      const result = await chat.sendMessageStream(lastMessage.content);
      
      let fullContent = '';
      let usageMetadata;
      
      for await (const chunk of result.stream) {
        const text = chunk.text();
        fullContent += text;
        onChunk({
          content: text,
          isComplete: false,
        });
        usageMetadata = chunk.usageMetadata;
      }
      
      onChunk({
        content: '',
        isComplete: true,
      });
      
      return {
        content: fullContent,
        usage: {
          promptTokens: usageMetadata?.promptTokenCount || 0,
          completionTokens: usageMetadata?.candidatesTokenCount || 0,
          totalTokens: usageMetadata?.totalTokenCount || 0,
        },
        model: this.model,
      };
    } catch (error) {
      logger.error('Gemini streaming error:', error);
      throw error;
    }
  }
}