/*
Callum Smith - S2145086
 */

package com.caraid

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
fun ChatListTopAppBar() {
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