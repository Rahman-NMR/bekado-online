package com.bekado.bekadoonline.view.viewmodel.user

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.model.AlamatModel
import com.bekado.bekadoonline.domain.usecase.UserUseCase

class AlamatViewModel(private val userUseCase: UserUseCase) : ViewModel() {
    fun isLoading(): LiveData<Boolean> = userUseCase.executeAlamatLoading()
    fun getDataAlamat(): LiveData<AlamatModel?> = userUseCase.executeGetDataAlamat()

    fun updateDataAlamat(namaAlamat: String, nohpAlamat: String, alamatLengkap: String, kodePos: String, response: (Boolean) -> Unit) {
        userUseCase.executeUpdateDataAlamat(namaAlamat, nohpAlamat, alamatLengkap, kodePos, response)
    }

    fun saveLatLong(location: Location, response: (Boolean) -> Unit) {
        userUseCase.executeSaveLatLong(location, response)
    }

    override fun onCleared() {
        super.onCleared()
        userUseCase.executeRemoveAlamatListener()
    }
}