package com.spbpu.schedule.data.models

data class Lesson(
    val timeStart: String,    // "08:00"
    val timeEnd:   String,    // "09:40"
    val subject:   String,    // полное название
    val type:      String,    // аббревиатура типа, напр. "Лек" или "Пр"
    val teacher:   String?,   // имя преподавателя, если есть
    val auditorium: String?   // номер аудитории, если есть
)
