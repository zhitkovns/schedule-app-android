package com.spbpu.schedule.presentation.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spbpu.schedule.R
import com.spbpu.schedule.RuzApi.RuzSpbStu
import com.spbpu.schedule.RuzApi.models.*
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import android.widget.Button
import android.widget.TextView

class ScheduleActivity : AppCompatActivity() {

    private var groupId: Int = -1
    private var currentDate: LocalDate = LocalDate.now()
    private var originalSchedule: List<DaySchedule> = emptyList()
    private var currentAuditory: Pair<String, String>? = null // Pair<BuildingName, RoomName>
    private var currentBuildingId: Int? = null

    private lateinit var scheduleText: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScheduleAdapter1
    private lateinit var emptyScheduleText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        groupId = intent.getIntExtra("GROUP_ID", -1)
        val groupName = intent.getStringExtra("GROUP_NAME") ?: "Неизвестно"

        scheduleText = findViewById(R.id.scheduleText)
        emptyScheduleText = findViewById(R.id.tvEmptySchedule)
        recyclerView = findViewById(R.id.recyclerSchedule)

        updateTitle(groupName)
        emptyScheduleText.visibility = View.GONE

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ScheduleAdapter1(emptyList())
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.btnNextWeek).setOnClickListener {
            currentDate = currentDate.plusWeeks(1)
            loadSchedule()
        }

        findViewById<Button>(R.id.btnPrevWeek).setOnClickListener {
            currentDate = currentDate.minusWeeks(1)
            loadSchedule()
        }

        findViewById<Button>(R.id.btnCurrentWeek).setOnClickListener {
            currentDate = LocalDate.now()
            loadSchedule()
        }

        findViewById<Button>(R.id.btnFilterAudience).setOnClickListener {
            showBuildingSelectionDialog()
        }

        loadSchedule()
    }

    private fun updateTitle(groupName: String) {
        scheduleText.text = currentAuditory?.let {
            "                   Расписание аудитории: \n" +
                    "                  ${it.first} ${it.second}"
        } ?: "                     Расписание группы: \n                        $groupName"
    }

    private fun loadSchedule() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val progressDialog = ProgressDialog(this@ScheduleActivity).apply {
                    setMessage("Загрузка расписания...")
                    setCancelable(false)
                    show()
                }

                val groupName = intent.getStringExtra("GROUP_NAME") ?: "Неизвестно"
                updateTitle(groupName)

                val schedule = currentAuditory?.let { (buildingName, roomName) ->
                    withContext(Dispatchers.IO) {
                        RuzSpbStu.getScheduleByBuildingAndRoom(buildingName, roomName, currentDate)
                    }
                } ?: withContext(Dispatchers.IO) {
                    RuzSpbStu.getScheduleByGroupIdAndDate(groupId, currentDate)
                }

                progressDialog.dismiss()

                if (schedule == null || schedule.days.isNullOrEmpty()) {
                    showEmptySchedule(true)
                    Toast.makeText(
                        this@ScheduleActivity,
                        "Нет занятий на выбранной неделе",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val daySchedules = schedule.days.map { day ->
                    val weekday = LocalDate.parse(day.date).dayOfWeek.getDisplayName(
                        TextStyle.FULL, Locale("ru"))
                    val formattedDate = LocalDate.parse(day.date).format(
                        DateTimeFormatter.ofPattern("dd.MM"))
                    val lessonsText = day.lessons.joinToString("\n────────────\n") { lesson ->
                        val room = lesson.auditories?.rooms?.firstOrNull()?.name ?: "Аудитория не указана"
                        val building = lesson.auditories?.building
                        val buildingInfo = building?.let { "${it.name} ($room)" } ?: "Корпус не указан"
                        val start = lesson.timeStart?.toString() ?: "??"
                        val end = lesson.timeEnd?.toString() ?: "??"
                        val teachers = lesson.teachers?.joinToString { it.fullName } ?: "Преподаватель не указан"
                        val groups = lesson.groups?.joinToString { it.nameGroup } ?: "Группа не указана"

                        """
                    - ${lesson.subject} ($start - $end)
                    Преподаватель: $teachers
                    Группы: $groups
                    $buildingInfo
                    """.trimIndent()
                    }
                    DaySchedule("$weekday, $formattedDate", lessons = lessonsText)
                }

                if (currentAuditory == null) {
                    originalSchedule = daySchedules
                }

                showEmptySchedule(daySchedules.isEmpty())
                adapter.updateItems(daySchedules)

            } catch (e: Exception) {
                ///
            }
        }
    }

    private fun showBuildingSelectionDialog() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val progressDialog = ProgressDialog(this@ScheduleActivity).apply {
                    setMessage("Загрузка корпусов...")
                    setCancelable(false)
                    show()
                }

                val buildings = withContext(Dispatchers.IO) {
                    RuzSpbStu.getBuildings()
                }?.sortedBy { it.name } ?: emptyList()

                progressDialog.dismiss()

                val buildingNames = mutableListOf<String>().apply {
                    add("❌ Сбросить фильтр")
                    addAll(buildings.map { it.name })
                }

                AlertDialog.Builder(this@ScheduleActivity)
                    .setTitle("Выберите корпус")
                    .setItems(buildingNames.toTypedArray()) { _, which ->
                        when (which) {
                            0 -> resetFilter()
                            else -> {
                                val selectedBuilding = buildings[which - 1]
                                currentBuildingId = selectedBuilding.id
                                showRoomsInBuilding(selectedBuilding)
                            }
                        }
                    }
                    .setNegativeButton("Отмена", null)
                    .show()

            } catch (e: Exception) {
                Toast.makeText(
                    this@ScheduleActivity,
                    "Ошибка при загрузке корпусов",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("ScheduleFilter", "Ошибка загрузки корпусов", e)
            }
        }
    }

    private fun showRoomsInBuilding(building: Building) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val progressDialog = ProgressDialog(this@ScheduleActivity).apply {
                    setMessage("Загрузка аудиторий...")
                    setCancelable(false)
                    show()
                }

                val auditory = withContext(Dispatchers.IO) {
                    RuzSpbStu.getAuditoriesByBuildingId(building.id)
                }

                progressDialog.dismiss()

                val rooms = auditory?.rooms?.sortedBy { it.name } ?: emptyList()

                if (rooms.isEmpty()) {
                    Toast.makeText(
                        this@ScheduleActivity,
                        "В корпусе ${building.name} нет аудиторий",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                AlertDialog.Builder(this@ScheduleActivity)
                    .setTitle("Аудитории в корпусе ${building.name}")
                    .setItems(rooms.map { it.name }.toTypedArray()) { _, which ->
                        val selectedRoom = rooms[which]
                        currentAuditory = Pair(building.name, selectedRoom.name)
                        loadSchedule()
                    }
                    .setNegativeButton("Назад") { _, _ -> showBuildingSelectionDialog() }
                    .show()

            } catch (e: Exception) {
                Toast.makeText(
                    this@ScheduleActivity,
                    "Ошибка при загрузке аудиторий",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("ScheduleFilter", "Ошибка загрузки аудиторий", e)
            }
        }
    }

    private fun resetFilter() {
        currentAuditory = null
        currentBuildingId = null
        currentDate = LocalDate.now()
        updateTitle(intent.getStringExtra("GROUP_NAME") ?: "Неизвестно")
        loadSchedule()
        Toast.makeText(
            this@ScheduleActivity,
            "Фильтр сброшен",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showEmptySchedule(show: Boolean) {
        emptyScheduleText.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE

        if (show) {
            emptyScheduleText.text = "Занятий на неделе нет. Можете отдыхать!"
        }
    }
}