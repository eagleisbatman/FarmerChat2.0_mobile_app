import { Router } from 'express';

const router = Router();

// User routes will be implemented here
router.get('/profile', (req, res) => {
  res.status(501).json({ message: 'Not implemented yet' });
});

export default router;