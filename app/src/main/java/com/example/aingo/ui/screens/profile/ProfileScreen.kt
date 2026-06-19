package com.example.aingo.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onNavigateToLogin: () -> Unit
) {
    val profile = viewModel.userProfile

    var isEditing by remember { mutableStateOf(false) }
    var tempName by remember(profile) { mutableStateOf(profile?.name ?: "") }
    var tempLevel by remember(profile) { mutableStateOf(profile?.level ?: "A1") }

    LaunchedEffect(Unit) {
        viewModel.updateLearnedCount()
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            //  HEADER
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 20.dp), // Мінімальний відступ зверху
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF00B0FF))),
                                CircleShape
                            )
                            .padding(2.dp)
                            .background(Color(0xFF0C1825), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!profile?.name.isNullOrBlank()) {
                            Text(
                                text = profile?.name?.take(1)?.uppercase() ?: "",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF00E5FF)
                            )
                        } else {
                            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(45.dp))
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = profile?.name ?: "User Name",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = profile?.email ?: "email@example.com",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp
                    )
                }
            }

            //  STATISTICS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBox(
                    modifier = Modifier.weight(1f),
                    label = "Words",
                    value = viewModel.learnedWordsCount.toString(),
                    icon = Icons.Default.MenuBook
                )
                StatBox(
                    modifier = Modifier.weight(1f),
                    label = "Chats",
                    value = profile?.totalChats?.toString() ?: "0",
                    icon = Icons.Default.ChatBubbleOutline
                )
            }

            Spacer(Modifier.height(20.dp))

            // AI ASSESSMENT CARD
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF00E5FF).copy(alpha = 0.05f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("AI Smart Assessment", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(Modifier.height(10.dp))

                    val aiText = if (viewModel.aiFeedback.isNotEmpty()) viewModel.aiFeedback
                    else "Analyze your chats to see how Aingo estimates your English level."

                    Text(
                        text = aiText,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.assessEnglishLevel() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !viewModel.isAnalyzingLevel
                    ) {
                        if (viewModel.isAnalyzingLevel) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF0C1825))
                        } else {
                            Text("Analyze Progress", color = Color(0xFF0C1825), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            //  INFO LIST
            InfoRow(label = "Current Level", value = profile?.level ?: "A2", icon = Icons.Default.BarChart)
            InfoRow(label = "Detected by AI", value = viewModel.aiDetectedLevel.ifEmpty { "Not analyzed" }, icon = Icons.Default.Psychology)

            Spacer(Modifier.height(24.dp))

            // ACTIONS
            Button(
                onClick = { isEditing = true },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Edit Profile Settings", color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = { viewModel.logout(onNavigateToLogin) },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.Logout, null, tint = Color.Red.copy(0.7f), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text("Logout from Account", color = Color.Red.copy(0.7f))
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    //  EDIT DIALOG
    if (isEditing) {
        AlertDialog(
            onDismissRequest = { isEditing = false },
            containerColor = Color(0xFF162A3D),
            title = { Text("Update Profile", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Name", color = Color(0xFF00E5FF)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color.White.copy(0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = tempLevel,
                        onValueChange = { tempLevel = it },
                        label = { Text("Level (A1-C2)", color = Color(0xFF00E5FF)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color.White.copy(0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(tempName, tempLevel)
                        isEditing = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                ) {
                    Text("Save", color = Color(0xFF0C1825), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditing = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun StatBox(modifier: Modifier, label: String, value: String, icon: ImageVector) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = Color(0xFF00E5FF).copy(0.6f), modifier = Modifier.size(16.dp))
            Spacer(Modifier.height(6.dp))
            Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(label, color = Color.White.copy(0.4f), fontSize = 10.sp)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color.White.copy(0.05f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, color = Color.White.copy(0.4f), fontSize = 11.sp)
            Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}