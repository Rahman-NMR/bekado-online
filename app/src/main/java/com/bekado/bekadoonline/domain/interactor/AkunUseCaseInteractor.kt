package com.bekado.bekadoonline.domain.interactor

import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.domain.repositories.AkunRepository
import com.bekado.bekadoonline.domain.usecase.AkunUseCase

class AkunUseCaseInteractor(private val akunRepository: AkunRepository) : AkunUseCase {
    override fun execute(): LiveData<AkunModel?> {
        return akunRepository.getAkun()
    }

    override fun executeLoading(): LiveData<Boolean> {
        return akunRepository.getLoading()
    }

    override fun remove() {
        akunRepository.logoutAkun()
    }
}