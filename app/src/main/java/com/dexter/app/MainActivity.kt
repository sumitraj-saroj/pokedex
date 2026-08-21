package com.dexter.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.dexter.app.data.repository.AppThemeMode
import com.dexter.app.data.repository.ThemePreferencesRepository
import com.dexter.app.navigation.DexterNavHost
import com.dexter.app.navigation.Screen
import com.dexter.app.ui.theme.DexterTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themePreferencesRepository: ThemePreferencesRepository

    private val intentFlow = MutableStateFlow<Intent?>(null)

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intentFlow.value = intent
        enableEdgeToEdge()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val themeMode by themePreferencesRepository.themeModeFlow
                .collectAsStateWithLifecycle(initialValue = AppThemeMode.SYSTEM)

            DexterTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    val currentIntent by intentFlow.collectAsStateWithLifecycle()
                    LaunchedEffect(currentIntent) {
                        currentIntent?.let {
                            handleWidgetIntent(it, navController)
                            intentFlow.value = null
                        }
                    }

                    DexterNavHost(
                        navController = navController,
                        windowWidthSizeClass = windowSizeClass.widthSizeClass
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intentFlow.value = intent
    }

    private fun handleWidgetIntent(intent: Intent, navController: NavHostController) {
        val destination = intent.getStringExtra(EXTRA_DESTINATION) ?: return
        when (destination) {
            DESTINATION_DETAIL -> {
                val pokemonId = intent.getIntExtra(EXTRA_POKEMON_ID, -1)
                if (pokemonId > 0) {
                    navController.navigate(Screen.Detail.createRoute(pokemonId)) {
                        launchSingleTop = true
                    }
                }
            }
            DESTINATION_QUIZ -> {
                navController.navigate(Screen.Quiz.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            DESTINATION_TEAM -> {
                navController.navigate(Screen.TeamBuilder.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    companion object {
        const val EXTRA_DESTINATION = "extra_destination"
        const val EXTRA_POKEMON_ID = "extra_pokemon_id"
        const val DESTINATION_DETAIL = "detail"
        const val DESTINATION_QUIZ = "quiz"
        const val DESTINATION_TEAM = "team"
    }
}
