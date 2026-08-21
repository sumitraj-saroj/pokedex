package com.dexter.app.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SettingsBrightness
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.dexter.app.ui.auth.LoginScreen
import com.dexter.app.ui.auth.LoginViewModel
import com.dexter.app.ui.auth.RegisterScreen
import com.dexter.app.ui.auth.RegisterViewModel
import com.dexter.app.ui.battle.BattleHubScreen
import com.dexter.app.ui.battle.BattleViewModel
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
import com.dexter.app.ui.region.RegionMapScreen
import com.dexter.app.ui.region.RegionMapViewModel
import com.dexter.app.ui.team.TeamBuilderScreen
import com.dexter.app.ui.team.TeamViewModel

import com.dexter.app.ui.common.GlassmorphicNavigationBar

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun DexterNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    windowWidthSizeClass: androidx.compose.material3.windowsizeclass.WindowWidthSizeClass = androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Compact
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Collect avatar for global top bar profile icon
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val profileUiState by profileViewModel.uiState.collectAsStateWithLifecycle()

    val bottomNavItems = listOf(
        BottomNavItem("Pokédex", Screen.Home.route, Icons.Default.CatchingPokemon),
        BottomNavItem("Team", Screen.TeamBuilder.route, Icons.Default.Groups),
        BottomNavItem("Battle", Screen.BattleHub.createRoute(), Icons.Default.Bolt),
        BottomNavItem("Compare", Screen.Compare.route, Icons.AutoMirrored.Filled.CompareArrows),
        BottomNavItem("Quiz", Screen.Quiz.route, Icons.Default.Psychology)
    )

    val showBottomBar = bottomNavItems.any { it.route.substringBefore("?") == currentRoute?.substringBefore("?") }
    val haptics = com.dexter.app.ui.common.rememberHapticUtils(isEnabled = profileUiState.trainerData.isHapticEnabled)

    // --- Scroll-to-hide bottom bar logic ---
    // Track whether the bar is visible (starts visible, hides on scroll down, shows on scroll up)
    var isBottomBarVisible by rememberSaveable { mutableStateOf(true) }

    // Reset visibility when navigating to a different tab
    androidx.compose.runtime.LaunchedEffect(currentRoute) {
        isBottomBarVisible = true
    }

    // NestedScrollConnection intercepts all child scroll gestures
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val dy = available.y
                if (dy < -4f) {
                    // Scrolling down → hide
                    isBottomBarVisible = false
                } else if (dy > 4f) {
                    // Scrolling up → show
                    isBottomBarVisible = true
                }
                return Offset.Zero // don't consume any scroll
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        SharedTransitionLayout {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(route = Screen.Home.route) {
                    val viewModel: HomeViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    HomeScreen(
                        uiState = uiState,
                        windowWidthSizeClass = windowWidthSizeClass,
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
                        onProfileClick = { navController.navigate(Screen.Profile.route) },
                        onRegionMapClick = { navController.navigate(Screen.RegionMap.route) },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable
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
                        onSwapSlots = viewModel::swapSlots,
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

                composable(
                    route = Screen.BattleHub.route,
                    arguments = listOf(
                        navArgument("tab") {
                            type = NavType.StringType
                            defaultValue = "damage"
                        },
                        navArgument("pokemonId") {
                            type = NavType.IntType
                            defaultValue = 0
                        }
                    )
                ) {
                    val viewModel: BattleViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    BattleHubScreen(
                        uiState = uiState,
                        onTabSelected = viewModel::selectTab,
                        // Damage Calc Handlers
                        onAttackerSelected = viewModel::setAttackerPokemon,
                        onDefenderSelected = viewModel::setDefenderPokemon,
                        onSwapAttackerDefender = viewModel::swapAttackerAndDefender,
                        onAttackerLevelChange = viewModel::setAttackerLevel,
                        onAttackerNatureChange = viewModel::setAttackerNature,
                        onAttackerItemChange = viewModel::setAttackerItem,
                        onAttackerAbilityChange = viewModel::setAttackerAbility,
                        onAttackerStageChange = viewModel::setAttackerStatStage,
                        onAttackerEvChange = viewModel::setAttackerEv,
                        onAttackerIvChange = viewModel::setAttackerIv,
                        onAttackerBurnChange = viewModel::setAttackerBurned,
                        onAttackerTeraTypeChange = viewModel::setAttackerTeraType,
                        onDefenderLevelChange = viewModel::setDefenderLevel,
                        onDefenderNatureChange = viewModel::setDefenderNature,
                        onDefenderItemChange = viewModel::setDefenderItem,
                        onDefenderAbilityChange = viewModel::setDefenderAbility,
                        onDefenderStageChange = viewModel::setDefenderStatStage,
                        onDefenderEvChange = viewModel::setDefenderEv,
                        onDefenderIvChange = viewModel::setDefenderIv,
                        onDefenderTeraTypeChange = viewModel::setDefenderTeraType,
                        onDefenderHpPercentChange = viewModel::setDefenderCurrentHpPercent,
                        onSelectMove = viewModel::selectMove,
                        onCriticalToggle = viewModel::setCritical,
                        onCustomMovePowerChange = viewModel::setCustomMovePower,
                        onCustomMoveTypeChange = viewModel::setCustomMoveType,
                        onCustomMoveCategoryChange = viewModel::setCustomMoveCategory,
                        onWeatherChange = viewModel::setWeather,
                        onTerrainChange = viewModel::setTerrain,
                        onDoublesToggle = viewModel::setDoubles,
                        onReflectToggle = viewModel::toggleReflect,
                        onLightScreenToggle = viewModel::toggleLightScreen,
                        onAuroraVeilToggle = viewModel::toggleAuroraVeil,
                        onStealthRockToggle = viewModel::toggleStealthRock,
                        onSpikesLayersChange = viewModel::setSpikesLayers,
                        onHelpingHandToggle = viewModel::toggleHelpingHand,
                        onOpenAttackerPicker = viewModel::openAttackerPicker,
                        onCloseAttackerPicker = viewModel::closeAttackerPicker,
                        onOpenDefenderPicker = viewModel::openDefenderPicker,
                        onCloseDefenderPicker = viewModel::closeDefenderPicker,
                        // Stat Calc Handlers
                        onStatCalcPokemonSelected = viewModel::setStatCalcPokemon,
                        onStatCalcLevelChange = viewModel::setStatCalcLevel,
                        onStatCalcNatureChange = viewModel::setStatCalcNature,
                        onStatCalcIvChange = viewModel::setStatCalcIv,
                        onStatCalcAllIvsChange = viewModel::setStatCalcAllIvs,
                        onStatCalcEvChange = viewModel::setStatCalcEv,
                        onStatCalcEvPresetSelected = viewModel::setStatCalcEvPreset,
                        onStatCalcItemChange = viewModel::setStatCalcItem,
                        onStatCalcTeraTypeChange = viewModel::setStatCalcTeraType,
                        onCopyShowdownSuccess = viewModel::triggerCopySuccess,
                        onSendStatCalcToDamageCalc = viewModel::sendStatCalcToDamageCalc,
                        onOpenStatCalcPicker = viewModel::openStatCalcPokemonPicker,
                        onCloseStatCalcPicker = viewModel::closeStatCalcPokemonPicker,
                        // Speed Tier Handlers
                        onSpeedTierLevelChange = viewModel::setSpeedTierLevel,
                        onSpeedTierSearchQueryChange = viewModel::setSpeedTierSearchQuery,
                        onSpeedTierCategoryFilterSelect = viewModel::setSpeedTierCategoryFilter,
                        onSpeedTierUserPokemonSelected = viewModel::setSpeedTierUserPokemon,
                        onUserSpeedNatureChange = viewModel::setUserSpeedNature,
                        onUserSpeedEvChange = viewModel::setUserSpeedEv,
                        onUserSpeedIvChange = viewModel::setUserSpeedIv,
                        onUserSpeedStatStageChange = viewModel::setUserSpeedStatStage,
                        onUserSpeedScarfToggle = viewModel::setUserSpeedScarf,
                        onUserSpeedBoosterToggle = viewModel::setUserSpeedBooster,
                        onUserSpeedSwiftSwimToggle = viewModel::setUserSpeedSwiftSwim,
                        onUserSpeedTailwindToggle = viewModel::setUserSpeedTailwind,
                        onUserSpeedParalyzedToggle = viewModel::setUserSpeedParalyzed,
                        onOpenSpeedTierPicker = viewModel::openSpeedTierUserPokemonPicker,
                        onCloseSpeedTierPicker = viewModel::closeSpeedTierUserPokemonPicker,
                        // Catch Calc Handlers
                        onCatchCalcPokemonSelected = viewModel::setCatchCalcPokemon,
                        onCatchCalcLevelChange = viewModel::setCatchCalcLevel,
                        onCatchCalcHpPercentChange = viewModel::setCatchCalcHpPercent,
                        onCatchCalcStatusChange = viewModel::setCatchCalcStatus,
                        onCatchCalcBallSelected = viewModel::setCatchCalcBall,
                        onCatchCalcTurnChange = viewModel::setCatchCalcTurn,
                        onCatchCalcNightOrCaveToggle = viewModel::setCatchCalcNightOrCave,
                        onCatchCalcWaterEncounterToggle = viewModel::setCatchCalcWaterEncounter,
                        onCatchCalcAlreadyInPokedexToggle = viewModel::setCatchCalcAlreadyInPokedex,
                        onCatchCalcPlayerLevelChange = viewModel::setCatchCalcPlayerLevel,
                        onOpenCatchCalcPicker = viewModel::openCatchCalcPokemonPicker,
                        onCloseCatchCalcPicker = viewModel::closeCatchCalcPokemonPicker
                    )
                }

                composable(route = Screen.Quiz.route) {
                    val viewModel: QuizViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    QuizScreen(
                        uiState = uiState,
                        onSelectOption = { pokemonId -> viewModel.selectOption(pokemonId, haptics) },
                        onPlayCry = viewModel::playCry,
                        onRestartGame = viewModel::restartGame,
                        onProfileClick = { navController.navigate(Screen.Profile.route) },
                        onToggleGeneration = viewModel::toggleGeneration,
                        onSelectAllGenerations = viewModel::selectAllGenerations,
                        onSelectGenerationPreset = viewModel::selectGenerationPreset
                    )
                }

                composable(route = Screen.Profile.route) {
                    val viewModel: ProfileViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    ProfileScreen(
                        uiState = uiState,
                        onBackClick = { navController.popBackStack() },
                        onAchievementsClick = { navController.navigate(Screen.Achievements.route) },
                        onAvatarSelect = viewModel::selectAvatar,
                        onHapticToggle = viewModel::setHapticEnabled,
                        onThemeSelect = viewModel::setThemeMode,
                        onLoginClick = { navController.navigate(Screen.Login.route) },
                        onLogoutClick = viewModel::logout
                    )
                }

                composable(route = Screen.Login.route) {
                    val viewModel: LoginViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    LoginScreen(
                        uiState = uiState,
                        onEmailChange = viewModel::onEmailChanged,
                        onPasswordChange = viewModel::onPasswordChanged,
                        onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
                        onFillDemoAccount = viewModel::fillDemoAccount,
                        onLoginClick = viewModel::login,
                        onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                        onBackClick = { navController.popBackStack() },
                        onLoginSuccess = { navController.popBackStack() }
                    )
                }

                composable(route = Screen.Register.route) {
                    val viewModel: RegisterViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    RegisterScreen(
                        uiState = uiState,
                        onTrainerNameChange = viewModel::onTrainerNameChanged,
                        onEmailChange = viewModel::onEmailChanged,
                        onPasswordChange = viewModel::onPasswordChanged,
                        onConfirmPasswordChange = viewModel::onConfirmPasswordChanged,
                        onAvatarSelect = viewModel::onAvatarSelected,
                        onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
                        onRegisterClick = viewModel::register,
                        onNavigateToLogin = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onBackClick = { navController.popBackStack() },
                        onRegisterSuccess = {
                            navController.popBackStack(Screen.Profile.route, inclusive = false)
                        }
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

                composable(route = Screen.RegionMap.route) {
                    val viewModel: RegionMapViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    RegionMapScreen(
                        uiState = uiState,
                        onBackClick = { navController.popBackStack() },
                        onRegionSelect = viewModel::selectRegion,
                        onLocationSelect = viewModel::selectLocation,
                        onFilterTypeSelect = viewModel::setFilterType,
                        onPokemonClick = { pokemonId ->
                            navController.navigate(Screen.Detail.createRoute(pokemonId))
                        },
                        onMapStyleSelect = viewModel::setMapStyle,
                        onToggleAudioTheme = viewModel::toggleRegionalTheme,
                        onPlayPokemonCry = viewModel::playPokemonCry,
                        onSearchQueryChange = viewModel::onSearchQueryChanged,
                        onSelectSearchResult = viewModel::selectSearchResult,
                        onClearSearch = viewModel::clearSearch
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
                        },
                        onRetryTcgCards = viewModel::retryFetchTcgCards,
                        onOpenBattleHub = { tab, pokemonId ->
                            navController.navigate(Screen.BattleHub.createRoute(tab, pokemonId))
                        },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable
                    )
                }
            }
        }

        // Animated bottom bar: slides in/out based on scroll direction
        AnimatedVisibility(
            visible = showBottomBar && isBottomBarVisible,
            enter = slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                initialOffsetY = { fullHeight -> fullHeight }
            ),
            exit = slideOutVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                targetOffsetY = { fullHeight -> fullHeight }
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            GlassmorphicNavigationBar(
                items = bottomNavItems,
                currentRoute = currentRoute,
                onItemClick = { item ->
                    if (currentRoute != item.route) {
                        haptics.selectionTick()
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
