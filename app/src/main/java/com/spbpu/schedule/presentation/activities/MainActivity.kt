package com.spbpu.schedule.presentation.activities

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spbpu.schedule.R
import com.spbpu.schedule.data.models.Day
import com.spbpu.schedule.data.parsers.ScheduleParser
import com.spbpu.schedule.data.repositories.ScheduleRepository
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import android.view.View
import kotlinx.coroutines.CancellationException

class MainActivity : AppCompatActivity() {

    private var weekOffset = 0
    private var groupId: Int = -1
    private lateinit var tvEmptySchedule: TextView

    private lateinit var btnPrev: Button
    private lateinit var btnCurr: Button
    private lateinit var btnNext: Button
    private lateinit var tvDateRange: TextView
    private lateinit var rvSchedule: RecyclerView
    private lateinit var scheduleAdapter: ScheduleAdapter

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val teacherName = intent.getStringExtra("TEACHER_NAME")
        if (teacherName != null) {
            supportActionBar?.title = "Расписание преподавателя\n$teacherName"
            // Скрываем кнопки и список, либо показываем пустой экран / лоадер
            findViewById<View>(R.id.btnPrevWeek).visibility = View.GONE
            findViewById<View>(R.id.btnCurrentWeek).visibility = View.GONE
            findViewById<View>(R.id.btnNextWeek).visibility = View.GONE
            // Можно оставить какой‑нибудь TextView: «В разработке»
            return
        }
        // Читаем ID и имя группы
        groupId = intent.getIntExtra("GROUP_ID", -1)
        val groupName = intent.getStringExtra("GROUP_NAME") ?: ""
        if (groupId < 0) {
            Toast.makeText(this, "Не передан GROUP_ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        supportActionBar?.title = "Расписание группы $groupName"

        // findViewById
        btnPrev     = findViewById(R.id.btnPrevWeek)
        btnCurr     = findViewById(R.id.btnCurrentWeek)
        btnNext     = findViewById(R.id.btnNextWeek)
        tvDateRange = findViewById(R.id.tvDateRange)
        rvSchedule  = findViewById(R.id.rvSchedule)
        tvEmptySchedule = findViewById(R.id.tvEmptySchedule)

        // RecyclerView
        scheduleAdapter = ScheduleAdapter(emptyList())

        rvSchedule.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter       = scheduleAdapter
        }

        // Кнопки
        btnPrev.setOnClickListener { changeWeek(-1) }
        btnCurr.setOnClickListener { changeWeek(0) }
        btnNext.setOnClickListener { changeWeek(+1) }

        loadSchedule()
    }

    private fun changeWeek(delta: Int) {
             // если delta == 0 — сброс на текущую, иначе накапливаем
             weekOffset = if (delta == 0) 0 else weekOffset + delta
             loadSchedule()
         }

    private fun loadSchedule() {
        // 1) Понедельник недели
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            add(Calendar.WEEK_OF_YEAR, weekOffset)
        }
        // 2) Диапазон dd.MM – dd.MM
        val fmtShort = SimpleDateFormat("dd.MM", Locale.getDefault())
        val start = fmtShort.format(cal.time)
        val endCal = Calendar.getInstance().apply {
            time = cal.time
            add(Calendar.DAY_OF_YEAR, 6)
        }
        val end = fmtShort.format(endCal.time)
        tvDateRange.text = "$start – $end"

        // 3) Параметр для API yyyy-MM-dd
        val fmtApi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateParam = fmtApi.format(cal.time)

        // 4) Запрос
        uiScope.launch {
            val html: String = try {
                withContext(Dispatchers.IO) {
                    ScheduleRepository()
                        .fetchSchedule(groupId, dateParam)
                        .body()
                        .orEmpty()
                }
            } catch (e: Exception) {
                // Если это отмена корутины — просто уходим, без тоста
                if (e is CancellationException) throw e
                // Иначе — реальная сетевая ошибка
                Toast.makeText(
                    this@MainActivity,
                    "Ошибка сети при загрузке расписания",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            // Парсим дни (парсер сам возвращает пустой список, если JSON не найден)
            val days = try {
                ScheduleParser.parse(html, groupId)
            } catch (_: Exception) {
                emptyList()
            }

            // Обновляем экран
            scheduleAdapter.update(days)
            withContext(Dispatchers.Main) {
                scheduleAdapter.update(days)
                tvEmptySchedule.visibility =
                    if (days.isEmpty()) View.VISIBLE else View.GONE
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
