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
import com.spbpu.schedule.RuzApi.models.DaySchedule
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
    private var originalSchedule: List<DaySchedule> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        teacherName = intent.getStringExtra("TEACHER_NAME") ?: ""
        binding.scheduleText.text = "           Расписание для преподавателя: \n          $teacherName"

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

    private fun loadSchedule() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d("TeacherSchedule", "Поиск преподавателя: $teacherName")

                val teacherList = withContext(Dispatchers.IO) {
                    RuzSpbStu.searchTeachersByName(teacherName)
                }
                Log.d("TeacherSchedule", "Найдено преподавателей: ${teacherList.size}")
                val teacher = teacherList.firstOrNull()

                if (teacher == null) {
                    showEmptySchedule(true)
                    Log.w("TeacherSchedule", "Преподаватель не найден")
                    Toast.makeText(this@TeacherScheduleActivity,
                        "Преподаватель не найден", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val schedule = withContext(Dispatchers.IO) {
                    RuzSpbStu.getScheduleByTeacherIdAndDate(teacher.id, currentDate)
                }

                if (schedule == null || schedule.days.isEmpty()) {
                    showEmptySchedule(true)
                    Toast.makeText(this@TeacherScheduleActivity,
                        "Нет занятий на выбранной неделе", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val daySchedules = schedule.days.map { day ->
                    val weekday = LocalDate.parse(day.date).dayOfWeek.getDisplayName(
                        TextStyle.FULL, Locale("ru"))
                    val formattedDate = LocalDate.parse(day.date).format(
                        DateTimeFormatter.ofPattern("dd.MM"))
                    val lessonsText = day.lessons.joinToString("\n────────────\n") { lesson ->
                        val room = lesson.auditories?.rooms?.firstOrNull()?.name ?: "Аудитория не указана"
                        val group = lesson.groups?.firstOrNull()?.nameGroup ?: "Группа не указана"
                        val building = lesson.auditories?.building
                        val buildingInfo = building?.let { "${it.name} (${room})" } ?: "Корпус не указан"
                        val start = lesson.timeStart?.toString() ?: "??"
                        val end = lesson.timeEnd?.toString() ?: "??"
                        "- ${lesson.subject} ($start - $end)\nГруппа:$group\n$buildingInfo"
                    }
                    DaySchedule("$weekday, $formattedDate", lessons = lessonsText)
                }

                originalSchedule = daySchedules
                showEmptySchedule(daySchedules.isEmpty())
                adapter?.updateItems(daySchedules)

            } catch (e: Exception) {
                Log.e("TeacherSchedule", "Ошибка загрузки расписания", e)
                Toast.makeText(this@TeacherScheduleActivity,
                    "Ошибка при загрузке", Toast.LENGTH_LONG).show()
                showEmptySchedule(false)
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
                            0 -> {
                                adapter?.updateItems(originalSchedule)
                                showEmptySchedule(originalSchedule.isEmpty())
                                Toast.makeText(this@TeacherScheduleActivity,
                                    "Фильтр сброшен", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                val selectedBuilding = buildings[which - 1]
                                showRoomsInBuilding(selectedBuilding.id)
                            }
                        }
                    }
                    .setNegativeButton("Отмена", null)
                    .show()

            } catch (e: Exception) {
                Toast.makeText(this@TeacherScheduleActivity,
                    "Ошибка при загрузке корпусов", Toast.LENGTH_SHORT).show()
                Log.e("TeacherFilter", "Ошибка загрузки корпусов", e)
            }
        }
    }

    private fun showRoomsInBuilding(buildingId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val progressDialog = ProgressDialog(this@TeacherScheduleActivity).apply {
                    setMessage("Загрузка аудиторий...")
                    setCancelable(false)
                    show()
                }

                val auditory = withContext(Dispatchers.IO) {
                    RuzSpbStu.getAuditoriesByBuildingId(buildingId)
                }

                progressDialog.dismiss()

                val rooms = auditory?.rooms?.sortedBy { it.name } ?: emptyList()

                if (rooms.isEmpty()) {
                    Toast.makeText(this@TeacherScheduleActivity,
                        "В этом корпусе нет аудиторий", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                AlertDialog.Builder(this@TeacherScheduleActivity)
                    .setTitle("Выберите аудиторию")
                    .setItems(rooms.map { it.name }.toTypedArray()) { _, which ->
                        val selectedRoom = rooms[which]
                        filterByAuditory(selectedRoom.name)
                    }
                    .setNegativeButton("Назад") { _, _ -> showBuildingSelectionDialog() }
                    .show()

            } catch (e: Exception) {
                Toast.makeText(this@TeacherScheduleActivity,
                    "Ошибка при загрузке аудиторий", Toast.LENGTH_SHORT).show()
                Log.e("TeacherFilter", "Ошибка загрузки аудиторий", e)
            }
        }
    }

    private fun filterByAuditory(auditoryName: String) {
        val filtered = originalSchedule.mapNotNull { day ->
            val filteredLessons = day.lessons.split("\n────────────\n")
                .filter { lesson ->
                    lesson.contains("($auditoryName)") ||
                            lesson.contains(" $auditoryName ")
                }
                .joinToString("\n────────────\n")

            if (filteredLessons.isNotBlank()) {
                day.copy(lessons = filteredLessons)
            } else {
                null
            }
        }

        if (filtered.isEmpty()) {
            showEmptySchedule(true)
            Toast.makeText(this, "Нет занятий в выбранной аудитории", Toast.LENGTH_SHORT).show()
        } else {
            showEmptySchedule(false)
            adapter?.updateItems(filtered)
        }
    }

    private fun showEmptySchedule(show: Boolean) {
        binding.tvEmptySchedule.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerSchedule.visibility = if (show) View.GONE else View.VISIBLE

        if (show) {
            binding.tvEmptySchedule.text = "Занятий на неделе нет. Можете отдыхать!"
        }
    }
}