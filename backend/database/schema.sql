-- FarmerChat Database Schema for Neon PostgreSQL

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table (standalone for Neon PostgreSQL)
CREATE TABLE IF NOT EXISTS users (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    email TEXT,
    phone TEXT,
    name TEXT,
    language TEXT DEFAULT 'en',
    location TEXT,
    location_info JSONB,
    crops TEXT[] DEFAULT '{}',
    livestock TEXT[] DEFAULT '{}',
    preferences JSONB DEFAULT '{}',
    role TEXT,
    gender TEXT,
    voice_enabled BOOLEAN DEFAULT true,
    response_length TEXT DEFAULT 'detailed',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Add missing columns if they don't exist
ALTER TABLE users ADD COLUMN IF NOT EXISTS firebase_uid TEXT UNIQUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS device_id TEXT UNIQUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS pin TEXT;

-- Conversations table
CREATE TABLE IF NOT EXISTS conversations (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    localized_titles JSONB DEFAULT '{}',
    last_message TEXT,
    last_message_time TIMESTAMPTZ,
    last_message_is_user BOOLEAN DEFAULT false,
    tags TEXT[] DEFAULT '{}',
    english_tags TEXT[] DEFAULT '{}',
    summary TEXT,
    is_archived BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Messages table
CREATE TABLE IF NOT EXISTS messages (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    is_user BOOLEAN NOT NULL,
    audio_url TEXT,
    is_voice_message BOOLEAN DEFAULT false,
    follow_up_questions TEXT[] DEFAULT '{}',
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Prompts table for prompt management
CREATE TABLE IF NOT EXISTS prompts (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    description TEXT,
    template TEXT NOT NULL,
    variables JSONB DEFAULT '{}',
    category TEXT,
    is_active BOOLEAN DEFAULT true,
    version INTEGER DEFAULT 1,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Prompt versions for history
CREATE TABLE IF NOT EXISTS prompt_versions (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    prompt_id UUID NOT NULL REFERENCES prompts(id) ON DELETE CASCADE,
    version INTEGER NOT NULL,
    template TEXT NOT NULL,
    variables JSONB DEFAULT '{}',
    changed_by UUID REFERENCES users(id),
    change_notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- UI Translations
CREATE TABLE IF NOT EXISTS ui_translations (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    key TEXT NOT NULL,
    language_code TEXT NOT NULL,
    translation TEXT NOT NULL,
    context TEXT,
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by TEXT,
    UNIQUE(key, language_code)
);

-- Crop Translations
CREATE TABLE IF NOT EXISTS crop_translations (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    crop_id TEXT NOT NULL,
    language_code TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(crop_id, language_code)
);

-- Livestock Translations
CREATE TABLE IF NOT EXISTS livestock_translations (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    livestock_id TEXT NOT NULL,
    language_code TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(livestock_id, language_code)
);

-- Translation Metadata
CREATE TABLE IF NOT EXISTS translation_metadata (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    language_code TEXT UNIQUE NOT NULL,
    language_name TEXT NOT NULL,
    native_name TEXT NOT NULL,
    is_rtl BOOLEAN DEFAULT false,
    is_enabled BOOLEAN DEFAULT true,
    translation_coverage INTEGER DEFAULT 0,
    last_updated TIMESTAMPTZ DEFAULT NOW()
);

-- Feedback table
CREATE TABLE IF NOT EXISTS feedback (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    conversation_id UUID REFERENCES conversations(id) ON DELETE CASCADE,
    message_id UUID REFERENCES messages(id) ON DELETE CASCADE,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    category TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Starter Questions
CREATE TABLE IF NOT EXISTS starter_questions (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    question TEXT NOT NULL,
    language TEXT NOT NULL,
    category TEXT,
    tags TEXT[] DEFAULT '{}',
    priority INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- API Usage Tracking
CREATE TABLE IF NOT EXISTS api_usage (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    provider TEXT NOT NULL,
    model TEXT NOT NULL,
    prompt_tokens INTEGER DEFAULT 0,
    completion_tokens INTEGER DEFAULT 0,
    total_tokens INTEGER DEFAULT 0,
    cost DECIMAL(10, 6),
    endpoint TEXT,
    status_code INTEGER,
    error_message TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Notifications
CREATE TABLE IF NOT EXISTS notifications (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    body TEXT NOT NULL,
    data JSONB DEFAULT '{}',
    type TEXT,
    is_read BOOLEAN DEFAULT false,
    sent_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_conversations_user_id ON conversations(user_id);
CREATE INDEX IF NOT EXISTS idx_conversations_created_at ON conversations(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_conversations_tags ON conversations USING GIN(tags);
CREATE INDEX IF NOT EXISTS idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_messages_created_at ON messages(created_at);
CREATE INDEX IF NOT EXISTS idx_feedback_user_id ON feedback(user_id);
CREATE INDEX IF NOT EXISTS idx_feedback_message_id ON feedback(message_id);
CREATE INDEX IF NOT EXISTS idx_api_usage_user_id ON api_usage(user_id);
CREATE INDEX IF NOT EXISTS idx_api_usage_created_at ON api_usage(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_starter_questions_language ON starter_questions(language);
CREATE INDEX IF NOT EXISTS idx_ui_translations_language ON ui_translations(language_code);

-- Create updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply updated_at triggers
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_conversations_updated_at BEFORE UPDATE ON conversations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_prompts_updated_at BEFORE UPDATE ON prompts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_starter_questions_updated_at BEFORE UPDATE ON starter_questions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Note: Row Level Security (RLS) disabled for Neon PostgreSQL
-- Security is handled at the application level via JWT token validation
-- and user_id filtering in application queries

-- Insert default translation languages
INSERT INTO translation_metadata (language_code, language_name, native_name, is_rtl) VALUES
    ('en', 'English', 'English', false),
    ('hi', 'Hindi', 'हिन्दी', false),
    ('sw', 'Swahili', 'Kiswahili', false),
    ('es', 'Spanish', 'Español', false),
    ('fr', 'French', 'Français', false),
    ('bn', 'Bengali', 'বাংলা', false),
    ('te', 'Telugu', 'తెలుగు', false),
    ('mr', 'Marathi', 'मराठी', false),
    ('ta', 'Tamil', 'தமிழ்', false),
    ('gu', 'Gujarati', 'ગુજરાતી', false),
    ('kn', 'Kannada', 'ಕನ್ನಡ', false),
    ('ar', 'Arabic', 'العربية', true),
    ('he', 'Hebrew', 'עברית', true)
ON CONFLICT (language_code) DO NOTHING;