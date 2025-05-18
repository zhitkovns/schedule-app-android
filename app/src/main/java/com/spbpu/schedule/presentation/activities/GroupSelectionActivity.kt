package com.spbpu.schedule.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.spbpu.schedule.data.models.Group
import com.spbpu.schedule.data.parsers.GroupParser
import com.spbpu.schedule.data.api.RetrofitClient
import com.spbpu.schedule.databinding.ActivityGroupSelectionBinding
import kotlinx.coroutines.*

class GroupSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupSelectionBinding
    private lateinit var adapter: GroupAdapter
    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализируем адаптер с пустыми списками
        adapter = GroupAdapter(this, emptyList(), emptyList())
        binding.listViewGroups.adapter = adapter

        setupSearch()
        loadGroups()
    }

    private fun setupSearch() {
        binding.searchViewGroups.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })
    }

    private fun loadGroups() {
        uiScope.launch {
            val html = try {
                withContext(Dispatchers.IO) {
                    RetrofitClient.api.getGroups().body().orEmpty()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@GroupSelectionActivity,
                    "Ошибка сети при загрузке групп",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            val groups = try {
                GroupParser.parse(html)
            } catch (e: Exception) {
                Toast.makeText(
                    this@GroupSelectionActivity,
                    "Ошибка обработки данных групп",
                    Toast.LENGTH_SHORT
                ).show()
                emptyList()
            }

            adapter.updateData(groups)

            binding.listViewGroups.setOnItemClickListener { _, _, position, _ ->
                val selectedGroup = adapter.getItem(position)
                val intent = Intent(this@GroupSelectionActivity, ScheduleActivity::class.java).apply {
                    putExtra("GROUP_ID", selectedGroup.id)
                    putExtra("GROUP_NAME", selectedGroup.name)
                }
                startActivity(intent)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}