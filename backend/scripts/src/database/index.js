"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __exportStar = (this && this.__exportStar) || function(m, exports) {
    for (var p in m) if (p !== "default" && !Object.prototype.hasOwnProperty.call(exports, p)) __createBinding(exports, m, p);
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.initializeDatabase = initializeDatabase;
const logger_1 = require("../utils/logger");
const db_1 = require("./db");
// Database initialization
async function initializeDatabase() {
    try {
        // Test Neon database connection
        const connected = await (0, db_1.testConnection)();
        if (!connected) {
            throw new Error('Failed to connect to Neon database');
        }
        logger_1.logger.info('Neon database connection established');
        // Verify schema
        await verifySchema();
    }
    catch (error) {
        logger_1.logger.error('Database initialization failed:', error);
        throw error;
    }
}
// Verify database schema
async function verifySchema() {
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
            const result = await db_1.pool.query(`SELECT EXISTS (
          SELECT FROM information_schema.tables 
          WHERE table_schema = 'public' 
          AND table_name = $1
        )`, [table]);
            if (!result.rows[0].exists) {
                logger_1.logger.warn(`Table '${table}' does not exist`);
            }
        }
        logger_1.logger.info('Database schema verified');
    }
    catch (error) {
        logger_1.logger.error('Schema verification failed:', error);
        throw error;
    }
}
// Export database utilities
__exportStar(require("./db"), exports);
