package com.spbpu.schedule.presentation.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.spbpu.schedule.databinding.ActivityTeacherInputBinding
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.*
import com.spbpu.schedule.RuzApi.RuzSpbStu
import android.util.Log
import com.spbpu.schedule.RuzApi.models.Teacher
import android.text.Editable
import android.text.TextWatcher



class TeacherInputActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTeacherInputBinding
    private lateinit var adapter: TeacherListAdapter
    private var allTeachers: List<Teacher> = emptyList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherInputBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = TeacherListAdapter(emptyList()) { teacher ->
            val intent = Intent(this, TeacherScheduleActivity::class.java)
            intent.putExtra("ROLE", "TEACHER")
            intent.putExtra("TEACHER_NAME", teacher.fullName)
            startActivity(intent)
            finish()
        }
        binding.recyclerTeachers.layoutManager = LinearLayoutManager(this)
        binding.recyclerTeachers.adapter = adapter


        CoroutineScope(Dispatchers.Main).launch {
            try {
                val teachers = withContext(Dispatchers.IO) {
                    RuzSpbStu.getTeachers() // или другой метод получения списка
                }
                allTeachers = teachers
                adapter.updateList(teachers)
            } catch (e: Exception) {
                Log.e("TeacherInputActivity", "Ошибка при получении списка преподавателей", e)
            }
        }


        binding.etTeacherName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim().orEmpty()
                val filtered = if (query.isEmpty()) {
                    allTeachers
                } else {
                    allTeachers.filter { teacher ->
                        teacher.fullName.contains(query, ignoreCase = true)
                    }
                }
                adapter.updateList(filtered)
            }

            override fun afterTextChanged(s: Editable?) {}
        })





        binding.btnContinue.setOnClickListener {
            val teacherName = binding.etTeacherName.text.toString()
            if (teacherName.isNotBlank()) {
                val intent = Intent(this, TeacherScheduleActivity::class.java)
                intent.putExtra("ROLE", "TEACHER")
                intent.putExtra("TEACHER_NAME", teacherName)
                startActivity(intent)
                startActivity(intent)
                finish()
            } else {
                binding.etTeacherName.error = "Введите ФИО"
            }
        }
    }
}