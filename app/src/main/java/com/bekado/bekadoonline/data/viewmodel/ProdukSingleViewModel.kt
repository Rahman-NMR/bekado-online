package com.bekado.bekadoonline.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.model.ProdukModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProdukSingleViewModel : ViewModel() {
    private val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("produk/produk")
    private var idProduk: String? = null

    private val _produkModel = MutableLiveData<ProdukModel?>()
    val produkModel: LiveData<ProdukModel?> get() = _produkModel

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private var produkListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.exists()) {
                val produk = dataSnapshot.getValue(ProdukModel::class.java)
                _produkModel.value = produk
                _isLoading.value = false
            } else {
                _produkModel.value = null
                _isLoading.value = false
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            _produkModel.value = null
            _isLoading.value = true
        }
    }

    fun loadProdukProduk(produkId: String) {
        if (produkId.isNotEmpty()) {
            idProduk = produkId
            ref.child(produkId).addValueEventListener(produkListener)
        } else {
            _produkModel.value = null
            _isLoading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        idProduk?.let { ref.child(it).removeEventListener(produkListener) }
    }
}