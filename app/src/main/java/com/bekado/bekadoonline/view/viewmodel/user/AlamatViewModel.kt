package com.bekado.bekadoonline.view.viewmodel.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.model.AlamatModel
import com.bekado.bekadoonline.domain.usecase.UserUseCase

class AlamatViewModel(private val userUseCase: UserUseCase) : ViewModel() {
    fun isLoading(): LiveData<Boolean> = userUseCase.executeAlamatLoading()
    fun getDataAlamat(): LiveData<AlamatModel?> = userUseCase.executeGetDataAlamat()

    override fun onCleared() {
        super.onCleared()
        userUseCase.executeRemoveAlamatListener()
    }
}