package com.bekado.bekadoonline.domain.repositories

import android.location.Location
import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.AlamatModel

interface AddressRepository {
    fun getDataAlamat(): LiveData<AlamatModel?>
    fun getLoading(): LiveData<Boolean>
    fun updateDataAlamat(namaAlamat: String, nohpAlamat: String, alamatLengkap: String, kodePos: String, response: (Boolean) -> Unit)
    fun saveLatLong(location: Location, response: (Boolean) -> Unit)
    fun removeListener()
}