package com.bekado.bekadoonline.domain.repositories

import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.AkunModel

interface UserRepository {
    fun getAkun(): LiveData<AkunModel?>
    fun getLoading(): LiveData<Boolean>
    fun logoutAkun()
    fun removeListener()
}