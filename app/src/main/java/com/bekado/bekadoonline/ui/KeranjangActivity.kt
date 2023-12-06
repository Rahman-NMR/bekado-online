package com.bekado.bekadoonline.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bekado.bekadoonline.databinding.ActivityKeranjangBinding

class KeranjangActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKeranjangBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeranjangBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}