package com.bekado.bekadoonline.domain.usecase

import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.google.firebase.auth.FirebaseUser

interface UserUseCase {
    fun executeCurrentUser(): FirebaseUser?
    fun executeGetDataAkun(): LiveData<AkunModel?>
    fun executeLoading(): LiveData<Boolean>
    fun executeLogout()
    fun executeRemoveListener()
}