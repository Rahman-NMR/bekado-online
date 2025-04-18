package com.bekado.bekadoonline.view.viewmodel.transaksi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bekado.bekadoonline.di.Injection
import com.bekado.bekadoonline.domain.usecase.TrxUseCase

@Suppress("UNCHECKED_CAST")
class TransaksiViewModelFactory(private val trxUseCase: TrxUseCase) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(TransaksiViewModel::class.java) -> TransaksiViewModel(trxUseCase) as T
            modelClass.isAssignableFrom(TransaksiListViewModel::class.java) -> TransaksiListViewModel(trxUseCase) as T
            modelClass.isAssignableFrom(CheckoutViewModel::class.java) -> CheckoutViewModel(trxUseCase) as T
            modelClass.isAssignableFrom(DetailTransaksiViewModel::class.java) -> DetailTransaksiViewModel(trxUseCase) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        fun getInstance() = TransaksiViewModelFactory(Injection.provideTrxUseCase())
    }
}