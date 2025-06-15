import { CacheService } from './cache.service';
import { query } from '../database';
import { logger } from '../utils/logger';
import { AppError } from '../middleware/errorHandler';

export interface TranslationBundle {
  ui: Record<string, string>;
  crops: Record<string, { name: string; description?: string }>;
  livestock: Record<string, { name: string; description?: string }>;
  metadata: {
    language: string;
    lastUpdated: string;
    coverage: number;
  };
}

export interface Language {
  code: string;
  name: string;
  nativeName: string;
  isRTL: boolean;
  coverage: number;
}

export class TranslationService {
  constructor(private cache: CacheService) {}
  
  async getTranslations(languageCode: string): Promise<TranslationBundle> {
    // Check cache first
    const cacheKey = `translations:${languageCode}`;
    const cached = await this.cache.get<TranslationBundle>(cacheKey);
    if (cached) {
      return cached;
    }
    
    try {
      // Fetch UI translations
      const uiResult = await query<{ key: string; translation: string }>(
        'SELECT key, translation FROM ui_translations WHERE language_code = $1',
        [languageCode]
      );
      
      const ui = uiResult.rows.reduce((acc, row) => {
        acc[row.key] = row.translation;
        return acc;
      }, {} as Record<string, string>);
      
      // Fetch crop translations
      const cropResult = await query<{ crop_id: string; name: string; description: string }>(
        'SELECT crop_id, name, description FROM crop_translations WHERE language_code = $1',
        [languageCode]
      );
      
      const crops = cropResult.rows.reduce((acc, row) => {
        acc[row.crop_id] = {
          name: row.name,
          description: row.description || undefined
        };
        return acc;
      }, {} as Record<string, { name: string; description?: string }>);
      
      // Fetch livestock translations
      const livestockResult = await query<{ livestock_id: string; name: string; description: string }>(
        'SELECT livestock_id, name, description FROM livestock_translations WHERE language_code = $1',
        [languageCode]
      );
      
      const livestock = livestockResult.rows.reduce((acc, row) => {
        acc[row.livestock_id] = {
          name: row.name,
          description: row.description || undefined
        };
        return acc;
      }, {} as Record<string, { name: string; description?: string }>);
      
      // Fetch metadata
      const metadataResult = await query<{
        language_name: string;
        translation_coverage: number;
        last_updated: string;
      }>(
        'SELECT language_name, translation_coverage, last_updated FROM translation_metadata WHERE language_code = $1',
        [languageCode]
      );
      
      if (metadataResult.rows.length === 0) {
        throw new AppError('Language not found', 404);
      }
      
      const metadata = metadataResult.rows[0];
      
      const bundle: TranslationBundle = {
        ui,
        crops,
        livestock,
        metadata: {
          language: metadata.language_name,
          lastUpdated: metadata.last_updated,
          coverage: metadata.translation_coverage
        }
      };
      
      // Cache the result
      await this.cache.set(cacheKey, bundle, 3600); // 1 hour TTL
      
      return bundle;
    } catch (error) {
      logger.error('Error fetching translations:', error);
      throw error;
    }
  }
  
  async getSupportedLanguages(): Promise<Language[]> {
    const cacheKey = 'languages:all';
    const cached = await this.cache.get<Language[]>(cacheKey);
    if (cached) {
      return cached;
    }
    
    try {
      const result = await query<{
        language_code: string;
        language_name: string;
        native_name: string;
        is_rtl: boolean;
        translation_coverage: number;
      }>(
        'SELECT language_code, language_name, native_name, is_rtl, translation_coverage FROM translation_metadata WHERE is_enabled = true ORDER BY language_name'
      );
      
      const languages: Language[] = result.rows.map(row => ({
        code: row.language_code,
        name: row.language_name,
        nativeName: row.native_name,
        isRTL: row.is_rtl,
        coverage: row.translation_coverage
      }));
      
      // Cache for 24 hours
      await this.cache.set(cacheKey, languages, 86400);
      
      return languages;
    } catch (error) {
      logger.error('Error fetching languages:', error);
      throw error;
    }
  }
  
  async updateTranslation(
    type: 'ui' | 'crops' | 'livestock',
    key: string,
    languageCode: string,
    translation: string,
    context?: string
  ): Promise<void> {
    try {
      switch (type) {
        case 'ui':
          await query(
            `INSERT INTO ui_translations (key, language_code, translation, context)
             VALUES ($1, $2, $3, $4)
             ON CONFLICT (key, language_code)
             DO UPDATE SET translation = $3, context = $4, updated_at = NOW()`,
            [key, languageCode, translation, context]
          );
          break;
          
        case 'crops':
          await query(
            `INSERT INTO crop_translations (crop_id, language_code, name)
             VALUES ($1, $2, $3)
             ON CONFLICT (crop_id, language_code)
             DO UPDATE SET name = $3, updated_at = NOW()`,
            [key, languageCode, translation]
          );
          break;
          
        case 'livestock':
          await query(
            `INSERT INTO livestock_translations (livestock_id, language_code, name)
             VALUES ($1, $2, $3)
             ON CONFLICT (livestock_id, language_code)
             DO UPDATE SET name = $3, updated_at = NOW()`,
            [key, languageCode, translation]
          );
          break;
      }
      
      // Invalidate cache
      await this.cache.delete(`translations:${languageCode}`);
      
      // Update coverage
      await this.updateTranslationCoverage(languageCode);
    } catch (error) {
      logger.error('Error updating translation:', error);
      throw error;
    }
  }
  
  private async updateTranslationCoverage(languageCode: string): Promise<void> {
    try {
      // Calculate coverage based on English translations
      const totalResult = await query<{ count: string }>(
        'SELECT COUNT(DISTINCT key) as count FROM ui_translations WHERE language_code = $1',
        ['en']
      );
      
      const translatedResult = await query<{ count: string }>(
        'SELECT COUNT(DISTINCT key) as count FROM ui_translations WHERE language_code = $1',
        [languageCode]
      );
      
      const total = parseInt(totalResult.rows[0].count);
      const translated = parseInt(translatedResult.rows[0].count);
      const coverage = total > 0 ? Math.round((translated / total) * 100) : 0;
      
      await query(
        'UPDATE translation_metadata SET translation_coverage = $1, last_updated = NOW() WHERE language_code = $2',
        [coverage, languageCode]
      );
    } catch (error) {
      logger.error('Error updating translation coverage:', error);
    }
  }
  
  async importTranslations(
    type: 'ui' | 'crops' | 'livestock',
    languageCode: string,
    data: Array<{ key: string; translation: string; context?: string }>
  ): Promise<{ imported: number; failed: number }> {
    let imported = 0;
    let failed = 0;
    
    for (const item of data) {
      try {
        await this.updateTranslation(type, item.key, languageCode, item.translation, item.context);
        imported++;
      } catch (error) {
        logger.error(`Failed to import translation: ${item.key}`, error);
        failed++;
      }
    }
    
    // Invalidate cache
    await this.cache.delete(`translations:${languageCode}`);
    
    return { imported, failed };
  }

  async getCropTranslations(languageCode: string): Promise<Record<string, { name: string; description?: string }>> {
    try {
      const result = await query<{ crop_id: string; name: string; description: string }>(
        'SELECT crop_id, name, description FROM crop_translations WHERE language_code = $1',
        [languageCode]
      );
      
      return result.rows.reduce((acc, row) => {
        acc[row.crop_id] = {
          name: row.name,
          description: row.description || undefined
        };
        return acc;
      }, {} as Record<string, { name: string; description?: string }>);
    } catch (error) {
      logger.error('Error fetching crop translations:', error);
      throw error;
    }
  }

  async getLivestockTranslations(languageCode: string): Promise<Record<string, { name: string; description?: string }>> {
    try {
      const result = await query<{ livestock_id: string; name: string; description: string }>(
        'SELECT livestock_id, name, description FROM livestock_translations WHERE language_code = $1',
        [languageCode]
      );
      
      return result.rows.reduce((acc, row) => {
        acc[row.livestock_id] = {
          name: row.name,
          description: row.description || undefined
        };
        return acc;
      }, {} as Record<string, { name: string; description?: string }>);
    } catch (error) {
      logger.error('Error fetching livestock translations:', error);
      throw error;
    }
  }

  async searchCrops(languageCode: string, searchTerm: string): Promise<Record<string, { name: string; description?: string }>> {
    try {
      const result = await query<{ crop_id: string; name: string; description: string }>(
        'SELECT crop_id, name, description FROM crop_translations WHERE language_code = $1 AND name ILIKE $2',
        [languageCode, `%${searchTerm}%`]
      );
      
      return result.rows.reduce((acc, row) => {
        acc[row.crop_id] = {
          name: row.name,
          description: row.description || undefined
        };
        return acc;
      }, {} as Record<string, { name: string; description?: string }>);
    } catch (error) {
      logger.error('Error searching crops:', error);
      throw error;
    }
  }

  async searchLivestock(languageCode: string, searchTerm: string): Promise<Record<string, { name: string; description?: string }>> {
    try {
      const result = await query<{ livestock_id: string; name: string; description: string }>(
        'SELECT livestock_id, name, description FROM livestock_translations WHERE language_code = $1 AND name ILIKE $2',
        [languageCode, `%${searchTerm}%`]
      );
      
      return result.rows.reduce((acc, row) => {
        acc[row.livestock_id] = {
          name: row.name,
          description: row.description || undefined
        };
        return acc;
      }, {} as Record<string, { name: string; description?: string }>);
    } catch (error) {
      logger.error('Error searching livestock:', error);
      throw error;
    }
  }
}