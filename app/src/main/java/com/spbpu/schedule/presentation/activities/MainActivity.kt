package com.spbpu.schedule.presentation.activities
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spbpu.schedule.R
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import android.view.Gravity
import android.view.ViewGroup

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val role = intent.getStringExtra("ROLE")

        when (role) {
            "STUDENT" -> {
                val group = intent.getStringExtra("GROUP")
                supportActionBar?.title = "Расписание группы $group"
            }
            "TEACHER" -> {
                supportActionBar?.setDisplayShowCustomEnabled(true)
                supportActionBar?.setDisplayShowTitleEnabled(false)

                val teacherName = intent.getStringExtra("TEACHER_NAME") ?: "Неизвестный преподаватель"
                val customView = layoutInflater.inflate(R.layout.custom_action_bar_title, null).apply {
                    layoutParams = ActionBar.LayoutParams(
                        ActionBar.LayoutParams.MATCH_PARENT,
                        ActionBar.LayoutParams.WRAP_CONTENT
                    )
                }

                val titleLine1 = customView.findViewById<TextView>(R.id.title_line1)
                val titleLine2 = customView.findViewById<TextView>(R.id.title_line2)

                titleLine1.text = "Расписание преподавателя"
                titleLine2.text = teacherName

                supportActionBar?.customView = customView
            }
        }

        // 1. Инициализация кнопок
        findViewById<Button>(R.id.btnPrevWeek).setOnClickListener {
            Toast.makeText(this, "Предыдущая неделя", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnNextWeek).setOnClickListener {
            Toast.makeText(this, "Следующая неделя", Toast.LENGTH_SHORT).show()
        }

        // 2. Настройка RecyclerView
        val rvSchedule = findViewById<RecyclerView>(R.id.rvSchedule)
        rvSchedule.layoutManager = LinearLayoutManager(this)

        // Временные данные для примера
        val days = listOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота")
        rvSchedule.adapter = DayAdapter(days)
    }
}

// Простой адаптер для теста
class DayAdapter(private val days: List<String>) :
    RecyclerView.Adapter<DayAdapter.DayViewHolder>() {

    class DayViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val tvDayName: android.widget.TextView = view.findViewById(R.id.tvDayName)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): DayViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.tvDayName.text = days[position]
    }

    override fun getItemCount() = days.size
}