package com.sathii.ai

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sathii.ai.data.NoteDao
import com.sathii.ai.data.NoteEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BgDark = Color(0xFF080B12)
private val SurfaceDark = Color(0xFF121824)
private val PrimaryCyan = Color(0xFF00E5FF)
private val AccentPurple = Color(0xFF7C4DFF)
private val StateListening = Color(0xFFFF5252)
private val StateSpeaking = Color(0xFF69F0AE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SathiUI(
    assistantState: AssistantState,
    chatMessages: List<ChatMessage>,
    onVoiceTrigger: () -> Unit,
    onExecuteText: (String) -> Unit,
    notesDao: NoteDao,
    onSaveNote: (String, String) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit
) {
    var currentTab by remember { mutableStateOf(0) }
    val notesList by notesDao.getAllNotes().collectAsState(initial = emptyList())

    Scaffold(
        containerColor = BgDark,
        bottomBar = {
            NavigationBar(containerColor = SurfaceDark) {
                listOf("Home", "Chat", "Tools", "Notes", "Settings").forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = currentTab == index,
                        onClick = { currentTab = index },
                        label = { Text(label, color = if (currentTab == index) PrimaryCyan else Color.Gray) },
                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Default.Home
                                    1 -> Icons.Default.Chat
                                    2 -> Icons.Default.Build
                                    3 -> Icons.Default.Note
                                    else -> Icons.Default.Settings
                                },
                                contentDescription = label,
                                tint = if (currentTab == index) PrimaryCyan else Color.Gray
                            )
                        }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (currentTab) {
                0 -> HomeScreen(assistantState, onVoiceTrigger, onExecuteText)
                1 -> ChatScreen(chatMessages, onExecuteText)
                2 -> ToolsScreen(onExecuteCommand = onExecuteText)
                3 -> NotesScreen(notesList, onSaveNote, onDeleteNote)
                4 -> SettingsScreen()
            }
        }
    }
}

@Composable
fun HomeScreen(
    state: AssistantState,
    onVoiceTrigger: () -> Unit,
    onExecuteText: (String) -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    val infiniteTransition = rememberInfiniteTransition(label = "orb")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (state == AssistantState.LISTENING || state == AssistantState.SPEAKING) 1.25f else 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (state == AssistantState.SPEAKING) 600 else 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val orbColor by animateColorAsState(
        targetValue = when (state) {
            AssistantState.IDLE -> PrimaryCyan
            AssistantState.LISTENING -> StateListening
            AssistantState.THINKING -> AccentPurple
            AssistantState.SPEAKING -> StateSpeaking
        },
        label = "orbColor"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SATHI AI", color = PrimaryCyan, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(
                when (state) {
                    AssistantState.IDLE -> "Ready for your command"
                    AssistantState.LISTENING -> "Aapki aawaz sun raha hoon..."
                    AssistantState.THINKING -> "Thinking..."
                    AssistantState.SPEAKING -> "Speaking..."
                },
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(170.dp)
                .scale(pulseScale)
                .background(
                    Brush.radialGradient(listOf(orbColor, AccentPurple, Color.Transparent)),
                    shape = CircleShape
                )
                .clickable { onVoiceTrigger() }
        ) {
            Icon(
                imageVector = when (state) {
                    AssistantState.LISTENING -> Icons.Default.GraphicEq
                    AssistantState.SPEAKING -> Icons.Default.VolumeUp
                    else -> Icons.Default.Mic
                },
                contentDescription = "Voice Orb",
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().background(SurfaceDark, RoundedCornerShape(16.dp)).padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Command boliye ya likhiye...", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        onExecuteText(textInput)
                        textInput = ""
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = PrimaryCyan)
            }
        }
    }
}

@Composable
fun ChatScreen(messages: List<ChatMessage>, onExecuteText: (String) -> Unit) {
    var textInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Conversation", color = PrimaryCyan, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (messages.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Koi baat cheet nahi hui abhi tak.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    val isUser = msg.sender == "User"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isUser) AccentPurple else SurfaceDark,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    if (isUser) "You" else "Sathi",
                                    color = if (isUser) Color.White else PrimaryCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(msg.text, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().background(SurfaceDark, RoundedCornerShape(14.dp)).padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Reply likhein...", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        onExecuteText(textInput)
                        textInput = ""
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = PrimaryCyan)
            }
        }
    }
}

@Composable
fun NotesScreen(
    notes: List<NoteEntity>,
    onSaveNote: (String, String) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column {
            Text("Voice & Disk Notes", color = PrimaryCyan, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            if (notes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Koi note nahi hai. Bol kar ya '+' daba kar banayein.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(notes) { note ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(note.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(note.content, color = Color.LightGray, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(note.timestamp)),
                                        color = Color.DarkGray,
                                        fontSize = 11.sp
                                    )
                                }
                                IconButton(onClick = { onDeleteNote(note) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            containerColor = PrimaryCyan,
            contentColor = BgDark,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Note")
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Naya Note Likhien") },
                text = {
                    Column {
                        TextField(value = noteTitle, onValueChange = { noteTitle = it }, placeholder = { Text("Title") })
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(value = noteContent, onValueChange = { noteContent = it }, placeholder = { Text("Content") })
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (noteContent.isNotBlank()) {
                            onSaveNote(if (noteTitle.isBlank()) "Quick Note" else noteTitle, noteContent)
                            noteTitle = ""
                            noteContent = ""
                            showDialog = false
                        }
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun ToolsScreen(onExecuteText: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Real Device OS Control Tools", color = Color.Gray)
    }
}

@Composable
fun SettingsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Sathi Engine & Voice Settings", color = Color.Gray)
    }
}
