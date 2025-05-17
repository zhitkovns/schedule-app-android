package com.spbpu.schedule.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
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
    private var groupList: List<Group> = emptyList()
    private lateinit var adapter: ArrayAdapter<String>
    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSearch()
        loadGroups()
    }

    private fun setupSearch() {
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

            groupList = try {
                GroupParser.parse(html)
            } catch (e: Exception) {
                emptyList()
            }

            val names = groupList.map { it.name }
            adapter = ArrayAdapter(
                this@GroupSelectionActivity,
                android.R.layout.simple_list_item_1,
                names
            )
            binding.listViewGroups.adapter = adapter

            binding.listViewGroups.setOnItemClickListener { _, _, pos, _ ->
                val g = groupList[pos]
                // Переход в расписание
                val intent = Intent(this@GroupSelectionActivity, ScheduleActivity::class.java)
                intent.putExtra("GROUP_ID", g.id)
                intent.putExtra("GROUP_NAME", g.name)
                startActivity(intent)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
