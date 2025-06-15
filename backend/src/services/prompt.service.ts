import { query } from '../database';
import { logger } from '../utils/logger';

export interface PromptTemplate {
  id: string;
  name: string;
  category: 'system' | 'follow_up' | 'title' | 'starter_question';
  languageCode: string;
  template: string;
  variables: string[];
  version: number;
  isActive: boolean;
  metadata?: any;
  createdAt: Date;
  updatedAt: Date;
}

export interface PromptVariables {
  [key: string]: string | string[] | number;
}

export class PromptService {
  private promptCache: Map<string, PromptTemplate> = new Map();
  private cacheExpiry: number = 5 * 60 * 1000; // 5 minutes
  private lastCacheUpdate: number = 0;

  constructor() {
    this.loadPrompts();
  }

  async getPrompt(
    category: PromptTemplate['category'],
    languageCode: string = 'en'
  ): Promise<PromptTemplate | null> {
    await this.refreshCacheIfNeeded();
    
    const cacheKey = `${category}_${languageCode}`;
    let prompt = this.promptCache.get(cacheKey);
    
    // Fallback to English if prompt not found in requested language
    if (!prompt && languageCode !== 'en') {
      const fallbackKey = `${category}_en`;
      prompt = this.promptCache.get(fallbackKey);
    }
    
    return prompt || null;
  }

  async renderPrompt(
    category: PromptTemplate['category'],
    variables: PromptVariables,
    languageCode: string = 'en'
  ): Promise<string> {
    const prompt = await this.getPrompt(category, languageCode);
    
    if (!prompt) {
      throw new Error(`Prompt not found for category: ${category}, language: ${languageCode}`);
    }

    return this.interpolateTemplate(prompt.template, variables);
  }

  async getSystemPrompt(userProfile: any, languageCode: string = 'en'): Promise<string> {
    const variables: PromptVariables = {
      location: userProfile.location || 'unspecified location',
      crops: Array.isArray(userProfile.crops) ? userProfile.crops.join(', ') : 'various crops',
      livestock: Array.isArray(userProfile.livestock) ? userProfile.livestock.join(', ') : 'various livestock',
      language: this.getLanguageName(languageCode),
      languageCode: languageCode,
      userName: userProfile.name || 'farmer'
    };

    return this.renderPrompt('system', variables, languageCode);
  }

  async getFollowUpPrompt(
    response: string,
    userProfile: any,
    languageCode: string = 'en'
  ): Promise<string> {
    const variables: PromptVariables = {
      response: response.substring(0, 500), // Limit response length
      language: this.getLanguageName(languageCode),
      languageCode: languageCode,
      crops: Array.isArray(userProfile.crops) ? userProfile.crops.join(', ') : 'various crops',
      livestock: Array.isArray(userProfile.livestock) ? userProfile.livestock.join(', ') : 'various livestock'
    };

    return this.renderPrompt('follow_up', variables, languageCode);
  }

  async getTitlePrompt(
    firstMessage: string,
    firstResponse: string,
    languageCode: string = 'en'
  ): Promise<string> {
    const variables: PromptVariables = {
      firstMessage: firstMessage.substring(0, 200),
      firstResponse: firstResponse.substring(0, 200),
      language: this.getLanguageName(languageCode),
      languageCode: languageCode
    };

    return this.renderPrompt('title', variables, languageCode);
  }

  async getStarterQuestionPrompt(
    userProfile: any,
    languageCode: string = 'en',
    currentMonth?: string
  ): Promise<string> {
    const variables: PromptVariables = {
      crops: Array.isArray(userProfile.crops) ? userProfile.crops.join(', ') : 'various crops',
      livestock: Array.isArray(userProfile.livestock) ? userProfile.livestock.join(', ') : 'various livestock',
      location: userProfile.location || 'unspecified location',
      language: this.getLanguageName(languageCode),
      languageCode: languageCode,
      currentMonth: currentMonth || new Date().toLocaleString('en', { month: 'long' })
    };

    return this.renderPrompt('starter_question', variables, languageCode);
  }

  async createPrompt(prompt: Partial<PromptTemplate>): Promise<PromptTemplate> {
    const metadata = {
      languageCode: prompt.languageCode || 'en',
      ...prompt.metadata
    };
    
    const result = await query<any>(
      `INSERT INTO prompts (name, category, template, variables, version, is_active)
       VALUES ($1, $2, $3, $4, $5, $6)
       RETURNING *`,
      [
        prompt.name,
        prompt.category,
        prompt.template,
        JSON.stringify(prompt.variables || []),
        prompt.version || 1,
        prompt.isActive ?? true
      ]
    );

    const newPrompt = this.mapDbRowToPrompt(result.rows[0], prompt.languageCode || 'en');
    this.promptCache.set(`${newPrompt.category}_${newPrompt.languageCode}`, newPrompt);
    
    return newPrompt;
  }

  async updatePrompt(id: string, updates: Partial<PromptTemplate>): Promise<PromptTemplate | null> {
    const setClauses = [];
    const values = [];
    let paramIndex = 1;

    if (updates.name !== undefined) {
      setClauses.push(`name = $${paramIndex++}`);
      values.push(updates.name);
    }
    if (updates.template !== undefined) {
      setClauses.push(`template = $${paramIndex++}`);
      values.push(updates.template);
    }
    if (updates.variables !== undefined) {
      setClauses.push(`variables = $${paramIndex++}`);
      values.push(JSON.stringify(updates.variables));
    }
    if (updates.isActive !== undefined) {
      setClauses.push(`is_active = $${paramIndex++}`);
      values.push(updates.isActive);
    }
    if (updates.metadata !== undefined) {
      setClauses.push(`metadata = $${paramIndex++}`);
      values.push(JSON.stringify(updates.metadata));
    }

    setClauses.push(`updated_at = NOW()`);
    values.push(id);

    const result = await query<PromptTemplate>(
      `UPDATE prompts SET ${setClauses.join(', ')} WHERE id = $${paramIndex} RETURNING *`,
      values
    );

    if (result.rows.length === 0) return null;

    const updatedPrompt = this.mapDbRowToPrompt(result.rows[0], 'en');
    this.promptCache.set(`${updatedPrompt.category}_${updatedPrompt.languageCode}`, updatedPrompt);
    
    return updatedPrompt;
  }

  async listPrompts(
    category?: PromptTemplate['category'],
    languageCode?: string,
    activeOnly: boolean = true
  ): Promise<PromptTemplate[]> {
    let query_text = 'SELECT * FROM prompts WHERE 1=1';
    const values = [];
    let paramIndex = 1;

    if (category) {
      query_text += ` AND category = $${paramIndex++}`;
      values.push(category);
    }

    if (languageCode) {
      query_text += ` AND language_code = $${paramIndex++}`;
      values.push(languageCode);
    }

    if (activeOnly) {
      query_text += ` AND is_active = true`;
    }

    query_text += ' ORDER BY category, version DESC';

    const result = await query<any>(query_text, values);
    return result.rows.map(row => this.mapDbRowToPrompt(row, languageCode || 'en'));
  }

  private async loadPrompts(): Promise<void> {
    try {
      const result = await query<any>('SELECT * FROM prompts WHERE is_active = true');
      
      this.promptCache.clear();
      result.rows.forEach(row => {
        const prompt = this.mapDbRowToPrompt(row, 'en');
        this.promptCache.set(`${prompt.category}_${prompt.languageCode}`, prompt);
      });
      
      this.lastCacheUpdate = Date.now();
      logger.info(`Loaded ${result.rows.length} active prompts into cache`);
    } catch (error) {
      logger.error('Failed to load prompts:', error);
    }
  }

  private async refreshCacheIfNeeded(): Promise<void> {
    if (Date.now() - this.lastCacheUpdate > this.cacheExpiry) {
      await this.loadPrompts();
    }
  }

  private interpolateTemplate(template: string, variables: PromptVariables): string {
    return template.replace(/\{\{(\w+)\}\}/g, (match, key) => {
      const value = variables[key];
      
      if (value === undefined) {
        logger.warn(`Template variable '${key}' not provided, using placeholder`);
        return `[${key}]`;
      }
      
      if (Array.isArray(value)) {
        return value.join(', ');
      }
      
      return String(value);
    });
  }

  private mapDbRowToPrompt(row: any, languageCode: string = 'en'): PromptTemplate {
    return {
      id: row.id,
      name: row.name,
      category: row.category,
      languageCode: languageCode,
      template: row.template,
      variables: Array.isArray(row.variables) ? row.variables : JSON.parse(row.variables || '[]'),
      version: row.version,
      isActive: row.is_active,
      metadata: {},
      createdAt: new Date(row.created_at),
      updatedAt: new Date(row.updated_at)
    };
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
      de: 'German',
      ja: 'Japanese',
      ko: 'Korean',
      ru: 'Russian',
      it: 'Italian',
      nl: 'Dutch'
    };
    
    return languages[code] || 'English';
  }

  async seedDefaultPrompts(): Promise<void> {
    const defaultPrompts = [
      {
        name: 'Agricultural Expert System Prompt',
        category: 'system' as const,
        languageCode: 'en',
        template: `You are an expert agricultural advisor helping farmers make better decisions. 
The farmer you're speaking with is located in {{location}}, grows {{crops}}, and raises {{livestock}}.
Please provide practical, actionable advice tailored to their specific context.
IMPORTANT: Always respond in {{language}} language (code: {{languageCode}}).
Keep responses concise but comprehensive, using bullet points where appropriate.
Focus on sustainable and locally appropriate farming practices.
Address the farmer respectfully as {{userName}}.`,
        variables: ['location', 'crops', 'livestock', 'language', 'languageCode', 'userName'],
        version: 1
      },
      {
        name: 'Follow-up Questions Generator',
        category: 'follow_up' as const,
        languageCode: 'en',
        template: `Based on this agricultural advice response, generate 3 short follow-up questions that a farmer might ask.

Response: {{response}}

Context: Farmer grows {{crops}} and raises {{livestock}}.

Requirements:
1. Generate questions in {{language}} language ONLY
2. Each question must be SHORT and CONCISE (maximum 40 characters)
3. Questions should be practical and actionable
4. Focus on the farmer's specific crops and livestock

Output format: Return only the 3 questions, one per line, no numbering or bullets.`,
        variables: ['response', 'language', 'languageCode', 'crops', 'livestock'],
        version: 1
      },
      {
        name: 'Conversation Title Generator',
        category: 'title' as const,
        languageCode: 'en',
        template: `Generate a short, descriptive title for this agricultural conversation in {{language}} language.

User: {{firstMessage}}
Assistant: {{firstResponse}}...

Requirements:
1. Title must be in {{language}} language
2. Maximum 50 characters
3. Should summarize the main agricultural topic
4. Be specific and descriptive

Output: Return only the title, nothing else.`,
        variables: ['firstMessage', 'firstResponse', 'language', 'languageCode'],
        version: 1
      },
      {
        name: 'Starter Questions Generator',
        category: 'starter_question' as const,
        languageCode: 'en',
        template: `Generate 4 relevant starter questions for a farmer who grows {{crops}} and raises {{livestock}} in {{location}}.

Requirements:
1. Generate questions in {{language}} language ONLY
2. Each question must be SHORT and CONCISE (maximum 60 characters)
3. Questions MUST be specific to the farmer's crops/livestock listed above
4. Questions should be seasonally relevant for {{currentMonth}}
5. Focus on practical farming challenges and opportunities

Output format: Return only the 4 questions, one per line, no numbering or bullets.`,
        variables: ['crops', 'livestock', 'location', 'language', 'languageCode', 'currentMonth'],
        version: 1
      }
    ];

    for (const prompt of defaultPrompts) {
      try {
        await this.createPrompt(prompt);
        logger.info(`Created default prompt: ${prompt.name}`);
      } catch (error) {
        // Prompt might already exist, that's okay
        logger.debug(`Prompt already exists: ${prompt.name}`);
      }
    }
  }
}