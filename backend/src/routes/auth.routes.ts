import { Router, Request, Response, NextFunction } from 'express';
import { AuthService } from '../services/auth.service';
import { AppError } from '../middleware/errorHandler';
import { logger } from '../utils/logger';
import { v4 as uuidv4 } from 'uuid';

const router = Router();
const authService = new AuthService();

/**
 * Device-based authentication endpoint
 * Creates or retrieves a user based on device ID
 */
/**
 * Login with phone and PIN
 */
router.post('/login', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { phone, pin } = req.body;
    
    if (!phone || !pin) {
      throw new AppError('Phone number and PIN are required', 400);
    }
    
    logger.info(`Login attempt for phone: ${phone}`);
    
    const result = await authService.loginWithPhone(phone, pin);
    
    res.json({
      success: true,
      data: {
        token: result.accessToken,
        refreshToken: result.refreshToken,
        expiresIn: result.expiresIn,
        user: result.user,
        profileComplete: result.profileComplete
      }
    });
  } catch (error) {
    next(error);
  }
});

/**
 * Register with phone and PIN
 */
router.post('/register', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { phone, pin, firebaseIdToken } = req.body;
    
    if (!phone || !pin) {
      throw new AppError('Phone number and PIN are required', 400);
    }
    
    if (pin.length !== 6) {
      throw new AppError('PIN must be 6 digits', 400);
    }
    
    logger.info(`Registration request for phone: ${phone}`);
    
    const result = await authService.registerWithPhone(phone, pin, firebaseIdToken);
    
    res.json({
      success: true,
      data: {
        token: result.accessToken,
        refreshToken: result.refreshToken,
        expiresIn: result.expiresIn,
        user: result.user
      }
    });
  } catch (error) {
    next(error);
  }
});

router.post('/device', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { deviceId, deviceInfo } = req.body;
    
    if (!deviceId) {
      throw new AppError('Device ID is required', 400);
    }
    
    logger.info(`Device authentication request for device: ${deviceId}`);
    
    const result = await authService.authenticateDevice(deviceId, deviceInfo);
    
    // Wrap response in expected format and rename accessToken to token
    res.json({
      success: true,
      data: {
        token: result.accessToken,
        refreshToken: result.refreshToken,
        expiresIn: result.expiresIn,
        user: result.user
      }
    });
  } catch (error) {
    next(error);
  }
});

/**
 * Phone authentication endpoints
 */
router.post('/phone/request-otp', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { phoneNumber, userId } = req.body;
    
    if (!phoneNumber || !userId) {
      throw new AppError('Phone number and user ID are required', 400);
    }
    
    const result = await authService.requestPhoneOTP(phoneNumber, userId);
    res.json(result);
  } catch (error) {
    next(error);
  }
});

router.post('/phone/verify-otp', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { phoneNumber, otp, userId } = req.body;
    
    if (!phoneNumber || !otp || !userId) {
      throw new AppError('Phone number, OTP, and user ID are required', 400);
    }
    
    const result = await authService.verifyPhoneOTP(phoneNumber, otp, userId);
    res.json(result);
  } catch (error) {
    next(error);
  }
});

/**
 * Legacy Firebase endpoint - will be removed
 * Temporarily kept for backward compatibility
 */
router.post('/verify', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { idToken } = req.body;
    
    if (!idToken) {
      throw new AppError('ID token is required', 400);
    }
    
    const result = await authService.verifyFirebaseToken(idToken);
    
    // Wrap response in expected format and rename accessToken to token
    res.json({
      success: true,
      data: {
        token: result.accessToken,
        refreshToken: result.refreshToken,
        expiresIn: result.expiresIn,
        user: result.user
      }
    });
  } catch (error) {
    next(error);
  }
});

export default router;