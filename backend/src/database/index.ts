import { config } from '../config';
import { logger } from '../utils/logger';
import { testConnection, pool } from './db';

// Database initialization
export async function initializeDatabase(): Promise<void> {
  try {
    // Test Neon database connection
    const connected = await testConnection();
    if (!connected) {
      throw new Error('Failed to connect to Neon database');
    }
    
    logger.info('Neon database connection established');
    
    // Verify schema
    await verifySchema();
  } catch (error) {
    logger.error('Database initialization failed:', error);
    throw error;
  }
}

// Verify database schema
async function verifySchema(): Promise<void> {
  try {
    const tables = [
      'users',
      'conversations',
      'messages',
      'prompts',
      'ui_translations',
      'crop_translations',
      'livestock_translations',
      'translation_metadata',
      'feedback',
      'starter_questions',
      'api_usage',
      'notifications'
    ];
    
    for (const table of tables) {
      const result = await pool.query(
        `SELECT EXISTS (
          SELECT FROM information_schema.tables 
          WHERE table_schema = 'public' 
          AND table_name = $1
        )`,
        [table]
      );
      
      if (!result.rows[0].exists) {
        logger.warn(`Table '${table}' does not exist`);
      }
    }
    
    logger.info('Database schema verified');
  } catch (error) {
    logger.error('Schema verification failed:', error);
    throw error;
  }
}

// Export database utilities
export * from './db';