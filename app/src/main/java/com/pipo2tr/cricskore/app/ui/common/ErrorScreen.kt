package com.pipo2tr.cricskore.app.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.Text

@Composable
fun ErrorScreen(message: String = "An unexpected error occurred") {
    Text(text = message, Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
}
