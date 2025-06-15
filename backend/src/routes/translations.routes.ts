import express from 'express';
import { TranslationService } from '../services/translation.service';
import { CacheService } from '../services/cache.service';
import { AppError } from '../middleware/errorHandler';
import { logger } from '../utils/logger';

const router = express.Router();
const cacheService = new CacheService();
const translationService = new TranslationService(cacheService);

// Get all translations for a language
router.get('/:languageCode', async (req, res) => {
  try {
    const { languageCode } = req.params;
    
    const translations = await translationService.getTranslations(languageCode);
    
    res.json({
      success: true,
      data: {
        languageCode,
        translations
      }
    });
  } catch (error) {
    logger.error('Get translations error:', error);
    res.status(500).json({
      success: false,
      error: error instanceof Error ? error.message : 'Failed to get translations'
    });
  }
});

// Get supported languages
router.get('/', async (req, res) => {
  try {
    const languages = await translationService.getSupportedLanguages();
    
    res.json({
      success: true,
      data: { languages }
    });
  } catch (error) {
    logger.error('Get supported languages error:', error);
    res.status(500).json({
      success: false,
      error: error instanceof Error ? error.message : 'Failed to get supported languages'
    });
  }
});

// Get crop translations
router.get('/:languageCode/crops', async (req, res) => {
  try {
    const { languageCode } = req.params;
    
    const crops = await translationService.getCropTranslations(languageCode);
    
    res.json({
      success: true,
      data: {
        languageCode,
        crops
      }
    });
  } catch (error) {
    logger.error('Get crop translations error:', error);
    res.status(500).json({
      success: false,
      error: error instanceof Error ? error.message : 'Failed to get crop translations'
    });
  }
});

// Get livestock translations
router.get('/:languageCode/livestock', async (req, res) => {
  try {
    const { languageCode } = req.params;
    
    const livestock = await translationService.getLivestockTranslations(languageCode);
    
    res.json({
      success: true,
      data: {
        languageCode,
        livestock
      }
    });
  } catch (error) {
    logger.error('Get livestock translations error:', error);
    res.status(500).json({
      success: false,
      error: error instanceof Error ? error.message : 'Failed to get livestock translations'
    });
  }
});

// Search crops/livestock
router.get('/:languageCode/search', async (req, res) => {
  try {
    const { languageCode } = req.params;
    const { q: query, type } = req.query;
    
    if (!query) {
      throw new AppError('Search query is required', 400);
    }
    
    let results;
    if (type === 'crops') {
      results = await translationService.searchCrops(languageCode, query as string);
    } else if (type === 'livestock') {
      results = await translationService.searchLivestock(languageCode, query as string);
    } else {
      // Search both
      const [crops, livestock] = await Promise.all([
        translationService.searchCrops(languageCode, query as string),
        translationService.searchLivestock(languageCode, query as string)
      ]);
      results = { crops, livestock };
    }
    
    res.json({
      success: true,
      data: {
        query,
        languageCode,
        results
      }
    });
  } catch (error) {
    logger.error('Search translations error:', error);
    res.status(500).json({
      success: false,
      error: error instanceof Error ? error.message : 'Failed to search translations'
    });
  }
});

export default router;