package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.MotionEventType
import com.example.data.MotionLog
import com.example.data.SensitivityLevel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Privacy Guard", appName)
    }

    @Test
    fun `insert and retrieve motion logs`() = runBlocking {
        val dao = database.motionLogDao()
        val log = MotionLog(
            eventType = MotionEventType.PEEKER_DETECTED,
            confidence = 0.92f,
            lightLevelLux = 65f,
            sensitivity = SensitivityLevel.MEDIUM.displayName,
            facesCount = 2,
            isBatterySaverActive = false,
            details = "Secondary person looking at screen"
        )
        val id = dao.insertLog(log)
        assertTrue(id > 0)

        val logs = dao.getAllLogs().first()
        assertEquals(1, logs.size)
        assertEquals(MotionEventType.PEEKER_DETECTED, logs[0].eventType)
        assertEquals(0.92f, logs[0].confidence, 0.01f)
    }

    @Test
    fun `verify environmental motion ignored query`() = runBlocking {
        val dao = database.motionLogDao()
        dao.insertLog(
            MotionLog(
                eventType = MotionEventType.BACKGROUND_IGNORED,
                confidence = 0.85f,
                lightLevelLux = 50f,
                sensitivity = "Medium",
                facesCount = 1,
                isBatterySaverActive = false,
                details = "Background movement filtered"
            )
        )
        val ignoredCount = dao.getIgnoredCount().first()
        assertEquals(1, ignoredCount)
    }
}
