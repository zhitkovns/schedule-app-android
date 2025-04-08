package com.spbpu.schedule.presentation.activities
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spbpu.schedule.R

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
                val teacherName = intent.getStringExtra("TEACHER_NAME")
                supportActionBar?.title = "Расписание преподавателя $teacherName"
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