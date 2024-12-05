package com.bekado.bekadoonline.view.viewmodel.others

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bekado.bekadoonline.data.repository.ZZZSimpleRepository
import com.bekado.bekadoonline.di.Injection

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val repository: ZZZSimpleRepository) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AboutBekadoViewModel::class.java) -> AboutBekadoViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        fun getInstance() = ViewModelFactory(Injection.provideUseCase())
    }
}