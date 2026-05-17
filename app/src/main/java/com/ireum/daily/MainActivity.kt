package com.ireum.daily

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ireum.daily.notification.FavoriteMealNotificationScheduler
import com.ireum.daily.ui.MealScreen
import com.ireum.daily.ui.MealViewModel
import com.ireum.daily.ui.theme.DailyTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as DailyApplication).appContainer
        requestNotificationPermissionIfNeeded()
        scheduleFavoriteMealNotifications()

        setContent {
            DailyTheme {
                val viewModel: MealViewModel = viewModel(
                    factory = MealViewModel.Factory(
                        mealRepository = appContainer.mealRepository,
                        taskRepository = appContainer.taskRepository
                    )
                )
                MealScreen(viewModel = viewModel)
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun scheduleFavoriteMealNotifications() {
        val appContainer = (application as DailyApplication).appContainer
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                appContainer.mealRepository.notificationTime.collect { notificationTime ->
                    FavoriteMealNotificationScheduler.scheduleDaily(
                        context = this@MainActivity,
                        notificationTime = notificationTime
                    )
                }
            }
        }
    }
}
