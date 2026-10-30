package com.example.model

import com.example.R

enum class NetworkMode(
    val id: String,
    val title: String,
    val shortName: String,
    val badge: String,
    val desc: String,
    val radioModeCode: Int,
    val spinnerMatchKeyword: String
) {
    NR_ONLY(
        id = "nr_only",
        title = "5G Only",
        shortName = "5G",
        badge = "NR SA",
        desc = "Locks radio strictly to 5G Standalone. Highest download & lowest ping. Requires VoNR support for voice calls.",
        radioModeCode = 20,
        spinnerMatchKeyword = "NR only"
    ),
    AUTO(
        id = "auto",
        title = "5G / 4G Auto",
        shortName = "Auto",
        badge = "NR / LTE",
        desc = "Recommended default. Automatically balances 5G ultra speed with 4G LTE coverage and battery efficiency.",
        radioModeCode = 33,
        spinnerMatchKeyword = "NR/LTE"
    ),
    LTE_ONLY(
        id = "lte_only",
        title = "4G Only",
        shortName = "4G",
        badge = "LTE",
        desc = "Locks radio to 4G LTE. Eliminates 5G battery drain and ping spikes caused by intermittent 5G signal tower handoffs.",
        radioModeCode = 11,
        spinnerMatchKeyword = "LTE only"
    ),
    LEGACY(
        id = "legacy",
        title = "3G / 2G",
        shortName = "3G",
        badge = "GSM/WCDMA",
        desc = "Extreme battery saver mode and emergency voice coverage in ultra-remote locations.",
        radioModeCode = 9,
        spinnerMatchKeyword = "WCDMA"
    )
}

enum class StatusBarIconType(
    val id: String,
    val title: String,
    val drawableRes: Int,
    val desc: String
) {
    FIVE_G("5g_badge", "5G Badge", R.drawable.ic_status_5g, "Bold 5G indicator in status bar"),
    FOUR_G("4g_badge", "4G LTE", R.drawable.ic_status_4g, "4G/LTE indicator in status bar"),
    TOWER("tower", "Cell Tower", R.drawable.ic_status_tower, "Cellular antenna tower glyph"),
    BOLT("bolt", "5G Flash", R.drawable.ic_status_bolt, "Dynamic lightning bolt icon"),
    SPEED("speed", "Speedometer", R.drawable.ic_status_speed, "Speedometer active gauge"),
    APPLE("apple", "Apple iOS", R.drawable.ic_status_apple, "Cupertino Apple emblem")
}

data class CellularTelemetry(
    val carrierName: String = "Detecting Carrier...",
    val networkType: String = "5G SA Active",
    val is5G: Boolean = true,
    val signalLevel: Int = 4, // 0 to 4
    val signalDbm: Int = -78,
    val asu: Int = 54,
    val rsrp: String = "-82 dBm",
    val rsrq: String = "-10 dB",
    val sinr: String = "22.4 dB",
    val primaryBand: String = "n78 (3.5 GHz Mid-Band)",
    val caStatus: String = "2CC Active (n78 + n28)",
    val simSlot: Int = 1,
    val operatorCode: String = "Cellular Radio",
    val isDataRoaming: Boolean = false,
    val dataState: String = "Connected",
    val vonrEnabled: Boolean = true
)

data class WifiTelemetry(
    val isConnected: Boolean = true,
    val ssid: String = "Apple-Studio-5G",
    val frequencyBand: String = "5.0 GHz (Wi-Fi 6)",
    val linkSpeedMbps: Int = 866,
    val ipAddress: String = "192.168.1.108",
    val signalStrengthPercent: Int = 92
)

data class PingMetrics(
    val isRunning: Boolean = false,
    val targetHost: String = "1.1.1.1",
    val targetName: String = "Cloudflare Edge DNS",
    val currentMs: Long = 18,
    val minMs: Long = 14,
    val maxMs: Long = 26,
    val avgMs: Long = 17,
    val jitterMs: Long = 2,
    val packetLossPercent: Int = 0,
    val pingHistory: List<Float> = listOf(19f, 17f, 18f, 15f, 22f, 16f, 18f, 17f, 24f, 16f, 15f, 18f)
)

data class CellTowerInfo(
    val id: String,
    val name: String,
    val operator: String,
    val type: String, // "5G NR SA", "5G NSA", "4G LTE+"
    val band: String, // "n78 (3500 MHz)"
    val cellId: Long,
    val pci: Int, // Physical Cell ID
    val tac: Int, // Tracking Area Code
    val rsrpDbm: Int,
    val sinr: String,
    val lat: Double,
    val lng: Double,
    val distanceMeters: Int,
    val azimuthDeg: Float,
    val isConnected: Boolean,
    val heightMeters: Int = 36,
    val coverageRadiusMeters: Int = 900
)

data class DiagnosticReport(
    val isRootAvailable: Boolean = false,
    val rootUid: String = "Unavailable",
    val isShizukuAvailable: Boolean = false,
    val isShizukuBound: Boolean = false,
    val shizukuVersion: Int = 0,
    val manufacturer: String = "Unknown",
    val model: String = "Android Device",
    val androidSdk: Int = 35,
    val androidRelease: String = "15",
    val basebandVersion: String = "Qualcomm Snapdragon X75 / Exynos 5G",
    val diagnosticSummary: String = "Device verified for privileged radio inspection."
)
