#!/usr/bin/env ts-node

/**
 * Migration script to hash existing plain text PINs in the database
 * Run this script once to convert all existing PINs to bcrypt hashes
 * 
 * Usage: ts-node scripts/hash-existing-pins.ts
 */

import bcrypt from 'bcrypt';
import { config } from '../src/config';
import { Pool } from 'pg';
import * as dotenv from 'dotenv';

// Load environment variables
dotenv.config();

const pool = new Pool({
  connectionString: config.database.url,
  ssl: process.env.NODE_ENV === 'production' ? { rejectUnauthorized: false } : false
});

async function hashExistingPins() {
  const client = await pool.connect();
  
  try {
    console.log('Starting PIN hashing migration...');
    
    // Begin transaction
    await client.query('BEGIN');
    
    // Get all users with plain text PINs
    // Bcrypt hashes always start with $2b$, $2a$, or $2y$
    const result = await client.query(
      `SELECT id, phone, pin FROM users 
       WHERE pin IS NOT NULL 
       AND pin NOT LIKE '$2%'`
    );
    
    console.log(`Found ${result.rows.length} users with plain text PINs`);
    
    let hashedCount = 0;
    let errorCount = 0;
    
    // Hash each PIN
    for (const user of result.rows) {
      try {
        // Skip if PIN doesn't look like a 6-digit PIN
        if (!/^\d{6}$/.test(user.pin)) {
          console.log(`Skipping user ${user.id} - PIN is not 6 digits: ${user.pin}`);
          continue;
        }
        
        const hashedPin = await bcrypt.hash(user.pin, 10);
        
        await client.query(
          'UPDATE users SET pin = $1, updated_at = NOW() WHERE id = $2',
          [hashedPin, user.id]
        );
        
        hashedCount++;
        console.log(`Hashed PIN for user ${user.id} (phone: ${user.phone})`);
      } catch (error) {
        console.error(`Error hashing PIN for user ${user.id}:`, error);
        errorCount++;
      }
    }
    
    // Commit transaction
    await client.query('COMMIT');
    
    console.log('\nMigration completed:');
    console.log(`- Total users processed: ${result.rows.length}`);
    console.log(`- PINs hashed: ${hashedCount}`);
    console.log(`- Errors: ${errorCount}`);
    
  } catch (error) {
    // Rollback on error
    await client.query('ROLLBACK');
    console.error('Migration failed:', error);
    process.exit(1);
  } finally {
    client.release();
    await pool.end();
  }
}

// Run the migration
hashExistingPins()
  .then(() => {
    console.log('Migration script completed successfully');
    process.exit(0);
  })
  .catch((error) => {
    console.error('Migration script failed:', error);
    process.exit(1);
  });