package com.bekado.bekadoonline.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.Repository
import com.bekado.bekadoonline.data.model.AkunModel

class AkunViewModel(private val repository: Repository) : ViewModel() {
    val akunModel: LiveData<AkunModel?> = repository.akunModel
    val isLoading: LiveData<Boolean> = repository.isLoading

    fun loadAkunData() {
        repository.loadAkunData()
    }
}