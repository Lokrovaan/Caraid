/*
Callum Smith - S2145086
 */

package com.caraid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.caraid.ui.theme.CaraidPurpleTertiary
import com.caraid.ui.theme.CaraidTheme
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val chatId = intent.getStringExtra("chatId") ?: ""
        val otherUserName = intent.getStringExtra("otherUserName") ?: ""
        setContent {
            CaraidTheme {
                ChatScreen(chatId, otherUserName)
            }
        }
    }
}

@Composable
fun ChatScreen(chatId: String, otherUserName: String) {
    var newMessage by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Message>() }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val scope = rememberCoroutineScope() // Create a CoroutineScope

    // Fetch messages and listen for updates
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
        // Display messages in a LazyColumn
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(messages) { message ->
                MessageItem(message, currentUserId, otherUserName, scope) // Pass the scope
            }
        }

        // Input field and send button
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
                    // Send new message
                    if (newMessage.isNotBlank()) {
                        val message = Message(
                            sender = User(userId = currentUserId),
                            content = newMessage,
                            timestamp = Timestamp.now()
                        )
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

// Display a single message item
@Composable
fun MessageItem(
    message: Message,
    currentUserId: String,
    otherUserName: String,
    scope: CoroutineScope
) {
    var showAnalysisDialog by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf("") } // Changed to String

    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = if (currentUserId == message.sender.userId) {
                "You: ${message.content}"
            } else {
                "$otherUserName: ${message.content}"
            },
            color = Color.White,
            modifier = Modifier.clickable {
                showAnalysisDialog = true
            }
        )

        // Show analysis result if available
        if (analysisResult.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .background(CaraidPurpleTertiary)
                    .padding(4.dp)
            ) {
                Column {
                    Text(text = "Analysis:", color = Color.White)
                    Text(text = analysisResult, color = Color.White)
                }
            }
        }
    }

    // Show confirmation dialog
    if (showAnalysisDialog) {
        AlertDialog(
            onDismissRequest = { showAnalysisDialog = false },
            title = { Text("Analyse Message?") },
            text = { Text("Do you want to analyse this message?") },
            confirmButton = {
                Button(onClick = {
                    showAnalysisDialog = false
                    // Perform NLP analysis in a coroutine
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            NLPAnalyser.analyseMessage(message.content)
                        }
                        analysisResult = result
                    }
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showAnalysisDialog = false
                }) {
                    Text("No")
                }
            }
        )
    }
}