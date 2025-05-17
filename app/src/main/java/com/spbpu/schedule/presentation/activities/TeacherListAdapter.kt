package com.spbpu.schedule.presentation.activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.spbpu.schedule.R
import com.spbpu.schedule.RuzApi.models.Teacher

class TeacherListAdapter(
    private var teachers: List<Teacher>,
    private val onClick: (Teacher) -> Unit

) : RecyclerView.Adapter<TeacherListAdapter.TeacherViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeacherViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_teacheritem, parent, false)
        return TeacherViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeacherViewHolder, position: Int) {
        holder.bind(teachers[position], onClick)
    }

    override fun getItemCount(): Int = teachers.size

    fun updateList(newList: List<Teacher>) {
        teachers = newList
        notifyDataSetChanged()
    }

    class TeacherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val btnTeacherName: Button = itemView.findViewById(R.id.btnTeacherName)

        fun bind(teacher: Teacher, onClick: (Teacher) -> Unit) {
            btnTeacherName.text = teacher.fullName
            btnTeacherName.setOnClickListener {
                onClick(teacher)
            }
        }
    }

}
