package com.caraid

import com.google.firebase.Timestamp

data class Chat(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    val chatName: String = "",
    val otherUserName: String = "",
    val lastMessage: String = "",
    val timestamp: com.google.firebase.Timestamp? = null
)