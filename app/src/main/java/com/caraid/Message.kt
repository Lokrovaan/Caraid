package com.caraid

data class Message(
    val senderId: String,
    val content: String,
    val timestamp: com.google.firebase.Timestamp
)