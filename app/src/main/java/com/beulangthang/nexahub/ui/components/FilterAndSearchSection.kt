package com.beulangthang.nexahub.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beulangthang.nexahub.data.model.Category
import com.beulangthang.nexahub.data.model.Priority
import com.beulangthang.nexahub.ui.util.LocalAppStrings
import com.beulangthang.nexahub.ui.viewmodel.FilterTab
import com.beulangthang.nexahub.ui.viewmodel.SortOption

@Composable
fun FilterAndSearchSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    activeTab: FilterTab,
    onTabSelected: (FilterTab) -> Unit,
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit,
    selectedPriority: Priority?,
    onPrioritySelected: (Priority?) -> Unit,
    selectedSortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var isSortMenuExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Search & Sort Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(strings.searchPlaceholder) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Sort Menu Button
            Row {
                IconButton(
                    onClick = { isSortMenuExpanded = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = strings.sortLabel,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                DropdownMenu(
                    expanded = isSortMenuExpanded,
                    onDismissRequest = { isSortMenuExpanded = false }
                ) {
                    SortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                onSortOptionSelected(option)
                                isSortMenuExpanded = false
                            },
                            leadingIcon = {
                                if (selectedSortOption == option) {
                                    Text("✓", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status Filter Tabs
        SecondaryTabRow(
            selectedTabIndex = activeTab.ordinal,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            FilterTab.entries.forEach { tab ->
                Tab(
                    selected = activeTab == tab,
                    onClick = { onTabSelected(tab) },
                    text = { Text(tab.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Category Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Category.entries.forEach { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category.label) },
                    leadingIcon = {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = category.color.copy(alpha = 0.2f),
                        selectedLabelColor = category.color,
                        selectedLeadingIconColor = category.color
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Horizontal Priority Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${strings.priorityLabel}:",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.align(Alignment.CenterVertically),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Priority.entries.forEach { priority ->
                val isSelected = selectedPriority == priority
                FilterChip(
                    selected = isSelected,
                    onClick = { onPrioritySelected(priority) },
                    label = { Text(priority.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = priority.color.copy(alpha = 0.2f),
                        selectedLabelColor = priority.color
                    )
                )
            }
        }
    }
}
