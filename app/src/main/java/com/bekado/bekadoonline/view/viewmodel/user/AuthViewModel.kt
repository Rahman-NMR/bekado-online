package com.bekado.bekadoonline.view.viewmodel.user

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.domain.usecase.UserUseCase
import com.google.firebase.auth.AuthCredential

class AuthViewModel(private val userUseCase: UserUseCase) : ViewModel() {
    fun launchSignInClient(): Intent = userUseCase.executeIntentGoogleSignIn()

    fun loginAuthManual(email: String, password: String, response: (Boolean) -> Unit) {
        userUseCase.executeLoginAuthManual(email, password, response)
    }

    fun loginAuthWithGoogle(data: Intent?, response: (Boolean) -> Unit) {
        userUseCase.executeLoginAuthWithGoogle(data, response)
    }

    fun registerAuth(email: String, password: String, nama: String, noHp: String, response: (Boolean) -> Unit) {
        userUseCase.executeRegisterAuth(email, password, nama, noHp, response)
    }

    fun autoRegisterToRtdb(param: (Boolean) -> Unit) {
        userUseCase.executeAutoRegisterUserToRtdb(param)
    }

    fun linkToGoogle(data: Intent?, response: (Boolean, String) -> Unit) {
        userUseCase.executeLinkToGoogle(data, response)
    }

    fun linkCredentials(credential: AuthCredential, response: (Boolean) -> Unit) {
        userUseCase.executeLinkCredentials(credential, response)
    }
}