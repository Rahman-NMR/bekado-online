package com.bekado.bekadoonline.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.model.ButtonModel
import com.bekado.bekadoonline.data.model.ProdukModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BerandaViewModel : ViewModel() {
    private val produkRef = FirebaseDatabase.getInstance().getReference("produk")

    private val _dataProduk = MutableLiveData<ArrayList<ProdukModel>?>()
    val dataProduk: LiveData<ArrayList<ProdukModel>?> get() = _dataProduk

    private val _dataButton = MutableLiveData<ArrayList<ButtonModel>?>()
    val dataButton: LiveData<ArrayList<ButtonModel>?> get() = _dataButton

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val produkListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val dataProdukList = ArrayList<ProdukModel>()
            val dataButtonList = ArrayList<ButtonModel>()
            val visibilityKategori = mutableMapOf<String, Boolean>()

            dataButtonList.add(ButtonModel("Semua", "", true, 0))
            for (item in snapshot.child("kategori").children) {
                val namaKategori = item.child("namaKategori").value as String
                val posisi = item.child("posisi").value as Long
                val idKategori = item.child("idKategori").value as String
                val visibilitas = item.child("visibilitas").value as Boolean

                visibilityKategori[idKategori] = visibilitas
                if (visibilitas) {
                    dataButtonList.add(ButtonModel(namaKategori, idKategori, false, posisi))
                }
            }
            dataButtonList.sortBy { it.posisi }

            for (item in snapshot.child("produk").children) {
                val produk = item.getValue(ProdukModel::class.java)
                if (produk != null && produk.visibility) {
                    val kategoriVisible = visibilityKategori[produk.idKategori] ?: false
                    if (kategoriVisible) dataProdukList.add(produk)
                }
            }

            _dataProduk.value = dataProdukList
            _dataButton.value = dataButtonList
            _isLoading.value = false
        }

        override fun onCancelled(error: DatabaseError) {
            _dataProduk.value = null
            _dataButton.value = null
            _isLoading.value = false
        }
    }

    init {
        _isLoading.value = true
        produkRef.addValueEventListener(produkListener)
    }

    override fun onCleared() {
        super.onCleared()
        produkRef.removeEventListener(produkListener)
    }
}