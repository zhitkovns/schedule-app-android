package com.spbpu.schedule.data.parsers

import com.spbpu.schedule.data.models.Day
import org.json.JSONObject

object ScheduleParser {

    /**
     * Извлекает JSON из <script> window.__INITIAL_STATE__ = {...};
     * И возвращает List<Day> по ключу lessons.data[groupId].
     */
    fun parse(html: String, groupId: Int): List<Day> {
        val marker = "window.__INITIAL_STATE__ ="
        val idx = html.indexOf(marker)
        if (idx < 0) return emptyList()

        // Вырезаем JSON-строку между маркером и </script>
        val jsonStr = html
            .substring(idx + marker.length)
            .substringBefore("</script>")
            .trim()
            .removeSuffix(";")

        val root = JSONObject(jsonStr)
        val lessonsObj = root
            .getJSONObject("lessons")
            .getJSONObject("data")
        val arr = lessonsObj.optJSONArray(groupId.toString()) ?: return emptyList()

        val days = mutableListOf<Day>()
        for (i in 0 until arr.length()) {
            val dayObj = arr.getJSONObject(i)
            val dateStr = dayObj.getString("date")  // "2025-04-28"
            val weekday = dayObj.getInt("weekday")  // 1–7
            val name = when (weekday) {
                1 -> "Понедельник"; 2 -> "Вторник"; 3 -> "Среда"
                4 -> "Четверг";   5 -> "Пятница"; 6 -> "Суббота"
                7 -> "Воскресенье"; else -> ""
            }
            days += Day(name = name, date = dateStr)
        }
        return days
    }
}
