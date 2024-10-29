package com.bekado.bekadoonline.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.AlamatModel
import com.bekado.bekadoonline.domain.repositories.AddressRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddressRepositoryImpl(private val auth: FirebaseAuth, private val db: FirebaseDatabase) : AddressRepository {
    private fun getCurrentUser(): FirebaseUser? = auth.currentUser

    private val _alamatModel = MutableLiveData<AlamatModel?>()
    private val alamatModel: LiveData<AlamatModel?> get() = _alamatModel

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private var alamatListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.exists()) {
                val alamatModel = dataSnapshot.getValue(AlamatModel::class.java)
                _alamatModel.value = alamatModel
                _isLoading.value = false
            } else {
                _alamatModel.value = null
                _isLoading.value = false
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            _alamatModel.value = null
            _isLoading.value = true
        }
    }

    init {
        val alamatRef = db.getReference("alamat/${getCurrentUser()?.uid}")
        alamatRef.removeEventListener(alamatListener)
        alamatRef.addValueEventListener(alamatListener)
    }

    override fun getLoading(): LiveData<Boolean> = isLoading
    override fun getDataAlamat(): LiveData<AlamatModel?> = alamatModel

    override fun removeListener() {
        val alamatRef = db.getReference("alamat/${getCurrentUser()?.uid}")
        alamatRef.removeEventListener(alamatListener)
    }
}