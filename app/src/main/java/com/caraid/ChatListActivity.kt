package com.caraid

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.caraid.ui.theme.CaraidTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatListActivity: ComponentActivity() {
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
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(currentUserId) {
        if (currentUserId!= null) {
            coroutineScope.launch {
                try {
                    // Fetch the chat first
                    val chatDocument = FirebaseFirestore.getInstance()
                        .collection("chats")
                        .whereArrayContains("participants", currentUserId)
                        .get()
                        .await()
                        .documents
                        .firstOrNull()

                    if (chatDocument!= null) {
                        val participants = chatDocument["participants"] as? List<String>?: emptyList()
                        val otherUserId = participants.firstOrNull { it!= currentUserId }?: ""

                        // Fetch the other user's document directly
                        val userDocument = FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(otherUserId)
                            .get()
                            .await()

                        val userNames = mapOf(
                            userDocument.id to (userDocument["username"] as? String?: "")
                        )
                        otherUserNames = userNames

                        // Then process the chat
                        try {
                            val chatId = chatDocument.id
                            val otherUserName = userNames[otherUserId]?: ""

                            coroutineScope.launch {
                                val messageSnapshot = FirebaseFirestore.getInstance()
                                    .collection("chats")
                                    .document(chatId)
                                    .collection("messages")
                                    .orderBy("timestamp", Query.Direction.DESCENDING)
                                    .limit(1)
                                    .get()
                                    .await()

                                val lastMessage = messageSnapshot.documents.firstOrNull()
                                    ?.get("content") as? String?: ""

                                val chat = chatDocument.toObject(Chat::class.java)?.copy(
                                    chatId = chatId,
                                    chatName = getChatName(participants, currentUserId),
                                    otherUserName = otherUserName,
                                    lastMessage = lastMessage
                                )
                                chat?.let { chats.add(it) }
                            }
                        } catch (e: Exception) {
                            Log.e("MyTag", "Error fetching chat details: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MyTag", "Error fetching chat or user: ${e.message}")
                }
            }
        }
    }

    LazyColumn {
        items(chats) { chat ->
            ChatItem(chat, onChatClick = {
                navController.navigate("chat_screen/${chat.chatId}")
            })
        }
    }
}

fun getChatName(participants: List<String>, currentUserId: String): String {
    return participants.firstOrNull { it!= currentUserId }?: "Unknown Chat"
}

@Composable
fun ChatItem(chat: Chat, onChatClick: () -> Unit) {
    Column(modifier = Modifier
        .clickable { onChatClick() }
        .padding(16.dp)) { // Added padding for better visual separation
        Text("Chat with: ${chat.otherUserName}")
        Text("Last message: ${chat.lastMessage}")
    }
}