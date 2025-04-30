/*
Callum Smith - S2145086
 */

package com.caraid

import com.google.firebase.Timestamp

//This data class represents a message in the application.
data class Message(
    val sender: User = User(),
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now()
)