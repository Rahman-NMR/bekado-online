package com.bekado.bekadoonline.model.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.model.AkunModel
import com.google.firebase.database.DatabaseReference

class ClientDataViewModel : ViewModel() {
    private val _dataAkun = MutableLiveData<AkunModel?>(null)
    val dataAkun: LiveData<AkunModel?> = _dataAkun

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadDataClient(akunRef: DatabaseReference) {
        _isLoading.value = true

        akunRef.get().addOnSuccessListener { data ->
            val akunModel = data.getValue(AkunModel::class.java)
            _dataAkun.value = akunModel
            _isLoading.value = false
        }.addOnFailureListener {
            _dataAkun.value = null
            _isLoading.value = false
        }
    }
}