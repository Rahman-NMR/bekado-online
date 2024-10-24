package com.bekado.bekadoonline.domain.interactor

import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.model.TrxCountModel
import com.bekado.bekadoonline.domain.repositories.TrxRepository
import com.bekado.bekadoonline.domain.usecase.TrxUseCase

class TrxUseCaseInteractor(private val trxRepository: TrxRepository) : TrxUseCase {
    override fun executeLoading(): LiveData<Boolean> {
        return trxRepository.getLoading()
    }

    override fun executeTotalTransaksi(akunModel: AkunModel?): LiveData<TrxCountModel?> {
        return trxRepository.getTotalTransaksi(akunModel)
    }

    override fun executeRemoveListener(akunModel: AkunModel?) {
        trxRepository.removeListener(akunModel)
    }
}