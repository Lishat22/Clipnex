package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ClipNexDatabase
import com.example.data.model.*
import com.example.data.remote.KattoApiService
import com.example.data.repository.ClipRepository
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ClipNexViewModel(application: Application) : AndroidViewModel(application) {
    private val database = ClipNexDatabase.getDatabase(application)
    private val prefs = UserPreferencesRepository(application)
    private val apiService = KattoApiService.create()
    val repository = ClipRepository(database.clipNexDao(), apiService, prefs)

    val projects: StateFlow<List<ProjectItem>> = repository.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isDemoMode: StateFlow<Boolean> = prefs.isDemoMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val hasSeenOnboarding: StateFlow<Boolean> = prefs.hasSeenOnboarding
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val customApiKey: StateFlow<String?> = prefs.customApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isApiKeyConfigured = MutableStateFlow(false)
    val isApiKeyConfigured: StateFlow<Boolean> = _isApiKeyConfigured.asStateFlow()

    private val _activeProjectId = MutableStateFlow<String?>(null)
    val activeProjectId: StateFlow<String?> = _activeProjectId.asStateFlow()

    val activeProject: StateFlow<ProjectItem?> = _activeProjectId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getProject(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Form State
    val inputUrl = MutableStateFlow("")
    val selectedDuration = MutableStateFlow(DurationOption.S30)
    val selectedClipCount = MutableStateFlow(3)
    val selectedAspectRatio = MutableStateFlow(AspectRatioOption.NINE_SIXTEEN)
    val selectedCaptionLanguage = MutableStateFlow(CaptionLanguageOption.BENGALI)

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        checkApiKeyStatus()
    }

    fun checkApiKeyStatus() {
        viewModelScope.launch {
            _isApiKeyConfigured.value = repository.isApiKeyConfigured()
        }
    }

    fun setInputUrl(url: String) {
        inputUrl.value = url
        _errorMessage.value = null
    }

    fun setDuration(duration: DurationOption) {
        selectedDuration.value = duration
    }

    fun setClipCount(count: Int) {
        selectedClipCount.value = count
    }

    fun setAspectRatio(ratio: AspectRatioOption) {
        selectedAspectRatio.value = ratio
    }

    fun setCaptionLanguage(language: CaptionLanguageOption) {
        selectedCaptionLanguage.value = language
    }

    fun selectProject(projectId: String) {
        _activeProjectId.value = projectId
    }

    fun setDemoMode(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setDemoMode(enabled)
        }
    }

    fun setOnboardingSeen(seen: Boolean) {
        viewModelScope.launch {
            prefs.setOnboardingSeen(seen)
        }
    }

    fun saveCustomApiKey(key: String) {
        viewModelScope.launch {
            prefs.setCustomApiKey(key)
            checkApiKeyStatus()
        }
    }

    fun startClipping(onSuccess: (String) -> Unit) {
        val url = inputUrl.value.trim()
        if (url.isBlank()) {
            _errorMessage.value = "অনুগ্রহ করে একটি সঠিক ভিডিও লিংক (URL) প্রদান করুন।"
            return
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            _errorMessage.value = "লিংকটি অবশ্যই http:// বা https:// দিয়ে শুরু হতে হবে।"
            return
        }

        viewModelScope.launch {
            val projectId = repository.createClippingJob(
                videoUrl = url,
                targetDuration = selectedDuration.value.seconds,
                clipCount = selectedClipCount.value,
                aspectRatio = selectedAspectRatio.value.ratioLabel,
                captionLanguage = selectedCaptionLanguage.value.code,
                forceDemoMode = isDemoMode.value
            )
            _activeProjectId.value = projectId
            onSuccess(projectId)

            // Trigger background processing & polling
            repository.processJob(projectId)
        }
    }

    fun submitCustomVideoWithSettings(
        videoUrl: String,
        settings: VideoClippingSettings,
        onResult: (KattoSubmissionResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.submitVideoWithSettings(
                videoUrl = videoUrl,
                settings = settings,
                forceDemoMode = isDemoMode.value
            )
            if (result is KattoSubmissionResult.Success) {
                _activeProjectId.value = result.projectId
                repository.processJob(result.projectId)
            }
            onResult(result)
        }
    }

    fun retryProject(projectId: String) {
        viewModelScope.launch {
            _activeProjectId.value = projectId
            repository.processJob(projectId)
        }
    }

    fun updateClipContent(clipId: String, title: String, transcript: String, postCaption: String) {
        viewModelScope.launch {
            repository.updateClipContent(clipId, title, transcript, postCaption)
        }
    }

    fun toggleFavorite(clipId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(clipId, isFavorite)
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            if (_activeProjectId.value == projectId) {
                _activeProjectId.value = null
            }
            repository.deleteProject(projectId)
        }
    }

    fun fillSampleUrl(url: String) {
        inputUrl.value = url
        _errorMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
