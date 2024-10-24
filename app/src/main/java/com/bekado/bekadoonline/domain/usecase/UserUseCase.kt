package com.bekado.bekadoonline.domain.usecase

import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.AkunModel

interface UserUseCase {
    fun execute(): LiveData<AkunModel?>
    fun executeLoading(): LiveData<Boolean>
    fun executeLogout()
    fun executeRemoveListener()
}