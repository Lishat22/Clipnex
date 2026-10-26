package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Katto API ভিডিও সাবমিশন রিকোয়েস্ট মডেল
 */
@JsonClass(generateAdapter = true)
data class KattoJobRequest(
    @Json(name = "url") val url: String,
    @Json(name = "clips") val clips: Int? = null,
    @Json(name = "clip_count") val clipCount: Int? = null,
    @Json(name = "duration") val duration: Int? = null,
    @Json(name = "aspect_ratio") val aspectRatio: String? = null,
    @Json(name = "language") val language: String? = null,
    @Json(name = "start") val start: Double? = null,
    @Json(name = "end") val end: Double? = null,
    @Json(name = "webhook_url") val webhookUrl: String? = null
)

/**
 * ভিডিও সাবমিশনের অতিরিক্ত কনফিগারেশন সেটিংস
 */
data class VideoClippingSettings(
    val durationSeconds: Int = 30,
    val clipCount: Int = 3,
    val aspectRatio: String = "9:16",
    val captionLanguage: String = "bn",
    val startTime: Double? = null,
    val endTime: Double? = null,
    val webhookUrl: String? = null
)

/**
 * Katto API প্রাথমিক সাবমিশন রেসপন্স মডেল
 */
@JsonClass(generateAdapter = true)
data class KattoJobResponse(
    @Json(name = "id") val id: String,
    @Json(name = "status") val status: String,
    @Json(name = "status_url") val statusUrl: String? = null
)

/**
 * রেন্ডারকৃত ক্লিপের তথ্য
 */
@JsonClass(generateAdapter = true)
data class KattoClipDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "start") val start: Double? = null,
    @Json(name = "end") val end: Double? = null,
    @Json(name = "duration") val duration: Double? = null,
    @Json(name = "score") val score: Double? = null,
    @Json(name = "virality_score") val viralityScore: Double? = null,
    @Json(name = "download_url") val downloadUrl: String? = null,
    @Json(name = "url") val url: String? = null,
    @Json(name = "transcript") val transcript: String? = null,
    @Json(name = "captions") val captions: String? = null
)

/**
 * Katto API স্ট্যাটাস পোলিং রেসপন্স মডেল
 */
@JsonClass(generateAdapter = true)
data class KattoJobStatusResponse(
    @Json(name = "id") val id: String? = null,
    @Json(name = "status") val status: String,
    @Json(name = "progress") val progress: Double? = null,
    @Json(name = "error") val error: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "clips") val clips: List<KattoClipDto>? = null,
    @Json(name = "transcript") val transcript: String? = null
)

/**
 * রিপোজিটরি সাবমিশন ফলাফলের জন্য Sealed Class
 */
sealed class KattoSubmissionResult {
    data class Success(
        val projectId: String,
        val jobId: String,
        val status: String,
        val statusUrl: String?,
        val isDemo: Boolean = false
    ) : KattoSubmissionResult()

    data class Error(
        val statusCode: Int?,
        val messageBengali: String,
        val rawMessage: String? = null
    ) : KattoSubmissionResult()
}
