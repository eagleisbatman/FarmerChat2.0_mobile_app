import { Server as SocketIOServer, Socket } from 'socket.io';
import jwt from 'jsonwebtoken';
import { AIService, ChatRequest } from '../services/ai.service';
import { logger } from '../utils/logger';
import { config } from '../config';
import { query } from '../database';

export interface AuthenticatedSocket extends Socket {
  userId?: string;
  user?: any;
}

export class ChatSocketHandler {
  private aiService: AIService;

  constructor() {
    this.aiService = new AIService();
  }

  initialize(io: SocketIOServer): void {
    // Authentication middleware for Socket.IO
    io.use(async (socket: AuthenticatedSocket, next) => {
      try {
        const token = socket.handshake.auth.token || socket.handshake.headers.authorization?.replace('Bearer ', '');
        
        if (!token) {
          return next(new Error('No token provided'));
        }

        // Verify JWT token
        const decoded = jwt.verify(token, config.jwt.secret) as any;
        socket.userId = decoded.userId || decoded.sub;
        
        // Get user data
        if (socket.userId) {
          const result = await query(
            'SELECT * FROM users WHERE id = $1',
            [socket.userId]
          );
          
          if (result.rows.length === 0) {
            return next(new Error('User not found'));
          }
          
          socket.user = result.rows[0];
        }
        
        next();
      } catch (error) {
        logger.error('Socket authentication error:', error);
        next(new Error('Authentication failed'));
      }
    });

    io.on('connection', (socket: AuthenticatedSocket) => {
      logger.info(`User ${socket.userId} connected to chat socket`);

      // Join user to their personal room
      if (socket.userId) {
        socket.join(`user_${socket.userId}`);
      }

      // Handle chat message with streaming
      socket.on('chat:stream', async (data) => {
        try {
          const { message, conversationId } = data;
          
          if (!message || !conversationId) {
            socket.emit('chat:error', { 
              error: 'Message and conversationId are required' 
            });
            return;
          }

          // Verify user owns this conversation
          const conversationResult = await query(
            'SELECT user_id FROM conversations WHERE id = $1',
            [conversationId]
          );

          if (conversationResult.rows.length === 0) {
            socket.emit('chat:error', { 
              error: 'Conversation not found' 
            });
            return;
          }

          if (conversationResult.rows[0].user_id !== socket.userId) {
            socket.emit('chat:error', { 
              error: 'Access denied' 
            });
            return;
          }

          // Emit typing indicator
          socket.emit('chat:typing', { isTyping: true });

          const chatRequest: ChatRequest = {
            message,
            conversationId,
            userId: socket.userId!,
            userProfile: socket.user,
            stream: true
          };

          let fullResponse = '';
          let chunkCount = 0;

          // Generate streaming response
          const response = await this.aiService.generateStreamResponse(
            chatRequest,
            (chunk) => {
              if (!chunk.isComplete) {
                fullResponse += chunk.content;
                chunkCount++;
                
                // Emit response chunk
                socket.emit('chat:chunk', {
                  content: chunk.content,
                  chunkNumber: chunkCount,
                  isComplete: false
                });
              }
            }
          );

          // Stop typing indicator
          socket.emit('chat:typing', { isTyping: false });

          // Extract follow-up questions
          const followUpQuestions = await this.aiService.extractFollowUpQuestions(
            fullResponse,
            socket.user?.language || 'en',
            socket.user
          );

          // Generate title if this is the first message in conversation
          const messageCount = await query(
            'SELECT COUNT(*) as count FROM messages WHERE conversation_id = $1',
            [conversationId]
          );
          
          let title;
          if (messageCount.rows[0].count <= 2) { // User message + AI response
            title = await this.aiService.generateConversationTitle(
              message,
              fullResponse,
              socket.user?.language || 'en'
            );
            
            // Update conversation title
            await query(
              'UPDATE conversations SET title = $1 WHERE id = $2',
              [title, conversationId]
            );
          }

          // Process conversation analytics asynchronously after a few messages
          const totalMessages = parseInt(messageCount.rows[0].count);
          if (totalMessages >= 4) { // After at least 2 exchanges (4 messages)
            // Run analytics processing in background
            this.aiService.processConversationAnalytics(
              conversationId,
              socket.user?.language || 'en',
              socket.user
            ).catch(error => {
              logger.error('Failed to process conversation analytics:', error);
            });
          }

          // Emit completion with metadata
          socket.emit('chat:complete', {
            content: fullResponse,
            followUpQuestions,
            title,
            usage: response.usage,
            totalChunks: chunkCount
          });

        } catch (error) {
          logger.error('Chat stream error:', error);
          socket.emit('chat:error', {
            error: error instanceof Error ? error.message : 'Failed to generate response'
          });
          socket.emit('chat:typing', { isTyping: false });
        }
      });

      // Handle stop generation
      socket.on('chat:stop', (data) => {
        // In a real implementation, you would stop the AI generation
        logger.info(`User ${socket.userId} stopped generation for conversation ${data.conversationId}`);
        socket.emit('chat:typing', { isTyping: false });
        socket.emit('chat:stopped', { stopped: true });
      });

      // Handle typing indicators
      socket.on('chat:typing', (data) => {
        const { conversationId, isTyping } = data;
        
        // Broadcast typing indicator to other participants (if it's a shared conversation)
        socket.to(`conversation_${conversationId}`).emit('chat:user_typing', {
          userId: socket.userId,
          isTyping
        });
      });

      // Handle joining conversation room
      socket.on('chat:join', (data) => {
        const { conversationId } = data;
        socket.join(`conversation_${conversationId}`);
        logger.info(`User ${socket.userId} joined conversation ${conversationId}`);
      });

      // Handle leaving conversation room
      socket.on('chat:leave', (data) => {
        const { conversationId } = data;
        socket.leave(`conversation_${conversationId}`);
        logger.info(`User ${socket.userId} left conversation ${conversationId}`);
      });

      // Handle disconnect
      socket.on('disconnect', (reason) => {
        logger.info(`User ${socket.userId} disconnected from chat socket: ${reason}`);
      });

      // Handle errors
      socket.on('error', (error) => {
        logger.error(`Socket error for user ${socket.userId}:`, error);
      });
    });
  }

  // Send notification to user via socket
  async sendNotificationToUser(userId: string, notification: any): Promise<void> {
    const io = (global as any).io as SocketIOServer;
    if (io) {
      io.to(`user_${userId}`).emit('notification', notification);
    }
  }

  // Send message to conversation participants
  async sendToConversation(conversationId: string, event: string, data: any): Promise<void> {
    const io = (global as any).io as SocketIOServer;
    if (io) {
      io.to(`conversation_${conversationId}`).emit(event, data);
    }
  }
}

export default ChatSocketHandler;