const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendMessage = functions.https.onCall(async (data, context) => {
  const { recipientToken, senderId, messageText } = data;

  const message = {
    token: recipientToken,
    notification: {
      title: 'New message',
      body: messageText
    },
    data: {
      senderId: senderId
    }
  };

  try {
    await admin.messaging().send(message);
    return { success: true };
  } catch (error) {
    console.error('Error sending message:', error);
    return { success: false, error: error.message };
  }
});