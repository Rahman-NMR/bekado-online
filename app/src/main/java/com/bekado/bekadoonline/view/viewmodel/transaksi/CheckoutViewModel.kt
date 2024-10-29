package com.bekado.bekadoonline.view.viewmodel.transaksi

import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.model.AlamatModel
import com.bekado.bekadoonline.data.model.TrxDetailModel
import com.bekado.bekadoonline.domain.usecase.TrxUseCase

class CheckoutViewModel(private val trxUseCase: TrxUseCase) : ViewModel() {
    fun addNewTransaksi(
        dataTransaksi: TrxDetailModel,
        dataAlamat: AlamatModel,
        produkMap: MutableMap<String, Any>,
        totalBelanja: Long,
        response: (Boolean) -> Unit
    ) {
        trxUseCase.executeCreateTransaksi(dataTransaksi, dataAlamat, produkMap, totalBelanja, response)
    }
}