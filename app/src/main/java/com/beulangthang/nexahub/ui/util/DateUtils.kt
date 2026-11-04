package com.beulangthang.nexahub.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun formatDate(timestamp: Long?): String {
        if (timestamp == null) return ""
        return dateFormatter.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long?): String {
        if (timestamp == null) return ""
        return "${dateFormatter.format(Date(timestamp))} ${timeFormatter.format(Date(timestamp))}"
    }

    fun isOverdue(timestamp: Long?): Boolean {
        if (timestamp == null) return false
        val now = System.currentTimeMillis()
        return timestamp < now
    }
}
