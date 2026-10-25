package com.bl4ckswordsman.cerberustiles.models

/**
 * Predefined screen brightness presets for quick one-tap adjustments.
 *
 * @property label User-facing display text.
 * @property level Normalized float value (0.0f..1.0f) passed to brightness utilities.
 * @property contentDescription Accessibility description for screen readers.
 */
enum class BrightnessPreset(
    val label: String,
    val level: Float,
    val contentDescription: String
) {
    /** Minimum screen brightness (~0%). */
    MIN("Min", 0f, "Minimum brightness"),

    /** Balanced comfortable indoor screen brightness (~40-50%). */
    BALANCED("Balanced", 0.75f, "Balanced brightness"),

    /** Maximum screen brightness (100%). */
    MAX("Max", 1f, "Maximum brightness");

    companion object {
        /**
         * Resolves the closest [BrightnessPreset] for a raw system brightness integer (0..255).
         */
        fun fromBrightness(brightness: Int): BrightnessPreset {
            return when {
                brightness <= 30 -> MIN
                brightness >= 200 -> MAX
                else -> BALANCED
            }
        }
    }
}
