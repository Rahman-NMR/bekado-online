package com.bekado.bekadoonline.view.viewmodel.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.model.VerificationResult
import com.bekado.bekadoonline.domain.usecase.UserUseCase

class UserViewModel(private val userUseCase: UserUseCase) : ViewModel() {
    fun currentUser() = userUseCase.executeCurrentUser()
    fun getDataAkun(): LiveData<AkunModel?> = userUseCase.executeGetDataAkun()
    fun isLoading(): LiveData<Boolean> = userUseCase.executeLoading()
    fun isVerified(): VerificationResult = userUseCase.executeIsVerified()

    fun clearAkunData() {
        userUseCase.executeLogout()
    }

    override fun onCleared() {
        super.onCleared()
        userUseCase.executeRemoveListener()
    }
}