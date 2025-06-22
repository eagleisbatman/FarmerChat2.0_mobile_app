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
        
        // Note: starter_questions table doesn't have user_id column, so we skip it
        
        // Commit transaction
        await pool.query('COMMIT');
        console.log('✅ Transaction committed successfully');
        
        // Show remaining data counts
        console.log('\n📊 Remaining data in database:');
        const counts = await pool.query(`
            SELECT 'Users' as table_name, COUNT(*) as count FROM users
            UNION ALL
            SELECT 'Conversations', COUNT(*) FROM conversations
            UNION ALL
            SELECT 'Messages', COUNT(*) FROM messages
            UNION ALL
            SELECT 'Translations', COUNT(*) FROM translations
            UNION ALL
            SELECT 'Prompts', COUNT(*) FROM prompts
            UNION ALL
            SELECT 'Starter Questions', COUNT(*) FROM starter_questions
        `);
        
        counts.rows.forEach(row => {
            console.log(`   ${row.table_name}: ${row.count}`);
        });
        
        console.log('\n✅ User data cleared successfully!');
        console.log('ℹ️  System data (translations, prompts) has been preserved.');
        
    } catch (error) {
        await pool.query('ROLLBACK');
        console.error('❌ Error clearing user data:', error);
        console.log('🔄 Transaction rolled back');
    } finally {
        await pool.end();
    }
};

// Add confirmation prompt
const readline = require('readline');
const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

console.log('⚠️  WARNING: This will delete all user data from the database!');
console.log('This includes: users, conversations, messages, and user-specific data.');
console.log('System data (translations, prompts) will be preserved.\n');

rl.question('Are you sure you want to continue? (yes/no): ', (answer: string) => {
    if (answer.toLowerCase() === 'yes') {
        clearUserData().then(() => {
            rl.close();
            process.exit(0);
        }).catch((error) => {
            console.error('Failed to clear user data:', error);
            rl.close();
            process.exit(1);
        });
    } else {
        console.log('❌ Operation cancelled');
        rl.close();
        process.exit(0);
    }
});