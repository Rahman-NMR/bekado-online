package com.bekado.bekadoonline.view.viewmodel.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.domain.usecase.AkunUseCase

class UserViewModel(private val akunUseCase: AkunUseCase) : ViewModel() {
    fun getDataAkun(): LiveData<AkunModel?> = akunUseCase.execute()
    fun isLoading(): LiveData<Boolean> = akunUseCase.executeLoading()

    fun clearAkunData() {
        akunUseCase.executeLogout()
    }

    override fun onCleared() {
        super.onCleared()
        akunUseCase.executeRemoveListener()
    }
}