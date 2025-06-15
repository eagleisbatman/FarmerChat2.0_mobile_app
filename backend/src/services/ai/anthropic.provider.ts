import Anthropic from '@anthropic-ai/sdk';
import { BaseAIProvider, AIMessage, AIResponse, StreamChunk, AIProviderConfig } from './base.provider';
import { logger } from '../../utils/logger';

export class AnthropicProvider extends BaseAIProvider {
  private anthropic: Anthropic;
  
  constructor(config: AIProviderConfig) {
    super(config);
    this.anthropic = new Anthropic({
      apiKey: this.apiKey,
    });
  }
  
  getProviderName(): string {
    return 'anthropic';
  }
  
  getDefaultModel(): string {
    return 'claude-3-opus-20240229';
  }
  
  getAvailableModels(): string[] {
    return [
      'claude-3-5-sonnet-20241022',
      'claude-3-5-haiku-20241022',
      'claude-3-opus-20240229',
      'claude-3-sonnet-20240229',
      'claude-3-haiku-20240307',
    ];
  }
  
  private convertToAnthropicMessages(messages: AIMessage[]): Anthropic.MessageParam[] {
    return messages
      .filter(msg => msg.role !== 'system')
      .map(msg => ({
        role: msg.role === 'assistant' ? 'assistant' : 'user',
        content: msg.content,
      }));
  }
  
  private getSystemMessage(messages: AIMessage[]): string | undefined {
    const systemMessage = messages.find(msg => msg.role === 'system');
    return systemMessage?.content;
  }
  
  async generateResponse(messages: AIMessage[]): Promise<AIResponse> {
    try {
      const response = await this.anthropic.messages.create({
        model: this.model,
        messages: this.convertToAnthropicMessages(messages),
        system: this.getSystemMessage(messages),
        temperature: this.temperature,
        max_tokens: this.maxTokens,
        top_p: this.topP,
        top_k: this.topK,
      });
      
      const content = response.content
        .map(block => (block.type === 'text' ? block.text : ''))
        .join('');
      
      return {
        content,
        usage: {
          promptTokens: response.usage.input_tokens,
          completionTokens: response.usage.output_tokens,
          totalTokens: response.usage.input_tokens + response.usage.output_tokens,
        },
        model: response.model,
        finishReason: response.stop_reason || undefined,
      };
    } catch (error) {
      logger.error('Anthropic generation error:', error);
      throw error;
    }
  }
  
  async generateStreamResponse(
    messages: AIMessage[],
    onChunk: (chunk: StreamChunk) => void
  ): Promise<AIResponse> {
    try {
      const stream = await this.anthropic.messages.create({
        model: this.model,
        messages: this.convertToAnthropicMessages(messages),
        system: this.getSystemMessage(messages),
        temperature: this.temperature,
        max_tokens: this.maxTokens,
        top_p: this.topP,
        top_k: this.topK,
        stream: true,
      });
      
      let fullContent = '';
      let usage;
      let model = '';
      
      for await (const event of stream) {
        if (event.type === 'content_block_delta' && event.delta.type === 'text_delta') {
          const content = event.delta.text;
          fullContent += content;
          onChunk({
            content,
            isComplete: false,
          });
        } else if (event.type === 'message_start') {
          usage = event.message.usage;
          model = event.message.model;
        }
      }
      
      onChunk({
        content: '',
        isComplete: true,
      });
      
      return {
        content: fullContent,
        usage: usage ? {
          promptTokens: usage.input_tokens,
          completionTokens: usage.output_tokens || 0,
          totalTokens: (usage.input_tokens || 0) + (usage.output_tokens || 0),
        } : undefined,
        model,
      };
    } catch (error) {
      logger.error('Anthropic streaming error:', error);
      throw error;
    }
  }
}