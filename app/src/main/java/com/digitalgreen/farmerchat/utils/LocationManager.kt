package com.digitalgreen.farmerchat.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.digitalgreen.farmerchat.data.LocationInfo
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationManager(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    
    private val geocoder = Geocoder(context, Locale.getDefault())
    
    // Check if location permissions are granted
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    // Get current location
    suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission()) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        
        try {
            val cancellationTokenSource = CancellationTokenSource()
            
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                continuation.resume(location)
            }.addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
            
            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        } catch (e: SecurityException) {
            continuation.resumeWithException(e)
        }
    }
    
    // Reverse geocode location to get detailed address
    suspend fun reverseGeocode(latitude: Double, longitude: Double): LocationInfo? {
        return withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Use async geocoding for Android 13+
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                            val locationInfo = addresses.firstOrNull()?.let { address ->
                                parseAddressToLocationInfo(address, latitude, longitude)
                            }
                            continuation.resume(locationInfo)
                        }
                    }
                } else {
                    // Use synchronous geocoding for older versions
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    addresses?.firstOrNull()?.let { address ->
                        parseAddressToLocationInfo(address, latitude, longitude)
                    }
                }
            } catch (e: Exception) {
                // Return basic location info if geocoding fails
                LocationInfo(
                    latitude = latitude,
                    longitude = longitude,
                    formattedAddress = "Lat: $latitude, Lon: $longitude",
                    timestamp = Date()
                )
            }
        }
    }
    
    // Parse Android Address object to our LocationInfo
    private fun parseAddressToLocationInfo(
        address: android.location.Address,
        latitude: Double,
        longitude: Double
    ): LocationInfo {
        return LocationInfo(
            latitude = latitude,
            longitude = longitude,
            country = address.countryName ?: "",
            countryCode = address.countryCode ?: "",
            regionLevel1 = address.adminArea ?: "", // State/Province
            regionLevel2 = address.subAdminArea ?: "", // County/District
            regionLevel3 = address.locality ?: "", // City/Town
            regionLevel4 = address.subLocality ?: "", // Neighborhood
            regionLevel5 = address.featureName ?: "", // Street/Building
            city = address.locality ?: address.subAdminArea ?: "",
            postalCode = address.postalCode ?: "",
            formattedAddress = buildString {
                val components = listOfNotNull(
                    address.featureName,
                    address.thoroughfare,
                    address.subLocality,
                    address.locality,
                    address.subAdminArea,
                    address.adminArea,
                    address.countryName,
                    address.postalCode
                ).filter { it.isNotBlank() }.distinct()
                
                append(components.joinToString(", "))
            },
            timestamp = Date()
        )
    }
    
    // Get formatted location string for display
    fun getFormattedLocationString(locationInfo: LocationInfo): String {
        val components = mutableListOf<String>()
        
        // Add city or region level 3
        if (locationInfo.city.isNotBlank()) {
            components.add(locationInfo.city)
        } else if (locationInfo.regionLevel3.isNotBlank()) {
            components.add(locationInfo.regionLevel3)
        }
        
        // Add region level 1 (state/province)
        if (locationInfo.regionLevel1.isNotBlank()) {
            components.add(locationInfo.regionLevel1)
        }
        
        // Add country
        if (locationInfo.country.isNotBlank()) {
            components.add(locationInfo.country)
        }
        
        return components.joinToString(", ").ifEmpty { 
            "Location: ${locationInfo.latitude}, ${locationInfo.longitude}" 
        }
    }
    
    // Get hierarchical location context for AI prompts
    fun getLocationContext(locationInfo: LocationInfo): Map<String, String> {
        return mapOf(
            "country" to locationInfo.country,
            "countryCode" to locationInfo.countryCode,
            "state_province" to locationInfo.regionLevel1,
            "district_county" to locationInfo.regionLevel2,
            "city_locality" to locationInfo.regionLevel3,
            "sub_locality" to locationInfo.regionLevel4,
            "coordinates" to "${locationInfo.latitude},${locationInfo.longitude}"
        ).filterValues { it.isNotBlank() }
    }
}