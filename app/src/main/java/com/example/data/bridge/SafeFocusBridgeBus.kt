package com.example.data.bridge

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

data class BridgeTriggerEvent(
    val packageName: String,
    val appName: String,
    val reason: String,
    val isReelsOrShorts: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class BridgeStatusSnapshot(
    val isMasterActive: Boolean = false,
    val overlayEnabled: Boolean = false,
    val blockedAppsCount: Int = 0,
    val lastDetectedEvent: String = "কোনো ইভেন্ট নেই"
)

object SafeFocusBridgeBus {
    const val PKG_YOUTUBE = "com.google.android.youtube"
    const val PKG_FACEBOOK = "com.facebook.katana"
    const val PKG_INSTAGRAM = "com.instagram.android"

    private val _triggerEvents = MutableSharedFlow<BridgeTriggerEvent>(extraBufferCapacity = 8)
    val triggerEvents: SharedFlow<BridgeTriggerEvent> = _triggerEvents.asSharedFlow()

    private val _status = MutableStateFlow(BridgeStatusSnapshot())
    val status: StateFlow<BridgeStatusSnapshot> = _status.asStateFlow()

    private val _toggleMasterRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 4)
    val toggleMasterRequests: SharedFlow<Unit> = _toggleMasterRequests.asSharedFlow()

    fun emitTrigger(event: BridgeTriggerEvent) {
        _status.value = _status.value.copy(
            lastDetectedEvent = "${event.appName}: ${event.reason}"
        )
        _triggerEvents.tryEmit(event)
    }

    fun updateStatus(
        isMasterActive: Boolean,
        overlayEnabled: Boolean,
        blockedAppsCount: Int
    ) {
        _status.value = _status.value.copy(
            isMasterActive = isMasterActive,
            overlayEnabled = overlayEnabled,
            blockedAppsCount = blockedAppsCount
        )
    }

    fun requestToggleMasterFromOverlay() {
        _toggleMasterRequests.tryEmit(Unit)
    }
}
