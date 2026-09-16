package com.sathii.ai

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SurfaceDark = Color(0xFF121824)
private val PrimaryCyan = Color(0xFF00E5FF)
private val AccentPurple = Color(0xFF7C4DFF)

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("sathi_settings", Context.MODE_PRIVATE) }

    var speechFeedback by remember {
        mutableStateOf(prefs.getBoolean("voice_feedback_enabled", true))
    }
    var wakeLockEnabled by remember {
        mutableStateOf(prefs.getBoolean("wake_lock_enabled", true))
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Engine & Voice Settings",
            color = PrimaryCyan,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Manage assistant behaviors and Android OS permissions",
            color = Color.Gray,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Voice Feedback & Audio",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Voice Feedback", color = Color.White, fontWeight = FontWeight.Medium)
                        Text("Speak out confirmations via TTS", color = Color.Gray, fontSize = 12.sp)
                    }
                    Switch(
                        checked = speechFeedback,
                        onCheckedChange = {
                            speechFeedback = it
                            prefs.edit().putBoolean("voice_feedback_enabled", it).apply()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryCyan)
                    )
                }

                Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Keep Screen Responsive", color = Color.White, fontWeight = FontWeight.Medium)
                        Text("Prevent aggressive lockscreen deep sleep", color = Color.Gray, fontSize = 12.sp)
                    }
                    Switch(
                        checked = wakeLockEnabled,
                        onCheckedChange = {
                            wakeLockEnabled = it
                            prefs.edit().putBoolean("wake_lock_enabled", it).apply()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryCyan)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "System Permissions",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = "Permissions", tint = PrimaryCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open App Permissions", color = Color.White)
                }

                OutlinedButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.BatteryAlert, contentDescription = "Battery", tint = AccentPurple)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Battery Optimization Settings", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("About Sathi AI", color = PrimaryCyan, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Version: 1.0.0 (Production Architecture)", color = Color.Gray, fontSize = 13.sp)
                Text("Build ID: Canonical Native Android", color = Color.Gray, fontSize = 13.sp)
            }
        }
    }
}
