import express from 'express';
import { AuthRequest, authenticate } from '../middleware/auth';
import { AppError } from '../middleware/errorHandler';
import { logger } from '../utils/logger';
import { query } from '../database';
import { NotificationService } from '../services/notification.service';

const router = express.Router();
const notificationService = new NotificationService();

// Get user profile
router.get('/profile', authenticate, async (req: AuthRequest, res) => {
  try {
    const result = await query(
      `SELECT id, email, phone, name, language, location, location_info, 
              crops, livestock, preferences, role, gender, created_at, updated_at
       FROM users WHERE id = $1`,
      [req.userId]
    );

    if (result.rows.length === 0) {
      throw new AppError('User not found', 404);
    }

    const user = result.rows[0];
    
    // Parse JSON fields
    user.location_info = typeof user.location_info === 'string' 
      ? JSON.parse(user.location_info || '{}') 
      : user.location_info || {};
    user.preferences = typeof user.preferences === 'string' 
      ? JSON.parse(user.preferences || '{}') 
      : user.preferences || {};

    res.json({
      success: true,
      data: { user }
    });
  } catch (error) {
    logger.error('Get user profile error:', error);
    res.status(500).json({
      success: false,
      error: error instanceof Error ? error.message : 'Failed to get user profile'
    });
  }
});

// Update user profile
router.put('/profile', authenticate, async (req: AuthRequest, res) => {
  try {
    const { 
      name, 
      language, 
      location, 
      location_info, 
      crops, 
      livestock,
      role,
      gender
    } = req.body;

    const setClauses = [];
    const values = [];
    let paramIndex = 1;

    if (name !== undefined) {
      setClauses.push(`name = $${paramIndex++}`);
      values.push(name);
    }

    if (language !== undefined) {
      setClauses.push(`language = $${paramIndex++}`);
      values.push(language);
    }

    if (location !== undefined) {
      setClauses.push(`location = $${paramIndex++}`);
      values.push(location);
    }

    if (location_info !== undefined) {
      setClauses.push(`location_info = $${paramIndex++}`);
      values.push(JSON.stringify(location_info));
    }

    if (crops !== undefined) {
      setClauses.push(`crops = $${paramIndex++}`);
      values.push(Array.isArray(crops) ? crops : []);
    }

    if (livestock !== undefined) {
      setClauses.push(`livestock = $${paramIndex++}`);
      values.push(Array.isArray(livestock) ? livestock : []);
    }

    if (role !== undefined) {
      setClauses.push(`role = $${paramIndex++}`);
      values.push(role);
    }

    if (gender !== undefined) {
      setClauses.push(`gender = $${paramIndex++}`);
      values.push(gender);
    }

    if (setClauses.length === 0) {
      throw new AppError('No fields to update', 400);
    }

    setClauses.push(`updated_at = NOW()`);
    values.push(req.userId);

    const result = await query(
      `UPDATE users SET ${setClauses.join(', ')} 
       WHERE id = $${paramIndex} RETURNING *`,
      values
    );

    const user = result.rows[0];
    
    // Parse JSON fields
    user.location_info = typeof user.location_info === 'string' 
      ? JSON.parse(user.location_info || '{}') 
      : user.location_info || {};
    user.preferences = typeof user.preferences === 'string' 
      ? JSON.parse(user.preferences || '{}') 
      : user.preferences || {};

    res.json({
      success: true,
      data: { user }
    });
  } catch (error) {
    logger.error('Update user profile error:', error);
    res.status(500).json({
      success: false,
      error: error instanceof Error ? error.message : 'Failed to update user profile'
    });
  }
});

// Update user preferences
router.put('/preferences', authenticate, async (req: AuthRequest, res) => {
  try {
    const { preferences } = req.body;

    if (!preferences || typeof preferences !== 'object') {
      throw new AppError('Valid preferences object is required', 400);
    }

    // Get current preferences and merge
    const currentResult = await query(
      'SELECT preferences FROM users WHERE id = $1',
      [req.userId]
    );

    let currentPreferences = {};
    if (currentResult.rows.length > 0) {
      const current = currentResult.rows[0].preferences;
      currentPreferences = typeof current === 'string' 
        ? JSON.parse(current || '{}') 
        : current || {};
    }

    const mergedPreferences = { ...currentPreferences, ...preferences };

    const result = await query(
      `UPDATE users SET preferences = $1, updated_at = NOW() 
       WHERE id = $2 RETURNING preferences`,
      [JSON.stringify(mergedPreferences), req.userId]
    );

    const updatedPreferences = typeof result.rows[0].preferences === 'string'
      ? JSON.parse(result.rows[0].preferences)
      : result.rows[0].preferences;

    res.json({
      success: true,
      data: { preferences: updatedPreferences }
    });
  } catch (error) {
    logger.error('Update user preferences error:', error);
    res.status(500).json({
      success: false,
      error: error instanceof Error ? error.message : 'Failed to update user preferences'
    });
  }
});

// Update FCM token
router.put('/fcm-token', authenticate, async (req: AuthRequest, res) => {
  try {
    const { fcmToken } = req.body;

    if (!fcmToken) {
      throw new AppError('FCM token is required', 400);
    }

    await notificationService.updateFCMToken(req.userId!, fcmToken);

    res.json({
      success: true,
      message: 'FCM token updated successfully'
    });
  } catch (error) {
    logger.error('Update FCM token error:', error);
    res.status(500).json({
      success: false,
      error: error instanceof Error ? error.message : 'Failed to update FCM token'
    });
  }
});

// Export user data
router.get('/export', authenticate, async (req: AuthRequest, res) => {
  try {
    // Get user profile
    const userResult = await query(
      `SELECT id, email, phone, name, language, location, location_info, 
              crops, livestock, preferences, role, gender, created_at
       FROM users WHERE id = $1`,
      [req.userId]
    );

    // Get conversations
    const conversationsResult = await query(
      `SELECT id, title, tags, created_at, updated_at
       FROM conversations WHERE user_id = $1
       ORDER BY created_at DESC`,
      [req.userId]
    );

    // Get messages for each conversation
    const conversations = [];
    for (const conv of conversationsResult.rows) {
      const messagesResult = await query(
        `SELECT content, is_user, is_voice_message, created_at
         FROM messages WHERE conversation_id = $1
         ORDER BY created_at ASC`,
        [conv.id]
      );

      conversations.push({
        ...conv,
        messages: messagesResult.rows
      });
    }

    // Get feedback
    const feedbackResult = await query(
      `SELECT rating, feedback_text, type, created_at
       FROM feedback WHERE user_id = $1
       ORDER BY created_at DESC`,
      [req.userId]
    );

    const exportData = {
      user: userResult.rows[0],
      conversations,
      feedback: feedbackResult.rows,
      exportedAt: new Date().toISOString()
    };

    res.json({
      success: true,
      data: exportData
    });
  } catch (error) {
    logger.error('Export user data error:', error);
    res.status(500).json({
      success: false,
      error: error instanceof Error ? error.message : 'Failed to export user data'
    });
  }
});

// Delete user account and all data
router.delete('/account', authenticate, async (req: AuthRequest, res) => {
  try {
    const { confirmation } = req.body;

    if (confirmation !== 'DELETE_MY_ACCOUNT') {
      throw new AppError('Account deletion requires confirmation', 400);
    }

    // Delete user (cascade will handle related data)
    const result = await query(
      'DELETE FROM users WHERE id = $1 RETURNING id',
      [req.userId]
    );

    if (result.rows.length === 0) {
      throw new AppError('User not found', 404);
    }

    res.json({
      success: true,
      message: 'Account deleted successfully'
    });
  } catch (error) {
    logger.error('Delete user account error:', error);
    res.status(500).json({
      success: false,
      error: error instanceof Error ? error.message : 'Failed to delete user account'
    });
  }
});

// Get user statistics
router.get('/stats', authenticate, async (req: AuthRequest, res) => {
  try {
    // Get conversation count
    const conversationStats = await query(
      'SELECT COUNT(*) as total_conversations FROM conversations WHERE user_id = $1',
      [req.userId]
    );

    // Get message statistics
    const messageStats = await query(
      `SELECT 
         COUNT(*) as total_messages,
         COUNT(*) FILTER (WHERE is_user = true) as user_messages,
         COUNT(*) FILTER (WHERE is_user = false) as ai_messages,
         COUNT(*) FILTER (WHERE is_voice_message = true) as voice_messages
       FROM messages m
       JOIN conversations c ON m.conversation_id = c.id
       WHERE c.user_id = $1`,
      [req.userId]
    );

    // Get account age
    const userInfo = await query(
      'SELECT created_at FROM users WHERE id = $1',
      [req.userId]
    );

    const stats = {
      totalConversations: parseInt(conversationStats.rows[0].total_conversations),
      totalMessages: parseInt(messageStats.rows[0].total_messages || 0),
      userMessages: parseInt(messageStats.rows[0].user_messages || 0),
      aiMessages: parseInt(messageStats.rows[0].ai_messages || 0),
      voiceMessages: parseInt(messageStats.rows[0].voice_messages || 0),
      accountCreated: userInfo.rows[0]?.created_at
    };

    res.json({
      success: true,
      data: { stats }
    });
  } catch (error) {
    logger.error('Get user stats error:', error);
    res.status(500).json({
      success: false,
      error: error instanceof Error ? error.message : 'Failed to get user statistics'
    });
  }
});

export default router;