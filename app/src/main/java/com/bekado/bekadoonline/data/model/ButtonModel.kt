package com.bekado.bekadoonline.data.model

data class ButtonModel(
    val namaKategori: String,
    val idKategori: String,
    var isActive: Boolean,
    var posisi: Long = 0
)
