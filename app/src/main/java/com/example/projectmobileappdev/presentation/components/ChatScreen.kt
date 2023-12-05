package com.example.projectmobileappdev.presentation.components


import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.projectmobileappdev.presentation.BluetoothUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ChatScreen(
    state: BluetoothUiState,
    onDisconnect: () -> Unit,
    onSendMessage: (String?, Uri?) -> Unit,
    onGetPhoneNumber: () -> Unit,
) {
    val imagePreviewUri = rememberSaveable { mutableStateOf<Uri?>(null) }
    val message = rememberSaveable {
        mutableStateOf("")
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { imagePreviewUri.value = it }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Messages",
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDisconnect) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Disconnect"
                )
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(state.messages) { message ->
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ChatMessage(
                        message = message,
                        modifier = Modifier
                            .align(
                                if(message.isFromLocalUser) Alignment.End else Alignment.Start
                            )
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onGetPhoneNumber)  {
                Text(text = "Send Phone Number")
            }
            Button(onClick = { launcher.launch("image/*") })  {
                Text(text = "Choose Image")
            }
            imagePreviewUri.value?.let { uri ->

                Image(
                    painter = rememberImagePainter(uri),
                    contentDescription = "Image Preview",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(shape = RoundedCornerShape(4.dp))
                )
            }

        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            TextField(
                value = message.value,
                onValueChange = { message.value = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(text = "Message")
                }

            )

            IconButton(onClick = {
                val textMessage = message.value
                val imageUri = imagePreviewUri.value
                onSendMessage(textMessage, imageUri)
                message.value = ""
                imagePreviewUri.value = null
                keyboardController?.hide()
            }) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send message"
                )
            }
        }
    }
}

