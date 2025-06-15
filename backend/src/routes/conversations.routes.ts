import express from 'express';
import { AuthRequest, authenticate } from '../middleware/auth';
import { AppError } from '../middleware/errorHandler';
import { logger } from '../utils/logger';
import { query } from '../database';
import { v4 as uuidv4 } from 'uuid';

const router = express.Router();

// Get user conversations
router.get('/', authenticate, async (req: AuthRequest, res) => {
  try {
    const limit = parseInt(req.query.limit as string) || 20;
    const offset = parseInt(req.query.offset as string) || 0;
    const search = req.query.search as string;
    
    let query_text = `
      SELECT id, title, last_message, last_message_time, 
             last_message_is_user, tags, created_at, updated_at
      FROM conversations 
      WHERE user_id = $1
    `;
    const values = [req.userId];
    let paramIndex = 2;

    if (search) {
      query_text += ` AND (title ILIKE $${paramIndex} OR last_message ILIKE $${paramIndex})`;
      values.push(`%${search}%`);
      paramIndex++;
    }

    query_text += ` ORDER BY updated_at DESC LIMIT $${paramIndex} OFFSET $${paramIndex + 1}`;
    values.push(limit.toString(), offset.toString());

    const result = await query(query_text, values);

    // Get total count
    let countQuery = 'SELECT COUNT(*) as total FROM conversations WHERE user_id = $1';
    const countValues = [req.userId];
    
    if (search) {
      countQuery += ' AND (title ILIKE $2 OR last_message ILIKE $2)';
      countValues.push(`%${search}%`);
    }
    
    const countResult = await query(countQuery, countValues);

    res.json({
      success: true,
      data: {
        conversations: result.rows,
        total: parseInt(countResult.rows[0].total),
        limit: limit,
        offset: offset
      }
    });
  } catch (error) {
    logger.error('Get conversations error:', error);
    res.status(500).json({
      success: false,
      error: error instanceof Error ? error.message : 'Failed to get conversations'
    });
  }
});

// Create new conversation
router.post('/', authenticate, async (req: AuthRequest, res) => {
  try {
    const { title = 'New Conversation', tags = [] } = req.body;
    const conversationId = uuidv4();

    const result = await query(
      `INSERT INTO conversations (id, user_id, title, tags, created_at, updated_at)
       VALUES ($1, $2, $3, $4, NOW(), NOW())
       RETURNING *`,
      [conversationId, req.userId, title, JSON.stringify(tags)]
    );

    res.json({
      success: true,
      data: {
        conversation: result.rows[0]
      }
    });
  } catch (error) {
    logger.error('Create conversation error:', error);
    res.status(500).json({
      success: false,
      error: error instanceof Error ? error.message : 'Failed to create conversation'
    });
  }
});

// Get single conversation
router.get('/:id', authenticate, async (req: AuthRequest, res) => {
  try {
    const { id } = req.params;

    const result = await query(
      `SELECT id, title, last_message, last_message_time, 
              last_message_is_user, tags, created_at, updated_at
       FROM conversations 
       WHERE id = $1 AND user_id = $2`,
      [id, req.userId]
    );

    if (result.rows.length === 0) {
      throw new AppError('Conversation not found', 404);
    }

    res.json({
      success: true,
      data: {
        conversation: result.rows[0]
      }
    });
  } catch (error) {
    logger.error('Get conversation error:', error);
    res.status(500).json({
      success: false,
      error: error instanceof Error ? error.message : 'Failed to get conversation'
    });
  }
});

// Update conversation
router.put('/:id', authenticate, async (req: AuthRequest, res) => {
  try {
    const { id } = req.params;
    const { title, tags } = req.body;

    // Verify ownership
    const existingResult = await query(
      'SELECT id FROM conversations WHERE id = $1 AND user_id = $2',
      [id, req.userId]
    );

    if (existingResult.rows.length === 0) {
      throw new AppError('Conversation not found', 404);
    }

    const setClauses = [];
    const values = [];
    let paramIndex = 1;

    if (title !== undefined) {
      setClauses.push(`title = $${paramIndex++}`);
      values.push(title);
    }

    if (tags !== undefined) {
      setClauses.push(`tags = $${paramIndex++}`);
      values.push(JSON.stringify(tags));
    }

    setClauses.push(`updated_at = NOW()`);
    values.push(id);

    const result = await query(
      `UPDATE conversations SET ${setClauses.join(', ')} 
       WHERE id = $${paramIndex} RETURNING *`,
      values
    );

    res.json({
      success: true,
      data: {
        conversation: result.rows[0]
      }
    });
  } catch (error) {
    logger.error('Update conversation error:', error);
    res.status(500).json({
      success: false,
      error: error instanceof Error ? error.message : 'Failed to update conversation'
    });
  }
});

// Delete conversation
router.delete('/:id', authenticate, async (req: AuthRequest, res) => {
  try {
    const { id } = req.params;

    // Verify ownership and delete
    const result = await query(
      'DELETE FROM conversations WHERE id = $1 AND user_id = $2 RETURNING id',
      [id, req.userId]
    );

    if (result.rows.length === 0) {
      throw new AppError('Conversation not found', 404);
    }

    res.json({
      success: true,
      message: 'Conversation deleted successfully'
    });
  } catch (error) {
    logger.error('Delete conversation error:', error);
    res.status(500).json({
      success: false,
      error: error instanceof Error ? error.message : 'Failed to delete conversation'
    });
  }
});

// Get conversation statistics
router.get('/:id/stats', authenticate, async (req: AuthRequest, res) => {
  try {
    const { id } = req.params;

    // Verify ownership
    const conversationResult = await query(
      'SELECT id FROM conversations WHERE id = $1 AND user_id = $2',
      [id, req.userId]
    );

    if (conversationResult.rows.length === 0) {
      throw new AppError('Conversation not found', 404);
    }

    // Get message statistics
    const statsResult = await query(
      `SELECT 
         COUNT(*) as total_messages,
         COUNT(*) FILTER (WHERE is_user = true) as user_messages,
         COUNT(*) FILTER (WHERE is_user = false) as ai_messages,
         COUNT(*) FILTER (WHERE is_voice_message = true) as voice_messages,
         MIN(created_at) as first_message_at,
         MAX(created_at) as last_message_at
       FROM messages 
       WHERE conversation_id = $1`,
      [id]
    );

    res.json({
      success: true,
      data: {
        stats: statsResult.rows[0]
      }
    });
  } catch (error) {
    logger.error('Get conversation stats error:', error);
    res.status(500).json({
      success: false,
      error: error instanceof Error ? error.message : 'Failed to get conversation statistics'
    });
  }
});

export default router;