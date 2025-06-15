import { Request, Response, NextFunction } from 'express';
import { logger } from '../utils/logger';

export const requestLogger = (req: Request, res: Response, next: NextFunction): void => {
  const start = Date.now();
  
  // Log request
  logger.debug(`Incoming ${req.method} ${req.path}`, {
    query: req.query,
    body: req.method !== 'GET' ? req.body : undefined,
    headers: {
      'user-agent': req.get('user-agent'),
      'content-type': req.get('content-type')
    }
  });
  
  // Log response
  const originalSend = res.send;
  res.send = function(data: any) {
    const duration = Date.now() - start;
    logger.info(`${req.method} ${req.path} ${res.statusCode} ${duration}ms`);
    
    // Log error responses
    if (res.statusCode >= 400) {
      logger.error(`Error response: ${res.statusCode}`, {
        path: req.path,
        method: req.method,
        duration,
        response: data
      });
    }
    
    return originalSend.call(this, data);
  };
  
  next();
};