package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PingMetrics
import com.example.ui.components.IosGroupedCard
import com.example.ui.components.IosListRow
import com.example.ui.theme.*

@Composable
fun SpeedPingScreen(
    ping: PingMetrics = PingMetrics(),
    onStartPing: (String, String) -> Unit = { _, _ -> },
    onStopPing: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedServerIndex by remember { mutableStateOf(0) }

    val servers = listOf(
        Pair("1.1.1.1", "Cloudflare Anycast"),
        Pair("8.8.8.8", "Google Global DNS"),
        Pair("208.67.222.222", "Cisco OpenDNS")
    )

    fun triggerHaptic() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(20)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Speed & Ping",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White
            )
            Text(
                text = "REAL-TIME LATENCY & JITTER ANALYZER",
                fontSize = 11.sp,
                color = IosCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }

        // Live Ping Card with real-time waveform graph
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(26.dp)),
            color = Color(0xFF14161F)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = ping.targetName.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = IosGray1,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = ping.targetHost,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IosCyan
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (ping.isRunning) IosGreen.copy(alpha = 0.15f) else Color(0xFF232530),
                        border = BorderStroke(1.dp, if (ping.isRunning) IosGreen.copy(alpha = 0.4f) else Color.Transparent)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (ping.isRunning) IosGreen else IosGray2)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (ping.isRunning) "STREAMING" else "IDLE",
                                color = if (ping.isRunning) IosGreen else IosGray1,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Big Latency Display
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${ping.currentMs}",
                        fontSize = 58.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ms",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = IosCyan,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Text(
                    text = if (ping.currentMs < 30) "ULTRA LOW LATENCY (5G SA)" else "OPTIMAL BROADBAND RESPONSE",
                    color = IosGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Custom Waveform Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F1118))
                        .padding(8.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val points = ping.pingHistory
                        if (points.size > 1) {
                            val maxVal = (points.maxOrNull() ?: 50f).coerceAtLeast(30f)
                            val stepX = size.width / (points.size - 1)
                            val path = Path()

                            points.forEachIndexed { index, ms ->
                                val x = index * stepX
                                val y = size.height - ((ms / maxVal) * size.height).coerceIn(4f, size.height - 4f)
                                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }

                            drawPath(
                                path = path,
                                brush = Brush.horizontalGradient(listOf(IosCyan, IosBlue)),
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stats breakdown (Min / Max / Avg / Jitter)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatPill("MIN", "${ping.minMs} ms", IosCyan)
                    StatPill("MAX", "${ping.maxMs} ms", IosOrange)
                    StatPill("AVG", "${ping.avgMs} ms", IosBlue)
                    StatPill("JITTER", "${ping.jitterMs} ms", IosGreen)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Button
                Button(
                    onClick = {
                        triggerHaptic()
                        if (ping.isRunning) {
                            onStopPing()
                        } else {
                            val s = servers[selectedServerIndex]
                            onStartPing(s.first, s.second)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (ping.isRunning) IosRed else IosGreen
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("ping_toggle_button")
                ) {
                    Icon(
                        imageVector = if (ping.isRunning) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (ping.isRunning) "Stop Live Test" else "Start Continuous Ping",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Server selection group
        Text(
            text = "TARGET PROBE EDGE SERVERS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = IosGray1,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        IosGroupedCard {
            servers.forEachIndexed { index, (host, name) ->
                val isSelected = index == selectedServerIndex
                IosListRow(
                    title = name,
                    subtitle = host,
                    icon = Icons.Rounded.Dns,
                    iconBgColor = if (isSelected) IosBlue else Color(0xFF323545),
                    showChevron = false,
                    onClick = {
                        triggerHaptic()
                        selectedServerIndex = index
                        if (ping.isRunning) onStartPing(host, name)
                    },
                    trailingContent = {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = IosCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )
                if (index < servers.size - 1) {
                    HorizontalDivider(color = IosDivider, modifier = Modifier.padding(start = 46.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun StatPill(label: String, value: String, color: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1B1E29))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = IosGray1, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(3.dp))
        Text(value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
