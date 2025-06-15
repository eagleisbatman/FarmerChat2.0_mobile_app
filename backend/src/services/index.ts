import { logger } from '../utils/logger';
import { CacheService } from './cache.service';
import { AIService } from './ai.service';
import { TranslationService } from './translation.service';
import { NotificationService } from './notification.service';

// Service instances
export let cacheService: CacheService;
export let aiService: AIService;
export let translationService: TranslationService;
export let notificationService: NotificationService;

// Initialize all services
export async function initializeServices(): Promise<void> {
  try {
    // Initialize cache service
    cacheService = new CacheService();
    await cacheService.connect();
    logger.info('Cache service initialized');
    
    // Initialize AI service
    aiService = new AIService();
    logger.info('AI service initialized');
    
    // Initialize translation service
    translationService = new TranslationService(cacheService);
    logger.info('Translation service initialized');
    
    // Initialize notification service (FCM)
    notificationService = new NotificationService();
    await notificationService.initialize();
    logger.info('Notification service initialized');
    
  } catch (error) {
    logger.error('Service initialization failed:', error);
    throw error;
  }
}

// Export services
export * from './cache.service';
export * from './ai.service';
export * from './translation.service';
export * from './auth.service';
export * from './conversation.service';
export * from './user.service';
export * from './prompt.service';
export * from './notification.service';