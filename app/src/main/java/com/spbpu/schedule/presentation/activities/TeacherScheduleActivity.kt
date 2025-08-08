package com.spbpu.schedule.presentation.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.spbpu.schedule.R
import com.spbpu.schedule.RuzApi.RuzSpbStu
import com.spbpu.schedule.databinding.ActivityScheduleBinding
import com.spbpu.schedule.RuzApi.models.*
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class TeacherScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleBinding
    private var currentDate: LocalDate = LocalDate.now()
    private var adapter: ScheduleTeacherAdapter? = null
    private var teacherName: String = ""
    private var teacherId: Int = -1
    private var originalSchedule: List<DaySchedule> = emptyList()
    private var currentAuditory: Pair<String, String>? = null // Pair<BuildingName, RoomName>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        teacherName = intent.getStringExtra("TEACHER_NAME") ?: ""
        updateTitle()

        adapter = ScheduleTeacherAdapter(emptyList())
        binding.recyclerSchedule.layoutManager = LinearLayoutManager(this)
        binding.recyclerSchedule.adapter = adapter

        binding.btnNextWeek.setOnClickListener {
            currentDate = currentDate.plusWeeks(1)
            loadSchedule()
        }

        binding.btnPrevWeek.setOnClickListener {
            currentDate = currentDate.minusWeeks(1)
            loadSchedule()
        }

        binding.btnCurrentWeek.setOnClickListener {
            currentDate = LocalDate.now()
            loadSchedule()
        }

        binding.btnFilterAudience.setOnClickListener {
            showBuildingSelectionDialog()
        }

        loadSchedule()
    }

    private fun updateTitle() {
        binding.scheduleText.text = currentAuditory?.let {
            "                   Расписание аудитории: \n" +
                    "                  ${it.first} ${it.second}"
        } ?: "              Расписание преподавателя: \n        $teacherName"
    }

    private fun loadSchedule() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val progressDialog = ProgressDialog(this@TeacherScheduleActivity).apply {
                    setMessage("Загрузка расписания...")
                    setCancelable(false)
                    show()
                }

                updateTitle()

                if (teacherId == -1) {
                    val teacherList = withContext(Dispatchers.IO) {
                        RuzSpbStu.searchTeachersByName(teacherName)
                    }
                    teacherId = teacherList?.firstOrNull()?.id ?: -1
                    if (teacherId == -1) {
                        progressDialog.dismiss()
                        showEmptySchedule(true)
                        Toast.makeText(
                            this@TeacherScheduleActivity,
                            "Преподаватель не найден",
                            Toast.LENGTH_LONG
                        ).show()
                        return@launch
                    }
                }

                val schedule = currentAuditory?.let { (buildingName, roomName) ->
                    withContext(Dispatchers.IO) {
                        RuzSpbStu.getScheduleByBuildingAndRoom(buildingName, roomName, currentDate)
                    }
                } ?: withContext(Dispatchers.IO) {
                    RuzSpbStu.getScheduleByTeacherIdAndDate(teacherId, currentDate)
                }

                progressDialog.dismiss()

                if (schedule == null || schedule.days.isNullOrEmpty()) {
                    showEmptySchedule(true)
                    Toast.makeText(
                        this@TeacherScheduleActivity,
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
                        val groups = lesson.groups?.joinToString { it.nameGroup } ?: "Группа не указана"
                        val teachers = lesson.teachers?.joinToString { it.fullName } ?: "Преподаватель не указан"

                        // Всегда показываем преподавателя, даже при фильтрации по аудитории
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
                adapter?.updateItems(daySchedules)

            } catch (e: Exception) {
                ///
            }
        }
    }

    private fun showBuildingSelectionDialog() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val progressDialog = ProgressDialog(this@TeacherScheduleActivity).apply {
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

                AlertDialog.Builder(this@TeacherScheduleActivity)
                    .setTitle("Выберите корпус")
                    .setItems(buildingNames.toTypedArray()) { _, which ->
                        when (which) {
                            0 -> resetFilter()
                            else -> {
                                val selectedBuilding = buildings[which - 1]
                                showRoomsInBuilding(selectedBuilding)
                            }
                        }
                    }
                    .setNegativeButton("Отмена", null)
                    .show()

            } catch (e: Exception) {
                Toast.makeText(
                    this@TeacherScheduleActivity,
                    "Ошибка при загрузке корпусов",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("TeacherFilter", "Ошибка загрузки корпусов", e)
            }
        }
    }

    private fun showRoomsInBuilding(building: Building) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val progressDialog = ProgressDialog(this@TeacherScheduleActivity).apply {
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
                        this@TeacherScheduleActivity,
                        "В корпусе ${building.name} нет аудиторий",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                AlertDialog.Builder(this@TeacherScheduleActivity)
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
                    this@TeacherScheduleActivity,
                    "Ошибка при загрузке аудиторий",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("TeacherFilter", "Ошибка загрузки аудиторий", e)
            }
        }
    }

    private fun resetFilter() {
        currentAuditory = null
        currentDate = LocalDate.now()
        updateTitle()
        loadSchedule()
        Toast.makeText(
            this@TeacherScheduleActivity,
            "Фильтр сброшен",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showEmptySchedule(show: Boolean) {
        binding.tvEmptySchedule.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerSchedule.visibility = if (show) View.GONE else View.VISIBLE

        if (show) {
            binding.tvEmptySchedule.text = "Занятий на неделе нет. Можете отдыхать!"
        }
    }
}