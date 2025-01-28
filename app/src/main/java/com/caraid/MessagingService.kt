package com.caraid

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.caraid.ui.theme.*

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle the received message
        //...
    }

    override fun onNewToken(token: String) {
        // Handle the new token
        //...
    }
}