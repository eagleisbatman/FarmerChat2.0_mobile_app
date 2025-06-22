import { Pool } from 'pg';
import * as dotenv from 'dotenv';
import { join } from 'path';

// Load environment variables
dotenv.config({ path: join(__dirname, '..', '.env') });

const clearUserData = async () => {
    const pool = new Pool({
        connectionString: process.env.DATABASE_URL,
        ssl: {
            rejectUnauthorized: false
        }
    });

    try {
        console.log('🗑️  Starting to clear user data...');
        
        // Start transaction
        await pool.query('BEGIN');
        
        // Clear data in order (respecting foreign keys)
        const messagesResult = await pool.query('DELETE FROM messages');
        console.log(`✅ Cleared ${messagesResult.rowCount} messages`);
        
        const conversationsResult = await pool.query('DELETE FROM conversations');
        console.log(`✅ Cleared ${conversationsResult.rowCount} conversations`);
        
        const usersResult = await pool.query('DELETE FROM users');
        console.log(`✅ Cleared ${usersResult.rowCount} users`);
        
        // Commit transaction
        await pool.query('COMMIT');
        console.log('✅ Transaction committed successfully');
        
        console.log('\n✅ All user data has been cleared!');
        console.log('📱 You can now start fresh with the app.');
        
    } catch (error) {
        await pool.query('ROLLBACK');
        console.error('❌ Error clearing user data:', error);
        console.log('🔄 Transaction rolled back');
    } finally {
        await pool.end();
    }
};

// Run immediately
clearUserData();