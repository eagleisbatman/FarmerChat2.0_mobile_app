package com.digitalgreen.farmerchat.network

import android.util.Log
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.json.JSONObject
import java.net.URI

class ChatWebSocketClient {
    private var socket: Socket? = null
    private val gson = Gson()
    private val chatEventChannel = Channel<ChatStreamEvent>(Channel.UNLIMITED)
    
    companion object {
        private const val TAG = "ChatWebSocket"
        private const val SOCKET_URL = "http://10.0.2.2:3004"
    }
    
    val chatEvents: Flow<ChatStreamEvent> = chatEventChannel.receiveAsFlow()
    
    fun connect(authToken: String) {
        try {
            val options = IO.Options().apply {
                auth = mapOf("token" to authToken)
                transports = arrayOf("websocket")
                upgrade = true
                rememberUpgrade = true
            }
            
            socket = IO.socket(URI.create(SOCKET_URL), options)
            
            socket?.apply {
                on(Socket.EVENT_CONNECT) {
                    Log.d(TAG, "WebSocket connected")
                }
                
                on(Socket.EVENT_DISCONNECT) { args ->
                    Log.d(TAG, "WebSocket disconnected: ${args.firstOrNull()}")
                }
                
                on(Socket.EVENT_CONNECT_ERROR) { args ->
                    Log.e(TAG, "WebSocket connection error: ${args.firstOrNull()}")
                }
                
                // Chat events
                on("chat:chunk") { args ->
                    handleChatEvent("chunk", args)
                }
                
                on("chat:complete") { args ->
                    handleChatEvent("complete", args)
                }
                
                on("chat:error") { args ->
                    handleChatEvent("error", args)
                }
                
                on("chat:typing") { args ->
                    handleChatEvent("typing", args)
                }
                
                on("chat:stopped") { args ->
                    handleChatEvent("stopped", args)
                }
                
                connect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting WebSocket", e)
        }
    }
    
    private fun handleChatEvent(type: String, args: Array<Any>) {
        try {
            val data = args.firstOrNull() as? JSONObject
            if (data != null) {
                val event = when (type) {
                    "chunk" -> ChatStreamEvent(
                        type = "chunk",
                        content = data.optString("content"),
                        chunkNumber = data.optInt("chunkNumber"),
                        isComplete = data.optBoolean("isComplete", false)
                    )
                    "complete" -> {
                        val followUpQuestions = data.optJSONArray("followUpQuestions")?.let { array ->
                            (0 until array.length()).map { i ->
                                val questionObj = array.getJSONObject(i)
                                FollowUpQuestion(
                                    id = questionObj.getString("id"),
                                    question = questionObj.getString("question")
                                )
                            }
                        } ?: emptyList()
                        
                        ChatStreamEvent(
                            type = "complete",
                            content = data.optString("content"),
                            isComplete = true,
                            followUpQuestions = followUpQuestions,
                            title = data.optString("title").takeIf { it.isNotEmpty() }
                        )
                    }
                    "error" -> ChatStreamEvent(
                        type = "error",
                        error = data.optString("error", "Unknown error")
                    )
                    "typing" -> ChatStreamEvent(
                        type = "typing",
                        isTyping = data.optBoolean("isTyping", false)
                    )
                    "stopped" -> ChatStreamEvent(
                        type = "stopped",
                        isComplete = true
                    )
                    else -> ChatStreamEvent(type = type)
                }
                
                chatEventChannel.trySend(event)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling chat event: $type", e)
        }
    }
    
    fun sendStreamingMessage(message: String, conversationId: String) {
        socket?.emit("chat:stream", JSONObject().apply {
            put("message", message)
            put("conversationId", conversationId)
        })
    }
    
    fun stopGeneration(conversationId: String) {
        socket?.emit("chat:stop", JSONObject().apply {
            put("conversationId", conversationId)
        })
    }
    
    fun sendTyping(conversationId: String, isTyping: Boolean) {
        socket?.emit("chat:typing", JSONObject().apply {
            put("conversationId", conversationId)
            put("isTyping", isTyping)
        })
    }
    
    fun joinConversation(conversationId: String) {
        socket?.emit("chat:join", JSONObject().apply {
            put("conversationId", conversationId)
        })
    }
    
    fun leaveConversation(conversationId: String) {
        socket?.emit("chat:leave", JSONObject().apply {
            put("conversationId", conversationId)
        })
    }
    
    fun disconnect() {
        socket?.disconnect()
        socket = null
        chatEventChannel.close()
        Log.d(TAG, "WebSocket disconnected and cleaned up")
    }
    
    fun isConnected(): Boolean = socket?.connected() == true
}