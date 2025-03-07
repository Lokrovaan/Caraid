package com.caraid

import com.google.firebase.Timestamp

data class Message(
    val sender: User = User(),
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now()
)