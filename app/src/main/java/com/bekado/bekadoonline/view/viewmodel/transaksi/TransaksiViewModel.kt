package com.bekado.bekadoonline.view.viewmodel.transaksi

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.model.TrxCountModel
import com.bekado.bekadoonline.domain.usecase.TrxUseCase

class TransaksiViewModel(private val trxUseCase: TrxUseCase) : ViewModel() {
    private var dataAkunModel: AkunModel? = AkunModel()

    fun isLoading(): LiveData<Boolean> = trxUseCase.executeLoading()
    fun totalTransaksi(akunModel: AkunModel?): LiveData<TrxCountModel?> {
        dataAkunModel = akunModel
        return trxUseCase.executeTotalTransaksi(akunModel)
    }

    override fun onCleared() {
        super.onCleared()
        trxUseCase.executeRemoveListener(dataAkunModel)
    }
}