package com.spbpu.schedule.data.models

data class Group(
    val id: Int,
    val name: String,  // Например: "3743806/20201"
    val spec: String   // Специальность (можно не использовать)
)