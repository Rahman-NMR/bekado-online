package com.bekado.bekadoonline.domain.usecase

import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.model.TrxCountModel

interface TrxUseCase {
    fun executeLoading(): LiveData<Boolean>
    fun executeTotalTransaksi(akunModel: AkunModel?): LiveData<TrxCountModel?>
    fun executeRemoveListener(akunModel: AkunModel?)
}