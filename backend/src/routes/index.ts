import { Router } from 'express';
import authRoutes from './auth.routes';
import chatRoutes from './chat.routes';
import userRoutes from './users.routes';
import conversationRoutes from './conversations.routes';
import translationRoutes from './translations.routes';
import { authenticate } from '../middleware/auth';

const router = Router();

// Public routes
router.use('/auth', authRoutes);
router.use('/translations', translationRoutes);

// Protected routes (require authentication)
router.use('/chat', authenticate, chatRoutes);
router.use('/users', authenticate, userRoutes);
router.use('/conversations', authenticate, conversationRoutes);

// API info endpoint
router.get('/', (req, res) => {
  res.json({
    message: 'FarmerChat API',
    version: 'v1',
    endpoints: {
      auth: '/auth',
      chat: '/chat',
      users: '/users',
      conversations: '/conversations',
      translations: '/translations'
    }
  });
});

export default router;