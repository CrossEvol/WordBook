package com.crossevol.wordbook.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box // Import Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add // Import Add icon for FAB
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications // Using Notifications for Review for now
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu // Import DropdownMenu
import androidx.compose.material3.DropdownMenuItem // Import DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton // Import FAB
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
import androidx.compose.runtime.LaunchedEffect // Import LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode // Import for preview check
import androidx.compose.ui.graphics.vector.ImageVector
import com.crossevol.wordbook.data.model.FilterOption // Import FilterOption
import com.crossevol.wordbook.data.model.WordItemDB
import com.crossevol.wordbook.ui.components.FilterDropdownMenu // Import FilterDropdownMenu
import com.crossevol.wordbook.ui.components.WordListItem
import com.crossevol.wordbook.data.WordRepository // Import WordRepository
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.crossevol.wordbook.data.SettingsRepository // Import SettingsRepository
import com.crossevol.wordbook.data.model.WordItemUI
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {} // Add logger instance


// Sample data for the home page list (Manually extracted from JSONs)
val sampleWordListEN = listOf(
    WordItemUI(
        id = 1L,
        title = "同人志二次創作", // Original text as title
        explanation = "Fan-created doujinshi or secondary creation doujinshi; refers to self-published works (often comics or novels) that are creative adaptations or extensions of existing intellectual property (manga, anime, games, etc.), typically produced by fans.",
        sentences = listOf("I found a fascinating fan-created doujinshi at the convention.", "Many artists specialize in doujinshi secondary creation based on popular series."),
        relatedWords = listOf("doujinshi", "fan work", "fan fiction", "self-publishing", "circle"),
        pronunciation = "/doʊˈdʒɪnʃi ˌsɛkənˌdɛri kriˈeɪʃən/ (Concept pronunciation)",
        rating = 3L // Sample rating 0-5
    ),
    WordItemUI(
        id = 2L,
        title = "轻小说", // Original text as title
        explanation = "A style of Japanese novel primarily targeting young adults, often featuring illustrations and a simpler writing style.",
        sentences = listOf("I enjoy reading light novels in my spare time.", "Many popular anime series are adapted from light novels."),
        relatedWords = listOf("manga", "anime", "novel", "young adult fiction", "genre fiction"),
        pronunciation = "/laɪt ˈnɒvəl/",
        rating = 5L // Sample rating 0-5
    ),
    WordItemUI(
        id = 3L,
        title = "视觉小说", // Original text as title
        explanation = "An interactive fiction video game genre, primarily text-based with static or animated visuals, often featuring branching narratives.",
        sentences = listOf("Visual novels are known for their strong focus on story and characters.", "I spent hours playing that new visual novel with multiple endings."),
        relatedWords = listOf("interactive fiction", "dating sim", "adventure game", "story game", "VN"),
        pronunciation = "/ˈvɪʒuəl ˈnɒvəl/",
        rating = 1L // Sample rating 0-5
    )
)

val sampleWordListJA = listOf(
    WordItemUI(
        id = 1L,
        title = "同人志二次創作", // Original text as title
        explanation = "既存の作品を基にファンが制作し、同人誌の形式で発表される二次創作物のこと。漫画、小説、イラスト集など。",
        sentences = listOf("彼女は同人誌二次創作で有名なサークルに所属しています。", "このイベントでは様々なジャンルの同人誌二次創作が頒布されています。"),
        relatedWords = listOf("同人誌", "二次創作", "同人", "サークル", "ファンアート"),
        pronunciation = "どうじんし にじそうさく (dōjinshi nijisōsaku)",
        rating = 3L // Same sample rating
    ),
    WordItemUI(
        id = 2L,
        title = "轻小说", // Original text as title
        explanation = "イラストを多用し、主に中高生を対象とした小説のジャンル。",
        sentences = listOf("週末は家でライトノベルを読んで過ごすのが好きです。", "そのアニメは有名なライトノベルシリーズが原作です。"),
        relatedWords = listOf("漫画", "アニメ", "小説", "ラノベ", "文芸"),
        pronunciation = "ライトノベル (raito noberu)",
        rating = 5L // Same sample rating
    ),
    WordItemUI(
        id = 3L,
        title = "视觉小说", // Original text as title
        explanation = "静止画や動画、テキストを用いてストーリーを進める形式のゲームジャンル。プレイヤーの選択によって展開が変化するものが多い。",
        sentences = listOf("最近、感動的なビジュアルノベルをプレイして泣きました。", "このビジュアルノベルは選択肢が多くて面白いです。"),
        relatedWords = listOf("ノベルゲーム", "アドベンチャーゲーム", "ゲーム", "インタラクティブフィクション", "AVG"),
        pronunciation = "ビジュアルノベル (bijuaru noberu)",
        rating = 1L // Same sample rating
    )
)

val sampleWordListZH = listOf(
    WordItemUI(
        id = 1L,
        title = "同人志二次創作", // Original text as title
        explanation = "基于现有作品（如动漫、游戏）进行创作并以同人志形式出版或分享的作品。特指粉丝创作的、非官方出版的二次创作物。",
        sentences = listOf("他创作了很多优秀的同人志二次创作作品。", "这本漫展上有很多关于热门IP的同人志二次创作。"),
        relatedWords = listOf("动漫", "漫画", "小说", "二次元", "文学"),
        pronunciation = "tóngrénzhì èrcì chuàngzuò",
        rating = 3L // Same sample rating
    ),
    WordItemUI(
        id = 2L,
        title = "轻小说", // Original text as title
        explanation = "一种源自日本的小说类型，主要面向青少年读者，常包含插图且文风相对轻松。",
        sentences = listOf("我喜欢在空闲时间读轻小说。", "很多受欢迎的动漫都是由轻小说改编的。", "另一个例子。"), // Added another example sentence
        relatedWords = listOf("动漫", "漫画", "小说", "二次元", "文学"),
        pronunciation = "qīng xiǎo shuō",
        rating = 5L // Sample rating 0-5
    ),
    WordItemUI(
        id = 3L,
        title = "视觉小说", // Original text as title
        explanation = "一种电子游戏类型，以文字叙述为主要表现形式，并搭配大量静态或动态图像，玩家通过选项影响剧情走向。",
        sentences = listOf("我喜欢玩剧情深度很高的视觉小说。", "这部视觉小说有精美的画面和动听的配乐。"),
        relatedWords = listOf("文字冒险游戏", "电子游戏", "互动小说", "AVG", "galgame"),
        pronunciation = "shì jué xiǎo shuō",
        rating = 1L // Sample rating 0-5
    )
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
    settingsRepository: SettingsRepository?, // Accept SettingsRepository
    wordRepository: WordRepository?, // Inject WordRepository (make nullable for previews)
    // words: List<WordItemUI> = sampleWordList, // Removed hardcoded sample list
    // onFilterClick: () -> Unit = {}, // Filter click is handled internally now
    onWordItemClick: (WordItemUI) -> Unit, // Changed: Make this non-optional for navigation
    onNavigate: (String) -> Unit = {} // Callback for bottom navigation
) {
    // State for tracking the selected bottom navigation item index
    var selectedItemIndex by remember { mutableStateOf(0) }
    // State for filter dropdown visibility
    var showFilterMenu by remember { mutableStateOf(false) }
    // State for selected filter options - Default to ALL
    var selectedFilters by remember { mutableStateOf(setOf(FilterOption.ALL)) }

    // Read LocalInspectionMode outside the remember block
    val isPreview = LocalInspectionMode.current

    // State for locale selection (New)
    // Initialize locale from settings or use default if repository is null (for preview)
    var currentLocale by remember {
        // Use the value read outside
        mutableStateOf(
            if (isPreview) "EN" else settingsRepository?.getLocale() ?: "EN"
        )
    }
    var showLocaleMenu by remember { mutableStateOf(false) } // Locale dropdown visibility

    // State to hold the list of words fetched from the repository
    var wordsToDisplay by remember { mutableStateOf<List<WordItemUI>>(emptyList()) }

    // Fetch words when locale or repository changes
    LaunchedEffect(currentLocale, wordRepository) {
        if (wordRepository != null) {
            logger.debug { "HomePage: Fetching words for locale: $currentLocale" }
            wordsToDisplay = wordRepository.getWordItemsForLanguage(currentLocale)
            logger.debug { "HomePage: Fetched ${wordsToDisplay.size} words." }
        } else if (!isPreview) { // Only log warning if not in preview mode
            // Handle the case where the repository is not yet available (log or show message)
            logger.warn { "HomePage: WordRepository is null. Cannot fetch words." }
            wordsToDisplay = emptyList() // Ensure list is empty if repo is null
        }
        // In preview mode, wordsToDisplay will remain empty unless set otherwise for preview
    }


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
                    // Locale Selector (New)
                    Box { // Box to anchor the dropdown
                        IconButton(onClick = { showLocaleMenu = true }) {
                            Text(
                                currentLocale, // Display current locale state
                                style = MaterialTheme.typography.labelLarge, // Or another suitable style
                                color = MaterialTheme.colorScheme.onPrimary // Match TopAppBar text color (assuming primary is dark enough)
                            )
                        }
                        DropdownMenu(
                            expanded = showLocaleMenu,
                            onDismissRequest = { showLocaleMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("English (EN)") },
                                onClick = {
                                    settingsRepository?.setLocale("EN") // Save to settings
                                    currentLocale = "EN" // Update local state
                                    showLocaleMenu = false
                                    // TODO: Implement actual locale change logic if needed elsewhere
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("日本語 (JA)") },
                                onClick = {
                                    settingsRepository?.setLocale("JA") // Save to settings
                                    currentLocale = "JA" // Update local state
                                    showLocaleMenu = false
                                    // TODO: Implement actual locale change logic if needed elsewhere
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("中文 (ZH)") },
                                onClick = { settingsRepository?.setLocale("ZH") // Save to settings
                                    currentLocale = "ZH" // Update local state
                                    showLocaleMenu = false
                                    // TODO: Implement actual locale change logic if needed elsewhere
                                }
                            )
                        }
                    }

                    // Existing Filter Icon and Dropdown Menu
                    Box { // Box to anchor the DropdownMenu
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate("fetch") }, // Navigate to fetch screen
                containerColor = MaterialTheme.colorScheme.secondary, // Example color
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add new word")
            }
        }
    ) { innerPadding ->
        // Call the extracted WordList composable
        WordList(
            // Pass the selected list based on locale
            // Use the fetched words state. If in preview and repo is null, use sample EN data.
            words = if (isPreview && wordRepository == null) sampleWordListEN else wordsToDisplay,
            // Alternative for preview: words = if (isPreview) sampleWordListEN else wordsToDisplay,
            // This depends if you want HomePagePreview to show data even if repo is null
            // words = wordsToDisplay, // Pass the fetched words state
            visibleFilters = selectedFilters,
            onWordItemClick = onWordItemClick,
            modifier = Modifier.padding(innerPadding) // Apply padding from Scaffold
        )
    }
}

/**
 * Displays the list of word items using a LazyColumn.
 *
 * @param words The list of [WordItemDB] to display.
 * @param visibleFilters The set of [FilterOption] to apply to each item.
 * @param onWordItemClick Callback invoked when a word item is clicked.
 * @param modifier Modifier for the LazyColumn.
 */
@Composable
fun WordList(
    words: List<WordItemUI>,
    visibleFilters: Set<FilterOption>,
    onWordItemClick: (WordItemUI) -> Unit, // Pass the click handler down
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
        // Provide dummy lambda, null settings repo, and null word repo for preview.
        // The LaunchedEffect won't run with a null repo in preview.
        // The WordList call inside HomePage is modified to show sample EN data in this case.
        HomePage(
            settingsRepository = null,
            wordRepository = null, // Pass null for preview
            onWordItemClick = {}
        )
    }
}

@Preview
@Composable
fun WordListPreviewEN() {
    MaterialTheme {
        WordList(
            words = sampleWordListEN,
            visibleFilters = setOf(FilterOption.ALL),
            onWordItemClick = {}
        )
    }
}

@Preview
@Composable
fun WordListPreviewJA() {
    MaterialTheme {
        WordList(
            words = sampleWordListJA,
            visibleFilters = setOf(FilterOption.ALL),
            onWordItemClick = {}
        )
    }
}

@Preview
@Composable
fun WordListPreviewZH() {
    MaterialTheme {
        WordList(
            words = sampleWordListZH,
            visibleFilters = setOf(FilterOption.ALL),
            onWordItemClick = {}
        )
    }
}
