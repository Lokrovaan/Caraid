package com.caraid

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.caraid.ui.theme.CaraidTheme
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val chatId = intent.getStringExtra("chatId") ?: ""
        val navController = NavController(this)
        setContent {
            CaraidTheme {
                ChatScreen(chatId, navController)
            }
        }
    }
}

/*navController isn't currently used but keeping it for
future possible implementation.*/
@Composable
fun ChatScreen(chatId: String, navController: NavController) {
    var newMessage by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Message>() }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(chatId) {
        try {
            FirebaseFirestore.getInstance()
                .collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        return@addSnapshotListener
                    }
                    messages.clear()
                    snapshot?.toObjects(Message::class.java)?.let { messages.addAll(it) }
                }
        } catch (_: Exception) {
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(messages) { message ->
                MessageItem(message, currentUserId)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            TextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Enter message") }
            )
            Button(
                onClick = {
                    if (newMessage.isNotBlank()) {
                        val message = Message(currentUserId, newMessage, Timestamp.now())
                        FirebaseFirestore.getInstance()
                            .collection("chats")
                            .document(chatId)
                            .collection("messages")
                            .add(message)
                        newMessage = ""
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Send")
            }
        }
    }
}

@Composable
fun MessageItem(message: Message, currentUserId: String) {
    val isCurrentUser = message.senderId == currentUserId
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = if (isCurrentUser) "You: ${message.content}" else "${message.senderId}: ${message.content}",
            color = Color.White
        )
    }
}