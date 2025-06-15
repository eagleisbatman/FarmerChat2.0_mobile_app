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