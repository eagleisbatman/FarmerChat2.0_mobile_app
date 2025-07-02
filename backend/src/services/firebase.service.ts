import * as admin from 'firebase-admin';
import { config } from '../config';
import { logger } from '../utils/logger';

export class FirebaseService {
  private static instance: FirebaseService;
  private app: admin.app.App;
  
  private constructor() {
    // Initialize Firebase Admin SDK for FCM only
    if (!admin.apps.length) {
      this.app = admin.initializeApp({
        credential: admin.credential.cert({
          projectId: config.firebase.projectId,
          privateKey: config.firebase.privateKey,
          clientEmail: config.firebase.clientEmail
        })
      });
      logger.info('Firebase Admin SDK initialized for FCM');
    } else {
      this.app = admin.app();
    }
  }
  
  static getInstance(): FirebaseService {
    if (!FirebaseService.instance) {
      FirebaseService.instance = new FirebaseService();
    }
    return FirebaseService.instance;
  }
  
  getMessaging(): admin.messaging.Messaging {
    return admin.messaging(this.app);
  }
  
  // Send FCM notification
  async sendNotification(
    token: string,
    title: string,
    body: string,
    data?: { [key: string]: string }
  ): Promise<string> {
    try {
      const message: admin.messaging.Message = {
        notification: {
          title,
          body
        },
        data,
        token
      };
      
      const response = await this.getMessaging().send(message);
      logger.info(`FCM notification sent: ${response}`);
      return response;
    } catch (error) {
      logger.error('Error sending FCM notification:', error);
      throw error;
    }
  }
  
  // Send notification to multiple devices
  async sendMulticastNotification(
    tokens: string[],
    title: string,
    body: string,
    data?: { [key: string]: string }
  ): Promise<admin.messaging.BatchResponse> {
    try {
      const message: admin.messaging.MulticastMessage = {
        notification: {
          title,
          body
        },
        data,
        tokens
      };
      
      const response = await this.getMessaging().sendEachForMulticast(message);
      logger.info(`FCM multicast sent: ${response.successCount} successful, ${response.failureCount} failed`);
      return response;
    } catch (error) {
      logger.error('Error sending FCM multicast:', error);
      throw error;
    }
  }
  
  // Get FCM config for client (only messaging sender ID needed)
  getFCMConfig() {
    return {
      messagingSenderId: config.firebase.messagingSenderId
    };
  }
}