package com.example.data.model

enum class AspectRatioOption(val ratioLabel: String, val bengaliDescription: String) {
    NINE_SIXTEEN("9:16", "ভার্টিক্যাল (রিলস, শর্টস, টিকটক)"),
    ONE_ONE("1:1", "স্কয়ার (ফেসবুক ও ইনস্টাগ্রাম ফিড)"),
    SIXTEEN_NINE("16:9", "ল্যান্ডস্কেপ (ইউটিউব ও ওয়েবসাইট)")
}

enum class DurationOption(val labelBengali: String, val seconds: Int) {
    S15("১৫ সেকেন্ড", 15),
    S30("৩০ সেকেন্ড", 30),
    S45("৪৫ সেকেন্ড", 45),
    S60("৬০ সেকেন্ড", 60)
}

enum class CaptionLanguageOption(val titleBengali: String, val code: String) {
    BENGALI("বাংলা (Bengali)", "bn"),
    ENGLISH("ইংরেজি (English)", "en"),
    AUTO("স্বয়ংক্রিয় (Auto Detect)", "auto")
}

enum class ProjectStatus(val bengaliText: String) {
    QUEUED("অপেক্ষার সারিতে"),
    PROCESSING("প্রসেসিং চলছে"),
    COMPLETED("সম্পন্ন হয়েছে"),
    FAILED("ব্যর্থ হয়েছে")
}

data class ClipItem(
    val id: String,
    val projectId: String,
    val title: String,
    val startTime: String,
    val endTime: String,
    val durationSeconds: Int,
    val viralityScore: Int,
    val hookScore: Int,
    val flowScore: Int,
    val valueScore: Int,
    val trendScore: Int,
    val downloadUrl: String,
    val videoUrl: String,
    val transcript: String,
    val postCaption: String,
    val hashtags: List<String>,
    val isFavorite: Boolean = false
)

data class ProjectItem(
    val id: String,
    val title: String,
    val videoUrl: String,
    val durationSeconds: Int,
    val clipCount: Int,
    val aspectRatio: String,
    val captionLanguage: String,
    val status: ProjectStatus,
    val progressPercent: Int,
    val statusBengali: String,
    val createdAt: Long,
    val isDemo: Boolean,
    val clips: List<ClipItem> = emptyList(),
    val errorMessage: String? = null
)
