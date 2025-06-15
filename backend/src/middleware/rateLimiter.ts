import { Request, Response, NextFunction } from 'express';
import Redis from 'ioredis';
import { config } from '../config';
import { AppError } from './errorHandler';

const redis = new Redis({
  host: config.redis.url,
  password: config.redis.password,
  db: config.redis.db,
  lazyConnect: true
});

// Connect to Redis with error handling
redis.connect().catch((err) => {
  console.error('Redis connection error:', err);
});

interface RateLimitOptions {
  windowMs: number;
  max: number;
  message?: string;
  keyPrefix?: string;
}

export const createRateLimiter = (options: RateLimitOptions) => {
  const {
    windowMs,
    max,
    message = 'Too many requests, please try again later.',
    keyPrefix = 'rl'
  } = options;
  
  return async (req: Request, res: Response, next: NextFunction) => {
    try {
      // Skip rate limiting if Redis is not connected
      if (redis.status !== 'ready') {
        return next();
      }
      
      // Generate key based on IP
      const key = `${keyPrefix}:${req.ip}`;
      
      // Get current count
      const current = await redis.incr(key);
      
      // Set expiry on first request
      if (current === 1) {
        await redis.expire(key, Math.ceil(windowMs / 1000));
      }
      
      // Check if limit exceeded
      if (current > max) {
        throw new AppError(message, 429, 'RATE_LIMIT_EXCEEDED');
      }
      
      // Add rate limit headers
      res.setHeader('X-RateLimit-Limit', max);
      res.setHeader('X-RateLimit-Remaining', Math.max(0, max - current));
      res.setHeader('X-RateLimit-Reset', new Date(Date.now() + windowMs).toISOString());
      
      next();
    } catch (error) {
      if (error instanceof AppError) {
        next(error);
      } else {
        // If Redis fails, allow the request but log the error
        console.error('Rate limiter error:', error);
        next();
      }
    }
  };
};

// Default rate limiter for API endpoints
export const rateLimiter = createRateLimiter({
  windowMs: 60 * 60 * 1000, // 1 hour
  max: config.rateLimit.api
});

// Translation API rate limiter
export const translationRateLimiter = createRateLimiter({
  windowMs: 60 * 60 * 1000, // 1 hour
  max: config.rateLimit.translation,
  keyPrefix: 'rl:translation'
});