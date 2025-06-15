import * as admin from 'firebase-admin';
import { config } from '../config';
import { logger } from '../utils/logger';

export class FirebaseService {
  private static instance: FirebaseService;
  private app: admin.app.App;
  
  private constructor() {
    // Initialize Firebase Admin SDK
    if (!admin.apps.length) {
      this.app = admin.initializeApp({
        credential: admin.credential.cert({
          projectId: config.firebase.projectId,
          privateKey: config.firebase.privateKey,
          clientEmail: config.firebase.clientEmail
        }),
        databaseURL: config.firebase.databaseURL
      });
      logger.info('Firebase Admin SDK initialized');
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
  
  getAuth(): admin.auth.Auth {
    return admin.auth(this.app);
  }
  
  getMessaging(): admin.messaging.Messaging {
    return admin.messaging(this.app);
  }
  
  // Phone OTP verification
  async verifyIdToken(idToken: string): Promise<admin.auth.DecodedIdToken> {
    try {
      const decodedToken = await this.getAuth().verifyIdToken(idToken);
      return decodedToken;
    } catch (error) {
      logger.error('Error verifying ID token:', error);
      throw error;
    }
  }
  
  // Get user by phone number
  async getUserByPhoneNumber(phoneNumber: string): Promise<admin.auth.UserRecord | null> {
    try {
      const userRecord = await this.getAuth().getUserByPhoneNumber(phoneNumber);
      return userRecord;
    } catch (error: any) {
      if (error.code === 'auth/user-not-found') {
        return null;
      }
      throw error;
    }
  }
  
  // Create custom token for server-side auth
  async createCustomToken(uid: string, claims?: object): Promise<string> {
    try {
      const customToken = await this.getAuth().createCustomToken(uid, claims);
      return customToken;
    } catch (error) {
      logger.error('Error creating custom token:', error);
      throw error;
    }
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
  
  // Get Firebase config for client
  getClientConfig() {
    return {
      apiKey: config.firebase.apiKey,
      authDomain: config.firebase.authDomain,
      projectId: config.firebase.projectId,
      messagingSenderId: config.firebase.messagingSenderId,
      appId: config.firebase.appId
    };
  }
}