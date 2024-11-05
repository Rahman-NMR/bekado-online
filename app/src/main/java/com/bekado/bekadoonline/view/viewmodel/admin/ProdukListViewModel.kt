package com.bekado.bekadoonline.view.viewmodel.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.domain.usecase.AdminUseCase

class ProdukListViewModel(private val adminUseCase: AdminUseCase) : ViewModel() {
    private var idKategori: String? = ""

    fun isLoading(): LiveData<Boolean> = adminUseCase.executeLoadingProdukList()

    fun getProdukList(idKategori: String?): LiveData<ArrayList<ProdukModel>?> {
        this.idKategori = idKategori
        return adminUseCase.executeGetDataProdukList(idKategori)
    }

    fun updateVisibilitasProduk(idProduk: String?, visibility: Boolean, response: (Boolean) -> Unit) {
        adminUseCase.executeUpdateVisibilityProduk(idProduk, visibility, response)
    }

    override fun onCleared() {
        super.onCleared()
        adminUseCase.executeRemoveListenerProdukList(idKategori)
    }
}