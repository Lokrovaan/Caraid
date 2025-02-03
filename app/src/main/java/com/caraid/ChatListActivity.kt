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

class ChatListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CaraidTheme { AppNavigation() }

        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "chat_list") {
        composable("chat_list") { ChatListScreen(navController) }
        composable("chat_screen/{chatId}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatScreen(chatId)
        }
    }
}

@Composable
fun ChatListScreen(navController: NavController) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val chats = remember { mutableStateListOf<Chat>() }

    // Fetch chat IDs for the current user
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            FirebaseFirestore.getInstance()
                .collection("chats")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        // Handle error
                        return@addSnapshotListener
                    }

                    val chatIds = snapshot?.documents?.map { it.id } ?: emptyList()
                    // Fetch chat data for each chat ID
                    chatIds.forEach { chatId ->
                        FirebaseFirestore.getInstance()
                            .collection("chats")
                            .document(chatId)
                            .get()
                            .addOnSuccessListener { document ->
                                val chat = document.toObject(Chat::class.java)
                                if (chat != null) {
                                    chats.add(chat)
                                }
                            }
                            .addOnFailureListener { exception ->
                                // Handle error
                            }
                    }
                }
        }
    }

    LazyColumn {
        items(chats) { chat ->
            ChatItem(chat) {
                // Navigate to chat screen with chatId
                navController.navigate("chat_screen/${chat.chatId}")
            }
        }
    }
}

@Composable
fun ChatItem(chat: Chat, onChatClick: () -> Unit) {
    // Display chat name, last message preview, and timestamp
    Column(modifier = Modifier.clickable { onChatClick() }) {
        Text("Chat ID: ${chat.chatId}")
        //... display other chat information...
    }
}