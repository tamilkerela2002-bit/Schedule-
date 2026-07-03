package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun AnimeCompanion(
    companionName: String,
    mood: String,
    timeOfDay: String,
    userRole: String,
    modifier: Modifier = Modifier
) {
    // Companion Dialogue Logic
    val dialogue = remember(companionName, mood, userRole) {
        getCompanionDialogue(companionName, mood, userRole)
    }

    // Interactive blinking and float animations
    val infiniteTransition = rememberInfiniteTransition(label = "CompanionAnimations")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatAnim"
    )

    // Blink state
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay((2000..5000).random().toLong())
            isBlinking = true
            delay(150)
            isBlinking = false
        }
    }

    // Background Gradient based on Time of Day
    val bgGradient = remember(timeOfDay) {
        when (timeOfDay) {
            "Morning" -> Brush.verticalGradient(
                colors = listOf(Color(0xFFFFE0B2), Color(0xFFFFB74D)) // Golden hour sunrise
            )
            "Afternoon" -> Brush.verticalGradient(
                colors = listOf(Color(0xFFB3E5FC), Color(0xFF4FC3F7)) // Bright sky blue
            )
            "Evening" -> Brush.verticalGradient(
                colors = listOf(Color(0xFFE1BEE7), Color(0xFFBA68C8)) // Sunset lilac violet
            )
            else -> Brush.verticalGradient(
                colors = listOf(Color(0xFF1A237E), Color(0xFF0D47A1)) // Starry deep night
            )
        }
    }

    // Base Hair Color based on companion
    val hairColor = remember(companionName) {
        when (companionName) {
            "Aria" -> Color(0xFF00ACC1) // Teal
            "Ken" -> Color(0xFFE65100) // Vibrant orange
            else -> Color(0xFFF06292) // Pastel pink for Koko
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("anime_companion_card"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Character Speech Bubble
        Box(
            modifier = Modifier
                .padding(bottom = 12.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .widthIn(max = 280.dp)
        ) {
            Column {
                Text(
                    text = "$companionName • ${companionTitle(companionName)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    text = dialogue,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        // The Vector Drawn Character Canvas
        Box(
            modifier = Modifier
                .size(150.dp)
                .offset { androidx.compose.ui.unit.IntOffset(0, floatOffset.dp.roundToPx()) }
                .clip(RoundedCornerShape(75.dp))
                .background(bgGradient)
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(75.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val scale = size.width / 150f

                // Draw Neck
                val neckPath = Path().apply {
                    moveTo(center.x - 15f * scale, center.y + 20f * scale)
                    lineTo(center.x + 15f * scale, center.y + 20f * scale)
                    lineTo(center.x + 10f * scale, center.y + 45f * scale)
                    lineTo(center.x - 10f * scale, center.y + 45f * scale)
                    close()
                }
                drawPath(neckPath, Color(0xFFFCE4EC)) // Skin-ish neck

                // Draw Shirt
                val shirtPath = Path().apply {
                    moveTo(center.x - 35f * scale, center.y + 42f * scale)
                    lineTo(center.x + 35f * scale, center.y + 42f * scale)
                    lineTo(center.x + 50f * scale, center.y + 75f * scale)
                    lineTo(center.x - 50f * scale, center.y + 75f * scale)
                    close()
                }
                drawPath(shirtPath, Color(0xFF37474F)) // Deep grey/navy suit

                // Draw Head / Face Circle
                drawCircle(
                    color = Color(0xFFFFF3E0), // Clean anime skin color
                    radius = 35f * scale,
                    center = center
                )

                // Blush on Cheeks (Happy, Creative, Stressed)
                if (mood == "Happy" || mood == "Creative" || mood == "Stressed") {
                    drawCircle(
                        color = Color(0xFFFF8A80).copy(alpha = 0.6f),
                        radius = 6f * scale,
                        center = Offset(center.x - 18f * scale, center.y + 10f * scale)
                    )
                    drawCircle(
                        color = Color(0xFFFF8A80).copy(alpha = 0.6f),
                        radius = 6f * scale,
                        center = Offset(center.x + 18f * scale, center.y + 10f * scale)
                    )
                }

                // Eye Drawing
                val leftEyeCenter = Offset(center.x - 14f * scale, center.y + 2f * scale)
                val rightEyeCenter = Offset(center.x + 14f * scale, center.y + 2f * scale)

                if (isBlinking) {
                    // Closed blinking eyes (line)
                    drawLine(Color(0xFF263238), leftEyeCenter - Offset(6f * scale, 0f), leftEyeCenter + Offset(6f * scale, 0f), strokeWidth = 3f * scale)
                    drawLine(Color(0xFF263238), rightEyeCenter - Offset(6f * scale, 0f), rightEyeCenter + Offset(6f * scale, 0f), strokeWidth = 3f * scale)
                } else {
                    when (mood) {
                        "Focused" -> {
                            // High determination eyes
                            drawCircle(Color(0xFF263238), radius = 5f * scale, center = leftEyeCenter)
                            drawCircle(Color(0xFF263238), radius = 5f * scale, center = rightEyeCenter)
                            // Adorable tiny glasses
                            drawCircle(Color(0xFFE0F7FA), radius = 10f * scale, center = leftEyeCenter, style = Stroke(width = 2f * scale))
                            drawCircle(Color(0xFFE0F7FA), radius = 10f * scale, center = rightEyeCenter, style = Stroke(width = 2f * scale))
                            drawLine(Color(0xFFE0F7FA), leftEyeCenter + Offset(10f * scale, 0f), rightEyeCenter - Offset(10f * scale, 0f), strokeWidth = 2f * scale)
                        }
                        "Happy" -> {
                            // Smiling closed eyes (^^)
                            val eyePathL = Path().apply {
                                moveTo(leftEyeCenter.x - 6f * scale, leftEyeCenter.y + 2f * scale)
                                quadraticTo(leftEyeCenter.x, leftEyeCenter.y - 4f * scale, leftEyeCenter.x + 6f * scale, leftEyeCenter.y + 2f * scale)
                            }
                            val eyePathR = Path().apply {
                                moveTo(rightEyeCenter.x - 6f * scale, rightEyeCenter.y + 2f * scale)
                                quadraticTo(rightEyeCenter.x, rightEyeCenter.y - 4f * scale, rightEyeCenter.x + 6f * scale, rightEyeCenter.y + 2f * scale)
                            }
                            drawPath(eyePathL, Color(0xFF263238), style = Stroke(width = 3f * scale))
                            drawPath(eyePathR, Color(0xFF263238), style = Stroke(width = 3f * scale))
                        }
                        "Tired" -> {
                            // Sleepy droopy eyes (half-closed)
                            drawArc(
                                color = Color(0xFF263238),
                                startAngle = 0f,
                                sweepAngle = 180f,
                                useCenter = false,
                                topLeft = Offset(leftEyeCenter.x - 6f * scale, leftEyeCenter.y - 2f * scale),
                                size = Size(12f * scale, 8f * scale),
                                style = Stroke(width = 2.5f * scale)
                            )
                            drawArc(
                                color = Color(0xFF263238),
                                startAngle = 0f,
                                sweepAngle = 180f,
                                useCenter = false,
                                topLeft = Offset(rightEyeCenter.x - 6f * scale, rightEyeCenter.y - 2f * scale),
                                size = Size(12f * scale, 8f * scale),
                                style = Stroke(width = 2.5f * scale)
                            )
                        }
                        "Creative" -> {
                            // Sparkly creative star eyes or glowing dots
                            drawCircle(Color(0xFFFFD54F), radius = 6f * scale, center = leftEyeCenter)
                            drawCircle(Color(0xFFFFD54F), radius = 6f * scale, center = rightEyeCenter)
                            drawCircle(Color(0xFFFFFFFF), radius = 2f * scale, center = leftEyeCenter - Offset(2f, 2f))
                            drawCircle(Color(0xFFFFFFFF), radius = 2f * scale, center = rightEyeCenter - Offset(2f, 2f))
                        }
                        "Stressed" -> {
                            // Worried wavy eyes
                            val eyePathL = Path().apply {
                                moveTo(leftEyeCenter.x - 5f * scale, leftEyeCenter.y - 1f * scale)
                                lineTo(leftEyeCenter.x + 5f * scale, leftEyeCenter.y + 1f * scale)
                            }
                            val eyePathR = Path().apply {
                                moveTo(rightEyeCenter.x - 5f * scale, rightEyeCenter.y + 1f * scale)
                                lineTo(rightEyeCenter.x + 5f * scale, rightEyeCenter.y - 1f * scale)
                            }
                            drawPath(eyePathL, Color(0xFF263238), style = Stroke(width = 3f * scale))
                            drawPath(eyePathR, Color(0xFF263238), style = Stroke(width = 3f * scale))
                        }
                        else -> {
                            // Normal eyes
                            drawCircle(Color(0xFF263238), radius = 4f * scale, center = leftEyeCenter)
                            drawCircle(Color(0xFF263238), radius = 4f * scale, center = rightEyeCenter)
                        }
                    }
                }

                // Mouth Drawing
                val mouthCenter = Offset(center.x, center.y + 16f * scale)
                when (mood) {
                    "Happy" -> {
                        // Wide happy smile
                        drawArc(
                            color = Color(0xFFD81B60),
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = true,
                            topLeft = Offset(mouthCenter.x - 6f * scale, mouthCenter.y - 3f * scale),
                            size = Size(12f * scale, 8f * scale)
                        )
                    }
                    "Tired" -> {
                        // Tiny yawn circle
                        drawCircle(Color(0xFFD81B60), radius = 3f * scale, center = mouthCenter)
                    }
                    "Stressed" -> {
                        // Worried wavy mouth
                        val mouthPath = Path().apply {
                            moveTo(mouthCenter.x - 6f * scale, mouthCenter.y)
                            quadraticTo(mouthCenter.x - 3f * scale, mouthCenter.y - 2f * scale, mouthCenter.x, mouthCenter.y)
                            quadraticTo(mouthCenter.x + 3f * scale, mouthCenter.y + 2f * scale, mouthCenter.x + 6f * scale, mouthCenter.y)
                        }
                        drawPath(mouthPath, Color(0xFF263238), style = Stroke(width = 2f * scale))
                    }
                    "Focused" -> {
                        // Flat determined line mouth
                        drawLine(Color(0xFF263238), mouthCenter - Offset(4f * scale, 0f), mouthCenter + Offset(4f * scale, 0f), strokeWidth = 2f * scale)
                    }
                    else -> {
                        // Tiny simple smile
                        val mouthPath = Path().apply {
                            moveTo(mouthCenter.x - 4f * scale, mouthCenter.y)
                            quadraticTo(mouthCenter.x, mouthCenter.y + 3f * scale, mouthCenter.x + 4f * scale, mouthCenter.y)
                        }
                        drawPath(mouthPath, Color(0xFF263238), style = Stroke(width = 2f * scale))
                    }
                }

                // Draw Hair (Custom Anime bangs and sides depending on selected character)
                val hairPath = Path()
                if (companionName == "Ken") {
                    // Spiky energetic hair
                    hairPath.apply {
                        moveTo(center.x - 42f * scale, center.y - 10f * scale)
                        lineTo(center.x - 35f * scale, center.y - 35f * scale)
                        lineTo(center.x - 20f * scale, center.y - 25f * scale)
                        lineTo(center.x, center.y - 45f * scale) // Top spike
                        lineTo(center.x + 20f * scale, center.y - 25f * scale)
                        lineTo(center.x + 35f * scale, center.y - 35f * scale)
                        lineTo(center.x + 42f * scale, center.y - 10f * scale)
                        // Bangs overlapping face
                        lineTo(center.x + 15f * scale, center.y - 15f * scale)
                        lineTo(center.x + 5f * scale, center.y - 22f * scale)
                        lineTo(center.x - 8f * scale, center.y - 15f * scale)
                        close()
                    }
                } else if (companionName == "Aria") {
                    // Smart long straight bangs with hair clips
                    hairPath.apply {
                        moveTo(center.x - 38f * scale, center.y + 10f * scale)
                        lineTo(center.x - 38f * scale, center.y - 35f * scale)
                        quadraticTo(center.x, center.y - 45f * scale, center.x + 38f * scale, center.y - 35f * scale)
                        lineTo(center.x + 38f * scale, center.y + 10f * scale)
                        // Bangs falling between eyes
                        lineTo(center.x + 10f * scale, center.y - 12f * scale)
                        lineTo(center.x, center.y - 18f * scale)
                        lineTo(center.x - 10f * scale, center.y - 12f * scale)
                        close()
                    }
                } else {
                    // Koko (cozy puffy hair)
                    hairPath.apply {
                        moveTo(center.x - 40f * scale, center.y)
                        quadraticTo(center.x - 38f * scale, center.y - 42f * scale, center.x, center.y - 42f * scale)
                        quadraticTo(center.x + 38f * scale, center.y - 42f * scale, center.x + 40f * scale, center.y)
                        // Soft cute curly bangs
                        lineTo(center.x + 20f * scale, center.y - 15f * scale)
                        quadraticTo(center.x + 10f * scale, center.y - 22f * scale, center.x, center.y - 16f * scale)
                        quadraticTo(center.x - 10f * scale, center.y - 22f * scale, center.x - 20f * scale, center.y - 15f * scale)
                        close()
                    }
                }
                drawPath(hairPath, hairColor)

                // Optional hair accessory or hair highlights
                if (companionName == "Aria") {
                    // Little golden hair clip
                    drawRect(
                        color = Color(0xFFFFD54F),
                        topLeft = Offset(center.x - 25f * scale, center.y - 25f * scale),
                        size = Size(8f * scale, 3f * scale)
                    )
                }
            }
        }
    }
}

@Composable
fun BentoCompanionCard(
    companionName: String,
    mood: String,
    timeOfDay: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Interactive blinking and float animations
    val infiniteTransition = rememberInfiniteTransition(label = "BentoCompanionAnimations")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BentoFloat"
    )

    // Blink state
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay((2000..5000).random().toLong())
            isBlinking = true
            delay(150)
            isBlinking = false
        }
    }

    // Background Color or light gradient matching the HTML theme (e.g. #EADDFF) with border #D0BCFF
    val cardBg = Color(0xFFEADDFF)
    val borderCol = Color(0xFFD0BCFF)
    val textCol = Color(0xFF381E72)

    // Base Hair Color based on companion
    val hairColor = remember(companionName) {
        when (companionName) {
            "Aria" -> Color(0xFF00ACC1) // Teal
            "Ken" -> Color(0xFFE65100) // Vibrant orange
            else -> Color(0xFFF06292) // Pastel pink for Koko
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(32.dp))
            .background(cardBg)
            .border(1.dp, borderCol, RoundedCornerShape(32.dp))
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Mood Badge: absolute top-3 left-3
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "MOOD: ${mood.uppercase()}",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = textCol
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(top = 16.dp)
        ) {
            // Character drawing
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .offset { androidx.compose.ui.unit.IntOffset(0, floatOffset.dp.roundToPx()) },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val scale = size.width / 150f

                    // Draw Neck
                    val neckPath = Path().apply {
                        moveTo(center.x - 15f * scale, center.y + 20f * scale)
                        lineTo(center.x + 15f * scale, center.y + 20f * scale)
                        lineTo(center.x + 10f * scale, center.y + 45f * scale)
                        lineTo(center.x - 10f * scale, center.y + 45f * scale)
                        close()
                    }
                    drawPath(neckPath, Color(0xFFFCE4EC)) // Skin-ish neck

                    // Draw Shirt
                    val shirtPath = Path().apply {
                        moveTo(center.x - 35f * scale, center.y + 42f * scale)
                        lineTo(center.x + 35f * scale, center.y + 42f * scale)
                        lineTo(center.x + 50f * scale, center.y + 75f * scale)
                        lineTo(center.x - 50f * scale, center.y + 75f * scale)
                        close()
                    }
                    drawPath(shirtPath, Color(0xFF37474F)) // Deep grey/navy suit

                    // Draw Head / Face Circle
                    drawCircle(
                        color = Color(0xFFFFF3E0), // Clean anime skin color
                        radius = 35f * scale,
                        center = center
                    )

                    // Blush on Cheeks (Happy, Creative, Stressed)
                    if (mood == "Happy" || mood == "Creative" || mood == "Stressed") {
                        drawCircle(
                            color = Color(0xFFFF8A80).copy(alpha = 0.6f),
                            radius = 6f * scale,
                            center = Offset(center.x - 18f * scale, center.y + 10f * scale)
                        )
                        drawCircle(
                            color = Color(0xFFFF8A80).copy(alpha = 0.6f),
                            radius = 6f * scale,
                            center = Offset(center.x + 18f * scale, center.y + 10f * scale)
                        )
                    }

                    // Eye Drawing
                    val leftEyeCenter = Offset(center.x - 14f * scale, center.y + 2f * scale)
                    val rightEyeCenter = Offset(center.x + 14f * scale, center.y + 2f * scale)

                    if (isBlinking) {
                        // Closed blinking eyes (line)
                        drawLine(Color(0xFF263238), leftEyeCenter - Offset(6f * scale, 0f), leftEyeCenter + Offset(6f * scale, 0f), strokeWidth = 3f * scale)
                        drawLine(Color(0xFF263238), rightEyeCenter - Offset(6f * scale, 0f), rightEyeCenter + Offset(6f * scale, 0f), strokeWidth = 3f * scale)
                    } else {
                        when (mood) {
                            "Focused" -> {
                                // High determination eyes
                                drawCircle(Color(0xFF263238), radius = 5f * scale, center = leftEyeCenter)
                                drawCircle(Color(0xFF263238), radius = 5f * scale, center = rightEyeCenter)
                                // Adorable tiny glasses
                                drawCircle(Color(0xFFE0F7FA), radius = 10f * scale, center = leftEyeCenter, style = Stroke(width = 2f * scale))
                                drawCircle(Color(0xFFE0F7FA), radius = 10f * scale, center = rightEyeCenter, style = Stroke(width = 2f * scale))
                                drawLine(Color(0xFFE0F7FA), leftEyeCenter + Offset(10f * scale, 0f), rightEyeCenter - Offset(10f * scale, 0f), strokeWidth = 2f * scale)
                            }
                            "Happy" -> {
                                // Smiling closed eyes (^^)
                                val eyePathL = Path().apply {
                                    moveTo(leftEyeCenter.x - 6f * scale, leftEyeCenter.y + 2f * scale)
                                    quadraticTo(leftEyeCenter.x, leftEyeCenter.y - 4f * scale, leftEyeCenter.x + 6f * scale, leftEyeCenter.y + 2f * scale)
                                }
                                val eyePathR = Path().apply {
                                    moveTo(rightEyeCenter.x - 6f * scale, rightEyeCenter.y + 2f * scale)
                                    quadraticTo(rightEyeCenter.x, rightEyeCenter.y - 4f * scale, rightEyeCenter.x + 6f * scale, rightEyeCenter.y + 2f * scale)
                                }
                                drawPath(eyePathL, Color(0xFF263238), style = Stroke(width = 3f * scale))
                                drawPath(eyePathR, Color(0xFF263238), style = Stroke(width = 3f * scale))
                            }
                            "Tired" -> {
                                // Sleepy droopy eyes (half-closed)
                                drawArc(
                                    color = Color(0xFF263238),
                                    startAngle = 0f,
                                    sweepAngle = 180f,
                                    useCenter = false,
                                    topLeft = Offset(leftEyeCenter.x - 6f * scale, leftEyeCenter.y - 2f * scale),
                                    size = Size(12f * scale, 8f * scale),
                                    style = Stroke(width = 2.5f * scale)
                                )
                                drawArc(
                                    color = Color(0xFF263238),
                                    startAngle = 0f,
                                    sweepAngle = 180f,
                                    useCenter = false,
                                    topLeft = Offset(rightEyeCenter.x - 6f * scale, rightEyeCenter.y - 2f * scale),
                                    size = Size(12f * scale, 8f * scale),
                                    style = Stroke(width = 2.5f * scale)
                                )
                            }
                            "Creative" -> {
                                // Sparkly creative star eyes or glowing dots
                                drawCircle(Color(0xFFFFD54F), radius = 6f * scale, center = leftEyeCenter)
                                drawCircle(Color(0xFFFFD54F), radius = 6f * scale, center = rightEyeCenter)
                                drawCircle(Color(0xFFFFFFFF), radius = 2f * scale, center = leftEyeCenter - Offset(2f, 2f))
                                drawCircle(Color(0xFFFFFFFF), radius = 2f * scale, center = rightEyeCenter - Offset(2f, 2f))
                            }
                            "Stressed" -> {
                                // Worried wavy eyes
                                val eyePathL = Path().apply {
                                    moveTo(leftEyeCenter.x - 5f * scale, leftEyeCenter.y - 1f * scale)
                                    lineTo(leftEyeCenter.x + 5f * scale, leftEyeCenter.y + 1f * scale)
                                }
                                val eyePathR = Path().apply {
                                    moveTo(rightEyeCenter.x - 5f * scale, rightEyeCenter.y + 1f * scale)
                                    lineTo(rightEyeCenter.x + 5f * scale, rightEyeCenter.y - 1f * scale)
                                }
                                drawPath(eyePathL, Color(0xFF263238), style = Stroke(width = 3f * scale))
                                drawPath(eyePathR, Color(0xFF263238), style = Stroke(width = 3f * scale))
                            }
                            else -> {
                                // Normal eyes
                                drawCircle(Color(0xFF263238), radius = 4f * scale, center = leftEyeCenter)
                                drawCircle(Color(0xFF263238), radius = 4f * scale, center = rightEyeCenter)
                            }
                        }
                    }

                    // Mouth Drawing
                    val mouthCenter = Offset(center.x, center.y + 16f * scale)
                    when (mood) {
                        "Happy" -> {
                            // Wide happy smile
                            drawArc(
                                color = Color(0xFFD81B60),
                                startAngle = 0f,
                                sweepAngle = 180f,
                                useCenter = true,
                                topLeft = Offset(mouthCenter.x - 6f * scale, mouthCenter.y - 3f * scale),
                                size = Size(12f * scale, 8f * scale)
                            )
                        }
                        "Tired" -> {
                            // Tiny yawn circle
                            drawCircle(Color(0xFFD81B60), radius = 3f * scale, center = mouthCenter)
                        }
                        "Stressed" -> {
                            // Worried wavy mouth
                            val mouthPath = Path().apply {
                                moveTo(mouthCenter.x - 6f * scale, mouthCenter.y)
                                quadraticTo(mouthCenter.x - 3f * scale, mouthCenter.y - 2f * scale, mouthCenter.x, mouthCenter.y)
                                quadraticTo(mouthCenter.x + 3f * scale, mouthCenter.y + 2f * scale, mouthCenter.x + 6f * scale, mouthCenter.y)
                            }
                            drawPath(mouthPath, Color(0xFF263238), style = Stroke(width = 2f * scale))
                        }
                        "Focused" -> {
                            // Flat determined line mouth
                            drawLine(Color(0xFF263238), mouthCenter - Offset(4f * scale, 0f), mouthCenter + Offset(4f * scale, 0f), strokeWidth = 2f * scale)
                        }
                        else -> {
                            // Tiny simple smile
                            val mouthPath = Path().apply {
                                moveTo(mouthCenter.x - 4f * scale, mouthCenter.y)
                                quadraticTo(mouthCenter.x, mouthCenter.y + 3f * scale, mouthCenter.x + 4f * scale, mouthCenter.y)
                            }
                            drawPath(mouthPath, Color(0xFF263238), style = Stroke(width = 2f * scale))
                        }
                    }

                    // Draw Hair (Custom Anime bangs and sides depending on selected character)
                    val hairPath = Path()
                    if (companionName == "Ken") {
                        // Spiky energetic hair
                        hairPath.apply {
                            moveTo(center.x - 42f * scale, center.y - 10f * scale)
                            lineTo(center.x - 35f * scale, center.y - 35f * scale)
                            lineTo(center.x - 20f * scale, center.y - 25f * scale)
                            lineTo(center.x, center.y - 45f * scale) // Top spike
                            lineTo(center.x + 20f * scale, center.y - 25f * scale)
                            lineTo(center.x + 35f * scale, center.y - 35f * scale)
                            lineTo(center.x + 42f * scale, center.y - 10f * scale)
                            // Bangs overlapping face
                            lineTo(center.x + 15f * scale, center.y - 15f * scale)
                            lineTo(center.x + 5f * scale, center.y - 22f * scale)
                            lineTo(center.x - 8f * scale, center.y - 15f * scale)
                            close()
                        }
                    } else if (companionName == "Aria") {
                        // Smart long straight bangs with hair clips
                        hairPath.apply {
                            moveTo(center.x - 38f * scale, center.y + 10f * scale)
                            lineTo(center.x - 38f * scale, center.y - 35f * scale)
                            quadraticTo(center.x, center.y - 45f * scale, center.x + 38f * scale, center.y - 35f * scale)
                            lineTo(center.x + 38f * scale, center.y + 10f * scale)
                            // Bangs falling between eyes
                            lineTo(center.x + 10f * scale, center.y - 12f * scale)
                            lineTo(center.x, center.y - 18f * scale)
                            lineTo(center.x - 10f * scale, center.y - 12f * scale)
                            close()
                        }
                    } else {
                        // Koko (cozy puffy hair)
                        hairPath.apply {
                            moveTo(center.x - 40f * scale, center.y)
                            quadraticTo(center.x - 38f * scale, center.y - 42f * scale, center.x, center.y - 42f * scale)
                            quadraticTo(center.x + 38f * scale, center.y - 42f * scale, center.x + 40f * scale, center.y)
                            // Soft cute curly bangs
                            lineTo(center.x + 20f * scale, center.y - 15f * scale)
                            quadraticTo(center.x + 10f * scale, center.y - 22f * scale, center.x, center.y - 16f * scale)
                            quadraticTo(center.x - 10f * scale, center.y - 22f * scale, center.x - 20f * scale, center.y - 15f * scale)
                            close()
                        }
                    }
                    drawPath(hairPath, hairColor)

                    // Optional hair accessory or hair highlights
                    if (companionName == "Aria") {
                        // Little golden hair clip
                        drawRect(
                            color = Color(0xFFFFD54F),
                            topLeft = Offset(center.x - 25f * scale, center.y - 25f * scale),
                            size = Size(8f * scale, 3f * scale)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Yuki • 09:41 AM
            val formattedTime = remember(timeOfDay) {
                when(timeOfDay) {
                    "Morning" -> "09:41 AM"
                    "Afternoon" -> "02:15 PM"
                    "Evening" -> "06:30 PM"
                    else -> "11:15 PM"
                }
            }
            Text(
                text = "$companionName • $formattedTime",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = textCol,
                letterSpacing = 0.5.sp
            )
        }
    }
}

private fun companionTitle(companion: String): String {
    return when (companion) {
        "Aria" -> "The Sage Scholar"
        "Ken" -> "The Kinetic Dynamo"
        else -> "The Cozy Comfort"
    }
}

private fun getCompanionDialogue(companionName: String, mood: String, userRole: String): String {
    val isStudent = userRole == "Student"
    return when (companionName) {
        "Aria" -> when (mood) {
            "Focused" -> if (isStudent) "My records indicate optimal cognitive performance. Perfect time for complex studies." else "Schedule streamlined. Let us execute high-priority operations with precision."
            "Happy" -> "Positive mental reinforcement detected. Joy is an outstanding catalyst for workflow efficiency!"
            "Tired" -> "System resources running low. I advise scheduling a 15-minute micro-nap to restore performance."
            "Creative" -> "Fascinating connections forming. Let us design something truly non-linear today."
            else -> "Deep abdominal breathing activates the parasympathetic system. We will organize this chaos together."
        }
        "Ken" -> when (mood) {
            "Focused" -> "BOOM! Productivity level over 9000! Let's crush this schedule, partner!"
            "Happy" -> "THAT IS WHAT I'M TALKING ABOUT! Tasks cleared! High five!"
            "Tired" -> "Tired? NO PROBLEM! 10 jumping jacks or a quick coffee sprint to reactivate!"
            "Creative" -> "Let's build something completely awesome! Sky is the limit today!"
            else -> "Stress is just potential energy waiting to explode! Let's attack one single task right now!"
        }
        else -> when (mood) { // Koko
            "Focused" -> "We're doing so well, step by step... no rush, just steady cozy progress."
            "Happy" -> "Yay! I'm so proud of you. Let's write down your wins and have a nice treat."
            "Tired" -> "A sleepy day is still a good day. Let's do the easy little tasks and take a soft rest."
            "Creative" -> "What a beautiful mind you have. Let's color our day with lovely ideas."
            else -> "Don't worry about finishing everything. You are doing great just being here. Let's choose one tiny task."
        }
    }
}
