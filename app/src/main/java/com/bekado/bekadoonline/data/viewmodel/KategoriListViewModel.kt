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

class KategoriListViewModel : ViewModel() {
    private val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("produk")

    private val _kategoriList = MutableLiveData<ArrayList<KategoriModel>?>(null)
    val kategoriList: LiveData<ArrayList<KategoriModel>?> = _kategoriList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val kategoriListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val kategoriList = ArrayList<KategoriModel>()

            _isLoading.value = true
            if (snapshot.exists()) {
                for (item in snapshot.child("kategori").children) {
                    val idKategori = item.child("idKategori").value as String
                    val namaKategori = item.child("namaKategori").value as String
                    val posisi = item.child("posisi").value as Long
                    val visibilitas = item.child("visibilitas").value as Boolean

                    var jumlahProdukNya = 0
                    var hidden = 0
                    for (data in snapshot.child("produk").children) {
                        val jumlahProduk = data.child("idKategori").value.toString()
                        val visibility = data.child("visibility").value as? Boolean ?: false
                        if (jumlahProduk == idKategori) jumlahProdukNya++
                        if (jumlahProduk == idKategori && !visibility) hidden++
                    }
                    val kategoriData = KategoriModel(idKategori, namaKategori, posisi, visibilitas, jumlahProdukNya.toLong(), hidden.toLong())
                    kategoriList.add(kategoriData)
                }

                kategoriList.sortBy { it.posisi }
                _kategoriList.value = kategoriList
                _isLoading.value = false
            } else {
                _kategoriList.value = null
                _isLoading.value = false
            }
        }

        override fun onCancelled(error: DatabaseError) {
            _kategoriList.value = null
            _isLoading.value = true
        }
    }

    init {
        ref.addValueEventListener(kategoriListener)
    }

    override fun onCleared() {
        super.onCleared()
        ref.removeEventListener(kategoriListener)
    }
}