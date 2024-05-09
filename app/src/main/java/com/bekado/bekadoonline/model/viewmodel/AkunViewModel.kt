package com.bekado.bekadoonline.model.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.model.AkunModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AkunViewModel : ViewModel() {
    private val _currentUser = MutableLiveData<FirebaseUser?>(null)
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    private val _akunModel = MutableLiveData<AkunModel?>(null)
    val akunModel: LiveData<AkunModel?> = _akunModel

    private val _isExists = MutableLiveData<Boolean>()
    val isExists: LiveData<Boolean> = _isExists

    private lateinit var listener: ValueEventListener

    fun loadCurrentUser() {
        _currentUser.value = FirebaseAuth.getInstance().currentUser
    }

    fun loadAkunData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val database = FirebaseDatabase.getInstance()

        if (currentUser != null) {
            val userId = currentUser.uid

            val akunRef = database.getReference("akun/$userId")
            listener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val akunModel = dataSnapshot.getValue(AkunModel::class.java)
                        _akunModel.value = akunModel
                        _isExists.value = true
                    } else {
                        _akunModel.value = null
                        _isExists.value = false
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    _akunModel.value = null
                }
            }

            akunRef.removeEventListener(listener)
            akunRef.addValueEventListener(listener)
        }
    }

    fun removeListener(akunRef: DatabaseReference) {
        akunRef.removeEventListener(listener)
    }
}