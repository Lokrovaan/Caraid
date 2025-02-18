package com.caraid

data class Chat(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    val chatName: String = "",
    val otherUserName: String = "",
    val timestamp: com.google.firebase.Timestamp? = null,
    val lastMessage: Message? = null
)