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
    // Generate dynamic system prompt without database dependency
    const location = userProfile?.location || 'unspecified location';
    const crops = Array.isArray(userProfile?.crops) ? userProfile.crops.join(', ') : 'various crops';
    const livestock = Array.isArray(userProfile?.livestock) ? userProfile.livestock.join(', ') : 'various livestock';
    const language = this.getLanguageName(languageCode);
    const userName = userProfile?.name || 'farmer';

    return `You are an AI agricultural assistant helping ${userName}, a farmer in ${location}.

Key Information:
- Location: ${location}
- Crops: ${crops}
- Livestock: ${livestock}
- Language: ${language} (code: ${languageCode})

CRITICAL INSTRUCTIONS:
1. YOU MUST RESPOND IN ${language.toUpperCase()} LANGUAGE ONLY. DO NOT USE ENGLISH.
2. EVERY WORD OF YOUR RESPONSE MUST BE IN ${language.toUpperCase()}.
3. If the user writes in ${language}, you MUST respond in ${language}.
4. Provide practical, actionable advice specific to their location and farming context
5. Consider local climate, soil conditions, and agricultural practices
6. Be concise but thorough in your responses
7. If asked about crops/livestock they don't grow, still provide helpful information
8. Always be encouraging and supportive
9. Use simple, clear language that farmers can easily understand
10. Include 2-3 relevant emojis in your response to make it more engaging (e.g., 🌱 for crops, 🌾 for harvest, 💧 for water, ☀️ for sun, 🐄 for cattle, 🐓 for poultry, 🌿 for plants, 📅 for timing, ⚠️ for warnings)

IMPORTANT: Your entire response must be in ${language} language. No English words except for technical terms that have no translation.

Remember: You are their trusted agricultural advisor. Help them improve their farming practices and livelihoods.`;
  }

  async getFollowUpPrompt(
    response: string,
    userProfile: any,
    languageCode: string = 'en'
  ): Promise<string> {
    // Generate dynamic follow-up prompt without database dependency
    const responseSnippet = response.substring(0, 500);
    const language = this.getLanguageName(languageCode);
    const crops = Array.isArray(userProfile?.crops) ? userProfile.crops.join(', ') : 'various crops';
    const livestock = Array.isArray(userProfile?.livestock) ? userProfile.livestock.join(', ') : 'various livestock';

    return `Based on this agricultural advice given to a farmer who grows ${crops} and raises ${livestock}:
"${responseSnippet}"

Generate 3 follow-up questions that the FARMER would ask to LEARN MORE about this topic.

Requirements:
1. Questions MUST be in ${language} language ONLY - NO ENGLISH
2. SHORT and CONCISE (maximum 40 characters each)
3. These are questions the FARMER asks TO get more information
4. Questions should dig deeper into the advice given
5. Be practical and actionable
6. Build upon the information provided

Example format (but in ${language}):
How much fertilizer to use?
When is best time to apply?
What are the costs?

Output ONLY the 3 questions in ${language}, one per line, without numbering, bullets, or hyphens.`;
  }

  async getTitlePrompt(
    firstMessage: string,
    firstResponse: string,
    languageCode: string = 'en'
  ): Promise<string> {
    // Generate dynamic title prompt without database dependency
    const messageSnippet = firstMessage.substring(0, 200);
    const responseSnippet = firstResponse.substring(0, 200);
    const language = this.getLanguageName(languageCode);

    return `Based on this conversation:
User: "${messageSnippet}"
Assistant: "${responseSnippet}"

Generate a SHORT conversation title that:
1. Is in ${language} language ONLY
2. Is maximum 4-5 words
3. Captures the main topic
4. Is clear and descriptive

Output ONLY the title, without quotes or extra formatting.`;
  }

  async getStarterQuestionPrompt(
    userProfile: any,
    languageCode: string = 'en',
    currentMonth?: string
  ): Promise<string> {
    // Generate dynamic starter question prompt without database dependency
    const crops = Array.isArray(userProfile?.crops) ? userProfile.crops.join(', ') : 'various crops';
    const livestock = Array.isArray(userProfile?.livestock) ? userProfile.livestock.join(', ') : 'various livestock';
    const location = userProfile?.location || 'unspecified location';
    const language = this.getLanguageName(languageCode);
    const month = currentMonth || new Date().toLocaleString('en', { month: 'long' });

    // Dynamic prompt generation based on user profile
    if (!userProfile?.crops?.length && !userProfile?.livestock?.length) {
      // Generic prompt for users without specific crops/livestock
      return `Generate 4 starter questions for a farmer in ${location} in ${language} language ONLY.
The questions should be:
1. General farming advice
2. Crop selection recommendations
3. Seasonal farming tips for ${month}
4. Agricultural best practices

Each question must be:
- SHORT and CONCISE (maximum 60 characters)
- In ${language} language ONLY
- Practical and relevant to farmers
- Formatted as a simple question

Output ONLY the 4 questions, one per line, without numbering or bullet points.`;
    }

    // Specific prompt for users with crops/livestock
    return `Generate 4 starter questions that a farmer in ${location} who grows ${crops} and raises ${livestock} would ASK to an agricultural AI assistant.

Requirements:
1. Generate questions in ${language} language ONLY - NO ENGLISH
2. Each question must be SHORT and CONCISE (maximum 60 characters)
3. Questions MUST be specific to the farmer's crops/livestock listed above
4. Questions should be seasonally relevant for ${month}
5. Make questions practical and actionable
6. These are questions the FARMER asks TO the AI assistant (not questions the AI asks the farmer)
7. Frame as questions seeking advice, information, or solutions

Example format (but in ${language}):
How can I improve wheat yield?
What fertilizer for sorghum?
When to plant barley?
How to prevent cow diseases?

Output ONLY the 4 questions in ${language}, one per line, without numbering, bullets, or hyphens.`;
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