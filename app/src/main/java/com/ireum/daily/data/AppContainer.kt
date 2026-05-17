package com.ireum.daily.data

import android.content.Context
import androidx.room.Room
import com.ireum.daily.BuildConfig
import com.ireum.daily.data.local.DailyDatabase
import com.ireum.daily.data.preferences.SchoolPreferences
import com.ireum.daily.data.remote.NeisMealApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(context: Context) {
    private val database: DailyDatabase = Room.databaseBuilder(
        context.applicationContext,
        DailyDatabase::class.java,
        "daily.db"
    ).build()

    private val neisMealApi: NeisMealApi = Retrofit.Builder()
        .baseUrl("https://open.neis.go.kr/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(NeisMealApi::class.java)

    private val schoolPreferences = SchoolPreferences(context.applicationContext)

    val mealRepository = MealRepository(
        mealDao = database.mealDao(),
        neisMealApi = neisMealApi,
        schoolPreferences = schoolPreferences,
        defaultNeisApiKey = BuildConfig.NEIS_API_KEY,
        defaultSchool = SchoolConfig(
            educationOfficeCode = BuildConfig.DEFAULT_OFFICE_CODE,
            schoolCode = BuildConfig.DEFAULT_SCHOOL_CODE,
            schoolName = BuildConfig.DEFAULT_SCHOOL_NAME
        )
    )
}
