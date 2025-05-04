package com.spbpu.schedule.data.parsers

import com.spbpu.schedule.data.models.Day
import com.spbpu.schedule.data.models.Lesson
import org.json.JSONObject

object ScheduleParser {
    fun parse(html: String, groupId: Int): List<Day> {
        val marker = "window.__INITIAL_STATE__ ="
        val start = html.indexOf(marker).takeIf { it >= 0 } ?: return emptyList()
        val begin = start + marker.length
        // находим конец JSON-объекта (последовательность "};")
        val end = html.indexOf("};", begin).takeIf { it >= 0 } ?: return emptyList()
        val jsonStr = html.substring(begin, end + 1)

        val root = JSONObject(jsonStr)
        val data = root
            .getJSONObject("lessons")
            .getJSONObject("data")
            .optJSONArray(groupId.toString())
            ?: return emptyList()

        val days = mutableListOf<Day>()
        for (i in 0 until data.length()) {
            val dayObj = data.getJSONObject(i)
            val dateStr = dayObj.getString("date")            // "2025-04-28"
            val weekday = dayObj.getInt("weekday")            // 1–7
            val name = when (weekday) {
                1 -> "Понедельник" 2 -> "Вторник" 3 -> "Среда"
                4 -> "Четверг"     5 -> "Пятница"  6 -> "Суббота"
                7 -> "Воскресенье" else -> ""
            }

            val list = dayObj.optJSONArray("lessons") ?: continue
            val lessons = mutableListOf<Lesson>()
            for (j in 0 until list.length()) {
                val l = list.getJSONObject(j)
                val timeStart = l.getString("time_start")
                val timeEnd   = l.getString("time_end")
                val subject   = l.getString("subject_short")
                val type      = l.getJSONObject("typeObj").getString("abbr")
                val teacher = l.optJSONArray("teachers")
                    ?.optJSONObject(0)
                    ?.optString("full_name")
                val auditorium = l.optJSONArray("auditories")
                    ?.optJSONObject(0)
                    ?.optString("name")

                lessons += Lesson(
                    timeStart = timeStart,
                    timeEnd = timeEnd,
                    subject = subject,
                    type = type,
                    teacher = teacher,
                    auditorium = auditorium
                )
            }

            days += Day(name = name, date = dateStr, lessons = lessons)
        }
        return days
    }
}
