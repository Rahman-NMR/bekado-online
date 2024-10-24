package com.bekado.bekadoonline.domain.interactor

import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.domain.repositories.UserRepository
import com.bekado.bekadoonline.domain.usecase.UserUseCase
import com.google.firebase.auth.FirebaseUser

class UserUseCaseInteractor(private val userRepository: UserRepository) : UserUseCase {
    override fun executeCurrentUser(): FirebaseUser? {
        return userRepository.getAuthCurrentUser()
    }

    override fun executeGetDataAkun(): LiveData<AkunModel?> {
        return userRepository.getAkun()
    }

    override fun executeLoading(): LiveData<Boolean> {
        return userRepository.getLoading()
    }

    override fun executeLogout() {
        userRepository.logoutAkun()
    }

    override fun executeRemoveListener() {
        userRepository.removeListener()
    }
}