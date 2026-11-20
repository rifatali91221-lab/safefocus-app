package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.MainActivity
import com.example.data.bridge.BridgeTriggerEvent
import com.example.data.bridge.SafeFocusBridgeBus
import com.example.data.local.SafeFocusDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SafeFocusAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastTriggerTimestamp = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (pkg != SafeFocusBridgeBus.PKG_YOUTUBE &&
            pkg != SafeFocusBridgeBus.PKG_FACEBOOK &&
            pkg != SafeFocusBridgeBus.PKG_INSTAGRAM
        ) {
            return
        }

        val now = System.currentTimeMillis()
        if (now - lastTriggerTimestamp < 2500L) return

        val rootNode = rootInActiveWindow
        val detectedReelsOrShorts = isReelsOrShortsVisible(pkg, rootNode, event)

        serviceScope.launch {
            val dao = SafeFocusDatabase.getInstance(applicationContext).safeFocusDao()
            val settings = dao.getSettingsSnapshot() ?: return@launch
            if (!settings.isMasterActive) return@launch

            val app = dao.getApp(pkg) ?: return@launch
            val isRenewActive = !app.isBlocked && app.renewExpiresAtMillis > now
            if (isRenewActive) return@launch

            val shouldBlockApp = app.isBlocked
            val shouldBlockReels = app.blockReelsAndShorts && detectedReelsOrShorts

            if (shouldBlockApp || shouldBlockReels) {
                lastTriggerTimestamp = now
                val reason = if (shouldBlockReels) {
                    "${app.displayName} ${app.reelsLabel} শনাক্ত হয়েছে"
                } else {
                    "${app.displayName} বর্তমানে ব্লক করা আছে"
                }

                SafeFocusBridgeBus.emitTrigger(
                    BridgeTriggerEvent(
                        packageName = app.packageName,
                        appName = app.displayName,
                        reason = reason,
                        isReelsOrShorts = shouldBlockReels
                    )
                )

                val launchIntent = Intent(applicationContext, MainActivity::class.java).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                    )
                    putExtra(EXTRA_TRIGGER_PACKAGE, app.packageName)
                    putExtra(EXTRA_TRIGGER_APP_NAME, app.displayName)
                    putExtra(EXTRA_TRIGGER_REASON, reason)
                }
                startActivity(launchIntent)
            }
        }
    }

    override fun onInterrupt() {
        // সার্ভিস ইন্টারাপ্ট হলে নিরাপদে হ্যান্ডল করা হয়
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun isReelsOrShortsVisible(
        packageName: String,
        root: AccessibilityNodeInfo?,
        event: AccessibilityEvent
    ): Boolean {
        val eventText = event.text.joinToString(" ").lowercase()
        val keywords = when (packageName) {
            SafeFocusBridgeBus.PKG_YOUTUBE -> listOf("shorts", "reel_player", "short_video")
            SafeFocusBridgeBus.PKG_FACEBOOK -> listOf("reels", "fb_reels", "watch_reels")
            SafeFocusBridgeBus.PKG_INSTAGRAM -> listOf("reels", "clips_viewer", "clips")
            else -> emptyList()
        }
        if (keywords.any { eventText.contains(it) }) return true
        return scanNodeTree(root, keywords, depth = 0)
    }

    private fun scanNodeTree(
        node: AccessibilityNodeInfo?,
        keywords: List<String>,
        depth: Int
    ): Boolean {
        if (node == null || depth > 6) return false
        val viewId = node.viewIdResourceName?.lowercase().orEmpty()
        val desc = node.contentDescription?.toString()?.lowercase().orEmpty()
        val text = node.text?.toString()?.lowercase().orEmpty()

        if (keywords.any { kw ->
                viewId.contains(kw) || desc.contains(kw) || text == kw
            }
        ) {
            return true
        }

        for (i in 0 until node.childCount) {
            if (scanNodeTree(node.getChild(i), keywords, depth + 1)) {
                return true
            }
        }
        return false
    }

    companion object {
        const val EXTRA_TRIGGER_PACKAGE = "extra_trigger_package"
        const val EXTRA_TRIGGER_APP_NAME = "extra_trigger_app_name"
        const val EXTRA_TRIGGER_REASON = "extra_trigger_reason"
    }
}
