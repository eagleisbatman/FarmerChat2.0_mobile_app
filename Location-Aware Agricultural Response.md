# Location-Aware Agricultural Response System

## Project Overview
Build a Firebase-based system that captures user location, performs reverse geocoding, and enhances AI responses with location-specific agricultural context for personalized farming advice.

## Technology Stack
- **Backend**: Firebase (Cloud Functions, Firestore, Cloud Scheduler)
- **Mobile**: Android with Jetpack Compose + Location Services
- **External APIs**: Google Maps Geocoding, OpenWeatherMap, Agricultural Data APIs
- **AI Integration**: Multi-provider LLM support (Claude, Gemini, OpenAI)

## Core Requirements

### 1. Location Detection & Management

#### Android Location Service Implementation
```kotlin
@Singleton
class LocationService @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient,
    private val firestore: FirebaseFirestore,
    private val context: Context
) {
    private val LOCATION_THRESHOLD_METERS = 100f
    private val CACHE_DURATION_HOURS = 1
    
    suspend fun getCurrentLocationContext(userId: String): LocationContext? {
        return try {
            // Check if we need to update location
            val shouldUpdate = shouldUpdateLocation(userId)
            
            if (shouldUpdate) {
                val newLocation = getCurrentLocation()
                updateLocationContext(userId, newLocation)
            } else {
                getCachedLocationContext(userId)
            }
        } catch (e: Exception) {
            Log.e("LocationService", "Failed to get location", e)
            getCachedLocationContext(userId) // Fallback to cached
        }
    }
    
    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(): Location = suspendCoroutine { continuation ->
        if (!hasLocationPermission()) {
            continuation.resumeWithException(SecurityException("Location permission not granted"))
            return@suspendCoroutine
        }
        
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(5000)
            .setMaxUpdateDelayMillis(15000)
            .build()
        
        fusedLocationClient.getCurrentLocation(locationRequest.priority, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    continuation.resume(location)
                } else {
                    continuation.resumeWithException(Exception("Location is null"))
                }
            }
            .addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
    }
    
    private suspend fun shouldUpdateLocation(userId: String): Boolean {
        val userDoc = firestore.collection("users").document(userId).get().await()
        val userData = userDoc.data ?: return true
        
        val lastLocation = userData["location"] as? Map<String, Any>
        val lastUpdate = (userData["lastLocationUpdate"] as? Timestamp)?.toDate()
        
        // Update if no previous location or cache expired
        if (lastLocation == null || lastUpdate == null) return true
        
        val cacheExpired = System.currentTimeMillis() - lastUpdate.time > CACHE_DURATION_HOURS * 60 * 60 * 1000
        if (cacheExpired) return true
        
        // Update if moved significantly
        val currentLocation = getCurrentLocation()
        val lastLat = lastLocation["latitude"] as? Double ?: return true
        val lastLng = lastLocation["longitude"] as? Double ?: return true
        
        val distance = FloatArray(1)
        Location.distanceBetween(currentLocation.latitude, currentLocation.longitude, lastLat, lastLng, distance)
        
        return distance[0] > LOCATION_THRESHOLD_METERS
    }
    
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, 
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}

// Location Permission Handling in Compose
@Composable
fun LocationPermissionHandler(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )
    
    LaunchedEffect(locationPermissionState.status) {
        when (locationPermissionState.status) {
            is PermissionStatus.Granted -> onPermissionGranted()
            is PermissionStatus.Denied -> {
                if (locationPermissionState.status.shouldShowRationale) {
                    // Show rationale dialog
                } else {
                    onPermissionDenied()
                }
            }
        }
    }
    
    if (!locationPermissionState.status.isGranted) {
        LocationPermissionDialog(
            onRequestPermission = { locationPermissionState.launchPermissionRequest() },
            onDismiss = onPermissionDenied
        )
    }
}

@Composable
fun LocationPermissionDialog(
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Location Access Required") },
        text = { 
            Text("This app needs location access to provide accurate agricultural advice based on your area's climate, soil conditions, and local farming practices.")
        },
        confirmButton = {
            TextButton(onClick = onRequestPermission) {
                Text("Grant Permission")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Skip")
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    )
}
```

### 2. Firebase Backend - Reverse Geocoding & Context Enhancement

#### Cloud Functions for Location Processing
```javascript
// functions/locationService.js
const functions = require('firebase-functions');
const admin = require('firebase-admin');
const axios = require('axios');

class LocationContextService {
  constructor() {
    this.googleMapsApiKey = functions.config().google.maps_api_key;
    this.weatherApiKey = functions.config().openweather.api_key;
  }

  async enhanceLocationContext(latitude, longitude, userId) {
    try {
      // Parallel execution for better performance
      const [geocodingResult, weatherData, agriculturalData] = await Promise.all([
        this.reverseGeocode(latitude, longitude),
        this.getWeatherData(latitude, longitude),
        this.getAgriculturalContext(latitude, longitude)
      ]);

      const locationContext = {
        coordinates: { latitude, longitude },
        ...geocodingResult,
        weather: weatherData,
        agricultural: agriculturalData,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        userId
      };

      // Cache in Firestore for future use
      await this.cacheLocationContext(userId, locationContext);

      return locationContext;
    } catch (error) {
      console.error('Error enhancing location context:', error);
      throw error;
    }
  }

  async reverseGeocode(lat, lng) {
    try {
      const response = await axios.get(
        `https://maps.googleapis.com/maps/api/geocode/json?latlng=${lat},${lng}&key=${this.googleMapsApiKey}`
      );

      if (response.data.status !== 'OK' || !response.data.results.length) {
        throw new Error('Geocoding failed');
      }

      const result = response.data.results[0];
      return this.parseGeocodingResult(result);
    } catch (error) {
      console.error('Reverse geocoding error:', error);
      // Fallback to basic context
      return {
        formattedAddress: `${lat.toFixed(4)}, ${lng.toFixed(4)}`,
        city: 'Unknown',
        state: 'Unknown',
        country: 'Unknown',
        district: 'Unknown',
        pincode: 'Unknown'
      };
    }
  }

  parseGeocodingResult(result) {
    const components = result.address_components;
    
    return {
      formattedAddress: result.formatted_address,
      city: this.extractComponent(components, 'locality') || 
            this.extractComponent(components, 'administrative_area_level_2'),
      state: this.extractComponent(components, 'administrative_area_level_1'),
      country: this.extractComponent(components, 'country'),
      district: this.extractComponent(components, 'administrative_area_level_2'),
      pincode: this.extractComponent(components, 'postal_code'),
      placeId: result.place_id
    };
  }

  extractComponent(components, type) {
    const component = components.find(comp => comp.types.includes(type));
    return component ? component.long_name : null;
  }

  async getWeatherData(lat, lng) {
    try {
      const response = await axios.get(
        `https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lng}&appid=${this.weatherApiKey}&units=metric`
      );

      const data = response.data;
      return {
        temperature: data.main.temp,
        humidity: data.main.humidity,
        description: data.weather[0].description,
        condition: data.weather[0].main,
        windSpeed: data.wind.speed,
        pressure: data.main.pressure,
        visibility: data.visibility,
        feelsLike: data.main.feels_like,
        uvIndex: data.uvi || null
      };
    } catch (error) {
      console.error('Weather data error:', error);
      return null;
    }
  }

  async getAgriculturalContext(lat, lng) {
    try {
      // Get agricultural zone data from multiple sources
      const [soilData, cropData, climateData] = await Promise.all([
        this.getSoilData(lat, lng),
        this.getCropRecommendations(lat, lng),
        this.getClimateZoneData(lat, lng)
      ]);

      return {
        soilType: soilData?.type || 'Unknown',
        soilPh: soilData?.ph || null,
        climateZone: climateData?.zone || 'Unknown',
        recommendedCrops: cropData?.crops || [],
        growingSeasons: cropData?.seasons || {},
        averageRainfall: climateData?.rainfall || 'Unknown',
        lastUpdated: new Date().toISOString()
      };
    } catch (error) {
      console.error('Agricultural context error:', error);
      return {};
    }
  }

  async getSoilData(lat, lng) {
    // This would integrate with soil data APIs or your own database
    // For now, returning mock data structure
    return {
      type: 'Red Soil', // Example: Red Soil, Black Soil, Alluvial
      ph: 6.5,
      nutrients: {
        nitrogen: 'Medium',
        phosphorus: 'Low', 
        potassium: 'High'
      }
    };
  }

  async getCropRecommendations(lat, lng) {
    // Integrate with agricultural databases or APIs
    const db = admin.firestore();
    
    try {
      // Query your agricultural zones database
      const zonesSnapshot = await db.collection('agricultural_zones')
        .where('bounds', 'array-contains-any', [
          { lat: Math.floor(lat), lng: Math.floor(lng) }
        ])
        .limit(1)
        .get();

      if (!zonesSnapshot.empty) {
        const zoneData = zonesSnapshot.docs[0].data();
        return {
          crops: zoneData.recommendedCrops || [],
          seasons: zoneData.seasons || {}
        };
      }
    } catch (error) {
      console.error('Crop recommendation error:', error);
    }

    // Fallback to generic recommendations based on coordinates
    return this.getGenericCropRecommendations(lat, lng);
  }

  getGenericCropRecommendations(lat, lng) {
    // Basic recommendations based on latitude (climate zones)
    if (lat >= 8 && lat <= 37 && lng >= 68 && lng <= 97) { // India bounds
      return {
        crops: ['Rice', 'Wheat', 'Sugarcane', 'Cotton', 'Maize'],
        seasons: {
          kharif: { start: 'June', end: 'October', crops: ['Rice', 'Cotton', 'Sugarcane'] },
          rabi: { start: 'November', end: 'April', crops: ['Wheat', 'Barley', 'Mustard'] },
          zaid: { start: 'April', end: 'June', crops: ['Maize', 'Fodder'] }
        }
      };
    }
    
    return { crops: [], seasons: {} };
  }

  async cacheLocationContext(userId, locationContext) {
    const db = admin.firestore();
    await db.collection('users').doc(userId).update({
      location: locationContext,
      lastLocationUpdate: admin.firestore.FieldValue.serverTimestamp()
    });
  }
}

// Cloud Function for chat with location context
exports.generateLocationAwareResponse = functions.https.onCall(async (data, context) => {
  const { userId, userMessage, conversationId, latitude, longitude } = data;
  
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
  }

  try {
    const locationService = new LocationContextService();
    
    // Get enhanced location context
    const locationContext = await locationService.enhanceLocationContext(
      latitude, 
      longitude, 
      userId
    );
    
    // Build location-aware prompt
    const enhancedPrompt = buildLocationAwarePrompt(userMessage, locationContext);
    
    // Generate AI response using multi-provider service
    const llmService = new LLMService();
    const aiResponse = await llmService.generateResponse(enhancedPrompt);
    
    // Store conversation with location context
    await storeLocationAwareMessage(conversationId, userMessage, aiResponse, locationContext);
    
    return {
      response: aiResponse,
      locationContext: {
        city: locationContext.city,
        state: locationContext.state,
        weather: locationContext.weather
      }
    };
    
  } catch (error) {
    console.error('Location-aware response error:', error);
    throw new functions.https.HttpsError('internal', 'Failed to generate response');
  }
});

function buildLocationAwarePrompt(userMessage, locationContext) {
  const { city, state, country, weather, agricultural } = locationContext;
  
  return `You are an expert agricultural advisor providing location-specific farming advice.

LOCATION CONTEXT:
- Location: ${city}, ${state}, ${country}
- Current Weather: ${weather?.description || 'Unknown'} (${weather?.temperature || 'N/A'}°C)
- Humidity: ${weather?.humidity || 'N/A'}%
- Soil Type: ${agricultural?.soilType || 'Unknown'}
- Climate Zone: ${agricultural?.climateZone || 'Unknown'}
- Recommended Crops: ${agricultural?.recommendedCrops?.join(', ') || 'Unknown'}

CURRENT CONDITIONS:
- Temperature: ${weather?.temperature || 'N/A'}°C (feels like ${weather?.feelsLike || 'N/A'}°C)
- Wind Speed: ${weather?.windSpeed || 'N/A'} m/s
- Atmospheric Pressure: ${weather?.pressure || 'N/A'} hPa
- Visibility: ${weather?.visibility ? (weather.visibility / 1000).toFixed(1) + ' km' : 'N/A'}

AGRICULTURAL CONTEXT:
- Growing Seasons: ${JSON.stringify(agricultural?.growingSeasons || {})}
- Average Rainfall: ${agricultural?.averageRainfall || 'Unknown'}
- Soil pH: ${agricultural?.soilPh || 'Unknown'}

USER QUERY: ${userMessage}

INSTRUCTIONS:
1. Provide location-specific agricultural advice considering the above context
2. Include weather-based recommendations if relevant
3. Suggest crops suitable for the local climate and soil
4. Mention seasonal considerations for the area
5. Include local agricultural practices if known
6. Be practical and actionable in your advice
7. If weather conditions affect the advice, explicitly mention this

Generate a helpful, location-aware response:`;
}

async function storeLocationAwareMessage(conversationId, userMessage, aiResponse, locationContext) {
  const db = admin.firestore();
  
  const conversationRef = db.collection('conversations').doc(conversationId);
  const messagesRef = conversationRef.collection('messages');
  
  // Store user message
  await messagesRef.add({
    text: userMessage,
    sender: 'user',
    timestamp: admin.firestore.FieldValue.serverTimestamp(),
    locationContext: {
      coordinates: locationContext.coordinates,
      city: locationContext.city,
      state: locationContext.state,
      weather: locationContext.weather
    }
  });
  
  // Store AI response
  await messagesRef.add({
    text: aiResponse,
    sender: 'ai',
    timestamp: admin.firestore.FieldValue.serverTimestamp(),
    locationAware: true,
    locationContext: {
      coordinates: locationContext.coordinates,
      city: locationContext.city,
      state: locationContext.state,
      weather: locationContext.weather
    }
  });
  
  // Update conversation metadata
  await conversationRef.update({
    lastMessageTime: admin.firestore.FieldValue.serverTimestamp(),
    lastLocation: locationContext.city + ', ' + locationContext.state,
    isLocationAware: true
  });
}
```

### 3. Firestore Data Structure

```javascript
// users collection - Enhanced with location
{
  id: "user_123",
  location: {
    coordinates: { latitude: 12.9716, longitude: 77.5946 },
    formattedAddress: "Bangalore, Karnataka, India",
    city: "Bangalore",
    state: "Karnataka", 
    country: "India",
    district: "Bangalore Urban",
    pincode: "560001",
    placeId: "ChIJbU60yXAWrjsR4E9-UejD3_g",
    weather: {
      temperature: 28.5,
      humidity: 65,
      description: "partly cloudy",
      condition: "Clouds",
      windSpeed: 3.2,
      pressure: 1013,
      feelsLike: 31.2
    },
    agricultural: {
      soilType: "Red Soil",
      soilPh: 6.8,
      climateZone: "Tropical Savanna",
      recommendedCrops: ["Rice", "Ragi", "Sugarcane"],
      growingSeasons: {
        kharif: { start: "June", end: "October" },
        rabi: { start: "November", end: "April" }
      },
      averageRainfall: "970mm"
    },
    timestamp: timestamp
  },
  lastLocationUpdate: timestamp,
  locationPreferences: {
    autoUpdate: true,
    shareLocation: true,
    weatherAlerts: true,
    locationAccuracy: "high" // high, medium, low
  }
}

// conversations/{convId}/messages - Enhanced with location context
{
  text: "What crops should I plant in this weather?",
  sender: "user",
  timestamp: timestamp,
  locationContext: {
    coordinates: { latitude: 12.9716, longitude: 77.5946 },
    city: "Bangalore",
    state: "Karnataka",
    weather: {
      temperature: 28.5,
      condition: "Clouds",
      description: "partly cloudy"
    }
  },
  locationAware: true
}

// agricultural_zones collection - For crop recommendations
{
  id: "karnataka_bangalore_urban",
  state: "Karnataka",
  district: "Bangalore Urban",
  bounds: [
    { lat: 12.8, lng: 77.4 },
    { lat: 13.1, lng: 77.8 }
  ],
  climateZone: "Tropical Savanna",
  soilTypes: ["Red Soil", "Laterite Soil"],
  recommendedCrops: ["Rice", "Ragi", "Sugarcane", "Mulberry"],
  seasons: {
    kharif: { 
      start: "June", 
      end: "October",
      crops: ["Rice", "Ragi", "Maize"],
      rainfall: "650mm"
    },
    rabi: { 
      start: "November", 
      end: "April",
      crops: ["Wheat", "Barley", "Chickpea"],
      rainfall: "200mm"
    }
  },
  averageRainfall: "970mm",
  pestManagement: {
    commonPests: ["Brown Plant Hopper", "Stem Borer"],
    organicSolutions: ["Neem Oil", "Pheromone Traps"]
  },
  fertilizers: {
    organic: ["Compost", "Vermicompost"],
    chemical: ["Urea", "DAP", "MOP"]
  }
}
```

### 4. Android Chat Integration

#### Location-Aware Chat Compose UI
```kotlin
@Composable
fun LocationAwareChatScreen(
    viewModel: ChatViewModel,
    userId: String,
    conversationId: String
) {
    val locationService = remember { LocationService(LocalContext.current) }
    var locationContext by remember { mutableStateOf<LocationContext?>(null) }
    var showLocationIndicator by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        locationContext = locationService.getCurrentLocationContext(userId)
        showLocationIndicator = locationContext != null
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Location Context Header
        AnimatedVisibility(
            visible = showLocationIndicator && locationContext != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            LocationContextCard(
                locationContext = locationContext!!,
                onDismiss = { showLocationIndicator = false }
            )
        }
        
        // Chat Messages
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true
        ) {
            items(viewModel.messages) { message ->
                ChatMessageItem(
                    message = message,
                    showLocationBadge = message.locationAware
                )
            }
        }
        
        // Input with location toggle
        LocationAwareChatInput(
            onSendMessage = { text ->
                viewModel.sendLocationAwareMessage(
                    text = text,
                    userId = userId,
                    conversationId = conversationId,
                    locationContext = locationContext
                )
            },
            locationEnabled = locationContext != null,
            onLocationToggle = {
                // Toggle location sharing
            }
        )
    }
}

@Composable
fun LocationContextCard(
    locationContext: LocationContext,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Location-aware advice enabled",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${locationContext.city}, ${locationContext.state}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (locationContext.weather != null) {
                        Text(
                            text = "${locationContext.weather.temperature}°C, ${locationContext.weather.description}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
                
                if (locationContext.agricultural?.recommendedCrops?.isNotEmpty() == true) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(locationContext.agricultural.recommendedCrops.take(3)) { crop ->
                            AssistChip(
                                onClick = { },
                                label = { 
                                    Text(
                                        text = crop,
                                        style = MaterialTheme.typography.labelSmall
                                    ) 
                                },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocationAwareChatInput(
    onSendMessage: (String) -> Unit,
    locationEnabled: Boolean,
    onLocationToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf("") }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Location toggle row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (locationEnabled) Icons.Default.LocationOn else Icons.Default.LocationOff,
                        contentDescription = "Location status",
                        tint = if (locationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (locationEnabled) "Location-aware responses" else "Location disabled",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (locationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = locationEnabled,
                    onCheckedChange = { onLocationToggle() },
                    modifier = Modifier.scale(0.8f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Message input row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { 
                        Text(
                            if (locationEnabled) 
                                "Ask about farming in your area..." 
                            else 
                                "Type your message..."
                        ) 
                    },
                    modifier = Modifier.weight(1f),
                    maxLines = 3
                )
                
                FilledIconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            onSendMessage(messageText)
                            messageText = ""
                        }
                    },
                    enabled = messageText.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send message"
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    showLocationBadge: Boolean,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == "user"
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser && showLocationBadge) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                LocationAwareBadge()
                Spacer(modifier = Modifier.height(4.dp))
                MessageBubble(message = message, isUser = false)
            }
        } else {
            MessageBubble(message = message, isUser = isUser)
        }
    }
}

@Composable
fun LocationAwareBadge() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        modifier = Modifier.wrapContentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location aware",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "Location-aware",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

// Data classes
data class LocationContext(
    val coordinates: Coordinates,
    val city: String,
    val state: String,
    val country: String,
    val weather: WeatherData?,
    val agricultural: AgriculturalData?
)

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

data class WeatherData(
    val temperature: Double,
    val humidity: Int,
    val description: String,
    val condition: String,
    val windSpeed: Double
)

data class AgriculturalData(
    val soilType: String,
    val climateZone: String,
    val recommendedCrops: List<String>,
    val averageRainfall: String
)
```

### 5. Location History & Geofencing Features

#### Location History Tracking
```kotlin
// Android Location History Service
@Singleton
class LocationHistoryService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val locationService: LocationService
) {
    
    suspend fun trackLocationHistory(userId: String, locationContext: LocationContext) {
        val historyEntry = LocationHistoryEntry(
            userId = userId,
            locationContext = locationContext,
            timestamp = System.currentTimeMillis(),
            activities = extractLocationActivities(locationContext)
        )
        
        firestore.collection("users")
            .document(userId)
            .collection("location_history")
            .add(historyEntry.toMap())
    }
    
    suspend fun getLocationHistory(
        userId: String, 
        days: Int = 30
    ): List<LocationHistoryEntry> {
        val startTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000)
        
        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("location_history")
            .whereGreaterThan("timestamp", startTime)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()
        
        return snapshot.documents.mapNotNull { doc ->
            LocationHistoryEntry.fromMap(doc.data ?: return@mapNotNull null)
        }
    }
    
    suspend fun getLocationTrends(userId: String): LocationTrends {
        val history = getLocationHistory(userId, 90) // Last 3 months
        
        return LocationTrends(
            mostVisitedLocations = getMostVisitedLocations(history),
            seasonalPatterns = getSeasonalPatterns(history),
            cropRecommendationTrends = getCropTrends(history),
            weatherPatterns = getWeatherPatterns(history)
        )
    }
    
    private fun extractLocationActivities(locationContext: LocationContext): List<String> {
        val activities = mutableListOf<String>()
        
        // Infer activities based on location and weather
        locationContext.weather?.let { weather ->
            when {
                weather.temperature > 35 -> activities.add("hot_weather_farming")
                weather.temperature < 10 -> activities.add("cold_weather_protection")
                weather.description.contains("rain") -> activities.add("monsoon_farming")
                weather.humidity > 80 -> activities.add("high_humidity_management")
            }
        }
        
        locationContext.agricultural?.let { agri ->
            if (agri.recommendedCrops.isNotEmpty()) {
                activities.add("crop_planning_${agri.recommendedCrops.first().lowercase()}")
            }
        }
        
        return activities
    }
}

// Geofencing Service
@Singleton
class GeofencingService @Inject constructor(
    private val geofencingClient: GeofencingClient,
    private val firestore: FirebaseFirestore,
    private val notificationService: NotificationService
) {
    
    fun setupAgriculturalGeofences(userId: String, userLocations: List<LocationContext>) {
        val geofences = mutableListOf<Geofence>()
        
        userLocations.forEach { location ->
            // Create geofence for each agricultural zone
            val geofence = Geofence.Builder()
                .setRequestId("agri_zone_${location.coordinates.latitude}_${location.coordinates.longitude}")
                .setCircularRegion(
                    location.coordinates.latitude,
                    location.coordinates.longitude,
                    1000f // 1km radius
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(
                    Geofence.GEOFENCE_TRANSITION_ENTER or 
                    Geofence.GEOFENCE_TRANSITION_EXIT
                )
                .build()
            
            geofences.add(geofence)
        }
        
        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofences)
            .build()
        
        val geofencePendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, GeofenceBroadcastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED) {
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
        }
    }
    
    suspend fun handleGeofenceTransition(
        geofenceTransition: Int,
        triggeringGeofences: List<Geofence>,
        userId: String
    ) {
        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                triggeringGeofences.forEach { geofence ->
                    handleZoneEntry(geofence.requestId, userId)
                }
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                triggeringGeofences.forEach { geofence ->
                    handleZoneExit(geofence.requestId, userId)
                }
            }
        }
    }
    
    private suspend fun handleZoneEntry(geofenceId: String, userId: String) {
        // Extract coordinates from geofence ID
        val coordinates = parseGeofenceId(geofenceId)
        
        // Get zone-specific information
        val zoneInfo = getAgriculturalZoneInfo(coordinates)
        
        // Send contextual notification
        val notification = createZoneEntryNotification(zoneInfo)
        notificationService.sendNotification(userId, notification)
        
        // Log zone entry in history
        logZoneTransition(userId, geofenceId, "ENTER", zoneInfo)
    }
    
    private suspend fun handleZoneExit(geofenceId: String, userId: String) {
        val coordinates = parseGeofenceId(geofenceId)
        val zoneInfo = getAgriculturalZoneInfo(coordinates)
        
        // Send exit summary if user spent significant time
        val entryTime = getLastEntryTime(userId, geofenceId)
        val timeSpent = System.currentTimeMillis() - entryTime
        
        if (timeSpent > 30 * 60 * 1000) { // 30 minutes
            val summaryNotification = createZoneExitSummary(zoneInfo, timeSpent)
            notificationService.sendNotification(userId, summaryNotification)
        }
        
        logZoneTransition(userId, geofenceId, "EXIT", zoneInfo)
    }
}

// Geofence Broadcast Receiver
class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        
        if (geofencingEvent.hasError()) {
            Log.e("GeofenceReceiver", "Geofencing error: ${geofencingEvent.errorCode}")
            return
        }
        
        val geofenceTransition = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return
        
        // Handle in background service
        val serviceIntent = Intent(context, GeofenceHandlerService::class.java).apply {
            putExtra("transition", geofenceTransition)
            putExtra("geofences", ArrayList(triggeringGeofences.map { it.requestId }))
        }
        
        context.startService(serviceIntent)
    }
}
```

#### Location History UI Components
```kotlin
@Composable
fun LocationHistoryScreen(
    viewModel: LocationHistoryViewModel,
    modifier: Modifier = Modifier
) {
    val locationHistory by viewModel.locationHistory.collectAsState()
    val locationTrends by viewModel.locationTrends.collectAsState()
    var selectedTimeRange by remember { mutableStateOf(TimeRange.MONTH) }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            LocationTrendsCard(
                trends = locationTrends,
                timeRange = selectedTimeRange,
                onTimeRangeChange = { selectedTimeRange = it }
            )
        }
        
        item {
            Text(
                text = "Location History",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(locationHistory) { historyEntry ->
            LocationHistoryItem(
                historyEntry = historyEntry,
                onClick = { viewModel.showLocationDetails(historyEntry) }
            )
        }
    }
}

@Composable
fun LocationTrendsCard(
    trends: LocationTrends?,
    timeRange: TimeRange,
    onTimeRangeChange: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Farming Insights",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                FilterChip(
                    selected = true,
                    onClick = { /* Show time range picker */ },
                    label = { Text(timeRange.displayName) }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (trends != null) {
                // Most visited locations
                if (trends.mostVisitedLocations.isNotEmpty()) {
                    LocationTrendSection(
                        title = "Most Visited Areas",
                        icon = Icons.Default.LocationOn,
                        content = {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(trends.mostVisitedLocations.take(3)) { location ->
                                    LocationChip(
                                        location = location.name,
                                        visitCount = location.count
                                    )
                                }
                            }
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Crop recommendations trend
                if (trends.cropRecommendationTrends.isNotEmpty()) {
                    LocationTrendSection(
                        title = "Recommended Crops",
                        icon = Icons.Default.Agriculture,
                        content = {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(trends.cropRecommendationTrends.take(4)) { crop ->
                                    CropTrendChip(crop = crop)
                                }
                            }
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Weather patterns
                WeatherPatternChart(
                    patterns = trends.weatherPatterns,
                    modifier = Modifier.height(120.dp)
                )
            }
        }
    }
}

@Composable
fun LocationHistoryItem(
    historyEntry: LocationHistoryEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${historyEntry.locationContext.city}, ${historyEntry.locationContext.state}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatDateTime(historyEntry.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                historyEntry.locationContext.weather?.let { weather ->
                    WeatherIcon(
                        condition = weather.condition,
                        temperature = weather.temperature.toInt()
                    )
                }
            }
            
            if (historyEntry.activities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(historyEntry.activities.take(3)) { activity ->
                        ActivityChip(activity = activity)
                    }
                }
            }
            
            if (historyEntry.locationContext.agricultural?.recommendedCrops?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Recommended: ${historyEntry.locationContext.agricultural.recommendedCrops.take(2).joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun GeofenceNotificationCard(
    zoneInfo: AgriculturalZoneInfo,
    transitionType: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (transitionType == "ENTER") 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (transitionType == "ENTER") 
                        Icons.Default.Login 
                    else 
                        Icons.Default.Logout,
                    contentDescription = transitionType,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = if (transitionType == "ENTER") 
                        "Entered agricultural zone" 
                    else 
                        "Left agricultural zone",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${zoneInfo.name} - ${zoneInfo.climateZone}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (zoneInfo.recommendedCrops.isNotEmpty()) {
                Text(
                    text = "Recommended crops: ${zoneInfo.recommendedCrops.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (transitionType == "ENTER") {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { /* Open local weather */ },
                        label = { Text("Local Weather") },
                        leadingIcon = {
                            Icon(Icons.Default.WbSunny, contentDescription = null)
                        }
                    )
                    
                    AssistChip(
                        onClick = { /* Show crop calendar */ },
                        label = { Text("Crop Calendar") },
                        leadingIcon = {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        }
                    )
                }
            }
        }
    }
}

// Data classes for location history
data class LocationHistoryEntry(
    val userId: String,
    val locationContext: LocationContext,
    val timestamp: Long,
    val activities: List<String>
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "locationContext" to locationContext.toMap(),
            "timestamp" to timestamp,
            "activities" to activities
        )
    }
    
    companion object {
        fun fromMap(data: Map<String, Any>): LocationHistoryEntry? {
            return try {
                LocationHistoryEntry(
                    userId = data["userId"] as String,
                    locationContext = LocationContext.fromMap(data["locationContext"] as Map<String, Any>),
                    timestamp = data["timestamp"] as Long,
                    activities = (data["activities"] as List<*>).map { it.toString() }
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

data class LocationTrends(
    val mostVisitedLocations: List<LocationVisit>,
    val seasonalPatterns: Map<String, List<String>>,
    val cropRecommendationTrends: List<CropTrend>,
    val weatherPatterns: List<WeatherPattern>
)

data class LocationVisit(
    val name: String,
    val count: Int,
    val coordinates: Coordinates
)

data class CropTrend(
    val cropName: String,
    val frequency: Int,
    val locations: List<String>
)

data class WeatherPattern(
    val date: String,
    val averageTemp: Double,
    val rainfall: Double,
    val humidity: Int
)

data class AgriculturalZoneInfo(
    val name: String,
    val climateZone: String,
    val recommendedCrops: List<String>,
    val soilType: String,
    val coordinates: Coordinates
)

enum class TimeRange(val displayName: String, val days: Int) {
    WEEK("This Week", 7),
    MONTH("This Month", 30),
    QUARTER("3 Months", 90),
    YEAR("This Year", 365)
}
```

#### Firebase Backend for Location History & Geofencing
```javascript
// Cloud Functions for location history and geofencing
exports.processLocationHistory = functions.firestore
  .document('users/{userId}/location_history/{historyId}')
  .onCreate(async (snap, context) => {
    const historyData = snap.data();
    const userId = context.params.userId;
    
    // Extract insights from location history
    const insights = await generateLocationInsights(historyData);
    
    // Update user's location trends
    await updateLocationTrends(userId, insights);
    
    // Check for geofence setup
    await setupUserGeofences(userId, historyData.locationContext);
  });

exports.handleGeofenceTransition = functions.https.onCall(async (data, context) => {
  const { userId, geofenceId, transitionType, timestamp } = data;
  
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
  }
  
  try {
    // Log the transition
    await admin.firestore()
      .collection('users')
      .doc(userId)
      .collection('geofence_transitions')
      .add({
        geofenceId,
        transitionType,
        timestamp: timestamp || admin.firestore.FieldValue.serverTimestamp()
      });
    
    // Generate contextual response based on zone entry/exit
    if (transitionType === 'ENTER') {
      await handleZoneEntry(userId, geofenceId);
    } else if (transitionType === 'EXIT') {
      await handleZoneExit(userId, geofenceId);
    }
    
    return { success: true };
  } catch (error) {
    console.error('Geofence transition error:', error);
    throw new functions.https.HttpsError('internal', 'Failed to process geofence transition');
  }
});

async function generateLocationInsights(historyData) {
  const location = historyData.locationContext;
  const weather = location.weather;
  const agricultural = location.agricultural;
  
  const insights = {
    weatherTrend: categorizeWeather(weather),
    cropOpportunities: agricultural?.recommendedCrops || [],
    seasonalAdvice: getSeasonalAdvice(location.state, new Date()),
    riskFactors: identifyRiskFactors(weather, agricultural)
  };
  
  return insights;
}

async function setupUserGeofences(userId, locationContext) {
  const db = admin.firestore();
  
  // Create geofence configuration for the user
  const geofenceConfig = {
    userId,
    center: {
      latitude: locationContext.coordinates.latitude,
      longitude: locationContext.coordinates.longitude
    },
    radius: 1000, // 1km
    agriculturalZone: {
      climateZone: locationContext.agricultural?.climateZone,
      soilType: locationContext.agricultural?.soilType,
      recommendedCrops: locationContext.agricultural?.recommendedCrops || []
    },
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    isActive: true
  };
  
  await db.collection('geofences').add(geofenceConfig);
}

function categorizeWeather(weather) {
  if (!weather) return 'unknown';
  
  const temp = weather.temperature;
  const humidity = weather.humidity;
  
  if (temp > 35) return 'hot';
  if (temp < 10) return 'cold';
  if (humidity > 80) return 'humid';
  if (weather.description.includes('rain')) return 'rainy';
  
  return 'moderate';
}

function getSeasonalAdvice(state, currentDate) {
  const month = currentDate.getMonth() + 1; // 1-12
  
  // Simplified seasonal advice for India
  if (month >= 6 && month <= 9) {
    return 'Kharif season - Good time for rice, cotton, sugarcane';
  } else if (month >= 10 && month <= 3) {
    return 'Rabi season - Suitable for wheat, barley, peas';
  } else {
    return 'Zaid season - Consider summer crops like maize, fodder';
  }
}
```

#### Smart Caching and API Usage
```javascript
// Implement intelligent caching to reduce API calls
class CostOptimizedLocationService extends LocationContextService {
  constructor() {
    super();
    this.geocodingCache = new Map();
    this.weatherCache = new Map();
  }

  async reverseGeocode(lat, lng) {
    // Round coordinates to reduce unique cache keys
    const roundedLat = Math.round(lat * 1000) / 1000;
    const roundedLng = Math.round(lng * 1000) / 1000;
    const cacheKey = `${roundedLat}_${roundedLng}`;
    
    if (this.geocodingCache.has(cacheKey)) {
      return this.geocodingCache.get(cacheKey);
    }
    
    const result = await super.reverseGeocode(lat, lng);
    this.geocodingCache.set(cacheKey, result);
    
    return result;
  }

  async getWeatherData(lat, lng) {
    const roundedLat = Math.round(lat * 100) / 100; // Less precision for weather
    const roundedLng = Math.round(lng * 100) / 100;
    const hour = Math.floor(Date.now() / (1000 * 60 * 60)); // Cache per hour
    const cacheKey = `${roundedLat}_${roundedLng}_${hour}`;
    
    if (this.weatherCache.has(cacheKey)) {
      return this.weatherCache.get(cacheKey);
    }
    
    const result = await super.getWeatherData(lat, lng);
    this.weatherCache.set(cacheKey, result);
    
    return result;
  }
}
```

## Expected Outcomes
- **Highly relevant agricultural advice** based on user's exact location
- **Weather-aware recommendations** for optimal farming decisions
- **Reduced API costs** through intelligent caching and optimization
- **Seamless user experience** with automatic location detection
- **Privacy-compliant** location handling with user consent

## Success Metrics
- **Response relevance**: 90%+ location-appropriate advice
- **API cost efficiency**: <$50/month for 10k active users
- **User engagement**: 40% increase in follow-up questions
- **Location accuracy**: Sub-100m precision for rural areas