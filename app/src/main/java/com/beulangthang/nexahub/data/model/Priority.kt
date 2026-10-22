package com.beulangthang.nexahub.data.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.beulangthang.nexahub.ui.util.LocalAppStrings

enum class Priority(
    val defaultLabel: String,
    val color: Color,
    val value: Int
) {
    LOW(defaultLabel = "Thấp", color = Color(0xFF4CAF50), value = 1),
    MEDIUM(defaultLabel = "Trung bình", color = Color(0xFFFF9800), value = 2),
    HIGH(defaultLabel = "Cao", color = Color(0xFFE53935), value = 3);

    val label: String
        @Composable
        get() {
            val strings = LocalAppStrings.current
            return when (this) {
                LOW -> strings.priorityLow
                MEDIUM -> strings.priorityMedium
                HIGH -> strings.priorityHigh
            }
        }

    companion object {
        fun fromValue(value: Int): Priority {
            return entries.find { it.value == value } ?: MEDIUM
        }
    }
}
