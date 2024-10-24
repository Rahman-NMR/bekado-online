package com.bekado.bekadoonline.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.domain.repositories.AkunRepository
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AkunRepositoryImpl(
    private val auth: FirebaseAuth,
    private val db: FirebaseDatabase,
    private val gsiClient: GoogleSignInClient
) : AkunRepository {
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

    override fun getAkun(): LiveData<AkunModel?> {
        val akunRef = db.getReference("akun/${getCurrentUser()?.uid}")
        akunRef.removeEventListener(akunListener)
        akunRef.addValueEventListener(akunListener)
        return akunModel
    }

    override fun getLoading(): LiveData<Boolean> = isLoading

    override fun logoutAkun() {
        _akunModel.value = null
        auth.signOut()
        gsiClient.signOut()
    }

    override fun removeListener() {
        val akunRef = db.getReference("akun/${getCurrentUser()?.uid}")
        akunRef.removeEventListener(akunListener)
    }
}