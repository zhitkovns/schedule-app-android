package com.spbpu.schedule.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spbpu.schedule.data.repositories.GroupRepository
import kotlinx.coroutines.launch

class GroupViewModel(
    private val repository: GroupRepository
) : ViewModel() {

    fun loadGroups(onSuccess: (List<String>) -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try {
                val groups = repository.fetchGroups()
                onSuccess(groups)
            } catch (e: Exception) {
                onError()
            }
        }
    }
}