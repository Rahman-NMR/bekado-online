package com.bekado.bekadoonline.view.viewmodel.user

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.domain.usecase.UserUseCase

class UserDataUpdateViewModel(private val userUseCase: UserUseCase) : ViewModel() {
    fun updateDataAkun(pathDb: String, value: String, response: (Boolean) -> Unit) {
        userUseCase.executeUpdateDataAkun(pathDb, value, response)
    }

    fun updateImageUri(imageUri: Uri, response: (Boolean) -> Unit) {
        userUseCase.executeUpdateImageUri(imageUri, response)
    }

    override fun onCleared() {
        super.onCleared()
        userUseCase.executeRemoveListener()
    }
}