package com.bekado.bekadoonline.domain.repositories

import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.KategoriModel
import java.util.ArrayList

interface KategoriListRepository {
    fun getLoading(): LiveData<Boolean>
    fun getDataKategoriList(): LiveData<ArrayList<KategoriModel>?>
    fun addNewKategori(namaKategori: String, posisi: Long, response: (Boolean) -> Unit)
    fun updateNamaKategori(idKategori: String?, namaKategori: String, response: (Boolean) -> Unit)
    fun updateVisibilitasKategori(idKategori: String?, visibilitas: Boolean, response: (Boolean) -> Unit)
    fun removeListenerKategoriList()
}