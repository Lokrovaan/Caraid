package com.caraid

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.caraid.ui.theme.CaraidPurplePrimary

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    Scaffold(
        topBar = {
            ChatListTopAppBar()
        },
        bottomBar = {
            ChatListBottomNavigationBar(navController)
        },
        containerColor = CaraidPurplePrimary
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "chat_list",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("chat_list") { ChatListScreen(navController) }
            composable("chat_screen/{chatId}") { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                ChatScreen(chatId, navController)
            }
        }
    }
}