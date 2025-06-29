import express, { Response, NextFunction } from 'express';
import { AIService, ChatRequest } from '../services/ai.service';
import { AuthRequest, authenticate } from '../middleware/auth';
import { AppError } from '../middleware/errorHandler';
import { logger } from '../utils/logger';
import { query } from '../database';
import multer from 'multer';

const router = express.Router();
const aiService = new AIService();

// Configure multer for audio file uploads
const upload = multer({
  storage: multer.memoryStorage(),
  limits: {
    fileSize: 25 * 1024 * 1024, // 25MB limit for audio files
  },
  fileFilter: (req, file, cb) => {
    // Accept audio files
    if (file.mimetype.startsWith('audio/')) {
      cb(null, true);
    } else {
      cb(new Error('Only audio files are allowed'));
    }
  },
});

// Send chat message
router.post('/send', authenticate, async (req: AuthRequest, res) => {
  try {
    const { message, conversationId } = req.body;
    
    if (!message || !conversationId) {
      throw new AppError('Message and conversationId are required', 400);
    }

    // Get user profile for context
    const userProfile = req.user;
    
    const chatRequest: ChatRequest = {
      message,
      conversationId,
      userId: req.userId!,
      userProfile,
    };

    // Generate AI response
    const response = await aiService.generateResponse(chatRequest);
    
    // Extract follow-up questions
    const followUpQuestions = await aiService.extractFollowUpQuestions(
      response.content,
      userProfile?.language || 'en',
      userProfile
    );

    // Generate title if this is the first message in conversation
    const messageCount = await query(
      'SELECT COUNT(*) as count FROM messages WHERE conversation_id = $1',
      [conversationId]
    );
    
    let title;
    if (messageCount.rows[0].count <= 2) { // User message + AI response
      title = await aiService.generateConversationTitle(
        message,
        response.content,
        userProfile?.language || 'en'
      );
      
      // Update conversation title
      await query(
        'UPDATE conversations SET title = $1 WHERE id = $2',
        [title, conversationId]
      );
    }

    res.json({
      success: true,
      data: {
        response: response.content,
        followUpQuestions,
        title,
        usage: response.usage
      }
    });
  } catch (error) {
    logger.error('Chat send error:', error);
    res.status(500).json({
      success: false,
      error: error instanceof Error ? error.message : 'Failed to send message'
    });
  }
});

// Get chat history
router.get('/:conversationId/messages', authenticate, async (req: AuthRequest, res) => {
  try {
    const { conversationId } = req.params;
    const { limit = 50, offset = 0 } = req.query;

    // Verify user owns this conversation
    const conversationResult = await query(
      'SELECT user_id FROM conversations WHERE id = $1',
      [conversationId]
    );

    if (conversationResult.rows.length === 0) {
      throw new AppError('Conversation not found', 404);
    }

    if (conversationResult.rows[0].user_id !== req.userId) {
      throw new AppError('Access denied', 403);
    }

    // Get messages
    const messagesResult = await query(
      `SELECT id, content, is_user, audio_url, is_voice_message, follow_up_questions, created_at 
       FROM messages 
       WHERE conversation_id = $1 
       ORDER BY created_at ASC 
       LIMIT $2 OFFSET $3`,
      [conversationId, limit, offset]
    );

    // Transform follow_up_questions from string[] to object[]
    const messages = messagesResult.rows.map(message => ({
      ...message,
      follow_up_questions: (message.follow_up_questions || []).map((q: string, idx: number) => ({
        id: `fq_${message.id}_${idx}`,
        question: q
      }))
    }));

    res.json({
      success: true,
      data: messages  // API expects data to be the array directly
    });
  } catch (error) {
    logger.error('Chat history error:', error);
    res.status(500).json({
      success: false,
      error: error instanceof Error ? error.message : 'Failed to get chat history'
    });
  }
});

// Generate starter questions
router.post('/starter-questions', authenticate, async (req: AuthRequest, res) => {
  try {
    const userProfile = req.user;
    const { languageCode = userProfile?.language || 'en' } = req.body;

    // Use prompt service for starter question generation
    const { PromptService } = await import('../services/prompt.service');
    const promptService = new PromptService();
    
    const prompt = await promptService.getStarterQuestionPrompt(
      userProfile,
      languageCode
    );

    // Generate questions using AI
    const response = await aiService.getProvider().generateResponse([
      { role: 'user', content: prompt }
    ]);

    const questions = response.content
      .split('\n')
      .filter(q => q.trim())
      .slice(0, 4)
      .map((question, index) => ({
        id: `starter-${index + 1}`,
        question: question.trim()
      }));

    res.json({
      success: true,
      data: questions  // This is already an array of objects
    });
  } catch (error) {
    logger.error('Starter questions error:', error);
    res.status(500).json({
      success: false,
      error: error instanceof Error ? error.message : 'Failed to generate starter questions'
    });
  }
});

// Rate a response
router.post('/messages/:messageId/rate', authenticate, async (req: AuthRequest, res) => {
  try {
    const { messageId } = req.params;
    const { rating, feedback } = req.body;
    
    if (!messageId || rating === undefined) {
      throw new AppError('Message ID and rating are required', 400);
    }

    // Verify message belongs to user
    const messageResult = await query(
      `SELECT m.id FROM messages m 
       JOIN conversations c ON m.conversation_id = c.id 
       WHERE m.id = $1 AND c.user_id = $2`,
      [messageId, req.userId]
    );

    if (messageResult.rows.length === 0) {
      throw new AppError('Message not found or access denied', 404);
    }

    // Save feedback
    await query(
      `INSERT INTO feedback (user_id, message_id, rating, feedback_text, type)
       VALUES ($1, $2, $3, $4, $5)`,
      [req.userId, messageId, rating, feedback, 'response_rating']
    );

    res.json({
      success: true,
      message: 'Rating saved successfully'
    });
  } catch (error) {
    logger.error('Rate response error:', error);
    res.status(500).json({
      success: false,
      error: error instanceof Error ? error.message : 'Failed to save rating'
    });
  }
});

// Generate follow-up questions for a message
router.post('/generate-followup', authenticate, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const { message, language } = req.body;
    
    if (!message) {
      throw new AppError('Message is required', 400);
    }
    
    if (!req.user) {
      throw new AppError('User not found', 404);
    }
    
    const userLanguage = language || req.user.language || 'en';
    
    const questions = await aiService.extractFollowUpQuestions(
      message,
      userLanguage,
      req.user
    );
    
    res.json({
      success: true,
      data: questions
    });
  } catch (error) {
    next(error);
  }
});

// Transcribe audio
router.post('/transcribe', authenticate, upload.single('audio'), async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    if (!req.file) {
      throw new AppError('Audio file is required', 400);
    }
    
    const { language } = req.body;
    const audioBuffer = req.file.buffer;
    
    // Get user language if not specified
    const userLanguage = language || req.user?.profile?.language || 'en';
    
    // Transcribe audio using OpenAI Whisper
    const transcription = await aiService.transcribeAudio(audioBuffer, userLanguage);
    
    res.json({
      success: true,
      data: {
        transcription,
        language: userLanguage
      }
    });
  } catch (error) {
    logger.error('Audio transcription error:', error);
    next(error);
  }
});

export default router;