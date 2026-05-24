package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ChatMessage
import com.example.ui.viewmodel.StudyBuddyViewModel
import kotlinx.coroutines.launch

data class TutorCharacter(
    val name: String,
    val role: String,
    val icon: ImageVector,
    val intro: String,
    val starColor: Color,
    val starters: List<String>
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AITutorScreen(
    viewModel: StudyBuddyViewModel,
    modifier: Modifier = Modifier
) {
    val activeTutor by viewModel.selectedTutor.collectAsState()
    val messages by viewModel.tutorMessages.collectAsState()
    val isSending by viewModel.isSendingMessage.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val tutors = listOf(
        TutorCharacter(
            name = "Math Mentor",
            role = "Algebra, Calculus & Geometry Expert",
            icon = Icons.Default.Calculate,
            intro = "Hi! I am your Math Mentor. Ask me any math question, homework problem, or let's review formulas step-by-step!",
            starColor = Color(0xFF386B1D),
            starters = listOf("Explain quadratic formula", "Help me solve: 3x + 5 = 20", "What is differentiation?")
        ),
        TutorCharacter(
            name = "Science Sage",
            role = "Physics, Chemistry & Biology Guide",
            icon = Icons.Default.Biotech,
            intro = "Greetings! I'm Science Sage. Send me any scientific concept or physical laws, and let's explore with fun analogies!",
            starColor = Color(0xFF43493E),
            starters = listOf("Explain photosynthesis", "What are Newton's Laws?", "How do covalent bonds work?")
        ),
        TutorCharacter(
            name = "History Guide",
            role = "Social Studies & Geography Storyteller",
            icon = Icons.Default.Museum,
            intro = "Welcome! I am your History Guide. Challenge me to explain world events, physical maps, or historical revolutions through stories!",
            starColor = Color(0xFF8D6E63),
            starters = listOf("Why did the Roman Empire fall?", "Tell me about the Magna Carta", "Explain tectonic plates")
        ),
        TutorCharacter(
            name = "English Coach",
            role = "Literature, Essay Structure & Grammar Guru",
            icon = Icons.Default.Spellcheck,
            intro = "Hello! I am your English Coach. Paste your paragraph or essay topic, and let's polish your arguments and review structures together!",
            starColor = Color(0xFF6E7E63),
            starters = listOf("How to write a thesis?", "Review this sentence grammar", "Explain active vs passive voice")
        )
    )

    val currentTutorObj = tutors.firstOrNull { it.name == activeTutor } ?: tutors.first()

    // Scroll to bottom on load or new message
    LaunchedEffect(messages.size, isSending) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentTutorObj.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentTutorObj.role,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.clearActiveTutorHistory() },
                        modifier = Modifier.testTag("clear_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteSweep,
                            contentDescription = "Clear History",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tutor Selector Horizontal Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tutors.forEach { tutor ->
                    val isSelected = tutor.name == activeTutor
                    AssistChip(
                        onClick = { viewModel.selectTutor(tutor.name) },
                        label = { Text(tutor.name) },
                        leadingIcon = {
                            Icon(
                                imageVector = tutor.icon,
                                contentDescription = null,
                                tint = if (isSelected) tutor.starColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = if (isSelected) {
                            AssistChipDefaults.assistChipColors(
                                containerColor = tutor.starColor.copy(alpha = 0.15f),
                                labelColor = tutor.starColor
                            )
                        } else {
                            AssistChipDefaults.assistChipColors()
                        },
                        modifier = Modifier.testTag("tutor_chip_${tutor.name.replace(" ", "_")}")
                    )
                }
            }

            // Message Stream Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (messages.isEmpty()) {
                    // Empty Chat state with Intro Cards & Suggested Starters
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(currentTutorObj.starColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = currentTutorObj.icon,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = currentTutorObj.starColor
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Meet your ${currentTutorObj.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentTutorObj.intro,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "💡 Tap a starter questions to begin:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.Center,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            currentTutorObj.starters.forEach { suggestion ->
                                Card(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .clickable { viewModel.sendTutorMessage(suggestion) }
                                        .testTag("prompt_starter_${suggestion.replace(" ", "_")}"),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    border = CardDefaults.outlinedCardBorder()
                                ) {
                                    Text(
                                        text = suggestion,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Chat Timeline LazyList
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("chat_messages_list"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            val isUser = msg.sender == "user"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                                verticalAlignment = Alignment.Top
                            ) {
                                if (!isUser) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(currentTutorObj.starColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = currentTutorObj.icon,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                Card(
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isUser) 16.dp else 4.dp,
                                        bottomEnd = if (isUser) 4.dp else 16.dp
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isUser) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    ),
                                    modifier = Modifier
                                        .widthIn(max = 280.dp)
                                        .testTag(if (isUser) "user_msg_bubble" else "ai_msg_bubble")
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = msg.message,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isUser) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }

                                if (isUser) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.secondary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Bottom-pad list item when generating
                        if (isSending) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(currentTutorObj.starColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = currentTutorObj.icon,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(12.dp),
                                                strokeWidth = 1.5.dp,
                                                color = currentTutorObj.starColor
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "${currentTutorObj.name} is drafting explanation...",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Message Composer Section
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            var inputMessageText by remember { mutableStateOf("") }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputMessageText,
                    onValueChange = { inputMessageText = it },
                    placeholder = { Text("Ask ${currentTutorObj.name}...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_text_field"),
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = currentTutorObj.starColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputMessageText.isNotBlank()) {
                            viewModel.sendTutorMessage(inputMessageText)
                            inputMessageText = ""
                        }
                    },
                    enabled = inputMessageText.isNotBlank() && !isSending,
                    modifier = Modifier
                        .testTag("chat_send_button")
                        .clip(CircleShape)
                        .background(
                            if (inputMessageText.isNotBlank() && !isSending) {
                                currentTutorObj.starColor
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Message",
                        tint = if (inputMessageText.isNotBlank() && !isSending) {
                            Color.White
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        }
                    )
                }
            }
        }
    }
}
