import swaggerJsdoc from 'swagger-jsdoc';
import { config } from './index';

const options: swaggerJsdoc.Options = {
  definition: {
    openapi: '3.0.0',
    info: {
      title: 'FarmerChat API',
      version: '1.0.0',
      description: 'API documentation for FarmerChat backend',
      contact: {
        name: 'FarmerChat Team',
        email: 'support@farmerchat.com'
      }
    },
    servers: [
      {
        url: `http://localhost:${config.server.port}/api/v1`,
        description: 'Development server'
      }
    ],
    tags: [
      {
        name: 'Authentication',
        description: 'Firebase authentication and JWT token management'
      },
      {
        name: 'Users',
        description: 'User profile management'
      },
      {
        name: 'Chat',
        description: 'AI chat functionality'
      },
      {
        name: 'Conversations',
        description: 'Conversation management'
      },
      {
        name: 'Translations',
        description: 'Multi-language translation services'
      }
    ],
    components: {
      securitySchemes: {
        BearerAuth: {
          type: 'http',
          scheme: 'bearer',
          bearerFormat: 'JWT',
          description: 'JWT Authorization header using the Bearer scheme'
        }
      },
      schemas: {
        Error: {
          type: 'object',
          properties: {
            error: {
              type: 'object',
              properties: {
                message: { type: 'string' },
                statusCode: { type: 'number' },
                code: { type: 'string' }
              }
            }
          }
        },
        AuthRequest: {
          type: 'object',
          required: ['idToken'],
          properties: {
            idToken: { 
              type: 'string',
              description: 'Firebase ID token'
            },
            deviceInfo: {
              type: 'object',
              properties: {
                deviceId: { type: 'string' },
                appVersion: { type: 'string' },
                fcmToken: { type: 'string' }
              }
            }
          }
        },
        AuthResponse: {
          type: 'object',
          properties: {
            success: { type: 'boolean' },
            data: {
              type: 'object',
              properties: {
                user: { $ref: '#/components/schemas/User' },
                token: { type: 'string' },
                refreshToken: { type: 'string' }
              }
            }
          }
        },
        User: {
          type: 'object',
          properties: {
            id: { type: 'string' },
            firebase_uid: { type: 'string' },
            name: { type: 'string' },
            phone_number: { type: 'string' },
            language: { type: 'string' },
            location: { type: 'string' },
            crops: {
              type: 'array',
              items: { type: 'string' }
            },
            livestock: {
              type: 'array',
              items: { type: 'string' }
            },
            response_length: { type: 'string' },
            created_at: { type: 'string', format: 'date-time' },
            updated_at: { type: 'string', format: 'date-time' }
          }
        },
        Conversation: {
          type: 'object',
          properties: {
            id: { type: 'string' },
            user_id: { type: 'string' },
            title: { type: 'string' },
            last_message: { type: 'string' },
            message_count: { type: 'number' },
            created_at: { type: 'string', format: 'date-time' },
            updated_at: { type: 'string', format: 'date-time' }
          }
        },
        Message: {
          type: 'object',
          properties: {
            id: { type: 'string' },
            conversation_id: { type: 'string' },
            content: { type: 'string' },
            is_user: { type: 'boolean' },
            metadata: { type: 'object' },
            created_at: { type: 'string', format: 'date-time' }
          }
        }
      }
    },
    security: [
      {
        BearerAuth: []
      }
    ]
  },
  apis: ['./src/routes/*.ts', './src/routes/**/*.ts'] // Path to the API routes
};

export const swaggerSpec = swaggerJsdoc(options);