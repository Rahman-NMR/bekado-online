package com.bekado.bekadoonline.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Repository(private val auth: FirebaseAuth, private val db: FirebaseDatabase, private val gsiClient: GoogleSignInClient) {
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

    fun loadAkunData() {
        val akunRef = db.getReference("akun/${getCurrentUser()?.uid}")
        akunRef.removeEventListener(akunListener)
        akunRef.addValueEventListener(akunListener)
    }

    fun logout() {
        _akunModel.value = null
        auth.signOut()
        gsiClient.signOut()
    }

/*
    private val _totalAntrian = MutableLiveData(0)
    val totalAntrian: LiveData<Int?> get() = _totalAntrian

    private val _totalProses = MutableLiveData(0)
    val totalProses: LiveData<Int?> get() = _totalProses

    private val _totalSelesai = MutableLiveData(0)
    val totalSelesai: LiveData<Int?> get() = _totalSelesai

    private var trxLstnrAdm: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            var totalAntrian = 0
            var totalProses = 0
            var totalSelesai = 0

            _isLoading.value = true
            for (userTransaksi in dataSnapshot.children) {
                for (item in userTransaksi.children) {
                    when (item.child("parentStatus").value.toString()) {
                        "antrian" -> _totalAntrian.value = totalAntrian++
                        "proses" -> _totalProses.value = totalProses++
                        "selesai" -> _totalSelesai.value = totalSelesai++
                    }
                }
            }

            returnValue(totalAntrian, totalProses, totalSelesai)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            errorValue()
        }
    }

    private var trxLstnrUsr: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            var totalAntrian = 0
            var totalProses = 0
            var totalSelesai = 0

            _isLoading.value = true
            for (item in dataSnapshot.children) {
                when (item.child("parentStatus").value.toString()) {
                    "antrian" -> _totalAntrian.value = totalAntrian++
                    "proses" -> _totalProses.value = totalProses++
                    "selesai" -> _totalSelesai.value = totalSelesai++
                }
            }

            returnValue(totalAntrian, totalProses, totalSelesai)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            errorValue()
        }
    }

    private fun returnValue(totalAntrian: Int, totalProses: Int, totalSelesai: Int) {
        _totalAntrian.value = totalAntrian
        _totalProses.value = totalProses
        _totalSelesai.value = totalSelesai
        _isLoading.value = false
    }

    private fun errorValue() {
        _totalAntrian.value = 0
        _totalProses.value = 0
        _totalSelesai.value = 0
        _isLoading.value = true
    }

    fun getDataTransaksi() {
        val refAdmin = akunModel.value.let {
            when {
                it.statusAdmin -> "transaksi"
                else -> "transaksi/${it.uid}"
            }
        } ?: "transaksi"
        val transaksiRef = db.getReference(refAdmin)

        if (isAdmin()) {
            transaksiRef.removeEventListener(trxLstnrAdm)
            transaksiRef.addValueEventListener(trxLstnrAdm)
        } else {
            transaksiRef.removeEventListener(trxLstnrUsr)
            transaksiRef.addValueEventListener(trxLstnrUsr)
        }
    }*/

    companion object {
        fun getInstance(
            firebaseAuth: FirebaseAuth,
            firebaseDatabase: FirebaseDatabase,
            googleSignInClient: GoogleSignInClient
        ) = Repository(firebaseAuth, firebaseDatabase, googleSignInClient)
    }
}