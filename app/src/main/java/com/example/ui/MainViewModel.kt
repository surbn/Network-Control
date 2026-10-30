package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import com.example.service.NetworkTelemetryManager
import com.example.service.RootNetworkController
import com.example.service.StatusBarIconService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    DASHBOARD("Control"),
    MAP("Tower Map"),
    TELEMETRY("Telemetry"),
    SPEED_PING("Ping"),
    TOOLS("Settings")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val telemetryManager = NetworkTelemetryManager(application.applicationContext)

    val cellularState = telemetryManager.cellularState
    val wifiState = telemetryManager.wifiState
    val pingState = telemetryManager.pingState
    val cellTowers = telemetryManager.cellTowers

    private val _diagnosticState = MutableStateFlow(DiagnosticReport())
    val diagnosticState: StateFlow<DiagnosticReport> = _diagnosticState.asStateFlow()

    private val _selectedMode = MutableStateFlow(NetworkMode.AUTO)
    val selectedMode: StateFlow<NetworkMode> = _selectedMode.asStateFlow()

    private val _isApplying = MutableStateFlow(false)
    val isApplying: StateFlow<Boolean> = _isApplying.asStateFlow()

    private val _applyResult = MutableStateFlow("")
    val applyResult: StateFlow<String> = _applyResult.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(true)
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled.asStateFlow()

    private val _statusBarIconEnabled = MutableStateFlow(true)
    val statusBarIconEnabled: StateFlow<Boolean> = _statusBarIconEnabled.asStateFlow()

    private val _selectedStatusBarIcon = MutableStateFlow(StatusBarIconType.FIVE_G)
    val selectedStatusBarIcon: StateFlow<StatusBarIconType> = _selectedStatusBarIcon.asStateFlow()

    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    init {
        refreshAll()
        syncStatusBar()
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun selectMode(mode: NetworkMode) {
        _selectedMode.value = mode
        syncStatusBar()
    }

    fun setHapticEnabled(enabled: Boolean) {
        _hapticEnabled.value = enabled
    }

    fun setStatusBarIconEnabled(enabled: Boolean) {
        _statusBarIconEnabled.value = enabled
        if (enabled) syncStatusBar()
        else StatusBarIconService.stopStatusBarIcon(getApplication())
    }

    fun setStatusBarIcon(iconType: StatusBarIconType) {
        _selectedStatusBarIcon.value = iconType
        if (_statusBarIconEnabled.value) {
            syncStatusBar()
        }
    }

    fun syncStatusBar() {
        if (!_statusBarIconEnabled.value) return
        val currentCellular = cellularState.value
        val currentPing = pingState.value
        StatusBarIconService.updateStatusBarIcon(
            context = getApplication(),
            iconType = _selectedStatusBarIcon.value,
            mode = _selectedMode.value,
            carrier = currentCellular.carrierName,
            pingMs = currentPing.currentMs
        )
    }

    fun refreshAll() {
        telemetryManager.refreshTelemetry()
        viewModelScope.launch {
            _diagnosticState.value = RootNetworkController.getDiagnostics()
            syncStatusBar()
        }
    }

    /**
     * Automatic One-Click Mode Switcher:
     * Executes via Root, Shizuku ADB, or arms the Accessibility Auto-Switcher and launches Radio Info!
     */
    fun applySelectedModeOneClick(context: Context) {
        viewModelScope.launch {
            _isApplying.value = true
            val mode = _selectedMode.value
            val result = RootNetworkController.applyModeOneClick(context, mode)
            _applyResult.value = result
            _isApplying.value = false
            telemetryManager.refreshTelemetry()
            syncStatusBar()
        }
    }

    fun applySelectedMode() {
        viewModelScope.launch {
            _isApplying.value = true
            val mode = _selectedMode.value
            val result = RootNetworkController.applyMode(mode)
            _applyResult.value = result
            _isApplying.value = false
            telemetryManager.refreshTelemetry()
            syncStatusBar()
        }
    }

    fun startPing(host: String = "1.1.1.1", name: String = "Cloudflare Edge") {
        telemetryManager.startLivePing(host, name)
    }

    fun stopPing() {
        telemetryManager.stopPing()
    }

    override fun onCleared() {
        super.onCleared()
        telemetryManager.stopPing()
    }
}
