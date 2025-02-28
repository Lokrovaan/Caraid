package com.caraid

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.caraid.ui.theme.CaraidPurplePrimary
import com.caraid.ui.theme.CaraidPurpleTertiary
import com.caraid.ui.theme.CaraidTheme
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

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

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "chat_list") {
        composable("chat_list") { ChatListScreen(navController) }
        // Add composable for "chat_screen/{chatId}" here to handle navigation to ChatScreenActivity
    }
}

@Composable
fun ChatListScreen(navController: NavController) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val chats = remember { mutableStateListOf<Chat>() }
    var otherUserNames by remember { mutableStateOf(mapOf<String, String>()) }
    rememberCoroutineScope()

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            val chatDocuments = FirebaseFirestore.getInstance()
                .collection("chats")
                .whereArrayContains("participants", currentUserId)
                .get()
                .await()
                .documents

            chatDocuments.forEach { chatDocument ->
                val participants = chatDocument["participants"] as? List<String> ?: emptyList()
                val otherUserId = participants.firstOrNull { it != currentUserId } ?: ""

                // Fetch the other user's document directly
                val userDocument = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(otherUserId)
                    .get()
                    .await()

                val userNames = mapOf(
                    userDocument.id to (userDocument["username"] as? String ?: "")
                )
                otherUserNames = userNames

                // Then process the chat
                try {
                    val chatId = chatDocument.id
                    val otherUserName = userNames[otherUserId] ?: ""

                    // Fetch the last message and its sender ID
                    val lastMessageSnapshot = FirebaseFirestore.getInstance()
                        .collection("chats")
                        .document(chatId)
                        .collection("messages")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .await()

                    val lastMessage = lastMessageSnapshot.documents.firstOrNull()
                        ?.get("content") as? String ?: ""
                    val lastMessageSenderId = lastMessageSnapshot.documents.firstOrNull()
                        ?.get("senderId") as? String ?: ""
                    val lastMessageTimestamp = lastMessageSnapshot.documents.firstOrNull()
                        ?.get("timestamp") as? Timestamp ?: Timestamp.now()

                    val chat = chatDocument.toObject(Chat::class.java)?.copy(
                        chatId = chatId,
                        chatName = getChatName(participants, currentUserId),
                        otherUserName = otherUserName,
                        lastMessage = Message(
                            lastMessageSenderId,
                            lastMessage,
                            lastMessageTimestamp
                        )
                    )
                    chat?.let { chats.add(it) }
                } catch (e: Exception) {
                    Log.e("MyTag", "Error fetching chat details: ${e.message}")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CustomTopAppBar(title = { Text("Chat List") })
        },
        bottomBar = {
            BottomNavigationBar(navController)
        },
        containerColor = CaraidPurplePrimary
    ) { innerPadding ->
        // The chat list will go here, with the innerPadding modifier
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            items(chats) { chat ->
                ChatItem(chat, onChatClick = {
                    navController.navigate("chat_screen/${chat.chatId}")
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(title: @Composable () -> Unit) {
    TopAppBar(
        title = title,
        colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
            containerColor = CaraidPurpleTertiary,
            titleContentColor = Color.White
        )
    )
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentRoute = remember { mutableStateOf("chat_list") }
    androidx.compose.material3.NavigationBar(
        containerColor = CaraidPurpleTertiary,
        contentColor = Color.White
    )
    {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = "Chats") },
            label = { Text("Chats") },
            selected = currentRoute.value == "chat_list",
            onClick = {
                currentRoute.value = "chat_list"
                navController.navigate("chat_list")
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = currentRoute.value == "settings", // Replace "settings" with your actual route
            onClick = {
                currentRoute.value = "settings"
                // Navigate to your settings screen
                navController.navigate("settings") // Replace "settings" with your actual route
            }
        )
    }
}

fun getChatName(participants: List<String>, currentUserId: String): String {
    return participants.firstOrNull { it != currentUserId } ?: "Unknown Chat"
}

@Composable
fun ChatItem(chat: Chat, onChatClick: () -> Unit) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid // Get current user ID

    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable { onChatClick() }
        .padding(16.dp)
        .border(1.dp, Color.Black)
        .background(CaraidPurpleTertiary)
    ) {
        Text("Chat with: ${chat.otherUserName}")
        Text(
            text = if (chat.lastMessage?.senderId == currentUserId) {
                "You: ${chat.lastMessage?.content}"
            } else {
                "${chat.otherUserName}: ${chat.lastMessage}"
            }
        )
    }
}