/*
Callum Smith - S2145086
 */

package com.caraid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.caraid.ui.theme.CaraidPurplePrimary
import com.caraid.ui.theme.CaraidPurpleTertiary
import com.caraid.ui.theme.CaraidTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        setContent {
            CaraidTheme { LoginScreen(auth) }
        }
    }
}

@Composable
fun LoginScreen(auth: FirebaseAuth) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var showAccountCreationForm by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .background(CaraidPurplePrimary)
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.white_v),
                    contentDescription = "Caraid logo",
                    modifier = Modifier
                        .size(250.dp)
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                // Email TextField
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = showError
                )

                // Password TextField
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = showError
                )

                if (showError) {
                    Text(
                        text = "Invalid email or password",
                        color = Color.Red,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // Login Button
                Button(
                    onClick = {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Sign in success, start ChatListActivity
                                    val intent = Intent(context, ChatListActivity::class.java)
                                    context.startActivity(intent)
                                } else {
                                    showError = true
                                }
                            }
                    },
                    modifier = Modifier.padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CaraidPurpleTertiary
                    )
                ) {
                    Text("Login", color = Color.White)
                }

                // Create Account Button
                Button(
                    onClick = { showAccountCreationForm = true },
                    modifier = Modifier.padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CaraidPurpleTertiary
                    )
                ) {
                    Text("Create Account", color = Color.White)
                }

                // Account Creation Form Dialog
                if (showAccountCreationForm) {
                    AlertDialog(
                        onDismissRequest = { showAccountCreationForm = false },
                        containerColor = CaraidPurplePrimary,
                        modifier = Modifier.background(Color.Transparent),
                        title = {
                            Text(
                                "Create Account",
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        },
                        text = { AccountCreationForm(auth) },
                        confirmButton = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Button(
                                    onClick = { showAccountCreationForm = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CaraidPurpleTertiary
                                    )
                                ) {
                                    Text("Cancel")
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun AccountCreationForm(auth: FirebaseAuth) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                                if (tokenTask.isSuccessful) {
                                    val token = tokenTask.result
                                    val db = FirebaseFirestore.getInstance()
                                    val user = hashMapOf(
                                        "userId" to userId,
                                        "username" to username,
                                        "email" to email,
                                        "fcmToken" to token
                                    )
                                    db.collection("users").document(userId!!)
                                        .set(user)
                                        .addOnSuccessListener {
                                            Log.d("MyTag", "Account Created")
                                            Toast.makeText(
                                                context,
                                                "Account created successfully!",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            // Log in the user
                                            auth.signInWithEmailAndPassword(email, password)
                                                .addOnCompleteListener { loginTask ->
                                                    if (loginTask.isSuccessful) {
                                                        // Navigate to the chat list screen
                                                        val intent = Intent(context, ChatListActivity::class.java)
                                                        context.startActivity(intent)
                                                    } else {
                                                        // Handle login error
                                                        Log.e(
                                                            "MyTag",
                                                            "Login failed after account creation",
                                                            loginTask.exception
                                                        )
                                                        Toast.makeText(
                                                            context,
                                                            "Login failed after account creation",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.e("MyTag", "Account Creation Failed", exception)
                                            Toast.makeText(
                                                context,
                                                "Account creation unsuccessful, please try again",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                } else {
                                    // Handle token fetch error
                                    Log.e("MyTag", "FCM token fetch failed", tokenTask.exception)
                                    Toast.makeText(
                                        context,
                                        "FCM token fetch failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            // Account creation failed, display error message
                            Log.e("MyTag", "Account creation failed", task.exception)
                            Toast.makeText(
                                context,
                                "Account creation failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = CaraidPurpleTertiary
            )
        ) {
            Text("Create Account", color = Color.White)
        }
    }
}