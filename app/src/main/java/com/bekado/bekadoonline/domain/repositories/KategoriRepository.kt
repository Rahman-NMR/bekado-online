package com.bekado.bekadoonline.domain.repositories

import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.KategoriModel

interface KategoriRepository {
    fun getLoading(): LiveData<Boolean>
    fun getDataKategori(idKategori: String?): LiveData<KategoriModel?>
    fun deleteKategori(idKategori: String?, response: (Boolean) -> Unit)
    fun removeListenerKategori(idKategori: String?)
}