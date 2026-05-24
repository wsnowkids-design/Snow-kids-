package com.example.data.db

import android.content.Context
import androidx.room.*
import com.example.data.model.HomeworkTask
import com.example.data.model.StudySlot
import com.example.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeworkDao {
    @Query("SELECT * FROM homework_tasks ORDER BY isCompleted ASC, id DESC")
    fun getAllTasks(): Flow<List<HomeworkTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: HomeworkTask)

    @Update
    suspend fun updateTask(task: HomeworkTask)

    @Query("DELETE FROM homework_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)
}

@Dao
interface StudySlotDao {
    @Query("SELECT * FROM study_slots ORDER BY isCompleted ASC, id DESC")
    fun getAllSlots(): Flow<List<StudySlot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: StudySlot)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlots(slots: List<StudySlot>)

    @Update
    suspend fun updateSlot(slot: StudySlot)

    @Query("DELETE FROM study_slots WHERE id = :id")
    suspend fun deleteSlotById(id: Int)

    @Query("DELETE FROM study_slots")
    suspend fun deleteAllSlots()
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE tutorSubject = :subject ORDER BY timestamp ASC")
    fun getMessagesForTutor(subject: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages WHERE tutorSubject = :subject")
    suspend fun deleteHistoryForTutor(subject: String)
}

@Database(entities = [HomeworkTask::class, StudySlot::class, ChatMessage::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun homeworkDao(): HomeworkDao
    abstract fun studySlotDao(): StudySlotDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "study_buddy_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
