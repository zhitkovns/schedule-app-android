package com.spbpu.schedule.data.parsers

import com.spbpu.schedule.data.models.Group

object GroupParser {
    fun parse(html: String): List<Group> {
        return try {
            val seenNames = mutableSetOf<String>() // Для отслеживания уникальности

            Regex("""\{"id":(\d+),"name":"([^"]+)",""")
                .findAll(html)
                .mapNotNull { match ->
                    val fullName = match.groupValues[2]
                    val name = fullName.substringBefore("_")

                    // Проверяем что имя начинается с цифры И не повторяется
                    if (name.first().isDigit() && seenNames.add(name)) {
                        Group(
                            id = match.groupValues[1].toInt(),
                            name = name,
                            spec = fullName.substringAfter("_", "")
                        )
                    } else {
                        null
                    }
                }
                .toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

}