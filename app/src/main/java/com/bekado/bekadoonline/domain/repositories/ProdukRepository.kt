package com.bekado.bekadoonline.domain.repositories

import android.net.Uri
import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.ProdukModel

interface ProdukRepository {
    fun getLoading(): LiveData<Boolean>
    fun getDataProduk(idProduk: String?): LiveData<ProdukModel?>
    fun updateDetailProduk(
        isEdit: Boolean,
        imageUri: Uri?,
        idProduk: String?,
        idKategori: String?,
        namaProduk: String,
        hargaProduk: Long,
        response: (Boolean) -> Unit
    )
    fun deleteProduk(idProduk: String?, response: (Boolean) -> Unit)
    fun removeListenerProduk(idProduk: String?)
}