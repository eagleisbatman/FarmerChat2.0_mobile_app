import { Router, Request, Response, NextFunction } from 'express';
import { AuthService } from '../services/auth.service';
import { AppError } from '../middleware/errorHandler';
import { logger } from '../utils/logger';

const router = Router();
const authService = new AuthService();

/**
 * @swagger
 * /auth/config:
 *   get:
 *     summary: Get Firebase auth configuration
 *     tags: [Authentication]
 *     security: []
 *     responses:
 *       200:
 *         description: Firebase configuration
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success: 
 *                   type: boolean
 *                 data:
 *                   type: object
 */
// Get Firebase auth config
router.get('/config', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const result = await authService.getAuthConfig();
    res.json(result);
  } catch (error) {
    next(error);
  }
});

/**
 * @swagger
 * /auth/verify:
 *   post:
 *     summary: Verify Firebase ID token and authenticate user
 *     tags: [Authentication]
 *     security: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/AuthRequest'
 *     responses:
 *       200:
 *         description: Authentication successful
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/AuthResponse'
 *       400:
 *         description: Invalid request
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/Error'
 *       401:
 *         description: Invalid token
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/Error'
 */
// Verify Firebase ID token
router.post('/verify', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { idToken, userInfo } = req.body;
    
    if (!idToken) {
      throw new AppError('ID token is required', 400);
    }
    
    const result = await authService.verifyFirebaseToken(idToken);
    
    res.json({
      success: true,
      data: {
        token: result.accessToken,
        refreshToken: result.refreshToken,
        expiresIn: result.expiresIn,
        user: result.user
      },
      error: null
    });
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
    
    res.json({
      success: true,
      data: {
        token: result.accessToken,
        refreshToken: result.refreshToken,
        expiresIn: result.expiresIn,
        user: result.user
      },
      error: null
    });
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