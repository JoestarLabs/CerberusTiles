package com.bl4ckswordsman.cerberustiles.models

import java.util.Locale

/**
 * Represents a network download speed limitation option natively managed by Android's
 * Developer Options (`Settings.Global.INGRESS_RATE_LIMIT_BYTES_PER_SECOND`).
 *
 * @property label The user-facing display name of this limit (e.g., "No limit", "1 Mbps").
 * @property bytesPerSecond The limit in bytes per second. Values <= 0 indicate throttling is disabled.
 */
data class NetworkSpeedLimit(
    val label: String,
    val bytesPerSecond: Long
) {
    /** True if this option disables network rate limiting. */
    val isUnlimited: Boolean
        get() = bytesPerSecond <= 0L

    companion object {
        const val NO_LIMIT_BYTES = -1L

        val NO_LIMIT = NetworkSpeedLimit(label = "No limit", bytesPerSecond = NO_LIMIT_BYTES)
        val LIMIT_128K = NetworkSpeedLimit(label = "128 kbps", bytesPerSecond = 16_000L)
        val LIMIT_256K = NetworkSpeedLimit(label = "256 kbps", bytesPerSecond = 32_000L)
        val LIMIT_1M = NetworkSpeedLimit(label = "1 Mbps", bytesPerSecond = 125_000L)
        val LIMIT_5M = NetworkSpeedLimit(label = "5 Mbps", bytesPerSecond = 625_000L)
        val LIMIT_15M = NetworkSpeedLimit(label = "15 Mbps", bytesPerSecond = 1_875_000L)

        /**
         * Standard AOSP presets corresponding to `ingress_rate_limit_entries` and
         * `ingress_rate_limit_values` in `com.android.settings`.
         */
        val DEFAULT_PRESETS: List<NetworkSpeedLimit> = listOf(
            NO_LIMIT,
            LIMIT_128K,
            LIMIT_256K,
            LIMIT_1M,
            LIMIT_5M,
            LIMIT_15M
        )

        /**
         * Formats a raw bytes/s rate into a clean, human-readable bandwidth label.
         */
        fun formatBytesPerSecond(bytes: Long): String {
            if (bytes <= 0L) return "No limit"
            val bitsPerSec = bytes * 8
            return when {
                bitsPerSec >= 1_000_000_000 -> {
                    val gbps = bitsPerSec / 1_000_000_000.0
                    if (gbps == gbps.toLong().toDouble()) "${gbps.toLong()} Gbps"
                    else String.format(Locale.US, "%.1f Gbps", gbps)
                }
                bitsPerSec >= 1_000_000 -> {
                    val mbps = bitsPerSec / 1_000_000.0
                    if (mbps == mbps.toLong().toDouble()) "${mbps.toLong()} Mbps"
                    else String.format(Locale.US, "%.1f Mbps", mbps)
                }
                bitsPerSec >= 1_000 -> {
                    val kbps = bitsPerSec / 1_000.0
                    if (kbps == kbps.toLong().toDouble()) "${kbps.toLong()} kbps"
                    else String.format(Locale.US, "%.1f kbps", kbps)
                }
                else -> "$bitsPerSec bps"
            }
        }

        /**
         * Resolves a [NetworkSpeedLimit] matching [bytes], falling back to synthesizing
         * a custom limit if not matched in [presets].
         */
        fun fromBytesPerSecond(
            bytes: Long,
            presets: List<NetworkSpeedLimit> = DEFAULT_PRESETS
        ): NetworkSpeedLimit {
            if (bytes <= 0L) {
                return presets.firstOrNull { it.isUnlimited } ?: NO_LIMIT
            }
            return presets.firstOrNull { it.bytesPerSecond == bytes }
                ?: NetworkSpeedLimit(label = formatBytesPerSecond(bytes), bytesPerSecond = bytes)
        }
    }
}
