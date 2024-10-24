package com.bekado.bekadoonline.domain.usecase

import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.model.TransaksiModel
import com.bekado.bekadoonline.data.model.TrxCountModel

interface TrxUseCase {
    fun executeLoading(): LiveData<Boolean>
    fun executeListDataTransaksi(akunModel: AkunModel?): LiveData<ArrayList<TransaksiModel>?>
    fun executeTotalTransaksi(akunModel: AkunModel?): LiveData<TrxCountModel?>
    fun executeRemoveListener(akunModel: AkunModel?)
}