package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CellularTelemetry
import com.example.model.WifiTelemetry
import com.example.ui.components.IosGroupedCard
import com.example.ui.components.IosListRow
import com.example.ui.components.MiniSignalBars
import com.example.ui.theme.*

@Composable
fun TelemetryScreen(
    cellular: CellularTelemetry,
    wifi: WifiTelemetry,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // iOS Navigation Title
        Column {
            Text(
                text = "Telemetry",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White
            )
            Text(
                text = "RADIO FREQUENCY & SIGNAL DIAGNOSTICS",
                fontSize = 11.sp,
                color = IosCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }

        // Live RF Signal Gauge Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(26.dp)),
            color = Color(0xFF14161F)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("CELLULAR SIGNAL LEVEL", color = IosGray1, fontSize = 10.sp, letterSpacing = 1.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${cellular.signalDbm}",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "dBm",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = IosCyan
                            )
                        }
                    }

                    // Signal Quality indicator badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = IosGreen.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, IosGreen.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("EXCELLENT", color = IosGreen, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            MiniSignalBars(level = cellular.signalLevel, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Progress gauge bar
                LinearProgressIndicator(
                    progress = { 0.85f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = IosCyan,
                    trackColor = Color(0xFF242735)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4-column signal metric grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricMiniBox(label = "RSRP", value = cellular.rsrp)
                    MetricMiniBox(label = "RSRQ", value = cellular.rsrq)
                    MetricMiniBox(label = "SINR", value = cellular.sinr)
                    MetricMiniBox(label = "ASU", value = "${cellular.asu}")
                }
            }
        }

        // 5G NR Band & Network Mode Details
        Text(
            text = "RADIO & CARRIER AGGREGATION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = IosGray1,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        IosGroupedCard {
            IosListRow(
                title = "Primary 5G Band",
                subtitle = "Sub-6 GHz Mid-Band NR",
                icon = Icons.Rounded.CellTower,
                iconBgColor = IosBlue,
                trailingBadge = "n78 (3.5 GHz)"
            )
            HorizontalDivider(color = IosDivider, modifier = Modifier.padding(start = 46.dp))

            IosListRow(
                title = "Carrier Aggregation",
                subtitle = "Multi-channel combined bandwidth",
                icon = Icons.Rounded.Layers,
                iconBgColor = IosPurple,
                trailingText = "2CC Active"
            )
            HorizontalDivider(color = IosDivider, modifier = Modifier.padding(start = 46.dp))

            IosListRow(
                title = "Network Architecture",
                subtitle = "NR Core connection type",
                icon = Icons.Rounded.Hub,
                iconBgColor = IosCyan,
                trailingBadge = "SA (Standalone)"
            )
            HorizontalDivider(color = IosDivider, modifier = Modifier.padding(start = 46.dp))

            IosListRow(
                title = "VoNR Calling",
                subtitle = "High-definition Voice over 5G NR",
                icon = Icons.Rounded.PhoneInTalk,
                iconBgColor = IosGreen,
                trailingText = "Supported"
            )
        }

        // Wi-Fi Status Section
        Text(
            text = "LOCAL WI-FI TELEMETRY",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = IosGray1,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        IosGroupedCard {
            IosListRow(
                title = "Wi-Fi Network",
                subtitle = wifi.frequencyBand,
                icon = Icons.Rounded.Wifi,
                iconBgColor = if (wifi.isConnected) IosCyan else IosGray2,
                trailingText = wifi.ssid
            )
            HorizontalDivider(color = IosDivider, modifier = Modifier.padding(start = 46.dp))

            IosListRow(
                title = "Link Speed",
                subtitle = "PHY connection transfer rate",
                icon = Icons.Rounded.Speed,
                iconBgColor = IosBlue,
                trailingText = "${wifi.linkSpeedMbps} Mbps"
            )
            HorizontalDivider(color = IosDivider, modifier = Modifier.padding(start = 46.dp))

            IosListRow(
                title = "Local IPv4",
                subtitle = "Assigned DHCP address",
                icon = Icons.Rounded.Lan,
                iconBgColor = IosOrange,
                trailingText = wifi.ipAddress
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun MetricMiniBox(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E212E))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = IosGray1, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
