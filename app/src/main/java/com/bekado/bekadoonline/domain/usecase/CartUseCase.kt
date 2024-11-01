package com.bekado.bekadoonline.domain.usecase

import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.CombinedKeranjangModel
import com.bekado.bekadoonline.data.model.ProdukModel
import com.google.android.gms.tasks.Task

interface CartUseCase {
    fun executeGetDataKeranjang(): LiveData<ArrayList<CombinedKeranjangModel>?>
    fun executeLoading(): LiveData<Boolean>
    fun executeUpdateJumlahProduk(path: String?, isPlus: Boolean, response: (Boolean) -> Unit)
    fun executeUpdateProdukTerpilih(idProduk: String?, isChecked: Boolean, response: (Boolean) -> Unit)
    fun executeDeleteThisProduk(idProduk: String?, response: (Boolean) -> Unit)
    fun executeDeleteSelectedProduk(produkSelected: List<CombinedKeranjangModel>?, response: (Boolean) -> Unit)
    fun executeCancelAction(itemKeranjang: CombinedKeranjangModel): Task<Void>?
    fun executeProdukExistsInKeranjang(idProduk: String?, response: (Boolean, Long) -> Unit)
    fun executeAddDataProdukKeKeranjang(produk: ProdukModel, response: (Boolean) -> Unit)
    fun executeStartListener()
    fun executeRemoveListener()
}