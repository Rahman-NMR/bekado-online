package com.bekado.bekadoonline.domain.repositories

import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.CombinedKeranjangModel
import com.google.android.gms.tasks.Task

interface CartRepository {
    fun getDataKeranjang(): LiveData<ArrayList<CombinedKeranjangModel>?>
    fun getLoading(): LiveData<Boolean>
    fun updateJumlahProduk(path: String?, isPlus: Boolean, response: (Boolean) -> Unit)
    fun updateProdukTerpilih(idProduk: String?, isChecked: Boolean, response: (Boolean) -> Unit)
    fun deleteThisProduk(idProduk: String?, response: (Boolean) -> Unit)
    fun deleteSelectedProduk(selectedKeranjang: List<CombinedKeranjangModel>?, response: (Boolean) -> Unit)
    fun cancelAction(itemKeranjang: CombinedKeranjangModel): Task<Void>?
    fun startListener()
    fun removeListener()
}