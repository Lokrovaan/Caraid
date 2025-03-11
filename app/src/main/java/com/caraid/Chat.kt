/*
Callum Smith - S2145086
 */

package com.caraid

data class Chat(
    val chatId: String = "",
    val currentUser: User? = null,
    val otherUser: User? = null,
    val lastMessage: Message? = null
)