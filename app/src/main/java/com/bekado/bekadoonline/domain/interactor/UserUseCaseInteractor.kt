package com.bekado.bekadoonline.domain.interactor

import android.content.Intent
import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.model.AlamatModel
import com.bekado.bekadoonline.domain.repositories.AddressRepository
import com.bekado.bekadoonline.domain.repositories.UserRepository
import com.bekado.bekadoonline.domain.usecase.UserUseCase
import com.google.firebase.auth.FirebaseUser

class UserUseCaseInteractor(private val userRepository: UserRepository, private val addressRepository: AddressRepository) : UserUseCase {
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

    override fun executeLoginAuthManual(email: String, password: String, response: (Boolean) -> Unit) {
        userRepository.loginManual(email, password, response)
    }

    override fun executeLoginAuthWithGoogle(idToken: String?, response: (Boolean) -> Unit) {
        userRepository.loginGoogle(idToken, response)
    }

    override fun executeIntentGoogleSignIn(): Intent {
        return userRepository.intentGoogleSignIn()
    }

    override fun executeRegisterAuth(email: String, password: String, nama: String, noHp: String, response: (Boolean) -> Unit) {
        userRepository.registerAuth(email, password, nama, noHp, response)
    }

    override fun executeAutoRegisterUserToRtdb(response: (Boolean) -> Unit) {
        userRepository.autoRegisterUserToRtdb(response)
    }

    override fun executeGetDataAlamat(): LiveData<AlamatModel?> {
        return addressRepository.getDataAlamat()
    }

    override fun executeAlamatLoading(): LiveData<Boolean> {
        return addressRepository.getLoading()
    }

    override fun executeRemoveAlamatListener() {
        addressRepository.removeListener()
    }
}