package com.dexter.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dexter.app.ui.achievements.AchievementsScreen
import com.dexter.app.ui.achievements.AchievementsViewModel
import com.dexter.app.ui.compare.CompareScreen
import com.dexter.app.ui.compare.CompareViewModel
import com.dexter.app.ui.detail.DetailScreen
import com.dexter.app.ui.detail.DetailViewModel
import com.dexter.app.ui.home.HomeScreen
import com.dexter.app.ui.home.HomeViewModel
import com.dexter.app.ui.profile.ProfileScreen
import com.dexter.app.ui.profile.ProfileViewModel
import com.dexter.app.ui.quiz.QuizScreen
import com.dexter.app.ui.quiz.QuizViewModel
import com.dexter.app.ui.team.TeamBuilderScreen
import com.dexter.app.ui.team.TeamViewModel

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DexterNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Collect avatar for global top bar profile icon
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val profileUiState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val avatarPokemonId = profileUiState.trainerData.avatarPokemonId

    val bottomNavItems = listOf(
        BottomNavItem("Pokédex", Screen.Home.route, Icons.Default.CatchingPokemon),
        BottomNavItem("Team Builder", Screen.TeamBuilder.route, Icons.Default.Groups),
        BottomNavItem("Compare", Screen.Compare.route, Icons.AutoMirrored.Filled.CompareArrows),
        BottomNavItem("Quiz", Screen.Quiz.route, Icons.Default.Psychology)
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }
    val showGlobalTopBar = currentRoute in listOf(Screen.TeamBuilder.route, Screen.Compare.route, Screen.Quiz.route)

    Scaffold(
        modifier = modifier,
        topBar = {
            if (showGlobalTopBar) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentRoute) {
                                Screen.TeamBuilder.route -> "Team Builder"
                                Screen.Compare.route -> "Compare Pokémon"
                                Screen.Quiz.route -> "Who's That Pokémon?"
                                else -> "Dexter"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                            val avatarUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${avatarPokemonId}.png"
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(avatarUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Trainer Profile",
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = Screen.Home.route) {
                val viewModel: HomeViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                HomeScreen(
                    uiState = uiState,
                    onPokemonClick = { pokemonId ->
                        navController.navigate(Screen.Detail.createRoute(pokemonId))
                    },
                    onSearchQueryChange = viewModel::onSearchQueryChanged,
                    onSortOptionSelect = viewModel::setSortOption,
                    onSortOrderSelect = viewModel::setSortOrder,
                    onGenerationToggle = viewModel::toggleGenerationFilter,
                    onTypeToggle = viewModel::toggleTypeFilter,
                    onSpecialCategoryToggle = viewModel::toggleSpecialCategory,
                    onSortOptionReset = { viewModel.setSortOption(com.dexter.app.ui.home.SortOption.NUMBER) },
                    onClearFilters = viewModel::clearFilters,
                    onResyncClick = viewModel::triggerResync,
                    onThemeToggleClick = viewModel::toggleThemeMode,
                    onProfileClick = { navController.navigate(Screen.Profile.route) }
                )
            }

            composable(route = Screen.TeamBuilder.route) {
                val viewModel: TeamViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                TeamBuilderScreen(
                    uiState = uiState,
                    onSlotClick = viewModel::openPickerForSlot,
                    onRemoveSlot = viewModel::removeSlot,
                    onClearTeam = viewModel::clearTeam,
                    onSelectPokemon = viewModel::selectPokemonForSlot,
                    onDismissPicker = viewModel::closePicker,
                    onPokemonClick = { pokemonId ->
                        navController.navigate(Screen.Detail.createRoute(pokemonId))
                    }
                )
            }

            composable(route = Screen.Compare.route) {
                val viewModel: CompareViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                CompareScreen(
                    uiState = uiState,
                    onOpenPicker = viewModel::openPicker,
                    onClosePicker = viewModel::closePicker,
                    onSelectPokemon = viewModel::selectPokemon,
                    onSwapPokemon = viewModel::swapPokemon,
                    onPokemonClick = { pokemonId ->
                        navController.navigate(Screen.Detail.createRoute(pokemonId))
                    }
                )
            }

            composable(route = Screen.Quiz.route) {
                val viewModel: QuizViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val hapticUtils = com.dexter.app.ui.common.rememberHapticUtils()

                QuizScreen(
                    uiState = uiState,
                    onSelectOption = { pokemonId -> viewModel.selectOption(pokemonId, hapticUtils) },
                    onRestartGame = viewModel::restartGame,
                    onProfileClick = { navController.navigate(Screen.Profile.route) }
                )
            }

            composable(route = Screen.Profile.route) {
                val viewModel: ProfileViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                ProfileScreen(
                    uiState = uiState,
                    onBackClick = { navController.popBackStack() },
                    onAchievementsClick = { navController.navigate(Screen.Achievements.route) },
                    onAvatarSelect = viewModel::selectAvatar
                )
            }

            composable(route = Screen.Achievements.route) {
                val viewModel: AchievementsViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                AchievementsScreen(
                    uiState = uiState,
                    onBackClick = { navController.popBackStack() },
                    onCategorySelect = viewModel::selectCategory
                )
            }

            composable(
                route = Screen.Detail.route,
                arguments = listOf(
                    navArgument("pokemonId") { type = NavType.IntType }
                )
            ) {
                val viewModel: DetailViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                DetailScreen(
                    uiState = uiState,
                    onBackClick = { navController.popBackStack() },
                    onToggleCaught = viewModel::toggleCaught,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onVariantSelected = viewModel::selectVariant,
                    onPokemonClick = { pokemonId ->
                        navController.navigate(Screen.Detail.createRoute(pokemonId))
                    }
                )
            }
        }
    }
}
