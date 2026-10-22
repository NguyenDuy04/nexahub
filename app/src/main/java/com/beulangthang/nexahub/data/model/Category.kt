package com.beulangthang.nexahub.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.beulangthang.nexahub.ui.util.LocalAppStrings

enum class Category(
    val defaultLabel: String,
    val icon: ImageVector,
    val color: Color
) {
    WORK(
        defaultLabel = "Công việc",
        icon = Icons.Default.Work,
        color = Color(0xFF2196F3)
    ),
    PERSONAL(
        defaultLabel = "Cá nhân",
        icon = Icons.Default.Person,
        color = Color(0xFF9C27B0)
    ),
    SHOPPING(
        defaultLabel = "Mua sắm",
        icon = Icons.Default.ShoppingCart,
        color = Color(0xFFFF5722)
    ),
    HEALTH(
        defaultLabel = "Sức khỏe",
        icon = Icons.Default.Favorite,
        color = Color(0xFFE91E63)
    ),
    STUDY(
        defaultLabel = "Học tập",
        icon = Icons.AutoMirrored.Filled.MenuBook,
        color = Color(0xFF009688)
    ),
    OTHER(
        defaultLabel = "Khác",
        icon = Icons.Default.Category,
        color = Color(0xFF607D8B)
    );

    val label: String
        @Composable
        get() {
            val strings = LocalAppStrings.current
            return when (this) {
                WORK -> strings.categoryWork
                PERSONAL -> strings.categoryPersonal
                SHOPPING -> strings.categoryShopping
                HEALTH -> strings.categoryHealth
                STUDY -> strings.categoryStudy
                OTHER -> strings.categoryOther
            }
        }

    companion object {
        fun fromName(name: String): Category {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: OTHER
        }
    }
}
