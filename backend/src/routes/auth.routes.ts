import { Router, Request, Response, NextFunction } from 'express';
import { AuthService } from '../services/auth.service';
import { AppError } from '../middleware/errorHandler';
import { logger } from '../utils/logger';

const router = Router();
const authService = new AuthService();

// Get Firebase auth config
router.get('/config', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const result = await authService.getAuthConfig();
    res.json(result);
  } catch (error) {
    next(error);
  }
});

// Verify Firebase ID token
router.post('/verify', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { idToken, userInfo } = req.body;
    
    if (!idToken) {
      throw new AppError('ID token is required', 400);
    }
    
    const result = await authService.verifyFirebaseToken(idToken);
    
    res.json(result);
  } catch (error) {
    next(error);
  }
});

// Refresh token
router.post('/refresh', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { refreshToken } = req.body;
    
    if (!refreshToken) {
      throw new AppError('Refresh token is required', 400);
    }
    
    const result = await authService.refreshToken(refreshToken);
    
    res.json(result);
  } catch (error) {
    next(error);
  }
});

// Sign out
router.post('/signout', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const authHeader = req.headers.authorization;
    
    if (authHeader && authHeader.startsWith('Bearer ')) {
      // Extract user ID from token
      const token = authHeader.substring(7);
      // In a real implementation, you would decode the token to get userId
      logger.info('User signed out');
    }
    
    res.json({ success: true });
  } catch (error) {
    next(error);
  }
});

export default router;