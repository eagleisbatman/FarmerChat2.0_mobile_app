
package com.digitalgreen.farmerchat.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// Data classes for onboarding
data class Location(val id: String, val name: String, val state: String)
data class Crop(val id: String, val name: String, val emoji: String)
data class Livestock(val id: String, val name: String, val emoji: String)

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    val state by viewModel.onboardingState.collectAsState()
    
    when (state.currentStep) {
        0 -> LanguageSelectionStep(
            selectedLanguage = state.selectedLanguage,
            onLanguageSelected = viewModel::selectLanguage,
            onNext = viewModel::nextStep
        )
        1 -> LocationSelectionStep(
            selectedLocation = state.selectedLocation,
            onLocationSelected = viewModel::selectLocation,
            onNext = viewModel::nextStep,
            onBack = viewModel::previousStep
        )
        2 -> CropSelectionStep(
            selectedCrops = state.selectedCrops,
            onCropToggled = viewModel::toggleCrop,
            onNext = viewModel::nextStep,
            onBack = viewModel::previousStep
        )
        3 -> LivestockSelectionStep(
            selectedLivestock = state.selectedLivestock,
            onLivestockToggled = viewModel::toggleLivestock,
            onComplete = {
                viewModel.completeOnboarding()
                onOnboardingComplete()
            },
            onBack = viewModel::previousStep
        )
    }
}

@Composable
fun LanguageSelectionStep(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onNext: () -> Unit
) {
    val languages = listOf(
        "en" to "English",
        "hi" to "हिंदी (Hindi)",
        "bn" to "বাংলা (Bengali)",
        "te" to "తెలుగు (Telugu)",
        "mr" to "मराठी (Marathi)",
        "ta" to "தமிழ் (Tamil)",
        "gu" to "ગુજરાતી (Gujarati)",
        "kn" to "ಕನ್ನಡ (Kannada)"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Choose your preferred language",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        Text(
            text = "Select the language you're most comfortable with",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(languages) { (code, name) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageSelected(code) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedLanguage == code) {
                            Color(0xFF4CAF50).copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Text(
                        text = name,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 16.sp
                    )
                }
            }
        }
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedLanguage.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Continue", modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
fun LocationSelectionStep(
    selectedLocation: String,
    onLocationSelected: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val locations = listOf(
        Location("mh", "Mumbai", "Maharashtra"),
        Location("dl", "Delhi", "Delhi"),
        Location("ka", "Bangalore", "Karnataka"),
        Location("tn", "Chennai", "Tamil Nadu"),
        Location("wb", "Kolkata", "West Bengal"),
        Location("up", "Lucknow", "Uttar Pradesh"),
        Location("gj", "Ahmedabad", "Gujarat"),
        Location("rj", "Jaipur", "Rajasthan"),
        Location("mp", "Bhopal", "Madhya Pradesh"),
        Location("hr", "Chandigarh", "Haryana")
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Where are you located?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        Text(
            text = "This helps us provide location-specific advice",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(locations) { location ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLocationSelected(location.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedLocation == location.id) {
                            Color(0xFF4CAF50).copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = location.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = location.state,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back", modifier = Modifier.padding(vertical = 8.dp))
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = selectedLocation.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Continue", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
fun CropSelectionStep(
    selectedCrops: List<String>,
    onCropToggled: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val crops = listOf(
        Crop("wheat", "Wheat", "🌾"),
        Crop("rice", "Rice", "🌾"),
        Crop("cotton", "Cotton", "🏵️"),
        Crop("sugarcane", "Sugarcane", "🎋"),
        Crop("maize", "Maize", "🌽"),
        Crop("potato", "Potato", "🥔"),
        Crop("tomato", "Tomato", "🍅"),
        Crop("onion", "Onion", "🧅"),
        Crop("soybean", "Soybean", "🌱"),
        Crop("groundnut", "Groundnut", "🥜"),
        Crop("pulses", "Pulses", "🫘"),
        Crop("vegetables", "Vegetables", "🥬")
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "What crops do you grow?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        Text(
            text = "Select all that apply (you can skip if none)",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(crops) { crop ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f)
                        .clickable { onCropToggled(crop.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (crop.id in selectedCrops) {
                            Color(0xFF4CAF50).copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = if (crop.id in selectedCrops) {
                        CardDefaults.outlinedCardBorder().copy(width = 2.dp)
                    } else null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = crop.emoji,
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = crop.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back", modifier = Modifier.padding(vertical = 8.dp))
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Continue", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
fun LivestockSelectionStep(
    selectedLivestock: List<String>,
    onLivestockToggled: (String) -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val livestock = listOf(
        Livestock("cow", "Cow", "🐄"),
        Livestock("buffalo", "Buffalo", "🐃"),
        Livestock("goat", "Goat", "🐐"),
        Livestock("sheep", "Sheep", "🐑"),
        Livestock("chicken", "Chicken", "🐓"),
        Livestock("duck", "Duck", "🦆"),
        Livestock("pig", "Pig", "🐖"),
        Livestock("fish", "Fish", "🐟")
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Do you have livestock?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        Text(
            text = "Select all that apply (you can skip if none)",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(livestock) { animal ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f)
                        .clickable { onLivestockToggled(animal.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (animal.id in selectedLivestock) {
                            Color(0xFF4CAF50).copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = if (animal.id in selectedLivestock) {
                        CardDefaults.outlinedCardBorder().copy(width = 2.dp)
                    } else null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = animal.emoji,
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = animal.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back", modifier = Modifier.padding(vertical = 8.dp))
            }
            
            Button(
                onClick = onComplete,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Start Chatting", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}