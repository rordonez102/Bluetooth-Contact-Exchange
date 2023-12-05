package com.example.projectmobileappdev.presentation.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectmobileappdev.domain.chat.BluetoothMessage
import com.example.projectmobileappdev.ui.theme.Pink80
import com.example.projectmobileappdev.ui.theme.ProjectMobileappdevTheme
import com.example.projectmobileappdev.ui.theme.Purple80

@Composable
fun ChatMessage(
    message: BluetoothMessage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(
                RoundedCornerShape(
                    topStart = if (message.isFromLocalUser) 15.dp else 0.dp,
                    topEnd = 15.dp,
                    bottomStart = 15.dp,
                    bottomEnd = if (message.isFromLocalUser) 0.dp else 15.dp
                )
            )
            .background(
                if (message.isFromLocalUser) Purple80 else Pink80
            )
            .padding(16.dp)
    ) {
        Text(
            text = message.senderName,
            fontSize = 10.sp,
            color = Color.Black
        )
        if (message.imageBytes != null) {
            val bitmap = BitmapFactory.decodeByteArray(message.imageBytes, 0, message.imageBytes.size)
            // Display the image
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Test Image",
                modifier = Modifier
                    .size(100.dp)
                    .clip(shape = RoundedCornerShape(4.dp))
            )
        } else if (message.message != null)  {
            // Display the text message
            Text(
                text = message.message,
                color = Color.Black,
                modifier = Modifier.widthIn(max = 250.dp)
            )
        }
    }

}

@Preview
@Composable
fun ChatMessagePreview() {
    ProjectMobileappdevTheme {
        ChatMessage(
            message = BluetoothMessage(
                message = "Hello World!",
                senderName = "Person 1",
                isFromLocalUser= true
            )
        )
    }
}