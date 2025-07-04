package com.digitalgreen.farmerchat.network

import retrofit2.Response
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

interface AuthApiService {
    @POST("auth/device")
    suspend fun authenticateDevice(@Body request: DeviceAuthRequest): Response<ApiResponse<AuthResponse>>
    
    @POST("auth/login")
    suspend fun loginWithPhone(@Body request: Map<String, String>): Response<ApiResponse<AuthResponse>>
    
    @POST("auth/register")
    suspend fun registerWithPhone(@Body request: Map<String, String>): Response<ApiResponse<AuthResponse>>
    
    @POST("auth/verify")
    suspend fun verifyFirebaseToken(@Body request: AuthRequest): Response<ApiResponse<AuthResponse>>
    
    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: Map<String, String>): Response<ApiResponse<AuthResponse>>
    
    @GET("auth/config")
    suspend fun getAuthConfig(): Response<ApiResponse<Map<String, Any>>>
    
    @POST("auth/phone/request-otp")
    suspend fun requestPhoneOTP(@Body request: PhoneOTPRequest): Response<ApiResponse<OTPResponse>>
    
    @POST("auth/phone/verify-otp")
    suspend fun verifyPhoneOTP(@Body request: VerifyOTPRequest): Response<ApiResponse<VerifyOTPResponse>>
}

interface UserApiService {
    @GET("users/profile")
    suspend fun getUserProfile(): Response<ApiResponse<ApiUser>>
    
    @PUT("users/profile")
    suspend fun updateUserProfile(@Body request: UpdateUserRequest): Response<ApiResponse<ApiUser>>
    
    @DELETE("users/profile")
    suspend fun deleteUserProfile(): Response<ApiResponse<Unit>>
}

interface ConversationApiService {
    @GET("conversations")
    suspend fun getConversations(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("search") search: String? = null
    ): Response<ApiResponse<ConversationsResponse>>
    
    @POST("conversations")
    suspend fun createConversation(@Body request: CreateConversationRequest): Response<ApiResponse<ApiConversation>>
    
    @GET("conversations/{id}")
    suspend fun getConversation(@Path("id") conversationId: String): Response<ApiResponse<ApiConversation>>
    
    @PUT("conversations/{id}")
    suspend fun updateConversation(
        @Path("id") conversationId: String,
        @Body request: Map<String, Any>
    ): Response<ApiResponse<ApiConversation>>
    
    @DELETE("conversations/{id}")
    suspend fun deleteConversation(@Path("id") conversationId: String): Response<ApiResponse<Unit>>
}

interface ChatApiService {
    @POST("chat/send")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<ApiResponse<SendMessageResponse>>
    
    @GET("chat/{conversationId}/messages")
    suspend fun getMessages(
        @Path("conversationId") conversationId: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<ApiResponse<List<ApiMessage>>>
    
    @POST("chat/starter-questions")
    suspend fun getStarterQuestions(@Body request: StarterQuestionsRequest): Response<ApiResponse<List<FollowUpQuestion>>>
    
    @POST("chat/messages/{messageId}/rate")
    suspend fun rateMessage(
        @Path("messageId") messageId: String,
        @Body request: RateMessageRequest
    ): Response<ApiResponse<Unit>>
    
    @POST("chat/generate-followup")
    suspend fun generateFollowUpQuestions(@Body request: GenerateFollowUpRequest): Response<ApiResponse<List<FollowUpQuestion>>>
    
    @Multipart
    @POST("chat/transcribe")
    suspend fun transcribeAudio(
        @Part audio: MultipartBody.Part,
        @Part("language") language: RequestBody
    ): Response<ApiResponse<AudioTranscriptionResponse>>
}

data class AudioTranscriptionResponse(
    @SerializedName("transcription") val transcription: String,
    @SerializedName("language") val language: String
)

interface TranslationApiService {
    @GET("translations/{languageCode}")
    suspend fun getTranslations(@Path("languageCode") languageCode: String): Response<ApiResponse<TranslationResponse>>
    
    @GET("translations/languages")
    suspend fun getSupportedLanguages(): Response<ApiResponse<List<Language>>>
    
    @GET("translations/{languageCode}/crops")
    suspend fun getCropTranslations(
        @Path("languageCode") languageCode: String,
        @Query("search") search: String? = null
    ): Response<ApiResponse<Map<String, CropTranslation>>>
    
    @GET("translations/{languageCode}/livestock")
    suspend fun getLivestockTranslations(
        @Path("languageCode") languageCode: String,
        @Query("search") search: String? = null
    ): Response<ApiResponse<Map<String, LivestockTranslation>>>
}