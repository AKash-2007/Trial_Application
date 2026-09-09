package com.example.detection

import android.graphics.Rect
import android.os.SystemClock
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.data.SensitivityLevel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.nio.ByteBuffer
import kotlin.math.abs

data class DetectionResult(
    val isPeekerDetected: Boolean = false,
    val facesCount: Int = 0,
    val threatDescription: String = "",
    val confidence: Float = 0f,
    val ambientLightLux: Float = 50f,
    val isLowLight: Boolean = false,
    val isEnvironmentalMotionOnly: Boolean = false,
    val fps: Int = 0,
    val cpuEstimatePercent: Int = 5,
    val peekerAngleDesc: String = "",
    val peekerDistanceDesc: String = ""
)

class PrivacyDetector(
    var sensitivity: SensitivityLevel = SensitivityLevel.MEDIUM,
    var isBatterySaverEnabled: Boolean = false,
    var isLowLightBoostEnabled: Boolean = true,
    private val onResult: (DetectionResult) -> Unit
) : ImageAnalysis.Analyzer {

    private val detector: FaceDetector

    // Performance and throttling
    private var lastAnalysisTimestamp = 0L
    private var frameCounter = 0
    private var lastFpsCalculationTime = SystemClock.elapsedRealtime()
    private var currentFps = 0

    // Background environmental motion tracking (8x8 downsampled luminance grid)
    private var previousGrid: IntArray? = null
    private val gridCols = 8
    private val gridRows = 8

    // Debounce tracking
    private var peekerStreakStart = 0L
    private var lastPeekerDetectedTime = 0L

    init {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setMinFaceSize(0.08f)
            .enableTracking()
            .build()
        detector = FaceDetection.getClient(options)
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val currentTime = SystemClock.elapsedRealtime()

        // FPS calculation
        frameCounter++
        if (currentTime - lastFpsCalculationTime >= 1000L) {
            currentFps = frameCounter
            frameCounter = 0
            lastFpsCalculationTime = currentTime
        }

        // Throttle for Battery-Saving Mode:
        // Battery saving mode processes at max ~3 FPS (330ms interval)
        // Standard mode processes at ~10-12 FPS (85ms interval)
        val minInterval = if (isBatterySaverEnabled) 330L else 85L
        if (currentTime - lastAnalysisTimestamp < minInterval) {
            imageProxy.close()
            return
        }
        lastAnalysisTimestamp = currentTime

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        // Measure ambient luminance (Y plane) for low-light evaluation
        val yPlane = mediaImage.planes[0]
        val buffer = yPlane.buffer
        val (avgLuma, currentGrid) = computeLumaAndGrid(buffer, mediaImage.width, mediaImage.height, yPlane.rowStride)
        val isLowLight = avgLuma < 40f
        val estimatedLux = (avgLuma / 255f) * 120f // Approximate lux representation

        // Check for environmental background motion
        val backgroundMotionScore = evaluateBackgroundMotion(currentGrid, previousGrid)
        previousGrid = currentGrid
        val hasEnvironmentalMotion = backgroundMotionScore > 0.15f

        val rotation = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotation)

        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                processFaces(
                    faces = faces,
                    imageWidth = mediaImage.width,
                    imageHeight = mediaImage.height,
                    avgLuma = avgLuma,
                    estimatedLux = estimatedLux,
                    isLowLight = isLowLight,
                    hasEnvironmentalMotion = hasEnvironmentalMotion,
                    currentTime = currentTime
                )
            }
            .addOnFailureListener {
                // If on-device ML fails or face detector encountered an issue, fallback safely
                onResult(
                    DetectionResult(
                        isPeekerDetected = false,
                        facesCount = 0,
                        threatDescription = "On-device analyzer standby",
                        ambientLightLux = estimatedLux,
                        isLowLight = isLowLight,
                        isEnvironmentalMotionOnly = hasEnvironmentalMotion,
                        fps = currentFps,
                        cpuEstimatePercent = if (isBatterySaverEnabled) 3 else 12
                    )
                )
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun processFaces(
        faces: List<Face>,
        imageWidth: Int,
        imageHeight: Int,
        avgLuma: Float,
        estimatedLux: Float,
        isLowLight: Boolean,
        hasEnvironmentalMotion: Boolean,
        currentTime: Long
    ) {
        val facesCount = faces.size
        val cpuUsage = if (isBatterySaverEnabled) 4 else 14

        if (faces.isEmpty()) {
            peekerStreakStart = 0L
            val isMotionOnly = hasEnvironmentalMotion
            onResult(
                DetectionResult(
                    isPeekerDetected = false,
                    facesCount = 0,
                    threatDescription = if (isMotionOnly) "Background environmental motion filtered" else "Scanning...",
                    confidence = 0f,
                    ambientLightLux = estimatedLux,
                    isLowLight = isLowLight,
                    isEnvironmentalMotionOnly = isMotionOnly,
                    fps = currentFps,
                    cpuEstimatePercent = cpuUsage
                )
            )
            return
        }

        // Identify primary user face (the largest bounding box, closest to screen)
        val sortedFaces = faces.sortedByDescending { it.boundingBox.width() * it.boundingBox.height() }
        val primaryFace = sortedFaces.first()

        // Potential peekers are any other faces, OR the primary face if it's looking from an intruder angle
        val secondaryFaces = sortedFaces.drop(1)

        var peekerFound = false
        var peekerConfidence = 0f
        var threatDesc = ""
        var angleDesc = ""
        var distanceDesc = ""

        // Adjust confidence threshold according to sensitivity and low-light booster
        var effectiveConfidenceThreshold = sensitivity.thresholdConfidence
        if (isLowLight && isLowLightBoostEnabled) {
            // In low light, adaptively lower threshold slightly so dark peekers aren't missed
            effectiveConfidenceThreshold = (effectiveConfidenceThreshold - 0.10f).coerceAtLeast(0.35f)
        }

        if (secondaryFaces.isNotEmpty()) {
            for (face in secondaryFaces) {
                val box = face.boundingBox
                val faceArea = (box.width().toFloat() * box.height()) / (imageWidth * imageHeight)
                val yaw = face.headEulerAngleY // negative = left, positive = right
                val pitch = face.headEulerAngleX

                // Check if secondary person is looking towards the phone screen
                // Gaze towards screen is typically within +/- 35 degrees of yaw
                val isLookingAtScreen = abs(yaw) <= 35f && abs(pitch) <= 30f

                // Base confidence from face size and angle alignment
                var conf = 0.60f
                if (isLookingAtScreen) conf += 0.25f
                if (faceArea > 0.05f) conf += 0.15f

                if (conf >= effectiveConfidenceThreshold) {
                    peekerFound = true
                    peekerConfidence = conf.coerceAtMost(0.99f)
                    val side = if (yaw > 10) "over right shoulder" else if (yaw < -10) "over left shoulder" else "directly behind"
                    angleDesc = "Angle: ${yaw.toInt()}° ($side)"
                    distanceDesc = if (faceArea > 0.12f) "Proximity: Very Close (<0.8m)" else "Proximity: Medium (~1-1.5m)"
                    threatDesc = "Secondary person looking at screen $side"
                    break
                }
            }
        }

        // Debounce handling: require the peeker detection to sustain across debounceMs
        val isTriggered: Boolean
        if (peekerFound) {
            if (peekerStreakStart == 0L) {
                peekerStreakStart = currentTime
            }
            val elapsed = currentTime - peekerStreakStart
            isTriggered = elapsed >= sensitivity.debounceMs
            if (isTriggered) {
                lastPeekerDetectedTime = currentTime
            }
        } else {
            // Retain blur for a brief grace window (800ms) to avoid jarring flicker if user blinks or moves slightly
            val graceWindow = 800L
            if (currentTime - lastPeekerDetectedTime < graceWindow) {
                isTriggered = true
                threatDesc = "Screen guarded (clearing...)"
                peekerConfidence = 0.70f
            } else {
                peekerStreakStart = 0L
                isTriggered = false
            }
        }

        // Environmental motion without a peeker:
        val isEnvironmentalOnly = !isTriggered && hasEnvironmentalMotion

        onResult(
            DetectionResult(
                isPeekerDetected = isTriggered,
                facesCount = facesCount,
                threatDescription = if (isTriggered) threatDesc else if (isEnvironmentalOnly) "Background environmental movement filtered" else "Protected: 1 user active",
                confidence = if (isTriggered) peekerConfidence else if (facesCount == 1) 0.95f else 0f,
                ambientLightLux = estimatedLux,
                isLowLight = isLowLight,
                isEnvironmentalMotionOnly = isEnvironmentalOnly,
                fps = currentFps,
                cpuEstimatePercent = cpuUsage,
                peekerAngleDesc = angleDesc,
                peekerDistanceDesc = distanceDesc
            )
        )
    }

    private fun computeLumaAndGrid(
        buffer: ByteBuffer,
        width: Int,
        height: Int,
        rowStride: Int
    ): Pair<Float, IntArray> {
        val grid = IntArray(gridCols * gridRows)
        val gridCounts = IntArray(gridCols * gridRows)
        var totalLuma = 0L
        var sampledPixels = 0

        // Subsample pixels for fast local analysis (every 4th pixel and 4th row)
        val step = 4
        buffer.rewind()

        val colStep = width / gridCols
        val rowStep = height / gridRows

        for (y in 0 until height step step) {
            val rowOffset = y * rowStride
            val gridY = (y / rowStep).coerceIn(0, gridRows - 1)
            for (x in 0 until width step step) {
                val pixelIndex = rowOffset + x
                if (pixelIndex < buffer.remaining()) {
                    val luma = buffer.get(pixelIndex).toInt() and 0xFF
                    totalLuma += luma
                    sampledPixels++

                    val gridX = (x / colStep).coerceIn(0, gridCols - 1)
                    val cellIndex = gridY * gridCols + gridX
                    grid[cellIndex] += luma
                    gridCounts[cellIndex]++
                }
            }
        }

        for (i in grid.indices) {
            if (gridCounts[i] > 0) {
                grid[i] = grid[i] / gridCounts[i]
            }
        }

        val avgLuma = if (sampledPixels > 0) totalLuma.toFloat() / sampledPixels else 128f
        return Pair(avgLuma, grid)
    }

    private fun evaluateBackgroundMotion(current: IntArray, previous: IntArray?): Float {
        if (previous == null || current.size != previous.size) return 0f
        var totalDiff = 0
        // Focus primarily on the peripheral borders (background motion)
        var peripheralCells = 0
        for (y in 0 until gridRows) {
            for (x in 0 until gridCols) {
                // If it's on outer edge (border 1-2 cells)
                val isPeripheral = x <= 1 || x >= gridCols - 2 || y <= 1 || y >= gridRows - 2
                if (isPeripheral) {
                    val idx = y * gridCols + x
                    totalDiff += abs(current[idx] - previous[idx])
                    peripheralCells++
                }
            }
        }
        if (peripheralCells == 0) return 0f
        val avgDiff = totalDiff.toFloat() / peripheralCells
        // Average difference above 15 represents notable movement (passing object, shadow, fan)
        return (avgDiff / 255f)
    }

    fun close() {
        try {
            detector.close()
        } catch (_: Exception) {}
    }
}
