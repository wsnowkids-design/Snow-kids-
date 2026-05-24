package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GeminiApiClient
import com.example.data.api.GeminiRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.Part
import com.example.data.db.AppDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.HomeworkTask
import com.example.data.model.StudySlot
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class StudySlotJson(
    val title: String,
    val date: String,
    val timeRange: String,
    val focusItems: String
)

class StudyBuddyRepository(private val database: AppDatabase) {

    private val homeworkDao = database.homeworkDao()
    private val studySlotDao = database.studySlotDao()
    private val chatDao = database.chatDao()

    // Homework Methods
    val allHomeworkTasks: Flow<List<HomeworkTask>> = homeworkDao.getAllTasks()

    suspend fun insertHomework(task: HomeworkTask) = withContext(Dispatchers.IO) {
        homeworkDao.insertTask(task)
    }

    suspend fun updateHomework(task: HomeworkTask) = withContext(Dispatchers.IO) {
        homeworkDao.updateTask(task)
    }

    suspend fun deleteHomeworkById(id: Int) = withContext(Dispatchers.IO) {
        homeworkDao.deleteTaskById(id)
    }

    // Study Slots Methods
    val allStudySlots: Flow<List<StudySlot>> = studySlotDao.getAllSlots()

    suspend fun insertStudySlot(slot: StudySlot) = withContext(Dispatchers.IO) {
        studySlotDao.insertSlot(slot)
    }

    suspend fun insertStudySlots(slots: List<StudySlot>) = withContext(Dispatchers.IO) {
        studySlotDao.insertSlots(slots)
    }

    suspend fun updateStudySlot(slot: StudySlot) = withContext(Dispatchers.IO) {
        studySlotDao.updateSlot(slot)
    }

    suspend fun deleteStudySlotById(id: Int) = withContext(Dispatchers.IO) {
        studySlotDao.deleteSlotById(id)
    }

    suspend fun clearAllStudySlots() = withContext(Dispatchers.IO) {
        studySlotDao.deleteAllSlots()
    }

    // Chat History Methods
    fun getMessagesForTutor(subject: String): Flow<List<ChatMessage>> = chatDao.getMessagesForTutor(subject)

    suspend fun insertChatMessage(message: ChatMessage) = withContext(Dispatchers.IO) {
        chatDao.insertMessage(message)
    }

    suspend fun clearHistoryForTutor(subject: String) = withContext(Dispatchers.IO) {
        chatDao.deleteHistoryForTutor(subject)
    }

    // Gemini API Direct REST wrapper
    suspend fun generateAIResponse(
        prompt: String,
        systemInstruction: String? = null,
        isJsonResponse: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Error: Gemini API Key is missing. Please add your key to Gemini API Secrets under settings / secrets panel in AI Studio."
        }

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = if (isJsonResponse) {
                GenerationConfig(
                    temperature = 0.4f,
                    responseMimeType = "application/json"
                )
            } else {
                GenerationConfig(temperature = 0.7f)
            },
            systemInstruction = systemInstruction?.let {
                Content(parts = listOf(Part(text = it)))
            }
        )

        try {
            val response = GeminiApiClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No response from AI."
        } catch (e: Exception) {
            e.printStackTrace()
            "Error contacting AI: ${e.localizedMessage ?: "Unknown Error"}"
        }
    }

    // Parse list of study slots from Gemini JSON response
    fun parseStudySlots(jsonString: String, subject: String): List<StudySlot> {
        return try {
            val listType = Types.newParameterizedType(List::class.java, StudySlotJson::class.java)
            val adapter = GeminiApiClient.moshi.adapter<List<StudySlotJson>>(listType)
            val parsed = adapter.fromJson(jsonString) ?: emptyList()
            parsed.map {
                StudySlot(
                    subject = subject,
                    title = it.title,
                    date = it.date,
                    timeRange = it.timeRange,
                    focusItems = it.focusItems,
                    isCompleted = false
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
