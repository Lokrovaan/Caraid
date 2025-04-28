# Caraid - Android Chat Application

Caraid is an Android chat application built using Kotlin and Jetpack Compose. It leverages Firebase for authentication, data storage, and real-time messaging, and utilizes the Google Cloud Natural Language Processing (NLP) API for message analysis[cite: 123, 124, 125].

## Features

* **User Authentication:** Users can create accounts using their email addresses and passwords, and securely log in to access the application's features. This process is handled using Firebase Authentication[cite: 128, 129].
* **Chat Management:** Users are presented with a list of their active chats, allowing them to easily navigate and select conversations. The application supports individual chat screens where users can exchange messages in real-time[cite: 129, 130].
* **Real-time Messaging:** Messages are sent and received instantly, providing a seamless and responsive chat experience. The application uses Firebase Firestore to persist messages and updates the UI in real-time[cite: 131, 132].
* **Message Analysis (Optional):** The application integrates with the Google Cloud Natural Language API to analyze the content of messages[cite: 133].
    * **Sentiment Analysis:** Determines the emotional tone of a message (positive, negative, or neutral)[cite: 133, 134].
    * **Entity Extraction:** Identifies and categorizes important entities within the message, such as people, places, and organizations[cite: 135].
* **User Interface:** The application features a modern and intuitive user interface built using Jetpack Compose[cite: 136, 137].
    * Clean and consistent design across different screens.
    * Reusable UI components for efficiency and maintainability.
    * Smooth navigation between screens.

## Technologies and Services Used

* **Jetpack Compose:** The application's user interface is built using Jetpack Compose, Android's modern toolkit for building native UI[cite: 138, 139].
* **Firebase:**
    * **Firebase Authentication:** Handles user registration, login, and account management[cite: 140].
    * **Firebase Firestore:** A NoSQL cloud database used to store user data, chat information, and messages.  Firestore enables real-time data synchronization[cite: 141, 142].
* **Google Cloud Natural Language API:** Used to perform natural language processing tasks (sentiment analysis and entity extraction)[cite: 143].
* **Kotlin Coroutines:** Used to handle asynchronous operations[cite: 144, 145].
* **AndroidX Navigation Compose:** Used to manage navigation between different composable functions[cite: 148].

## Data Model

The application uses the following data classes to structure data[cite: 149]:

* **User:** Represents a user of the application (User.kt)[cite: 149].
* **Chat:** Represents a chat conversation between two users (Chat.kt)[cite: 150].
* **Message:** Represents a single message within a chat (Message.kt)[cite: 151].

## Architecture

The application follows a modular design[cite: 152, 153]:

* **UI Layer:** Built with Jetpack Compose, handles the presentation of data and user interaction[cite: 153, 154].
* **Data Layer:** Responsible for data access and management. Interacts with Firebase Firestore and the Google Cloud NL API[cite: 155, 156].
* **Navigation:** Navigation.kt uses NavHost to define the navigation graph[cite: 157].

## Workflow

1.  **User Authentication:** The user launches the application and is presented with the login screen (LoginActivity.kt). The user can log in or create an account. Firebase Authentication handles this[cite: 158, 159].
2.  **Chat List Display:** Upon successful login, the user is navigated to the chat list screen (ChatListActivity.kt). The user's active chats are retrieved from Firestore and displayed[cite: 160, 161].
3.  **Chat Interaction:** The user selects a chat to open the chat screen (ChatScreenActivity.kt). Users can send and receive messages in real-time. Messages are stored in Firestore[cite: 162, 163, 164].
4.  **Message Analysis (Optional):** Users can choose to analyze individual messages. The Google Cloud NL API is used for sentiment analysis and entity extraction, and the results are displayed[cite: 165, 166, 167].

## Key Components

* **LoginActivity.kt:** Handles user authentication[cite: 168, 169].
* **ChatListActivity.kt:** Displays the list of active chats[cite: 169, 170].
* **ChatScreenActivity.kt:** Provides the interface for chat conversations and message analysis[cite: 170].
* **Navigation.kt:** Defines the navigation structure[cite: 171].
* **NLPAnalyser.kt:** Integrates with the Google Cloud NL API for message analysis[cite: 171].
* **UI.kt:** Contains reusable UI components[cite: 172].
