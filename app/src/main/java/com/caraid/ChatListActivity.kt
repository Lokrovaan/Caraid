package com.caraid

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
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
import androidx.navigation.NavHostController
import com.caraid.ui.theme.CaraidPurpleTertiary
import com.caraid.ui.theme.CaraidTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Activity that displays the list of chats for the current user
class ChatListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CaraidTheme {
                AppNavigation()
            }
        }
    }
}

// Composable function that displays the list of chats
@Composable
fun ChatListScreen(navController: NavHostController) {
    val chats = remember { mutableStateListOf<Chat>() } // State to hold the list of chats
    val coroutineScope = rememberCoroutineScope() // Coroutine scope for asynchronous operations
    var currentUserId by remember { mutableStateOf("") } // State to hold the current user's ID

    // LaunchedEffect to fetch chats when the screen is displayed
    LaunchedEffect(Unit) {
        // Get Firebase auth instance
        val auth = FirebaseAuth.getInstance()

        // Check if user is logged in
        auth.currentUser?.let { user ->

            // Get current user's ID
            currentUserId = user.uid

            coroutineScope.launch {
                // Fetch chats for the current user
                fetchChats(user.uid, chats)
            }
        }
    }

    // LazyColumn to display the list of chats efficiently
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
    ) {
        items(chats) { chat -> // Iterate through the list of chats
            ChatItem(chat, onChatClick = {
                // Navigate to the chat screen when a chat item is clicked
                navController.navigate("chat_screen/${chat.chatId}/${chat.otherUser?.userName}")
            })
        }
    }
}

// Function to fetch the list of chats for the current user
suspend fun fetchChats(
    currentUserId: String,
    chats: MutableList<Chat>
) {
    // Fetch chat documents from Firestore where the current user is a participant
    val chatDocuments = FirebaseFirestore.getInstance()
        .collection("chats")
        .whereArrayContains("participants", currentUserId)
        .get()
        .await()
        .documents

    // Iterate through the chat documents and fetch chat details
    chatDocuments.forEach { chatDocument ->
        try {
            // Get the chat ID
            val chatId = chatDocument.id

            // Get the participants
            val participants = chatDocument["participants"] as? List<String> ?: emptyList()

            // Get the other user's ID
            val otherUserId = participants.firstOrNull { it != currentUserId } ?: ""

            // Fetch the current user's details
            val currentUser = fetchUser(currentUserId)

            // Fetch the other user's details
            val otherUser = fetchUser(otherUserId)

            // Fetch the last message in the chat
            val lastMessage = fetchLastMessage(chatId)

            // Create a Chat object with the fetched details
            val chat = Chat(
                chatId = chatId,
                currentUser = currentUser,
                otherUser = otherUser,
                lastMessage = lastMessage
            )

            // Add the chat to the list
            chats.add(chat)
        } catch (e: Exception) {
            // Log any errors
            Log.e("ChatListActivity", "Error fetching chat details: ${e.message}")
        }
    }
}

// Function to fetch the last message in a chat
suspend fun fetchLastMessage(chatId: String): Message? {
    return try {
        // Fetch the last message from Firestore
        val lastMessageSnapshot = FirebaseFirestore.getInstance()
            .collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy(
                "timestamp",
                Query.Direction.DESCENDING
            ) // Order by timestamp in descending order
            .limit(1) // Limit to the last message
            .get()
            .await()

        // Get the last message document
        val lastMessageDocument = lastMessageSnapshot.documents.firstOrNull()
        lastMessageDocument?.let {
            // Get the sender ID
            val senderId = it["senderId"] as? String ?: ""
            // Create a Message object with the fetched details
            Message(
                sender = User(
                    userId = senderId,
                    userName = fetchUserName(senderId)
                ),
                content = it["content"] as? String ?: "",
                timestamp = it["timestamp"] as? com.google.firebase.Timestamp
                    ?: com.google.firebase.Timestamp.now()
            )
        }
    } catch (e: Exception) {
        // Log any errors
        Log.e("ChatListActivity", "Error fetching last message: ${e.message}")
        null
    }
}

// Function to fetch user details by user ID
suspend fun fetchUser(userId: String): User? {
    return try {
        // Fetch user document from Firestore
        val userDocument = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .await()

        // Create a User object with the fetched details
        User(
            userId = userId,
            // Get the username
            userName = userDocument["username"] as? String ?: ""
        )
    } catch (e: Exception) {
        // Log any errors
        Log.e("ChatListActivity", "Error fetching user: ${e.message}")
        null
    }
}

// Function to fetch username by user ID
private suspend fun fetchUserName(userId: String): String {
    return try {
        // Fetch user document from Firestore
        val userDocument = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .await()

        // Get the username
        userDocument["username"] as? String ?: ""
    } catch (e: Exception) {
        // Log any errors
        Log.e("ChatListActivity", "Error fetching user name: ${e.message}")
        ""
    }
}

// Composable function that displays a single chat item
@Composable
fun ChatItem(chat: Chat, onChatClick: () -> Unit) {
    // Get the current user's ID
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    // Column to display chat item details
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChatClick() }
            .padding(10.dp)
            .border(2.dp, Color.Black)
            .background(CaraidPurpleTertiary)
            .padding(10.dp)
    ) {
        // Display the other user's name
        Text("Chat with: ${chat.otherUser?.userName ?: "Unknown"}")

        // Display the last message content
        Text(
            text = if (chat.lastMessage?.sender?.userId == currentUserId) {
                // If the sender is the current user, display "You"
                "You: ${chat.lastMessage?.content}"
            } else {
                // Otherwise, display the other user's name
                "${chat.otherUser?.userName ?: "Unknown"}: ${chat.lastMessage?.content}"
            }
        )
    }
}