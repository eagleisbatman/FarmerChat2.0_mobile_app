# Android Build Fix Summary

## Current Issues:
1. **MigrationManager** - References non-existent repositories
2. **HybridViewModels** - Complex migration logic with type inference issues
3. **FarmerChatRepository** - Still references Firestore directly

## Solution:
Since there are no existing users, we can:
1. Use AppRepository (API-based) directly
2. Skip migration complexity
3. Comment out or remove hybrid ViewModels
4. Use simplified chat and conversation ViewModels

## Quick Fix Steps:
1. Disable MigrationManager
2. Use ApiChatViewModel and ApiConversationsViewModel directly
3. Update navigation to use API-based ViewModels