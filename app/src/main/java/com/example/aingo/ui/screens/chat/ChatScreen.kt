package com.example.aingo.ui.chat

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aingo.ui.components.ChatBubble
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val messages by viewModel.messages.collectAsState()
    var textState by remember { mutableStateOf(TextFieldValue("")) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Лаунчер
    val recordAudioLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening { voiceText -> textState = TextFieldValue(voiceText) }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    val isKeyboardVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp
    LaunchedEffect(isKeyboardVisible) {
        if (isKeyboardVisible && messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        //  TOP BAR
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Aingo Teacher", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Online AI Tutor", color = Color(0xFF00E5FF), fontSize = 9.sp)
                }
                IconButton(onClick = { viewModel.toggleSound() }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (viewModel.isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = null,
                        tint = if (viewModel.isSoundEnabled) Color(0xFF00E5FF) else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        //  MESSAGES LIST
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message -> ChatBubble(message) }
        }

        // COMPACT INPUT BAR
        ChatInputBar(
            textState = textState,
            isRecording = viewModel.isRecording,
            onMicClick = {
                if (viewModel.isRecording) {
                    viewModel.stopListening()
                } else {
                    recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            onTextChange = { textState = it },
            onSendClick = {
                if (textState.text.isNotBlank()) {
                    viewModel.sendMessage(textState.text)
                    textState = TextFieldValue("")
                    coroutineScope.launch { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1) }
                }
            }
        )

        Spacer(Modifier.height(4.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputBar(
    textState: TextFieldValue,
    isRecording: Boolean,
    onMicClick: () -> Unit,
    onTextChange: (TextFieldValue) -> Unit,
    onSendClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
        color = Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMicClick,
                modifier = Modifier.size(34.dp).background(
                    if (isRecording) Color.Red.copy(0.2f) else Color.Transparent,
                    CircleShape
                )
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.MicNone else Icons.Default.Mic,
                    null,
                    tint = if (isRecording) Color.Red else Color(0xFF00E5FF),
                    modifier = Modifier.size(18.dp)
                )
            }

            TextField(
                value = textState,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type...", color = Color.Gray, fontSize = 13.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF00E5FF)
                ),
                maxLines = 3
            )

            Box(
                modifier = Modifier.size(34.dp).clip(CircleShape).background(
                    if (textState.text.isNotBlank()) Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF00B0FF)))
                    else SolidColor(Color.White.copy(alpha = 0.1f))
                ).clickable(enabled = textState.text.isNotBlank()) { onSendClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Send, null, tint = if (textState.text.isNotBlank()) Color(0xFF0C1825) else Color.Gray, modifier = Modifier.size(14.dp))
            }
            Spacer(Modifier.width(4.dp))
        }
    }
}