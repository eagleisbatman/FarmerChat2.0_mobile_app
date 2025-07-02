# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with the FarmerChat repository.

## 📁 Modular Documentation Structure

For better performance and organization, domain-specific instructions are in their respective directories:

- **[backend/CLAUDE.md](./backend/CLAUDE.md)** - Backend server, API endpoints, Node.js configuration
- **[backend/CLAUDE-database.md](./backend/CLAUDE-database.md)** - Neon PostgreSQL, schema, queries, migrations
- **[app/CLAUDE.md](./app/CLAUDE.md)** - Android app, UI/UX, build commands, testing

## 🚫 CRITICAL DEVELOPMENT RULES

### NO SHORTCUTS POLICY
1. **NEVER take shortcuts** - Always follow the proper process
2. **NEVER create temporary files** - Use existing scripts and infrastructure
3. **NEVER bypass established workflows** - Follow the documented procedures
4. **ALWAYS use existing scripts** - Check the scripts folder before creating new files
5. **ALWAYS run proper translation scripts** - Use the backend translation system for all UI strings
6. **ALWAYS follow the complete build process** - No partial solutions or workarounds
7. **NEVER hardcode translations** - Use backend translation system, not StringsManager.kt

## 🚀 Quick Start

### Backend
```bash
./start-backend.sh  # Automated script - kills port 3004 and starts server
```

### Mobile App
```bash
# Ensure backend is running first!
./gradlew clean assembleDebug && ./gradlew installDebug
```

### Database
```typescript
// Always use Neon MCP tools, never psql
mcp__neon__run_sql({
  projectId: "shiny-hill-62800533",
  sql: "SELECT * FROM users LIMIT 1;"
})
```

## 🏗️ Architecture Overview

### Current Stack
- **Backend**: Node.js + Express + TypeScript (Port 3004)
- **Database**: Neon PostgreSQL (Project: `shiny-hill-62800533`)
- **Mobile**: Android (Kotlin + Jetpack Compose)
- **AI**: OpenAI (gpt-4o-mini) - Only provider enabled
- **Auth**: Phone + PIN authentication → Backend JWT

### Migration Status
✅ Successfully migrated from Firebase to Node.js + Neon PostgreSQL
- **Firebase completely removed except FCM** for push notifications
- **NO Firebase Auth** - using phone + PIN authentication with bcrypt hashing
- **NO Firebase database** - all data in Neon PostgreSQL
- All data operations through REST API
- Real-time updates via WebSocket (Socket.IO)
- Authentication: Phone + PIN with bcrypt (10 salt rounds)
- **Translation system fully operational**: 270 keys × 53 languages = 14,310 translations

## ⚠️ Known Issues

### Translation System Implementation Notes
- Backend translation APIs are fully implemented with all 270 keys in 53 languages
- App has `TranslationManager` that fetches translations on startup
- `StringsManager.getString()` checks API translations first, then falls back to hardcoded values
- All authentication screens now use translation keys instead of hardcoded strings
- **Note**: If translations don't appear, check network connectivity and API response timing

## 📋 Common Tasks

For detailed instructions on specific tasks, refer to the appropriate file:
- Backend development → See [backend/CLAUDE.md](./backend/CLAUDE.md)
- Mobile app changes → See [app/CLAUDE.md](./app/CLAUDE.md)
- Database operations → See [backend/CLAUDE-database.md](./backend/CLAUDE-database.md)

## 🔧 Environment Info
- Working directory: /Users/eagleisbatman/digitalgreen_projects/FarmerChat
- Platform: darwin (macOS)
- Git repo: Yes
- Main branch: main

## 📝 Recent Updates
- June 30, 2025: 
  - **Completed Firebase Auth removal** - now using Phone + PIN with bcrypt hashing
  - **Completed translation system** - 270 keys × 53 languages = 14,310 translations
  - Added 54 new translation keys (authentication, UI labels, error messages)
  - Replaced 60+ hardcoded strings with translation keys
  - Improved `sync-all-string-keys.ts` script with resume capabilities
  - Firebase only retained for FCM push notifications
- June 21, 2025: Fixed navigation loops, implemented dynamic prompts
- January 2025: Completed Node.js + Neon PostgreSQL migration