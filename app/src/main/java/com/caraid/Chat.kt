package com.caraid

data class Chat(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    val chatName: String = "",
    val otherUserName: String = "", // Add this field
    val lastMessage: String = ""    // Add this field
)