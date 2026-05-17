package com.ireum.daily

import android.app.Application
import com.ireum.daily.data.AppContainer

class DailyApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
