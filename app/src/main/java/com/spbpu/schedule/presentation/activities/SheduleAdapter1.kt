package com.spbpu.schedule.presentation.activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.spbpu.schedule.R
import com.spbpu.schedule.RuzApi.models.DaySchedule

class ScheduleAdapter1(private var items: List<DaySchedule>) :
    RecyclerView.Adapter<ScheduleAdapter1.ScheduleViewHolder>() {

    class ScheduleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateText: TextView = view.findViewById(R.id.textDate)
        val lessonsText: TextView = view.findViewById(R.id.textLessons)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val item = items[position]
        holder.dateText.text = item.date
        holder.lessonsText.text = item.lessons
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<DaySchedule>) {
        items = newItems
        notifyDataSetChanged()
    }
}
