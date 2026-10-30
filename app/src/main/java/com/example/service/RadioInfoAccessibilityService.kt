package com.example.service

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class RadioInfoAccessibilityService : AccessibilityService() {

    companion object {
        var targetModeKeyword: String? = null
        var isAutoSwitchRequested: Boolean = false

        fun requestAutoSwitch(keyword: String) {
            targetModeKeyword = keyword
            isAutoSwitchRequested = true
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private var isSearchingDropdown = false

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!isAutoSwitchRequested || targetModeKeyword == null) return
        val rootNode = rootInActiveWindow ?: return

        try {
            val target = targetModeKeyword ?: return

            // Step 1: Check if the spinner popup is already open
            val targetNodes = rootNode.findAccessibilityNodeInfosByText(target)
            if (targetNodes.isNotEmpty()) {
                for (node in targetNodes) {
                    if (node.isClickable || node.parent?.isClickable == true) {
                        val toClick = if (node.isClickable) node else node.parent
                        toClick?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        isAutoSwitchRequested = false
                        isSearchingDropdown = false
                        showSuccess("✓ 1-Click Auto Switched to $target!")
                        // Return back to app automatically after 600ms
                        handler.postDelayed({
                            performGlobalAction(GLOBAL_ACTION_BACK)
                        }, 600)
                        return
                    }
                }
            }

            // Step 2: If popup is not yet open, find the "Set Preferred Network Type" spinner
            if (!isSearchingDropdown) {
                val labelNodes = rootNode.findAccessibilityNodeInfosByText("Set Preferred Network Type")
                if (labelNodes.isNotEmpty()) {
                    // Find neighbor spinner
                    findAndClickSpinner(rootNode)
                    isSearchingDropdown = true
                }
            }
        } catch (_: Exception) {}
    }

    private fun findAndClickSpinner(node: AccessibilityNodeInfo) {
        if (node.className?.toString()?.contains("Spinner", ignoreCase = true) == true) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            return
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            findAndClickSpinner(child)
        }
    }

    private fun showSuccess(msg: String) {
        handler.post {
            Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
        }
    }

    override fun onInterrupt() {}
}
