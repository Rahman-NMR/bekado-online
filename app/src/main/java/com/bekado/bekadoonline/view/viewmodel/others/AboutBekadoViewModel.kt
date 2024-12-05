package com.bekado.bekadoonline.view.viewmodel.others

import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.model.TokoModel
import com.bekado.bekadoonline.data.repository.ZZZSimpleRepository

class AboutBekadoViewModel(val repository: ZZZSimpleRepository) : ViewModel() {
    fun getDataToko(response: (TokoModel, Boolean) -> Unit) {
        repository.getDataToko(response)
    }
}