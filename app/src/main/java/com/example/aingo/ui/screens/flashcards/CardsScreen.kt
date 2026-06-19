package com.example.aingo.ui.screens


import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aingo.domain.model.WordCard
import kotlin.math.abs


@Composable
fun FlashcardsScreen(flashViewModel: FlashcardsViewModel = viewModel()) {
        Box(
                modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0C1825))
        ) {
                // Ефект градієнту
                Canvas(modifier = Modifier.fillMaxSize().blur(100.dp)) {
                        drawCircle(Color(0xFF00E5FF).copy(0.12f), 500f, Offset(size.width, 0f))
                        drawCircle(Color(0xFFFF3D00).copy(0.08f), 700f, Offset(0f, size.height))
                }

                Scaffold(
                        containerColor = Color.Transparent,
                        floatingActionButton = {
                                FloatingActionButton(
                                        onClick = { flashViewModel.onSyncClicked() },
                                        containerColor = Color(0xFF00E5FF),
                                        contentColor = Color(0xFF0C1825),
                                        shape = CircleShape
                                ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Sync")
                                }
                        }
                ) { paddingValues ->
                        Column(
                                modifier = Modifier
                                        .fillMaxSize()
                                        .padding(paddingValues)
                        ) {

                                Column(
                                        modifier = Modifier
                                                .padding(horizontal = 24.dp)
                                                .padding(top = 8.dp, bottom = 4.dp)
                                ) {
                                        Text(
                                                text = "Vocabulary",
                                                color = Color.White,
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                                text = "Master your new words",
                                                color = Color(0xFF00E5FF).copy(alpha = 0.7f),
                                                fontSize = 13.sp
                                        )
                                }

                                //  CARDS AREA
                                Box(
                                        modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f),
                                        contentAlignment = Alignment.Center
                                ) {
                                        if (flashViewModel.wordCards.isEmpty() && !flashViewModel.isLoading) {
                                                EmptyStateView(isNew = true)
                                        } else {
                                                flashViewModel.wordCards.forEachIndexed { index, word ->
                                                        val isTopCard = index == flashViewModel.wordCards.size - 1
                                                        if (index >= flashViewModel.wordCards.size - 2) {
                                                                key(word.id) {
                                                                        SwipeableCard(
                                                                                word = word,
                                                                                isTopCard = isTopCard,
                                                                                onSwiped = { isKnown -> flashViewModel.onCardSwiped(isKnown, word) }
                                                                        )
                                                                }
                                                        }
                                                }
                                        }
                                }

                                // --- TABS ---
                                CustomTabSelector(
                                        selectedIndex = flashViewModel.filterIndex,
                                        onTabSelected = { flashViewModel.filterIndex = it }
                                )

                                Spacer(Modifier.height(16.dp))
                        }
                }
        }
}

@Composable
fun SwipeableCard(
        word: WordCard,
        isTopCard: Boolean,
        onSwiped: (Boolean) -> Unit
) {
        var offsetX by remember { mutableStateOf(0f) }
        var isFlipped by remember { mutableStateOf(false) }

        // Анімація свайпу (X)
        val animatedX by animateFloatAsState(
                targetValue = offsetX,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow),
                label = "cardX"
        )

        // Анімація перевороту (Y)
        val rotationY by animateFloatAsState(
                targetValue = if (isFlipped) 180f else 0f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                label = "cardFlip"
        )

        val rotationZ = animatedX / 25f
        val scale = if (isTopCard) 1f else 0.9f
        val verticalOffset = if (isTopCard) 0.dp else 15.dp

        Surface(
                modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .offset(y = verticalOffset)
                        .fillMaxWidth()
                        .height(460.dp)
                        .graphicsLayer {
                                // Основне обертання всієї картки
                                translationX = animatedX
                                this.rotationZ = rotationZ
                                this.rotationY = rotationY
                                scaleX = scale
                                scaleY = scale
                                cameraDistance = 12f * density
                                alpha = if (isTopCard) 1f else (abs(animatedX) / 500f).coerceIn(0f, 0.4f)
                        }
                        .clickable(enabled = isTopCard) {
                                isFlipped = !isFlipped
                        }
                        .pointerInput(isTopCard) {
                                if (!isTopCard) return@pointerInput
                                detectDragGestures(
                                        onDrag = { change, dragAmount ->
                                                change.consume()
                                                offsetX += dragAmount.x
                                        },
                                        onDragEnd = {
                                                if (offsetX > 450) onSwiped(true)
                                                else if (offsetX < -450) onSwiped(false)
                                                offsetX = 0f
                                                isFlipped = false
                                        }
                                )
                        },
                color = Color(0xFF162A3D),
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                shadowElevation = if (isTopCard) 8.dp else 0.dp
        ) {

                Box(
                        modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                        // ТУТ ФІКС: Обертаємо контент всередині на 180, якщо картка перевернута,
                                        // щоб текст не відображався дзеркально.
                                        this.rotationY = if (rotationY > 90f) 180f else 0f
                                },
                        contentAlignment = Alignment.Center
                ) {
                        if (rotationY <= 90f) {
                                // ПЕРЕДНЯ СТОРОА
                                Column(
                                        modifier = Modifier.padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                        Text(
                                                text = word.original,
                                                color = Color(0xFF00E5FF),
                                                fontSize = 38.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                        )
                                        Spacer(Modifier.height(12.dp))
                                        Text(
                                                text = "Tap to see translation",
                                                color = Color.White.copy(alpha = 0.3f),
                                                fontSize = 12.sp
                                        )
                                }
                        } else {
                                // ЗАДНЯ СТОРОНА
                                Column(
                                        modifier = Modifier.padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                        Text(
                                                text = word.translation,
                                                color = Color.White,
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.Medium,
                                                textAlign = TextAlign.Center
                                        )
                                        Spacer(Modifier.height(24.dp))

                                        Box(
                                                modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .background(Color.Black.copy(alpha = 0.2f))
                                                        .padding(20.dp)
                                        ) {
                                                Text(
                                                        text = word.example,
                                                        color = Color.LightGray,
                                                        fontSize = 16.sp,
                                                        textAlign = TextAlign.Center,
                                                        lineHeight = 22.sp
                                                )
                                        }
                                }
                        }
                }
        }
}

@Composable
fun CustomTabSelector(selectedIndex: Int, onTabSelected: (Int) -> Unit) {
        val tabs = listOf("New Words", "Mastered")

        Row(
                modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(28.dp))
                        .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
                tabs.forEachIndexed { index, title ->
                        val isSelected = selectedIndex == index
                        val bgColor by animateColorAsState(
                                if (isSelected) Color(0xFF00E5FF) else Color.Transparent, label = ""
                        )
                        val textColor by animateColorAsState(
                                if (isSelected) Color(0xFF0C1825) else Color.Gray, label = ""
                        )

                        Box(
                                modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(bgColor)
                                        .clickable { onTabSelected(index) },
                                contentAlignment = Alignment.Center
                        ) {
                                Text(text = title, color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                }
        }
}

@Composable
fun EmptyStateView(isNew: Boolean) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                        modifier = Modifier.size(120.dp).background(Color.White.copy(0.05f), CircleShape),
                        contentAlignment = Alignment.Center
                ) {
                        Icon(
                                if (isNew) Icons.Default.Refresh else Icons.Default.Refresh,
                                null,
                                tint = Color.Gray,
                                modifier = Modifier.size(50.dp)
                        )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                        if (isNew) "Your stack is empty!" else "No learned words yet.",
                        color = Color.Gray,
                        fontSize = 16.sp
                )
        }
}