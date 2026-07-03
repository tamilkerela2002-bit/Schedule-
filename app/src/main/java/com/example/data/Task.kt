package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String = "General", // Work, Study, Personal, Health
    val isCompleted: Boolean = false,
    val priorityScore: Int = 3, // 1 (Lowest) to 5 (Highest)
    val orderIndex: Int = 0, // Order in the prioritized schedule
    val aiReasoning: String = "",
    val durationMinutes: Int = 30,
    val deadline: String = "End of Day", // e.g., "14:00", "End of Day", "ASAP"
    val userAssignedImportance: String = "Medium", // "High", "Medium", "Low"
    val userPriorityAdjustment: Int = 0, // Manual bump offset: positive = up, negative = down
    val feedbackText: String = "", // user feedback text on why priority was right/wrong
    val feedbackRating: Int = 0, // 0 = no feedback, 1-5 rating on AI placement
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY orderIndex ASC, priorityScore DESC, timestamp DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()
}

@Database(entities = [Task::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}

class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun insert(task: Task) = taskDao.insertTask(task)

    suspend fun update(task: Task) = taskDao.updateTask(task)

    suspend fun delete(task: Task) = taskDao.deleteTask(task)

    suspend fun clearAll() = taskDao.clearAllTasks()
}
