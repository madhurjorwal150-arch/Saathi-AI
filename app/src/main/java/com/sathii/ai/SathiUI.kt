package com.sathii.ai

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sathii.ai.model.ActionResult

val BgDark = Color(0xFF080B12)
val SurfaceDark = Color(0xFF10151F)
val CardDark = Color(0xFF151B26)
val AccentCyan = Color(0xFF00E5FF)
val AccentViolet = Color(0xFF7C4DFF)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF9CA3AF)

data class ChatMessage(val text: String, val isUser: Boolean, val timestamp: Long = System.currentTimeMillis())
data class NoteItem(val id: Long, val title: String, val content: String)

@Composable
fun SathiApp(
    onTriggerVoice: () -> Unit,
    onExecuteCommand: (String) -> ActionResult,
    savedNotes: List<NoteItem>
) {
    var currentTab by remember { mutableStateOf(0) }
    var userName by remember { mutableStateOf("Madhur") }
    var customWakeWord by remember { mutableStateOf("Hey Sathi") }
    val chatMessages = remember {
        mutableStateListOf(
            ChatMessage("Namaste $userName! Main aapka Sathi hoon. Boliye ya likhiye, main ready hoon!", false)
        )
    }

    Scaffold(
        containerColor = BgDark,
        bottomBar = {
            NavigationBar(containerColor = SurfaceDark) {
                val items = listOf("Home", "Chat", "Tools", "Notes", "Settings")
                val icons = listOf(Icons.Default.Home, Icons.Default.Chat, Icons.Default.Build, Icons.Default.Notes, Icons.Default.Settings)
                items.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = currentTab == index,
                        onClick = { currentTab = index },
                        icon = { Icon(icons[index], contentDescription = title) },
                        label = { Text(text = title, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentCyan,
                            selectedTextColor = AccentCyan,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = CardDark
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (currentTab) {
                0 -> HomeScreen(userName = userName, onTabSelect = { currentTab = it }, onTriggerVoice = onTriggerVoice)
                1 -> ChatScreen(
                    messages = chatMessages,
                    onSendMessage = { text ->
                        chatMessages.add(ChatMessage(text, true))
                        val res = onExecuteCommand(text)
                        chatMessages.add(ChatMessage(res.message, false))
                    },
                    onTriggerVoice = onTriggerVoice
                )
                2 -> ToolsScreen(onSelectTool = { toolPrompt ->
                    currentTab = 1
                    chatMessages.add(ChatMessage(toolPrompt, true))
                    val res = onExecuteCommand(toolPrompt)
                    chatMessages.add(ChatMessage(res.message, false))
                })
                3 -> NotesScreen(notes = savedNotes)
                4 -> SettingsScreen(
                    userName = userName,
                    wakeWord = customWakeWord,
                    onSave = { n, w ->
                        userName = n
                        customWakeWord = w
                    }
                )
            }
        }
    }
}

@Composable
fun HomeScreen(userName: String, onTabSelect: (Int) -> Unit, onTriggerVoice: () -> Unit) {
    val greetingText = remember(userName) {
        "Namaste $userName! 🙏\nMain aapka Sathi hoon, bataiye aaj kya help karu?"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "SATHI AI", fontWeight = FontWeight.Bold, color = AccentCyan, fontSize = 20.sp)
            Surface(
                color = CardDark,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(4.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF00E676)))
                    Spacer(Modifier.width(6.dp))
                    Text(text = "Always Active", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }

        Spacer(Modifier.height(30.dp))

        GlowingVoiceOrb(isListening = false, onClick = onTriggerVoice)

        Spacer(Modifier.height(24.dp))

        Text(
            text = greetingText,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(30.dp))

        Text(text = "Quick Actions", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickCard("Talk to Sathi", Icons.Default.Mic, Modifier.weight(1f)) { onTriggerVoice() }
            QuickCard("Ask Anything", Icons.Default.Send, Modifier.weight(1f)) { onTabSelect(1) }
        }
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickCard("15+ AI Tools", Icons.Default.AutoAwesome, Modifier.weight(1f)) { onTabSelect(2) }
            QuickCard("My Notes", Icons.Default.Edit, Modifier.weight(1f)) { onTabSelect(3) }
        }
    }
}

@Composable
fun QuickCard(title: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = title, tint = AccentCyan)
            Spacer(Modifier.height(8.dp))
            Text(text = title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

@Composable
fun GlowingVoiceOrb(isListening: Boolean, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(140.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(AccentCyan.copy(alpha = 0.8f), AccentViolet.copy(alpha = 0.5f), Color.Transparent)
                )
            )
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(SurfaceDark)
                .border(2.dp, AccentCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Mic, contentDescription = "Voice", tint = AccentCyan, modifier = Modifier.size(34.dp))
        }
    }
}

@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onTriggerVoice: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            items(messages) { msg ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (msg.isUser) AccentViolet else CardDark)
                            .padding(14.dp)
                            .widthIn(max = 280.dp)
                    ) {
                        Text(text = msg.text, color = TextPrimary, fontSize = 15.sp)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onTriggerVoice) {
                Icon(Icons.Default.Mic, contentDescription = "Mic", tint = AccentCyan)
            }
            TextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text(text = "Sathi ko boliye ya likhiye...", color = TextSecondary) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CardDark,
                    unfocusedContainerColor = CardDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        onSendMessage(textInput)
                        textInput = ""
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = AccentCyan)
            }
        }
    }
}

@Composable
fun ToolsScreen(onSelectTool: (String) -> Unit) {
    val tools = listOf(
        "Message Polish" to "Professional message likh do",
        "Summarize Text" to "Is paragraph ka summary banao",
        "YouTube Title" to "YouTube video ke viral title suggest karo",
        "Email Writer" to "Leave ke liye ek professional email draft karo",
        "Grammar Fixer" to "Is sentence ki English aur grammar theek karo",
        "Caption Maker" to "Instagram photo ke liye trending captions do",
        "Spotify Music" to "Play Sidhu Moose Wala on Spotify",
        "Torch Control" to "Torch on karo"
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "AI Tools Hub", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 22.sp)
        Text(text = "Direct 1-click productivity tools", color = TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(tools) { (title, prompt) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().clickable { onSelectTool(prompt) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = prompt, color = TextSecondary, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AccentCyan)
                    }
                }
            }
        }
    }
}

@Composable
fun NotesScreen(notes: List<NoteItem>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "My Sathi Notes", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 22.sp)
        Text(text = "Voice se save kiye gaye aapke notes", color = TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))

        if (notes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Abhi koi note nahi hai.\nBoliye: 'Sathi, ek note bana do'", color = TextSecondary, fontSize = 14.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(notes) { note ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = note.title, color = AccentCyan, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text(text = note.content, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(userName: String, wakeWord: String, onSave: (String, String) -> Unit) {
    var nameInput by remember { mutableStateOf(userName) }
    var wakeInput by remember { mutableStateOf(wakeWord) }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text(text = "Profile & Settings", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 22.sp)
        Spacer(Modifier.height(20.dp))

        Text(text = "Aapka Naam:", color = TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        TextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CardDark,
                unfocusedContainerColor = CardDark,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Text(text = "Custom Wake Word (Awaz Trigger):", color = TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        TextField(
            value = wakeInput,
            onValueChange = { wakeInput = it },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CardDark,
                unfocusedContainerColor = CardDark,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onSave(nameInput, wakeInput) },
            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Save Preferences", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}
