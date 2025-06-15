import { logger } from '../utils/logger';
import { FirebaseService } from './firebase.service';
import { query } from '../database';

export class NotificationService {
  private firebase: FirebaseService;
  
  constructor() {
    this.firebase = FirebaseService.getInstance();
  }
  
  async initialize(): Promise<void> {
    logger.info('Notification service initialized with Firebase');
  }
  
  async sendNotification(userId: string, title: string, body: string, data?: any): Promise<void> {
    try {
      // Get user's FCM token from database
      const result = await query<{ fcm_token: string }>(
        'SELECT fcm_token FROM users WHERE id = $1',
        [userId]
      );
      
      if (result.rows.length === 0 || !result.rows[0].fcm_token) {
        logger.warn(`No FCM token found for user ${userId}`);
        return;
      }
      
      const fcmToken = result.rows[0].fcm_token;
      
      // Send notification via Firebase
      await this.firebase.sendNotification(fcmToken, title, body, data);
      
      // Save notification to database
      await query(
        `INSERT INTO notifications (user_id, title, body, data, type, sent_at)
         VALUES ($1, $2, $3, $4, $5, NOW())`,
        [userId, title, body, JSON.stringify(data || {}), 'fcm']
      );
    } catch (error) {
      logger.error(`Failed to send notification to user ${userId}:`, error);
      throw error;
    }
  }
  
  async sendBulkNotifications(
    userIds: string[],
    title: string,
    body: string,
    data?: any
  ): Promise<void> {
    try {
      // Get FCM tokens for all users
      const result = await query<{ id: string; fcm_token: string }>(
        'SELECT id, fcm_token FROM users WHERE id = ANY($1) AND fcm_token IS NOT NULL',
        [userIds]
      );
      
      if (result.rows.length === 0) {
        logger.warn('No FCM tokens found for any users');
        return;
      }
      
      const tokens = result.rows.map(row => row.fcm_token);
      const userTokenMap = new Map(result.rows.map(row => [row.fcm_token, row.id]));
      
      // Send multicast notification
      const response = await this.firebase.sendMulticastNotification(tokens, title, body, data);
      
      // Save successful notifications to database
      const successfulTokens = tokens.filter((_, index) => !response.responses[index].error);
      const successfulUserIds = successfulTokens.map(token => userTokenMap.get(token)!);
      
      if (successfulUserIds.length > 0) {
        const values = successfulUserIds.map(userId => 
          [userId, title, body, JSON.stringify(data || {}), 'fcm']
        );
        
        // Batch insert notifications
        const placeholders = values.map((_, i) => 
          `($${i * 5 + 1}, $${i * 5 + 2}, $${i * 5 + 3}, $${i * 5 + 4}, $${i * 5 + 5}, NOW())`
        ).join(', ');
        
        await query(
          `INSERT INTO notifications (user_id, title, body, data, type, sent_at)
           VALUES ${placeholders}`,
          values.flat()
        );
      }
      
      logger.info(`Sent notifications: ${response.successCount} successful, ${response.failureCount} failed`);
    } catch (error) {
      logger.error('Failed to send bulk notifications:', error);
      throw error;
    }
  }
  
  async updateFCMToken(userId: string, fcmToken: string): Promise<void> {
    await query(
      'UPDATE users SET fcm_token = $1, updated_at = NOW() WHERE id = $2',
      [fcmToken, userId]
    );
  }
}