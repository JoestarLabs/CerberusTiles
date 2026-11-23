package com.bl4ckswordsman.cerberustiles.util

import android.annotation.SuppressLint
import android.content.Context
import com.bl4ckswordsman.cerberustiles.models.NetworkSpeedLimit
import java.util.Locale

/**
 * Repository responsible for providing the available network speed limitation options.
 *
 * It dynamically discovers the localized entries and values defined in Android's
 * `com.android.settings` package (`ingress_rate_limit_entries` and `ingress_rate_limit_values`),
 * falling back gracefully to [NetworkSpeedLimit.DEFAULT_PRESETS] if the system resources cannot
 * be loaded (e.g. customized OEM ROMs, older Android versions, or missing resources).
 */
object NetworkSpeedLimitRepository {
    private const val SETTINGS_PACKAGE = "com.android.settings"
    private const val ARRAY_ENTRIES = "ingress_rate_limit_entries"
    private const val ARRAY_VALUES = "ingress_rate_limit_values"

    @Volatile
    private var cachedLimits: List<NetworkSpeedLimit>? = null

    @Volatile
    private var cachedLocaleTag: String? = null

    /**
     * Returns the available network speed limits for this device.
     */
    fun getAvailableLimits(context: Context): List<NetworkSpeedLimit> {
        val currentLocale = Locale.getDefault().toLanguageTag()
        val cached = cachedLimits
        if (cached != null && cachedLocaleTag == currentLocale) return cached

        synchronized(this) {
            val rechecked = cachedLimits
            if (rechecked != null && cachedLocaleTag == currentLocale) return rechecked

            val resolved = resolveFromSystemSettings(context)
            val result =
                if (!resolved.isNullOrEmpty()) resolved else NetworkSpeedLimit.DEFAULT_PRESETS
            cachedLimits = result
            cachedLocaleTag = currentLocale
            return result
        }
    }

    /**
     * Clears cached limits, forcing re-resolution on next access.
     */
    fun clearCache() {
        cachedLimits = null
        cachedLocaleTag = null
    }

    /**
     * Attempts dynamic resolution from the host device's Settings package resources.
     */
    @SuppressLint("DiscouragedApi") // Cross-package lookup in com.android.settings has no compile-time R.array ID
    internal fun resolveFromSystemSettings(context: Context): List<NetworkSpeedLimit>? {
        return try {
            val pm = context.packageManager
            val settingsRes = pm.getResourcesForApplication(SETTINGS_PACKAGE)
            val entriesId = settingsRes.getIdentifier(ARRAY_ENTRIES, "array", SETTINGS_PACKAGE)
            val valuesId = settingsRes.getIdentifier(ARRAY_VALUES, "array", SETTINGS_PACKAGE)

            if (entriesId == 0 || valuesId == 0) return null

            val entries = settingsRes.getTextArray(entriesId)
            val values = settingsRes.getStringArray(valuesId)

            if (entries.isEmpty() || entries.size != values.size) return null

            val limits = entries.indices.mapNotNull { i ->
                val bytes = values[i].toLongOrNull() ?: return@mapNotNull null
                NetworkSpeedLimit(label = entries[i].toString(), bytesPerSecond = bytes)
            }
            if (limits.isEmpty()) null else limits
        } catch (_: Exception) {
            null
        }
    }
}
