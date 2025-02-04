package com.caraid

data class Chat(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    val chatName: String = "",
)