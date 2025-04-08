package com.spbpu.schedule.presentation.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.spbpu.schedule.databinding.ActivityGroupSelectionBinding
import com.spbpu.schedule.data.repositories.GroupRepository
import com.spbpu.schedule.presentation.factories.GroupViewModelFactory
import com.spbpu.schedule.presentation.viewmodels.GroupViewModel

class GroupSelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupSelectionBinding
    private val viewModel: GroupViewModel by lazy {
        ViewModelProvider(
            this,
            GroupViewModelFactory(GroupRepository())
        )[GroupViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadGroups()
    }

    private fun loadGroups() {
        viewModel.loadGroups(
            onSuccess = { groups ->
                // Создаем адаптер для списка
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1, // Стандартный layout Android
                    groups
                )
                binding.listViewGroups.adapter = adapter
            },
            onError = {
                // Обработка ошибки
            }
        )
    }
}