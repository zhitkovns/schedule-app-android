package com.spbpu.schedule.data.repositories

import com.spbpu.schedule.data.api.RetrofitClient
import com.spbpu.schedule.data.parsers.GroupParser

class GroupRepository {
    suspend fun fetchGroups(): List<String> {
        val response = RetrofitClient.api.getGroups()
        return if (response.isSuccessful) {
            GroupParser.parse(response.body()!!)
                .map { it.name }
                .sorted()
        } else {
            emptyList()
        }
    }
}