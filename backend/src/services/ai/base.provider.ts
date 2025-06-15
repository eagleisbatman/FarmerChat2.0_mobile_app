import { logger } from '../../utils/logger';

export interface AIMessage {
  role: 'system' | 'user' | 'assistant';
  content: string;
}

export interface AIProviderConfig {
  apiKey: string;
  model?: string;
  temperature?: number;
  maxTokens?: number;
  topP?: number;
  topK?: number;
}

export interface AIResponse {
  content: string;
  usage?: {
    promptTokens: number;
    completionTokens: number;
    totalTokens: number;
  };
  model?: string;
  finishReason?: string;
}

export interface StreamChunk {
  content: string;
  isComplete: boolean;
}

export abstract class BaseAIProvider {
  protected apiKey: string;
  protected model: string;
  protected temperature: number;
  protected maxTokens: number;
  protected topP?: number;
  protected topK?: number;
  
  constructor(config: AIProviderConfig) {
    this.apiKey = config.apiKey;
    this.model = config.model || this.getDefaultModel();
    this.temperature = config.temperature ?? 0.7;
    this.maxTokens = config.maxTokens ?? 2048;
    this.topP = config.topP;
    this.topK = config.topK;
  }
  
  abstract getProviderName(): string;
  abstract getDefaultModel(): string;
  abstract getAvailableModels(): string[];
  abstract generateResponse(messages: AIMessage[]): Promise<AIResponse>;
  abstract generateStreamResponse(
    messages: AIMessage[],
    onChunk: (chunk: StreamChunk) => void
  ): Promise<AIResponse>;
  
  protected buildSystemMessage(userProfile: any): string {
    const crops = userProfile.crops?.join(', ') || 'various crops';
    const livestock = userProfile.livestock?.join(', ') || 'various livestock';
    const location = userProfile.location || 'unspecified location';
    const language = userProfile.language || 'en';
    
    return `You are an expert agricultural advisor helping farmers make better decisions. 
The farmer you're speaking with is located in ${location}, grows ${crops}, and raises ${livestock}.
Please provide practical, actionable advice tailored to their specific context.
Always respond in the farmer's preferred language (${language}).
Keep responses concise but comprehensive, using bullet points where appropriate.`;
  }
  
  async logUsage(
    userId: string,
    model: string,
    usage?: AIResponse['usage']
  ): Promise<void> {
    try {
      const { query } = await import('../../database');
      await query(
        `INSERT INTO api_usage (user_id, provider, model, prompt_tokens, completion_tokens, total_tokens)
         VALUES ($1, $2, $3, $4, $5, $6)`,
        [
          userId,
          this.getProviderName(),
          model,
          usage?.promptTokens || 0,
          usage?.completionTokens || 0,
          usage?.totalTokens || 0
        ]
      );
    } catch (error) {
      logger.error('Failed to log API usage:', error);
    }
  }
}