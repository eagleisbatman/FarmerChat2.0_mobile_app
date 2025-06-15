import { Server as SocketIOServer } from 'socket.io';
import ChatSocketHandler from './chat.socket';
import { logger } from '../utils/logger';

export function initializeSocketHandlers(io: SocketIOServer): void {
  logger.info('Initializing Socket.IO handlers...');
  
  // Make io globally available for services
  (global as any).io = io;
  
  // Initialize chat socket handler
  const chatHandler = new ChatSocketHandler();
  chatHandler.initialize(io);
  
  // Add connection logging
  io.on('connection', (socket) => {
    logger.info(`New WebSocket connection: ${socket.id}`);
    
    socket.on('disconnect', (reason) => {
      logger.info(`WebSocket disconnected: ${socket.id}, reason: ${reason}`);
    });
  });
  
  logger.info('Socket.IO handlers initialized successfully');
}

export { ChatSocketHandler };