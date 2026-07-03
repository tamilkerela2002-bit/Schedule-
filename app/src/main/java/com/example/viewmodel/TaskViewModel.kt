package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiScheduler
import com.example.data.AppDatabase
import com.example.data.Task
import com.example.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val database: AppDatabase by lazy {
        androidx.room.Room.databaseBuilder(
            application.applicationContext,
            AppDatabase::class.java,
            "auratask_database"
        ).fallbackToDestructiveMigration().build()
    }

    private val repository: TaskRepository by lazy {
        TaskRepository(database.taskDao())
    }

    val tasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // User configurations
    private val _userRole = MutableStateFlow("Professional") // "Professional" or "Student"
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _userMood = MutableStateFlow("Focused") // "Focused", "Happy", "Tired", "Creative", "Stressed"
    val userMood: StateFlow<String> = _userMood.asStateFlow()

    private val _timeOfDay = MutableStateFlow("Morning") // "Morning", "Afternoon", "Evening", "Night"
    val timeOfDay: StateFlow<String> = _timeOfDay.asStateFlow()

    private val _selectedCompanion = MutableStateFlow("Aria") // "Aria" (The Scholar), "Ken" (The Dynamo), "Koko" (The Cozy)
    val selectedCompanion: StateFlow<String> = _selectedCompanion.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _isOptimizing = MutableStateFlow(false)
    val isOptimizing: StateFlow<Boolean> = _isOptimizing.asStateFlow()

    private val _optimizationError = MutableStateFlow<String?>(null)
    val optimizationError: StateFlow<String?> = _optimizationError.asStateFlow()

    init {
        detectTimeOfDay()
    }

    private fun detectTimeOfDay() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        _timeOfDay.value = when (hour) {
            in 5..11 -> "Morning"
            in 12..16 -> "Afternoon"
            in 17..21 -> "Evening"
            else -> "Night"
        }
    }

    fun setUserRole(role: String) {
        _userRole.value = role
    }

    fun setUserMood(mood: String) {
        _userMood.value = mood
    }

    fun setTimeOfDay(time: String) {
        _timeOfDay.value = time
    }

    fun setSelectedCompanion(companion: String) {
        _selectedCompanion.value = companion
    }

    fun togglePremium() {
        _isPremium.value = !_isPremium.value
    }

    fun addTask(title: String, description: String, category: String, durationMinutes: Int, deadline: String, importance: String) {
        viewModelScope.launch {
            val currentTasksCount = tasks.value.size
            val newTask = Task(
                title = title,
                description = description,
                category = category,
                durationMinutes = durationMinutes,
                deadline = deadline,
                userAssignedImportance = importance,
                orderIndex = currentTasksCount
            )
            repository.insert(newTask)
        }
    }

    fun adjustTaskPriority(task: Task, bumpUp: Boolean) {
        viewModelScope.launch {
            val adjustment = if (bumpUp) 1 else -1
            val newScore = (task.priorityScore + adjustment).coerceIn(1, 5)
            repository.update(
                task.copy(
                    userPriorityAdjustment = task.userPriorityAdjustment + adjustment,
                    priorityScore = newScore
                )
            )
        }
    }

    fun submitTaskFeedback(task: Task, rating: Int, text: String) {
        viewModelScope.launch {
            repository.update(
                task.copy(
                    feedbackRating = rating,
                    feedbackText = text
                )
            )
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            repository.update(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
        }
    }

    fun clearAllTasks() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun runAiPrioritization() {
        val currentTasks = tasks.value.filter { !it.isCompleted }
        if (currentTasks.isEmpty()) return

        viewModelScope.launch {
            _isOptimizing.value = true
            _optimizationError.value = null
            try {
                val results = GeminiScheduler.prioritizeTasks(
                    tasks = currentTasks,
                    userRole = _userRole.value,
                    mood = _userMood.value,
                    timeOfDay = _timeOfDay.value
                )

                if (results.isNotEmpty()) {
                    // Update the tasks with returned results
                    for (result in results) {
                        val matchingTask = currentTasks.find { it.id == result.id }
                        if (matchingTask != null) {
                            repository.update(
                                matchingTask.copy(
                                    priorityScore = result.priorityScore,
                                    orderIndex = result.orderIndex,
                                    aiReasoning = result.aiReasoning
                                )
                            )
                        }
                    }
                } else {
                    _optimizationError.value = "Unable to customize schedule. Please ensure your Gemini API Key is configured in the Secrets panel."
                }
            } catch (e: Exception) {
                _optimizationError.value = "Error: ${e.message}. Check your internet connection."
            } finally {
                _isOptimizing.value = false
            }
        }
    }
}
