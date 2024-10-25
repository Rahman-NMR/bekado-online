package com.bekado.bekadoonline.view.viewmodel.user

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.domain.usecase.UserUseCase

class AuthViewModel(private val userUseCase: UserUseCase) : ViewModel() {
    fun launchSignInClient(): Intent = userUseCase.executeIntentGoogleSignIn()

    fun loginAuthManual(email: String, password: String, response: (Boolean) -> Unit) {
        userUseCase.executeLoginAuthManual(email, password, response)
    }

    fun loginAuthWithGoogle(idToken: String?, response: (Boolean) -> Unit) {
        userUseCase.executeLoginAuthWithGoogle(idToken, response)
    }

    fun registerAuth(email: String, password: String, nama: String, noHp: String, response: (Boolean) -> Unit) {
        userUseCase.executeRegisterAuth(email, password, nama, noHp, response)
    }

    fun autoRegisterToRtdb(param: (Boolean) -> Unit) {
        userUseCase.executeAutoRegisterUserToRtdb(param)
    }
}