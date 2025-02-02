package com.caraid

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.caraid.ui.theme.CaraidTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase

class ChatScreenActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        setContent {
            CaraidTheme {
                ChatScreen()
            }
        }
    }
}

@Composable
fun ChatScreen() {
    var currentMessage by remember { mutableStateOf("") }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val messages = remember { mutableStateListOf<Message>() }
    val functions = FirebaseFunctions.getInstance()
    val sendMessageCallable = functions.getHttpsCallable("sendMessage")

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
                value = currentMessage,
                onValueChange = { currentMessage = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Enter message") }
            )
            Button(
                onClick = {
                    val message = Message(
                        senderId = currentUserId!!,
                        content = currentMessage,
                        timestamp = System.currentTimeMillis()
                    )

                    val data = hashMapOf(
                        "recipientToken" to "recipient_fcm_token", // Replace with actual recipient token
                        "senderId" to message.senderId,
                        "messageText" to message.content
                    )

                    sendMessageCallable.call(data)
                        .addOnSuccessListener {
                            // Handle success
                            Log.d("ChatScreen", "Message sent successfully")
                        }
                        .addOnFailureListener { exception ->
                            // Handle error
                            Log.e("ChatScreen", "Error sending message: ${exception.message}")
                        }

                    currentMessage = "" // Clear the input field
                },
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
    //TODO
}

//TODO The recipientToken is a placeholder; you'll need to replace it with the actual recipient's FCM token.
//TODO The MessageCard composable is still a placeholder; you'll need to implement it to display the message content.