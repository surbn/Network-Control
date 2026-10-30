package com.example

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.example.model.NetworkMode
import com.example.service.RootNetworkController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NetworkTileService : TileService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var is5GForced = false

    override fun onStartListening() {
        super.onStartListening()
        updateTileUI()
    }

    private fun updateTileUI() {
        qsTile?.apply {
            label = "5G Switch Pro"
            subtitle = if (is5GForced) "5G NR Only" else "5G/4G Auto"
            state = if (is5GForced) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        is5GForced = !is5GForced
        scope.launch {
            val targetMode = if (is5GForced) NetworkMode.NR_ONLY else NetworkMode.AUTO
            RootNetworkController.applyMode(targetMode)
            withContext(Dispatchers.Main) {
                updateTileUI()
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
