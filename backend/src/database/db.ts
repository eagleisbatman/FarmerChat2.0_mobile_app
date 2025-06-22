import { Pool } from 'pg';
import { config } from '../config';
import { logger } from '../utils/logger';

// Create connection pool
export const pool = new Pool({
  connectionString: config.database.url,
  max: 20, // Maximum number of clients in the pool
  idleTimeoutMillis: 30000, // How long a client is allowed to remain idle before being closed
  connectionTimeoutMillis: 10000, // Increased timeout to 10 seconds for initial connection
  ssl: {
    rejectUnauthorized: false // Required for Neon DB
  }
});

// Test database connection
export async function testConnection(): Promise<boolean> {
  try {
    logger.info('Attempting to connect to Neon database...');
    logger.info(`Database URL: ${config.database.url?.substring(0, 50)}...`);
    
    const result = await pool.query('SELECT NOW()');
    logger.info(`Database connected at: ${result.rows[0].now}`);
    logger.info('Neon database connection established');
    return true;
  } catch (error: any) {
    logger.error('Database connection failed:', {
      message: error.message,
      code: error.code,
      detail: error.detail
    });
    console.error('Full database error:', error);
    console.error('Database URL:', config.database.url?.substring(0, 50) + '...');
    throw error;
  }
}

// Query helper with automatic client management
export async function query<T = any>(
  text: string,
  params?: any[]
): Promise<{ rows: T[]; rowCount: number }> {
  const start = Date.now();
  try {
    const result = await pool.query(text, params);
    const duration = Date.now() - start;
    logger.debug(`Query executed in ${duration}ms`, { text, params, rowCount: result.rowCount });
    return {
      rows: result.rows,
      rowCount: result.rowCount || 0
    };
  } catch (error) {
    logger.error('Query error:', { text, params, error });
    throw error;
  }
}

// Transaction helper
export async function transaction<T>(
  callback: (client: any) => Promise<T>
): Promise<T> {
  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    const result = await callback(client);
    await client.query('COMMIT');
    return result;
  } catch (error) {
    await client.query('ROLLBACK');
    throw error;
  } finally {
    client.release();
  }
}

// Batch insert helper
export async function batchInsert(
  table: string,
  columns: string[],
  values: any[][]
): Promise<number> {
  if (values.length === 0) return 0;
  
  const placeholders = values.map((_, groupIndex) => 
    `(${columns.map((_, colIndex) => 
      `$${groupIndex * columns.length + colIndex + 1}`
    ).join(', ')})`
  ).join(', ');
  
  const flatValues = values.flat();
  const text = `
    INSERT INTO ${table} (${columns.join(', ')})
    VALUES ${placeholders}
    ON CONFLICT DO NOTHING
  `;
  
  const result = await query(text, flatValues);
  return result.rowCount;
}

// Close pool (for graceful shutdown)
export async function closePool(): Promise<void> {
  await pool.end();
  logger.info('Database pool closed');
}