package com.bekado.bekadoonline.view.viewmodel.admin

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.domain.usecase.AdminUseCase

class ProdukViewModel(private val adminUseCase: AdminUseCase) : ViewModel() {
    private var idProduk: String? = ""

    fun isLoading(): LiveData<Boolean> = adminUseCase.executeLoadingProduk()

    fun getDataProduk(idProduk: String?): LiveData<ProdukModel?> {
        this.idProduk = idProduk
        return adminUseCase.executeGetDataProduk(idProduk)
    }

    fun updateDataProduk(
        isEdit: Boolean,
        imageUri: Uri?,
        idProduk: String?,
        idKategori: String?,
        namaProduk: String,
        hargaProduk: Long,
        response: (Boolean) -> Unit
    ) {
        adminUseCase.executeUpdateDetailProduk(
            isEdit = isEdit,
            idProduk = idProduk,
            imageUri = imageUri,
            idKategori = idKategori,
            namaProduk = namaProduk,
            hargaProduk = hargaProduk,
            response = response
        )
    }

    fun deleteProduk(idProduk: String?, response: (Boolean) -> Unit) {
        adminUseCase.executeDeleteProduk(idProduk, response)
    }

    override fun onCleared() {
        super.onCleared()
        adminUseCase.executeRemoveListenerProduk(idProduk)
    }
}