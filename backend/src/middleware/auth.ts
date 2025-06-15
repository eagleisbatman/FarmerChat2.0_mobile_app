import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import { config } from '../config';
import { AppError } from './errorHandler';
import { query } from '../database';

export interface AuthRequest extends Request {
  userId?: string;
  user?: any;
}

export const authenticate = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
): Promise<void> => {
  try {
    // Get token from header
    const authHeader = req.headers.authorization;
    if (!authHeader) {
      throw new AppError('No authorization header', 401);
    }
    
    const token = authHeader.startsWith('Bearer ') 
      ? authHeader.substring(7) 
      : authHeader;
    
    if (!token) {
      throw new AppError('No token provided', 401);
    }
    
    // Verify JWT token
    try {
      const decoded = jwt.verify(token, config.jwt.secret) as any;
      req.userId = decoded.userId || decoded.sub;
      
      // Optionally fetch user data
      if (req.userId) {
        const result = await query(
          'SELECT * FROM users WHERE id = $1',
          [req.userId]
        );
        
        if (result.rows.length === 0) {
          throw new AppError('User not found', 401);
        }
        
        req.user = result.rows[0];
      }
      
      next();
    } catch (error) {
      if (error instanceof jwt.JsonWebTokenError) {
        throw new AppError('Invalid token', 401);
      } else if (error instanceof jwt.TokenExpiredError) {
        throw new AppError('Token expired', 401);
      }
      throw error;
    }
  } catch (error) {
    next(error);
  }
};

// Optional authentication (doesn't fail if no token)
export const optionalAuth = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
): Promise<void> => {
  try {
    const authHeader = req.headers.authorization;
    if (authHeader) {
      const token = authHeader.startsWith('Bearer ') 
        ? authHeader.substring(7) 
        : authHeader;
      
      if (token) {
        try {
          const decoded = jwt.verify(token, config.jwt.secret) as any;
          req.userId = decoded.userId || decoded.sub;
          
          // Fetch user data if userId exists
          if (req.userId) {
            const result = await query(
              'SELECT * FROM users WHERE id = $1',
              [req.userId]
            );
            
            if (result.rows.length > 0) {
              req.user = result.rows[0];
            }
          }
        } catch (error) {
          // Ignore token errors for optional auth
        }
      }
    }
    next();
  } catch (error) {
    next(error);
  }
};