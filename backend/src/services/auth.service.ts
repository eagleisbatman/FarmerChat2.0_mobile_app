import jwt from 'jsonwebtoken';
import { query, transaction } from '../database';
import { config } from '../config';
import { logger } from '../utils/logger';
import { AppError } from '../middleware/errorHandler';
import { FirebaseService } from './firebase.service';

export interface User {
  id: string;
  email?: string;
  phone?: string;
  name?: string;
  language: string;
  location?: string;
  locationInfo?: any;
  crops: string[];
  livestock: string[];
  preferences: any;
  createdAt: Date;
  updatedAt: Date;
}

export interface AuthToken {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: User;
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
      logger.error('Error verifying Firebase token:', error);
      if (error.code === 'auth/id-token-expired') {
        throw new AppError('Token expired', 401);
      }
      if (error.code === 'auth/invalid-id-token') {
        throw new AppError('Invalid token', 401);
      }
      throw new AppError('Failed to verify token', 500);
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
    userId: string,
    identifier: string
  ): Promise<User> {
    const isEmail = identifier.includes('@');
    const isPhone = identifier.startsWith('+') || /^\d+$/.test(identifier);
    
    return transaction(async (client) => {
      // Check if user exists
      const existingResult = await client.query(
        'SELECT * FROM users WHERE id = $1',
        [userId]
      );
      
      if (existingResult.rows.length > 0) {
        return existingResult.rows[0];
      }
      
      // Create new user
      let query = 'INSERT INTO users (id';
      let values = [userId];
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
    
    const options = {
      expiresIn: config.jwt.expiresIn
    };
    
    return jwt.sign(payload, config.jwt.secret, options);
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
}