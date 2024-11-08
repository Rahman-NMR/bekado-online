package com.bekado.bekadoonline.domain.interactor

import android.content.Intent
import android.location.Location
import android.net.Uri
import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.model.AlamatModel
import com.bekado.bekadoonline.data.model.VerificationResult
import com.bekado.bekadoonline.domain.repositories.AddressRepository
import com.bekado.bekadoonline.domain.repositories.UserRepository
import com.bekado.bekadoonline.domain.repositories.UserUpdateRepository
import com.bekado.bekadoonline.domain.usecase.UserUseCase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser

class UserUseCaseInteractor(
    private val userRepository: UserRepository,
    private val addressRepository: AddressRepository,
    private val userUpdateRepository: UserUpdateRepository
) : UserUseCase {
    override fun executeCurrentUser(): FirebaseUser? {
        return userRepository.getAuthCurrentUser()
    }

    override fun executeGetDataAkun(): LiveData<AkunModel?> {
        return userRepository.getAkun()
    }

    override fun executeLoading(): LiveData<Boolean> {
        return userRepository.getLoading()
    }

    override fun executeIsVerified(): VerificationResult {
        return userRepository.isVerified()
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

    override fun executeUpdateDataAlamat(
        namaAlamat: String,
        nohpAlamat: String,
        alamatLengkap: String,
        kodePos: String,
        response: (Boolean) -> Unit
    ) {
        addressRepository.updateDataAlamat(namaAlamat, nohpAlamat, alamatLengkap, kodePos, response)
    }

    override fun executeSaveLatLong(location: Location, response: (Boolean) -> Unit) {
        addressRepository.saveLatLong(location, response)
    }

    override fun executeRemoveAlamatListener() {
        addressRepository.removeListener()
    }

    override fun executeUpdateDataAkun(pathDb: String, value: String, response: (Boolean) -> Unit) {
        userUpdateRepository.updateDataAkun(pathDb, value, response)
    }

    override fun executeUpdateImageUri(imageUri: Uri, response: (Boolean) -> Unit) {
        userUpdateRepository.updateImageUri(imageUri, response)
    }

    override fun executeLinkToGoogle(data: Intent?, response: (Boolean, String) -> Unit) {
        userRepository.linkToGoogle(data, response)
    }

    override fun executeLinkCredentials(credential: AuthCredential, response: (Boolean) -> Unit) {
        userRepository.linkCredentials(credential, response)
    }
}