package com.spbpu.schedule.presentation.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spbpu.schedule.data.repositories.GroupRepository
import com.spbpu.schedule.presentation.viewmodels.GroupViewModel

class GroupViewModelFactory(
    private val repository: GroupRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GroupViewModel(repository) as T
    }
}