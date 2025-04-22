package com.crossevol.wordbook.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications // Using Notifications for Review for now
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar // Use NavigationBar for M3 bottom nav
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.crossevol.wordbook.data.model.FilterOption // Import FilterOption
import com.crossevol.wordbook.data.model.WordItem
import com.crossevol.wordbook.ui.components.FilterDropdownMenu // Import FilterDropdownMenu
import com.crossevol.wordbook.ui.components.WordListItem
import com.crossevol.wordbook.ui.components.sampleWordItem
import org.jetbrains.compose.ui.tooling.preview.Preview


// Sample data for the home page list
val sampleWordList = listOf(
    sampleWordItem,
    sampleWordItem.copy(id = 2L, title = "你好世界",  rating = 4, sentences = listOf("这是一个中文例子。", "另一个例子。")),
    sampleWordItem.copy(id = 3L, title = "こんにちは世界",  rating = 2, sentences = listOf("これは日本語の例です。")),
    sampleWordItem.copy(id = 4L, title = "Bonjour le monde",  rating = 5, sentences = listOf("Ceci est un exemple français.")),
    sampleWordItem.copy(id = 5L, title = "Hallo Welt",  rating = 1, sentences = listOf("Dies ist ein deutsches Beispiel."))
)

// Data class to represent bottom navigation items
data class BottomNavItem(val label: String, val icon: ImageVector, val route: String)

// Define bottom navigation items
val bottomNavItems = listOf(
    BottomNavItem("Browse", Icons.Filled.Home, "home"),
    BottomNavItem("Review", Icons.Filled.Notifications, "review"), // Placeholder icon
    BottomNavItem("Settings", Icons.Filled.Settings, "settings")
)


/**
 * The main Home Page screen composable using Material 3.
 * Includes TopAppBar, Bottom Navigation Bar, and a list of WordListItems.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    words: List<WordItem> = sampleWordList, // Default to sample data
    // onFilterClick: () -> Unit = {}, // Filter click is handled internally now
    onWordItemClick: (WordItem) -> Unit, // Changed: Make this non-optional for navigation
    onNavigate: (String) -> Unit = {} // Callback for bottom navigation
) {
    // State for tracking the selected bottom navigation item index
    var selectedItemIndex by remember { mutableStateOf(0) }
    // State for filter dropdown visibility
    var showFilterMenu by remember { mutableStateOf(false) }
    // State for selected filter options - Default to ALL
    var selectedFilters by remember { mutableStateOf(setOf(FilterOption.ALL)) }

    // Logic to handle filter option toggles
    val onFilterOptionToggle = { option: FilterOption, isSelected: Boolean ->
        selectedFilters = when (option) {
            FilterOption.ALL -> setOf(FilterOption.ALL) // Selecting ALL overrides others
            FilterOption.NONE -> setOf(FilterOption.NONE) // Selecting NONE overrides others
            else -> {
                // If toggling an individual option, remove ALL/NONE and update the set
                val currentFilters = selectedFilters.toMutableSet()
                currentFilters.remove(FilterOption.ALL)
                currentFilters.remove(FilterOption.NONE)
                if (isSelected) {
                    currentFilters.add(option)
                } else {
                    currentFilters.remove(option)
                }
                // If all individuals are selected, maybe switch to ALL? (Optional enhancement)
                // If no individuals are selected, maybe switch to NONE? (Optional enhancement)
                 if (currentFilters.isEmpty()) setOf(FilterOption.NONE) else currentFilters // Default to NONE if empty
            }
        }
         // Close the menu after selection? Maybe not, allow multiple toggles.
         // showFilterMenu = false
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Browse") },
                actions = {
                    // Box to anchor the DropdownMenu
                    androidx.compose.foundation.layout.Box {
                        IconButton(onClick = { showFilterMenu = true }) { // Open the menu
                            Icon(
                                imageVector = Icons.Filled.List,
                                contentDescription = "Filter words"
                            )
                        }
                        // The Filter Dropdown Menu itself
                        FilterDropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false },
                            selectedOptions = selectedFilters,
                            onOptionToggle = onFilterOptionToggle
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Use primary color from theme
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            // Use NavigationBar for Material 3 bottom navigation
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer, // Example color
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selectedItemIndex == index,
                        onClick = {
                            selectedItemIndex = index
                            onNavigate(item.route) // Trigger navigation callback
                        },
                        // Customize colors using NavigationBarItemDefaults if needed
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer // Color behind selected item
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        // Call the extracted WordList composable
        WordList(
            words = words,
            visibleFilters = selectedFilters,
            onWordItemClick = onWordItemClick,
            modifier = Modifier.padding(innerPadding) // Apply padding from Scaffold
        )
    }
}

/**
 * Displays the list of word items using a LazyColumn.
 *
 * @param words The list of [WordItem] to display.
 * @param visibleFilters The set of [FilterOption] to apply to each item.
 * @param onWordItemClick Callback invoked when a word item is clicked.
 * @param modifier Modifier for the LazyColumn.
 */
@Composable
fun WordList(
    words: List<WordItem>,
    visibleFilters: Set<FilterOption>,
    onWordItemClick: (WordItem) -> Unit, // Pass the click handler down
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(words, key = { it.id }) { word -> // Use word id as key for better performance
            WordListItem(
                item = word,
                visibleFilters = visibleFilters, // Pass the selected filters
                // Apply the clickable modifier here, calling the passed lambda
                modifier = Modifier.clickable { onWordItemClick(word) }
            )
        }
    }
}


@Preview
@Composable
fun HomePagePreview() {
    MaterialTheme { // Ensure MaterialTheme is applied for preview
        // Provide a dummy lambda for the preview
        HomePage(onWordItemClick = {})
    }
}
