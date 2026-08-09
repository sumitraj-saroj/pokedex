package com.dexter.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.dexter.app.data.repository.AppThemeMode
import com.dexter.app.data.repository.ThemePreferencesRepository
import com.dexter.app.navigation.DexterNavHost
import com.dexter.app.ui.theme.DexterTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themePreferencesRepository: ThemePreferencesRepository

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val themeMode by themePreferencesRepository.themeModeFlow
                .collectAsStateWithLifecycle(initialValue = AppThemeMode.SYSTEM)

            DexterTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    DexterNavHost(
                        navController = navController,
                        windowWidthSizeClass = windowSizeClass.widthSizeClass
                    )
                }
            }
        }
    }
}
