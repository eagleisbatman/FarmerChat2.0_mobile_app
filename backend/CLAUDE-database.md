# CLAUDE-database.md

This file provides database-specific guidance to Claude Code for the FarmerChat Neon PostgreSQL database.

## 🗄️ Database Configuration

### Active Database
- **Provider**: Neon PostgreSQL (Serverless)
- **Project ID**: `shiny-hill-62800533`
- **Project Name**: FarmerChat2.0-Secure
- **Region**: azure-eastus2
- **PostgreSQL Version**: 17
- **Default Database**: `neondb`

## 🔧 Neon MCP Usage Rules

### CRITICAL: Always Use Neon MCP Tools
```typescript
// ✅ CORRECT - Use Neon MCP
mcp__neon__run_sql({
  projectId: "shiny-hill-62800533",
  sql: "SELECT * FROM ui_translations WHERE key = 'YOU';"
})

// ❌ WRONG - Never use psql directly
// psql $DATABASE_URL -c "SELECT * FROM ui_translations"
```

### Available Neon MCP Tools
1. `mcp__neon__run_sql` - Execute SQL queries
2. `mcp__neon__describe_table_schema` - Get table structure
3. `mcp__neon__get_database_tables` - List all tables
4. `mcp__neon__list_projects` - Verify project access
5. `mcp__neon__prepare_database_migration` - Schema changes
6. `mcp__neon__complete_database_migration` - Apply migrations

## 📊 Database Schema

### Core Tables

#### Users Table
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    firebase_uid VARCHAR(255) UNIQUE,
    phone VARCHAR(20),
    name VARCHAR(255),
    role VARCHAR(50),
    language VARCHAR(10) DEFAULT 'en',
    location JSONB,
    selected_crops INTEGER[],
    selected_livestock INTEGER[],
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
```

#### Conversations Table
```sql
CREATE TABLE conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(500),
    last_message TEXT,
    last_message_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
```

#### Messages Table
```sql
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID REFERENCES conversations(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    is_from_user BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
```

### Translation Tables (NEVER DELETE/TRUNCATE!)

#### UI Translations (10,335+ entries)
```sql
CREATE TABLE ui_translations (
    id SERIAL PRIMARY KEY,
    key VARCHAR(255) NOT NULL,
    language VARCHAR(10) NOT NULL,
    value TEXT NOT NULL,
    UNIQUE(key, language)
);
```

#### Crop Translations (2,915+ entries)
```sql
CREATE TABLE crop_translations (
    id INTEGER NOT NULL,
    language VARCHAR(10) NOT NULL,
    crop_name VARCHAR(500) NOT NULL,
    PRIMARY KEY (id, language)
);
```

#### Livestock Translations (1,080+ entries)
```sql
CREATE TABLE livestock_translations (
    id INTEGER NOT NULL,
    language VARCHAR(10) NOT NULL,
    livestock_name VARCHAR(500) NOT NULL,
    PRIMARY KEY (id, language)
);
```

## 🚨 Critical Database Rules

### NEVER Delete Translation Data
```sql
-- ❌ NEVER DO THIS
TRUNCATE ui_translations;
TRUNCATE crop_translations;
TRUNCATE livestock_translations;
TRUNCATE translation_metadata;

-- ✅ Only clear user-specific data when needed
DELETE FROM messages WHERE user_id = 'specific_user_id';
DELETE FROM conversations WHERE user_id = 'specific_user_id';
DELETE FROM users WHERE id = 'specific_user_id';
```

### Safe Data Operations
```sql
-- View translation counts (safe)
SELECT language, COUNT(*) FROM ui_translations GROUP BY language;

-- Check specific translations (safe)
SELECT * FROM ui_translations WHERE key = 'CONTINUE_CONVERSATION';

-- Update user data (safe)
UPDATE users SET language = 'hi' WHERE id = 'user_id';

-- Add new translations (safe)
INSERT INTO ui_translations (key, language, value) 
VALUES ('NEW_KEY', 'en', 'New Value')
ON CONFLICT (key, language) DO UPDATE SET value = EXCLUDED.value;
```

## 📋 Common Database Queries

### User Management
```sql
-- Get user by Firebase UID
SELECT * FROM users WHERE firebase_uid = 'firebase_uid_here';

-- Update user profile
UPDATE users 
SET name = 'New Name', 
    location = '{"country": "IN", "state": "Karnataka"}'::jsonb,
    updated_at = NOW()
WHERE id = 'user_id';

-- Get user's selected crops with translations
SELECT ct.* 
FROM crop_translations ct
WHERE ct.id = ANY(
    SELECT unnest(selected_crops) 
    FROM users 
    WHERE id = 'user_id'
) AND ct.language = 'en';
```

### Conversation Queries
```sql
-- Get user's conversations
SELECT c.*, COUNT(m.id) as message_count
FROM conversations c
LEFT JOIN messages m ON m.conversation_id = c.id
WHERE c.user_id = 'user_id'
GROUP BY c.id
ORDER BY c.last_message_at DESC;

-- Get conversation messages
SELECT * FROM messages 
WHERE conversation_id = 'conversation_id'
ORDER BY created_at ASC;
```

### Translation Queries
```sql
-- Get all UI translations for a language
SELECT key, value FROM ui_translations WHERE language = 'hi';

-- Search crops across all languages
SELECT DISTINCT id, crop_name, language 
FROM crop_translations 
WHERE LOWER(crop_name) LIKE '%rice%';

-- Get missing translations
SELECT DISTINCT u1.key 
FROM ui_translations u1 
WHERE NOT EXISTS (
    SELECT 1 FROM ui_translations u2 
    WHERE u2.key = u1.key AND u2.language = 'sw'
);
```

## 🔄 Migration Best Practices

### Using Neon Migration Tools
```typescript
// Step 1: Prepare migration
mcp__neon__prepare_database_migration({
  projectId: "shiny-hill-62800533",
  migrationSql: `
    ALTER TABLE users 
    ADD COLUMN preferences JSONB DEFAULT '{}'::jsonb;
  `
});

// Step 2: Test on temporary branch
mcp__neon__run_sql({
  projectId: "shiny-hill-62800533",
  branchId: "temp-branch-id",
  sql: "SELECT * FROM users LIMIT 1;"
});

// Step 3: Apply migration
mcp__neon__complete_database_migration({
  migrationId: "migration-id-from-step-1"
});
```

## 📊 Database Monitoring

### Check Table Sizes
```sql
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

### Active Connections
```sql
SELECT count(*) FROM pg_stat_activity;
```

### Slow Queries
```sql
SELECT 
    query,
    mean_exec_time,
    calls
FROM pg_stat_statements
WHERE mean_exec_time > 1000  -- queries taking > 1 second
ORDER BY mean_exec_time DESC
LIMIT 10;
```

## 🔐 Security Notes

- **Connection strings** managed by Neon MCP - never expose
- **Row-level security** not enabled (handled at API level)
- **SSL required** for all connections
- **Backup automatically** handled by Neon
- **Point-in-time recovery** available through Neon console

## ⚠️ Important Warnings

1. **Translation Tables**: Contains production data for 50+ languages
   - NEVER truncate or delete in bulk
   - Only INSERT or UPDATE operations allowed

2. **User Data**: Always cascade deletes properly
   - Deleting a user should cascade to their conversations/messages

3. **Performance**: Use indexes for frequently queried columns
   ```sql
   CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);
   CREATE INDEX idx_conversations_user_id ON conversations(user_id);
   ```

4. **Migrations**: Always test on a branch first using Neon's branching feature