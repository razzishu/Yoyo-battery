package com.yoyo.battery.dashboard.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.yoyo.battery.dashboard.data.model.ChargingSession
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DataExportManager {

    private const val FILE_PROVIDER_AUTHORITY = "com.yoyo.battery.dashboard.fileprovider"

    private fun getExportsDir(context: Context): File {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        return exportDir
    }

    fun convertSessionsToCsv(sessions: List<ChargingSession>): String {
        val sb = java.lang.StringBuilder()
        sb.append("ID,Start Time,End Time,Start Level (%),End Level (%),Gained (%),Duration (min),Charge Added (mAh),Avg Current (mA),Max Power (W),Calculated Capacity (mAh),Plug Type\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        for (s in sessions) {
            val startDateStr = dateFormat.format(Date(s.startTimeMs))
            val endDateStr = if (s.endTimeMs != null) dateFormat.format(Date(s.endTimeMs)) else "Ongoing"
            val durationMin = s.durationMinutes
            val endLvl = s.endLevel ?: s.startLevel
            val chargeMah = String.format(Locale.US, "%.1f", s.chargeAddedMah)
            val avgCurrent = s.avgCurrentMa
            val maxWatts = String.format(Locale.US, "%.2f", s.maxPowerWatts)
            val calcCap = String.format(Locale.US, "%.0f", s.estimatedFullCapacityMah)
            val plug = s.plugType

            sb.append("\"${s.id}\",\"$startDateStr\",\"$endDateStr\",${s.startLevel},$endLvl,${s.gainedPercent},$durationMin,$chargeMah,$avgCurrent,$maxWatts,$calcCap,\"$plug\"\n")
        }
        return sb.toString()
    }

    fun convertSessionsToJson(sessions: List<ChargingSession>): String {
        val sb = java.lang.StringBuilder()
        sb.append("[\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        sessions.forEachIndexed { index, s ->
            val startDateStr = dateFormat.format(Date(s.startTimeMs))
            val endDateStr = if (s.endTimeMs != null) dateFormat.format(Date(s.endTimeMs)) else "Ongoing"
            val endLvl = s.endLevel ?: s.startLevel
            sb.append("  {\n")
            sb.append("    \"id\": \"${s.id}\",\n")
            sb.append("    \"startTime\": \"$startDateStr\",\n")
            sb.append("    \"endTime\": \"$endDateStr\",\n")
            sb.append("    \"startLevel\": ${s.startLevel},\n")
            sb.append("    \"endLevel\": $endLvl,\n")
            sb.append("    \"gainedPercent\": ${s.gainedPercent},\n")
            sb.append("    \"durationMinutes\": ${s.durationMinutes},\n")
            sb.append("    \"chargeAddedMah\": ${String.format(Locale.US, "%.2f", s.chargeAddedMah)},\n")
            sb.append("    \"avgCurrentMa\": ${s.avgCurrentMa},\n")
            sb.append("    \"maxPowerWatts\": ${String.format(Locale.US, "%.2f", s.maxPowerWatts)},\n")
            sb.append("    \"estimatedFullCapacityMah\": ${String.format(Locale.US, "%.1f", s.estimatedFullCapacityMah)},\n")
            sb.append("    \"plugType\": \"${s.plugType}\"\n")
            sb.append("  }${if (index < sessions.size - 1) "," else ""}\n")
        }
        sb.append("]\n")
        return sb.toString()
    }

    fun exportSessionsToCsv(context: Context, sessions: List<ChargingSession>): Uri? {
        return try {
            val exportDir = getExportsDir(context)
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(exportDir, "yoyo_charging_sessions_$timestamp.csv")

            FileWriter(file).use { writer ->
                writer.write(convertSessionsToCsv(sessions))
            }

            FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportSessionsToJson(context: Context, sessions: List<ChargingSession>): Uri? {
        return try {
            val exportDir = getExportsDir(context)
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(exportDir, "yoyo_charging_sessions_$timestamp.json")

            FileWriter(file).use { writer ->
                writer.write(convertSessionsToJson(sessions))
            }

            FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareExportFile(context: Context, uri: Uri, mimeType: String, chooserTitle: String = "Export Battery Data") {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun importSessionsFromUri(context: Context, uri: Uri): List<ChargingSession> {
        return try {
            val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } ?: return emptyList()

            val trimmed = content.trim()
            if (trimmed.startsWith("[") || trimmed.startsWith("{")) {
                parseSessionsFromJson(trimmed)
            } else {
                parseSessionsFromCsv(trimmed)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun parseSessionsFromJson(jsonStr: String): List<ChargingSession> {
        val list = mutableListOf<ChargingSession>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        try {
            val array = if (jsonStr.startsWith("[")) {
                org.json.JSONArray(jsonStr)
            } else {
                org.json.JSONArray().put(org.json.JSONObject(jsonStr))
            }

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id", java.util.UUID.randomUUID().toString())
                val startStr = obj.optString("startTime", "")
                val endStr = obj.optString("endTime", "")

                val startTimeMs = try {
                    if (startStr.isNotEmpty()) dateFormat.parse(startStr)?.time ?: System.currentTimeMillis()
                    else obj.optLong("startTimeMs", System.currentTimeMillis())
                } catch (_: Exception) {
                    System.currentTimeMillis()
                }

                val endTimeMs = if (endStr == "Ongoing" || endStr.isBlank()) {
                    if (obj.has("endTimeMs") && !obj.isNull("endTimeMs")) obj.getLong("endTimeMs") else null
                } else {
                    try { dateFormat.parse(endStr)?.time } catch (_: Exception) { null }
                }

                val startLevel = obj.optInt("startLevel", 0)
                val endLevel = if (obj.isNull("endLevel")) null else obj.optInt("endLevel")
                val chargeAddedMah = obj.optDouble("chargeAddedMah", 0.0)
                val avgCurrentMa = obj.optInt("avgCurrentMa", 0)
                val maxPowerWatts = obj.optDouble("maxPowerWatts", 0.0)
                val estCap = obj.optDouble("estimatedFullCapacityMah", 0.0)
                val plugType = obj.optString("plugType", "AC")

                list.add(
                    ChargingSession(
                        id = id,
                        startTimeMs = startTimeMs,
                        endTimeMs = endTimeMs,
                        startLevel = startLevel,
                        endLevel = endLevel,
                        chargeAddedMah = chargeAddedMah,
                        plugType = plugType,
                        maxCurrentMa = avgCurrentMa,
                        avgCurrentMa = avgCurrentMa,
                        maxTemperatureCelsius = 30f,
                        maxVoltageVolts = 4.1,
                        maxPowerWatts = maxPowerWatts,
                        estimatedFullCapacityMah = estCap
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun parseSessionsFromCsv(csvStr: String): List<ChargingSession> {
        val list = mutableListOf<ChargingSession>()
        val lines = csvStr.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        for (line in lines.drop(1)) {
            val tokens = splitCsvLine(line)
            if (tokens.size >= 7) {
                try {
                    val id = tokens.getOrNull(0)?.replace("\"", "")?.trim() ?: java.util.UUID.randomUUID().toString()
                    val startStr = tokens.getOrNull(1)?.replace("\"", "")?.trim() ?: ""
                    val endStr = tokens.getOrNull(2)?.replace("\"", "")?.trim() ?: ""

                    val startTimeMs = try {
                        dateFormat.parse(startStr)?.time ?: System.currentTimeMillis()
                    } catch (_: Exception) {
                        System.currentTimeMillis()
                    }

                    val endTimeMs = if (endStr == "Ongoing" || endStr.isBlank()) null else try {
                        dateFormat.parse(endStr)?.time
                    } catch (_: Exception) {
                        null
                    }

                    val startLevel = tokens.getOrNull(3)?.trim()?.toIntOrNull() ?: 0
                    val endLevel = tokens.getOrNull(4)?.trim()?.toIntOrNull()
                    val chargeMah = tokens.getOrNull(7)?.trim()?.toDoubleOrNull() ?: 0.0
                    val avgCurrent = tokens.getOrNull(8)?.trim()?.toIntOrNull() ?: 0
                    val maxWatts = tokens.getOrNull(9)?.trim()?.toDoubleOrNull() ?: 0.0
                    val estCap = tokens.getOrNull(10)?.trim()?.toDoubleOrNull() ?: 0.0
                    val plugType = tokens.getOrNull(11)?.replace("\"", "")?.trim().takeUnless { it.isNullOrEmpty() } ?: "AC"

                    list.add(
                        ChargingSession(
                            id = id,
                            startTimeMs = startTimeMs,
                            endTimeMs = endTimeMs,
                            startLevel = startLevel,
                            endLevel = endLevel,
                            chargeAddedMah = chargeMah,
                            plugType = plugType,
                            maxCurrentMa = avgCurrent,
                            avgCurrentMa = avgCurrent,
                            maxTemperatureCelsius = 30f,
                            maxVoltageVolts = 4.1,
                            maxPowerWatts = maxWatts,
                            estimatedFullCapacityMah = estCap
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return list
    }

    private fun splitCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        for (c in line) {
            when {
                c == '\"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> {
                    result.add(current.toString().trim())
                    current.clear()
                }
                else -> current.append(c)
            }
        }
        result.add(current.toString().trim())
        return result
    }
}
