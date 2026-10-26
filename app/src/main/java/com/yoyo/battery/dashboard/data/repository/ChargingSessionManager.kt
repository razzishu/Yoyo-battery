package com.yoyo.battery.dashboard.data.repository

import android.content.Context
import com.yoyo.battery.dashboard.data.db.DatabaseHelper
import com.yoyo.battery.dashboard.data.db.TimeRangeFilter
import com.yoyo.battery.dashboard.data.model.BatteryInfo
import com.yoyo.battery.dashboard.data.model.ChargingSession
import com.yoyo.battery.dashboard.data.model.PluggedSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class ChargingSessionManager private constructor(context: Context) {

    private val dbHelper = DatabaseHelper.getInstance(context)
    private var activeSession: ChargingSession? = null
    private var lastUpdateTimeMs: Long = 0L
    private var totalCurrentSumMa: Long = 0L
    private var sampleCount: Int = 0
    private var lastDbWriteTimeMs: Long = 0L
    private var lastPersistedLevel: Int = -1

    companion object {
        @Volatile
        private var instance: ChargingSessionManager? = null

        fun getInstance(context: Context): ChargingSessionManager {
            return instance ?: synchronized(this) {
                instance ?: ChargingSessionManager(context.applicationContext).also { instance = it }
            }
        }
    }

    init {
        restoreOrCleanSessions()
    }

    @Synchronized
    private fun restoreOrCleanSessions() {
        val allSessions = dbHelper.getAllSessions()
        val now = System.currentTimeMillis()
        val unclosed = allSessions.firstOrNull { it.endTimeMs == null }

        if (unclosed != null) {
            // If the unclosed session is recent (within last 15 mins), restore it as active
            if (now - unclosed.startTimeMs < 15 * 60 * 1000L) {
                activeSession = unclosed
                lastUpdateTimeMs = now
                totalCurrentSumMa = unclosed.avgCurrentMa.toLong()
                sampleCount = 1
            } else {
                // Otherwise close it cleanly
                val completed = unclosed.copy(
                    endTimeMs = unclosed.startTimeMs + 60000L,
                    endLevel = unclosed.startLevel
                )
                dbHelper.insertOrUpdateSession(completed)
            }
        }
    }

    @Synchronized
    fun processBatteryUpdate(info: BatteryInfo, filter: TimeRangeFilter = TimeRangeFilter.ALL_TIME): List<ChargingSession> {
        val now = System.currentTimeMillis()
        val isActuallyCharging = info.isCharging && info.pluggedSource != PluggedSource.UNPLUGGED

        if (isActuallyCharging) {
            if (activeSession == null) {
                // Start a new charging session
                val newSession = ChargingSession(
                    id = UUID.randomUUID().toString(),
                    startTimeMs = now,
                    startLevel = info.level,
                    plugType = info.pluggedSource.name,
                    maxCurrentMa = info.currentNowMa,
                    avgCurrentMa = info.currentNowMa,
                    maxTemperatureCelsius = info.tempCelsius,
                    maxVoltageVolts = info.voltageVolts,
                    maxPowerWatts = info.powerWatts
                )
                activeSession = newSession
                lastUpdateTimeMs = now
                lastDbWriteTimeMs = now
                lastPersistedLevel = info.level
                totalCurrentSumMa = info.currentNowMa.toLong().coerceAtLeast(0L)
                sampleCount = 1
                dbHelper.insertOrUpdateSession(newSession)
            } else {
                // Deduplicate if invoked in rapid succession (< 400ms) by service + viewmodel
                val elapsedSinceLast = now - lastUpdateTimeMs
                if (elapsedSinceLast >= 400L) {
                    val current = activeSession!!
                    val deltaSec = (elapsedSinceLast / 1000.0).coerceIn(0.1, 10.0)
                    lastUpdateTimeMs = now

                    val currentMa = info.currentNowMa.coerceAtLeast(0)
                    val addedMah = currentMa * (deltaSec / 3600.0)
                    val newTotalChargeMah = current.chargeAddedMah + addedMah

                    totalCurrentSumMa += currentMa
                    sampleCount++
                    val newAvgCurrent = (totalCurrentSumMa / sampleCount).toInt()

                    val gainedPct = (info.level - current.startLevel).coerceAtLeast(0)
                    val sessionEstCap = if (gainedPct >= 5 && newTotalChargeMah > 10.0) {
                        newTotalChargeMah / (gainedPct / 100.0)
                    } else {
                        current.estimatedFullCapacityMah
                    }

                    val updated = current.copy(
                        chargeAddedMah = newTotalChargeMah,
                        maxCurrentMa = maxOf(current.maxCurrentMa, info.currentNowMa),
                        avgCurrentMa = newAvgCurrent,
                        maxTemperatureCelsius = maxOf(current.maxTemperatureCelsius, info.tempCelsius),
                        maxVoltageVolts = maxOf(current.maxVoltageVolts, info.voltageVolts),
                        maxPowerWatts = maxOf(current.maxPowerWatts, info.powerWatts),
                        estimatedFullCapacityMah = sessionEstCap
                    )
                    activeSession = updated

                    // Throttle SQLite disk writes: flush on level increase or every 15 seconds
                    val shouldFlushToDb = (info.level != lastPersistedLevel) || (now - lastDbWriteTimeMs >= 15000L)
                    if (shouldFlushToDb) {
                        dbHelper.insertOrUpdateSession(updated)
                        lastDbWriteTimeMs = now
                        lastPersistedLevel = info.level
                    }
                }
            }
        } else {
            // Unplugged: finalize active session if one was running
            if (activeSession != null) {
                val current = activeSession!!
                val gainedPct = (info.level - current.startLevel).coerceAtLeast(0)
                val sessionEstCap = if (gainedPct >= 5 && current.chargeAddedMah > 10.0) {
                    current.chargeAddedMah / (gainedPct / 100.0)
                } else {
                    current.estimatedFullCapacityMah
                }

                val completed = current.copy(
                    endTimeMs = now,
                    endLevel = info.level,
                    estimatedFullCapacityMah = sessionEstCap
                )
                dbHelper.insertOrUpdateSession(completed)
                activeSession = null
                lastUpdateTimeMs = 0L
                lastDbWriteTimeMs = 0L
                lastPersistedLevel = -1
                totalCurrentSumMa = 0L
                sampleCount = 0
            }
        }

        return dbHelper.getSessionsByFilter(filter)
    }

    suspend fun processBatteryUpdateAsync(info: BatteryInfo, filter: TimeRangeFilter = TimeRangeFilter.ALL_TIME): List<ChargingSession> {
        return withContext(Dispatchers.IO) {
            processBatteryUpdate(info, filter)
        }
    }

    fun getSessionsByFilter(filter: TimeRangeFilter): List<ChargingSession> {
        return dbHelper.getSessionsByFilter(filter)
    }

    suspend fun getSessionsByFilterAsync(filter: TimeRangeFilter): List<ChargingSession> {
        return withContext(Dispatchers.IO) {
            dbHelper.getSessionsByFilter(filter)
        }
    }

    suspend fun restoreSessionsAsync(sessions: List<ChargingSession>): Int {
        return withContext(Dispatchers.IO) {
            dbHelper.insertOrUpdateSessions(sessions)
        }
    }

    @Synchronized
    fun clearAllSessions() {
        activeSession = null
        lastUpdateTimeMs = 0L
        totalCurrentSumMa = 0L
        sampleCount = 0
        dbHelper.clearAllSessions()
    }
}
