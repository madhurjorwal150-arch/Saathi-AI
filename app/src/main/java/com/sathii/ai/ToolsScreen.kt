package com.sathii.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SurfaceDark = Color(0xFF121824)
private val PrimaryCyan = Color(0xFF00E5FF)
private val AccentPurple = Color(0xFF7C4DFF)

@Composable
fun ToolsScreen(onExecuteCommand: (String) -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "System Tools & OS Controls",
            color = PrimaryCyan,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Direct native hardware and system actions",
            color = Color.Gray,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hardware Controls Section
        Text(
            text = "Hardware Quick Actions",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ToolActionCard(
                modifier = Modifier.weight(1f),
                title = "Torch ON",
                icon = Icons.Default.FlashlightOn,
                iconColor = PrimaryCyan,
                onClick = { onExecuteCommand("torch on") }
            )
            ToolActionCard(
                modifier = Modifier.weight(1f),
                title = "Torch OFF",
                icon = Icons.Default.FlashlightOff,
                iconColor = Color.Red,
                onClick = { onExecuteCommand("torch off") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ToolActionCard(
                modifier = Modifier.weight(1f),
                title = "Volume Up",
                icon = Icons.Default.VolumeUp,
                iconColor = PrimaryCyan,
                onClick = { onExecuteCommand("volume up") }
            )
            ToolActionCard(
                modifier = Modifier.weight(1f),
                title = "Volume Down",
                icon = Icons.Default.VolumeDown,
                iconColor = PrimaryCyan,
                onClick = { onExecuteCommand("volume down") }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // System Clocks
        Text(
            text = "Clock & Alarms",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ToolActionCard(
                modifier = Modifier.weight(1f),
                title = "5 Min Timer",
                icon = Icons.Default.Timer,
                iconColor = AccentPurple,
                onClick = { onExecuteCommand("timer 5") }
            )
            ToolActionCard(
                modifier = Modifier.weight(1f),
                title = "6 AM Alarm",
                icon = Icons.Default.Alarm,
                iconColor = AccentPurple,
                onClick = { onExecuteCommand("alarm 6") }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // System Diagnostics Status
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "System Diagnostics",
                    color = PrimaryCyan,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                DiagnosticRow(label = "Voice Engine", status = "Native SpeechRecognizer", ok = true)
                DiagnosticRow(label = "Offline Storage", status = "Room Database Active", ok = true)
                DiagnosticRow(label = "Routing Mode", status = "Tri-Mode Strict", ok = true)
                DiagnosticRow(label = "Target SDK", status = "Android 14 (API 34)", ok = true)
            }
        }
    }
}

@Composable
fun ToolActionCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun DiagnosticRow(label: String, status: String, ok: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 13.sp)
        Text(
            text = status,
            color = if (ok) Color(0xFF69F0AE) else Color.Red,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
