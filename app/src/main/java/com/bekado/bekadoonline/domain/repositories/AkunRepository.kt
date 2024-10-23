package com.bekado.bekadoonline.domain.repositories

import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.AkunModel

interface AkunRepository {
    fun getAkun(): LiveData<AkunModel?>
    fun getLoading(): LiveData<Boolean>
    fun logoutAkun()
}