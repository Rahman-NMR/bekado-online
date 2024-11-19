package com.bekado.bekadoonline.domain.usecase

import android.content.Intent
import android.location.Location
import android.net.Uri
import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.model.AlamatModel
import com.bekado.bekadoonline.data.model.VerificationResult
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser

interface UserUseCase {
    fun executeCurrentUser(): FirebaseUser?
    fun executeGetDataAkun(): LiveData<AkunModel?>
    fun executeLoading(): LiveData<Boolean>
    fun executeIsVerified(): VerificationResult
    fun executeLogout()
    fun executeRemoveListener()

    fun executeLoginAuthManual(email: String, password: String, response: (Boolean) -> Unit)
    fun executeLoginAuthWithGoogle(data: Intent?, response: (Boolean) -> Unit)
    fun executeIntentGoogleSignIn(): Intent
    fun executeRegisterAuth(email: String, password: String, nama: String, noHp: String, response: (Boolean) -> Unit)
    fun executeAutoRegisterUserToRtdb(response: (Boolean) -> Unit)

    fun executeGetDataAlamat(): LiveData<AlamatModel?>
    fun executeAlamatLoading(): LiveData<Boolean>
    fun executeUpdateDataAlamat(namaAlamat: String, nohpAlamat: String, alamatLengkap: String, kodePos: String, response: (Boolean) -> Unit)
    fun executeSaveLatLong(location: Location, response: (Boolean) -> Unit)
    fun executeRemoveAlamatListener()

    fun executeUpdateDataAkun(pathDb: String, value: String, response: (Boolean) -> Unit)
    fun executeUpdateImageUri(imageUri: Uri, response: (Boolean) -> Unit)

    fun executeLinkToGoogle(data: Intent?, response: (Boolean, String) -> Unit)
    fun executeLinkCredentials(credential: AuthCredential, response: (Boolean) -> Unit)
}