package com.bekado.bekadoonline.domain.repositories

import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.AlamatModel

interface AddressRepository {
    fun getDataAlamat(): LiveData<AlamatModel?>
    fun getLoading(): LiveData<Boolean>
    fun removeListener()
}