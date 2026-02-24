package com.example.smart_labs.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.smart_labs.R

@Composable
fun AppHeader(
    modifier: Modifier = Modifier,
    showScreenTitle: Boolean = false,
    screenTitle: String = ""
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_rr),
            contentDescription = "Логотип RzhevskyRobotics",
            modifier = Modifier.size(96.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Управление умным реактором химического синтеза",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        if (showScreenTitle && screenTitle.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = screenTitle,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}