package com.bekado.bekadoonline.view.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bekado.bekadoonline.di.Injection
import com.bekado.bekadoonline.domain.usecase.AdminUseCase

@Suppress("UNCHECKED_CAST")
class AdminViewModelFactory(private val adminUseCase: AdminUseCase) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(KategoriListViewModel::class.java) -> KategoriListViewModel(adminUseCase) as T
            modelClass.isAssignableFrom(ProdukListViewModel::class.java) -> ProdukListViewModel(adminUseCase) as T
            modelClass.isAssignableFrom(KategoriViewModel::class.java) -> KategoriViewModel(adminUseCase) as T
            modelClass.isAssignableFrom(ProdukViewModel::class.java) -> ProdukViewModel(adminUseCase) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        fun getInstance() = AdminViewModelFactory(Injection.provideAdminUseCase())
    }
}