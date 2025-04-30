/*
Callum Smith - S2145086
 */

package com.caraid

//This data class represents a chat between users in the application.
data class Chat(
    val chatId: String = "",
    val currentUser: User? = null,
    val otherUser: User? = null,
    val lastMessage: Message? = null
)