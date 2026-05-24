package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.HomeworkTask
import com.example.data.model.StudySlot
import com.example.data.repository.StudyBuddyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StudyBuddyViewModel(
    application: Application,
    private val repository: StudyBuddyRepository
) : AndroidViewModel(application) {

    // Lists observed reactively from Room Flows
    val homeworkTasks: StateFlow<List<HomeworkTask>> = repository.allHomeworkTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studySlots: StateFlow<List<StudySlot>> = repository.allStudySlots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI States
    private val _selectedTutor = MutableStateFlow("Math Mentor")
    val selectedTutor: StateFlow<String> = _selectedTutor.asStateFlow()

    // Observe active tutor's chat history persistently
    val tutorMessages: StateFlow<List<ChatMessage>> = _selectedTutor
        .flatMapLatest { tutor -> repository.getMessagesForTutor(tutor) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Loading & Action states
    private val _isGeneratingPlanMap = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val isGeneratingPlanMap: StateFlow<Map<Int, Boolean>> = _isGeneratingPlanMap.asStateFlow()

    private val _isAutoScheduling = MutableStateFlow(false)
    val isAutoScheduling: StateFlow<Boolean> = _isAutoScheduling.asStateFlow()

    private val _isSendingMessage = MutableStateFlow(false)
    val isSendingMessage: StateFlow<Boolean> = _isSendingMessage.asStateFlow()

    // Actions: Homework
    fun addHomework(subject: String, title: String, description: String, dueDate: String, priority: String) {
        viewModelScope.launch {
            val task = HomeworkTask(
                subject = subject,
                title = title,
                description = description,
                dueDate = dueDate,
                priority = priority
            )
            repository.insertHomework(task)
        }
    }

    fun toggleHomeworkCompleted(task: HomeworkTask) {
        viewModelScope.launch {
            repository.updateHomework(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteHomework(id: Int) {
        viewModelScope.launch {
            repository.deleteHomeworkById(id)
        }
    }

    fun generateStudyPlanForHomework(task: HomeworkTask) {
        viewModelScope.launch {
            _isGeneratingPlanMap.update { it + (task.id to true) }
            try {
                val prompt = """
                    You are an expert academic tutor. Break down this specific homework assignment into 2 structured study slots or milestones.
                    
                    Subject: ${task.subject}
                    Assignment Title: ${task.title}
                    Description: ${task.description}
                    Due Date: ${task.dueDate}
                    Today's Date: May 24, 2026 (Please coordinate dates around this).
                    
                    Respond STRICTLY with a JSON array containing exactly 2 study sessions. Do NOT contain markdown code blocks (such as ```json) or chat padding.
                    Each object in the array MUST strictly conform to this schema:
                    [
                      {
                        "title": "Clear study milestone title",
                        "date": "MMM dd, yyyy",
                        "timeRange": "HH:MM - HH:MM",
                        "focusItems": "Detailed bullet points of exactly what to master or draft."
                      }
                    ]
                """.trimIndent()

                val apiResponse = repository.generateAIResponse(
                    prompt = prompt,
                    systemInstruction = "You are an AI Study Schedule Coordinator. Output only raw JSON.",
                    isJsonResponse = true
                )

                // Clean the JSON response of any accidental markdown formatting
                val baseJson = apiResponse
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()

                val slots = repository.parseStudySlots(baseJson, task.subject)
                if (slots.isNotEmpty()) {
                    // Update task with plan summary and insert schedule slots
                    val planContent = slots.joinToString(separator = "\n\n") { slot ->
                        "📌 **${slot.title}** (${slot.date}, ${slot.timeRange}):\n${slot.focusItems}"
                    }
                    repository.updateHomework(task.copy(aiStudyPlan = planContent))
                    repository.insertStudySlots(slots)
                } else {
                    repository.updateHomework(task.copy(aiStudyPlan = "AI could not parse the slots, please retry or create a manual study schedule slot."))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                repository.updateHomework(task.copy(aiStudyPlan = "Error generating plan: ${e.localizedMessage}"))
            } finally {
                _isGeneratingPlanMap.update { it - task.id }
            }
        }
    }

    // Actions: Study Planner Slots
    fun addCustomStudySlot(subject: String, title: String, date: String, timeRange: String, focusItems: String) {
        viewModelScope.launch {
            val slot = StudySlot(
                subject = subject,
                title = title,
                date = date,
                timeRange = timeRange,
                focusItems = focusItems
            )
            repository.insertStudySlot(slot)
        }
    }

    fun toggleStudySlotCompleted(slot: StudySlot) {
        viewModelScope.launch {
            repository.updateStudySlot(slot.copy(isCompleted = !slot.isCompleted))
        }
    }

    fun deleteStudySlot(id: Int) {
        viewModelScope.launch {
            repository.deleteStudySlotById(id)
        }
    }

    fun clearAllStudySlots() {
        viewModelScope.launch {
            repository.clearAllStudySlots()
        }
    }

    fun autoGenerateWeeklySchedule() {
        viewModelScope.launch {
            val tasks = homeworkTasks.value.filter { !it.isCompleted }
            if (tasks.isEmpty()) return@launch

            _isAutoScheduling.value = true
            try {
                val tasksDescription = tasks.joinToString("\n") {
                    "- ${it.subject}: ${it.title} (${it.description}) - Priority: ${it.priority}, Due: ${it.dueDate}"
                }

                val prompt = """
                    You are an expert academic coordinator. Based on the following homework assignments of a student:
                    $tasksDescription
                    
                    Today's relative date is May 24, 2026.
                    Please design an optimal weekly study schedule with 3-4 realistic sessions distributed over the next week to complete these tasks.
                    
                    Respond STRICTLY with a JSON array. Do not include markdown wraps (like ```json).
                    Schema:
                    [
                      {
                        "title": "Study session study theme (e.g. Science Review, Math Problem Solving)",
                        "date": "MMM dd, yyyy",
                        "timeRange": "HH:MM - HH:MM",
                        "focusItems": "Detailed key concepts and tasks to complete."
                      }
                    ]
                """.trimIndent()

                val apiResponse = repository.generateAIResponse(
                    prompt = prompt,
                    systemInstruction = "You are an AI Personal Scheduler. Output only raw JSON.",
                    isJsonResponse = true
                )

                val baseJson = apiResponse
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()

                val slots = repository.parseStudySlots(baseJson, "Weekly Study")
                if (slots.isNotEmpty()) {
                    repository.insertStudySlots(slots)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isAutoScheduling.value = false
            }
        }
    }

    // Actions: AI Tutors
    fun selectTutor(tutor: String) {
        _selectedTutor.value = tutor
    }

    fun sendTutorMessage(text: String) {
        if (text.isBlank()) return
        val activeTutor = _selectedTutor.value

        viewModelScope.launch {
            _isSendingMessage.value = true
            // Save user message
            val userMsg = ChatMessage(tutorSubject = activeTutor, sender = "user", message = text)
            repository.insertChatMessage(userMsg)

            // Construct conversation history context
            val history = tutorMessages.value + userMsg
            val contextPrompt = history.takeLast(10).joinToString("\n") {
                "${if (it.sender == "user") "Student" else "Tutor"}: ${it.message}"
            }

            val systemInstr = when (activeTutor) {
                "Math Mentor" -> "You are a warm, extremely clear Math Mentor. Help the student with mathematics, calculus, algebra, or geometry. Explain steps clearly, formatting mathematical formulas elegantly. Encourage asking followups."
                "Science Sage" -> "You are Biology/Chemistry/Physics Mentor. You use great intuitive everyday analogies and visual explanations. Encourage curiosity and explain scientific processes simply."
                "History Guide" -> "You are a Storytelling History Tutor. Bring social studies, geography, and world/local histories to life with storytelling narratives, fun historical anecdotes, and structural summaries."
                "English Coach" -> "You are English Literature, grammar, and Essay Tutor. Guide the student in structuring writing, mastering comprehension guides, grammar check feedback, and reviewing essays with critical love."
                else -> "You are an academic coach, help the student with homework, study advice and organization."
            }

            val reply = repository.generateAIResponse(
                prompt = contextPrompt,
                systemInstruction = systemInstr,
                isJsonResponse = false
            )

            val aiMsg = ChatMessage(tutorSubject = activeTutor, sender = "ai", message = reply)
            repository.insertChatMessage(aiMsg)
            _isSendingMessage.value = false
        }
    }

    fun clearActiveTutorHistory() {
        viewModelScope.launch {
            repository.clearHistoryForTutor(_selectedTutor.value)
        }
    }

    // Factory
    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = AppDatabase.getDatabase(application)
                val repo = StudyBuddyRepository(db)
                return StudyBuddyViewModel(application, repo) as T
            }
        }
    }
}
