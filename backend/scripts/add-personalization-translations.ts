// Script to add personalization benefit translations to all languages

const personalizationTranslations = {
  // Hindi
  "hi": {
    "PERSONALIZATION_TITLE": "हम यह जानकारी क्यों मांगते हैं",
    "LOCATION_BENEFIT": "मौसम की चेतावनी, स्थानीय बाजार मूल्य और क्षेत्र-विशिष्ट खेती की सलाह प्राप्त करें",
    "NAME_BENEFIT": "व्यक्तिगत अभिवादन प्राप्त करें और हमारे AI सहायक के साथ विश्वास बनाएं",
    "LANGUAGE_BENEFIT": "बेहतर समझ के लिए अपनी पसंदीदा भाषा में सलाह प्राप्त करें",
    "GENDER_BENEFIT": "लिंग-विशिष्ट कार्यक्रमों और अनुकूलित कृषि अनुशंसाओं तक पहुंच प्राप्त करें",
    "ROLE_BENEFIT": "अपनी विशेषज्ञता स्तर और पेशेवर आवश्यकताओं के अनुरूप सामग्री प्राप्त करें",
    "CROPS_BENEFIT": "रोपण, कीट नियंत्रण और फसल काटने के समय पर फसल-विशिष्ट सलाह प्राप्त करें",
    "LIVESTOCK_BENEFIT": "पशु स्वास्थ्य, खिलाना और प्रजनन पर लक्षित मार्गदर्शन प्राप्त करें"
  },
  
  // Bengali
  "bn": {
    "PERSONALIZATION_TITLE": "আমরা কেন এই তথ্য চাই",
    "LOCATION_BENEFIT": "আবহাওয়া সতর্কতা, স্থানীয় বাজার মূল্য এবং অঞ্চল-নির্দিষ্ট কৃষি পরামর্শ পান",
    "NAME_BENEFIT": "ব্যক্তিগত শুভেচ্ছা পান এবং আমাদের AI সহায়কের সাথে বিশ্বাস গড়ে তুলুন",
    "LANGUAGE_BENEFIT": "ভাল বোঝার জন্য আপনার পছন্দের ভাষায় পরামর্শ পান",
    "GENDER_BENEFIT": "লিঙ্গ-নির্দিষ্ট প্রোগ্রাম এবং কাস্টমাইজড কৃষি সুপারিশে প্রবেশাধিকার পান",
    "ROLE_BENEFIT": "আপনার দক্ষতা স্তর এবং পেশাদার প্রয়োজনের উপযুক্ত বিষয়বস্তু পান",
    "CROPS_BENEFIT": "রোপণ, কীটপতঙ্গ নিয়ন্ত্রণ এবং ফসল কাটার সময় সম্পর্কে ফসল-নির্দিষ্ট পরামর্শ পান",
    "LIVESTOCK_BENEFIT": "পশু স্বাস্থ্য, খাওয়ানো এবং প্রজনন সম্পর্কে লক্ষ্যবস্তু নির্দেশনা পান"
  },
  
  // Spanish
  "es": {
    "PERSONALIZATION_TITLE": "Por qué solicitamos esta información",
    "LOCATION_BENEFIT": "Recibe alertas meteorológicas, precios del mercado local y consejos agrícolas específicos de la región",
    "NAME_BENEFIT": "Recibe saludos personalizados y construye confianza con nuestro asistente de IA",
    "LANGUAGE_BENEFIT": "Obtén consejos en tu idioma preferido para una mejor comprensión",
    "GENDER_BENEFIT": "Accede a programas específicos de género y recomendaciones agrícolas personalizadas",
    "ROLE_BENEFIT": "Recibe contenido adaptado a tu nivel de experiencia y necesidades profesionales",
    "CROPS_BENEFIT": "Obtén consejos específicos sobre plantación, control de plagas y momento de cosecha",
    "LIVESTOCK_BENEFIT": "Recibe orientación específica sobre salud animal, alimentación y reproducción"
  },
  
  // Swahili
  "sw": {
    "PERSONALIZATION_TITLE": "Kwa nini tunaomba maelezo haya",
    "LOCATION_BENEFIT": "Pata arifa za hali ya hewa, bei za soko la karibu na ushauri wa kilimo maalum wa eneo",
    "NAME_BENEFIT": "Pokea salamu za kibinafsi na ujenga uaminifu na msaidizi wetu wa AI",
    "LANGUAGE_BENEFIT": "Pata ushauri katika lugha unayopendelea kwa uelewa bora",
    "GENDER_BENEFIT": "Pata ufikiaji wa programu maalum za kijinsia na mapendekezo ya kilimo yaliyobinafsishwa",
    "ROLE_BENEFIT": "Pokea maudhui yanayofaa kiwango chako cha utaalamu na mahitaji ya kitaaluma",
    "CROPS_BENEFIT": "Pata ushauri maalum wa mazao kuhusu upandaji, udhibiti wa wadudu na wakati wa mavuno",
    "LIVESTOCK_BENEFIT": "Pokea mwongozo maalum kuhusu afya ya mifugo, kulisha na uzazi"
  },
  
  // Arabic
  "ar": {
    "PERSONALIZATION_TITLE": "لماذا نطلب هذه المعلومات",
    "LOCATION_BENEFIT": "احصل على تنبيهات الطقس وأسعار السوق المحلية والنصائح الزراعية الخاصة بالمنطقة",
    "NAME_BENEFIT": "تلقى تحيات شخصية وبناء الثقة مع مساعد الذكاء الاصطناعي",
    "LANGUAGE_BENEFIT": "احصل على المشورة بلغتك المفضلة لفهم أفضل",
    "GENDER_BENEFIT": "الوصول إلى البرامج الخاصة بالجنس والتوصيات الزراعية المخصصة",
    "ROLE_BENEFIT": "تلقي محتوى يناسب مستوى خبرتك واحتياجاتك المهنية",
    "CROPS_BENEFIT": "احصل على نصائح خاصة بالمحاصيل حول الزراعة ومكافحة الآفات وتوقيت الحصاد",
    "LIVESTOCK_BENEFIT": "تلقي إرشادات مستهدفة حول صحة الحيوان والتغذية والتربية"
  },
  
  // French
  "fr": {
    "PERSONALIZATION_TITLE": "Pourquoi nous demandons ces informations",
    "LOCATION_BENEFIT": "Recevez des alertes météo, les prix du marché local et des conseils agricoles spécifiques à votre région",
    "NAME_BENEFIT": "Recevez des salutations personnalisées et établissez une confiance avec notre assistant IA",
    "LANGUAGE_BENEFIT": "Obtenez des conseils dans votre langue préférée pour une meilleure compréhension",
    "GENDER_BENEFIT": "Accédez à des programmes spécifiques au genre et à des recommandations agricoles personnalisées",
    "ROLE_BENEFIT": "Recevez du contenu adapté à votre niveau d'expertise et à vos besoins professionnels",
    "CROPS_BENEFIT": "Obtenez des conseils spécifiques aux cultures sur la plantation, la lutte antiparasitaire et le moment de la récolte",
    "LIVESTOCK_BENEFIT": "Recevez des conseils ciblés sur la santé animale, l'alimentation et la reproduction"
  },
  
  // Portuguese
  "pt": {
    "PERSONALIZATION_TITLE": "Por que pedimos essas informações",
    "LOCATION_BENEFIT": "Receba alertas meteorológicos, preços do mercado local e conselhos agrícolas específicos da região",
    "NAME_BENEFIT": "Receba saudações personalizadas e construa confiança com nosso assistente de IA",
    "LANGUAGE_BENEFIT": "Obtenha conselhos no seu idioma preferido para melhor compreensão",
    "GENDER_BENEFIT": "Acesse programas específicos de gênero e recomendações agrícolas personalizadas",
    "ROLE_BENEFIT": "Receba conteúdo adequado ao seu nível de especialização e necessidades profissionais",
    "CROPS_BENEFIT": "Obtenha conselhos específicos sobre plantio, controle de pragas e época de colheita",
    "LIVESTOCK_BENEFIT": "Receba orientação direcionada sobre saúde animal, alimentação e reprodução"
  },
  
  // Chinese (Simplified)
  "zh": {
    "PERSONALIZATION_TITLE": "为什么我们需要这些信息",
    "LOCATION_BENEFIT": "获取天气预警、当地市场价格和地区特定的农业建议",
    "NAME_BENEFIT": "接收个性化问候，与我们的AI助手建立信任",
    "LANGUAGE_BENEFIT": "以您偏好的语言获取建议，以便更好地理解",
    "GENDER_BENEFIT": "获取性别特定计划和定制的农业建议",
    "ROLE_BENEFIT": "接收适合您专业水平和职业需求的内容",
    "CROPS_BENEFIT": "获取关于种植、病虫害防治和收获时机的作物特定建议",
    "LIVESTOCK_BENEFIT": "接收关于动物健康、饲养和繁殖的针对性指导"
  },
  
  // Russian
  "ru": {
    "PERSONALIZATION_TITLE": "Почему мы запрашиваем эту информацию",
    "LOCATION_BENEFIT": "Получайте предупреждения о погоде, местные рыночные цены и советы по сельскому хозяйству для вашего региона",
    "NAME_BENEFIT": "Получайте персонализированные приветствия и укрепляйте доверие с нашим ИИ-помощником",
    "LANGUAGE_BENEFIT": "Получайте советы на предпочитаемом языке для лучшего понимания",
    "GENDER_BENEFIT": "Получите доступ к гендерно-ориентированным программам и персонализированным сельскохозяйственным рекомендациям",
    "ROLE_BENEFIT": "Получайте контент, соответствующий вашему уровню знаний и профессиональным потребностям",
    "CROPS_BENEFIT": "Получайте советы по конкретным культурам о посадке, борьбе с вредителями и времени сбора урожая",
    "LIVESTOCK_BENEFIT": "Получайте целевые рекомендации по здоровью животных, кормлению и разведению"
  },
  
  // German
  "de": {
    "PERSONALIZATION_TITLE": "Warum wir diese Informationen benötigen",
    "LOCATION_BENEFIT": "Erhalten Sie Wetterwarnungen, lokale Marktpreise und regionsspezifische Landwirtschaftsberatung",
    "NAME_BENEFIT": "Erhalten Sie personalisierte Begrüßungen und bauen Sie Vertrauen zu unserem KI-Assistenten auf",
    "LANGUAGE_BENEFIT": "Erhalten Sie Ratschläge in Ihrer bevorzugten Sprache für besseres Verständnis",
    "GENDER_BENEFIT": "Zugang zu geschlechtsspezifischen Programmen und maßgeschneiderten landwirtschaftlichen Empfehlungen",
    "ROLE_BENEFIT": "Erhalten Sie Inhalte, die Ihrem Fachwissen und Ihren beruflichen Bedürfnissen entsprechen",
    "CROPS_BENEFIT": "Erhalten Sie kulturspezifische Beratung zu Pflanzung, Schädlingsbekämpfung und Erntezeit",
    "LIVESTOCK_BENEFIT": "Erhalten Sie gezielte Anleitungen zu Tiergesundheit, Fütterung und Zucht"
  },
  
  // Japanese
  "ja": {
    "PERSONALIZATION_TITLE": "なぜこの情報が必要なのか",
    "LOCATION_BENEFIT": "天気予報、地元の市場価格、地域固有の農業アドバイスを取得",
    "NAME_BENEFIT": "パーソナライズされた挨拶を受け取り、AIアシスタントとの信頼関係を構築",
    "LANGUAGE_BENEFIT": "より良い理解のために、お好みの言語でアドバイスを取得",
    "GENDER_BENEFIT": "性別固有のプログラムとカスタマイズされた農業推奨事項へのアクセス",
    "ROLE_BENEFIT": "専門知識レベルと専門的ニーズに適したコンテンツを受信",
    "CROPS_BENEFIT": "植え付け、害虫駆除、収穫時期に関する作物固有のアドバイスを取得",
    "LIVESTOCK_BENEFIT": "動物の健康、飼育、繁殖に関する的を絞ったガイダンスを受信"
  },
  
  // Korean
  "ko": {
    "PERSONALIZATION_TITLE": "왜 이 정보가 필요한가요",
    "LOCATION_BENEFIT": "날씨 알림, 지역 시장 가격 및 지역별 농업 조언 받기",
    "NAME_BENEFIT": "맞춤형 인사말을 받고 AI 도우미와 신뢰 구축",
    "LANGUAGE_BENEFIT": "더 나은 이해를 위해 선호하는 언어로 조언 받기",
    "GENDER_BENEFIT": "성별별 프로그램 및 맞춤형 농업 권장 사항 이용",
    "ROLE_BENEFIT": "전문 지식 수준과 전문적 요구에 맞는 콘텐츠 받기",
    "CROPS_BENEFIT": "재배, 해충 방제 및 수확 시기에 대한 작물별 조언 받기",
    "LIVESTOCK_BENEFIT": "동물 건강, 사료 공급 및 번식에 대한 맞춤형 지침 받기"
  },
  
  // Telugu
  "te": {
    "PERSONALIZATION_TITLE": "మేము ఈ సమాచారం ఎందుకు అడుగుతున్నాము",
    "LOCATION_BENEFIT": "వాతావరణ హెచ్చరికలు, స్థానిక మార్కెట్ ధరలు మరియు ప్రాంత-నిర్దిష్ట వ్యవసాయ సలహా పొందండి",
    "NAME_BENEFIT": "వ్యక్తిగత శుభాకాంక్షలు స్వీకరించండి మరియు మా AI సహాయకుడితో నమ్మకాన్ని పెంచుకోండి",
    "LANGUAGE_BENEFIT": "మెరుగైన అవగాహన కోసం మీకు నచ్చిన భాషలో సలహా పొందండి",
    "GENDER_BENEFIT": "లింగ-నిర్దిష్ట కార్యక్రమాలు మరియు అనుకూలీకరించిన వ్యవసాయ సిఫార్సులకు ప్రాప్యత పొందండి",
    "ROLE_BENEFIT": "మీ నైపుణ్య స్థాయి మరియు వృత్తిపరమైన అవసరాలకు తగిన కంటెంట్ స్వీకరించండి",
    "CROPS_BENEFIT": "నాటడం, పురుగుల నియంత్రణ మరియు పంట కోత సమయంపై పంట-నిర్దిష్ట సలహా పొందండి",
    "LIVESTOCK_BENEFIT": "జంతు ఆరోగ్యం, దాణా మరియు పెంపకంపై లక్ష్య మార్గదర్శకత్వం పొందండి"
  },
  
  // Tamil
  "ta": {
    "PERSONALIZATION_TITLE": "இந்த தகவலை நாங்கள் ஏன் கேட்கிறோம்",
    "LOCATION_BENEFIT": "வானிலை எச்சரிக்கைகள், உள்ளூர் சந்தை விலைகள் மற்றும் பிராந்திய சார்ந்த விவசாய ஆலோசனைகளைப் பெறுங்கள்",
    "NAME_BENEFIT": "தனிப்பயனாக்கப்பட்ட வாழ்த்துக்களைப் பெற்று எங்கள் AI உதவியாளருடன் நம்பிக்கையை வளர்த்துக் கொள்ளுங்கள்",
    "LANGUAGE_BENEFIT": "சிறந்த புரிதலுக்காக உங்களுக்கு விருப்பமான மொழியில் ஆலோசனை பெறுங்கள்",
    "GENDER_BENEFIT": "பாலின-சார்ந்த திட்டங்கள் மற்றும் தனிப்பயனாக்கப்பட்ட விவசாய பரிந்துரைகளை அணுகுங்கள்",
    "ROLE_BENEFIT": "உங்கள் நிபுணத்துவ நிலை மற்றும் தொழில்முறை தேவைகளுக்கு ஏற்ற உள்ளடக்கத்தைப் பெறுங்கள்",
    "CROPS_BENEFIT": "நடவு, பூச்சி கட்டுப்பாடு மற்றும் அறுவடை நேரம் பற்றிய பயிர்-சார்ந்த ஆலோசனையைப் பெறுங்கள்",
    "LIVESTOCK_BENEFIT": "விலங்கு சுகாதாரம், உணவளித்தல் மற்றும் இனப்பெருக்கம் பற்றிய இலக்கு வழிகாட்டுதலைப் பெறுங்கள்"
  },
  
  // Marathi
  "mr": {
    "PERSONALIZATION_TITLE": "आम्ही ही माहिती का विचारतो",
    "LOCATION_BENEFIT": "हवामान इशारे, स्थानिक बाजार किंमती आणि प्रदेश-विशिष्ट शेती सल्ला मिळवा",
    "NAME_BENEFIT": "वैयक्तिक शुभेच्छा मिळवा आणि आमच्या AI सहाय्यकासह विश्वास निर्माण करा",
    "LANGUAGE_BENEFIT": "चांगल्या समजुतीसाठी तुमच्या पसंतीच्या भाषेत सल्ला मिळवा",
    "GENDER_BENEFIT": "लिंग-विशिष्ट कार्यक्रम आणि अनुकूलित कृषी शिफारसींमध्ये प्रवेश मिळवा",
    "ROLE_BENEFIT": "तुमच्या कौशल्य पातळी आणि व्यावसायिक गरजांसाठी योग्य सामग्री मिळवा",
    "CROPS_BENEFIT": "लागवड, कीड नियंत्रण आणि कापणी वेळेबद्दल पीक-विशिष्ट सल्ला मिळवा",
    "LIVESTOCK_BENEFIT": "प्राणी आरोग्य, खाद्य आणि प्रजनन यावर लक्ष्यित मार्गदर्शन मिळवा"
  },
  
  // Gujarati
  "gu": {
    "PERSONALIZATION_TITLE": "અમે આ માહિતી શા માટે માંગીએ છીએ",
    "LOCATION_BENEFIT": "હવામાન ચેતવણીઓ, સ્થાનિક બજાર ભાવો અને પ્રદેશ-વિશિષ્ટ ખેતી સલાહ મેળવો",
    "NAME_BENEFIT": "વ્યક્તિગત શુભેચ્છાઓ મેળવો અને અમારા AI સહાયક સાથે વિશ્વાસ બનાવો",
    "LANGUAGE_BENEFIT": "સારી સમજણ માટે તમારી પસંદગીની ભાષામાં સલાહ મેળવો",
    "GENDER_BENEFIT": "લિંગ-વિશિષ્ટ કાર્યક્રમો અને અનુકૂળ કૃષિ ભલામણો સુધી પહોંચ મેળવો",
    "ROLE_BENEFIT": "તમારા કુશળતા સ્તર અને વ્યાવસાયિક જરૂરિયાતોને અનુરૂપ સામગ્રી મેળવો",
    "CROPS_BENEFIT": "વાવેતર, જંતુ નિયંત્રણ અને કાપણી સમય વિશે પાક-વિશિષ્ટ સલાહ મેળવો",
    "LIVESTOCK_BENEFIT": "પશુ આરોગ્ય, ખોરાક અને સંવર્ધન પર લક્ષિત માર્ગદર્શન મેળવો"
  },
  
  // Kannada
  "kn": {
    "PERSONALIZATION_TITLE": "ನಾವು ಈ ಮಾಹಿತಿಯನ್ನು ಏಕೆ ಕೇಳುತ್ತಿದ್ದೇವೆ",
    "LOCATION_BENEFIT": "ಹವಾಮಾನ ಎಚ್ಚರಿಕೆಗಳು, ಸ್ಥಳೀಯ ಮಾರುಕಟ್ಟೆ ಬೆಲೆಗಳು ಮತ್ತು ಪ್ರದೇಶ-ನಿರ್ದಿಷ್ಟ ಕೃಷಿ ಸಲಹೆ ಪಡೆಯಿರಿ",
    "NAME_BENEFIT": "ವೈಯಕ್ತೀಕರಿಸಿದ ಶುಭಾಶಯಗಳನ್ನು ಸ್ವೀಕರಿಸಿ ಮತ್ತು ನಮ್ಮ AI ಸಹಾಯಕರೊಂದಿಗೆ ನಂಬಿಕೆ ನಿರ್ಮಿಸಿ",
    "LANGUAGE_BENEFIT": "ಉತ್ತಮ ತಿಳುವಳಿಕೆಗಾಗಿ ನಿಮ್ಮ ಆದ್ಯತೆಯ ಭಾಷೆಯಲ್ಲಿ ಸಲಹೆ ಪಡೆಯಿರಿ",
    "GENDER_BENEFIT": "ಲಿಂಗ-ನಿರ್ದಿಷ್ಟ ಕಾರ್ಯಕ್ರಮಗಳು ಮತ್ತು ಅನುಕೂಲಿತ ಕೃಷಿ ಶಿಫಾರಸುಗಳಿಗೆ ಪ್ರವೇಶ ಪಡೆಯಿರಿ",
    "ROLE_BENEFIT": "ನಿಮ್ಮ ಪರಿಣತಿ ಮಟ್ಟ ಮತ್ತು ವೃತ್ತಿಪರ ಅಗತ್ಯಗಳಿಗೆ ಸೂಕ್ತವಾದ ವಿಷಯವನ್ನು ಸ್ವೀಕರಿಸಿ",
    "CROPS_BENEFIT": "ನಾಟಿ, ಕೀಟ ನಿಯಂತ್ರಣ ಮತ್ತು ಕೊಯ್ಲು ಸಮಯದ ಬಗ್ಗೆ ಬೆಳೆ-ನಿರ್ದಿಷ್ಟ ಸಲಹೆ ಪಡೆಯಿರಿ",
    "LIVESTOCK_BENEFIT": "ಪ್ರಾಣಿ ಆರೋಗ್ಯ, ಆಹಾರ ಮತ್ತು ಸಂತಾನೋತ್ಪತ್ತಿ ಬಗ್ಗೆ ಗುರಿ ಮಾರ್ಗದರ್ಶನ ಸ್ವೀಕರಿಸಿ"
  },
  
  // Malayalam
  "ml": {
    "PERSONALIZATION_TITLE": "ഞങ്ങൾ ഈ വിവരങ്ങൾ ചോദിക്കുന്നത് എന്തുകൊണ്ട്",
    "LOCATION_BENEFIT": "കാലാവസ്ഥ മുന്നറിയിപ്പുകൾ, പ്രാദേശിക വിപണി വിലകൾ, പ്രദേശ-നിർദ്ദിഷ്ട കാർഷിക ഉപദേശം എന്നിവ നേടുക",
    "NAME_BENEFIT": "വ്യക്തിഗത ആശംസകൾ സ്വീകരിക്കുകയും ഞങ്ങളുടെ AI സഹായിയുമായി വിശ്വാസം വളർത്തുകയും ചെയ്യുക",
    "LANGUAGE_BENEFIT": "മെച്ചപ്പെട്ട ധാരണയ്ക്കായി നിങ്ങളുടെ ഇഷ്ട ഭാഷയിൽ ഉപദേശം നേടുക",
    "GENDER_BENEFIT": "ലിംഗ-നിർദ്ദിഷ്ട പ്രോഗ്രാമുകളിലേക്കും ഇച്ഛാനുസൃത കാർഷിക ശുപാർശകളിലേക്കും പ്രവേശനം നേടുക",
    "ROLE_BENEFIT": "നിങ്ങളുടെ വൈദഗ്ധ്യ നിലവാരത്തിനും പ്രൊഫഷണൽ ആവശ്യങ്ങൾക്കും അനുയോജ്യമായ ഉള്ളടക്കം സ്വീകരിക്കുക",
    "CROPS_BENEFIT": "നടീൽ, കീട നിയന്ത്രണം, വിളവെടുപ്പ് സമയം എന്നിവയെക്കുറിച്ച് വിള-നിർദ്ദിഷ്ട ഉപദേശം നേടുക",
    "LIVESTOCK_BENEFIT": "മൃഗ ആരോഗ്യം, തീറ്റ, പ്രജനനം എന്നിവയെക്കുറിച്ച് ലക്ഷ്യമിട്ട മാർഗ്ഗനിർദ്ദേശം സ്വീകരിക്കുക"
  },
  
  // Punjabi
  "pa": {
    "PERSONALIZATION_TITLE": "ਅਸੀਂ ਇਹ ਜਾਣਕਾਰੀ ਕਿਉਂ ਮੰਗਦੇ ਹਾਂ",
    "LOCATION_BENEFIT": "ਮੌਸਮ ਦੀਆਂ ਚੇਤਾਵਨੀਆਂ, ਸਥਾਨਕ ਬਾਜ਼ਾਰ ਦੀਆਂ ਕੀਮਤਾਂ ਅਤੇ ਖੇਤਰ-ਵਿਸ਼ੇਸ਼ ਖੇਤੀ ਸਲਾਹ ਪ੍ਰਾਪਤ ਕਰੋ",
    "NAME_BENEFIT": "ਵਿਅਕਤੀਗਤ ਸ਼ੁਭਕਾਮਨਾਵਾਂ ਪ੍ਰਾਪਤ ਕਰੋ ਅਤੇ ਸਾਡੇ AI ਸਹਾਇਕ ਨਾਲ ਵਿਸ਼ਵਾਸ ਬਣਾਓ",
    "LANGUAGE_BENEFIT": "ਬਿਹਤਰ ਸਮਝ ਲਈ ਆਪਣੀ ਪਸੰਦੀਦਾ ਭਾਸ਼ਾ ਵਿੱਚ ਸਲਾਹ ਪ੍ਰਾਪਤ ਕਰੋ",
    "GENDER_BENEFIT": "ਲਿੰਗ-ਵਿਸ਼ੇਸ਼ ਪ੍ਰੋਗਰਾਮਾਂ ਅਤੇ ਅਨੁਕੂਲਿਤ ਖੇਤੀਬਾੜੀ ਸਿਫ਼ਾਰਸ਼ਾਂ ਤੱਕ ਪਹੁੰਚ ਪ੍ਰਾਪਤ ਕਰੋ",
    "ROLE_BENEFIT": "ਆਪਣੇ ਮੁਹਾਰਤ ਦੇ ਪੱਧਰ ਅਤੇ ਪੇਸ਼ੇਵਰ ਲੋੜਾਂ ਦੇ ਅਨੁਕੂਲ ਸਮੱਗਰੀ ਪ੍ਰਾਪਤ ਕਰੋ",
    "CROPS_BENEFIT": "ਬਿਜਾਈ, ਕੀੜੇ ਨਿਯੰਤਰਣ ਅਤੇ ਵਾਢੀ ਦੇ ਸਮੇਂ ਬਾਰੇ ਫਸਲ-ਵਿਸ਼ੇਸ਼ ਸਲਾਹ ਪ੍ਰਾਪਤ ਕਰੋ",
    "LIVESTOCK_BENEFIT": "ਜਾਨਵਰਾਂ ਦੀ ਸਿਹਤ, ਖੁਰਾਕ ਅਤੇ ਪ੍ਰਜਨਨ ਬਾਰੇ ਨਿਸ਼ਾਨਾ ਮਾਰਗਦਰਸ਼ਨ ਪ੍ਰਾਪਤ ਕਰੋ"
  },
  
  // Odia
  "or": {
    "PERSONALIZATION_TITLE": "ଆମେ ଏହି ସୂଚନା କାହିଁକି ପଚାରୁଛୁ",
    "LOCATION_BENEFIT": "ପାଣିପାଗ ସତର୍କତା, ସ୍ଥାନୀୟ ବଜାର ମୂଲ୍ୟ ଏବଂ ଅଞ୍ଚଳ-ନିର୍ଦ୍ଦିଷ୍ଟ କୃଷି ପରାମର୍ଶ ପାଆନ୍ତୁ",
    "NAME_BENEFIT": "ବ୍ୟକ୍ତିଗତ ଅଭିବାଦନ ଗ୍ରହଣ କରନ୍ତୁ ଏବଂ ଆମର AI ସହାୟକ ସହିତ ବିଶ୍ୱାସ ଗଢ଼ନ୍ତୁ",
    "LANGUAGE_BENEFIT": "ଉତ୍ତମ ବୁଝାମଣା ପାଇଁ ଆପଣଙ୍କର ପସନ୍ଦର ଭାଷାରେ ପରାମର୍ଶ ପାଆନ୍ତୁ",
    "GENDER_BENEFIT": "ଲିଙ୍ଗ-ନିର୍ଦ୍ଦିଷ୍ଟ କାର୍ଯ୍ୟକ୍ରମ ଏବଂ ବ୍ୟକ୍ତିଗତ କୃଷି ସୁପାରିଶରେ ପ୍ରବେଶ ପାଆନ୍ତୁ",
    "ROLE_BENEFIT": "ଆପଣଙ୍କର ଦକ୍ଷତା ସ୍ତର ଏବଂ ବୃତ୍ତିଗତ ଆବଶ୍ୟକତା ଅନୁଯାୟୀ ବିଷୟବସ୍ତୁ ଗ୍ରହଣ କରନ୍ତୁ",
    "CROPS_BENEFIT": "ରୋପଣ, କୀଟ ନିୟନ୍ତ୍ରଣ ଏବଂ ଅମଳ ସମୟ ବିଷୟରେ ଫସଲ-ନିର୍ଦ୍ଦିଷ୍ଟ ପରାମର୍ଶ ପାଆନ୍ତୁ",
    "LIVESTOCK_BENEFIT": "ପଶୁ ସ୍ୱାସ୍ଥ୍ୟ, ଖାଦ୍ୟ ଏବଂ ପ୍ରଜନନ ବିଷୟରେ ଲକ୍ଷ୍ୟିତ ମାର୍ଗଦର୍ଶନ ଗ୍ରହଣ କରନ୍ତୁ"
  },
  
  // Assamese
  "as": {
    "PERSONALIZATION_TITLE": "আমি এই তথ্য কিয় বিচাৰোঁ",
    "LOCATION_BENEFIT": "বতৰৰ সতৰ্কতা, স্থানীয় বজাৰ মূল্য আৰু অঞ্চল-নিৰ্দিষ্ট কৃষি পৰামৰ্শ লাভ কৰক",
    "NAME_BENEFIT": "ব্যক্তিগত শুভেচ্ছা লাভ কৰক আৰু আমাৰ AI সহায়কৰ সৈতে বিশ্বাস গঢ়ি তোলক",
    "LANGUAGE_BENEFIT": "উন্নত বুজাবুজিৰ বাবে আপোনাৰ পছন্দৰ ভাষাত পৰামৰ্শ লাভ কৰক",
    "GENDER_BENEFIT": "লিংগ-নিৰ্দিষ্ট কাৰ্যসূচী আৰু কাষ্টমাইজড কৃষি পৰামৰ্শৰ সুবিধা লাভ কৰক",
    "ROLE_BENEFIT": "আপোনাৰ দক্ষতা স্তৰ আৰু পেছাদাৰী প্ৰয়োজনৰ উপযুক্ত সমল লাভ কৰক",
    "CROPS_BENEFIT": "ৰোপণ, কীট নিয়ন্ত্ৰণ আৰু চপোৱাৰ সময় সম্পৰ্কে শস্য-নিৰ্দিষ্ট পৰামৰ্শ লাভ কৰক",
    "LIVESTOCK_BENEFIT": "পশু স্বাস্থ্য, খাদ্য আৰু প্ৰজনন সম্পৰ্কে লক্ষ্যবদ্ধ নিৰ্দেশনা লাভ কৰক"
  },
  
  // Urdu
  "ur": {
    "PERSONALIZATION_TITLE": "ہم یہ معلومات کیوں مانگتے ہیں",
    "LOCATION_BENEFIT": "موسم کی تنبیہات، مقامی بازار کی قیمتیں اور علاقہ مخصوص زرعی مشورہ حاصل کریں",
    "NAME_BENEFIT": "ذاتی مبارکباد حاصل کریں اور ہمارے AI اسسٹنٹ کے ساتھ اعتماد بنائیں",
    "LANGUAGE_BENEFIT": "بہتر تفہیم کے لیے اپنی پسندیدہ زبان میں مشورہ حاصل کریں",
    "GENDER_BENEFIT": "جنس مخصوص پروگراموں اور حسب ضرورت زرعی سفارشات تک رسائی حاصل کریں",
    "ROLE_BENEFIT": "اپنی مہارت کی سطح اور پیشہ ورانہ ضروریات کے لیے موزوں مواد حاصل کریں",
    "CROPS_BENEFIT": "کاشت، کیڑوں کے کنٹرول اور فصل کاٹنے کے وقت کے بارے میں فصل مخصوص مشورہ حاصل کریں",
    "LIVESTOCK_BENEFIT": "جانوروں کی صحت، خوراک اور افزائش کے بارے میں ہدفی رہنمائی حاصل کریں"
  },
  
  // Amharic
  "am": {
    "PERSONALIZATION_TITLE": "ይህንን መረጃ የምንጠይቀው ለምንድን ነው",
    "LOCATION_BENEFIT": "የአየር ሁኔታ ማስጠንቀቂያዎች፣ የአካባቢ ገበያ ዋጋዎች እና ክልል-ተኮር የግብርና ምክር ያግኙ",
    "NAME_BENEFIT": "ግላዊ ሰላምታዎችን ይቀበሉ እና ከAI ረዳታችን ጋር እምነት ይገንቡ",
    "LANGUAGE_BENEFIT": "ለተሻለ ግንዛቤ በመረጡት ቋንቋ ምክር ያግኙ",
    "GENDER_BENEFIT": "ጾታ-ተኮር ፕሮግራሞችን እና የተበጁ የግብርና ምክሮችን ይድረሱ",
    "ROLE_BENEFIT": "ለችሎታ ደረጃዎ እና ሙያዊ ፍላጎቶችዎ የሚስማማ ይዘት ይቀበሉ",
    "CROPS_BENEFIT": "ስለ መትከል፣ ተባይ መቆጣጠሪያ እና የመሰብሰብ ጊዜ ሰብል-ተኮር ምክር ያግኙ",
    "LIVESTOCK_BENEFIT": "ስለ እንስሳ ጤና፣ መመገብ እና መራባት የታለመ መመሪያ ይቀበሉ"
  },
  
  // Hausa
  "ha": {
    "PERSONALIZATION_TITLE": "Me yasa muke neman wannan bayanin",
    "LOCATION_BENEFIT": "Sami gargadin yanayi, farashin kasuwa na gida da shawarwarin noma na musamman na yanki",
    "NAME_BENEFIT": "Karɓi gaisuwa ta musamman kuma ka gina amincewa da mataimakin AI ɗinmu",
    "LANGUAGE_BENEFIT": "Sami shawara a cikin yaren da kuka fi so don ƙarin fahimta",
    "GENDER_BENEFIT": "Samun damar shiga shirye-shiryen jinsi da shawarwarin noma na musamman",
    "ROLE_BENEFIT": "Karɓi abun ciki wanda ya dace da matakin ƙwarewar ku da bukatun sana'a",
    "CROPS_BENEFIT": "Sami shawarwarin amfanin gona game da shuka, sarrafa kwari da lokacin girbi",
    "LIVESTOCK_BENEFIT": "Karɓi jagora kan lafiyar dabbobi, ciyarwa da kiwo"
  },
  
  // Yoruba
  "yo": {
    "PERSONALIZATION_TITLE": "Kini idi ti a fi n beere alaye yii",
    "LOCATION_BENEFIT": "Gba ikilọ oju-ọjọ, idiyele ọja agbegbe ati imọran ogbin ti agbegbe pato",
    "NAME_BENEFIT": "Gba ikini ti ara ẹni ki o si kọ igbẹkẹle pẹlu oluranlọwọ AI wa",
    "LANGUAGE_BENEFIT": "Gba imọran ni ede ti o fẹ fun oye to dara julọ",
    "GENDER_BENEFIT": "Wọle si awọn eto akọ-n-bọ ati awọn iṣeduro ogbin ti a ṣe deede",
    "ROLE_BENEFIT": "Gba akoonu ti o baamu pẹlu ipele imọ-ẹrọ rẹ ati awọn aini alamọdaju",
    "CROPS_BENEFIT": "Gba imọran ohun ọgbin pato lori gbingbin, iṣakoso kokoro ati akoko ikore",
    "LIVESTOCK_BENEFIT": "Gba itọsọna ti a fojusi lori ilera ẹranko, ifunni ati ibisi"
  },
  
  // Igbo
  "ig": {
    "PERSONALIZATION_TITLE": "Ihe mere anyị ji na-ajụ ozi a",
    "LOCATION_BENEFIT": "Nweta ọkwa ihu igwe, ọnụahịa ahịa mpaghara na ndụmọdụ ọrụ ugbo nke mpaghara",
    "NAME_BENEFIT": "Nata ekele nkeonwe wee wulite ntụkwasị obi na onye enyemaka AI anyị",
    "LANGUAGE_BENEFIT": "Nweta ndụmọdụ n'asụsụ ịmasịrị gị maka nghọta ka mma",
    "GENDER_BENEFIT": "Nweta ohere ịbanye na mmemme okike na ndụmọdụ ọrụ ugbo ahaziri",
    "ROLE_BENEFIT": "Nata ọdịnaya dabara na ọkwa nka gị na mkpa ọrụ aka",
    "CROPS_BENEFIT": "Nweta ndụmọdụ ihe ọkụkụ gbasara ịkụ ihe, njikwa ụmụ ahụhụ na oge owuwe ihe ubi",
    "LIVESTOCK_BENEFIT": "Nata nduzi lekwasịrị anya na ahụike anụmanụ, nri na ọmụmụ"
  },
  
  // Zulu
  "zu": {
    "PERSONALIZATION_TITLE": "Kungani sicela lolu lwazi",
    "LOCATION_BENEFIT": "Thola izexwayiso zesimo sezulu, amanani emakethe yendawo nezeluleko zokulima eziqondene nendawo",
    "NAME_BENEFIT": "Yamukela imibingelelo yomuntu siqu futhi wakhe ukwethembana nomsizi wethu we-AI",
    "LANGUAGE_BENEFIT": "Thola izeluleko ngolimi lwakho olukhethayo ukuze uqonde kangcono",
    "GENDER_BENEFIT": "Finyelela izinhlelo eziqondene nobulili nezincomo zokulima ezenzelwe wena",
    "ROLE_BENEFIT": "Yamukela okuqukethwe okufanele izinga lakho lobuchwepheshe nezidingo zomsebenzi",
    "CROPS_BENEFIT": "Thola izeluleko eziqondene nezitshalo mayelana nokutshala, ukulawula izinambuzane nesikhathi sokuvuna",
    "LIVESTOCK_BENEFIT": "Yamukela isiqondiso esiqondisiwe mayelana nempilo yezilwane, ukondla nokuzalanisa"
  },
  
  // Xhosa
  "xh": {
    "PERSONALIZATION_TITLE": "Kutheni sicela olu lwazi",
    "LOCATION_BENEFIT": "Fumana izilumkiso zemozulu, amaxabiso emarike yendawo kunye neengcebiso zolimo ezijolise kummandla",
    "NAME_BENEFIT": "Yamkela imibuliso yobuqu kwaye wakhe ukuthembana nomncedisi wethu we-AI",
    "LANGUAGE_BENEFIT": "Fumana iingcebiso ngolwimi lwakho olukhethayo ukuze uqonde ngcono",
    "GENDER_BENEFIT": "Fikelela kwiinkqubo ezijolise kwisini kunye neengcebiso zolimo ezilungiselelwe wena",
    "ROLE_BENEFIT": "Yamkela umxholo olungele inqanaba lakho lobuchule kunye neemfuno zobungcali",
    "CROPS_BENEFIT": "Fumana iingcebiso ezijolise kwizityalo malunga nokutyala, ukulawula izinambuzane kunye nexesha lokuvuna",
    "LIVESTOCK_BENEFIT": "Yamkela isikhokelo esijoliswe kwimpilo yezilwane, ukondla kunye nokuzala"
  },
  
  // Afrikaans
  "af": {
    "PERSONALIZATION_TITLE": "Hoekom ons hierdie inligting vra",
    "LOCATION_BENEFIT": "Kry weerwaarskuwings, plaaslike markpryse en streekspesifieke landbouadvies",
    "NAME_BENEFIT": "Ontvang persoonlike groete en bou vertroue met ons KI-assistent",
    "LANGUAGE_BENEFIT": "Kry advies in jou voorkeur taal vir beter begrip",
    "GENDER_BENEFIT": "Kry toegang tot geslagspesifieke programme en pasgemaakte landbouaanbevelings",
    "ROLE_BENEFIT": "Ontvang inhoud wat pas by jou kundigheids vlak en professionele behoeftes",
    "CROPS_BENEFIT": "Kry gewas-spesifieke advies oor plant, plaagbeheer en oestyd",
    "LIVESTOCK_BENEFIT": "Ontvang geteikende leiding oor dieregesondheid, voeding en teling"
  },
  
  // Indonesian
  "id": {
    "PERSONALIZATION_TITLE": "Mengapa kami meminta informasi ini",
    "LOCATION_BENEFIT": "Dapatkan peringatan cuaca, harga pasar lokal, dan saran pertanian khusus wilayah",
    "NAME_BENEFIT": "Terima salam personal dan bangun kepercayaan dengan asisten AI kami",
    "LANGUAGE_BENEFIT": "Dapatkan saran dalam bahasa pilihan Anda untuk pemahaman yang lebih baik",
    "GENDER_BENEFIT": "Akses program khusus gender dan rekomendasi pertanian yang disesuaikan",
    "ROLE_BENEFIT": "Terima konten yang sesuai dengan tingkat keahlian dan kebutuhan profesional Anda",
    "CROPS_BENEFIT": "Dapatkan saran khusus tanaman tentang penanaman, pengendalian hama, dan waktu panen",
    "LIVESTOCK_BENEFIT": "Terima panduan yang ditargetkan tentang kesehatan hewan, pemberian pakan, dan pembiakan"
  },
  
  // Malay
  "ms": {
    "PERSONALIZATION_TITLE": "Mengapa kami meminta maklumat ini",
    "LOCATION_BENEFIT": "Dapatkan amaran cuaca, harga pasaran tempatan dan nasihat pertanian khusus wilayah",
    "NAME_BENEFIT": "Terima ucapan peribadi dan bina kepercayaan dengan pembantu AI kami",
    "LANGUAGE_BENEFIT": "Dapatkan nasihat dalam bahasa pilihan anda untuk pemahaman yang lebih baik",
    "GENDER_BENEFIT": "Akses program khusus jantina dan cadangan pertanian yang disesuaikan",
    "ROLE_BENEFIT": "Terima kandungan yang sesuai dengan tahap kepakaran dan keperluan profesional anda",
    "CROPS_BENEFIT": "Dapatkan nasihat khusus tanaman tentang penanaman, kawalan perosak dan masa menuai",
    "LIVESTOCK_BENEFIT": "Terima panduan yang disasarkan mengenai kesihatan haiwan, pemberian makanan dan pembiakan"
  },
  
  // Thai
  "th": {
    "PERSONALIZATION_TITLE": "ทำไมเราถึงขอข้อมูลนี้",
    "LOCATION_BENEFIT": "รับการแจ้งเตือนสภาพอากาศ ราคาตลาดท้องถิ่น และคำแนะนำการเกษตรเฉพาะพื้นที่",
    "NAME_BENEFIT": "รับคำทักทายส่วนตัวและสร้างความไว้วางใจกับผู้ช่วย AI ของเรา",
    "LANGUAGE_BENEFIT": "รับคำแนะนำในภาษาที่คุณต้องการเพื่อความเข้าใจที่ดีขึ้น",
    "GENDER_BENEFIT": "เข้าถึงโปรแกรมเฉพาะเพศและคำแนะนำการเกษตรที่ปรับแต่ง",
    "ROLE_BENEFIT": "รับเนื้อหาที่เหมาะกับระดับความเชี่ยวชาญและความต้องการทางอาชีพของคุณ",
    "CROPS_BENEFIT": "รับคำแนะนำเฉพาะพืชเกี่ยวกับการปลูก การควบคุมศัตรูพืช และเวลาเก็บเกี่ยว",
    "LIVESTOCK_BENEFIT": "รับคำแนะนำเฉพาะเกี่ยวกับสุขภาพสัตว์ การให้อาหาร และการผสมพันธุ์"
  },
  
  // Vietnamese
  "vi": {
    "PERSONALIZATION_TITLE": "Tại sao chúng tôi yêu cầu thông tin này",
    "LOCATION_BENEFIT": "Nhận cảnh báo thời tiết, giá thị trường địa phương và lời khuyên nông nghiệp theo khu vực",
    "NAME_BENEFIT": "Nhận lời chào cá nhân và xây dựng niềm tin với trợ lý AI của chúng tôi",
    "LANGUAGE_BENEFIT": "Nhận lời khuyên bằng ngôn ngữ ưa thích của bạn để hiểu rõ hơn",
    "GENDER_BENEFIT": "Truy cập các chương trình theo giới tính và khuyến nghị nông nghiệp được tùy chỉnh",
    "ROLE_BENEFIT": "Nhận nội dung phù hợp với trình độ chuyên môn và nhu cầu nghề nghiệp của bạn",
    "CROPS_BENEFIT": "Nhận lời khuyên cụ thể về cây trồng về việc trồng, kiểm soát sâu bệnh và thời gian thu hoạch",
    "LIVESTOCK_BENEFIT": "Nhận hướng dẫn mục tiêu về sức khỏe động vật, cho ăn và nhân giống"
  },
  
  // Filipino
  "fil": {
    "PERSONALIZATION_TITLE": "Bakit namin hinihiling ang impormasyong ito",
    "LOCATION_BENEFIT": "Makatanggap ng mga babala sa panahon, presyo ng lokal na merkado at payo sa pagsasaka na tukoy sa rehiyon",
    "NAME_BENEFIT": "Makatanggap ng personal na pagbati at bumuo ng tiwala sa aming AI assistant",
    "LANGUAGE_BENEFIT": "Makakuha ng payo sa iyong piniling wika para sa mas mainam na pag-unawa",
    "GENDER_BENEFIT": "Ma-access ang mga programang tukoy sa kasarian at mga naka-customize na rekomendasyon sa agrikultura",
    "ROLE_BENEFIT": "Makatanggap ng nilalaman na angkop sa iyong antas ng kadalubhasaan at mga pangangailangan sa propesyon",
    "CROPS_BENEFIT": "Makakuha ng payong tukoy sa pananim tungkol sa pagtatanim, pagkontrol sa peste at oras ng pag-ani",
    "LIVESTOCK_BENEFIT": "Makatanggap ng nakatuong gabay sa kalusugan ng hayop, pagpapakain at pagpaparami"
  },
  
  // Khmer
  "km": {
    "PERSONALIZATION_TITLE": "ហេតុអ្វីបានជាយើងស្នើសុំព័ត៌មាននេះ",
    "LOCATION_BENEFIT": "ទទួលបានការព្រមានអំពីអាកាសធាតុ តម្លៃទីផ្សារក្នុងតំបន់ និងដំបូន្មានកសិកម្មជាក់លាក់តាមតំបន់",
    "NAME_BENEFIT": "ទទួលការស្វាគមន៍ផ្ទាល់ខ្លួន និងកសាងទំនុកចិត្តជាមួយជំនួយការ AI របស់យើង",
    "LANGUAGE_BENEFIT": "ទទួលបានដំបូន្មានជាភាសាដែលអ្នកពេញចិត្តដើម្បីការយល់ដឹងកាន់តែប្រសើរ",
    "GENDER_BENEFIT": "ទទួលបានការចូលប្រើកម្មវិធីជាក់លាក់តាមភេទ និងអនុសាសន៍កសិកម្មដែលបានកែតម្រូវ",
    "ROLE_BENEFIT": "ទទួលបានមាតិកាដែលសមស្របនឹងកម្រិតជំនាញ និងតម្រូវការវិជ្ជាជីវៈរបស់អ្នក",
    "CROPS_BENEFIT": "ទទួលបានដំបូន្មានជាក់លាក់អំពីដំណាំអំពីការដាំដុះ ការគ្រប់គ្រងសត្វល្អិត និងពេលវេលាប្រមូលផល",
    "LIVESTOCK_BENEFIT": "ទទួលបានការណែនាំគោលដៅអំពីសុខភាពសត្វ ការចិញ្ចឹម និងការបង្កាត់ពូជ"
  },
  
  // Lao
  "lo": {
    "PERSONALIZATION_TITLE": "ເປັນຫຍັງພວກເຮົາຈຶ່ງຂໍຂໍ້ມູນນີ້",
    "LOCATION_BENEFIT": "ໄດ້ຮັບການເຕືອນໄພອາກາດ, ລາຄາຕະຫຼາດທ້ອງຖິ່ນ ແລະຄໍາແນະນໍາການກະສິກໍາສະເພາະພື້ນທີ່",
    "NAME_BENEFIT": "ໄດ້ຮັບການທັກທາຍສ່ວນຕົວ ແລະສ້າງຄວາມໄວ້ວາງໃຈກັບຜູ້ຊ່ວຍ AI ຂອງພວກເຮົາ",
    "LANGUAGE_BENEFIT": "ໄດ້ຮັບຄໍາແນະນໍາໃນພາສາທີ່ທ່ານມັກເພື່ອຄວາມເຂົ້າໃຈທີ່ດີກວ່າ",
    "GENDER_BENEFIT": "ເຂົ້າເຖິງໂຄງການສະເພາະເພດ ແລະຄໍາແນະນໍາການກະສິກໍາທີ່ປັບແຕ່ງ",
    "ROLE_BENEFIT": "ໄດ້ຮັບເນື້ອຫາທີ່ເໝາະສົມກັບລະດັບຄວາມຊໍານານ ແລະຄວາມຕ້ອງການດ້ານວິຊາຊີບຂອງທ່ານ",
    "CROPS_BENEFIT": "ໄດ້ຮັບຄໍາແນະນໍາສະເພາະພືດກ່ຽວກັບການປູກ, ການຄວບຄຸມສັດຕູພືດ ແລະເວລາເກັບກ່ຽວ",
    "LIVESTOCK_BENEFIT": "ໄດ້ຮັບຄໍາແນະນໍາເປົ້າໝາຍກ່ຽວກັບສຸຂະພາບສັດ, ການໃຫ້ອາຫານ ແລະການປັບປຸງພັນ"
  },
  
  // Burmese
  "my": {
    "PERSONALIZATION_TITLE": "ကျွန်ုပ်တို့သည် ဤအချက်အလက်များကို အဘယ်ကြောင့် တောင်းခံသနည်း",
    "LOCATION_BENEFIT": "ရာသီဥတုသတိပေးချက်များ၊ ဒေသဆိုင်ရာစျေးနှုန်းများနှင့် ဒေသအလိုက် စိုက်ပျိုးရေးအကြံဉာဏ်များ ရယူပါ",
    "NAME_BENEFIT": "ပုဂ္ဂိုလ်ရေးဆိုင်ရာ နှုတ်ဆက်စကားများ လက်ခံပြီး ကျွန်ုပ်တို့၏ AI လက်ထောက်နှင့် ယုံကြည်မှု တည်ဆောက်ပါ",
    "LANGUAGE_BENEFIT": "ပိုမိုနားလည်မှုအတွက် သင်နှစ်သက်သော ဘာသာစကားဖြင့် အကြံဉာဏ်များ ရယူပါ",
    "GENDER_BENEFIT": "လိင်အလိုက် အစီအစဉ်များနှင့် စိတ်ကြိုက်ပြင်ဆင်ထားသော စိုက်ပျိုးရေး အကြံပြုချက်များကို ရယူပါ",
    "ROLE_BENEFIT": "သင်၏ ကျွမ်းကျင်မှုအဆင့်နှင့် အသက်မွေးဝမ်းကြောင်း လိုအပ်ချက်များနှင့် ကိုက်ညီသော အကြောင်းအရာများ လက်ခံပါ",
    "CROPS_BENEFIT": "စိုက်ပျိုးခြင်း၊ ပိုးမွှားထိန်းချုပ်ခြင်းနှင့် ရိတ်သိမ်းချိန်အကြောင်း သီးနှံအလိုက် အကြံဉာဏ်များ ရယူပါ",
    "LIVESTOCK_BENEFIT": "တိရစ္ဆာန်ကျန်းမာရေး၊ အစာကျွေးခြင်းနှင့် မျိုးပွားခြင်းဆိုင်ရာ ရည်မှန်းထားသော လမ်းညွှန်ချက်များ လက်ခံပါ"
  },
  
  // Italian
  "it": {
    "PERSONALIZATION_TITLE": "Perché richiediamo queste informazioni",
    "LOCATION_BENEFIT": "Ricevi avvisi meteo, prezzi del mercato locale e consigli agricoli specifici per la regione",
    "NAME_BENEFIT": "Ricevi saluti personalizzati e costruisci fiducia con il nostro assistente AI",
    "LANGUAGE_BENEFIT": "Ottieni consigli nella tua lingua preferita per una migliore comprensione",
    "GENDER_BENEFIT": "Accedi a programmi specifici per genere e raccomandazioni agricole personalizzate",
    "ROLE_BENEFIT": "Ricevi contenuti adatti al tuo livello di competenza e alle esigenze professionali",
    "CROPS_BENEFIT": "Ottieni consigli specifici sulle colture riguardo semina, controllo dei parassiti e tempi di raccolta",
    "LIVESTOCK_BENEFIT": "Ricevi indicazioni mirate su salute animale, alimentazione e allevamento"
  },
  
  // Dutch
  "nl": {
    "PERSONALIZATION_TITLE": "Waarom we deze informatie vragen",
    "LOCATION_BENEFIT": "Ontvang weerwaarschuwingen, lokale marktprijzen en regiospecifiek landbouwadvies",
    "NAME_BENEFIT": "Ontvang persoonlijke begroetingen en bouw vertrouwen op met onze AI-assistent",
    "LANGUAGE_BENEFIT": "Krijg advies in uw voorkeurstaal voor beter begrip",
    "GENDER_BENEFIT": "Toegang tot genderspecifieke programma's en op maat gemaakte landbouwaanbevelingen",
    "ROLE_BENEFIT": "Ontvang inhoud die past bij uw expertiseniveau en professionele behoeften",
    "CROPS_BENEFIT": "Krijg gewasspecifiek advies over planten, ongediertebestrijding en oogsttijd",
    "LIVESTOCK_BENEFIT": "Ontvang gerichte begeleiding over diergezondheid, voeding en fokkerij"
  },
  
  // Polish
  "pl": {
    "PERSONALIZATION_TITLE": "Dlaczego prosimy o te informacje",
    "LOCATION_BENEFIT": "Otrzymuj ostrzeżenia pogodowe, lokalne ceny rynkowe i porady rolnicze specyficzne dla regionu",
    "NAME_BENEFIT": "Otrzymuj spersonalizowane pozdrowienia i buduj zaufanie z naszym asystentem AI",
    "LANGUAGE_BENEFIT": "Otrzymuj porady w preferowanym języku dla lepszego zrozumienia",
    "GENDER_BENEFIT": "Uzyskaj dostęp do programów specyficznych dla płci i spersonalizowanych zaleceń rolniczych",
    "ROLE_BENEFIT": "Otrzymuj treści dostosowane do Twojego poziomu wiedzy i potrzeb zawodowych",
    "CROPS_BENEFIT": "Otrzymuj porady dotyczące upraw w zakresie sadzenia, zwalczania szkodników i czasu zbiorów",
    "LIVESTOCK_BENEFIT": "Otrzymuj ukierunkowane wskazówki dotyczące zdrowia zwierząt, żywienia i hodowli"
  },
  
  // Ukrainian
  "uk": {
    "PERSONALIZATION_TITLE": "Чому ми запитуємо цю інформацію",
    "LOCATION_BENEFIT": "Отримуйте попередження про погоду, місцеві ринкові ціни та поради щодо сільського господарства для вашого регіону",
    "NAME_BENEFIT": "Отримуйте персоналізовані привітання та зміцнюйте довіру з нашим ІІ-помічником",
    "LANGUAGE_BENEFIT": "Отримуйте поради вашою рідною мовою для кращого розуміння",
    "GENDER_BENEFIT": "Отримайте доступ до гендерно-орієнтованих програм та персоналізованих сільськогосподарських рекомендацій",
    "ROLE_BENEFIT": "Отримуйте контент, що відповідає вашому рівню знань та професійним потребам",
    "CROPS_BENEFIT": "Отримуйте поради щодо конкретних культур про посадку, боротьбу зі шкідниками та час збору врожаю",
    "LIVESTOCK_BENEFIT": "Отримуйте цільові рекомендації щодо здоров'я тварин, годування та розведення"
  },
  
  // Romanian
  "ro": {
    "PERSONALIZATION_TITLE": "De ce solicităm aceste informații",
    "LOCATION_BENEFIT": "Primiți alerte meteo, prețuri de piață locale și sfaturi agricole specifice regiunii",
    "NAME_BENEFIT": "Primiți salutări personalizate și construiți încredere cu asistentul nostru AI",
    "LANGUAGE_BENEFIT": "Obțineți sfaturi în limba preferată pentru o mai bună înțelegere",
    "GENDER_BENEFIT": "Accesați programe specifice genului și recomandări agricole personalizate",
    "ROLE_BENEFIT": "Primiți conținut potrivit nivelului dvs. de expertiză și nevoilor profesionale",
    "CROPS_BENEFIT": "Obțineți sfaturi specifice culturilor despre plantare, controlul dăunătorilor și timpul de recoltare",
    "LIVESTOCK_BENEFIT": "Primiți îndrumări țintite privind sănătatea animalelor, hrănirea și reproducerea"
  },
  
  // Greek
  "el": {
    "PERSONALIZATION_TITLE": "Γιατί ζητάμε αυτές τις πληροφορίες",
    "LOCATION_BENEFIT": "Λάβετε ειδοποιήσεις καιρού, τοπικές τιμές αγοράς και συμβουλές γεωργίας ειδικές για την περιοχή",
    "NAME_BENEFIT": "Λάβετε εξατομικευμένους χαιρετισμούς και χτίστε εμπιστοσύνη με τον AI βοηθό μας",
    "LANGUAGE_BENEFIT": "Λάβετε συμβουλές στη γλώσσα που προτιμάτε για καλύτερη κατανόηση",
    "GENDER_BENEFIT": "Αποκτήστε πρόσβαση σε προγράμματα ειδικά για το φύλο και εξατομικευμένες γεωργικές συστάσεις",
    "ROLE_BENEFIT": "Λάβετε περιεχόμενο που ταιριάζει στο επίπεδο εμπειρίας και τις επαγγελματικές σας ανάγκες",
    "CROPS_BENEFIT": "Λάβετε συμβουλές ειδικές για καλλιέργειες σχετικά με τη φύτευση, τον έλεγχο παρασίτων και τον χρόνο συγκομιδής",
    "LIVESTOCK_BENEFIT": "Λάβετε στοχευμένη καθοδήγηση για την υγεία των ζώων, τη διατροφή και την αναπαραγωγή"
  },
  
  // Czech
  "cs": {
    "PERSONALIZATION_TITLE": "Proč požadujeme tyto informace",
    "LOCATION_BENEFIT": "Získejte upozornění na počasí, místní tržní ceny a zemědělské rady specifické pro region",
    "NAME_BENEFIT": "Přijímejte osobní pozdravy a budujte důvěru s naším AI asistentem",
    "LANGUAGE_BENEFIT": "Získejte rady ve vašem preferovaném jazyce pro lepší porozumění",
    "GENDER_BENEFIT": "Získejte přístup k programům specifickým pro pohlaví a přizpůsobeným zemědělským doporučením",
    "ROLE_BENEFIT": "Přijímejte obsah odpovídající vaší úrovni odbornosti a profesním potřebám",
    "CROPS_BENEFIT": "Získejte rady specifické pro plodiny o výsadbě, kontrole škůdců a době sklizně",
    "LIVESTOCK_BENEFIT": "Přijímejte cílené pokyny o zdraví zvířat, krmení a chovu"
  },
  
  // Hungarian
  "hu": {
    "PERSONALIZATION_TITLE": "Miért kérjük ezeket az információkat",
    "LOCATION_BENEFIT": "Kapjon időjárási figyelmeztetéseket, helyi piaci árakat és régióspecifikus mezőgazdasági tanácsokat",
    "NAME_BENEFIT": "Kapjon személyre szabott üdvözleteket és építsen bizalmat AI asszisztensünkkel",
    "LANGUAGE_BENEFIT": "Kapjon tanácsokat az Ön által preferált nyelven a jobb megértés érdekében",
    "GENDER_BENEFIT": "Hozzáférés nemspecifikus programokhoz és testreszabott mezőgazdasági ajánlásokhoz",
    "ROLE_BENEFIT": "Kapjon szakértelmének és szakmai igényeinek megfelelő tartalmat",
    "CROPS_BENEFIT": "Kapjon növényspecifikus tanácsokat az ültetésről, kártevő-szabályozásról és betakarítási időről",
    "LIVESTOCK_BENEFIT": "Kapjon célzott útmutatást az állategészségügyről, takarmányozásról és tenyésztésről"
  },
  
  // Swedish
  "sv": {
    "PERSONALIZATION_TITLE": "Varför vi ber om denna information",
    "LOCATION_BENEFIT": "Få vädervarningar, lokala marknadspriser och regionspecifika jordbruksråd",
    "NAME_BENEFIT": "Ta emot personliga hälsningar och bygg förtroende med vår AI-assistent",
    "LANGUAGE_BENEFIT": "Få råd på ditt föredragna språk för bättre förståelse",
    "GENDER_BENEFIT": "Få tillgång till könsspecifika program och skräddarsydda jordbruksrekommendationer",
    "ROLE_BENEFIT": "Ta emot innehåll som passar din expertisnivå och professionella behov",
    "CROPS_BENEFIT": "Få grödspecifika råd om plantering, skadedjursbekämpning och skördetid",
    "LIVESTOCK_BENEFIT": "Ta emot riktad vägledning om djurhälsa, utfodring och avel"
  },
  
  // Danish
  "da": {
    "PERSONALIZATION_TITLE": "Hvorfor vi beder om disse oplysninger",
    "LOCATION_BENEFIT": "Få vejradvarsler, lokale markedspriser og regionsspecifikke landbrugsråd",
    "NAME_BENEFIT": "Modtag personlige hilsner og opbyg tillid med vores AI-assistent",
    "LANGUAGE_BENEFIT": "Få råd på dit foretrukne sprog for bedre forståelse",
    "GENDER_BENEFIT": "Få adgang til kønsspecifikke programmer og skræddersyede landbrugsanbefalinger",
    "ROLE_BENEFIT": "Modtag indhold, der passer til dit ekspertiseniveau og professionelle behov",
    "CROPS_BENEFIT": "Få afgrødespecifikke råd om plantning, skadedyrsbekæmpelse og høsttid",
    "LIVESTOCK_BENEFIT": "Modtag målrettet vejledning om dyresundhed, fodring og avl"
  },
  
  // Finnish
  "fi": {
    "PERSONALIZATION_TITLE": "Miksi pyydämme näitä tietoja",
    "LOCATION_BENEFIT": "Saa säävaroituksia, paikallisia markkinahintoja ja aluekohtaisia maatalousneuvoja",
    "NAME_BENEFIT": "Vastaanota henkilökohtaisia tervehdyksiä ja rakenna luottamusta AI-avustajamme kanssa",
    "LANGUAGE_BENEFIT": "Saa neuvoja haluamallasi kielellä paremman ymmärryksen saavuttamiseksi",
    "GENDER_BENEFIT": "Pääse sukupuolikohtaisiin ohjelmiin ja räätälöityihin maataloussuosituksiin",
    "ROLE_BENEFIT": "Vastaanota asiantuntemuksesi tasolle ja ammatillisiin tarpeisiisi sopivaa sisältöä",
    "CROPS_BENEFIT": "Saa viljelykohtaisia neuvoja istutuksesta, tuholaistorjunnasta ja sadonkorjuuajasta",
    "LIVESTOCK_BENEFIT": "Vastaanota kohdennettua ohjausta eläinten terveydestä, ruokinnasta ja jalostuksesta"
  },
  
  // Norwegian
  "no": {
    "PERSONALIZATION_TITLE": "Hvorfor vi ber om denne informasjonen",
    "LOCATION_BENEFIT": "Få værvarsler, lokale markedspriser og regionspesifikke landbruksråd",
    "NAME_BENEFIT": "Motta personlige hilsener og bygg tillit med vår AI-assistent",
    "LANGUAGE_BENEFIT": "Få råd på ditt foretrukne språk for bedre forståelse",
    "GENDER_BENEFIT": "Få tilgang til kjønnsspesifikke programmer og skreddersydde landbruksanbefalinger",
    "ROLE_BENEFIT": "Motta innhold som passer ditt ekspertisenivå og profesjonelle behov",
    "CROPS_BENEFIT": "Få avlingsspesifikke råd om planting, skadedyrkontroll og høstetid",
    "LIVESTOCK_BENEFIT": "Motta målrettet veiledning om dyrehelse, fôring og avl"
  },
  
  // Turkish
  "tr": {
    "PERSONALIZATION_TITLE": "Bu bilgileri neden istiyoruz",
    "LOCATION_BENEFIT": "Hava durumu uyarıları, yerel pazar fiyatları ve bölgeye özel tarım tavsiyeleri alın",
    "NAME_BENEFIT": "Kişiselleştirilmiş selamlamalar alın ve AI asistanımızla güven oluşturun",
    "LANGUAGE_BENEFIT": "Daha iyi anlayış için tercih ettiğiniz dilde tavsiye alın",
    "GENDER_BENEFIT": "Cinsiyete özel programlara ve özelleştirilmiş tarım önerilerine erişin",
    "ROLE_BENEFIT": "Uzmanlık seviyenize ve profesyonel ihtiyaçlarınıza uygun içerik alın",
    "CROPS_BENEFIT": "Ekim, zararlı kontrolü ve hasat zamanı hakkında ürüne özel tavsiyeler alın",
    "LIVESTOCK_BENEFIT": "Hayvan sağlığı, besleme ve üreme konularında hedeflenmiş rehberlik alın"
  },
  
  // Hebrew
  "he": {
    "PERSONALIZATION_TITLE": "למה אנחנו מבקשים את המידע הזה",
    "LOCATION_BENEFIT": "קבלו התראות מזג אוויר, מחירי שוק מקומיים ועצות חקלאיות ספציפיות לאזור",
    "NAME_BENEFIT": "קבלו ברכות אישיות ובנו אמון עם עוזר הבינה המלאכותית שלנו",
    "LANGUAGE_BENEFIT": "קבלו עצות בשפה המועדפת עליכם להבנה טובה יותר",
    "GENDER_BENEFIT": "קבלו גישה לתוכניות ספציפיות למגדר והמלצות חקלאיות מותאמות אישית",
    "ROLE_BENEFIT": "קבלו תוכן המתאים לרמת המומחיות והצרכים המקצועיים שלכם",
    "CROPS_BENEFIT": "קבלו עצות ספציפיות לגידולים על שתילה, הדברה וזמן קציר",
    "LIVESTOCK_BENEFIT": "קבלו הדרכה ממוקדת על בריאות בעלי חיים, האכלה ורבייה"
  },
  
  // Persian
  "fa": {
    "PERSONALIZATION_TITLE": "چرا این اطلاعات را درخواست می‌کنیم",
    "LOCATION_BENEFIT": "هشدارهای آب و هوا، قیمت‌های بازار محلی و مشاوره کشاورزی مخصوص منطقه دریافت کنید",
    "NAME_BENEFIT": "احوالپرسی‌های شخصی دریافت کنید و با دستیار هوش مصنوعی ما اعتماد بسازید",
    "LANGUAGE_BENEFIT": "برای درک بهتر، مشاوره را به زبان دلخواه خود دریافت کنید",
    "GENDER_BENEFIT": "به برنامه‌های مختص جنسیت و توصیه‌های کشاورزی سفارشی دسترسی پیدا کنید",
    "ROLE_BENEFIT": "محتوایی متناسب با سطح تخصص و نیازهای حرفه‌ای خود دریافت کنید",
    "CROPS_BENEFIT": "مشاوره‌های مختص محصول درباره کاشت، کنترل آفات و زمان برداشت دریافت کنید",
    "LIVESTOCK_BENEFIT": "راهنمایی هدفمند درباره سلامت دام، تغذیه و پرورش دریافت کنید"
  }
};

// Output the translations in a format ready to be added to StringsManager.kt
console.log("Add these translations to StringsManager.kt:");
console.log("=====================================\n");

Object.entries(personalizationTranslations).forEach(([langCode, translations]) => {
  console.log(`// ${langCode} - Personalization benefits`);
  Object.entries(translations).forEach(([key, value]) => {
    console.log(`StringKey.${key} to "${value}",`);
  });
  console.log("");
});