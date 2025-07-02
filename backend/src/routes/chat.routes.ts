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
    const { message, conversationId, language } = req.body;
    
    if (!message || !conversationId) {
      throw new AppError('Message and conversationId are required', 400);
    }

    // Get user profile for context
    const userProfile = req.user;
    
    // Use language from request if provided, otherwise fall back to user profile
    const requestLanguage = language || userProfile?.language || 'en';
    
    const chatRequest: ChatRequest = {
      message,
      conversationId,
      userId: req.userId!,
      userProfile: {
        ...userProfile,
        language: requestLanguage
      },
    };

    // Generate AI response
    const response = await aiService.generateResponse(chatRequest);
    
    // Extract follow-up questions
    const followUpQuestions = await aiService.extractFollowUpQuestions(
      response.content,
      requestLanguage,
      {
        ...userProfile,
        language: requestLanguage
      }
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
        requestLanguage
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
    // Accept both 'language' and 'languageCode' for flexibility
    const { languageCode, language } = req.body;
    const selectedLanguage = languageCode || language || userProfile?.language || 'en';
    
    // Debug logging to trace language issue
    logger.info('Starter questions request:', {
      bodyContent: req.body,
      userLanguage: userProfile?.language,
      selectedLanguage: selectedLanguage,
      headers: req.headers
    });

    // Use prompt service for starter question generation
    const { PromptService } = await import('../services/prompt.service');
    const promptService = new PromptService();
    
    const prompt = await promptService.getStarterQuestionPrompt(
      userProfile,
      selectedLanguage
    );
    
    // Add language-specific system prompt for better compliance
    const systemPrompt = `You are an AI that MUST generate responses ONLY in ${selectedLanguage === 'hi' ? 'Hindi (हिंदी)' : selectedLanguage === 'sw' ? 'Swahili' : selectedLanguage} language. 
DO NOT use any English words except technical terms with no translation.
The user's selected language is ${selectedLanguage}.`;

    logger.info('Generating starter questions with prompt:', {
      systemPrompt: systemPrompt.substring(0, 100) + '...',
      userPrompt: prompt.substring(0, 100) + '...',
      language: selectedLanguage
    });

    // Generate questions using AI with system prompt
    const response = await aiService.getProvider().generateResponse([
      { role: 'system', content: systemPrompt },
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
    const userLanguage = language || req.user?.language || 'en';
    
    logger.info('=== BACKEND TRANSCRIPTION START ===');
    logger.info('Transcription request details:', {
      fileSize: audioBuffer.length,
      fileSizeKB: (audioBuffer.length / 1024).toFixed(2) + ' KB',
      mimeType: req.file.mimetype,
      originalName: req.file.originalname,
      language: userLanguage,
      userId: req.userId,
      userLanguage: req.user?.language,
      hasAuthHeader: !!req.headers.authorization
    });
    
    // Log first few bytes to verify it's an audio file
    const audioHeader = audioBuffer.slice(0, 20).toString('hex');
    logger.info('Audio file header (hex):', audioHeader);
    
    try {
      // Transcribe audio using OpenAI Whisper
      logger.info('Calling AI service transcribeAudio...');
      const transcription = await aiService.transcribeAudio(audioBuffer, userLanguage);
      
      logger.info('=== TRANSCRIPTION SUCCESS ===');
      logger.info('Transcription result:', {
        text: transcription,
        length: transcription.length,
        language: userLanguage
      });
      
      res.json({
        success: true,
        data: {
          transcription,
          language: userLanguage
        }
      });
    } catch (transcribeError: any) {
      logger.error('=== TRANSCRIPTION ERROR IN TRY BLOCK ===', {
        error: transcribeError.message,
        stack: transcribeError.stack
      });
      throw transcribeError;
    }
  } catch (error: any) {
    logger.error('Audio transcription error:', error);
    
    // Parse custom error types
    if (error.message?.includes(':')) {
      const [errorType, errorMessage] = error.message.split(':', 2);
      
      const errorResponses: { [key: string]: { status: number; userMessage: string } } = {
        'LANGUAGE_MISMATCH': {
          status: 422,
          userMessage: `Please speak in ${req.user?.language === 'hi' ? 'हिंदी' : req.user?.language === 'sw' ? 'Kiswahili' : 'the selected language'}. ${errorMessage}`
        },
        'EMPTY_TRANSCRIPTION': {
          status: 422,
          userMessage: 'Could not hear clearly. Please speak louder and try again.'
        },
        'TOO_SHORT': {
          status: 422,
          userMessage: 'Message too short. Please speak a complete sentence.'
        },
        'AUDIO_TOO_LARGE': {
          status: 413,
          userMessage: 'Recording is too long. Please record a shorter message (max 2 minutes).'
        },
        'INVALID_AUDIO': {
          status: 400,
          userMessage: 'Audio format not supported. Please try recording again.'
        },
        'TRANSCRIPTION_FAILED': {
          status: 500,
          userMessage: 'Could not process audio. Please try again.'
        }
      };
      
      const errorResponse = errorResponses[errorType] || {
        status: 500,
        userMessage: 'Transcription failed. Please try again.'
      };
      
      res.status(errorResponse.status).json({
        success: false,
        error: errorResponse.userMessage,
        errorType: errorType
      });
      return; // Ensure function returns void
    }
    
    // Default error response
    res.status(500).json({
      success: false,
      error: 'Failed to transcribe audio. Please try again.',
      errorType: 'UNKNOWN'
    });
  }
});

export default router;