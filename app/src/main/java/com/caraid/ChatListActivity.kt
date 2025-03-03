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
fun ChatListScreen(navController: NavHostController) {
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

                val userDocument = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(otherUserId)
                    .get()
                    .await()

                val userNames = mapOf(
                    userDocument.id to (userDocument["username"] as? String ?: "")
                )
                otherUserNames = userNames

                try {
                    val chatId = chatDocument.id
                    val otherUserName = userNames[otherUserId] ?: ""

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
                    Log.e("ChatListActivity", "Error fetching chat details: ${e.message}")
                }
            }
        }
    }

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

fun getChatName(participants: List<String>, currentUserId: String): String {
    return participants.firstOrNull { it != currentUserId } ?: "Unknown Chat"
}

@Composable
fun ChatItem(chat: Chat, onChatClick: () -> Unit) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChatClick() }
            .padding(10.dp) //outer padding for the Item as a whole.
            .padding(10.dp) //inner padding for the text inside.
            .border(2.dp, Color.Black)
            .background(CaraidPurpleTertiary)
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