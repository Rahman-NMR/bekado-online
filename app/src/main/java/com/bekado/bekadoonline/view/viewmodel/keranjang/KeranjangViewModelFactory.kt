package com.bekado.bekadoonline.view.viewmodel.keranjang

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bekado.bekadoonline.di.Injection
import com.bekado.bekadoonline.domain.usecase.CartUseCase

@Suppress("UNCHECKED_CAST")
class KeranjangViewModelFactory(private val cartUseCase: CartUseCase) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(KeranjangViewModel::class.java) -> KeranjangViewModel(cartUseCase) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        fun getInstance() = KeranjangViewModelFactory(Injection.provideCartUseCase())
    }
}