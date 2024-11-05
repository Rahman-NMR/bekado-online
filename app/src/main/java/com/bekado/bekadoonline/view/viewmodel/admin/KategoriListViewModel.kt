package com.bekado.bekadoonline.view.viewmodel.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.model.KategoriModel
import com.bekado.bekadoonline.domain.usecase.AdminUseCase

class KategoriListViewModel(private val adminUseCase: AdminUseCase) : ViewModel() {
    fun isLoading(): LiveData<Boolean> = adminUseCase.executeLoadingKategoriList()
    fun getKategoriList(): LiveData<ArrayList<KategoriModel>?> = adminUseCase.executeGetDataKategoriList()

    fun addKategori(namaKategori: String, posisi: Long, response: (Boolean) -> Unit) {
        adminUseCase.executeAddNewKategori(namaKategori, posisi, response)
    }

    fun updateNamaKategori(idKategori: String?, namaKategori: String, response: (Boolean) -> Unit) {
        adminUseCase.executeUpdateNamaKategori(idKategori, namaKategori, response)
    }

    fun updateVisibilitasKategori(idKategori: String?, visibilitas: Boolean, response: (Boolean) -> Unit) {
        adminUseCase.executeUpdateVisibilitasKategori(idKategori, visibilitas, response)
    }

    override fun onCleared() {
        super.onCleared()
        adminUseCase.executeRemoveListenerKategoriList()
    }
}