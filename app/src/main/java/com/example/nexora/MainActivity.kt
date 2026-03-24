package com.example.nexora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.nexora.ui.screens.NexoraApp
import com.example.nexora.ui.theme.NexoraTheme
import com.example.nexora.ui.viewmodel.LoginViewModel

class MainActivity : ComponentActivity() {

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NexoraTheme {
                NexoraApp(loginViewModel = loginViewModel)
            }
        }
    }
}
