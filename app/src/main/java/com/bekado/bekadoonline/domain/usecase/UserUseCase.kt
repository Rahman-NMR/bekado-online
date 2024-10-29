package com.bekado.bekadoonline.domain.usecase

import android.content.Intent
import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.model.AlamatModel
import com.google.firebase.auth.FirebaseUser

interface UserUseCase {
    fun executeCurrentUser(): FirebaseUser?
    fun executeGetDataAkun(): LiveData<AkunModel?>
    fun executeLoading(): LiveData<Boolean>
    fun executeLogout()
    fun executeRemoveListener()
    fun executeLoginAuthManual(email: String, password: String, response: (Boolean) -> Unit)
    fun executeLoginAuthWithGoogle(idToken: String?, response: (Boolean) -> Unit)
    fun executeIntentGoogleSignIn(): Intent
    fun executeRegisterAuth(email: String, password: String, nama: String, noHp: String, response: (Boolean) -> Unit)
    fun executeAutoRegisterUserToRtdb(response: (Boolean) -> Unit)
    fun executeGetDataAlamat(): LiveData<AlamatModel?>
    fun executeAlamatLoading(): LiveData<Boolean>
    fun executeRemoveAlamatListener()
}