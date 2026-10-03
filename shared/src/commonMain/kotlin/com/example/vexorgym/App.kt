package com.example.vexorgym

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.vexorgym.ui.navigation.AppNavHost

@Composable
@Preview
fun App() {
    MaterialTheme {
        Surface(modifier = Modifier, color = MaterialTheme.colorScheme.background) {
            AppNavHost()
        }
    }
}
