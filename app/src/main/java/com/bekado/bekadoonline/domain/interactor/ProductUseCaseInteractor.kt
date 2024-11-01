package com.bekado.bekadoonline.domain.interactor

import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.ButtonModel
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.domain.repositories.ProductRepository
import com.bekado.bekadoonline.domain.usecase.ProductUseCase
import java.util.ArrayList

class ProductUseCaseInteractor(private val productRepository: ProductRepository) : ProductUseCase {
    override fun executeLoadingProduk(): LiveData<Boolean> {
        return productRepository.getLoading()
    }

    override fun executeGetAllDataProduk(): LiveData<ArrayList<ProdukModel>?> {
        return productRepository.getAllDataProduk()
    }

    override fun executeFilterByKategori(): LiveData<ArrayList<ButtonModel>?> {
        return productRepository.filterByKategori()
    }

    override fun executeRemoveListener() {
        productRepository.removeListener()
    }
}