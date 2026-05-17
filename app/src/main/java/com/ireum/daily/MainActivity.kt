package com.ireum.daily

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ireum.daily.notification.FavoriteMealNotificationScheduler
import com.ireum.daily.notification.SummaryNotificationScheduler
import com.ireum.daily.ui.MealScreen
import com.ireum.daily.ui.MealViewModel
import com.ireum.daily.ui.theme.DailyTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as DailyApplication).appContainer
        scheduleFavoriteMealNotifications()
        scheduleSummaryNotifications()

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

    private fun scheduleSummaryNotifications() {
        val appContainer = (application as DailyApplication).appContainer
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                appContainer.mealRepository.summaryNotificationSettings.collect { settings ->
                    SummaryNotificationScheduler.schedule(
                        context = this@MainActivity,
                        settings = settings
                    )
                }
            }
        }
    }
}
