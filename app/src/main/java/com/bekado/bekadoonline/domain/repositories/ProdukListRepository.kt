package com.bekado.bekadoonline.domain.repositories

import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.ProdukModel

interface ProdukListRepository {
    fun getLoading(): LiveData<Boolean>
    fun getDataProdukList(idKategori: String?): LiveData<ArrayList<ProdukModel>?>
    fun updateVisibilityProduk(idProduk: String?, visibility: Boolean, response: (Boolean) -> Unit)
    fun removeListenerProdukList(idKategori: String?)
}