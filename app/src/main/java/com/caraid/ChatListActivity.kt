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
import kotlinx.coroutines.tasks.await

// Activity that displays the list of chats for the current user.
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

// Composable function that displays the list of chats.
@Composable
fun ChatListScreen(navController: NavHostController) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val chats = remember { mutableStateListOf<Chat>() }
    var otherUserNames by remember { mutableStateOf(mapOf<String, String>()) }
    rememberCoroutineScope()

    // LaunchedEffect to fetch chats when the current user ID changes.
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            fetchChats(currentUserId, chats, otherUserNames) { newOtherUsernames ->
                otherUserNames = newOtherUsernames
            }
        }
    }

    // LazyColumn to display the list of chats efficiently.
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
    ) {
        items(chats) { chat ->
            ChatItem(chat, onChatClick = {
                navController.navigate("chat_screen/${chat.chatId}")
            })
        }
    }
}

// Function to fetch the list of chats for the current user.
suspend fun fetchChats(
    currentUserId: String,
    chats: MutableList<Chat>,
    otherUserNames: Map<String, String>,
    onOtherUserNamesUpdated: (Map<String, String>) -> Unit
) {
    // Fetch chat documents from Firestore where the current user is a participant.
    val chatDocuments = FirebaseFirestore.getInstance()
        .collection("chats")
        .whereArrayContains("participants", currentUserId)
        .get()
        .await()
        .documents

    val newOtherUserNames = mutableMapOf<String, String>()

    // Iterate through the chat documents and fetch chat details.
    chatDocuments.forEach { chatDocument ->
        val participants = chatDocument["participants"] as? List<String> ?: emptyList()
        val otherUserId = participants.firstOrNull { it != currentUserId } ?: ""

        // Fetch the other user's name.
        val otherUserName = fetchOtherUserName(otherUserId)
        newOtherUserNames[otherUserId] = otherUserName

        try {
            val chatId = chatDocument.id
            val lastMessage = fetchLastMessage(chatId)

            // Create a Chat object with the fetched details.
            val chat = chatDocument.toObject(Chat::class.java)?.copy(
                chatId = chatId,
                chatName = getChatName(participants, currentUserId),
                otherUserName = otherUserName,
                lastMessage = lastMessage
            )
            chat?.let { chats.add(it) }
        } catch (e: Exception) {
            Log.e("ChatListActivity", "Error fetching chat details: ${e.message}")
        }
    }

    // Update the otherUserNames map with the fetched names.
    onOtherUserNamesUpdated(newOtherUserNames)
}

// Function to fetch the username of the other user in a chat.
suspend fun fetchOtherUserName(otherUserId: String): String {
    return try {
        val userDocument = FirebaseFirestore.getInstance()
            .collection("users")
            .document(otherUserId)
            .get()
            .await()
        userDocument["username"] as? String ?: ""
    } catch (e: Exception) {
        Log.e("ChatListActivity", "Error fetching other user name: ${e.message}")
        ""
    }
}

// Function to fetch the last message in a chat.
suspend fun fetchLastMessage(chatId: String): Message? {
    return try {
        val lastMessageSnapshot = FirebaseFirestore.getInstance()
            .collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()

        val lastMessageDocument = lastMessageSnapshot.documents.firstOrNull()
        lastMessageDocument?.let {
            Message(
                it["senderId"] as? String ?: "",
                it["content"] as? String ?: "",
                it["timestamp"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now()
            )
        }
    } catch (e: Exception) {
        Log.e("ChatListActivity", "Error fetching last message: ${e.message}")
        null
    }
}

// Function to get the name of a chat based on its participants and the current user ID.
fun getChatName(participants: List<String>, currentUserId: String): String {
    return participants.firstOrNull { it != currentUserId } ?: "Unknown Chat"
}

// Composable function that displays a single chat item.
@Composable
fun ChatItem(chat: Chat, onChatClick: () -> Unit) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChatClick() }
            .padding(10.dp)
            .border(2.dp, Color.Black)
            .background(CaraidPurpleTertiary)
            .padding(10.dp)
    ) {
        Text("Chat with: ${chat.otherUserName}")
        Text(
            text = if (chat.lastMessage?.senderId == currentUserId) {
                "You: ${chat.lastMessage?.content}"
            } else {
                "${chat.otherUserName}: ${chat.lastMessage?.content}"
            }
        )
    }
}