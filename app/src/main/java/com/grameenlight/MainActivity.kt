package com.grameenlight

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.grameenlight.data.local.DataSeeder
import com.grameenlight.data.local.ThemePreferences
import com.grameenlight.presentation.common.MainAppNavigation
import com.grameenlight.presentation.common.theme.GrameenLightTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var dataSeeder: DataSeeder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            dataSeeder.seedDataIfNecessary()
        }

        val themePreferences = ThemePreferences(this)

        setContent {
            val systemTheme = isSystemInDarkTheme()
            val isDarkModePref by themePreferences.isDarkModeFlow.collectAsState(initial = systemTheme)
            val isDarkMode = isDarkModePref ?: systemTheme
            val coroutineScope = rememberCoroutineScope()

            GrameenLightTheme(darkTheme = isDarkMode) {
                MainAppNavigation(
                    onThemeToggle = {
                        coroutineScope.launch {
                            themePreferences.saveThemePreference(!isDarkMode)
                        }
                    }
                )
            }
        }
    }
}
