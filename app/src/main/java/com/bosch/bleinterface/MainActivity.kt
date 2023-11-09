package com.bosch.bleinterface

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bosch.bleinterface.databinding.ActivityMainBinding
import com.bosch.blelibrary.BleManagerActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btBleSettings.setOnClickListener {
            val intent = Intent(this, BleActivity::class.java)
            startActivity(intent)
        }
    }
}