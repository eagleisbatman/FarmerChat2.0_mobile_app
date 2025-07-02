import { Router, Request, Response, NextFunction } from 'express';
import { TranslationService } from '../services/translation.service';
import { cacheService } from '../services';
import { translationRateLimiter } from '../middleware/rateLimiter';
import { optionalAuth } from '../middleware/auth';
import { AppError } from '../middleware/errorHandler';

const router = Router();
const translationService = new TranslationService(cacheService);

// Apply rate limiting to all translation endpoints
router.use(translationRateLimiter);

// Get all translations for a language
router.get('/:languageCode', optionalAuth, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { languageCode } = req.params;
    
    if (!languageCode) {
      throw new AppError('Language code is required', 400);
    }
    
    const translations = await translationService.getTranslations(languageCode);
    
    res.json({
      success: true,
      data: translations
    });
  } catch (error) {
    next(error);
  }
});

// Get supported languages
router.get('/languages', optionalAuth, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const languages = await translationService.getSupportedLanguages();
    
    res.json({
      success: true,
      data: languages
    });
  } catch (error) {
    next(error);
  }
});

export default router;