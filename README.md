#   Caraid - Android Chat Application

Caraid is an Android chat application built using Kotlin and Jetpack Compose. It utilizes Firebase for authentication, data storage, and real-time messaging, and the Google Cloud Natural Language Processing (NLP) API for message analysis.

##   Features

* **User Authentication:** Users can create accounts using their email addresses and passwords, and securely log in to access the application's features.
    * This process is handled using Firebase Authentication.
* **Chat Management:** Users are presented with a list of their active chats, allowing them to easily navigate and select conversations.
    * The application supports individual chat screens where users can exchange messages in real-time.
* **Real-time Messaging:** Messages are sent and received instantly, providing a seamless and responsive chat experience.
    * The application uses Firebase Firestore to persist messages and updates the UI in real-time.
* **Message Analysis (Optional):** The application integrates with the Google Cloud Natural Language API to analyze the content of messages.
    * **Sentiment Analysis:** Determines the emotional tone of a message (positive, negative, or neutral).
* **User Interface:** The application features a modern and intuitive user interface built using Jetpack Compose.
    * Clean and consistent design across different screens.
    * Reusable UI components for efficiency and maintainability.
    * Smooth navigation between screens.

##   Technologies and Services Used

* **Jetpack Compose:** The application's user interface is built using Jetpack Compose, Android's modern toolkit for building native UI.
* **Firebase:**
    * **Firebase Authentication:** Handles user registration, login, and account management.
    * **Firebase Firestore:** A NoSQL cloud database used to store user data, chat information, and messages. Firestore enables real-time data synchronization.
* **Google Cloud Natural Language API:** This API is used to perform natural language processing tasks (sentiment analysis and entity extraction).
* **Kotlin Coroutines:** Kotlin coroutines are used to handle asynchronous operations.
* **AndroidX Navigation Compose:** This library is used to manage navigation between different composable functions.

##   Data Model

The application uses the following data classes to structure data:

* **User:** Represents a user of the application (User.kt).
* **Chat:** Represents a chat conversation between two users (Chat.kt).
* **Message:** Represents a single message within a chat, including the sender, content, and timestamp (Message.kt).

##   Architecture

The application follows a modular design:

* **UI Layer:** Built with Jetpack Compose, this layer handles the presentation of data and user interaction.
* **Data Layer:** This layer is responsible for data access and management. It interacts with Firebase Firestore and the Google Cloud NL API.
* **Navigation:** The Navigation.kt file uses NavHost to define the navigation graph, enabling seamless transitions between different screens within the application.

##   Workflow

1.  **User Authentication:** A user launches the application and is presented with the login screen (LoginActivity.kt). The user can either log in with existing credentials or create a new account. Firebase Authentication handles the authentication process.
2.  **Chat List Display:** Upon successful login, the user is navigated to the chat list screen (ChatListActivity.kt). The application retrieves the user's active chats from Firestore and displays them in a list.
3.  **Chat Interaction:** The user selects a chat from the list to open the chat screen (ChatScreenActivity.kt). On the chat screen, users can send and receive messages in real-time. Messages are stored in Firestore, and any new messages are immediately visible to both users.
4.  **Message Analysis (Optional):** The application provides the functionality to analyze individual messages. When a user chooses to analyze a message, the application uses the Google Cloud NL API to perform sentiment analysis and entity extraction. The results of the analysis are displayed to the user.

##   Key Components

* **LoginActivity.kt:** Handles user authentication, including login and account creation.
* **ChatListActivity.kt:** Displays the list of active chats for the logged-in user.
* **ChatScreenActivity.kt:** Provides the interface for individual chat conversations, including message display, sending, and analysis.
* **Navigation.kt:** Defines the navigation structure of the application.
* **NLPAnalyser.kt:** Integrates with the Google Cloud NL API to perform message analysis.
* **UI.kt:** Contains reusable UI components, such as CustomTopAppBar and BottomNavigationBar.
