package com.ireum.daily.data.remote

import com.google.gson.annotations.SerializedName

data class NeisSchoolResponse(
    @SerializedName("schoolInfo")
    val schoolInfo: List<SchoolInfoBlock>? = null,
    @SerializedName("RESULT")
    val result: NeisResult? = null
) {
    fun rows(): List<RemoteSchool> =
        schoolInfo.orEmpty().flatMap { block -> block.rows.orEmpty() }

    fun resultCode(): String? =
        result?.code ?: schoolInfo.orEmpty()
            .flatMap { block -> block.head.orEmpty() }
            .firstNotNullOfOrNull { head -> head.result?.code }
}

data class SchoolInfoBlock(
    @SerializedName("head")
    val head: List<NeisHeadItem>? = null,
    @SerializedName("row")
    val rows: List<RemoteSchool>? = null
)

data class RemoteSchool(
    @SerializedName("ATPT_OFCDC_SC_CODE")
    val educationOfficeCode: String? = null,
    @SerializedName("ATPT_OFCDC_SC_NM")
    val educationOfficeName: String? = null,
    @SerializedName("SD_SCHUL_CODE")
    val schoolCode: String? = null,
    @SerializedName("SCHUL_NM")
    val schoolName: String? = null,
    @SerializedName("SCHUL_KND_SC_NM")
    val schoolKind: String? = null,
    @SerializedName("ORG_RDNMA")
    val roadAddress: String? = null,
    @SerializedName("LCTN_SC_NM")
    val locationName: String? = null
)
