package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MotionEventType(val label: String) {
    PEEKER_DETECTED("Peeker Detected"),
    MULTIPLE_FACES("Multiple Faces"),
    BACKGROUND_IGNORED("Background Motion Ignored"),
    LOW_LIGHT_WARNING("Low-Light Peeker Warning")
}

enum class SensitivityLevel(val displayName: String, val thresholdConfidence: Float, val debounceMs: Long) {
    LOW("Low", 0.80f, 500L),
    MEDIUM("Medium", 0.65f, 300L),
    HIGH("High", 0.45f, 150L)
}

@Entity(tableName = "motion_logs")
data class MotionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: MotionEventType,
    val confidence: Float,
    val lightLevelLux: Float,
    val sensitivity: String,
    val facesCount: Int,
    val isBatterySaverActive: Boolean,
    val details: String
)
