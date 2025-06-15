import { pool } from '../src/database';
import { logger } from '../src/utils/logger';

// Translation data extracted from StringsManager.kt
const translations = {
  en: {
    // App Name
    APP_NAME: "FarmerChat",
    
    // Onboarding
    CHOOSE_LANGUAGE: "Choose your preferred language",
    LANGUAGE_SUBTITLE: "You can change this later in settings",
    WHERE_LOCATED: "Where are you located?",
    LOCATION_SUBTITLE: "This helps us provide location-specific advice",
    LOCATION_PERMISSION_RATIONALE: "Location access helps provide weather and region-specific farming advice",
    ENABLE_LOCATION: "Enable Location",
    DETECT_MY_LOCATION: "Detect my location",
    LOCATION_DETECTED: "Location detected",
    GETTING_LOCATION: "Getting location...",
    OR_ENTER_MANUALLY: "Or enter manually",
    LOCATION_PLACEHOLDER: "e.g., Mumbai, Maharashtra",
    SELECT_CROPS: "Select your crops",
    CROPS_SUBTITLE: "Choose all crops you grow",
    SELECT_LIVESTOCK: "Select your livestock",
    LIVESTOCK_SUBTITLE: "Choose all animals you raise",
    
    // Common Actions
    CONTINUE: "Continue",
    BACK: "Back",
    NEXT: "Next",
    SKIP: "Skip",
    DONE: "Done",
    CANCEL: "Cancel",
    OK: "OK",
    RETRY: "Retry",
    SEARCH: "Search",
    CLEAR: "Clear",
    SAVE: "Save",
    SEARCH_CROPS: "Search crops...",
    SEARCH_LIVESTOCK: "Search livestock...",
    
    // Conversations Screen
    MY_CONVERSATIONS: "My Conversations",
    NEW_CONVERSATION: "New Conversation",
    NO_CONVERSATIONS: "No conversations yet",
    START_FIRST_CONVERSATION: "Start your first conversation to get farming advice",
    DELETE_CONVERSATION: "Delete conversation?",
    SEARCH_CONVERSATIONS: "Search conversations...",
    START_A_CONVERSATION: "Start a conversation...",
    
    // Chat Screen
    TYPE_MESSAGE: "Type a message...",
    ASK_QUESTION: "Ask a question...",
    STARTER_QUESTIONS: "Starter Questions",
    WHAT_TO_KNOW_MORE: "What would you like to know more about?",
    LISTENING: "Listening...",
    TAP_TO_SPEAK: "Tap to speak",
    RATE_RESPONSE: "Rate this response",
    SPEAK_MESSAGE: "Speak your message",
    STOP_SPEAKING: "Stop speaking",
    
    // Settings
    SETTINGS: "Settings",
    PROFILE: "Profile",
    NAME: "Name",
    LOCATION: "Location",
    CROPS: "Crops",
    LIVESTOCK: "Livestock",
    SELECTED: "selected",
    PREFERENCES: "Preferences",
    LANGUAGE: "Language",
    VOICE_RESPONSES: "Voice Responses",
    VOICE_RESPONSES_DESC: "Read out AI responses automatically",
    VOICE_INPUT: "Voice Input",
    VOICE_INPUT_DESC: "Use voice to ask questions",
    AI_SETTINGS: "AI Settings",
    RESPONSE_LENGTH: "Response Length",
    CONCISE: "Concise",
    DETAILED: "Detailed",
    COMPREHENSIVE: "Comprehensive",
    FORMATTED_RESPONSES: "Formatted Responses",
    FORMATTED_RESPONSES_DESC: "Show responses with bullet points and sections",
    DATA_PRIVACY: "Data & Privacy",
    EXPORT_DATA: "Export My Data",
    EXPORT_DATA_DESC: "Download all your conversations and data",
    DELETE_ALL_DATA: "Delete All Data",
    DELETE_ALL_DATA_DESC: "Permanently delete your account and all data",
    DELETE_DATA_CONFIRM: "Are you sure you want to delete all your data? This action cannot be undone.",
    DELETE: "Delete",
    ABOUT: "About",
    APP_VERSION: "App Version",
    APP_DESCRIPTION: "FarmerChat provides AI-powered agricultural advice to help farmers make better decisions.",
    VERSION: "Version",
    HELP_FEEDBACK: "Help & Feedback",
    HELP_FEEDBACK_DESC: "Get help or send feedback",
    RESET: "Reset",
    RESET_ONBOARDING_CONFIRM: "Are you sure you want to reset onboarding? This will clear all your settings and data."
  },
  hi: {
    // App Name
    APP_NAME: "किसान चैट",
    
    // Onboarding
    CHOOSE_LANGUAGE: "अपनी पसंदीदा भाषा चुनें",
    LANGUAGE_SUBTITLE: "आप इसे बाद में सेटिंग्स में बदल सकते हैं",
    WHERE_LOCATED: "आप कहाँ स्थित हैं?",
    LOCATION_SUBTITLE: "यह हमें स्थान-विशिष्ट सलाह प्रदान करने में मदद करता है",
    LOCATION_PERMISSION_RATIONALE: "स्थान पहुंच मौसम और क्षेत्र-विशिष्ट खेती सलाह प्रदान करने में मदद करती है",
    ENABLE_LOCATION: "स्थान सक्षम करें",
    DETECT_MY_LOCATION: "मेरा स्थान पता लगाएं",
    LOCATION_DETECTED: "स्थान मिल गया",
    GETTING_LOCATION: "स्थान प्राप्त कर रहे हैं...",
    OR_ENTER_MANUALLY: "या मैन्युअल रूप से दर्ज करें",
    LOCATION_PLACEHOLDER: "जैसे, मुंबई, महाराष्ट्र",
    SELECT_CROPS: "अपनी फसलें चुनें",
    CROPS_SUBTITLE: "वे सभी फसलें चुनें जो आप उगाते हैं",
    SELECT_LIVESTOCK: "अपने पशुधन चुनें",
    LIVESTOCK_SUBTITLE: "वे सभी जानवर चुनें जो आप पालते हैं",
    
    // Common Actions
    CONTINUE: "जारी रखें",
    BACK: "वापस",
    NEXT: "अगला",
    SKIP: "छोड़ें",
    DONE: "हो गया",
    CANCEL: "रद्द करें",
    OK: "ठीक है",
    RETRY: "फिर से कोशिश करें",
    SEARCH: "खोजें",
    CLEAR: "साफ़ करें",
    SAVE: "सहेजें",
    SEARCH_CROPS: "फसलें खोजें...",
    SEARCH_LIVESTOCK: "पशुधन खोजें...",
    
    // Conversations Screen
    MY_CONVERSATIONS: "मेरी बातचीत",
    NEW_CONVERSATION: "नई बातचीत",
    NO_CONVERSATIONS: "अभी तक कोई बातचीत नहीं",
    START_FIRST_CONVERSATION: "खेती सलाह पाने के लिए अपनी पहली बातचीत शुरू करें",
    DELETE_CONVERSATION: "बातचीत हटाएं?",
    SEARCH_CONVERSATIONS: "बातचीत खोजें...",
    START_A_CONVERSATION: "बातचीत शुरू करें...",
    
    // Chat Screen
    TYPE_MESSAGE: "संदेश टाइप करें...",
    ASK_QUESTION: "प्रश्न पूछें...",
    STARTER_QUESTIONS: "शुरुआती प्रश्न",
    WHAT_TO_KNOW_MORE: "आप किस बारे में और जानना चाहेंगे?",
    LISTENING: "सुन रहे हैं...",
    TAP_TO_SPEAK: "बोलने के लिए टैप करें",
    RATE_RESPONSE: "इस उत्तर को रेट करें",
    SPEAK_MESSAGE: "अपना संदेश बोलें",
    STOP_SPEAKING: "बोलना बंद करें",
    
    // Settings
    SETTINGS: "सेटिंग्स",
    PROFILE: "प्रोफ़ाइल",
    NAME: "नाम",
    LOCATION: "स्थान",
    CROPS: "फसलें",
    LIVESTOCK: "पशुधन",
    SELECTED: "चयनित",
    PREFERENCES: "प्राथमिकताएं",
    LANGUAGE: "भाषा",
    VOICE_RESPONSES: "वॉइस प्रतिक्रियाएं",
    VOICE_RESPONSES_DESC: "AI प्रतिक्रियाओं को स्वचालित रूप से पढ़ें",
    VOICE_INPUT: "वॉइस इनपुट",
    VOICE_INPUT_DESC: "प्रश्न पूछने के लिए आवाज़ का उपयोग करें",
    AI_SETTINGS: "AI सेटिंग्स",
    RESPONSE_LENGTH: "प्रतिक्रिया की लंबाई",
    CONCISE: "संक्षिप्त",
    DETAILED: "विस्तृत",
    COMPREHENSIVE: "व्यापक",
    FORMATTED_RESPONSES: "स्वरूपित प्रतिक्रियाएं",
    FORMATTED_RESPONSES_DESC: "बुलेट पॉइंट्स और सेक्शन के साथ प्रतिक्रियाएं दिखाएं",
    DATA_PRIVACY: "डेटा और गोपनीयता",
    EXPORT_DATA: "मेरा डेटा निर्यात करें",
    EXPORT_DATA_DESC: "अपनी सभी बातचीत और डेटा डाउनलोड करें",
    DELETE_ALL_DATA: "सभी डेटा हटाएं",
    DELETE_ALL_DATA_DESC: "अपना खाता और सभी डेटा स्थायी रूप से हटाएं",
    DELETE_DATA_CONFIRM: "क्या आप वाकई अपना सारा डेटा हटाना चाहते हैं? इस क्रिया को पूर्ववत नहीं किया जा सकता।",
    DELETE: "हटाएं",
    ABOUT: "के बारे में",
    APP_VERSION: "ऐप संस्करण",
    APP_DESCRIPTION: "किसान चैट किसानों को बेहतर निर्णय लेने में मदद करने के लिए AI-संचालित कृषि सलाह प्रदान करता है।",
    VERSION: "संस्करण",
    HELP_FEEDBACK: "सहायता और प्रतिक्रिया",
    HELP_FEEDBACK_DESC: "सहायता प्राप्त करें या प्रतिक्रिया भेजें",
    RESET: "रीसेट",
    RESET_ONBOARDING_CONFIRM: "क्या आप वाकई ऑनबोर्डिंग रीसेट करना चाहते हैं? यह आपकी सभी सेटिंग्स और डेटा को साफ़ कर देगा।"
  },
  sw: {
    // App Name
    APP_NAME: "MkulimaChat",
    
    // Onboarding
    CHOOSE_LANGUAGE: "Chagua lugha unayopendelea",
    LANGUAGE_SUBTITLE: "Unaweza kuibadilisha baadaye katika mipangilio",
    WHERE_LOCATED: "Uko wapi?",
    LOCATION_SUBTITLE: "Hii inatusaidia kutoa ushauri maalum wa eneo",
    LOCATION_PERMISSION_RATIONALE: "Ufikiaji wa mahali husaidia kutoa ushauri wa kilimo wa hali ya hewa na mkoa",
    ENABLE_LOCATION: "Wezesha Mahali",
    DETECT_MY_LOCATION: "Tambua mahali pangu",
    LOCATION_DETECTED: "Mahali pametambuliwa",
    GETTING_LOCATION: "Kupata mahali...",
    OR_ENTER_MANUALLY: "Au ingiza mwenyewe",
    LOCATION_PLACEHOLDER: "Mfano, Dar es Salaam, Tanzania",
    SELECT_CROPS: "Chagua mazao yako",
    CROPS_SUBTITLE: "Chagua mazao yote unayolima",
    SELECT_LIVESTOCK: "Chagua mifugo yako",
    LIVESTOCK_SUBTITLE: "Chagua wanyama wote unaofuga",
    
    // Common Actions
    CONTINUE: "Endelea",
    BACK: "Rudi",
    NEXT: "Ifuatayo",
    SKIP: "Ruka",
    DONE: "Imekamilika",
    CANCEL: "Ghairi",
    OK: "Sawa",
    RETRY: "Jaribu tena",
    SEARCH: "Tafuta",
    CLEAR: "Futa",
    SAVE: "Hifadhi",
    SEARCH_CROPS: "Tafuta mazao...",
    SEARCH_LIVESTOCK: "Tafuta mifugo...",
    
    // Conversations Screen
    MY_CONVERSATIONS: "Mazungumzo Yangu",
    NEW_CONVERSATION: "Mazungumzo Mapya",
    NO_CONVERSATIONS: "Hakuna mazungumzo bado",
    START_FIRST_CONVERSATION: "Anza mazungumzo yako ya kwanza kupata ushauri wa kilimo",
    DELETE_CONVERSATION: "Futa mazungumzo?",
    SEARCH_CONVERSATIONS: "Tafuta mazungumzo...",
    START_A_CONVERSATION: "Anza mazungumzo...",
    
    // Chat Screen
    TYPE_MESSAGE: "Andika ujumbe...",
    ASK_QUESTION: "Uliza swali...",
    STARTER_QUESTIONS: "Maswali ya Kuanza",
    WHAT_TO_KNOW_MORE: "Ungependa kujua zaidi kuhusu nini?",
    LISTENING: "Nasikiliza...",
    TAP_TO_SPEAK: "Gonga ili kuzungumza",
    RATE_RESPONSE: "Kadiria jibu hili",
    SPEAK_MESSAGE: "Sema ujumbe wako",
    STOP_SPEAKING: "Acha kuzungumza",
    
    // Settings
    SETTINGS: "Mipangilio",
    PROFILE: "Wasifu",
    NAME: "Jina",
    LOCATION: "Mahali",
    CROPS: "Mazao",
    LIVESTOCK: "Mifugo",
    SELECTED: "imechaguliwa",
    PREFERENCES: "Mapendeleo",
    LANGUAGE: "Lugha",
    VOICE_RESPONSES: "Majibu ya Sauti",
    VOICE_RESPONSES_DESC: "Soma majibu ya AI kiotomatiki",
    VOICE_INPUT: "Ingizo la Sauti",
    VOICE_INPUT_DESC: "Tumia sauti kuuliza maswali",
    AI_SETTINGS: "Mipangilio ya AI",
    RESPONSE_LENGTH: "Urefu wa Jibu",
    CONCISE: "Mfupi",
    DETAILED: "Kina",
    COMPREHENSIVE: "Kamili",
    FORMATTED_RESPONSES: "Majibu Yaliyopangwa",
    FORMATTED_RESPONSES_DESC: "Onyesha majibu yenye alama za risasi na sehemu",
    DATA_PRIVACY: "Data na Faragha",
    EXPORT_DATA: "Hamisha Data Yangu",
    EXPORT_DATA_DESC: "Pakua mazungumzo yako yote na data",
    DELETE_ALL_DATA: "Futa Data Zote",
    DELETE_ALL_DATA_DESC: "Futa akaunti yako na data zote kabisa",
    DELETE_DATA_CONFIRM: "Je, una uhakika unataka kufuta data yako yote? Kitendo hiki hakiwezi kutengwa.",
    DELETE: "Futa",
    ABOUT: "Kuhusu",
    APP_VERSION: "Toleo la Programu",
    APP_DESCRIPTION: "MkulimaChat inatoa ushauri wa kilimo unaotumia AI kusaidia wakulima kufanya maamuzi bora.",
    VERSION: "Toleo",
    HELP_FEEDBACK: "Msaada na Maoni",
    HELP_FEEDBACK_DESC: "Pata msaada au tuma maoni",
    RESET: "Weka upya",
    RESET_ONBOARDING_CONFIRM: "Je, una uhakika unataka kuweka upya mwongozo? Hii itafuta mipangilio yako yote na data."
  }
};

async function importTranslations() {
  const client = await pool.connect();
  
  try {
    await client.query('BEGIN');
    
    logger.info('Starting translation import...');
    
    // Import UI translations
    for (const [langCode, langTranslations] of Object.entries(translations)) {
      logger.info(`Importing ${Object.keys(langTranslations).length} UI translations for ${langCode}...`);
      
      for (const [key, value] of Object.entries(langTranslations)) {
        await client.query(
          `INSERT INTO ui_translations (language_code, key, translation)
           VALUES ($1, $2, $3)
           ON CONFLICT (language_code, key)
           DO UPDATE SET translation = $3, updated_at = NOW()`,
          [langCode, key, value]
        );
      }
    }
    
    // Import crop translations from the Android app
    const cropTranslations = {
      en: {
        rice: "Rice",
        wheat: "Wheat",
        maize: "Maize",
        millet: "Millet",
        sorghum: "Sorghum",
        barley: "Barley",
        cotton: "Cotton",
        sugarcane: "Sugarcane",
        jute: "Jute",
        coffee: "Coffee",
        tea: "Tea",
        rubber: "Rubber",
        coconut: "Coconut",
        groundnut: "Groundnut",
        mustard: "Mustard",
        sunflower: "Sunflower",
        soybean: "Soybean",
        sesame: "Sesame",
        safflower: "Safflower",
        lentil: "Lentil",
        chickpea: "Chickpea",
        pigeon_pea: "Pigeon Pea",
        black_gram: "Black Gram",
        green_gram: "Green Gram",
        kidney_bean: "Kidney Bean",
        potato: "Potato",
        tomato: "Tomato",
        onion: "Onion",
        brinjal: "Brinjal",
        okra: "Okra",
        cabbage: "Cabbage",
        cauliflower: "Cauliflower",
        carrot: "Carrot",
        radish: "Radish",
        peas: "Peas",
        beans: "Beans",
        spinach: "Spinach",
        coriander: "Coriander",
        chilli: "Chilli",
        garlic: "Garlic",
        ginger: "Ginger",
        turmeric: "Turmeric",
        mango: "Mango",
        banana: "Banana",
        apple: "Apple",
        orange: "Orange",
        grapes: "Grapes",
        papaya: "Papaya",
        guava: "Guava",
        pomegranate: "Pomegranate",
        watermelon: "Watermelon",
        custard_apple: "Custard Apple",
        sapota: "Sapota",
        jackfruit: "Jackfruit",
        cashew: "Cashew"
      },
      hi: {
        rice: "चावल",
        wheat: "गेहूं",
        maize: "मक्का",
        millet: "बाजरा",
        sorghum: "ज्वार",
        barley: "जौ",
        cotton: "कपास",
        sugarcane: "गन्ना",
        jute: "जूट",
        coffee: "कॉफी",
        tea: "चाय",
        rubber: "रबर",
        coconut: "नारियल",
        groundnut: "मूंगफली",
        mustard: "सरसों",
        sunflower: "सूरजमुखी",
        soybean: "सोयाबीन",
        sesame: "तिल",
        safflower: "कुसुम",
        lentil: "मसूर",
        chickpea: "चना",
        pigeon_pea: "अरहर",
        black_gram: "उड़द",
        green_gram: "मूंग",
        kidney_bean: "राजमा",
        potato: "आलू",
        tomato: "टमाटर",
        onion: "प्याज",
        brinjal: "बैंगन",
        okra: "भिंडी",
        cabbage: "पत्तागोभी",
        cauliflower: "फूलगोभी",
        carrot: "गाजर",
        radish: "मूली",
        peas: "मटर",
        beans: "फलियाँ",
        spinach: "पालक",
        coriander: "धनिया",
        chilli: "मिर्च",
        garlic: "लहसुन",
        ginger: "अदरक",
        turmeric: "हल्दी",
        mango: "आम",
        banana: "केला",
        apple: "सेब",
        orange: "संतरा",
        grapes: "अंगूर",
        papaya: "पपीता",
        guava: "अमरूद",
        pomegranate: "अनार",
        watermelon: "तरबूज",
        custard_apple: "सीताफल",
        sapota: "चीकू",
        jackfruit: "कटहल",
        cashew: "काजू"
      },
      sw: {
        rice: "Mchele",
        wheat: "Ngano",
        maize: "Mahindi",
        millet: "Mtama",
        sorghum: "Mtama mkuu",
        barley: "Shayiri",
        cotton: "Pamba",
        sugarcane: "Miwa",
        jute: "Jute",
        coffee: "Kahawa",
        tea: "Chai",
        rubber: "Mpira",
        coconut: "Nazi",
        groundnut: "Karanga",
        mustard: "Haradali",
        sunflower: "Alizeti",
        soybean: "Soya",
        sesame: "Ufuta",
        safflower: "Safflower",
        lentil: "Dengu",
        chickpea: "Dengu za kihindi",
        pigeon_pea: "Mbaazi",
        black_gram: "Choroko mweusi",
        green_gram: "Choroko",
        kidney_bean: "Maharage mekundu",
        potato: "Viazi",
        tomato: "Nyanya",
        onion: "Kitunguu",
        brinjal: "Bilingani",
        okra: "Bamia",
        cabbage: "Kabichi",
        cauliflower: "Koliflawa",
        carrot: "Karoti",
        radish: "Figili",
        peas: "Njegere",
        beans: "Maharage",
        spinach: "Mchicha",
        coriander: "Giligilani",
        chilli: "Pilipili",
        garlic: "Kitunguu saumu",
        ginger: "Tangawizi",
        turmeric: "Manjano",
        mango: "Embe",
        banana: "Ndizi",
        apple: "Tofaa",
        orange: "Chungwa",
        grapes: "Zabibu",
        papaya: "Papai",
        guava: "Mapera",
        pomegranate: "Komamanga",
        watermelon: "Tikiti maji",
        custard_apple: "Topetope",
        sapota: "Sapota",
        jackfruit: "Fenesi",
        cashew: "Korosho"
      }
    };
    
    // Import crop translations
    for (const [langCode, crops] of Object.entries(cropTranslations)) {
      logger.info(`Importing ${Object.keys(crops).length} crop translations for ${langCode}...`);
      
      for (const [key, value] of Object.entries(crops)) {
        await client.query(
          `INSERT INTO crop_translations (language_code, crop_id, name)
           VALUES ($1, $2, $3)
           ON CONFLICT (language_code, crop_id)
           DO UPDATE SET name = $3, updated_at = NOW()`,
          [langCode, key, value]
        );
      }
    }
    
    // Import livestock translations
    const livestockTranslations = {
      en: {
        cow: "Cow",
        buffalo: "Buffalo",
        goat: "Goat",
        sheep: "Sheep",
        pig: "Pig",
        chicken: "Chicken",
        duck: "Duck",
        turkey: "Turkey",
        goose: "Goose",
        quail: "Quail",
        rabbit: "Rabbit",
        horse: "Horse",
        donkey: "Donkey",
        mule: "Mule",
        camel: "Camel",
        yak: "Yak",
        fish: "Fish",
        shrimp: "Shrimp",
        crab: "Crab",
        bee: "Bee",
        silkworm: "Silkworm"
      },
      hi: {
        cow: "गाय",
        buffalo: "भैंस",
        goat: "बकरी",
        sheep: "भेड़",
        pig: "सुअर",
        chicken: "मुर्गी",
        duck: "बत्तख",
        turkey: "टर्की",
        goose: "हंस",
        quail: "बटेर",
        rabbit: "खरगोश",
        horse: "घोड़ा",
        donkey: "गधा",
        mule: "खच्चर",
        camel: "ऊंट",
        yak: "याक",
        fish: "मछली",
        shrimp: "झींगा",
        crab: "केकड़ा",
        bee: "मधुमक्खी",
        silkworm: "रेशम कीट"
      },
      sw: {
        cow: "Ng'ombe",
        buffalo: "Nyati",
        goat: "Mbuzi",
        sheep: "Kondoo",
        pig: "Nguruwe",
        chicken: "Kuku",
        duck: "Bata",
        turkey: "Bata mzinga",
        goose: "Bata bukini",
        quail: "Kware",
        rabbit: "Sungura",
        horse: "Farasi",
        donkey: "Punda",
        mule: "Nyumbu",
        camel: "Ngamia",
        yak: "Yak",
        fish: "Samaki",
        shrimp: "Kamba",
        crab: "Kaa",
        bee: "Nyuki",
        silkworm: "Minyoo ya hariri"
      }
    };
    
    // Import livestock translations
    for (const [langCode, livestock] of Object.entries(livestockTranslations)) {
      logger.info(`Importing ${Object.keys(livestock).length} livestock translations for ${langCode}...`);
      
      for (const [key, value] of Object.entries(livestock)) {
        await client.query(
          `INSERT INTO livestock_translations (language_code, livestock_id, name)
           VALUES ($1, $2, $3)
           ON CONFLICT (language_code, livestock_id)
           DO UPDATE SET name = $3, updated_at = NOW()`,
          [langCode, key, value]
        );
      }
    }
    
    await client.query('COMMIT');
    logger.info('Translation import completed successfully!');
    
  } catch (error) {
    await client.query('ROLLBACK');
    console.error('Error importing translations:', error);
    logger.error('Error importing translations:', error);
    throw error;
  } finally {
    client.release();
  }
}

// Run the import
importTranslations()
  .then(() => {
    logger.info('Import script completed');
    process.exit(0);
  })
  .catch((error) => {
    console.error('Import script failed:', error);
    logger.error('Import script failed:', error);
    process.exit(1);
  });