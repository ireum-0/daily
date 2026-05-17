package com.ireum.daily.data.remote

import com.google.gson.annotations.SerializedName

data class NeisMealResponse(
    @SerializedName("mealServiceDietInfo")
    val mealServiceDietInfo: List<MealServiceDietInfoBlock>? = null,
    @SerializedName("RESULT")
    val result: NeisResult? = null
) {
    fun rows(): List<RemoteMeal> =
        mealServiceDietInfo.orEmpty().flatMap { block -> block.rows.orEmpty() }

    fun resultCode(): String? =
        result?.code ?: mealServiceDietInfo.orEmpty()
            .flatMap { block -> block.head.orEmpty() }
            .firstNotNullOfOrNull { head -> head.result?.code }
}

data class MealServiceDietInfoBlock(
    @SerializedName("head")
    val head: List<NeisHeadItem>? = null,
    @SerializedName("row")
    val rows: List<RemoteMeal>? = null
)

data class NeisHeadItem(
    @SerializedName("list_total_count")
    val totalCount: Int? = null,
    @SerializedName("RESULT")
    val result: NeisResult? = null
)

data class NeisResult(
    @SerializedName("CODE")
    val code: String? = null,
    @SerializedName("MESSAGE")
    val message: String? = null
)

data class RemoteMeal(
    @SerializedName("ATPT_OFCDC_SC_CODE")
    val educationOfficeCode: String? = null,
    @SerializedName("SD_SCHUL_CODE")
    val schoolCode: String? = null,
    @SerializedName("SCHUL_NM")
    val schoolName: String? = null,
    @SerializedName("MMEAL_SC_CODE")
    val mealCode: String? = null,
    @SerializedName("MMEAL_SC_NM")
    val mealName: String? = null,
    @SerializedName("MLSV_YMD")
    val mealDate: String? = null,
    @SerializedName("DDISH_NM")
    val dishName: String? = null,
    @SerializedName("ORPLC_INFO")
    val originInfo: String? = null,
    @SerializedName("CAL_INFO")
    val calorieInfo: String? = null,
    @SerializedName("NTR_INFO")
    val nutrientInfo: String? = null
)
