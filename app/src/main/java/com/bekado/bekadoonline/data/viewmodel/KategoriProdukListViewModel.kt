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

class KategoriProdukListViewModel : ViewModel() {
    private val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("produk/produk")
    private var produkListener: ValueEventListener? = null

    private val _produkList = MutableLiveData<ArrayList<ProdukModel>?>()
    val produkList: LiveData<ArrayList<ProdukModel>?> get() = _produkList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun loadKategoriProduk(kategoriId: String?) {
        produkListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _isLoading.value = true
                val listProduk = ArrayList<ProdukModel>()

                if (snapshot.exists()) {
                    for (item in snapshot.children) {
                        val idKategori = item.child("idKategori").value as String
                        if (kategoriId == idKategori) {
                            val produk = item.getValue(ProdukModel::class.java) ?: ProdukModel()
                            listProduk.add(produk)
                        }
                    }

                    _produkList.value = listProduk
                    _isLoading.value = false
                } else {
                    _produkList.value = null
                    _isLoading.value = false
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _produkList.value = null
                _isLoading.value = true
            }
        }

        produkListener?.let { ref.orderByChild("namaProduk").addValueEventListener(it) }
    }

    override fun onCleared() {
        super.onCleared()
        produkListener?.let { ref.orderByChild("namaProduk").removeEventListener(it) }
    }
}