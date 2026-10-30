package com.bl4ckswordsman.cerberustiles.data

import android.content.Context
import android.content.pm.PackageManager
import com.bl4ckswordsman.cerberustiles.SettingsUtils

/**
 * Represents a single permission required by Cerberus Tiles, its associated features,
 * rationale, and grant mechanism.
 */
data class PermissionItem(
    val title: String,
    val feature: String,
    val why: String,
    val howToGrant: String,
    val isGranted: (Context) -> Boolean = { true },
    val onGrantClick: ((Context) -> Unit)? = null
)

/**
 * Represents a question and answer item for the permissions and privacy FAQ.
 */
data class FaqItem(
    val question: String,
    val answer: String
)

/**
 * The single source of truth for all permissions and privacy documentation.
 * Used to render the in-app [com.bl4ckswordsman.cerberustiles.ui.PermissionsScreen]
 * and to automatically synchronize the README documentation via unit tests.
 */
object PermissionsData {

    val permissions: List<PermissionItem> = listOf(
        PermissionItem(
            title = "Write System Settings",
            feature = "Adaptive Brightness, Ringer Mode",
            why = "Toggles device brightness mode and ringer/vibration states.",
            howToGrant = "Standard system settings screen",
            isGranted = { SettingsUtils.canWriteSettings(it) },
            onGrantClick = { SettingsUtils.openPermissionSettings(it) }
        ),
        PermissionItem(
            title = "Notification Policy (DND)",
            feature = "Vibration Ringer Mode",
            why = "Prevents system crashes when switching ringer mode while Do Not Disturb is active.",
            howToGrant = "Standard system settings screen",
            isGranted = { SettingsUtils.canAccessNotificationPolicy(it) },
            onGrantClick = { SettingsUtils.openDndPermissionSettings(it) }
        ),
        PermissionItem(
            title = "Write Secure Settings",
            feature = "Limit Charge to 80%",
            why = "Modifies the system charge_optimization_mode setting on Pixel devices.",
            howToGrant = "One-time ADB command",
            isGranted = { context ->
                context.checkSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS) ==
                        PackageManager.PERMISSION_GRANTED
            },
            onGrantClick = null
        ),
        PermissionItem(
            title = "Accessibility Service",
            feature = "Smart Pixels",
            why = "Draws a hardware-accelerated true-black pixel grid over the display without blocking touch interaction.",
            howToGrant = "Accessibility Settings",
            isGranted = { SettingsUtils.SmartPixels.isAccessibilityServiceEnabled(it) },
            onGrantClick = { SettingsUtils.SmartPixels.openAccessibilitySettings(it) }
        )
    )

    val faqs: List<FaqItem> = listOf(
        FaqItem(
            question = "Why does Smart Pixels require an Accessibility Service?",
            answer = "To save battery on OLED displays, Smart Pixels renders an overlay of blacked-out pixels across the screen.\n" +
                    "- Using standard screen overlays (`TYPE_APPLICATION_OVERLAY`) causes Android to block touch inputs in secure apps (like GitHub, banking apps, and system dialogs) due to tapjacking protection (`filterTouchesWhenObscured`).\n" +
                    "- An Accessibility Overlay (`TYPE_ACCESSIBILITY_OVERLAY`) is treated by Android as a trusted visual layer, allowing 100% transparent touch pass-through across all apps and system bars."
        ),
        FaqItem(
            question = "Why does Android warn that the app has \"full control of your device\"?",
            answer = "This is Android's mandatory, static system warning shown for *all* accessibility services regardless of their actual declared capabilities. Cerberus Tiles' service is strictly hardened:\n" +
                    "- `canRetrieveWindowContent=\"false\"`: Cannot read screen content, text, or passwords.\n" +
                    "- `canPerformGestures=\"false\"`: Cannot simulate clicks, gestures, or taps.\n" +
                    "- `canRequestFilterKeyEvents=\"false\"`: Cannot log or intercept keystrokes.\n" +
                    "- `canRequestTouchExplorationMode=\"false\"`: Cannot intercept touch exploration.\n" +
                    "- **Zero Event Types**: Does not receive or listen to window or UI events."
        ),
        FaqItem(
            question = "How do I grant the Charging Optimization permission?",
            answer = "The 80% charging limit modifies an Android secure system setting, which requires granting permission once via ADB:\n" +
                    "```bash\n" +
                    "adb shell pm grant com.bl4ckswordsman.cerberustiles android.permission.WRITE_SECURE_SETTINGS\n" +
                    "```"
        ),
        FaqItem(
            question = "Does Cerberus Tiles collect any personal data?",
            answer = "No. The app does not request `INTERNET` permission in its manifest and has no ability to send data anywhere. Everything runs strictly on your device."
        )
    )

    /**
     * Converts the permissions and FAQ data into clean GitHub-flavored markdown.
     */
    fun toMarkdown(): String = buildString {
        appendLine("## Permissions & Privacy")
        appendLine()
        appendLine("Cerberus Tiles operates with **zero internet access**, **zero analytics**, and **zero tracking**. All permissions are strictly optional and only prompted when you choose to enable the corresponding feature.")
        appendLine()
        appendLine("| Permission | Feature | Why It Is Needed | How It Is Granted |")
        appendLine("| :--- | :--- | :--- | :--- |")
        for (item in permissions) {
            appendLine("| **${item.title}** | ${item.feature} | ${item.why} | ${item.howToGrant} |")
        }
        appendLine()
        appendLine("### Frequently Asked Questions")
        appendLine()
        for (faq in faqs) {
            appendLine("<details>")
            appendLine("  <summary><strong>${faq.question}</strong></summary>")
            appendLine()
            val indentedAnswer = faq.answer.lines().joinToString("\n") { line ->
                if (line.isBlank()) "" else "  $line"
            }
            appendLine(indentedAnswer)
            appendLine("</details>")
            appendLine()
        }
    }.trimEnd()
}
