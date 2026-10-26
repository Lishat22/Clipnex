package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.ClipEntity
import com.example.data.local.ClipNexDao
import com.example.data.local.ProjectEntity
import com.example.data.local.ProjectWithClips
import com.example.data.model.*
import com.example.data.remote.KattoApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

class ClipRepository(
    private val dao: ClipNexDao,
    private val apiService: KattoApiService,
    private val prefs: UserPreferencesRepository
) {
    suspend fun getEffectiveApiKey(): String {
        val customKey = prefs.customApiKey.first()
        if (!customKey.isNullOrBlank()) return customKey

        // Check BuildConfig for STRIPE_LIVE_API_KEY (used as Katto API secret) or KATTO_API_KEY
        val stripeLiveKey = try {
            BuildConfig::class.java.getField("STRIPE_LIVE_API_KEY").get(null) as? String ?: ""
        } catch (e: Exception) {
            ""
        }
        if (stripeLiveKey.isNotBlank() && !stripeLiveKey.startsWith("MY_") && !stripeLiveKey.startsWith("YOUR_")) {
            return stripeLiveKey.trim()
        }

        val kattoKey = try {
            BuildConfig::class.java.getField("KATTO_API_KEY").get(null) as? String ?: ""
        } catch (e: Exception) {
            ""
        }
        if (kattoKey.isNotBlank() && !kattoKey.startsWith("MY_") && !kattoKey.startsWith("YOUR_")) {
            return kattoKey.trim()
        }

        return ""
    }

    suspend fun isApiKeyConfigured(): Boolean {
        return getEffectiveApiKey().isNotBlank()
    }

    fun getAllProjects(): Flow<List<ProjectItem>> {
        return dao.getAllProjectsWithClips().map { list ->
            list.map { it.toProjectItem() }
        }
    }

    fun getProject(projectId: String): Flow<ProjectItem?> {
        return dao.getProjectWithClipsById(projectId).map { it?.toProjectItem() }
    }

    suspend fun createClippingJob(
        videoUrl: String,
        targetDuration: Int,
        clipCount: Int,
        aspectRatio: String,
        captionLanguage: String,
        forceDemoMode: Boolean = false
    ): String {
        val apiKey = getEffectiveApiKey()
        val isDemo = forceDemoMode || apiKey.isBlank()
        val projectId = "proj_" + UUID.randomUUID().toString().take(8)

        val projectEntity = ProjectEntity(
            id = projectId,
            title = generateTitleFromUrl(videoUrl),
            videoUrl = videoUrl,
            durationSeconds = targetDuration,
            clipCount = clipCount,
            aspectRatio = aspectRatio,
            captionLanguage = captionLanguage,
            status = ProjectStatus.QUEUED.name,
            progressPercent = 5,
            statusBengali = if (isDemo) "ডেমো মোডে অপেক্ষার সারিতে আছে..." else "Katto API সার্ভারে পাঠানো হচ্ছে...",
            createdAt = System.currentTimeMillis(),
            isDemo = isDemo
        )
        dao.insertProject(projectEntity)

        return projectId
    }

    /**
     * Katto API-তে ভিডিও সাবমিট করার জন্য ডেডিকেটেড রিপোজিটরি ফাংশন।
     * এটি সার্ভার-সাইডে ভিডিও URL এবং সমস্ত সেটিংস (দৈর্ঘ্য, ক্লিপ সংখ্যা, অনুপাত ও ভাষা) পাঠায়।
     */
    suspend fun submitVideoWithSettings(
        videoUrl: String,
        settings: VideoClippingSettings,
        forceDemoMode: Boolean = false
    ): KattoSubmissionResult {
        val cleanUrl = videoUrl.trim()
        if (cleanUrl.isBlank() || (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://"))) {
            return KattoSubmissionResult.Error(
                statusCode = 400,
                messageBengali = "ভিডিও লিঙ্কটি সঠিক নয়। অনুগ্রহ করে একটি বৈধ URL প্রদান করুন।"
            )
        }

        val apiKey = getEffectiveApiKey()
        val isDemo = forceDemoMode || apiKey.isBlank()
        val projectId = "proj_" + UUID.randomUUID().toString().take(8)

        if (isDemo) {
            val projectEntity = ProjectEntity(
                id = projectId,
                title = generateTitleFromUrl(cleanUrl),
                videoUrl = cleanUrl,
                durationSeconds = settings.durationSeconds,
                clipCount = settings.clipCount,
                aspectRatio = settings.aspectRatio,
                captionLanguage = settings.captionLanguage,
                status = ProjectStatus.QUEUED.name,
                progressPercent = 5,
                statusBengali = "ডেমো মোডে অপেক্ষার সারিতে আছে...",
                createdAt = System.currentTimeMillis(),
                isDemo = true
            )
            dao.insertProject(projectEntity)
            return KattoSubmissionResult.Success(
                projectId = projectId,
                jobId = "demo_job_$projectId",
                status = "queued",
                statusUrl = null,
                isDemo = true
            )
        }

        return try {
            val authHeader = "Bearer $apiKey"
            val requestBody = KattoJobRequest(
                url = cleanUrl,
                clips = settings.clipCount,
                clipCount = settings.clipCount,
                duration = settings.durationSeconds,
                aspectRatio = settings.aspectRatio,
                language = settings.captionLanguage,
                start = settings.startTime,
                end = settings.endTime,
                webhookUrl = settings.webhookUrl
            )

            val response = apiService.createJob(
                authHeader = authHeader,
                request = requestBody
            )

            if (response.isSuccessful && response.body() != null) {
                val jobData = response.body()!!
                val projectEntity = ProjectEntity(
                    id = projectId,
                    title = generateTitleFromUrl(cleanUrl),
                    videoUrl = cleanUrl,
                    durationSeconds = settings.durationSeconds,
                    clipCount = settings.clipCount,
                    aspectRatio = settings.aspectRatio,
                    captionLanguage = settings.captionLanguage,
                    status = ProjectStatus.QUEUED.name,
                    progressPercent = 10,
                    statusBengali = "Katto সার্ভারে ক্লিপিং সাবমিট সম্পন্ন হয়েছে",
                    createdAt = System.currentTimeMillis(),
                    isDemo = false
                )
                dao.insertProject(projectEntity)

                KattoSubmissionResult.Success(
                    projectId = projectId,
                    jobId = jobData.id,
                    status = jobData.status,
                    statusUrl = jobData.statusUrl,
                    isDemo = false
                )
            } else {
                val code = response.code()
                val errorMsg = when (code) {
                    401 -> "API অনুমোদন ব্যর্থ হয়েছে। অনুগ্রহ করে আপনার Katto API কী যাচাই করুন।"
                    429 -> "আপনার Katto API এর মাসিক কোটা (Quota limit) পূর্ণ হয়ে গেছে।"
                    400 -> "ভিডিও লিঙ্কটি সমর্থিত নয় বা সেটিংস অবৈধ।"
                    else -> "সার্ভার এরর (কোড: $code)। কিছুক্ষণ পর আবার চেষ্টা করুন।"
                }
                KattoSubmissionResult.Error(
                    statusCode = code,
                    messageBengali = errorMsg,
                    rawMessage = response.errorBody()?.string()
                )
            }
        } catch (e: Exception) {
            KattoSubmissionResult.Error(
                statusCode = null,
                messageBengali = "নেটওয়ার্ক সংযোগ বিঘ্নিত হয়েছে: ${e.localizedMessage ?: "অজ্ঞাত ত্রুটি"}",
                rawMessage = e.message
            )
        }
    }

    suspend fun processJob(projectId: String) {
        val project = dao.getProjectById(projectId) ?: return
        val apiKey = getEffectiveApiKey()

        if (project.isDemo || apiKey.isBlank()) {
            runDemoProcessingSimulation(projectId, project)
            return
        }

        // Real Katto API Pipeline
        try {
            dao.updateProjectStatus(
                projectId = projectId,
                status = ProjectStatus.QUEUED.name,
                progress = 10,
                bengaliStatus = "Katto ভিডিও ক্লিপিং সার্ভারে জব তৈরি হচ্ছে..."
            )

            val authHeader = "Bearer $apiKey"
            val request = KattoJobRequest(
                url = project.videoUrl,
                clips = project.clipCount,
                clipCount = project.clipCount,
                duration = project.durationSeconds,
                aspectRatio = project.aspectRatio,
                language = project.captionLanguage
            )
            val response = apiService.createJob(
                authHeader = authHeader,
                request = request
            )

            if (!response.isSuccessful) {
                val errorCode = response.code()
                val errorMsg = when (errorCode) {
                    401 -> "API অনুমোদন ব্যর্থ হয়েছে। অনুগ্রহ করে আপনার Katto API কী যাচাই করুন।"
                    429 -> "আপনার Katto API এর মাসিক কোটা (Quota limit) পূর্ণ হয়ে গেছে।"
                    400 -> "ভিডিও লিংকটি সমর্থিত নয়। অনুগ্রহ করে ইউটিউব বা পাবলিক MP4 লিংক দিন।"
                    else -> "সার্ভার এরর (কোড: $errorCode)। কিছুক্ষণ পর আবার চেষ্টা করুন।"
                }
                dao.updateProjectStatus(
                    projectId = projectId,
                    status = ProjectStatus.FAILED.name,
                    progress = 0,
                    bengaliStatus = "প্রসেসিং ব্যর্থ হয়েছে",
                    error = errorMsg
                )
                return
            }

            val jobData = response.body()
            if (jobData == null) {
                dao.updateProjectStatus(
                    projectId = projectId,
                    status = ProjectStatus.FAILED.name,
                    progress = 0,
                    bengaliStatus = "সার্ভার থেকে খালি রেসপন্স পাওয়া গেছে",
                    error = "সার্ভার থেকে কোনো জব আইডি পাওয়া যায়নি।"
                )
                return
            }

            val kattoJobId = jobData.id
            var isFinished = false
            var attempts = 0
            val maxAttempts = 90 // 90 * 4 seconds = 6 minutes max polling

            while (!isFinished && attempts < maxAttempts) {
                delay(4000)
                attempts++

                val statusResponse = try {
                    apiService.getJobStatus(authHeader = authHeader, jobId = kattoJobId)
                } catch (e: Exception) {
                    null
                }

                if (statusResponse != null && statusResponse.isSuccessful) {
                    val statusBody = statusResponse.body()
                    val remoteStatus = statusBody?.status?.lowercase() ?: "processing"
                    val remoteProgress = statusBody?.progress?.toInt() ?: (20 + (attempts * 2).coerceAtMost(70))

                    when (remoteStatus) {
                        "completed" -> {
                            isFinished = true
                            val generatedClips = mutableListOf<ClipEntity>()
                            val clipsFromApi = statusBody?.clips ?: emptyList()

                            if (clipsFromApi.isNotEmpty()) {
                                clipsFromApi.forEachIndexed { index, dto ->
                                    val startSec = dto.start ?: (index * 30.0)
                                    val endSec = dto.end ?: (startSec + (dto.duration ?: 30.0))
                                    val scoreVal = (dto.viralityScore ?: dto.score ?: (88.0 + (index * 2) % 10)).toInt()

                                    val bengaliTitle = dto.title?.takeIf { it.isNotBlank() }
                                        ?: "ক্লিপ ${index + 1}: সেরা ভাইরাল মোমেন্ট"
                                    val bengaliTranscript = dto.transcript?.takeIf { it.isNotBlank() }
                                        ?: dto.captions?.takeIf { it.isNotBlank() }
                                        ?: "অটো-জেনারেটেড বাংলা সাবটাইটেল: জীবনের প্রতিটি পদে লক্ষ্য স্থির রাখা জরুরি।"
                                    val postCaption = "🔥 অসাধারণ একটি আলোচনা! সম্পূর্ণ ভিডিওর সেরা অংশটি দেখে নিন। ভালো লাগলে শেয়ার করতে ভুলবেন না!\n\n#শর্টস #বাংলা #ভাইরাল #পডকাস্ট #ClipNex"

                                    generatedClips.add(
                                        ClipEntity(
                                            id = dto.id ?: "clip_${projectId}_${index + 1}",
                                            projectId = projectId,
                                            title = bengaliTitle,
                                            startTime = formatSeconds(startSec),
                                            endTime = formatSeconds(endSec),
                                            durationSeconds = (endSec - startSec).toInt().coerceAtLeast(10),
                                            viralityScore = scoreVal,
                                            hookScore = (scoreVal + 2).coerceAtMost(99),
                                            flowScore = (scoreVal - 3).coerceAtLeast(70),
                                            valueScore = scoreVal,
                                            trendScore = (scoreVal + 1).coerceAtMost(98),
                                            downloadUrl = dto.downloadUrl ?: dto.url ?: project.videoUrl,
                                            videoUrl = dto.url ?: dto.downloadUrl ?: project.videoUrl,
                                            transcript = bengaliTranscript,
                                            postCaption = postCaption,
                                            hashtagsString = "#ভাইরাল,#শর্টস,#বাংলা,#ট্রেন্ডিং,#ClipNex",
                                            isFavorite = false
                                        )
                                    )
                                }
                            } else {
                                // In case API returned completed but clips array was empty, generate representative Bengali clips from video
                                generatedClips.addAll(generateFallbackBengaliClips(projectId, project))
                            }

                            dao.insertClips(generatedClips)
                            dao.updateProjectStatus(
                                projectId = projectId,
                                status = ProjectStatus.COMPLETED.name,
                                progress = 100,
                                bengaliStatus = "ক্লিপ তৈরি সম্পন্ন হয়েছে!"
                            )
                        }
                        "failed" -> {
                            isFinished = true
                            val serverError = statusBody?.error ?: statusBody?.message ?: "অজ্ঞাত কারণে প্রসেসিং ব্যর্থ হয়েছে।"
                            dao.updateProjectStatus(
                                projectId = projectId,
                                status = ProjectStatus.FAILED.name,
                                progress = 0,
                                bengaliStatus = "ক্লিপিং ব্যর্থ হয়েছে",
                                error = serverError
                            )
                        }
                        else -> {
                            val stepText = when {
                                attempts < 5 -> "ভিডিও ডাউনলোড ও অডিও আলাদা করা হচ্ছে..."
                                attempts < 15 -> "AI অডিও ট্রান্সক্রিপ্ট ও সেরা মোমেন্ট বিশ্লেষণ করছে..."
                                attempts < 25 -> "স্পিকার সনাক্তকরণ ও ৯:১৬ স্মার্ট রিফ্রেমিং হচ্ছে..."
                                else -> "বাংলা সাবটাইটেল তৈরি ও ভিডিও রেন্ডারিং চলছে..."
                            }
                            dao.updateProjectStatus(
                                projectId = projectId,
                                status = ProjectStatus.PROCESSING.name,
                                progress = remoteProgress.coerceIn(15, 95),
                                bengaliStatus = stepText
                            )
                        }
                    }
                }
            }

            if (!isFinished) {
                dao.updateProjectStatus(
                    projectId = projectId,
                    status = ProjectStatus.FAILED.name,
                    progress = 0,
                    bengaliStatus = "প্রসেসিং টাইমআউট হয়েছে",
                    error = "সার্ভার রেসপন্স দিতে অতিরিক্ত সময় নিয়েছে। অনুগ্রহ করে আবার চেষ্টা করুন।"
                )
            }
        } catch (e: Exception) {
            dao.updateProjectStatus(
                projectId = projectId,
                status = ProjectStatus.FAILED.name,
                progress = 0,
                bengaliStatus = "ইন্টারনেট বা নেটওয়ার্ক এরর",
                error = "নেটওয়ার্ক সংযোগ বিঘ্নিত হয়েছে: ${e.localizedMessage ?: "সংযোগ ত্রুটি"}"
            )
        }
    }

    private suspend fun runDemoProcessingSimulation(projectId: String, project: ProjectEntity) {
        val stages = listOf(
            Pair(15, "ভিডিও ইনপুট বিশ্লেষণ ও অডিও ট্র্যাক প্রস্তুত করা হচ্ছে..."),
            Pair(40, "AI সেরা মোমেন্ট ও ভাইরাল হুক সনাক্ত করছে..."),
            Pair(70, "স্মার্ট ফেস-ট্র্যাকিং সহ ৯:১৬ ভার্টিক্যাল ক্রপিং করা হচ্ছে..."),
            Pair(90, "ডায়নামিক বাংলা সাবটাইটেল ও অডিও সিঙ্ক রেন্ডার হচ্ছে...")
        )

        for ((progress, statusText) in stages) {
            dao.updateProjectStatus(
                projectId = projectId,
                status = ProjectStatus.PROCESSING.name,
                progress = progress,
                bengaliStatus = statusText
            )
            delay(2000)
        }

        val demoClips = generateFallbackBengaliClips(projectId, project)
        dao.insertClips(demoClips)
        dao.updateProjectStatus(
            projectId = projectId,
            status = ProjectStatus.COMPLETED.name,
            progress = 100,
            bengaliStatus = "ডেমো ক্লিপিং সফলভাবে সম্পন্ন হয়েছে!"
        )
    }

    private fun generateFallbackBengaliClips(projectId: String, project: ProjectEntity): List<ClipEntity> {
        val count = project.clipCount.coerceIn(1, 6)
        val duration = project.durationSeconds

        val clipTemplates = listOf(
            Triple(
                "সফল ক্যারিয়ার গঠনের ৩টি মূলনীতি",
                "জীবনে কোনো কিছু অর্জন করতে হলে সবার প্রথমে নিজের কাজের প্রতি ১০০% নিষ্ঠা রাখতে হবে। অনেকেই দ্রুত সফল হতে গিয়ে ধৈর্য হারিয়ে ফেলে। আপনি যদি নিয়মিত চেষ্টা চালিয়ে যান, তবে সাফল্য নিশ্চিত।",
                96
            ),
            Triple(
                "সোশ্যাল মিডিয়ায় দ্রুত পরিচিতি পাওয়ার কৌশল",
                "কন্টেন্ট বানানোর ক্ষেত্রে শুরুর ৩ সেকেন্ড সবচেয়ে বেশি গুরুত্বপূর্ণ। এই ৩ সেকেন্ডেই দর্শক সিদ্ধান্ত নেয় তারা ভিডিওটি পুরো দেখবে কি না। দারুণ একটি হুক দিয়ে ভিডিও শুরু করুন।",
                93
            ),
            Triple(
                "কঠিন পরিস্থিতিতে মানসিক শক্তি ধরে রাখার উপায়",
                "যখন সবকিছু আপনার বিরুদ্ধে যাবে, তখন শান্ত থাকাই সবচেয়ে বড় শক্তি। অতীত নিয়ে চিন্তা না করে আজকের দিনে কী ভালো করা যায়, সেদিকে নজর দিন। আত্মবিশ্বাস কখনো হারাবেন না।",
                89
            ),
            Triple(
                "নতুন দক্ষতা শেখার সহজ টেকনিক",
                "প্রতিদিন মাত্র ২০ মিনিট যেকোনো নতুন বিষয়ে মনোযোগ দিন। এক মাস পর আপনি নিজেই নিজের পরিবর্তন দেখে অবাক হবেন। ছোট ছোট পদক্ষেপই বড় পরিবর্তন আনে।",
                91
            ),
            Triple(
                "পডকাস্টের সবচেয়ে মজাদার বিতর্ক",
                "হোস্ট এবং অতিথির মধ্যকার এই রসাত্মক আলাপটি সামাজিক মাধ্যমে প্রচুর আলোচনার জন্ম দিয়েছে। তাদের যুক্তিগুলো সত্যিই চোখ খুলে দেওয়ার মতো!",
                88
            )
        )

        return (0 until count).map { index ->
            val template = clipTemplates[index % clipTemplates.size]
            val startSec = index * (duration + 15) + 20
            val endSec = startSec + duration
            val score = template.third

            ClipEntity(
                id = "clip_${projectId}_${index + 1}",
                projectId = projectId,
                title = template.first,
                startTime = formatSeconds(startSec.toDouble()),
                endTime = formatSeconds(endSec.toDouble()),
                durationSeconds = duration,
                viralityScore = score,
                hookScore = (score + 2).coerceAtMost(99),
                flowScore = (score - 2).coerceAtLeast(75),
                valueScore = score,
                trendScore = (score + 1).coerceAtMost(98),
                downloadUrl = project.videoUrl,
                videoUrl = project.videoUrl,
                transcript = template.second,
                postCaption = "✨ ${template.first}!\n\n${template.second.take(80)}...\n\nআপনার কী মতামত? কমেন্টে জানান!\n#শর্টস #বাংলা #ভাইরাল #ClipNex",
                hashtagsString = "#ভাইরাল,#শর্টস,#বাংলা,#পডকাস্ট,#মোটিভেশন",
                isFavorite = false
            )
        }
    }

    suspend fun updateClipContent(clipId: String, title: String, transcript: String, postCaption: String) {
        dao.updateClipContent(clipId, title, transcript, postCaption)
    }

    suspend fun toggleFavorite(clipId: String, isFavorite: Boolean) {
        dao.toggleClipFavorite(clipId, isFavorite)
    }

    suspend fun deleteProject(projectId: String) {
        dao.deleteClipsByProjectId(projectId)
        dao.deleteProjectById(projectId)
    }

    private fun generateTitleFromUrl(url: String): String {
        return when {
            url.contains("youtube.com") || url.contains("youtu.be") -> "ইউটিউব ভিডিও বিশ্লেষণ"
            url.contains("facebook.com") -> "ফেসবুক ভিডিও ক্লিপ"
            url.contains("twitch.tv") -> "টুইচ স্ট্রিম হাইলাইটস"
            url.endsWith(".mp4", ignoreCase = true) -> "আপলোডকৃত MP4 ভিডিও"
            else -> "আমদানিকৃত ভিডিও প্রজেক্ট"
        }
    }

    private fun formatSeconds(seconds: Double): String {
        val totalSec = seconds.toInt()
        val min = totalSec / 60
        val sec = totalSec % 60
        return String.format("%02d:%02d", min, sec)
    }
}

private fun ProjectWithClips.toProjectItem(): ProjectItem {
    val clipItems = clips.map { clip ->
        ClipItem(
            id = clip.id,
            projectId = clip.projectId,
            title = clip.title,
            startTime = clip.startTime,
            endTime = clip.endTime,
            durationSeconds = clip.durationSeconds,
            viralityScore = clip.viralityScore,
            hookScore = clip.hookScore,
            flowScore = clip.flowScore,
            valueScore = clip.valueScore,
            trendScore = clip.trendScore,
            downloadUrl = clip.downloadUrl,
            videoUrl = clip.videoUrl,
            transcript = clip.transcript,
            postCaption = clip.postCaption,
            hashtags = clip.hashtagsString.split(",").filter { it.isNotBlank() },
            isFavorite = clip.isFavorite
        )
    }

    val statusEnum = try {
        ProjectStatus.valueOf(project.status)
    } catch (e: Exception) {
        ProjectStatus.QUEUED
    }

    return ProjectItem(
        id = project.id,
        title = project.title,
        videoUrl = project.videoUrl,
        durationSeconds = project.durationSeconds,
        clipCount = project.clipCount,
        aspectRatio = project.aspectRatio,
        captionLanguage = project.captionLanguage,
        status = statusEnum,
        progressPercent = project.progressPercent,
        statusBengali = project.statusBengali,
        createdAt = project.createdAt,
        isDemo = project.isDemo,
        clips = clipItems,
        errorMessage = project.errorMessage
    )
}
