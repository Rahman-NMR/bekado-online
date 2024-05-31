package com.bekado.bekadoonline.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.model.AlamatModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AlamatViewModel : ViewModel() {
    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> get() = _currentUser

    private val _alamatModel = MutableLiveData<AlamatModel?>()
    val alamatModel: LiveData<AlamatModel?> get() = _alamatModel

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

    fun loadCurrentUser() {
        _currentUser.value = FirebaseAuth.getInstance().currentUser
    }

    fun loadAlamatData() {
        val currentUser = currentUser.value
        val database = FirebaseDatabase.getInstance()

        if (currentUser != null) {
            val userId = currentUser.uid
            _isLoading.value = true

            val alamatRef = database.getReference("alamat/$userId")
            alamatRef.removeEventListener(alamatListener)
            alamatRef.addValueEventListener(alamatListener)
        }
    }

    fun removeAlamatListener(alamatRef: DatabaseReference) {
        alamatRef.removeEventListener(alamatListener)
    }
}