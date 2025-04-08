package com.spbpu.schedule.data.api

import retrofit2.Response
import retrofit2.http.GET

interface ScheduleApi {
    @GET("faculty/100/groups")
    suspend fun getGroups(): Response<String> // Получаем HTML как строку
}