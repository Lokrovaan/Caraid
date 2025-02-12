package com.caraid

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            val chatId = remoteMessage.data["chatId"]
            val senderId = remoteMessage.data["senderId"]
            val content = remoteMessage.data["content"]
            val timestamp = remoteMessage.data["timestamp"]?.toLongOrNull()

            if (chatId != null && senderId != null && content != null && timestamp != null) {
                val message = Message(senderId, content, timestamp)
                handleReceivedMessage(chatId, message)
            } else {
                handleNow()
            }
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            // sendNotification(it.body!!)  // This is not needed for data messages
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    private fun sendRegistrationToServer(token: String?) {
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    private fun handleReceivedMessage(chatId: String, message: Message) {
        // Check if the chat screen for the given chatId is open
        val isChatScreenOpen = isChatScreenOpen(chatId)

        if (isChatScreenOpen) {
            // If the chat screen is open, send a broadcast to notify the activity
            // to add the new message to the UI
            sendMessageBroadcast(chatId, message)
        } else {
            // If the chat screen is not open, show a notification
            sendNotificationForMessage(chatId, message)
        }
    }

    // Helper function to check if the chat screen is open
    private fun isChatScreenOpen(chatId: String): Boolean {
        // Implement logic to check if the chat screen for the given chatId is open
        // You can use a shared preference or a global variable to track open chat screens
        // For example, you can store a list of open chat IDs in a shared preference
        // and check if the given chatId is present in the list
        val sharedPref = getSharedPreferences("open_chats", Context.MODE_PRIVATE)
        val openChatIds = sharedPref.getStringSet("open_chat_ids", setOf()) ?: setOf()
        return openChatIds.contains(chatId)
    }

    // Helper function to send a broadcast
    private fun sendMessageBroadcast(chatId: String, message: Message) {
        val intent = Intent("NEW_MESSAGE")
        intent.putExtra("chatId", chatId)
        intent.putExtra("senderId", message.senderId)
        intent.putExtra("content", message.content)
        intent.putExtra("timestamp", message.timestamp)
        sendBroadcast(intent)
    }

    // Helper function to send a notification for a new message
    private fun sendNotificationForMessage(chatId: String, message: Message) {
        val intent = Intent(this, ChatScreenActivity::class.java).apply {
            putExtra("chatId", chatId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.white_purple_v)
            .setContentTitle("New message from ${message.senderId}")
            .setContentText(message.content)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "New Messages",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        notificationManager.notify(chatId.hashCode(), notificationBuilder.build())
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}