package com.bekado.bekadoonline.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.model.KategoriModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class KategoriSingleViewModel : ViewModel() {
    private val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("produk/kategori")
    private var idKategori: String? = null

    private val _kategoriModel = MutableLiveData<KategoriModel?>()
    val kategoriModel: LiveData<KategoriModel?> get() = _kategoriModel

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private var kategoriListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.exists()) {
                val kategori = dataSnapshot.getValue(KategoriModel::class.java)
                _kategoriModel.value = kategori
                _isLoading.value = false
            } else {
                _kategoriModel.value = null
                _isLoading.value = false
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            _kategoriModel.value = null
            _isLoading.value = true
        }
    }

    fun loadKategoriProduk(kategoriId: String) {
        idKategori = kategoriId
        ref.child(kategoriId).addValueEventListener(kategoriListener)
    }

    override fun onCleared() {
        super.onCleared()
        idKategori?.let { ref.child(it).removeEventListener(kategoriListener) }
    }
}