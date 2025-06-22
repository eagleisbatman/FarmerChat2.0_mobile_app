-- Clear User Data Script for FarmerChat
-- This script clears only user-generated data while preserving system data

-- Start transaction for safety
BEGIN;

-- Clear messages first (has foreign key to conversations)
DELETE FROM messages;

-- Clear conversations (has foreign key to users)
DELETE FROM conversations;

-- Clear users
DELETE FROM users;

-- Clear any user-specific starter questions if they exist
DELETE FROM starter_questions WHERE user_id IS NOT NULL;

-- Reset sequences to start IDs from 1 again (optional)
-- ALTER SEQUENCE users_id_seq RESTART WITH 1;
-- ALTER SEQUENCE conversations_id_seq RESTART WITH 1;
-- ALTER SEQUENCE messages_id_seq RESTART WITH 1;

-- Commit the transaction
COMMIT;

-- Show counts to verify
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
SELECT 'Starter Questions', COUNT(*) FROM starter_questions;