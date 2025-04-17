package com.spbpu.schedule.presentation.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.spbpu.schedule.databinding.ActivityRoleSelectionBinding

class RoleSelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRoleSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStudent.setOnClickListener {
            startActivity(Intent(this, GroupSelectionActivity::class.java))
            finish() // Закрываем текущую активность
        }

        binding.btnTeacher.setOnClickListener {
            startActivity(Intent(this, TeacherInputActivity::class.java))
            finish()
        }
    }
}