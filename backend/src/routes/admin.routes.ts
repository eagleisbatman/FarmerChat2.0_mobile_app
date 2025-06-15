import { Router } from 'express';

const router = Router();

// Admin routes will be implemented here
router.get('/stats', (req, res) => {
  res.status(501).json({ message: 'Not implemented yet' });
});

export default router;