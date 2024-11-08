package com.bekado.bekadoonline.data.repository

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.model.VerificationResult
import com.bekado.bekadoonline.domain.repositories.UserRepository
import com.bekado.bekadoonline.helper.constval.VariableConstant.Companion.RES_MISCAST
import com.bekado.bekadoonline.helper.constval.VariableConstant.Companion.RES_DIFFRNT
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserRepositoryImpl(
    private val auth: FirebaseAuth,
    private val db: FirebaseDatabase,
    private val gsiClient: GoogleSignInClient
) : UserRepository {
    private fun getCurrentUser(): FirebaseUser? = auth.currentUser

    private val _akunModel = MutableLiveData<AkunModel?>()
    val akunModel: LiveData<AkunModel?> get() = _akunModel

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private var akunListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                val akun = snapshot.getValue(AkunModel::class.java)
                _akunModel.value = akun
                _isLoading.value = false
            } else {
                _akunModel.value = null
                _isLoading.value = false
            }
        }

        override fun onCancelled(error: DatabaseError) {
            _akunModel.value = null
            _isLoading.value = true
        }
    }

    init {
        val akunRef = db.getReference("akun/${getCurrentUser()?.uid}")
        akunRef.removeEventListener(akunListener)
        akunRef.addValueEventListener(akunListener)
    }

    override fun getAuthCurrentUser(): FirebaseUser? = getCurrentUser()
    override fun getAkun(): LiveData<AkunModel?> = akunModel
    override fun getLoading(): LiveData<Boolean> = isLoading

    override fun isVerified(): VerificationResult {
        val googleVerified = getCurrentUser()?.providerData?.any { it.providerId == GoogleAuthProvider.PROVIDER_ID } ?: false
        val emailVerified = getCurrentUser()?.providerData?.any { it.providerId == EmailAuthProvider.PROVIDER_ID } ?: false
        return VerificationResult(googleVerified, emailVerified)
    }

    override fun logoutAkun() {
        _akunModel.value = null
        auth.signOut()
        gsiClient.signOut()
    }

    override fun removeListener() {
        val akunRef = db.getReference("akun/${getCurrentUser()?.uid}")
        akunRef.removeEventListener(akunListener)
    }

    override fun loginManual(email: String, password: String, response: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) response.invoke(true)
                else response.invoke(false)
            }.addOnFailureListener { response.invoke(false) }
    }

    override fun loginGoogle(idToken: String?, response: (Boolean) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) response.invoke(true)
                else response.invoke(false)
            }.addOnFailureListener { response.invoke(false) }
    }

    override fun intentGoogleSignIn(): Intent = gsiClient.signInIntent

    override fun registerAuth(email: String, password: String, nama: String, noHp: String, response: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) registerAkunToRtdb(email, nama, noHp, "", response)
                else response.invoke(false)
            }.addOnFailureListener { response.invoke(false) }
    }

    override fun autoRegisterUserToRtdb(response: (Boolean) -> Unit) {
        getCurrentUser()?.reload()?.addOnCompleteListener {
            val email = getCurrentUser()?.email ?: ""
            val fotoProfil = getCurrentUser()?.photoUrl ?: ""
            val nama = getCurrentUser()?.displayName ?: ""
            val noHp = getCurrentUser()?.phoneNumber ?: ""
            registerAkunToRtdb(email, nama, noHp, fotoProfil.toString(), response)
        }?.addOnFailureListener { logoutAkun() }
    }

    private fun registerAkunToRtdb(email: String, nama: String, noHp: String, fotoProfil: String, response: (Boolean) -> Unit) {
        getCurrentUser()?.let { currentUser ->
            val userID = currentUser.uid
            val userRef = db.getReference("akun/$userID")

            val user = HashMap<String, Any>()
            user["email"] = email
            if (fotoProfil.isNotEmpty()) user["fotoProfil"] = fotoProfil
            if (nama.isNotEmpty()) user["nama"] = nama
            if (noHp.isNotEmpty()) user["noHp"] = noHp
            user["statusAdmin"] = false
            user["uid"] = userID

            userRef.setValue(user)
                .addOnCompleteListener { task -> response.invoke(task.isSuccessful) }
                .addOnFailureListener { response.invoke(false) }
        } ?: response.invoke(false)
    }

    override fun linkToGoogle(data: Intent?, response: (Boolean, String) -> Unit) {
        GoogleSignIn.getSignedInAccountFromIntent(data).addOnCompleteListener { task ->
            val googleAccount = task.result
            if (googleAccount != null) {
                val googleEmail = googleAccount.email
                val currentUserEmail = getCurrentUser()?.email

                if (googleEmail == currentUserEmail) {
                    googleAccount.idToken?.let { idToken -> response.invoke(true, idToken) }
                } else {
                    response.invoke(false, RES_DIFFRNT)
                    gsiClient.signOut()
                }
            } else response.invoke(false, RES_MISCAST)
        }.addOnFailureListener { response.invoke(false, RES_MISCAST) }
    }

    override fun linkCredentials(credential: AuthCredential, response: (Boolean) -> Unit) {
        auth.currentUser?.linkWithCredential(credential)
            ?.addOnCompleteListener {
                if (it.isSuccessful) response.invoke(true)
                else response.invoke(false)
            }?.addOnFailureListener { response.invoke(false) }
    }
}