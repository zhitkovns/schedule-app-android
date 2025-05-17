package com.spbpu.schedule.presentation.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spbpu.schedule.R
import com.spbpu.schedule.RuzApi.RuzSpbStu
import com.spbpu.schedule.RuzApi.models.Day
import com.spbpu.schedule.presentation.activities.ScheduleAdapter1
import com.spbpu.schedule.RuzApi.models.DaySchedule
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


class ScheduleActivity : AppCompatActivity() {

    private var groupId: Int = -1
    private var currentDate: LocalDate = LocalDate.now()

    private lateinit var scheduleText: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScheduleAdapter1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        groupId = intent.getIntExtra("GROUP_ID", -1)
        val groupName = intent.getStringExtra("GROUP_NAME") ?: "Неизвестно"

        scheduleText = findViewById(R.id.scheduleText)
        scheduleText.text = "Расписание для группы $groupName (ID: $groupId)"

        recyclerView = findViewById(R.id.recyclerSchedule)
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

        loadSchedule()
    }

    private fun loadSchedule() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val schedule = withContext(Dispatchers.IO) {
                    RuzSpbStu.getScheduleByGroupIdAndDate(groupId, currentDate)
                }

                if (schedule == null) {
                    Toast.makeText(this@ScheduleActivity, "Не удалось загрузить расписание", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val daySchedules = schedule.days?.map { day ->
                    val weekday = LocalDate.parse(day.date).dayOfWeek.getDisplayName(TextStyle.FULL, Locale("ru"))
                    val formattedDate = LocalDate.parse(day.date).format(DateTimeFormatter.ofPattern("dd.MM"))
                    val lessonsText = day.lessons.joinToString("\n────────────\n") { lesson ->
                        val room = lesson.auditories?.rooms?.firstOrNull()?.name ?: "Аудитория не указана"
                        Log.d("ScheduleActivity", "Rooms count for lesson '${lesson.subject}': ${lesson.auditories?.rooms?.size}")

                        val building = lesson.auditories?.building
                        val buildingInfo = building?.let { "${it.name} (Аудитория:${room})" } ?: "Корпус не указан"
                        val start = lesson.timeStart?.toString() ?: "??"
                        val end = lesson.timeEnd?.toString() ?: "??"
                        val teacherNames = lesson.teachers?.joinToString { it.fullName } ?: "Преподаватель не указан"
                        "- ${lesson.subject} ($start - $end)\n$teacherNames\n$buildingInfo"
                    }
                    DaySchedule("$weekday, $formattedDate", lessons = lessonsText)
                } ?: emptyList()

                adapter.updateItems(daySchedules)

            } catch (e: Exception) {
                Log.e("ScheduleActivity", "Ошибка при загрузке расписания", e)
                Toast.makeText(this@ScheduleActivity, "Ошибка при загрузке расписания", Toast.LENGTH_LONG).show()
            }
        }
    }
}
