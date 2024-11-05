package com.bekado.bekadoonline.view.viewmodel.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.model.KategoriModel
import com.bekado.bekadoonline.domain.usecase.AdminUseCase

class KategoriViewModel(private val adminUseCase: AdminUseCase) : ViewModel() {
    private var idKategori: String? = ""

    fun isLoading(): LiveData<Boolean> = adminUseCase.executeLoadingKategori()

    fun getDataKategori(idKategori: String?): LiveData<KategoriModel?> {
        this.idKategori = idKategori
        return adminUseCase.executeGetDataKategori(idKategori)
    }

    fun deleteKategori(idKategori: String?, response: (Boolean) -> Unit) {
        adminUseCase.executeDeleteKategori(idKategori, response)
    }

    override fun onCleared() {
        super.onCleared()
        adminUseCase.executeRemoveListenerKategori(idKategori)
    }
}