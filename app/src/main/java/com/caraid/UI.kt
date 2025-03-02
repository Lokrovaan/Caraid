package com.caraid

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.caraid.ui.theme.CaraidPurpleTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(title: @Composable () -> Unit) {
    TopAppBar(
        title = title,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CaraidPurpleTertiary,
            titleContentColor = Color.White
        )
    )
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    // This is empty for now, but you can add your bottom bar content here later
}

@Composable
fun ChatListTopAppBar() {
    // Replace with your actual image resource ID
    val image = painterResource(R.drawable.white_h)

    CustomTopAppBar {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = image,
                contentDescription = "Caraid Logo",
            )
        }
    }
}

@Composable
fun ChatListBottomNavigationBar(navController: NavController) {
    // This is empty for now, but you can add your bottom bar content here later
}