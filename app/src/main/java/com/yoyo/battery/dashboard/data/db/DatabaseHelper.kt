package com.yoyo.battery.dashboard.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.yoyo.battery.dashboard.data.model.ChargingSession

enum class TimeRangeFilter(val label: String) {
    DAY("Day"),
    WEEK("Week"),
    MONTH("Month"),
    THREE_MONTHS("3 Months"),
    ALL_TIME("All Time")
}

class DatabaseHelper private constructor(context: Context) : SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "yoyo_battery_analytics.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_SESSIONS = "charging_sessions"
        private const val COL_ID = "id"
        private const val COL_START_TIME = "start_time"
        private const val COL_END_TIME = "end_time"
        private const val COL_START_LEVEL = "start_level"
        private const val COL_END_LEVEL = "end_level"
        private const val COL_CHARGE_ADDED_MAH = "charge_added_mah"
        private const val COL_PLUG_TYPE = "plug_type"
        private const val COL_PEAK_CURRENT = "peak_current_ma"
        private const val COL_AVG_CURRENT = "avg_current_ma"
        private const val COL_MAX_TEMP = "max_temp_celsius"
        private const val COL_MAX_VOLTAGE = "max_voltage_volts"
        private const val COL_MAX_POWER = "max_power_watts"
        private const val COL_ESTIMATED_CAPACITY = "estimated_full_capacity_mah"

        @Volatile
        private var instance: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: DatabaseHelper(context.applicationContext).also { instance = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_SESSIONS (
                $COL_ID TEXT PRIMARY KEY,
                $COL_START_TIME INTEGER,
                $COL_END_TIME INTEGER,
                $COL_START_LEVEL INTEGER,
                $COL_END_LEVEL INTEGER,
                $COL_CHARGE_ADDED_MAH REAL,
                $COL_PLUG_TYPE TEXT,
                $COL_PEAK_CURRENT INTEGER,
                $COL_AVG_CURRENT INTEGER,
                $COL_MAX_TEMP REAL,
                $COL_MAX_VOLTAGE REAL,
                $COL_MAX_POWER REAL,
                $COL_ESTIMATED_CAPACITY REAL
            )
        """.trimIndent()
        db.execSQL(createTable)
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_sessions_start_time ON $TABLE_SESSIONS ($COL_START_TIME DESC)")
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_sessions_start_time ON $TABLE_SESSIONS ($COL_START_TIME DESC)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    fun insertOrUpdateSession(session: ChargingSession) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ID, session.id)
            put(COL_START_TIME, session.startTimeMs)
            put(COL_END_TIME, session.endTimeMs)
            put(COL_START_LEVEL, session.startLevel)
            put(COL_END_LEVEL, session.endLevel)
            put(COL_CHARGE_ADDED_MAH, session.chargeAddedMah)
            put(COL_PLUG_TYPE, session.plugType)
            put(COL_PEAK_CURRENT, session.maxCurrentMa)
            put(COL_AVG_CURRENT, session.avgCurrentMa)
            put(COL_MAX_TEMP, session.maxTemperatureCelsius.toDouble())
            put(COL_MAX_VOLTAGE, session.maxVoltageVolts)
            put(COL_MAX_POWER, session.maxPowerWatts)
            put(COL_ESTIMATED_CAPACITY, session.estimatedFullCapacityMah)
        }
        db.insertWithOnConflict(TABLE_SESSIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun insertOrUpdateSessions(sessions: List<ChargingSession>): Int {
        val db = writableDatabase
        var count = 0
        db.beginTransaction()
        try {
            for (session in sessions) {
                val values = ContentValues().apply {
                    put(COL_ID, session.id)
                    put(COL_START_TIME, session.startTimeMs)
                    put(COL_END_TIME, session.endTimeMs)
                    put(COL_START_LEVEL, session.startLevel)
                    put(COL_END_LEVEL, session.endLevel)
                    put(COL_CHARGE_ADDED_MAH, session.chargeAddedMah)
                    put(COL_PLUG_TYPE, session.plugType)
                    put(COL_PEAK_CURRENT, session.maxCurrentMa)
                    put(COL_AVG_CURRENT, session.avgCurrentMa)
                    put(COL_MAX_TEMP, session.maxTemperatureCelsius.toDouble())
                    put(COL_MAX_VOLTAGE, session.maxVoltageVolts)
                    put(COL_MAX_POWER, session.maxPowerWatts)
                    put(COL_ESTIMATED_CAPACITY, session.estimatedFullCapacityMah)
                }
                val rowId = db.insertWithOnConflict(TABLE_SESSIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
                if (rowId != -1L) count++
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return count
    }

    fun getAllSessions(): List<ChargingSession> {
        return getSessionsByFilter(TimeRangeFilter.ALL_TIME)
    }

    fun getSessionsByFilter(filter: TimeRangeFilter): List<ChargingSession> {
        val list = mutableListOf<ChargingSession>()
        val db = readableDatabase

        val now = System.currentTimeMillis()
        val minTimeMs = when (filter) {
            TimeRangeFilter.DAY -> {
                val cal = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }
                cal.timeInMillis
            }
            TimeRangeFilter.WEEK -> now - (7L * 24 * 3600 * 1000L)
            TimeRangeFilter.MONTH -> now - (30L * 24 * 3600 * 1000L)
            TimeRangeFilter.THREE_MONTHS -> now - (90L * 24 * 3600 * 1000L)
            TimeRangeFilter.ALL_TIME -> 0L
        }

        val query = if (minTimeMs > 0L) {
            "SELECT * FROM $TABLE_SESSIONS WHERE $COL_START_TIME >= $minTimeMs ORDER BY $COL_START_TIME DESC"
        } else {
            "SELECT * FROM $TABLE_SESSIONS ORDER BY $COL_START_TIME DESC"
        }
        val cursor = db.rawQuery(query, null)

        cursor.use { c ->
            if (c.moveToFirst()) {
                val idxId = c.getColumnIndexOrThrow(COL_ID)
                val idxStartTime = c.getColumnIndexOrThrow(COL_START_TIME)
                val idxEndTime = c.getColumnIndexOrThrow(COL_END_TIME)
                val idxStartLvl = c.getColumnIndexOrThrow(COL_START_LEVEL)
                val idxEndLvl = c.getColumnIndexOrThrow(COL_END_LEVEL)
                val idxChargeAdded = c.getColumnIndexOrThrow(COL_CHARGE_ADDED_MAH)
                val idxPlug = c.getColumnIndexOrThrow(COL_PLUG_TYPE)
                val idxPeakCurrent = c.getColumnIndexOrThrow(COL_PEAK_CURRENT)
                val idxAvgCurrent = c.getColumnIndexOrThrow(COL_AVG_CURRENT)
                val idxMaxTemp = c.getColumnIndexOrThrow(COL_MAX_TEMP)
                val idxMaxVolt = c.getColumnIndexOrThrow(COL_MAX_VOLTAGE)
                val idxMaxPower = c.getColumnIndexOrThrow(COL_MAX_POWER)
                val idxEstCap = c.getColumnIndexOrThrow(COL_ESTIMATED_CAPACITY)

                do {
                    val session = ChargingSession(
                        id = c.getString(idxId),
                        startTimeMs = c.getLong(idxStartTime),
                        endTimeMs = if (c.isNull(idxEndTime)) null else c.getLong(idxEndTime),
                        startLevel = c.getInt(idxStartLvl),
                        endLevel = if (c.isNull(idxEndLvl)) null else c.getInt(idxEndLvl),
                        chargeAddedMah = c.getDouble(idxChargeAdded),
                        plugType = c.getString(idxPlug),
                        maxCurrentMa = c.getInt(idxPeakCurrent),
                        avgCurrentMa = c.getInt(idxAvgCurrent),
                        maxTemperatureCelsius = c.getFloat(idxMaxTemp),
                        maxVoltageVolts = c.getDouble(idxMaxVolt),
                        maxPowerWatts = c.getDouble(idxMaxPower),
                        estimatedFullCapacityMah = c.getDouble(idxEstCap)
                    )
                    list.add(session)
                } while (c.moveToNext())
            }
        }
        return list
    }

    fun clearAllSessions() {
        val db = writableDatabase
        db.delete(TABLE_SESSIONS, null, null)
    }
}
