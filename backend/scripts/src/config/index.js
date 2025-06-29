"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.config = void 0;
const dotenv_1 = require("dotenv");
// Load environment variables
(0, dotenv_1.config)();
// Validate required environment variables
const requiredEnvVars = [
    'DATABASE_URL',
    'JWT_SECRET'
];
for (const envVar of requiredEnvVars) {
    if (!process.env[envVar]) {
        throw new Error(`Missing required environment variable: ${envVar}`);
    }
}
exports.config = {
    server: {
        env: process.env.NODE_ENV || 'development',
        port: parseInt(process.env.PORT || '3000', 10),
        apiVersion: process.env.API_VERSION || 'v1',
        maxFileSize: process.env.MAX_FILE_SIZE || '10mb'
    },
    database: {
        url: process.env.DATABASE_URL,
        neonProjectId: process.env.NEON_PROJECT_ID || '',
        neonBranchId: process.env.NEON_BRANCH_ID || ''
    },
    firebase: {
        projectId: process.env.FIREBASE_PROJECT_ID || '',
        privateKey: process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, '\n') || '',
        clientEmail: process.env.FIREBASE_CLIENT_EMAIL || '',
        databaseURL: process.env.FIREBASE_DATABASE_URL || '',
        // Web app config for client-side
        apiKey: process.env.FIREBASE_API_KEY || '',
        authDomain: process.env.FIREBASE_AUTH_DOMAIN || '',
        messagingSenderId: process.env.FIREBASE_MESSAGING_SENDER_ID || '',
        appId: process.env.FIREBASE_APP_ID || ''
    },
    ai: {
        defaultProvider: process.env.DEFAULT_AI_PROVIDER || 'gemini',
        providers: {
            gemini: {
                apiKey: process.env.GEMINI_API_KEY || '',
                model: process.env.GEMINI_MODEL || 'gemini-1.5-flash'
            },
            openai: {
                apiKey: process.env.OPENAI_API_KEY || '',
                model: process.env.OPENAI_MODEL || 'gpt-4o-mini'
            },
            anthropic: {
                apiKey: process.env.ANTHROPIC_API_KEY || '',
                model: process.env.ANTHROPIC_MODEL || 'claude-3-haiku-20240307'
            }
        }
    },
    redis: {
        url: process.env.REDIS_URL || '',
        password: process.env.REDIS_PASSWORD,
        db: parseInt(process.env.REDIS_DB || '0', 10)
    },
    jwt: {
        secret: process.env.JWT_SECRET,
        expiresIn: process.env.JWT_EXPIRES_IN || '7d'
    },
    translation: {
        cacheTTL: parseInt(process.env.TRANSLATION_CACHE_TTL || '3600', 10),
        defaultLanguage: process.env.TRANSLATION_DEFAULT_LANGUAGE || 'en',
        fallbackEnabled: process.env.TRANSLATION_FALLBACK_ENABLED === 'true',
        supportedLanguages: (process.env.SUPPORTED_LANGUAGES || 'en,hi,sw,es,fr,bn,te,mr,ta,gu,kn').split(',')
    },
    rateLimit: {
        api: parseInt(process.env.API_RATE_LIMIT || '1000', 10),
        translation: parseInt(process.env.TRANSLATION_API_RATE_LIMIT || '5000', 10)
    },
    cors: {
        origin: (process.env.CORS_ORIGIN || 'http://localhost:3000').split(','),
        credentials: process.env.CORS_CREDENTIALS === 'true'
    },
    logging: {
        level: process.env.LOG_LEVEL || 'debug',
        format: process.env.LOG_FORMAT || 'combined'
    },
    websocket: {
        enabled: process.env.WEBSOCKET_ENABLED === 'true',
        corsOrigin: process.env.WEBSOCKET_CORS_ORIGIN || '*'
    },
    admin: {
        apiKey: process.env.ADMIN_API_KEY || '',
        emails: (process.env.ADMIN_EMAILS || '').split(',').filter(Boolean)
    },
    monitoring: {
        sentryDsn: process.env.SENTRY_DSN || '',
        enableMetrics: process.env.ENABLE_METRICS === 'true'
    },
    upload: {
        allowedTypes: (process.env.ALLOWED_FILE_TYPES || 'image/jpeg,image/png,image/webp,audio/wav,audio/mp3').split(',')
    }
};
// Validate AI provider configuration
const provider = exports.config.ai.defaultProvider;
if (!['gemini', 'openai', 'anthropic'].includes(provider)) {
    throw new Error(`Invalid DEFAULT_AI_PROVIDER: ${provider}`);
}
const providerConfig = exports.config.ai.providers[provider];
if (!providerConfig.apiKey) {
    throw new Error(`Missing API key for default AI provider: ${provider}`);
}
