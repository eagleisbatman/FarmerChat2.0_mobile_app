#!/bin/bash

# API Testing Script for FarmerChat Backend
API_URL="http://localhost:3000/api/v1"
PHONE="+1234567890"
FIREBASE_TOKEN=""
JWT_TOKEN=""

echo "🧪 Testing FarmerChat API Endpoints"
echo "=================================="

# 1. Test Health Endpoint
echo -e "\n1. Testing Health Endpoint..."
curl -s http://localhost:3000/health | jq .

# 2. Test Translation Languages
echo -e "\n2. Testing Translation Languages..."
curl -s $API_URL/translations/languages | jq .

# 3. Test Translations (English)
echo -e "\n3. Testing English Translations..."
curl -s $API_URL/translations/en | jq '. | {total: .total, sample: (.translations | to_entries | .[0:3])}'

# 4. Test Auth Endpoint (will fail without Firebase token)
echo -e "\n4. Testing Auth Endpoint (expected to fail without Firebase token)..."
curl -s -X POST $API_URL/auth/verify \
  -H "Content-Type: application/json" \
  -d '{"idToken": "test-token"}' | jq .

# 5. Test Unauthenticated Endpoints
echo -e "\n5. Testing Crop Translations..."
curl -s $API_URL/translations/crops/en | jq '. | {total: .total, sample: (.crops | to_entries | .[0:3])}'

echo -e "\n6. Testing Livestock Translations..."
curl -s $API_URL/translations/livestock/en | jq '. | {total: .total, sample: (.livestock | to_entries | .[0:3])}'

# 7. Test WebSocket Connection
echo -e "\n7. Testing WebSocket Support..."
curl -s -I http://localhost:3000/socket.io/ | grep -E "HTTP|upgrade"

echo -e "\n✅ Basic API Testing Complete!"
echo "=================================="
echo "Note: Authenticated endpoints require Firebase auth token"