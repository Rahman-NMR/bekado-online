package com.bekado.bekadoonline.domain.repositories

import android.content.Intent
import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.google.firebase.auth.FirebaseUser

interface UserRepository {
    fun getAuthCurrentUser(): FirebaseUser?
    fun getAkun(): LiveData<AkunModel?>
    fun getLoading(): LiveData<Boolean>
    fun logoutAkun()
    fun removeListener()
    fun loginManual(email: String, password: String, response: (Boolean) -> Unit)
    fun loginGoogle(idToken: String?, response: (Boolean) -> Unit)
    fun intentGoogleSignIn(): Intent
    fun registerAuth(email: String, password: String, nama: String, noHp: String, response: (Boolean) -> Unit)
    fun autoRegisterUserToRtdb(response: (Boolean) -> Unit)
}