package com.caraid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.caraid.ui.theme.CaraidTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

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

    LaunchedEffect(currentUserId) {
        if (currentUserId!= null) {
            FirebaseFirestore.getInstance()
                .collection("chats")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener { snapshot, error ->
                    if (error!= null) {
                        // Handle error
                        return@addSnapshotListener
                    }

                    chats.clear()
                    snapshot?.documents?.forEach { document ->
                        val chatId = document.id
                        val participants = document["participants"] as? List<String>?: emptyList()
                        val otherUserId = participants.firstOrNull { it!= currentUserId }?: ""

                        FirebaseFirestore.getInstance().collection("users").document(otherUserId)
                            .get()
                            .addOnSuccessListener { userDocument ->
                                val otherUserName = userDocument["username"] as? String?: ""

                                FirebaseFirestore.getInstance()
                                    .collection("chats")
                                    .document(chatId)
                                    .collection("messages")
                                    .orderBy("timestamp", Query.Direction.DESCENDING)
                                    .limit(1)
                                    .get()
                                    .addOnSuccessListener { messageSnapshot ->
                                        val lastMessage = messageSnapshot.documents.firstOrNull()
                                            ?.get("content") as? String?: ""

                                        val chat = document.toObject(Chat::class.java)?.copy(
                                            chatId = chatId,
                                            chatName = getChatName(participants, currentUserId),
                                            otherUserName = otherUserName,
                                            lastMessage = lastMessage
                                        )
                                        chat?.let { chats.add(it) }
                                    }
                            }
                    }
                }
        }
    }

    LazyColumn {
        items(chats) { chat ->
            ChatItem(chat) {
                navController.navigate("chat_screen/${chat.chatId}")
            }
        }
    }
}

fun getChatName(participants: List<String>, currentUserId: String): String {
    return if (participants.size == 2) {
        participants.firstOrNull { it!= currentUserId }?: "Unknown Chat"
    } else {
        "Group Chat"
    }
}

@Composable
fun ChatItem(chat: Chat, onChatClick: () -> Unit) {
    Column(modifier = Modifier.clickable { onChatClick() }) {
        Text("Chat with: ${chat.otherUserName}")
        Text("Last message: ${chat.lastMessage}")
    }
}