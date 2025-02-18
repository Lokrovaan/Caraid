package com.caraid

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatScreenActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPref: SharedPreferences
    private val messages = mutableStateListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        val chatId = intent.getStringExtra("chatId") ?: ""
        sharedPref = getSharedPreferences("open_chats", Context.MODE_PRIVATE)

        setContent {
            ChatScreen(chatId)
        }
    }

    override fun onResume() {
        super.onResume()
        // Add chatId to shared preference
        val chatId = intent.getStringExtra("chatId") ?: ""
        val editor = sharedPref.edit()
        val openChatIds =
            sharedPref.getStringSet("open_chat_ids", setOf())?.toMutableSet() ?: mutableSetOf()
        openChatIds.add(chatId)
        editor.putStringSet("open_chat_ids", openChatIds)
        editor.apply()

        // Register broadcast receiver
        val filter = IntentFilter("NEW_MESSAGE")
        registerReceiver(newMessageReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        // Remove chatId from shared preference
        val chatId = intent.getStringExtra("chatId") ?: ""
        val editor = sharedPref.edit()
        val openChatIds =
            sharedPref.getStringSet("open_chat_ids", setOf())?.toMutableSet() ?: mutableSetOf()
        openChatIds.remove(chatId)
        editor.putStringSet("open_chat_ids", openChatIds)
        editor.apply()

        // Unregister broadcast receiver
        unregisterReceiver(newMessageReceiver)
    }

    private val newMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "NEW_MESSAGE") {
                intent.getStringExtra("chatId") ?: ""
                val senderId = intent.getStringExtra("senderId") ?: ""
                val content = intent.getStringExtra("content") ?: ""
                val timestamp = Timestamp.now() // Or any other Timestamp object
                intent.putExtra("timestamp", timestamp)
                val newMessage = Message(senderId, content, timestamp)

                // Add the new message to the messages list
                messages.add(newMessage)
            }
        }
    }

    @Composable
    fun ChatScreen(chatId: String) {
        var messageText by remember { mutableStateOf("") }
        val firestore = FirebaseFirestore.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Fetch messages from Firestore
        LaunchedEffect(chatId) {
            firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        // Handle error
                        return@addSnapshotListener
                    }
                    messages.clear()
                    snapshot?.documents?.mapNotNull { it.toObject(Message::class.java) }
                        ?.let { messages.addAll(it) }
                }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Chat messages
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(messages) { message ->
                    MessageCard(message)
                }
            }

            // Message input and send button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Enter message") }
                )
                Button(
                    onClick = {
                        if (messageText.isNotBlank() && currentUserId.isNotBlank()) {
                            val newMessage = Message(
                                senderId = currentUserId,
                                content = messageText,
                                timestamp = Timestamp.now()
                            )

                            // Create a HashMap for the Message object
                            val messageData = hashMapOf(
                                "senderId" to newMessage.senderId,
                                "content" to newMessage.content,
                                "timestamp" to newMessage.timestamp
                            )

                            // Send message to Firestore
                            firestore.collection("chats")
                                .document(chatId)
                                .collection("messages")
                                .add(messageData)
                                .addOnSuccessListener {
                                    // Clear message text field
                                    messageText = ""

                                    // Update lastMessage in "chats" collection
                                    val chatUpdates = hashMapOf(
                                        "lastMessage" to messageData
                                    )
                                    firestore.collection("chats")
                                        .document(chatId)
                                        .update(chatUpdates as Map<String, Any>)
                                        .addOnFailureListener { exception ->
                                            // Handle error
                                        }
                                }
                                .addOnFailureListener { exception ->
                                    // Handle error
                                }
                        }
                    }
                ) {
                    Text("Send")
                }
            }
        }
    }

    @Composable
    fun MessageCard(message: Message) {
        // Display message content
        Column {
            Text(text = "From: ${message.senderId}")
            Text(text = message.content)
        }
    }
}