package com.bekado.bekadoonline.view.viewmodel.keranjang

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.model.CombinedKeranjangModel
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

    fun deleteSelectedProduk(selectedKeranjang: List<CombinedKeranjangModel>?, response: (Boolean) -> Unit) {
        cartUseCase.executeDeleteSelectedProduk(selectedKeranjang, response)
    }

    fun cancelAction(itemKeranjang: CombinedKeranjangModel) {
        cartUseCase.executeCancelAction(itemKeranjang)
    }

    fun startListener() {
        cartUseCase.executeStartListener()
    }
    //todo(remove & start listener taroh kaya deleteSelectedProduk aja pas di tempat checkout kalo make cartRepository)
    fun clearListener() {
        cartUseCase.executeRemoveListener()
    }

    override fun onCleared() {
        super.onCleared()
        clearListener()
    }
}