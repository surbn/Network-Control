package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CellTowerInfo
import com.example.model.CellularTelemetry
import com.example.ui.components.IosGroupedCard
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun TowerMapScreen(
    towers: List<CellTowerInfo> = emptyList(),
    onRefreshTowers: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTower by remember(towers) {
        mutableStateOf(towers.firstOrNull { it.isConnected } ?: towers.firstOrNull())
    }

    var mapZoom by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var activeLayer by remember { mutableStateOf("Radar") } // Radar, Beams, Heatmap

    // Continuous Radar sweep animation
    val infiniteTransition = rememberInfiniteTransition(label = "radar_sweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
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

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "Copied: $text", Toast.LENGTH_SHORT).show()
    }

    fun openDirections(lat: Double, lng: Double, label: String) {
        try {
            val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($label)")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Maps app unavailable. Coordinates: $lat, $lng", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // iOS Navigation Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Tower Map",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White
                )
                Text(
                    text = "LOCAL 5G/4G BASE STATION RADAR",
                    fontSize = 11.sp,
                    color = IosCyan,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        triggerHaptic()
                        onRefreshTowers()
                    },
                color = IosBlue.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, IosBlue.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh",
                        tint = IosBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rescan", color = IosBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Interactive Map / Radar Canvas Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .clip(RoundedCornerShape(26.dp))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(26.dp)),
            color = Color(0xFF090B10)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            panOffset += dragAmount
                        }
                    }
            ) {
                // Canvas Radar View
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f + panOffset.x, size.height / 2f + panOffset.y)
                    val baseRadius = (size.minDimension / 2.2f) * mapZoom

                    // Concentric Range Rings (500m, 1000m, 1500m)
                    val ringSteps = listOf(0.33f to "500m", 0.66f to "1km", 1.0f to "1.5km")
                    for ((fraction, _) in ringSteps) {
                        drawCircle(
                            color = Color(0x2232425E),
                            radius = baseRadius * fraction,
                            center = center,
                            style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                        )
                    }

                    // Axis Crosshairs
                    drawLine(
                        color = Color(0x1AFFFFFF),
                        start = Offset(center.x - baseRadius * 1.1f, center.y),
                        end = Offset(center.x + baseRadius * 1.1f, center.y),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color(0x1AFFFFFF),
                        start = Offset(center.x, center.y - baseRadius * 1.1f),
                        end = Offset(center.x, center.y + baseRadius * 1.1f),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Sweeping radar beam
                    if (activeLayer == "Radar") {
                        val sweepRad = Math.toRadians(sweepAngle.toDouble())
                        val endX = center.x + (baseRadius * 1.05f) * cos(sweepRad).toFloat()
                        val endY = center.y + (baseRadius * 1.05f) * sin(sweepRad).toFloat()
                        drawLine(
                            brush = Brush.radialGradient(
                                listOf(IosCyan.copy(alpha = 0.6f), Color.Transparent),
                                center = center,
                                radius = baseRadius
                            ),
                            start = center,
                            end = Offset(endX, endY),
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    // Plot Tower Sector Cones and Markers
                    towers.forEach { tower ->
                        val distRatio = (tower.distanceMeters.toFloat() / 1500f).coerceIn(0.2f, 1.0f)
                        val angleRad = Math.toRadians((tower.azimuthDeg - 90.0))
                        val towerX = center.x + (baseRadius * distRatio) * cos(angleRad).toFloat()
                        val towerY = center.y + (baseRadius * distRatio) * sin(angleRad).toFloat()
                        val towerPos = Offset(towerX, towerY)

                        val isCurrentSelected = (tower.id == selectedTower?.id)
                        val markerColor = when {
                            tower.isConnected -> IosCyan
                            tower.type.contains("5G") -> IosGreen
                            else -> IosBlue
                        }

                        // Draw Sector Beam Cone
                        if (activeLayer == "Beams" || tower.isConnected) {
                            val beamPath = Path().apply {
                                moveTo(towerPos.x, towerPos.y)
                                val beamRadius = 70f * mapZoom
                                val startAngle = (tower.azimuthDeg + 150f).toDouble()
                                val endAngle = (tower.azimuthDeg + 210f).toDouble()
                                lineTo(
                                    towerPos.x + beamRadius * cos(Math.toRadians(startAngle)).toFloat(),
                                    towerPos.y + beamRadius * sin(Math.toRadians(startAngle)).toFloat()
                                )
                                lineTo(
                                    towerPos.x + beamRadius * cos(Math.toRadians(endAngle)).toFloat(),
                                    towerPos.y + beamRadius * sin(Math.toRadians(endAngle)).toFloat()
                                )
                                close()
                            }
                            drawPath(
                                path = beamPath,
                                color = markerColor.copy(alpha = if (isCurrentSelected) 0.35f else 0.18f)
                            )
                        }

                        // Connection beam to device if connected
                        if (tower.isConnected) {
                            drawLine(
                                brush = Brush.linearGradient(listOf(IosCyan, Color.Transparent)),
                                start = towerPos,
                                end = center,
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                            )
                        }

                        // Outer ring for selected tower
                        if (isCurrentSelected) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.5f),
                                radius = 22.dp.toPx(),
                                center = towerPos,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }

                        // Tower Marker Circle
                        drawCircle(
                            color = markerColor,
                            radius = if (tower.isConnected) 9.dp.toPx() else 7.dp.toPx(),
                            center = towerPos
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.dp.toPx(),
                            center = towerPos
                        )
                    }

                    // User Device Position (Apple Blue dot with pulse)
                    drawCircle(
                        color = IosBlue.copy(alpha = 0.3f),
                        radius = 16.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 8.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = IosBlue,
                        radius = 6.dp.toPx(),
                        center = center
                    )
                }

                // Top Floating Status Header on Map
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xCC14161F),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(IosCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${towers.size} Towers • GPS Active",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Layer Switcher (Radar / Beams / Clean)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xCC14161F),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Row(modifier = Modifier.padding(2.dp)) {
                            listOf("Radar", "Beams").forEach { layer ->
                                val active = layer == activeLayer
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) IosBlue else Color.Transparent)
                                        .clickable {
                                            triggerHaptic()
                                            activeLayer = layer
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = layer,
                                        color = if (active) Color.White else IosGray1,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Zoom & Recenter Controls (Bottom-Right overlay)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MapMiniButton(icon = Icons.Rounded.Add) {
                        triggerHaptic()
                        mapZoom = (mapZoom + 0.25f).coerceAtMost(2.5f)
                    }
                    MapMiniButton(icon = Icons.Rounded.Remove) {
                        triggerHaptic()
                        mapZoom = (mapZoom - 0.25f).coerceAtLeast(0.6f)
                    }
                    MapMiniButton(icon = Icons.Rounded.MyLocation) {
                        triggerHaptic()
                        panOffset = Offset.Zero
                        mapZoom = 1f
                    }
                }
            }
        }

        // Horizontal Tower Quick Picker Chips
        Text(
            text = "DETECTED LOCAL TOWERS (TAP TO INSPECT)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = IosGray1,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            towers.forEach { tower ->
                val isSelected = tower.id == selectedTower?.id
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            1.dp,
                            if (isSelected) IosCyan else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            triggerHaptic()
                            selectedTower = tower
                        }
                        .testTag("tower_chip_${tower.id}"),
                    color = if (isSelected) IosCyan.copy(alpha = 0.16f) else Color(0xFF14161F)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(if (tower.isConnected) IosCyan else Color(0xFF242738)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CellTower,
                                contentDescription = null,
                                tint = if (tower.isConnected) Color.Black else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (tower.isConnected) "Primary (Serving)" else tower.type,
                                    color = if (tower.isConnected) IosCyan else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${tower.distanceMeters}m • ${tower.rsrpDbm} dBm",
                                color = IosGray1,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Selected Tower Comprehensive Detail Card
        selectedTower?.let { tower ->
            IosGroupedCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tower.name,
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${tower.operator} • ${tower.type}",
                            color = IosCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (tower.isConnected) IosGreen.copy(alpha = 0.18f) else Color(0xFF26293A),
                        border = BorderStroke(1.dp, if (tower.isConnected) IosGreen.copy(alpha = 0.4f) else Color.Transparent)
                    ) {
                        Text(
                            text = if (tower.isConnected) "CONNECTED" else "NEIGHBOR",
                            color = if (tower.isConnected) IosGreen else IosGray1,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Primary 4-Stat Box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TowerDetailMiniStat("DISTANCE", "${tower.distanceMeters} m", IosCyan)
                    TowerDetailMiniStat("BEARING", "${tower.azimuthDeg.roundToInt()}°", IosBlue)
                    TowerDetailMiniStat("RSRP", "${tower.rsrpDbm} dBm", if (tower.rsrpDbm > -85) IosGreen else IosOrange)
                    TowerDetailMiniStat("SINR", tower.sinr, IosPurple)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Technical Telephony Details Table
                HorizontalDivider(color = IosDivider)
                Spacer(modifier = Modifier.height(8.dp))

                TowerTableRow("Physical Cell ID (PCI)", "${tower.pci}") {
                    copyToClipboard("PCI", "${tower.pci}")
                }
                TowerTableRow("Tracking Area Code (TAC)", "${tower.tac}") {
                    copyToClipboard("TAC", "${tower.tac}")
                }
                TowerTableRow("gNodeB / Cell ID", "${tower.cellId}") {
                    copyToClipboard("Cell ID", "${tower.cellId}")
                }
                TowerTableRow("Frequency Band", tower.band)
                TowerTableRow("Antenna Mast Height", "${tower.heightMeters} meters")
                TowerTableRow("Coverage Radius", "~${tower.coverageRadiusMeters} meters")
                TowerTableRow("GPS Coordinates", "%.4f, %.4f".format(tower.lat, tower.lng)) {
                    copyToClipboard("Coordinates", "${tower.lat},${tower.lng}")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation & Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            triggerHaptic()
                            openDirections(tower.lat, tower.lng, tower.name)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IosBlue),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("navigate_to_tower_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Navigation,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Navigate to Tower", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            triggerHaptic()
                            val summary = "Tower: ${tower.name}\nOperator: ${tower.operator}\nBand: ${tower.band}\nPCI: ${tower.pci}\nTAC: ${tower.tac}\nCell ID: ${tower.cellId}\nRSRP: ${tower.rsrpDbm} dBm"
                            copyToClipboard("Tower Details", summary)
                        },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.height(46.dp)
                    ) {
                        Icon(Icons.Rounded.ContentCopy, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy All", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun MapMiniButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        color = Color(0xDD1B1E2B),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun TowerDetailMiniStat(label: String, value: String, color: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1B1E29))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = IosGray1, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(3.dp))
        Text(value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TowerTableRow(
    label: String,
    value: String,
    onCopy: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onCopy != null) Modifier.clickable { onCopy() } else Modifier)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = IosGray1, fontSize = 12.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            if (onCopy != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = "Copy",
                    tint = IosGray2,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
