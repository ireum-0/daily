package com.ireum.daily.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface NeisMealApi {
    @GET("hub/mealServiceDietInfo")
    suspend fun getMealServiceDietInfo(
        @Query("KEY") apiKey: String,
        @Query("Type") responseType: String = "json",
        @Query("pIndex") pageIndex: Int = 1,
        @Query("pSize") pageSize: Int = 100,
        @Query("ATPT_OFCDC_SC_CODE") educationOfficeCode: String,
        @Query("SD_SCHUL_CODE") schoolCode: String,
        @Query("MLSV_YMD") mealDate: String? = null,
        @Query("MLSV_FROM_YMD") mealStartDate: String? = null,
        @Query("MLSV_TO_YMD") mealEndDate: String? = null
    ): NeisMealResponse

    @GET("hub/schoolInfo")
    suspend fun searchSchools(
        @Query("KEY") apiKey: String,
        @Query("Type") responseType: String = "json",
        @Query("pIndex") pageIndex: Int = 1,
        @Query("pSize") pageSize: Int = 20,
        @Query("SCHUL_NM") schoolName: String
    ): NeisSchoolResponse
}
