package com.bekado.bekadoonline.domain.usecase

import android.net.Uri
import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.KategoriModel
import com.bekado.bekadoonline.data.model.ProdukModel
import java.util.ArrayList

interface AdminUseCase {
    fun executeLoadingKategoriList(): LiveData<Boolean>
    fun executeGetDataKategoriList(): LiveData<ArrayList<KategoriModel>?>
    fun executeAddNewKategori(namaKategori: String, posisi: Long, response: (Boolean) -> Unit)
    fun executeUpdateNamaKategori(idKategori: String?, namaKategori: String, response: (Boolean) -> Unit)
    fun executeUpdateVisibilitasKategori(idKategori: String?, visibilitas: Boolean, response: (Boolean) -> Unit)
    fun executeRemoveListenerKategoriList()

    fun executeLoadingProdukList(): LiveData<Boolean>
    fun executeGetDataProdukList(idKategori: String?): LiveData<ArrayList<ProdukModel>?>
    fun executeUpdateVisibilityProduk(idProduk: String?, visibility: Boolean, response: (Boolean) -> Unit)
    fun executeRemoveListenerProdukList(idKategori: String?)

    fun executeLoadingKategori(): LiveData<Boolean>
    fun executeGetDataKategori(extraIdKategori: String?): LiveData<KategoriModel?>
    fun executeDeleteKategori(idKategori: String?, response: (Boolean) -> Unit)
    fun executeRemoveListenerKategori(idKategori: String?)

    fun executeLoadingProduk(): LiveData<Boolean>
    fun executeGetDataProduk(idProduk: String?): LiveData<ProdukModel?>
    fun executeUpdateDetailProduk(
        isEdit: Boolean,
        imageUri: Uri?,
        idProduk: String?,
        idKategori: String?,
        namaProduk: String,
        hargaProduk: Long,
        response: (Boolean) -> Unit
    )
    fun executeDeleteProduk(idProduk: String?, response: (Boolean) -> Unit)
    fun executeRemoveListenerProduk(idProduk: String?)
}