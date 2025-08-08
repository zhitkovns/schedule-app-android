package com.spbpu.schedule.presentation.activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.spbpu.schedule.R
import com.spbpu.schedule.RuzApi.models.DaySchedule

class ScheduleTeacherAdapter(
    private var items: List<DaySchedule>
) : RecyclerView.Adapter<ScheduleTeacherAdapter.TeacherScheduleViewHolder>() {

    class TeacherScheduleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateText: TextView = view.findViewById(R.id.textDate)
        val lessonsText: TextView = view.findViewById(R.id.textLessons)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeacherScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day_schedule, parent, false)
        return TeacherScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeacherScheduleViewHolder, position: Int) {
        val item = items[position]
        holder.dateText.text = item.date
        holder.lessonsText.text = item.lessons
    }

    override fun getItemCount(): Int = items.size
    var originalItems: List<DaySchedule> = items
        private set
    fun updateItems(newItems: List<DaySchedule>) {
        originalItems = newItems
        items = newItems
        notifyDataSetChanged()
    }
}
