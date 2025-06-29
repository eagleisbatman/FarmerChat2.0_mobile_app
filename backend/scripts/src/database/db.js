"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.pool = void 0;
exports.testConnection = testConnection;
exports.query = query;
exports.transaction = transaction;
exports.batchInsert = batchInsert;
exports.closePool = closePool;
const pg_1 = require("pg");
const config_1 = require("../config");
const logger_1 = require("../utils/logger");
// Create connection pool
exports.pool = new pg_1.Pool({
    connectionString: config_1.config.database.url,
    max: 20, // Maximum number of clients in the pool
    idleTimeoutMillis: 30000, // How long a client is allowed to remain idle before being closed
    connectionTimeoutMillis: 10000, // Increased timeout to 10 seconds for initial connection
    ssl: {
        rejectUnauthorized: false // Required for Neon DB
    }
});
// Test database connection
async function testConnection() {
    try {
        logger_1.logger.info('Attempting to connect to Neon database...');
        logger_1.logger.info(`Database URL: ${config_1.config.database.url?.substring(0, 50)}...`);
        const result = await exports.pool.query('SELECT NOW()');
        logger_1.logger.info(`Database connected at: ${result.rows[0].now}`);
        logger_1.logger.info('Neon database connection established');
        return true;
    }
    catch (error) {
        logger_1.logger.error('Database connection failed:', {
            message: error.message,
            code: error.code,
            detail: error.detail
        });
        console.error('Full database error:', error);
        console.error('Database URL:', config_1.config.database.url?.substring(0, 50) + '...');
        throw error;
    }
}
// Query helper with automatic client management
async function query(text, params) {
    const start = Date.now();
    try {
        const result = await exports.pool.query(text, params);
        const duration = Date.now() - start;
        logger_1.logger.debug(`Query executed in ${duration}ms`, { text, params, rowCount: result.rowCount });
        return {
            rows: result.rows,
            rowCount: result.rowCount || 0
        };
    }
    catch (error) {
        logger_1.logger.error('Query error:', { text, params, error });
        throw error;
    }
}
// Transaction helper
async function transaction(callback) {
    const client = await exports.pool.connect();
    try {
        await client.query('BEGIN');
        const result = await callback(client);
        await client.query('COMMIT');
        return result;
    }
    catch (error) {
        await client.query('ROLLBACK');
        throw error;
    }
    finally {
        client.release();
    }
}
// Batch insert helper
async function batchInsert(table, columns, values) {
    if (values.length === 0)
        return 0;
    const placeholders = values.map((_, groupIndex) => `(${columns.map((_, colIndex) => `$${groupIndex * columns.length + colIndex + 1}`).join(', ')})`).join(', ');
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
async function closePool() {
    await exports.pool.end();
    logger_1.logger.info('Database pool closed');
}
