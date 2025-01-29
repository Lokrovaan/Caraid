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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatScreen()
        }
    }
}

@Composable
fun ChatScreen() {
    var currentMessageText by remember { mutableStateOf("") }
    val currentUserId = Firebase.auth.currentUser?.uid
    val messages = remember { mutableStateListOf<Message>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Display the list of messages
        LazyColumn(
            reverseLayout = true, // Reverse the order to display latest messages at the bottom
            modifier = Modifier.weight(1f) // Occupy available space
        ) {
            items(messages) { message ->
                MessageCard(message)
            }
        }

        // Input field and send button
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = currentMessageText,
                onValueChange = { currentMessageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Enter message") }
            )
            Button(
                onClick = {
                    // 1. Create a new message object
                    val newMessage = Message(
                        senderId = currentUserId ?: "",
                        content = currentMessageText,
                        timestamp = System.currentTimeMillis()
                    )
                     sendMessage(newMessage) },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Send")
            }
        }
    }
}

@Composable
fun MessageCard(message: Message) {
    // Display the message content (text, sender, timestamp, etc.)
    //...
}

fun sendMessage(message: Message) {
    // 2. Send the message using FCM

}