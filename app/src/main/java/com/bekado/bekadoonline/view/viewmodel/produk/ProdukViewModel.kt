package com.bekado.bekadoonline.view.viewmodel.produk

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.model.ButtonModel
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.domain.usecase.ProductUseCase

class ProdukViewModel(private val produkUseCase: ProductUseCase) : ViewModel() {
    fun isLoading(): LiveData<Boolean> = produkUseCase.executeLoadingProduk()
    fun getAllProduk(): LiveData<ArrayList<ProdukModel>?> = produkUseCase.executeGetAllDataProduk()
    fun filterByKategori(): LiveData<ArrayList<ButtonModel>?> = produkUseCase.executeFilterByKategori()

    override fun onCleared() {
        super.onCleared()
        produkUseCase.executeRemoveListener()
    }
}