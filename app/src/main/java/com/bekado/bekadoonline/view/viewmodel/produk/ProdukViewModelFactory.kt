package com.bekado.bekadoonline.view.viewmodel.produk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bekado.bekadoonline.di.Injection
import com.bekado.bekadoonline.domain.usecase.ProductUseCase

@Suppress("UNCHECKED_CAST")
class ProdukViewModelFactory(private val produkUseCase: ProductUseCase) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ProdukViewModel::class.java) -> ProdukViewModel(produkUseCase) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        fun getInstance() = ProdukViewModelFactory(Injection.provideProductUseCase())
    }
}