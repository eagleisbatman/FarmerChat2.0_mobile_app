import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import morgan from 'morgan';
import compression from 'compression';
import dotenv from 'dotenv';
import { createServer } from 'http';
import { Server as SocketIOServer } from 'socket.io';
import { config } from './config';
import { errorHandler } from './middleware/errorHandler';
import { rateLimiter } from './middleware/rateLimiter';
import { requestLogger } from './middleware/requestLogger';
import routes from './routes';
import { initializeDatabase } from './database';
import { initializeServices } from './services';
import { initializeSocketHandlers } from './socket';
import { logger } from './utils/logger';

// Load environment variables
dotenv.config();

// Create Express app
const app = express();
const server = createServer(app);

// Initialize Socket.IO if enabled
let io: SocketIOServer | undefined;
if (config.websocket.enabled) {
  io = new SocketIOServer(server, {
    cors: {
      origin: config.websocket.corsOrigin,
      credentials: true
    }
  });
}

// Global middleware
app.use(helmet());
app.use(cors(config.cors));
app.use(compression());
app.use(express.json({ limit: config.server.maxFileSize }));
app.use(express.urlencoded({ extended: true, limit: config.server.maxFileSize }));
app.use(morgan(config.logging.format));
app.use(requestLogger);

// API rate limiting
app.use('/api', rateLimiter);

// Health check endpoint
app.get('/health', (req, res) => {
  res.status(200).json({
    status: 'healthy',
    timestamp: new Date().toISOString(),
    environment: config.server.env,
    version: config.server.apiVersion
  });
});

// API routes
app.use(`/api/${config.server.apiVersion}`, routes);

// Socket.IO connection handling
if (io) {
  initializeSocketHandlers(io);
}

// Error handling middleware (must be last)
app.use(errorHandler);

// 404 handler
app.use((req, res) => {
  res.status(404).json({
    error: 'Not Found',
    message: `Route ${req.method} ${req.path} not found`
  });
});

// Initialize services and start server
async function startServer() {
  try {
    // Initialize database connection
    await initializeDatabase();
    logger.info('Database initialized successfully');
    
    // Initialize services
    await initializeServices();
    logger.info('Services initialized successfully');
    
    // Start server
    const port = config.server.port;
    server.listen(port, () => {
      logger.info(`🚀 Server running on port ${port} in ${config.server.env} mode`);
      logger.info(`📡 API version: ${config.server.apiVersion}`);
      if (config.websocket.enabled) {
        logger.info('🔌 WebSocket enabled');
      }
    });
  } catch (error) {
    logger.error('Failed to start server:', error);
    process.exit(1);
  }
}

// Handle graceful shutdown
process.on('SIGTERM', async () => {
  logger.info('SIGTERM received, shutting down gracefully...');
  server.close(() => {
    logger.info('Server closed');
    process.exit(0);
  });
});

process.on('SIGINT', async () => {
  logger.info('SIGINT received, shutting down gracefully...');
  server.close(() => {
    logger.info('Server closed');
    process.exit(0);
  });
});

// Export for testing
export { app, server, io };

// Start server if not in test environment
if (process.env.NODE_ENV !== 'test') {
  startServer();
}