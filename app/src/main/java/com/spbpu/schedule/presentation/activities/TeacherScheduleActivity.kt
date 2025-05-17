package com.spbpu.schedule.presentation.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.spbpu.schedule.RuzApi.RuzSpbStu
import com.spbpu.schedule.databinding.ActivityScheduleBinding
import com.spbpu.schedule.presentation.activities.ScheduleTeacherAdapter
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        teacherName = intent.getStringExtra("TEACHER_NAME") ?: ""
        binding.scheduleText.text = "Расписание для преподавателя: $teacherName"

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
                    Log.w("TeacherSchedule", "Преподаватель не найден")
                    Toast.makeText(this@TeacherScheduleActivity, "Преподаватель не найден", Toast.LENGTH_LONG).show()
                    return@launch
                }

                Log.d("TeacherSchedule", "ID преподавателя: ${teacher.id}, ФИО: ${teacher.fullName}")

                val schedule = withContext(Dispatchers.IO) {
                    RuzSpbStu.getScheduleByTeacherIdAndDate(teacher.id, currentDate)
                }

                if (schedule == null) {
                    Log.w("TeacherSchedule", "schedule = null от API")
                    Toast.makeText(this@TeacherScheduleActivity, "Нет данных", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                Log.d("TeacherSchedule", "Загружено дней: ${schedule.days.size}")

                if (schedule.days.isEmpty()) {
                    Toast.makeText(this@TeacherScheduleActivity, "Нет данных для отображения", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val daySchedules = schedule.days.map { day ->
                    val weekday = LocalDate.parse(day.date).dayOfWeek.getDisplayName(TextStyle.FULL, Locale("ru"))
                    val formattedDate = LocalDate.parse(day.date).format(DateTimeFormatter.ofPattern("dd.MM"))
                    val lessonsText = day.lessons.joinToString("\n────────────\n") { lesson ->
                        val room = lesson.auditories?.rooms?.firstOrNull()?.name ?: "Аудитория не указана"
                        val group = lesson.groups?.firstOrNull()?.nameGroup ?: "Группа не указана"

                        val building = lesson.auditories?.building
                        val buildingInfo = building?.let { "${it.name} (${room})" } ?: "Корпус не указан"
                        val start = lesson.timeStart?.toString() ?: "??"
                        val end = lesson.timeEnd?.toString() ?: "??"
                        "- ${lesson.subject} ($start - $end)\nГруппа:$group\n$buildingInfo"
                    }
                    Log.d("TeacherSchedule", "День ${day.weekDay}: ${lessonsText.replace("\n", " | ")}")
                    DaySchedule("$weekday, $formattedDate", lessons = lessonsText)
                }
                adapter?.updateItems(daySchedules)

            } catch (e: Exception) {
                Log.e("TeacherSchedule", "Ошибка загрузки расписания", e)
                Toast.makeText(this@TeacherScheduleActivity, "Ошибка при загрузке", Toast.LENGTH_LONG).show()
            }
        }
    }
}
