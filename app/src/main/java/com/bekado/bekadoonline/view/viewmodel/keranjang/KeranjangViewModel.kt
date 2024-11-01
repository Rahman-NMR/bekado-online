package com.bekado.bekadoonline.view.viewmodel.keranjang

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.model.CombinedKeranjangModel
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.domain.usecase.CartUseCase

class KeranjangViewModel(private val cartUseCase: CartUseCase) : ViewModel() {
    fun isLoading(): LiveData<Boolean> = cartUseCase.executeLoading()
    fun getDataKeranjang(): LiveData<ArrayList<CombinedKeranjangModel>?> = cartUseCase.executeGetDataKeranjang()

    fun addJumlahProduk(path: String?, isPlus: Boolean, response: (Boolean) -> Unit) {
        cartUseCase.executeUpdateJumlahProduk(path, isPlus, response)
    }

    fun updateProdukTerpilih(idProduk: String?, isChecked: Boolean, response: (Boolean) -> Unit) {
        cartUseCase.executeUpdateProdukTerpilih(idProduk, isChecked, response)
    }

    fun deleteThisProduk(idProduk: String?, response: (Boolean) -> Unit) {
        cartUseCase.executeDeleteThisProduk(idProduk, response)
    }

    fun deleteSelectedProduk(produkSelected: List<CombinedKeranjangModel>?, response: (Boolean) -> Unit) {
        cartUseCase.executeDeleteSelectedProduk(produkSelected, response)
    }

    fun cancelAction(itemKeranjang: CombinedKeranjangModel) {
        cartUseCase.executeCancelAction(itemKeranjang)
    }

    fun produkExistsInKeranjang(idProduk: String?, response: (Boolean, Long) -> Unit) {
        cartUseCase.executeProdukExistsInKeranjang(idProduk, response)
    }

    fun addDataProdukKeKeranjang(produk: ProdukModel, response: (Boolean) -> Unit) {
        cartUseCase.executeAddDataProdukKeKeranjang(produk, response)
    }

    fun startListener() {
        cartUseCase.executeStartListener()
    }

    override fun onCleared() {
        super.onCleared()
        cartUseCase.executeRemoveListener()
    }
}