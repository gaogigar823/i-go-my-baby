package com.example.igomybaby

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.igomybaby.data.AppPreferences
import com.example.igomybaby.service.DecibelForegroundService
import com.example.igomybaby.notification.NoiseNotifications
import com.example.igomybaby.ui.screens.LaunchScreen
import com.example.igomybaby.ui.screens.MainScreen
import com.example.igomybaby.ui.screens.OnboardingScreen
import com.example.igomybaby.ui.screens.SettingsScreen
import com.example.igomybaby.ui.theme.IGoMyBabyTheme
import com.example.igomybaby.viewmodel.MainViewModel
import com.example.igomybaby.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private object Routes {
    const val LAUNCH = "launch"
    const val ONBOARDING = "onboarding"
    const val MAIN = "main"
    const val SETTINGS = "settings"
}

class MainActivity : ComponentActivity() {

    private val notifPermLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NoiseNotifications.createChannels(this)
        // 온보딩을 이미 마친 사용자도 알림 권한이 없으면 즉시 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            IGoMyBabyTheme {
                AppRoot()
            }
        }
    }

    @Composable
    private fun AppRoot() {
        val prefs = remember { AppPreferences(applicationContext) }
        val nav = rememberNavController()

        val mainVm: MainViewModel = viewModel()
        val settingsVm: SettingsViewModel = viewModel()

        val mainState by mainVm.state.collectAsState()
        val settingsState by settingsVm.state.collectAsState()

        NavHost(navController = nav, startDestination = Routes.LAUNCH) {
            composable(Routes.LAUNCH) {
                LaunchScreen(
                    onDone = {
                        lifecycleScope.launch {
                            val onboardingDone = prefs.onboardingDone.first()
                            // 온보딩을 마쳤더라도 마이크 권한이 회수됐다면 다시 온보딩으로 보냄
                            // (RECORD_AUDIO 없이 startForegroundService 호출 시 SecurityException)
                            val next = if (onboardingDone && hasMicPermission()) {
                                startDecibelService()
                                Routes.MAIN
                            } else {
                                Routes.ONBOARDING
                            }
                            nav.navigate(next) {
                                popUpTo(Routes.LAUNCH) { inclusive = true }
                            }
                        }
                    },
                )
            }

            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onStart = {
                        lifecycleScope.launch { prefs.setOnboardingDone() }
                        startDecibelService()
                        nav.navigate(Routes.MAIN) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                )
            }

            composable(Routes.MAIN) {
                MainScreen(
                    state = mainState,
                    onTogglePause = { mainVm.togglePause() },
                    onSettings = { nav.navigate(Routes.SETTINGS) },
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    state = settingsState,
                    onThresholdChange = { settingsVm.setThreshold(it) },
                    onPreview = { settingsVm.sendPreviewNotification() },
                    onBack = { nav.popBackStack() },
                )
            }
        }
    }

    private fun startDecibelService() {
        // 마이크 권한 없으면 절대 시작하지 않음 — Android 14+ 에서 SecurityException 으로 크래시됨
        if (!hasMicPermission()) return
        val intent = Intent(this, DecibelForegroundService::class.java)
        startForegroundService(intent)
    }

    private fun hasMicPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
}
