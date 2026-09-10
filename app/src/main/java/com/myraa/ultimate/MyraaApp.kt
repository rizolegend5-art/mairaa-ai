package com.myraa.ultimate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dark = Color(0xFF050913)
private val navy = Color(0xFF0A1730)
private val cyan = Color(0xFF37D8FF)
private val violet = Color(0xFF8B5CFF)
private val glassBorder = Color(0x33FFFFFF)

@Composable
fun MyraaApp(vm: MyraaViewModel, onRepeatClick: () -> Unit = {}) {
    val msgs by vm.messages.collectAsStateWithLifecycle()
    val status by vm.status.collectAsStateWithLifecycle()
    val liveState by vm.liveState.collectAsStateWithLifecycle()
    var text by remember { mutableStateOf("") }
    var on by remember { mutableStateOf(false) }

    MaterialTheme(colorScheme = darkColorScheme(primary = cyan, secondary = violet, background = dark)) {
        Scaffold(containerColor = dark) { pad ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF162B52), navy, dark),
                            radius = 900f
                        )
                    )
                    .padding(16.dp)
            ) {
                Header(status)
                Spacer(Modifier.height(10.dp))
                StatsRow(status)
                Spacer(Modifier.height(10.dp))
                ReactorCore(status)
                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassPill(if (on) "VOICE MODE: ON" else "VOICE MODE: OFF", if (on) cyan else Color(0xFF6B7A99))
                    GlassPill("say “Hey Captain”", violet)
                }
                Spacer(Modifier.height(10.dp))

                val liveOn = liveState == "LISTENING" || liveState == "SPEAKING" || liveState == "CONNECTING"
                Button(
                    onClick = { if (liveOn) vm.stopLiveChat() else vm.startLiveChat() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    if (liveOn) listOf(Color(0xFFE64980), Color(0xFFB33FCE)) else listOf(Color(0xFF19B4D6), violet)
                                ),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            when (liveState) {
                                "CONNECTING" -> "CONNECTING..."
                                "LISTENING" -> "🔴 AI LIVE CHAT • LISTENING"
                                "SPEAKING" -> "🔴 AI LIVE CHAT • SPEAKING"
                                else -> "🤖 START AI LIVE CHAT (Gemini)"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                if (liveState.startsWith("ERROR")) {
                    Spacer(Modifier.height(6.dp))
                    Text(liveState, color = Color(0xFFFF6B6B), style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { on = !on; vm.toggle(on) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    if (on) listOf(Color(0xFF19B4D6), cyan) else listOf(violet, Color(0xFF5E3FCE))
                                ),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (on) "VOICE MODE ON • TAP TO STOP" else "START BACKGROUND VOICE MODE",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "Try: “Hey Captain, [video naam] video chalao” • “note likho ...” • “ChatGPT kholo”",
                    color = Color(0xFF8FA2C2),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = onRepeatClick,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color(0x1AFFFFFF), RoundedCornerShape(14.dp))
                            .padding(1.dp)
                            .background(navy.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎙  REPEAT MY VOICE", color = cyan, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))

                GlassCard(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(msgs) { m ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = if (m.user) Arrangement.End else Arrangement.Start
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = if (m.user) Color(0xFF1C3F63) else Color(0xFF141E33),
                                    border = BorderStroke(1.dp, if (m.user) Color(0x5537D8FF) else Color(0x338B5CFF)),
                                    modifier = Modifier.widthIn(max = 300.dp)
                                ) {
                                    Text(m.text, Modifier.padding(12.dp), color = Color(0xFFEAF0FA))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        text,
                        { text = it },
                        Modifier.weight(1f),
                        label = { Text("CAPTAIN se baat karein") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = cyan,
                            unfocusedBorderColor = glassBorder
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { vm.send(text); text = "" },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = cyan)
                    ) { Text("SEND", color = dark, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun Header(status: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .background(cyan, CircleShape)
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text("CAPTAIN", color = cyan, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
            Text("STARK-CLASS SHIP AI • $status", color = Color(0xFF9FB0C6), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun StatsRow(status: String) {
    val battery = rememberBatteryLevel()
    val clock = rememberClock()
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        StatChip("BATTERY", "$battery%", Modifier.weight(1f))
        StatChip("TIME", clock, Modifier.weight(1f))
        StatChip("MIC", status, Modifier.weight(1f))
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(Color(0x14FFFFFF), RoundedCornerShape(10.dp))
            .border(BorderStroke(1.dp, glassBorder), RoundedCornerShape(10.dp))
            .padding(vertical = 6.dp, horizontal = 8.dp)
    ) {
        Text(label, color = Color(0xFF6FA8C9), fontSize = 9.sp, letterSpacing = 1.sp)
        Text(value, color = cyan, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}

@Composable
private fun rememberBatteryLevel(): Int {
    val context = LocalContext.current
    var level by remember { mutableStateOf(0) }
    DisposableEffect(Unit) {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, i: Intent?) {
                val raw = i?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = i?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                if (raw >= 0 && scale > 0) level = raw * 100 / scale
            }
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose { context.unregisterReceiver(receiver) }
    }
    return level
}

@Composable
private fun rememberClock(): String {
    var time by remember { mutableStateOf(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())) }
    LaunchedEffect(Unit) {
        while (true) {
            time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            delay(30000)
        }
    }
    return time
}

@Composable
private fun GlassPill(text: String, accent: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color(0x1AFFFFFF),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.6f))
    ) {
        Text(text, Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = accent, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GlassCard(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier
            .fillMaxWidth()
            .background(Color(0x14FFFFFF), RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(Color(0x0DFFFFFF), Color.Transparent)), RoundedCornerShape(20.dp))
            .padding(1.dp)
            .background(navy.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        content()
    }
}

@Composable
private fun ReactorCore(status: String) {
    val inf = rememberInfiniteTransition(label = "reactor")
    val rot1 by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(7000, easing = LinearEasing)), label = "r1")
    val rot2 by inf.animateFloat(360f, 0f, infiniteRepeatable(tween(11000, easing = LinearEasing)), label = "r2")
    val rot3 by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(16000, easing = LinearEasing)), label = "r3")
    val pulse by inf.animateFloat(0.95f, 1.05f, infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "p")

    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(200.dp)
                .scale(pulse)
                .background(Brush.radialGradient(listOf(cyan.copy(alpha = 0.28f), Color.Transparent)), CircleShape)
        )
        Box(
            Modifier
                .size(180.dp)
                .rotate(rot1)
                .border(BorderStroke(2.dp, Brush.sweepGradient(listOf(cyan, Color.Transparent, cyan, Color.Transparent, cyan))), CircleShape)
        )
        Box(
            Modifier
                .size(150.dp)
                .rotate(rot2)
                .border(BorderStroke(2.dp, Brush.sweepGradient(listOf(violet, Color.Transparent, violet, Color.Transparent))), CircleShape)
        )
        Box(
            Modifier
                .size(120.dp)
                .rotate(rot3)
                .border(BorderStroke(1.dp, Brush.sweepGradient(listOf(cyan, Color.Transparent))), CircleShape)
        )
        Box(
            Modifier
                .size(64.dp)
                .scale(pulse)
                .background(Brush.radialGradient(listOf(Color.White, cyan, violet, dark)), CircleShape)
        )
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter),
            shape = RoundedCornerShape(50),
            color = navy.copy(alpha = 0.85f),
            border = BorderStroke(1.dp, cyan.copy(alpha = 0.5f))
        ) {
            Text(
                status,
                Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                color = cyan,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
