package com.spbpu.schedule.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.spbpu.schedule.data.repositories.GroupRepository
import com.spbpu.schedule.databinding.ActivityGroupSelectionBinding
import com.spbpu.schedule.presentation.factories.GroupViewModelFactory
import com.spbpu.schedule.presentation.viewmodels.GroupViewModel

class GroupSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupSelectionBinding
    private lateinit var adapter: ArrayAdapter<String>

    private val viewModel: GroupViewModel by viewModels {
        GroupViewModelFactory(GroupRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSearchView()
        loadGroups()
    }

    private fun setupSearchView() {
        // слушаем ввод и фильтруем ArrayAdapter
        binding.searchViewGroups.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                if (::adapter.isInitialized) {
                    adapter.filter.filter(newText)
                }
                return true
            }
        })
    }

    private fun loadGroups() {
        viewModel.loadGroups(
            onSuccess = { groups: List<String> ->
                // groups — это список имен групп
                adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    groups
                )
                binding.listViewGroups.adapter = adapter

                // при клике на элемент — открываем MainActivity,
                // передавая выбранное имя группы
                binding.listViewGroups.setOnItemClickListener { _, _, position, _ ->
                    val groupName = adapter.getItem(position)!!
                    startActivity(
                        Intent(this, MainActivity::class.java)
                            .putExtra("GROUP_NAME", groupName)
                    )
                }
            },
            onError = {
                // TODO: сообщить пользователю об ошибке загрузки групп
            }
        )
    }
}
