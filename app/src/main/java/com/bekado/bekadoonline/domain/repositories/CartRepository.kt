package com.bekado.bekadoonline.domain.repositories

import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.CombinedKeranjangModel
import com.bekado.bekadoonline.data.model.ProdukModel
import com.google.android.gms.tasks.Task

interface CartRepository {
    fun getDataKeranjang(): LiveData<ArrayList<CombinedKeranjangModel>?>
    fun getLoading(): LiveData<Boolean>
    fun updateJumlahProduk(path: String?, isPlus: Boolean, response: (Boolean) -> Unit)
    fun updateProdukTerpilih(idProduk: String?, isChecked: Boolean, response: (Boolean) -> Unit)
    fun deleteThisProduk(idProduk: String?, response: (Boolean) -> Unit)
    fun deleteSelectedProduk(produkSelected: List<CombinedKeranjangModel>?, response: (Boolean) -> Unit)
    fun cancelAction(itemKeranjang: CombinedKeranjangModel): Task<Void>?
    fun produkExistsInKeranjang(idProduk: String?, response: (Boolean, Long) -> Unit)
    fun addDataProdukKeKeranjang(produk: ProdukModel, response: (Boolean) -> Unit)
    fun startListener()
    fun removeListener()
}