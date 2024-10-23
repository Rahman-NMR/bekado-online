package com.bekado.bekadoonline.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.Repository

class TransaksiViewModel(private val repository: Repository) : ViewModel() {
    /*val totalAntrian: LiveData<Int?> get() = repository.totalAntrian
    val totalProses: LiveData<Int?> get() = repository.totalProses
    val totalSelesai: LiveData<Int?> get() = repository.totalSelesai
    val isLoading: LiveData<Boolean> get() = repository.isLoading

    fun loadTransaksiData() {
        repository.getDataTransaksi()
    }*/
}