package com.bekado.bekadoonline.domain.interactor

import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.CombinedKeranjangModel
import com.bekado.bekadoonline.domain.repositories.CartRepository
import com.bekado.bekadoonline.domain.usecase.CartUseCase
import com.google.android.gms.tasks.Task

class CartUseCaseInteractor(private val cartRepository: CartRepository) : CartUseCase {
    override fun executeGetDataKeranjang(): LiveData<ArrayList<CombinedKeranjangModel>?> {
        return cartRepository.getDataKeranjang()
    }

    override fun executeLoading(): LiveData<Boolean> {
        return cartRepository.getLoading()
    }

    override fun executeUpdateJumlahProduk(path: String?, isPlus: Boolean, response: (Boolean) -> Unit) {
        return cartRepository.updateJumlahProduk(path, isPlus, response)
    }

    override fun executeUpdateProdukTerpilih(idProduk: String?, isChecked: Boolean, response: (Boolean) -> Unit) {
        return cartRepository.updateProdukTerpilih(idProduk, isChecked, response)
    }

    override fun executeDeleteThisProduk(idProduk: String?, response: (Boolean) -> Unit) {
        return cartRepository.deleteThisProduk(idProduk, response)
    }

    override fun executeDeleteSelectedProduk(produkSelected: List<CombinedKeranjangModel>?, response: (Boolean) -> Unit) {
        return cartRepository.deleteSelectedProduk(produkSelected, response)
    }

    override fun executeCancelAction(itemKeranjang: CombinedKeranjangModel): Task<Void>? {
        return cartRepository.cancelAction(itemKeranjang)
    }

    override fun executeStartListener() {
        return cartRepository.startListener()
    }

    override fun executeRemoveListener() {
        return cartRepository.removeListener()
    }
}