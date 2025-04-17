package com.spbpu.schedule.data.parsers

import com.spbpu.schedule.data.models.Group

object GroupParser {
    fun parse(html: String): List<Group> {
        val regex = Regex("""\{"id":(\d+),"name":"([^"]+)",""")
        return regex.findAll(html).map {
            Group(
                id = it.groupValues[1].toInt(),
                name = it.groupValues[2].substringBefore("_"),
                spec = "" // Можно пропустить
            )
        }.toList()
    }
}