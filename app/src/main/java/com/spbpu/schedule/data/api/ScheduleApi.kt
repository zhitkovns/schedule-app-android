package com.spbpu.schedule.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ScheduleApi {
    @GET("faculty/100/groups")
    suspend fun getGroups(): Response<String>

    @GET("faculty/100/groups/{groupId}")
    suspend fun getSchedule(
        @Path("groupId") groupId: Int,
        @Query("date") date: String
    ): Response<String>
}
