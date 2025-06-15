import { Router } from 'express';

const router = Router();

// Prompt routes will be implemented here
router.get('/', (req, res) => {
  res.status(501).json({ message: 'Not implemented yet' });
});

export default router;