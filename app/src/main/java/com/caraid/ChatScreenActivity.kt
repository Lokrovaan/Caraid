package com.caraid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatScreenActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        val chatId = intent.getStringExtra("chatId") ?: ""

        setContent {
            ChatScreen(chatId)
        }
    }
}

@Composable
fun ChatScreen(chatId: String) {
    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Message>() }
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
                MessageCard(message, currentUserId)
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
                            timestamp = System.currentTimeMillis()
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
                                    "lastMessage" to messageData // Store the Message object as a HashMap
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
fun MessageCard(message: Message, currentUserId: String) {
    // Display message content
    Column {
        Text(text = "From: ${message.senderId}")
        Text(text = message.content)
    }
}