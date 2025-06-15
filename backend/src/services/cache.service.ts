import Redis from 'ioredis';
import { config } from '../config';
import { logger } from '../utils/logger';

export class CacheService {
  private redis: Redis;
  private connected: boolean = false;
  
  constructor() {
    this.redis = new Redis({
      host: config.redis.url,
      password: config.redis.password,
      db: config.redis.db,
      lazyConnect: true,
      retryStrategy: (times: number) => {
        const delay = Math.min(times * 50, 2000);
        return delay;
      }
    });
    
    this.redis.on('error', (err) => {
      logger.error('Redis error:', err);
      this.connected = false;
    });
    
    this.redis.on('connect', () => {
      logger.info('Redis connected');
      this.connected = true;
    });
  }
  
  async connect(): Promise<void> {
    try {
      await this.redis.connect();
      this.connected = true;
    } catch (error) {
      logger.error('Failed to connect to Redis:', error);
      // Don't throw - cache should be optional
    }
  }
  
  async get<T>(key: string): Promise<T | null> {
    if (!this.connected) return null;
    
    try {
      const value = await this.redis.get(key);
      return value ? JSON.parse(value) : null;
    } catch (error) {
      logger.error(`Cache get error for key ${key}:`, error);
      return null;
    }
  }
  
  async set(key: string, value: any, ttl?: number): Promise<void> {
    if (!this.connected) return;
    
    try {
      const stringValue = JSON.stringify(value);
      if (ttl) {
        await this.redis.set(key, stringValue, 'EX', ttl);
      } else {
        await this.redis.set(key, stringValue);
      }
    } catch (error) {
      logger.error(`Cache set error for key ${key}:`, error);
    }
  }
  
  async delete(key: string): Promise<void> {
    if (!this.connected) return;
    
    try {
      await this.redis.del(key);
    } catch (error) {
      logger.error(`Cache delete error for key ${key}:`, error);
    }
  }
  
  async deletePattern(pattern: string): Promise<void> {
    if (!this.connected) return;
    
    try {
      const keys = await this.redis.keys(pattern);
      if (keys.length > 0) {
        await this.redis.del(...keys);
      }
    } catch (error) {
      logger.error(`Cache delete pattern error for ${pattern}:`, error);
    }
  }
  
  async exists(key: string): Promise<boolean> {
    if (!this.connected) return false;
    
    try {
      const result = await this.redis.exists(key);
      return result === 1;
    } catch (error) {
      logger.error(`Cache exists error for key ${key}:`, error);
      return false;
    }
  }
  
  async incr(key: string, ttl?: number): Promise<number> {
    if (!this.connected) return 0;
    
    try {
      const value = await this.redis.incr(key);
      if (ttl && value === 1) {
        await this.redis.expire(key, ttl);
      }
      return value;
    } catch (error) {
      logger.error(`Cache incr error for key ${key}:`, error);
      return 0;
    }
  }
  
  async disconnect(): Promise<void> {
    if (this.connected) {
      await this.redis.quit();
      this.connected = false;
    }
  }
}