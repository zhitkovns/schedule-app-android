package com.spbpu.schedule.data.repositories

import com.spbpu.schedule.data.api.RetrofitClient
import retrofit2.Response

class ScheduleRepository {
    suspend fun fetchSchedule(groupId: Int, date: String): Response<String> {
        return RetrofitClient.api.getSchedule(groupId, date)
    }
}