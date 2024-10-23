package com.bekado.bekadoonline.view.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bekado.bekadoonline.di.Injection
import com.bekado.bekadoonline.domain.usecase.AkunUseCase

@Suppress("UNCHECKED_CAST")
class UserViewModelFactory(private val akunUseCase: AkunUseCase) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(UserViewModel::class.java) -> UserViewModel(akunUseCase) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        fun getInstance(context: Context) = UserViewModelFactory(Injection.provideAkunUseCase(context))
    }
}