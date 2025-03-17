/*
Callum Smith - S2145086
 */

package com.caraid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.caraid.ui.theme.CaraidTheme
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

    // Create and remember LazyListState
    val listState = rememberLazyListState()

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
                    // Scroll to the bottom after new messages are added
                }
        } catch (_: Exception) {
        }
    }

    // Scroll to the bottom whenever messages list changes
    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            // Launch a coroutine to call scrollToItem
            launch {
                listState.scrollToItem(messages.lastIndex)
            }
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
            contentPadding = PaddingValues(16.dp),
            state = listState, // Attach LazyListState
            reverseLayout = true // Reverse the layout
        ) {
            items(messages.reversed()) { message -> // Display items in reversed order
                MessageItem(message, currentUserId, otherUserName)
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
) {
    var sentimentResult by remember { mutableStateOf<Map<String, Any>?>(null) }
    var entitiesResult by remember { mutableStateOf<Map<String, Any>?>(null) }
    val coroutineScope = rememberCoroutineScope()
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
                // Perform NLP analysis directly when clicked
                coroutineScope.launch {
                    try {
                        // Sentiment analysis
                        val sentiment = withContext(Dispatchers.IO) {
                            NLPAnalyser.analyseMessage(message.content)
                        }
                        sentimentResult = sentiment
                        // Show results in Toast
                        // val sentimentScore = sentiment["sentiment"]
                        // val toastMessage = "Sentiment: $sentimentScore"
                        // Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show()

                    } catch (e: Exception) {
                        // Handle exceptions here
                        e.printStackTrace()
                        sentimentResult = mapOf("error" to "Analysis failed: ${e.message}")
                        withContext(Dispatchers.Main) {
                            // Toast.makeText(context, "Sentiment Analysis failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }

                    try {
                        // Entity analysis
                        val entities = withContext(Dispatchers.IO) {
                            NLPAnalyser.analyseEntities(message.content)
                        }
                        entitiesResult = entities

                    } catch (e: Exception) {
                        // Handle exceptions here
                        e.printStackTrace()
                        entitiesResult = mapOf("error" to "Analysis failed: ${e.message}")
                        withContext(Dispatchers.Main) {
                            // Toast.makeText(context, "Entities Analysis failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        )

        // Sentiment analysis result display
        if (sentimentResult != null) {
            val result = sentimentResult!!
            Column(
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.2f))
                    .padding(8.dp)
            ) {
                Text(text = "Sentiment Analysis Results:", color = Color.White)
                if (result.containsKey("error")) {
                    Text(text = "Error: ${result["error"]}", color = Color.White)
                } else {
                    Text(text = "Sentiment: ${result["sentiment"]}", color = Color.White)
                }
            }
        }

        // Entity analysis result display
        if (entitiesResult != null) {
            val result = entitiesResult!!
            Column(
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.2f))
                    .padding(8.dp)
            ) {
                Text(text = "Entity Analysis Results:", color = Color.White)
                if (result.containsKey("error")) {
                    Text(text = "Error: ${result["error"]}", color = Color.White)
                } else {
                    val entities = result["entities"]
                    if (entities is List<*>) {
                        Text(text = "Entities: ${entities.joinToString(", ")}", color = Color.White)
                    } else {
                        Text(text = "Entities: ${result["entities"]}", color = Color.White)
                    }
                }
            }
        }
    }
}