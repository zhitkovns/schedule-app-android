package com.spbpu.schedule.presentation.activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.spbpu.schedule.R
import com.spbpu.schedule.data.models.Day

class ScheduleAdapter(
    private var days: List<Day>
) : RecyclerView.Adapter<ScheduleAdapter.DayVH>() {

    inner class DayVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDayName: TextView = itemView.findViewById(R.id.tvDayName)
        val tvDate:    TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        DayVH(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day, parent, false))

    override fun getItemCount() = days.size

    override fun onBindViewHolder(holder: DayVH, position: Int) {
        val day = days[position]
        holder.tvDayName.text = day.name
        holder.tvDate   .text = day.date
    }

    /** Позволяет обновить данные после парсинга */
    fun update(newDays: List<Day>) {
        days = newDays
        notifyDataSetChanged()
    }
}
