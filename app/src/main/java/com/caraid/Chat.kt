package com.caraid

data class Chat(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    val messages: List<Message> = emptyList(),
    val lastMessage: Message? = null,
    val lastMessageTimestamp: Long = 0
)