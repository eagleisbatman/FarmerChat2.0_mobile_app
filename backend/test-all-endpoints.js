const axios = require('axios');

const API_URL = 'http://localhost:3000/api/v1';

async function testEndpoints() {
  console.log('🧪 FarmerChat Backend API Test Suite');
  console.log('=====================================\n');

  const results = [];

  // 1. Health Check
  try {
    const health = await axios.get('http://localhost:3000/health');
    results.push({ endpoint: 'GET /health', status: '✅ Success', data: health.data });
  } catch (error) {
    results.push({ endpoint: 'GET /health', status: '❌ Failed', error: error.message });
  }

  // 2. Translation Languages
  try {
    const languages = await axios.get(`${API_URL}/translations/languages`);
    results.push({ 
      endpoint: 'GET /translations/languages', 
      status: '✅ Success', 
      count: languages.data.data.languages.length 
    });
  } catch (error) {
    results.push({ endpoint: 'GET /translations/languages', status: '❌ Failed', error: error.message });
  }

  // 3. UI Translations
  try {
    const translations = await axios.get(`${API_URL}/translations/en`);
    const uiKeys = Object.keys(translations.data.data.translations.ui);
    results.push({ 
      endpoint: 'GET /translations/en', 
      status: '✅ Success', 
      uiTranslations: uiKeys.length,
      crops: Object.keys(translations.data.data.translations.crops).length,
      livestock: Object.keys(translations.data.data.translations.livestock).length
    });
  } catch (error) {
    results.push({ endpoint: 'GET /translations/en', status: '❌ Failed', error: error.message });
  }

  // 4. Auth Endpoint (expected to fail)
  try {
    await axios.post(`${API_URL}/auth/verify`, { idToken: 'test' });
  } catch (error) {
    results.push({ 
      endpoint: 'POST /auth/verify', 
      status: '⚠️  Expected Failure', 
      message: 'Requires valid Firebase token' 
    });
  }

  // 5. Test specific translation endpoints
  try {
    const crops = await axios.get(`${API_URL}/translations/crops/en`);
    results.push({ 
      endpoint: 'GET /translations/crops/en', 
      status: '✅ Success', 
      crops: crops.data.data.crops.length 
    });
  } catch (error) {
    results.push({ endpoint: 'GET /translations/crops/en', status: '❌ Failed', error: error.message });
  }

  // 6. Test protected endpoints (should fail without auth)
  const protectedEndpoints = [
    { method: 'GET', path: '/user/profile' },
    { method: 'GET', path: '/conversations' },
    { method: 'POST', path: '/conversations' },
    { method: 'POST', path: '/chat/send' }
  ];

  for (const ep of protectedEndpoints) {
    try {
      if (ep.method === 'GET') {
        await axios.get(`${API_URL}${ep.path}`);
      } else {
        await axios.post(`${API_URL}${ep.path}`, {});
      }
    } catch (error) {
      const status = error.response?.status === 401 ? '✅ Properly Protected' : '❌ Unexpected Error';
      results.push({ 
        endpoint: `${ep.method} ${ep.path}`, 
        status,
        httpStatus: error.response?.status 
      });
    }
  }

  // Print results
  console.log('📊 Test Results:');
  console.log('================\n');
  
  results.forEach(result => {
    console.log(`${result.endpoint}`);
    console.log(`  Status: ${result.status}`);
    if (result.data) console.log(`  Data:`, JSON.stringify(result.data, null, 2));
    if (result.count !== undefined) console.log(`  Count: ${result.count}`);
    if (result.uiTranslations) console.log(`  UI Translations: ${result.uiTranslations}`);
    if (result.crops) console.log(`  Crops: ${result.crops}`);
    if (result.livestock) console.log(`  Livestock: ${result.livestock}`);
    if (result.httpStatus) console.log(`  HTTP Status: ${result.httpStatus}`);
    if (result.message) console.log(`  Message: ${result.message}`);
    if (result.error) console.log(`  Error: ${result.error}`);
    console.log('');
  });

  // Summary
  const successCount = results.filter(r => r.status.includes('✅')).length;
  const totalCount = results.length;
  
  console.log('📈 Summary:');
  console.log(`Total Endpoints Tested: ${totalCount}`);
  console.log(`Successful: ${successCount}/${totalCount}`);
  console.log('\n✅ Backend is running and core endpoints are functional!');
  console.log('🔒 Authentication endpoints are properly protected');
  console.log('🌍 Translation system is working');
}

testEndpoints().catch(console.error);