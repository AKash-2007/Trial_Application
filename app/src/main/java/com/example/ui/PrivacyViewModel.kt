package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.MotionEventType
import com.example.data.MotionLog
import com.example.data.MotionLogRepository
import com.example.data.SensitivityLevel
import com.example.detection.DetectionResult
import com.example.detection.PrivacyDetector
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PrivacyUiState(
    val isProtectionActive: Boolean = true,
    val isPrivacyBlurTriggered: Boolean = false,
    val blurReason: String = "",
    val peekerAngle: String = "",
    val peekerDistance: String = "",
    val facesCount: Int = 1,
    val confidence: Float = 0f,
    val ambientLightLux: Float = 65f,
    val isLowLight: Boolean = false,
    val isEnvironmentalMotionFiltered: Boolean = false,
    val environmentalMotionCount: Int = 0,
    val currentFps: Int = 12,
    val cpuEstimatePercent: Int = 12,
    val sensitivity: SensitivityLevel = SensitivityLevel.MEDIUM,
    val isBatterySaverEnabled: Boolean = false,
    val isLowLightBoostEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true,
    val selectedTab: Int = 0,
    val searchQuery: String = "",
    val selectedFilterType: MotionEventType? = null,
    val lastEventTimestamp: Long = 0L,
    val isSnoozed: Boolean = false
)

class PrivacyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MotionLogRepository
    private val vibrator: Vibrator?

    private val _uiState = MutableStateFlow(PrivacyUiState())
    val uiState: StateFlow<PrivacyUiState> = _uiState.asStateFlow()

    private var privacyDetector: PrivacyDetector? = null
    private var lastLoggedThreatTime = 0L
    private var lastLoggedEnvMotionTime = 0L
    private var snoozeJob: Job? = null
    private var simulationJob: Job? = null

    init {
        val database = AppDatabase.getInstance(application)
        repository = MotionLogRepository(database.motionLogDao())

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        initializeDetector()
        seedInitialDemoLogsIfEmpty()
    }

    private fun initializeDetector() {
        privacyDetector = PrivacyDetector(
            sensitivity = _uiState.value.sensitivity,
            isBatterySaverEnabled = _uiState.value.isBatterySaverEnabled,
            isLowLightBoostEnabled = _uiState.value.isLowLightBoostEnabled,
            onResult = { result ->
                onDetectionResult(result)
            }
        )
    }

    fun getDetector(): PrivacyDetector? = privacyDetector

    val logsList: StateFlow<List<MotionLog>> = combine(
        repository.allLogs,
        _uiState
    ) { logs, state ->
        var filtered = logs
        if (state.selectedFilterType != null) {
            filtered = filtered.filter { it.eventType == state.selectedFilterType }
        }
        if (state.searchQuery.isNotBlank()) {
            val q = state.searchQuery.lowercase().trim()
            filtered = filtered.filter {
                it.details.lowercase().contains(q) ||
                        it.eventType.label.lowercase().contains(q) ||
                        it.sensitivity.lowercase().contains(q)
            }
        }
        filtered
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val threatCount: StateFlow<Int> = repository.threatCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val ignoredCount: StateFlow<Int> = repository.ignoredCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun onDetectionResult(result: DetectionResult) {
        if (!_uiState.value.isProtectionActive) return

        val now = System.currentTimeMillis()
        val isThreat = result.isPeekerDetected && !_uiState.value.isSnoozed

        _uiState.update { current ->
            current.copy(
                isPrivacyBlurTriggered = isThreat,
                blurReason = if (isThreat) result.threatDescription else "",
                peekerAngle = result.peekerAngleDesc,
                peekerDistance = result.peekerDistanceDesc,
                facesCount = result.facesCount,
                confidence = result.confidence,
                ambientLightLux = result.ambientLightLux,
                isLowLight = result.isLowLight,
                isEnvironmentalMotionFiltered = result.isEnvironmentalMotionOnly,
                currentFps = result.fps,
                cpuEstimatePercent = result.cpuEstimatePercent,
                lastEventTimestamp = if (isThreat) now else current.lastEventTimestamp
            )
        }

        // Record threat log if newly detected (throttled to at most one per 4 seconds)
        if (isThreat && (now - lastLoggedThreatTime > 4000L)) {
            lastLoggedThreatTime = now
            triggerVibrationAlert()
            val eventType = if (result.isLowLight) {
                MotionEventType.LOW_LIGHT_WARNING
            } else if (result.facesCount > 1) {
                MotionEventType.PEEKER_DETECTED
            } else {
                MotionEventType.PEEKER_DETECTED
            }

            viewModelScope.launch {
                repository.insertLog(
                    MotionLog(
                        timestamp = now,
                        eventType = eventType,
                        confidence = result.confidence,
                        lightLevelLux = result.ambientLightLux,
                        sensitivity = _uiState.value.sensitivity.displayName,
                        facesCount = result.facesCount,
                        isBatterySaverActive = _uiState.value.isBatterySaverEnabled,
                        details = "${result.threatDescription}. ${result.peekerAngleDesc} ${result.peekerDistanceDesc}".trim()
                    )
                )
            }
        }

        // Record environmental motion ignored (throttled to at most one per 12 seconds)
        if (result.isEnvironmentalMotionOnly && (now - lastLoggedEnvMotionTime > 12000L)) {
            lastLoggedEnvMotionTime = now
            _uiState.update { it.copy(environmentalMotionCount = it.environmentalMotionCount + 1) }
            viewModelScope.launch {
                repository.insertLog(
                    MotionLog(
                        timestamp = now,
                        eventType = MotionEventType.BACKGROUND_IGNORED,
                        confidence = 0.88f,
                        lightLevelLux = result.ambientLightLux,
                        sensitivity = _uiState.value.sensitivity.displayName,
                        facesCount = result.facesCount,
                        isBatterySaverActive = _uiState.value.isBatterySaverEnabled,
                        details = "Peripheral background movement detected (passing shadow/object); no human face looking at screen. Privacy blur remained inactive."
                    )
                )
            }
        }
    }

    private fun triggerVibrationAlert() {
        if (!_uiState.value.isVibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 150, 100, 200),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 150, 100, 200), -1)
            }
        } catch (_: Exception) {}
    }

    fun toggleProtection() {
        _uiState.update {
            val nextState = !it.isProtectionActive
            it.copy(
                isProtectionActive = nextState,
                isPrivacyBlurTriggered = if (!nextState) false else it.isPrivacyBlurTriggered
            )
        }
    }

    fun dismissBlur() {
        snoozeJob?.cancel()
        _uiState.update { it.copy(isPrivacyBlurTriggered = false, isSnoozed = true) }
        snoozeJob = viewModelScope.launch {
            // Snooze for 4 seconds so user can dismiss blur and view their screen
            delay(4000L)
            _uiState.update { it.copy(isSnoozed = false) }
        }
    }

    fun setSensitivity(level: SensitivityLevel) {
        _uiState.update { it.copy(sensitivity = level) }
        privacyDetector?.sensitivity = level
    }

    fun toggleBatterySaver() {
        _uiState.update {
            val next = !it.isBatterySaverEnabled
            val newFps = if (next) 3 else 12
            val newCpu = if (next) 4 else 14
            it.copy(isBatterySaverEnabled = next, currentFps = newFps, cpuEstimatePercent = newCpu)
        }
        privacyDetector?.isBatterySaverEnabled = _uiState.value.isBatterySaverEnabled
    }

    fun toggleLowLightBoost() {
        _uiState.update { it.copy(isLowLightBoostEnabled = !it.isLowLightBoostEnabled) }
        privacyDetector?.isLowLightBoostEnabled = _uiState.value.isLowLightBoostEnabled
    }

    fun toggleVibration() {
        _uiState.update { it.copy(isVibrationEnabled = !it.isVibrationEnabled) }
    }

    fun setSelectedTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setFilterType(type: MotionEventType?) {
        _uiState.update { it.copy(selectedFilterType = type) }
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch {
            repository.deleteLogById(id)
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearAllLogs()
        }
    }

    // Interactive Testing & Simulation Scenarios:
    fun triggerSimulatedPeeker(side: String = "over right shoulder", lowLight: Boolean = false) {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            val conf = if (lowLight) 0.82f else 0.94f
            val lux = if (lowLight) 14f else 78f
            onDetectionResult(
                DetectionResult(
                    isPeekerDetected = true,
                    facesCount = 2,
                    threatDescription = "Secondary person detected looking at screen $side",
                    confidence = conf,
                    ambientLightLux = lux,
                    isLowLight = lowLight,
                    isEnvironmentalMotionOnly = false,
                    fps = _uiState.value.currentFps,
                    cpuEstimatePercent = _uiState.value.cpuEstimatePercent,
                    peekerAngleDesc = "Angle: 22° ($side)",
                    peekerDistanceDesc = "Proximity: Close (~0.9m)"
                )
            )
            // Keep blur active for 4.5 seconds for the test demo
            delay(4500L)
            onDetectionResult(
                DetectionResult(
                    isPeekerDetected = false,
                    facesCount = 1,
                    threatDescription = "Protected: 1 user active",
                    confidence = 0.95f,
                    ambientLightLux = lux,
                    isLowLight = lowLight,
                    isEnvironmentalMotionOnly = false,
                    fps = _uiState.value.currentFps,
                    cpuEstimatePercent = _uiState.value.cpuEstimatePercent
                )
            )
        }
    }

    fun triggerSimulatedEnvironmentalMotion() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            onDetectionResult(
                DetectionResult(
                    isPeekerDetected = false,
                    facesCount = 1,
                    threatDescription = "Background environmental motion filtered (curtain/shadow); screen remains clear",
                    confidence = 0.92f,
                    ambientLightLux = _uiState.value.ambientLightLux,
                    isLowLight = false,
                    isEnvironmentalMotionOnly = true,
                    fps = _uiState.value.currentFps,
                    cpuEstimatePercent = _uiState.value.cpuEstimatePercent
                )
            )
            delay(3000L)
            _uiState.update { it.copy(isEnvironmentalMotionFiltered = false) }
        }
    }

    private fun seedInitialDemoLogsIfEmpty() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.insertLog(
                MotionLog(
                    timestamp = now - 180000L,
                    eventType = MotionEventType.PEEKER_DETECTED,
                    confidence = 0.94f,
                    lightLevelLux = 85f,
                    sensitivity = "Medium",
                    facesCount = 2,
                    isBatterySaverActive = false,
                    details = "Secondary person detected peering over right shoulder (Yaw: 18°, Distance: ~1m). Privacy blur activated immediately."
                )
            )
            repository.insertLog(
                MotionLog(
                    timestamp = now - 360000L,
                    eventType = MotionEventType.BACKGROUND_IGNORED,
                    confidence = 0.89f,
                    lightLevelLux = 92f,
                    sensitivity = "Medium",
                    facesCount = 1,
                    isBatterySaverActive = false,
                    details = "Environmental movement in background window (passing pedestrian); no human face directed at phone screen. Filtered out safely."
                )
            )
            repository.insertLog(
                MotionLog(
                    timestamp = now - 720000L,
                    eventType = MotionEventType.LOW_LIGHT_WARNING,
                    confidence = 0.86f,
                    lightLevelLux = 18f,
                    sensitivity = "Medium",
                    facesCount = 2,
                    isBatterySaverActive = true,
                    details = "Low-light peeker detected in dim room. On-device contrast boost identified secondary gaze behind left shoulder."
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
        snoozeJob?.cancel()
        privacyDetector?.close()
    }
}
