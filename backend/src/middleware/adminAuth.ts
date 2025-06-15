import { Request, Response, NextFunction } from 'express';
import { config } from '../config';
import { AppError } from './errorHandler';
import { AuthRequest } from './auth';

export const adminAuth = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
): Promise<void> => {
  try {
    // Check for admin API key
    const apiKey = req.headers['x-admin-api-key'] as string;
    
    if (apiKey && apiKey === config.admin.apiKey) {
      req.userId = 'admin';
      return next();
    }
    
    // Check if user is authenticated and is admin
    if (req.user && config.admin.emails.includes(req.user.email)) {
      return next();
    }
    
    throw new AppError('Admin access required', 403);
  } catch (error) {
    next(error);
  }
};