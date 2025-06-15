import { PromptService } from '../src/services/prompt.service';
import { logger } from '../src/utils/logger';

async function seedPrompts() {
  try {
    logger.info('Starting prompt seeding...');
    
    const promptService = new PromptService();
    await promptService.seedDefaultPrompts();
    
    logger.info('Prompt seeding completed successfully!');
    process.exit(0);
  } catch (error) {
    logger.error('Prompt seeding failed:', error);
    process.exit(1);
  }
}

seedPrompts();