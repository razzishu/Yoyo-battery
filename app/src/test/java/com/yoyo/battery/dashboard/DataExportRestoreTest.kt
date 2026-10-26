package com.yoyo.battery.dashboard

import com.yoyo.battery.dashboard.data.export.DataExportManager
import com.yoyo.battery.dashboard.data.model.ChargingSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DataExportRestoreTest {

    @Test
    fun testJsonExportAndRestoreRoundTrip() {
        val originalSessions = listOf(
            ChargingSession(
                id = "session_001",
                startTimeMs = 1700000000000L,
                endTimeMs = 1700003600000L,
                startLevel = 15,
                endLevel = 85,
                chargeAddedMah = 3500.0,
                plugType = "AC",
                maxPowerWatts = 45.0,
                avgCurrentMa = 2200,
                estimatedFullCapacityMah = 5000.0
            ),
            ChargingSession(
                id = "session_002",
                startTimeMs = 1700010000000L,
                endTimeMs = 1700012000000L,
                startLevel = 40,
                endLevel = 80,
                chargeAddedMah = 2000.0,
                plugType = "USB",
                maxPowerWatts = 18.0,
                avgCurrentMa = 1500,
                estimatedFullCapacityMah = 5000.0
            )
        )

        // Convert to JSON
        val jsonPayload = DataExportManager.convertSessionsToJson(originalSessions)
        assertTrue(jsonPayload.contains("session_001"))
        assertTrue(jsonPayload.contains("session_002"))

        // Restore from JSON
        val restored = DataExportManager.parseSessionsFromJson(jsonPayload)
        assertEquals(2, restored.size)

        val first = restored[0]
        assertEquals("session_001", first.id)
        assertEquals(1700000000000L, first.startTimeMs)
        assertEquals(1700003600000L, first.endTimeMs)
        assertEquals(15, first.startLevel)
        assertEquals(85, first.endLevel)
        assertEquals(3500.0, first.chargeAddedMah, 0.01)
        assertEquals("AC", first.plugType)
        assertEquals(45.0, first.maxPowerWatts, 0.01)
        assertEquals(2200, first.avgCurrentMa)
        assertEquals(5000.0, first.estimatedFullCapacityMah, 0.01)
    }

    @Test
    fun testCsvExportAndRestoreRoundTrip() {
        val originalSessions = listOf(
            ChargingSession(
                id = "session_csv_1",
                startTimeMs = 1710000000000L,
                endTimeMs = 1710005000000L,
                startLevel = 20,
                endLevel = 90,
                chargeAddedMah = 3400.0,
                plugType = "WIRELESS",
                maxPowerWatts = 15.0,
                avgCurrentMa = 1200,
                estimatedFullCapacityMah = 4857.0
            )
        )

        val csvPayload = DataExportManager.convertSessionsToCsv(originalSessions)
        assertTrue(csvPayload.contains("session_csv_1"))
        assertTrue(csvPayload.contains("WIRELESS"))

        val restored = DataExportManager.parseSessionsFromCsv(csvPayload)
        assertEquals(1, restored.size)

        val item = restored[0]
        assertEquals("session_csv_1", item.id)
        assertEquals(1710000000000L, item.startTimeMs)
        assertEquals(1710005000000L, item.endTimeMs)
        assertEquals(20, item.startLevel)
        assertEquals(90, item.endLevel)
        assertEquals(3400.0, item.chargeAddedMah, 0.1)
        assertEquals("WIRELESS", item.plugType)
        assertEquals(15.0, item.maxPowerWatts, 0.1)
        assertEquals(1200, item.avgCurrentMa)
        assertEquals(4857.0, item.estimatedFullCapacityMah, 0.1)
    }

    @Test
    fun testCorruptPayloadsReturnGracefully() {
        // Corrupted JSON
        val corruptJson = "{ this is invalid json"
        val restoredJson = DataExportManager.parseSessionsFromJson(corruptJson)
        assertTrue("Corrupted JSON should return empty list without crash", restoredJson.isEmpty())

        // Empty string
        val emptyRestored = DataExportManager.parseSessionsFromJson("")
        assertTrue(emptyRestored.isEmpty())

        // Corrupted CSV lines
        val badCsv = "ID,Start Time,End Time,Start Level (%),End Level (%)\nbad_line_with_missing_columns\n"
        val restoredCsv = DataExportManager.parseSessionsFromCsv(badCsv)
        assertTrue("Bad CSV lines should be skipped safely", restoredCsv.isEmpty())
    }
}
