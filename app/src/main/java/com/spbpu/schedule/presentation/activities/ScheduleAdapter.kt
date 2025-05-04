package com.spbpu.schedule.presentation.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.spbpu.schedule.R
import com.spbpu.schedule.data.models.Day
import com.spbpu.schedule.data.models.Lesson

class ScheduleAdapter(
    private var days: List<Day>
) : RecyclerView.Adapter<ScheduleAdapter.DayVH>() {

    inner class DayVH(val container: LinearLayout) : RecyclerView.ViewHolder(container) {
        val tvDayName: TextView = container.findViewById(R.id.tvDayName)
        val tvDate:    TextView = container.findViewById(R.id.tvDate)
        val llLessons: LinearLayout = container.findViewById(R.id.llLessons)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day, parent, false) as LinearLayout
        return DayVH(view)
    }

    override fun getItemCount() = days.size

    override fun onBindViewHolder(holder: DayVH, position: Int) {
        val day = days[position]
        holder.tvDayName.text = day.name
        holder.tvDate   .text = day.date

        // Очищаем предыдущие уроки
        holder.llLessons.removeAllViews()

        // Вкидываем каждый Lesson через inflate
        for (lesson in day.lessons) {
            val lessonView = LayoutInflater.from(holder.container.context)
                .inflate(R.layout.item_lesson, holder.llLessons, false)

            lessonView.findViewById<TextView>(R.id.tvLessonTime).text =
                "${lesson.timeStart}–${lesson.timeEnd}"
            lessonView.findViewById<TextView>(R.id.tvLessonSubject).text =
                "${lesson.subject} (${lesson.type})"

            val info = listOfNotNull(
                lesson.teacher,
                lesson.auditorium?.let { "ауд. $it" }
            ).joinToString(" – ")
            lessonView.findViewById<TextView>(R.id.tvLessonInfo).text = info

            holder.llLessons.addView(lessonView)
        }
    }

    fun update(newDays: List<Day>) {
        days = newDays
        notifyDataSetChanged()
    }
}
