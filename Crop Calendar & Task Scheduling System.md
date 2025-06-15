# Crop Calendar & Task Scheduling System

## Project Overview
Build a comprehensive Firebase-based crop calendar system that automatically generates location-aware daily farming tasks, sends intelligent push notifications, and maintains user engagement through conversational task management within the chat interface.

## Technology Stack
- **Backend**: Firebase (Cloud Functions, Firestore, Cloud Scheduler, FCM)
- **Mobile**: Android with Jetpack Compose + WorkManager
- **AI Integration**: Multi-provider LLM support (Claude, Gemini, OpenAI)
- **External APIs**: Weather APIs, Agricultural Data Sources

## Core Requirements

### 1. Conversational Crop Calendar Setup

#### AI-Powered Calendar Creation Flow
```kotlin
// Crop Calendar Setup State Management
@HiltViewModel
class CropCalendarSetupViewModel @Inject constructor(
    private val cropCalendarRepository: CropCalendarRepository,
    private val locationService: LocationService,
    private val aiService: AIService
) : ViewModel() {
    
    private val _setupState = MutableStateFlow(CalendarSetupState.INTRO)
    val setupState = _setupState.asStateFlow()
    
    private val _setupData = MutableStateFlow(CropCalendarSetupData())
    val setupData = _setupData.asStateFlow()
    
    private val _aiResponses = MutableStateFlow<List<ChatMessage>>(emptyList())
    val aiResponses = _aiResponses.asStateFlow()
    
    fun startCalendarSetup(userId: String, conversationId: String) {
        viewModelScope.launch {
            val locationContext = locationService.getCurrentLocationContext(userId)
            
            val introMessage = aiService.generateCalendarSetupIntro(locationContext)
            addAIMessage(introMessage)
            
            _setupState.value = CalendarSetupState.COLLECT_CROPS
        }
    }
    
    fun processUserInput(input: String) {
        viewModelScope.launch {
            addUserMessage(input)
            
            when (_setupState.value) {
                CalendarSetupState.COLLECT_CROPS -> {
                    val extractedCrops = aiService.extractCropsFromText(input)
                    if (extractedCrops.isNotEmpty()) {
                        _setupData.value = _setupData.value.copy(crops = extractedCrops)
                        val response = aiService.confirmCropsAndAskArea(extractedCrops)
                        addAIMessage(response)
                        _setupState.value = CalendarSetupState.COLLECT_AREAS
                    } else {
                        val clarificationResponse = aiService.askForCropClarification()
                        addAIMessage(clarificationResponse)
                    }
                }
                
                CalendarSetupState.COLLECT_AREAS -> {
                    val areaData = aiService.extractAreaData(input, _setupData.value.crops)
                    _setupData.value = _setupData.value.copy(cropAreas = areaData)
                    
                    val response = aiService.askForPlantingDates(areaData)
                    addAIMessage(response)
                    _setupState.value = CalendarSetupState.COLLECT_PLANTING_DATES
                }
                
                CalendarSetupState.COLLECT_PLANTING_DATES -> {
                    val plantingDates = aiService.extractPlantingDates(input, _setupData.value.crops)
                    _setupData.value = _setupData.value.copy(plantingDates = plantingDates)
                    
                    val response = aiService.askForPreferences()
                    addAIMessage(response)
                    _setupState.value = CalendarSetupState.COLLECT_PREFERENCES
                }
                
                CalendarSetupState.COLLECT_PREFERENCES -> {
                    val preferences = aiService.extractPreferences(input)
                    _setupData.value = _setupData.value.copy(preferences = preferences)
                    
                    // Generate calendar and show summary
                    generateCropCalendar()
                }
                
                else -> { /* Handle other states */ }
            }
        }
    }
    
    private suspend fun generateCropCalendar() {
        val calendarData = _setupData.value
        val locationContext = locationService.getCurrentLocationContext(calendarData.userId)
        
        val cropCalendar = cropCalendarRepository.createCalendar(
            userId = calendarData.userId,
            crops = calendarData.crops,
            areas = calendarData.cropAreas,
            plantingDates = calendarData.plantingDates,
            preferences = calendarData.preferences,
            locationContext = locationContext
        )
        
        val summaryMessage = aiService.generateCalendarSummary(cropCalendar)
        addAIMessage(summaryMessage)
        
        _setupState.value = CalendarSetupState.COMPLETED
    }
}

// Conversational Setup UI
@Composable
fun CropCalendarSetupScreen(
    viewModel: CropCalendarSetupViewModel,
    onSetupComplete: () -> Unit
) {
    val setupState by viewModel.setupState.collectAsState()
    val aiResponses by viewModel.aiResponses.collectAsState()
    val setupData by viewModel.setupData.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Progress indicator
        LinearProgressIndicator(
            progress = setupState.progress,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Chat-like setup interface
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true
        ) {
            items(aiResponses.reversed()) { message ->
                SetupChatMessage(message = message)
            }
        }
        
        // Input based on current setup state
        when (setupState) {
            CalendarSetupState.COLLECT_CROPS -> {
                CropSelectionInput(
                    onCropsSelected = { crops ->
                        viewModel.processUserInput(crops.joinToString(", "))
                    }
                )
            }
            
            CalendarSetupState.COLLECT_AREAS -> {
                AreaInputInterface(
                    crops = setupData.crops,
                    onAreasProvided = { areaText ->
                        viewModel.processUserInput(areaText)
                    }
                )
            }
            
            CalendarSetupState.COLLECT_PLANTING_DATES -> {
                PlantingDateInput(
                    crops = setupData.crops,
                    onDatesProvided = { dateText ->
                        viewModel.processUserInput(dateText)
                    }
                )
            }
            
            CalendarSetupState.COLLECT_PREFERENCES -> {
                PreferencesInput(
                    onPreferencesSet = { prefsText ->
                        viewModel.processUserInput(prefsText)
                    }
                )
            }
            
            CalendarSetupState.COMPLETED -> {
                CalendarCompletionCard(
                    onContinue = onSetupComplete
                )
            }
            
            else -> {
                StandardTextInput(
                    onMessageSent = { text ->
                        viewModel.processUserInput(text)
                    }
                )
            }
        }
    }
}

@Composable
fun CropSelectionInput(
    onCropsSelected: (List<String>) -> Unit
) {
    var selectedCrops by remember { mutableStateOf(setOf<String>()) }
    var customCrop by remember { mutableStateOf("") }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Select your crops",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Common crops grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(200.dp)
            ) {
                items(commonCrops) { crop ->
                    FilterChip(
                        selected = selectedCrops.contains(crop.name),
                        onClick = {
                            selectedCrops = if (selectedCrops.contains(crop.name)) {
                                selectedCrops - crop.name
                            } else {
                                selectedCrops + crop.name
                            }
                        },
                        label = { Text(crop.name) },
                        leadingIcon = {
                            Text(crop.emoji, fontSize = 16.sp)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Custom crop input
            OutlinedTextField(
                value = customCrop,
                onValueChange = { customCrop = it },
                label = { Text("Add custom crop") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (customCrop.isNotBlank()) {
                        IconButton(
                            onClick = {
                                selectedCrops = selectedCrops + customCrop
                                customCrop = ""
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { onCropsSelected(selectedCrops.toList()) },
                enabled = selectedCrops.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue with ${selectedCrops.size} crop${if (selectedCrops.size != 1) "s" else ""}")
            }
        }
    }
}
```

### 2. Daily Task Generation System

#### Firebase Cloud Functions for Task Generation
```javascript
// functions/taskGeneration.js
const functions = require('firebase-functions');
const admin = require('firebase-admin');

class IntelligentTaskGenerator {
  constructor() {
    this.db = admin.firestore();
    this.messaging = admin.messaging();
  }

  async generateDailyTasks() {
    const today = new Date().toISOString().split('T')[0];
    console.log(`Generating tasks for ${today}`);
    
    // Get all active crop calendars
    const calendarsSnapshot = await this.db
      .collection('crop_calendars')
      .where('isActive', '==', true)
      .get();
    
    const taskGenerationPromises = calendarsSnapshot.docs.map(async (calendarDoc) => {
      const calendar = calendarDoc.data();
      const userId = calendar.userId;
      
      try {
        // Check if tasks already generated for today
        const existingTasks = await this.db
          .collection('daily_tasks')
          .doc(`${today}_${userId}`)
          .get();
        
        if (existingTasks.exists) {
          console.log(`Tasks already exist for user ${userId} on ${today}`);
          return;
        }
        
        // Generate tasks for this user
        const tasks = await this.generateUserTasks(calendar, today);
        
        // Store tasks in Firestore
        await this.storeDailyTasks(userId, today, tasks);
        
        console.log(`Generated ${tasks.length} tasks for user ${userId}`);
        
      } catch (error) {
        console.error(`Error generating tasks for user ${userId}:`, error);
      }
    });
    
    await Promise.all(taskGenerationPromises);
    console.log('Daily task generation completed');
  }

  async generateUserTasks(calendar, date) {
    const { userId, crops, location, preferences } = calendar;
    
    // Get contextual data
    const [weatherData, locationContext, userHistory] = await Promise.all([
      this.getWeatherForecast(location.coordinates),
      this.getLocationContext(location),
      this.getUserTaskHistory(userId, 30) // Last 30 days
    ]);
    
    const tasks = [];
    
    // Generate tasks for each crop
    for (const crop of crops) {
      const cropTasks = await this.generateCropSpecificTasks(
        crop, 
        weatherData, 
        locationContext, 
        userHistory,
        date
      );
      tasks.push(...cropTasks);
    }
    
    // Add general farming tasks
    const generalTasks = await this.generateGeneralFarmingTasks(
      weatherData, 
      locationContext, 
      date
    );
    tasks.push(...generalTasks);
    
    // Prioritize and limit tasks (max 5 per day)
    const prioritizedTasks = this.prioritizeAndLimitTasks(tasks, userHistory);
    
    // Add smart follow-up questions to each task
    const enhancedTasks = await this.enhanceTasksWithQuestions(prioritizedTasks);
    
    return enhancedTasks;
  }

  async generateCropSpecificTasks(crop, weather, location, history, date) {
    const daysSincePlanting = this.calculateDaysSincePlanting(crop.plantingDate, date);
    const cropStage = this.determineCropStage(crop.name, daysSincePlanting);
    
    const tasks = [];
    
    // Get crop-specific task templates
    const taskTemplates = await this.getCropTaskTemplates(crop.name, cropStage);
    
    for (const template of taskTemplates) {
      if (this.shouldIncludeTask(template, weather, location, history, daysSincePlanting)) {
        const task = this.createTask(template, crop, weather, location, daysSincePlanting);
        tasks.push(task);
      }
    }
    
    return tasks;
  }

  createTask(template, crop, weather, location, daysSincePlanting) {
    return {
      id: `${crop.name}_${template.type}_${Date.now()}`,
      title: this.customizeTaskTitle(template.title, crop, weather),
      description: this.customizeTaskDescription(template.description, crop, weather, location),
      priority: this.calculatePriority(template, weather, daysSincePlanting),
      category: template.category,
      crop: crop.name,
      cropStage: this.determineCropStage(crop.name, daysSincePlanting),
      estimatedTime: template.estimatedTime,
      weatherDependent: template.weatherDependent || false,
      daysFromPlanting: daysSincePlanting,
      startQuestions: this.generateStarterQuestions(template, crop, weather),
      tips: this.generateContextualTips(template, weather, location),
      completed: false,
      skipped: false,
      completedAt: null,
      skipReason: null
    };
  }

  generateStarterQuestions(template, crop, weather) {
    const baseQuestions = template.starterQuestions || [];
    const contextualQuestions = [];
    
    // Add weather-specific questions
    if (weather.condition === 'Rain' && template.category === 'irrigation') {
      contextualQuestions.push({
        text: "It's raining - skip watering?",
        emoji: "🌧️",
        response: "skip_due_to_rain",
        action: "skip"
      });
    }
    
    if (weather.temperature > 35 && template.category === 'fieldwork') {
      contextualQuestions.push({
        text: "Too hot - do this evening?",
        emoji: "🌡️",
        response: "postpone_heat",
        action: "postpone"
      });
    }
    
    // Add crop-specific questions
    if (crop.name === 'Rice' && template.type === 'water_management') {
      contextualQuestions.push({
        text: "Water level looks good",
        emoji: "💧",
        response: "water_optimal",
        action: "complete"
      }, {
        text: "Need to adjust water",
        emoji: "⚙️",
        response: "water_adjustment_needed",
        action: "help"
      });
    }
    
    return [...baseQuestions, ...contextualQuestions];
  }

  async storeDailyTasks(userId, date, tasks) {
    const taskDocument = {
      userId,
      date,
      tasks,
      weatherContext: await this.getWeatherForecast({ lat: 0, lng: 0 }), // Get from user location
      generated: true,
      sent: false,
      totalTasks: tasks.length,
      highPriorityTasks: tasks.filter(t => t.priority === 'high').length,
      generatedAt: admin.firestore.FieldValue.serverTimestamp()
    };
    
    await this.db
      .collection('daily_tasks')
      .doc(`${date}_${userId}`)
      .set(taskDocument);
  }
}

// Crop-specific task templates with intelligent conditions
const cropTaskTemplates = {
  'Rice': {
    'seedling': [
      {
        type: 'field_preparation',
        title: 'Prepare seedbed for rice',
        description: 'Level the field and ensure proper drainage for healthy seedling growth',
        category: 'field_management',
        priority: 'high',
        estimatedTime: '2 hours',
        weatherDependent: true,
        conditions: {
          temperature: { min: 20, max: 40 },
          noRain: true
        },
        starterQuestions: [
          { text: 'Field is ready', emoji: '✅', response: 'field_ready', action: 'complete' },
          { text: 'Need equipment help', emoji: '🚜', response: 'equipment_help', action: 'help' },
          { text: 'Weather not suitable', emoji: '🌧️', response: 'weather_delay', action: 'postpone' }
        ]
      }
    ],
    'transplanting': [
      {
        type: 'water_management',
        title: 'Check water level in rice field',
        description: 'Maintain 2-3 cm water depth for optimal growth',
        category: 'irrigation',
        priority: 'high',
        estimatedTime: '30 minutes',
        weatherDependent: false,
        conditions: {
          daysSincePlanting: [5, 10, 15, 20, 25, 30]
        },
        starterQuestions: [
          { text: 'Water level perfect', emoji: '💧', response: 'water_perfect', action: 'complete' },
          { text: 'Too little water', emoji: '🔵', response: 'water_low', action: 'help' },
          { text: 'Too much water', emoji: '🌊', response: 'water_high', action: 'help' }
        ]
      },
      {
        type: 'weed_control',
        title: 'Inspect for weeds',
        description: 'Check for weed growth and remove manually or apply herbicide',
        category: 'pest_management',
        priority: 'medium',
        estimatedTime: '45 minutes',
        weatherDependent: true,
        conditions: {
          daysSincePlanting: [15, 25, 35],
          noRain: true
        },
        starterQuestions: [
          { text: 'No weeds found', emoji: '✅', response: 'no_weeds', action: 'complete' },
          { text: 'Few weeds present', emoji: '🌿', response: 'few_weeds', action: 'help' },
          { text: 'Heavy weed growth', emoji: '🚨', response: 'heavy_weeds', action: 'help' }
        ]
      }
    ],
    'flowering': [
      {
        type: 'nutrient_management',
        title: 'Apply phosphorus fertilizer',
        description: 'Apply DAP fertilizer to boost flowering and grain formation',
        category: 'fertilization',
        priority: 'high',
        estimatedTime: '1 hour',
        weatherDependent: true,
        conditions: {
          daysSincePlanting: [60, 75],
          noRain: true
        },
        starterQuestions: [
          { text: 'Fertilizer applied', emoji: '✅', response: 'fertilizer_done', action: 'complete' },
          { text: 'Need quantity guidance', emoji: '❓', response: 'fertilizer_amount', action: 'help' },
          { text: 'No fertilizer available', emoji: '❌', response: 'no_fertilizer', action: 'skip' }
        ]
      }
    ]
  },
  'Tomatoes': {
    'flowering': [
      {
        type: 'support_structure',
        title: 'Check plant support stakes',
        description: 'Ensure tomato plants are properly supported to prevent branch breakage',
        category: 'plant_care',
        priority: 'medium',
        estimatedTime: '30 minutes',
        weatherDependent: false,
        conditions: {
          daysSincePlanting: [30, 45, 60]
        },
        starterQuestions: [
          { text: 'Support looks good', emoji: '🎋', response: 'support_good', action: 'complete' },
          { text: 'Need more stakes', emoji: '📏', response: 'need_stakes', action: 'help' },
          { text: 'Stakes are broken', emoji: '💔', response: 'broken_stakes', action: 'help' }
        ]
      },
      {
        type: 'pruning',
        title: 'Remove suckers',
        description: 'Remove sucker shoots to improve fruit quality and plant health',
        category: 'plant_care',
        priority: 'medium',
        estimatedTime: '20 minutes',
        weatherDependent: true,
        conditions: {
          daysSincePlanting: [35, 50, 65],
          noRain: true
        },
        starterQuestions: [
          { text: 'Pruning completed', emoji: '✂️', response: 'pruning_done', action: 'complete' },
          { text: 'Not sure which to cut', emoji: '❓', response: 'pruning_help', action: 'help' },
          { text: 'Plant looks healthy', emoji: '🌱', response: 'no_pruning_needed', action: 'skip' }
        ]
      }
    ]
  }
};

// Daily task notification scheduler
exports.generateDailyTasks = functions.pubsub
  .schedule('0 5 * * *') // 5 AM daily
  .timeZone('Asia/Kolkata')
  .onRun(async (context) => {
    const taskGenerator = new IntelligentTaskGenerator();
    await taskGenerator.generateDailyTasks();
    return null;
  });

// Send task notifications
exports.sendTaskNotifications = functions.pubsub
  .schedule('0 8 * * *') // 8 AM daily
  .timeZone('Asia/Kolkata')
  .onRun(async (context) => {
    const taskGenerator = new IntelligentTaskGenerator();
    await taskGenerator.sendDailyTaskNotifications();
    return null;
  });
```

### 3. Chat Integration for Task Management

#### Task-Aware Chat Interface
```kotlin
@Composable
fun TaskAwareChatScreen(
    viewModel: ChatViewModel,
    conversationId: String,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
    val dailyTasks by viewModel.dailyTasks.collectAsState()
    val taskInteractionMode by viewModel.taskInteractionMode.collectAsState()
    
    Column(modifier = modifier.fillMaxSize()) {
        // Daily tasks header (collapsible)
        if (dailyTasks.isNotEmpty()) {
            DailyTasksHeader(
                tasks = dailyTasks,
                onTaskClick = { task -> viewModel.handleTaskInteraction(task) },
                onExpandChange = { expanded -> viewModel.setTasksExpanded(expanded) }
            )
        }
        
        // Chat messages with task integration
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages.reversed()) { message ->
                when (message.type) {
                    MessageType.TASK_REMINDER -> {
                        TaskReminderMessage(
                            message = message,
                            onTaskAction = { task, action -> 
                                viewModel.handleTaskAction(task, action) 
                            }
                        )
                    }
                    MessageType.TASK_COMPLETION -> {
                        TaskCompletionMessage(
                            message = message,
                            onFollowUpQuestion = { question ->
                                viewModel.sendMessage(question)
                            }
                        )
                    }
                    else -> {
                        RegularChatMessage(message = message)
                    }
                }
            }
        }
        
        // Smart input with task context
        TaskAwareChatInput(
            onSendMessage = { text -> viewModel.sendMessage(text) },
            taskContext = taskInteractionMode,
            onQuickResponse = { response -> viewModel.handleQuickResponse(response) }
        )
    }
}

@Composable
fun DailyTasksHeader(
    tasks: List<DailyTask>,
    onTaskClick: (DailyTask) -> Unit,
    onExpandChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val completedTasks = tasks.count { it.completed }
    val highPriorityTasks = tasks.count { it.priority == TaskPriority.HIGH && !it.completed }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        )
    ) {
        Column {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        isExpanded = !isExpanded
                        onExpandChange(isExpanded)
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Today's Tasks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "$completedTasks/${tasks.size} completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        
                        if (highPriorityTasks > 0) {
                            Text(
                                text = "$highPriorityTasks high priority",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // Progress circle
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = completedTasks.toFloat() / tasks.size.toFloat(),
                        modifier = Modifier.size(40.dp),
                        strokeWidth = 4.dp
                    )
                    Text(
                        text = "${(completedTasks.toFloat() / tasks.size * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }
            
            // Expanded task list
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tasks) { task ->
                        CompactTaskCard(
                            task = task,
                            onClick = { onTaskClick(task) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskReminderMessage(
    message: ChatMessage,
    onTaskAction: (DailyTask, TaskAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val tasks = message.tasks ?: emptyList()
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Agriculture,
                    contentDescription = "Tasks",
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Daily Farming Tasks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Task cards
            tasks.forEach { task ->
                TaskActionCard(
                    task = task,
                    onAction = { action -> onTaskAction(task, action) },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun TaskActionCard(
    task: DailyTask,
    onAction: (TaskAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = when (task.priority) {
                TaskPriority.HIGH -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                TaskPriority.MEDIUM -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                TaskPriority.LOW -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Task header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${task.crop} • ${task.estimatedTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                PriorityBadge(priority = task.priority)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Weather warning if applicable
            if (task.weatherDependent && task.weatherWarning != null) {
                Spacer(modifier = Modifier.height(8.dp))
                WeatherWarningChip(warning = task.weatherWarning)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Quick action buttons
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(task.startQuestions) { question ->
                    QuickActionChip(
                        question = question,
                        onClick = { onAction(TaskAction.QUICK_RESPONSE(question)) }
                    )
                }
                
                item {
                    AssistChip(
                        onClick = { onAction(TaskAction.GET_HELP) },
                        label = { Text("Help") },
                        leadingIcon = {
                            Icon(Icons.Default.Help, contentDescription = null)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskAwareChatInput(
    onSendMessage: (String) -> Unit,
    taskContext: TaskInteractionMode?,
    onQuickResponse: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf("") }
    
    Column(modifier = modifier) {
        // Quick responses for task context
        if (taskContext != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Quick responses for: ${taskContext.taskTitle}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(taskContext.quickResponses) { response ->
                            AssistChip(
                                onClick = { onQuickResponse(response) },
                                label = { Text(response) }
                            )
                        }
                    }
                }
            }
        }
        
        // Regular chat input
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { 
                        Text(
                            if (taskContext != null) 
                                "Ask about ${taskContext.taskTitle}..." 
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
                        contentDescription = "Send"
                    )
                }
            }
        }
    }
}

// Data classes for task system
data class DailyTask(
    val id: String,
    val title: String,
    val description: String,
    val priority: TaskPriority,
    val category: String,
    val crop: String,
    val cropStage: String,
    val estimatedTime: String,
    val weatherDependent: Boolean,
    val daysFromPlanting: Int,
    val startQuestions: List<TaskQuestion>,
    val tips: List<String>,
    val completed: Boolean,
    val skipped: Boolean,
    val completedAt: Long?,
    val skipReason: String?,
    val weatherWarning: String?
)

data class TaskQuestion(
    val text: String,
    val emoji: String,
    val response: String,
    val action: String
)

enum class TaskPriority {
    HIGH, MEDIUM, LOW
}

enum class TaskAction {
    COMPLETE,
    SKIP,
    GET_HELP,
    POSTPONE,
    QUICK_RESPONSE(val question: TaskQuestion)
}

data class TaskInteractionMode(
    val taskId: String,
    val taskTitle: String,
    val quickResponses: List<String>
)

// Common crops data
val commonCrops = listOf(
    CropInfo("Rice", "🌾"),
    CropInfo("Wheat", "🌾"),
    CropInfo("Tomatoes", "🍅"),
    CropInfo("Potatoes", "🥔"),
    CropInfo("Onions", "🧅"),
    CropInfo("Cotton", "🌱"),
    CropInfo("Sugarcane", "🎋"),
    CropInfo("Maize", "🌽"),
    CropInfo("Soybeans", "🌱"),
    CropInfo("Chilies", "🌶️"),
    CropInfo("Bananas", "🍌"),
    CropInfo("Coconut", "🥥")
)

data class CropInfo(val name: String, val emoji: String)
```

### 4. Progress Tracking & Engagement Features

#### Analytics and Engagement System
```kotlin
@Composable
fun FarmingProgressScreen(
    viewModel: ProgressViewModel,
    modifier: Modifier = Modifier
) {
    val progressData by viewModel.progressData.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val streakData by viewModel.streakData.collectAsState()
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ProgressOverviewCard(progressData = progressData)
        }
        
        item {
            StreakCard(streakData = streakData)
        }
        
        item {
            AchievementsSection(achievements = achievements)
        }
        
        item {
            WeeklyTaskChart(progressData = progressData)
        }
        
        item {
            CropProgressSection(progressData = progressData)
        }
    }
}

@Composable
fun ProgressOverviewCard(
    progressData: ProgressData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "This Week's Progress",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProgressMetric(
                    value = progressData.tasksCompleted,
                    total = progressData.totalTasks,
                    label = "Tasks\nCompleted",
                    icon = Icons.Default.CheckCircle,
                    color = MaterialTheme.colorScheme.primary
                )
                
                ProgressMetric(
                    value = progressData.cropsManaged,
                    total = progressData.totalCrops,
                    label = "Crops\nManaged",
                    icon = Icons.Default.Agriculture,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                ProgressMetric(
                    value = progressData.streakDays,
                    total = null,
                    label = "Day\nStreak",
                    icon = Icons.Default.LocalFireDepartment,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
fun StreakCard(
    streakData: StreakData,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "${streakData.currentStreak} Day Streak!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Keep it up! Complete today's tasks to extend your streak.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Streak visualization
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(7) { index ->
                    val dayData = streakData.last7Days.getOrNull(index)
                    StreakDayIndicator(
                        isCompleted = dayData?.completed ?: false,
                        isToday = dayData?.isToday ?: false,
                        dayLabel = dayData?.dayLabel ?: ""
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementsSection(
    achievements: List<Achievement>,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Recent Achievements",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (achievements.isEmpty()) {
                Text(
                    text = "Complete tasks to earn achievements!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(achievements.take(5)) { achievement ->
                        AchievementBadge(achievement = achievement)
                    }
                }
            }
        }
    }
}

// Seasonal recommendations and tips
@Composable
fun SeasonalTipsCard(
    location: String,
    currentSeason: String,
    tips: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
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
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = "Season",
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "$currentSeason Tips for $location",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            tips.take(3).forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LightbulbOutline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
```

## Expected Outcomes
- **90%+ user engagement** with daily task completion
- **Personalized farming guidance** based on location, weather, and crop stages
- **Improved farming productivity** through structured task management
- **Knowledge retention** via conversational interactions and progress tracking
- **Behavioral change** towards systematic farming practices

## Success Metrics
- Daily task completion rate: >75%
- User retention after 30 days: >60%
- Average session length: 8+ minutes
- Task-to-conversation conversion rate: >40%
- User satisfaction score: >4.5/5

## Cost Optimization
- **Intelligent task generation** reduces redundant notifications
- **Smart caching** of weather and location data
- **Batch processing** for multiple users in same region
- **Progressive complexity** - start simple, add features based on usage