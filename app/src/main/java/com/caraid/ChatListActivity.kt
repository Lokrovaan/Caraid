package com.caraid

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.caraid.ui.theme.CaraidTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatListActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CaraidTheme {
                Log.d("MyTag", "ChatListActivity onCreate called")
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "chat_list") {
        composable("chat_list") { ChatListScreen(navController) } // Pass navController here
    }
}

@Composable
fun ChatListScreen(navController: NavController) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val chats = remember { mutableStateListOf<Chat>() }
    val context = LocalContext.current // Get the context here

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

                    chats.clear() // Clear the list before adding new data
                    snapshot?.documents?.mapNotNull { document ->
                        (document["lastMessageTimestamp"] as? com.google.firebase.Timestamp)?.toDate()?.time?: 0 // Convert Timestamp to Long
                        document.toObject(Chat::class.java)?.copy(
                            chatId = document.id,
                            chatName = getChatName(document["participants"] as? List<String>?: emptyList(), currentUserId),
                        )
                    }?.let { chats.addAll(it) }
                }
        }
    }

    LazyColumn {
        items(chats) { chat ->
            ChatItem(chat) {
                // Navigate to ChatScreenActivity using an Intent
                val intent = Intent(context, ChatScreenActivity::class.java)
                intent.putExtra("chatId", chat.chatId)
                context.startActivity(intent)
            }
        }
    }
}

// Helper function to determine the chat name
fun getChatName(participants: List<String>, currentUserId: String): String {
    // If there are only two participants, return the other participant's ID
    // Otherwise, return a generic name like "Group Chat"
    return if (participants.size == 2) {
        participants.firstOrNull { it!= currentUserId }?: "Unknown Chat"
    } else {
        "Group Chat"
    }
}

@Composable
fun ChatItem(chat: Chat, onChatClick: () -> Unit) {
    Column(modifier = Modifier.clickable { onChatClick() }) {
        Text("Chat Name: ${chat.chatName}")
    }
}