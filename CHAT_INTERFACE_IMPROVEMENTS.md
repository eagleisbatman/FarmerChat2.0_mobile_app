# Chat Interface Improvements Task List

## 1. Chat Bubble Alignment
### Current Issue:
- Bubbles are alternately aligned (user right, AI left)
- Takes up too much space
- Doesn't respect RTL languages

### Solution:
- **LTR Languages** (English, Hindi, Spanish, etc.): All bubbles left-aligned
- **RTL Languages** (Arabic, Hebrew, Urdu): All bubbles right-aligned
- Reduce bubble width to ~80% of screen width
- Add proper padding and margins

### Tasks:
- [ ] Detect RTL languages in ChatScreen
- [ ] Update MessageBubble composable to respect text direction
- [ ] Reduce bubble width and improve spacing
- [ ] Test with Arabic/Hebrew languages

## 2. Dynamic Starter Questions
### Current Issue:
- Static starter questions from database
- Not personalized based on user context
- Same questions appear repeatedly

### Solution:
- Generate fresh questions using AI based on:
  - User's location
  - Selected crops/livestock
  - Current season
  - Recent conversations
  - Time of day

### Tasks:
- [ ] Create StarterQuestionGenerator using Gemini AI
- [ ] Consider user context (location, crops, season)
- [ ] Refresh questions on each chat screen visit
- [ ] Limit to 3-5 concise questions

## 3. Navigation Flow Improvements
### Current Issue:
- Empty conversation list shown after onboarding
- No way to cancel onboarding reset

### Solution:
- Navigate directly to chat screen after onboarding
- Add cancel option to onboarding reset
- Improve empty state handling

### Tasks:
- [ ] Update navigation after onboarding completion
- [ ] Add AlertDialog for onboarding reset confirmation
- [ ] Skip empty conversation list for new users
- [ ] Add proper back navigation

## 4. Follow-up Questions
### Current Issue:
- Follow-up questions too lengthy
- Not always visible in conversations
- Missing in existing conversations

### Solution:
- Shorten follow-up questions (max 50 chars)
- Always show 2-3 relevant follow-ups
- Make them contextual and actionable

### Tasks:
- [ ] Update PromptManager to generate concise follow-ups
- [ ] Always show follow-up questions section
- [ ] Add character limit to follow-up generation
- [ ] Make follow-ups more action-oriented

## 5. Overall UX Improvements
### Additional Enhancements:
- [ ] Add typing indicator when AI is responding
- [ ] Improve loading states
- [ ] Add pull-to-refresh for starter questions
- [ ] Optimize chat bubble animations
- [ ] Add message timestamps (subtle)

## Priority Order:
1. Chat bubble alignment (Quick win, major UX improvement)
2. Navigation flow fixes (Improves first-time experience)
3. Follow-up questions (Enhances engagement)
4. Dynamic starter questions (Personalization)
5. Additional UX polish