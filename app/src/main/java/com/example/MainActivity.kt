package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Task
import com.example.ui.components.AnimeCompanion
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_activity_root")
                ) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = viewModel()
) {
    val tasks by viewModel.tasks.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val userMood by viewModel.userMood.collectAsState()
    val timeOfDay by viewModel.timeOfDay.collectAsState()
    val selectedCompanion by viewModel.selectedCompanion.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val isOptimizing by viewModel.isOptimizing.collectAsState()
    val optimizationError by viewModel.optimizationError.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showSubscriptionDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val activeTasks = remember(tasks) { tasks.filter { !it.isCompleted } }
    val completedTasks = remember(tasks) { tasks.filter { it.isCompleted } }

    // Focus Score calculation
    val focusScore = remember(tasks) {
        if (tasks.isEmpty()) 100 else {
            val ratio = (completedTasks.size.toFloat() / tasks.size.toFloat()) * 100
            ratio.toInt()
        }
    }

    // Top priority task
    val topTask = remember(activeTasks) { activeTasks.firstOrNull() }
    val nextTask = remember(activeTasks) { activeTasks.getOrNull(1) }

    // Caching filtered task list outside of composition loop to save CPU cycles
    val filteredTasks = remember(tasks, selectedCategoryFilter) {
        tasks.filter {
            selectedCategoryFilter == "All" || it.category == selectedCategoryFilter
        }
    }

    // Colors
    val premiumPrimary = Color(0xFFD4AF37)
    val brandPurple = Color(0xFF6750A4)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAF9FD))
    ) {
        // --- BENTO HEADER AREA ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AI SCHEDULER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = brandPurple,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "AuraTask",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F)
                )
            }

            // Sync/Premium Avatar Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isPremium) premiumPrimary.copy(alpha = 0.2f) else brandPurple.copy(alpha = 0.1f))
                    .border(
                        1.5.dp,
                        if (isPremium) premiumPrimary else brandPurple.copy(alpha = 0.3f),
                        CircleShape
                    )
                    .clickable { showSubscriptionDialog = true }
                    .testTag("premium_profile_avatar"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPremium) Icons.Filled.Stars else Icons.Filled.Sync,
                    contentDescription = "Subscription status",
                    tint = if (isPremium) premiumPrimary else brandPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // --- SCROLLABLE BENTO GRID ---
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ROW 1: Focus Card + Companion Card
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1.6f)
                            .height(250.dp)
                            .testTag("bento_focus_card"),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF7FF)),
                        border = BorderStroke(1.dp, Color(0xFFE6E1E5))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = brandPurple.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Priority 01",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = brandPurple
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color(0xFF4CAF50), CircleShape)
                                    )
                                    Text(
                                        text = "Privacy Local",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF49454F)
                                    )
                                }
                            }

                            if (topTask != null) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = topTask.title,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1C1B1F),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (topTask.aiReasoning.isNotBlank()) topTask.aiReasoning else "AI suggests doing this first to build early day momentum.",
                                        fontSize = 12.sp,
                                        color = Color(0xFF49454F).copy(alpha = 0.8f),
                                        style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                        modifier = Modifier.padding(top = 4.dp),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Button(
                                    onClick = { viewModel.toggleTaskCompletion(topTask) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("complete_top_task_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = brandPurple),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("Complete Task", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "No Tasks",
                                        tint = brandPurple,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "All Caught Up!",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1C1B1F)
                                    )
                                    Text(
                                        text = "Add tasks to plan your day.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF49454F)
                                    )
                                }

                                Button(
                                    onClick = { showAddTaskDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = brandPurple),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("Add Task", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1.0f)
                            .height(250.dp)
                            .testTag("bento_companion_card"),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF)),
                        border = BorderStroke(1.dp, Color(0xFFD0BCFF))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.4f), CircleShape)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "MOOD: $userMood",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF381E72)
                                    )
                                }

                                AnimeCompanion(
                                    companionName = selectedCompanion,
                                    mood = userMood,
                                    timeOfDay = timeOfDay,
                                    userRole = userRole,
                                    modifier = Modifier.weight(1f)
                                )

                                Text(
                                    text = "$selectedCompanion • $timeOfDay",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF381E72),
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            // ROW 2: Focus Score + AI Cloud Sync
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp)
                            .testTag("bento_focus_score_card"),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFCAC4D0))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "FOCUS SCORE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF49454F),
                                letterSpacing = 1.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = "$focusScore%",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Light,
                                    color = Color(0xFF1C1B1F)
                                )
                                Text(
                                    text = if (focusScore > 75) "+5%" else "Steady",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (focusScore > 75) Color(0xFF2E7D32) else Color(0xFFD84315)
                                )
                            }

                            LinearProgressIndicator(
                                progress = { focusScore.toFloat() / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = brandPurple,
                                trackColor = Color(0xFFE6E1E5)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp)
                            .testTag("bento_sync_card")
                            .clickable { showSubscriptionDialog = true },
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8DEF8)),
                        border = BorderStroke(1.dp, Color(0xFFD0BCFF))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AI CLOUD SYNC",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF381E72),
                                    letterSpacing = 1.sp
                                )

                                Icon(
                                    imageVector = if (isPremium) Icons.Filled.CloudDone else Icons.Filled.CloudOff,
                                    contentDescription = "Sync",
                                    tint = if (isPremium) brandPurple else Color(0xFF49454F),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = if (isPremium) "Aura Pro Active" else "Offline-First Tier",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1C1B1F)
                            )

                            Text(
                                text = if (isPremium) "Device sync connected" else "Backup (Tap to upgrade)",
                                fontSize = 9.sp,
                                color = Color(0xFF49454F)
                            )
                        }
                    }
                }
            }

            // ROW 3: Timeline Strip
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .testTag("bento_timeline_strip"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFCAC4D0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(brandPurple, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (nextTask != null) "Up Next: ${nextTask.title}" else "No upcoming tasks today",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1C1B1F),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = if (nextTask != null) nextTask.deadline else "--:--",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF49454F)
                        )
                    }
                }
            }

            // ROW 4: AI Optimization Controllers
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                    border = BorderStroke(1.dp, Color(0xFFE6E1E5))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = "Engine settings",
                                    tint = brandPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "AI Optimization Controls",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1C1B1F)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Role: ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF49454F)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Row(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(2.dp)
                                ) {
                                    val isPro = userRole == "Professional"
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isPro) brandPurple else Color.Transparent,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable { viewModel.setUserRole("Professional") }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .testTag("role_pro_button")
                                    ) {
                                        Text(
                                            "Pro",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPro) Color.White else Color(0xFF49454F)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (!isPro) brandPurple else Color.Transparent,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable { viewModel.setUserRole("Student") }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .testTag("role_student_button")
                                    ) {
                                        Text(
                                            "Student",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (!isPro) Color.White else Color(0xFF49454F)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Active Character Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF49454F))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Aria", "Ken", "Koko").forEach { companion ->
                                FilterChip(
                                    selected = selectedCompanion == companion,
                                    onClick = { viewModel.setSelectedCompanion(companion) },
                                    label = { Text(companion, fontSize = 11.sp) },
                                    modifier = Modifier.testTag("companion_$companion")
                                )
                            }
                        }

                        Text("Current Workflow Energy / Mood", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF49454F))
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Focused", "Happy", "Tired", "Creative", "Stressed").forEach { mood ->
                                FilterChip(
                                    selected = userMood == mood,
                                    onClick = { viewModel.setUserMood(mood) },
                                    label = { Text(mood, fontSize = 10.sp) },
                                    modifier = Modifier.testTag("mood_$mood")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.runAiPrioritization() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("prioritize_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = brandPurple),
                            enabled = !isOptimizing && activeTasks.isNotEmpty()
                        ) {
                            if (isOptimizing) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Aura Engine is Sorting Tasks...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = "Optimize", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Prioritize Optimal Daily Schedule", fontWeight = FontWeight.Bold)
                            }
                        }

                        optimizationError?.let { err ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = err,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ROW 5: Daily Schedule Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Suggested Daily Schedule",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("All", "Work", "Study").forEach { cat ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (selectedCategoryFilter == cat) brandPurple else Color.White,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(8.dp))
                                    .clickable { selectedCategoryFilter = cat }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedCategoryFilter == cat) Color.White else Color(0xFF49454F)
                                )
                            }
                        }
                    }
                }
            }

            // ROW 6: Task items mapping
            // Using cached filteredTasks list from MainScreen to prevent recomposition filtering

            if (filteredTasks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFCAC4D0))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlaylistAdd,
                                contentDescription = "No Tasks",
                                tint = brandPurple.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your Schedule is Clear!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1C1B1F)
                            )
                            Text(
                                text = "Tap 'Add Daily Task' to create a customized list.",
                                fontSize = 11.sp,
                                color = Color(0xFF49454F)
                            )
                        }
                    }
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    BentoTaskCard(
                        task = task,
                        viewModel = viewModel,
                        onCompleteToggle = { viewModel.toggleTaskCompletion(task) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }

        // --- FOOTER ACTION BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, Color(0xFFCAC4D0).copy(alpha = 0.3f))
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { viewModel.clearAllTasks() },
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFBA1A1A))
            ) {
                Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear All", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear All")
            }

            Button(
                onClick = { showAddTaskDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = brandPurple),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_task_bottom_btn")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Daily Task", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Modal Add Task Form Dialog
    if (showAddTaskDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Work") }
        var duration by remember { mutableStateOf("30") }
        var deadline by remember { mutableStateOf("12:00 PM") }
        var importance by remember { mutableStateOf("Medium") }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Plan New Daily Task", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_task_title_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Short Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("Time (mins)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = deadline,
                            onValueChange = { deadline = it },
                            label = { Text("Deadline") },
                            modifier = Modifier.weight(1.2f)
                        )
                    }

                    Column {
                        Text("Task Importance Rating", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF49454F))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("High", "Medium", "Low").forEach { imp ->
                                FilterChip(
                                    selected = importance == imp,
                                    onClick = { importance = imp },
                                    label = { Text(imp, fontSize = 11.sp) },
                                    modifier = Modifier.testTag("importance_$imp")
                                )
                            }
                        }
                    }

                    Column {
                        Text("Workflow Category", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF49454F))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Work", "Study", "Personal", "Health").forEach { cat ->
                                FilterChip(
                                    selected = category == cat,
                                    onClick = { category = cat },
                                    label = { Text(cat, fontSize = 11.sp) },
                                    modifier = Modifier.testTag("category_$cat")
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.addTask(
                                title = title,
                                description = description,
                                category = category,
                                durationMinutes = duration.toIntOrNull() ?: 30,
                                deadline = deadline,
                                importance = importance
                            )
                            showAddTaskDialog = false
                        }
                    },
                    modifier = Modifier.testTag("dialog_submit_button"),
                    enabled = title.isNotBlank()
                ) {
                    Text("Add Schedule Slot")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSubscriptionDialog) {
        AlertDialog(
            onDismissRequest = { showSubscriptionDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Stars, contentDescription = "Premium", tint = premiumPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Premium Device Sync", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "AuraTask is free for local storage and core AI scheduling! Unlock cloud backup and multi-device sync with Aura Premium.",
                        fontSize = 13.sp,
                        color = Color(0xFF49454F)
                    )
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    listOf(
                        "🔄 Continuous Cloud Sync" to "Backup across Android devices instantly.",
                        "🚫 100% Ad-free Forever" to "Minimal clean UI with zero commercials.",
                        "✨ Custom High-Contrast Accent" to "Unlock beautiful layout customization."
                    ).forEach { (feat, desc) ->
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Filled.Check, contentDescription = "Check", tint = brandPurple, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(feat, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(desc, fontSize = 10.sp, color = Color(0xFF49454F))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.togglePremium()
                        showSubscriptionDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = brandPurple),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isPremium) "Switch to Basic (Free)" else "Enable Aura Pro - $4.99/mo")
                }
            }
        )
    }
}

@Composable
fun BentoTaskCard(
    task: Task,
    viewModel: TaskViewModel,
    onCompleteToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val brandPurple = Color(0xFF6750A4)
    var showFeedbackDialog by remember { mutableStateOf(false) }

    val categoryColor = when (task.category) {
        "Work" -> Color(0xFF1E88E5)
        "Study" -> Color(0xFF8E24AA)
        "Personal" -> Color(0xFF00897B)
        else -> Color(0xFFF4511E)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bento_task_card_${task.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) Color(0xFFFEF7FF).copy(alpha = 0.5f) else Color.White
        ),
        border = BorderStroke(
            1.dp,
            if (task.isCompleted) Color(0xFFCAC4D0).copy(alpha = 0.4f) else categoryColor.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { onCompleteToggle() },
                        modifier = Modifier.testTag("checkbox_${task.id}")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = task.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                            color = if (task.isCompleted) Color(0xFF49454F).copy(alpha = 0.5f) else Color(0xFF1C1B1F)
                        )
                        if (task.description.isNotBlank()) {
                            Text(
                                text = task.description,
                                fontSize = 12.sp,
                                color = Color(0xFF49454F),
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                                modifier = Modifier.padding(top = 1.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(categoryColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = task.category,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp).testTag("delete_${task.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFBA1A1A), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.AccessTime, contentDescription = "Duration", tint = Color(0xFF49454F), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("${task.durationMinutes}m", fontSize = 11.sp, color = Color(0xFF49454F))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = "Deadline", tint = Color(0xFF49454F), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(task.deadline, fontSize = 11.sp, color = Color(0xFF49454F))
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEADDFF), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "Importance: ${task.userAssignedImportance}",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF381E72)
                        )
                    }
                }

                if (!task.isCompleted) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFF3EDF7), RoundedCornerShape(12.dp))
                            .padding(2.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.adjustTaskPriority(task, bumpUp = false) },
                            modifier = Modifier.size(24.dp).testTag("bump_down_${task.id}")
                        ) {
                            Icon(Icons.Filled.ArrowDownward, contentDescription = "Decrease", tint = Color(0xFF49454F), modifier = Modifier.size(14.dp))
                        }

                        Text(
                            text = "${task.priorityScore}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = brandPurple,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        IconButton(
                            onClick = { viewModel.adjustTaskPriority(task, bumpUp = true) },
                            modifier = Modifier.size(24.dp).testTag("bump_up_${task.id}")
                        ) {
                            Icon(Icons.Filled.ArrowUpward, contentDescription = "Increase", tint = Color(0xFF49454F), modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            if (task.aiReasoning.isNotBlank() && !task.isCompleted) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF).copy(alpha = 0.3f)),
                    border = BorderStroke(0.5.dp, Color(0xFFD0BCFF).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = "Advice", tint = brandPurple, modifier = Modifier.size(14.dp).padding(top = 1.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = task.aiReasoning,
                            fontSize = 11.sp,
                            color = Color(0xFF381E72),
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            if (!task.isCompleted) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Rate Priority: ",
                            fontSize = 10.sp,
                            color = Color(0xFF49454F)
                        )
                        (1..5).forEach { star ->
                            val active = star <= task.feedbackRating
                            Icon(
                                imageVector = if (active) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "Star $star",
                                tint = if (active) Color(0xFFFFD54F) else Color(0xFFCAC4D0),
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        viewModel.submitTaskFeedback(task, star, task.feedbackText)
                                        if (star <= 2) {
                                            showFeedbackDialog = true
                                        }
                                    }
                            )
                        }
                    }

                    if (task.feedbackRating > 0) {
                        Text(
                            text = if (task.feedbackText.isNotBlank()) "Feedback: Received" else "Tuned!",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = brandPurple
                        )
                    } else {
                        Text(
                            text = "Aura Feedback Loop",
                            fontSize = 9.sp,
                            style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                            color = Color(0xFF49454F).copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }

    if (showFeedbackDialog) {
        var feedbackComment by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            title = { Text("Correct AI Scheduling", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "You rated this task's priority as low. Help AuraTask learn! What was wrong with its placement in the schedule?",
                        fontSize = 12.sp,
                        color = Color(0xFF49454F),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = feedbackComment,
                        onValueChange = { feedbackComment = it },
                        label = { Text("How should we schedule this?") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitTaskFeedback(task, task.feedbackRating, feedbackComment)
                        showFeedbackDialog = false
                    }
                ) {
                    Text("Submit Feedback Loop")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFeedbackDialog = false }) {
                    Text("Skip")
                }
            }
        )
    }
}
