package com.spbpu.schedule.presentation.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.spbpu.schedule.databinding.ActivityTeacherInputBinding

class TeacherInputActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTeacherInputBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnContinue.setOnClickListener {
            val teacherName = binding.etTeacherName.text.toString()
            if (teacherName.isNotBlank()) {
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("ROLE", "TEACHER")
                    putExtra("TEACHER_NAME", teacherName)
                }
                startActivity(intent)
                finish()
            } else {
                binding.etTeacherName.error = "Введите ФИО"
            }
        }
    }
}