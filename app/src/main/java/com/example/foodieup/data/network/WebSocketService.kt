package com.example.foodieup.data.network

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject

object WebSocketService {

    private const val TAG = "WebSocketService"
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    private val _orderStatusUpdates = MutableSharedFlow<String>()
    val orderStatusUpdates = _orderStatusUpdates.asSharedFlow()

    fun startTracking(orderId: Int) {
        stopTracking()
        val request = Request.Builder()
            .url("ws://100.66.233.102:8000/ws/order/$orderId/")
            .build()
        webSocket = client.newWebSocket(request, OrderWebSocketListener())
        Log.d(TAG, "Начинаю отслеживать заказ: $orderId")
    }

    fun stopTracking() {
        if (webSocket != null) {
            Log.d(TAG, "Останавливаю отслеживание")
            webSocket?.close(1000, "Tracking stopped by client")
            webSocket = null
        }
    }

    private class OrderWebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket соединение открыто")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "Получено сообщение: $text")
            try {
                val jsonObject = JSONObject(text)
                val message = jsonObject.getString("message")
                _orderStatusUpdates.tryEmit(message)
                stopTracking()
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка парсинга JSON: $text", e)
                _orderStatusUpdates.tryEmit(text)
            }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d(TAG, "Получены байты: ${bytes.hex()}")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket закрывается: $code / $reason")
            webSocket.close(1000, null)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "Ошибка WebSocket: ${t.message}", t)
        }
    }
}
