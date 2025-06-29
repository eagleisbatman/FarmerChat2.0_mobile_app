import jwt from 'jsonwebtoken';
import { query, transaction } from '../database';
import { config } from '../config';
import { logger } from '../utils/logger';
import { AppError } from '../middleware/errorHandler';
import { FirebaseService } from './firebase.service';

export interface User {
  id: string;
  firebase_uid?: string; // Made optional for non-Firebase auth
  device_id?: string;    // New field for device-based auth
  email?: string;
  phone?: string;
  name?: string;
  language: string;
  location?: string;
  locationInfo?: any;
  crops: string[];
  livestock: string[];
  preferences: any;
  role?: string;
  gender?: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface AuthToken {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: User;
  profileComplete?: boolean;
}

export class AuthService {
  private firebase: FirebaseService;
  
  constructor() {
    this.firebase = FirebaseService.getInstance();
  }
  
  // Note: Firebase Auth phone OTP is handled client-side
  // This endpoint returns Firebase config for the client
  async getAuthConfig(): Promise<{ firebaseConfig: any; message: string }> {
    return {
      firebaseConfig: this.firebase.getClientConfig(),
      message: 'Use Firebase Auth on client side for phone OTP'
    };
  }
  
  // Verify Firebase ID token from client
  async verifyFirebaseToken(idToken: string): Promise<AuthToken> {
    try {
      // Verify the ID token from Firebase
      const decodedToken = await this.firebase.verifyIdToken(idToken);
      
      if (!decodedToken.uid) {
        throw new AppError('Invalid token', 400);
      }
      
      // Extract phone number or email
      const phoneNumber = decodedToken.phone_number;
      const email = decodedToken.email;
      const identifier = phoneNumber || email || decodedToken.uid;
      
      // Get or create user in our database
      const user = await this.getOrCreateUser(decodedToken.uid, identifier);
      
      // Generate JWT tokens for our backend
      const accessToken = this.generateAccessToken(user);
      const refreshToken = this.generateRefreshToken(user);
      
      return {
        accessToken,
        refreshToken,
        expiresIn: 7 * 24 * 60 * 60, // 7 days in seconds
        user
      };
    } catch (error: any) {
      logger.error('Error verifying Firebase token:', {
        message: error.message,
        code: error.code,
        stack: error.stack,
        fullError: JSON.stringify(error, null, 2)
      });
      if (error.code === 'auth/id-token-expired') {
        throw new AppError('Token expired', 401);
      }
      if (error.code === 'auth/invalid-id-token') {
        throw new AppError('Invalid token', 401);
      }
      if (error.code === 'auth/argument-error') {
        throw new AppError('Invalid token format', 401);
      }
      throw new AppError(`Failed to verify token: ${error.message || 'Unknown error'}`, 500);
    }
  }
  
  async refreshToken(refreshToken: string): Promise<AuthToken> {
    try {
      const decoded = jwt.verify(refreshToken, config.jwt.secret) as any;
      
      if (decoded.type !== 'refresh') {
        throw new AppError('Invalid token type', 401);
      }
      
      // Get user from database
      const result = await query<User>(
        'SELECT * FROM users WHERE id = $1',
        [decoded.userId]
      );
      
      if (result.rows.length === 0) {
        throw new AppError('User not found', 404);
      }
      
      const user = result.rows[0];
      
      // Generate new tokens
      const accessToken = this.generateAccessToken(user);
      const newRefreshToken = this.generateRefreshToken(user);
      
      return {
        accessToken,
        refreshToken: newRefreshToken,
        expiresIn: 7 * 24 * 60 * 60,
        user
      };
    } catch (error) {
      if (error instanceof jwt.JsonWebTokenError) {
        throw new AppError('Invalid refresh token', 401);
      }
      throw error;
    }
  }
  
  private async getOrCreateUser(
    firebaseUid: string,
    identifier: string
  ): Promise<User> {
    const isEmail = identifier.includes('@');
    const isPhone = identifier.startsWith('+') || /^\d+$/.test(identifier);
    
    return transaction(async (client) => {
      // Check if user exists by Firebase UID
      const existingResult = await client.query(
        'SELECT * FROM users WHERE firebase_uid = $1',
        [firebaseUid]
      );
      
      if (existingResult.rows.length > 0) {
        return existingResult.rows[0];
      }
      
      // Create new user with a proper UUID
      let query = 'INSERT INTO users (firebase_uid';
      let values = [firebaseUid];
      let paramCount = 1;
      
      if (isEmail) {
        query += ', email';
        values.push(identifier);
        paramCount++;
      } else if (isPhone) {
        query += ', phone';
        values.push(identifier);
        paramCount++;
      }
      
      query += `) VALUES ($1${paramCount > 1 ? ', $2' : ''}) RETURNING *`;
      
      const newUserResult = await client.query(query, values);
      
      return newUserResult.rows[0];
    });
  }
  
  private generateAccessToken(user: User): string {
    const payload = {
      userId: user.id,
      email: user.email,
      phone: user.phone,
      type: 'access'
    };
    
    return jwt.sign(payload, config.jwt.secret, {
      expiresIn: config.jwt.expiresIn as any
    });
  }
  
  private generateRefreshToken(user: User): string {
    return jwt.sign(
      {
        userId: user.id,
        type: 'refresh'
      },
      config.jwt.secret,
      {
        expiresIn: '30d'
      }
    );
  }
  
  async signOut(userId: string): Promise<void> {
    // If using Supabase Auth, we could invalidate the session
    // For JWT, we rely on token expiration
    logger.info(`User ${userId} signed out`);
  }

  // New device-based authentication method
  async authenticateDevice(deviceId: string, deviceInfo?: any): Promise<AuthToken> {
    try {
      logger.info(`Authenticating device: ${deviceId}`);
      
      // Check if user exists with this device ID
      const existingResult = await query<User>(
        'SELECT * FROM users WHERE device_id = $1',
        [deviceId]
      );
      
      let user: User;
      
      if (existingResult.rows.length > 0) {
        // User exists, return their data
        user = existingResult.rows[0];
        logger.info(`Found existing user for device: ${deviceId}, user ID: ${user.id}`);
      } else {
        // Create new user for this device
        const createResult = await query<User>(
          `INSERT INTO users (device_id, language, crops, livestock, preferences, created_at, updated_at)
           VALUES ($1, $2, $3, $4, $5, NOW(), NOW())
           RETURNING *`,
          [deviceId, 'en', [], [], {}]
        );
        
        user = createResult.rows[0];
        logger.info(`Created new user for device: ${deviceId}, user ID: ${user.id}`);
      }
      
      // Generate tokens
      const accessToken = this.generateAccessToken(user);
      const refreshToken = this.generateRefreshToken(user);
      
      return {
        accessToken,
        refreshToken,
        expiresIn: 7 * 24 * 60 * 60, // 7 days
        user
      };
    } catch (error) {
      logger.error('Device authentication error:', error);
      throw new AppError('Failed to authenticate device', 500);
    }
  }

  // Login with phone and PIN
  async loginWithPhone(phone: string, pin: string): Promise<AuthToken> {
    try {
      logger.info(`Login attempt for phone: ${phone}`);
      
      // Normalize phone number - handle multiple country codes
      const phoneVariants = [phone];
      
      // If phone doesn't start with +, reject it
      if (!phone.startsWith('+')) {
        throw new AppError('Phone number must include country code (e.g., +91, +1, +44)', 400);
      }
      
      // Also try without the + for backward compatibility
      phoneVariants.push(phone.substring(1));
      
      // Get user by phone (try all variants)
      const userResult = await query<User & { pin: string }>(
        'SELECT * FROM users WHERE phone = ANY($1::text[])',
        [phoneVariants]
      );
      
      if (userResult.rows.length === 0) {
        throw new AppError('Invalid phone number or PIN', 401);
      }
      
      const user = userResult.rows[0];
      
      // Check if user has a PIN set
      if (!user.pin) {
        throw new AppError('No PIN set for this account. Please complete registration.', 403);
      }
      
      // For now, we're storing PIN in plain text (in production, use bcrypt)
      if (user.pin !== pin) {
        throw new AppError('Invalid phone number or PIN', 401);
      }
      
      // Check if profile is complete
      const profileComplete = this.isProfileComplete(user);
      
      // Generate tokens
      const accessToken = this.generateAccessToken(user);
      const refreshToken = this.generateRefreshToken(user);
      
      // Remove PIN from user object before returning
      const { pin: _, ...userWithoutPin } = user;
      
      return {
        accessToken,
        refreshToken,
        expiresIn: 7 * 24 * 60 * 60, // 7 days
        user: userWithoutPin as User,
        profileComplete
      };
    } catch (error) {
      if (error instanceof AppError) throw error;
      logger.error('Login error:', error);
      throw new AppError('Login failed', 500);
    }
  }
  
  // Register with phone and PIN
  async registerWithPhone(phone: string, pin: string, firebaseIdToken?: string): Promise<AuthToken> {
    try {
      logger.info(`Registration for phone: ${phone}`);
      
      // Normalize phone number - handle multiple country codes
      const phoneVariants = [phone];
      
      // If phone doesn't start with +, reject it
      if (!phone.startsWith('+')) {
        throw new AppError('Phone number must include country code (e.g., +91, +1, +44)', 400);
      }
      
      // Also try without the + for backward compatibility
      phoneVariants.push(phone.substring(1));
      
      // Check if phone already exists
      const existingResult = await query<User & { pin: string }>(
        'SELECT * FROM users WHERE phone = ANY($1::text[])',
        [phoneVariants]
      );
      
      if (existingResult.rows.length > 0) {
        const existingUser = existingResult.rows[0];
        
        // If user exists but has no PIN, allow them to set one
        if (!existingUser.pin) {
          logger.info(`User exists without PIN, allowing PIN setup for phone: ${phone}`);
          
          // Update the user's PIN
          const updateResult = await query<User>(
            `UPDATE users SET pin = $1, updated_at = NOW() 
             WHERE id = $2 
             RETURNING id, phone, firebase_uid, email, name, language, location, location_info, crops, livestock, preferences, role, gender, created_at, updated_at`,
            [pin, existingUser.id]
          );
          
          const user = updateResult.rows[0];
          
          // Generate tokens
          const accessToken = this.generateAccessToken(user);
          const refreshToken = this.generateRefreshToken(user);
          
          return {
            accessToken,
            refreshToken,
            expiresIn: 7 * 24 * 60 * 60, // 7 days
            user,
            profileComplete: this.isProfileComplete(user)
          };
        }
        
        // If user has a PIN, they should login instead
        throw new AppError('Phone number already registered. Please login.', 409);
      }
      
      // Verify Firebase token if provided
      let firebaseUid: string | undefined;
      if (firebaseIdToken) {
        try {
          const decodedToken = await this.firebase.verifyIdToken(firebaseIdToken);
          firebaseUid = decodedToken.uid;
        } catch (error) {
          logger.warn('Firebase token verification failed during registration', error);
        }
      }
      
      // Create new user with PIN
      const createResult = await query<User>(
        `INSERT INTO users (phone, pin, firebase_uid, language, crops, livestock, preferences, created_at, updated_at)
         VALUES ($1, $2, $3, $4, $5, $6, $7, NOW(), NOW())
         RETURNING id, phone, firebase_uid, email, name, language, location, location_info, crops, livestock, preferences, role, gender, created_at, updated_at`,
        [phone, pin, firebaseUid || null, 'en', [], [], {}]
      );
      
      const user = createResult.rows[0];
      logger.info(`Created new user with phone: ${phone}, user ID: ${user.id}`);
      
      // Generate tokens
      const accessToken = this.generateAccessToken(user);
      const refreshToken = this.generateRefreshToken(user);
      
      return {
        accessToken,
        refreshToken,
        expiresIn: 7 * 24 * 60 * 60, // 7 days
        user,
        profileComplete: false // New users need onboarding
      };
    } catch (error) {
      if (error instanceof AppError) throw error;
      logger.error('Registration error:', error);
      throw new AppError('Registration failed', 500);
    }
  }
  
  // Check if user profile is complete
  private isProfileComplete(user: User): boolean {
    return !!(
      user.name &&
      user.location &&
      user.role &&
      user.language !== 'en' &&
      (user.crops.length > 0 || user.livestock.length > 0)
    );
  }
  
  // Phone OTP methods for future implementation
  async requestPhoneOTP(phoneNumber: string, userId: string): Promise<{ success: boolean; message: string }> {
    // TODO: Implement actual OTP sending via SMS service
    logger.info(`OTP requested for phone: ${phoneNumber}, user: ${userId}`);
    
    // For now, just log the OTP (in production, send via SMS)
    const otp = Math.floor(100000 + Math.random() * 900000).toString();
    logger.info(`Generated OTP: ${otp} for phone: ${phoneNumber}`);
    
    // Store OTP in database or cache with expiration
    
    return {
      success: true,
      message: 'OTP sent successfully'
    };
  }

  async verifyPhoneOTP(phoneNumber: string, otp: string, userId: string): Promise<{ success: boolean; verified: boolean }> {
    // TODO: Implement actual OTP verification
    logger.info(`Verifying OTP for phone: ${phoneNumber}, user: ${userId}`);
    
    // For development, accept any 6-digit OTP
    const isValid = /^\d{6}$/.test(otp);
    
    if (isValid) {
      // Update user's phone number
      await query(
        'UPDATE users SET phone = $1, updated_at = NOW() WHERE id = $2',
        [phoneNumber, userId]
      );
    }
    
    return {
      success: true,
      verified: isValid
    };
  }
}