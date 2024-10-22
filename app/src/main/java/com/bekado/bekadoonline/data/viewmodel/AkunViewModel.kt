package com.bekado.bekadoonline.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.Repository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AkunViewModel(private val repository: Repository) : ViewModel() {
    private val _currentUser = MutableLiveData<FirebaseUser?>(null)
    val currentUser: LiveData<FirebaseUser?> get() = _currentUser

    private val _akunModel = MutableLiveData<AkunModel?>(null)
    val akunModel: LiveData<AkunModel?> get() = _akunModel

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private var akunListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.exists()) {
                val akunModel = dataSnapshot.getValue(AkunModel::class.java)
                _akunModel.value = akunModel
                _isLoading.value = false
            } else {
                _akunModel.value = null
                _isLoading.value = false
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            _akunModel.value = null
            _isLoading.value = true
        }
    }

    fun loadCurrentUser() {
        _currentUser.value = repository.getCurrentUser()
    }

    fun loadAkunData() {
        _isLoading.value = true

        val akunRef = repository.akunRef()
        akunRef.removeEventListener(akunListener)
        akunRef.addValueEventListener(akunListener)
    }

    fun clearAkunData() {
        repository.logout()
        _akunModel.value = null
        _currentUser.value = null
    }

    override fun onCleared() {
        super.onCleared()
        repository.akunRef().removeEventListener(akunListener)
    }
}