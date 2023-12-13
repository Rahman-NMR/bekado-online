package com.bekado.bekadoonline.adapter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.ActivityPembayaranBinding

class PembayaranActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPembayaranBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPembayaranBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}