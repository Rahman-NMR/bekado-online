package com.bekado.bekadoonline.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.ButtonModel
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.domain.repositories.ProductRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList

class ProductRepositoryImpl(db: FirebaseDatabase) : ProductRepository {
    private val produkRef = db.getReference("produk")

    private val _dataProduk = MutableLiveData<ArrayList<ProdukModel>?>()
    private val dataProduk: LiveData<ArrayList<ProdukModel>?> get() = _dataProduk

    private val _dataButton = MutableLiveData<ArrayList<ButtonModel>?>()
    private val dataButton: LiveData<ArrayList<ButtonModel>?> get() = _dataButton

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val produkListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val dataProdukList = ArrayList<ProdukModel>()
            val dataButtonList = ArrayList<ButtonModel>()
            val visibilityKategori = mutableMapOf<String, Boolean>()

            dataButtonList.add(ButtonModel("Semua", "", true, 0))
            for (item in snapshot.child("kategori").children) {
                val namaKategori = item.child("namaKategori").value as? String ?: "Kosong"
                val posisi = item.child("posisi").value as? Long ?: 9999
                val idKategori = item.child("idKategori").value as? String ?: "Kosong"
                val visibilitas = item.child("visibilitas").value as? Boolean ?: false

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

    override fun getLoading(): LiveData<Boolean> = isLoading
    override fun getAllDataProduk(): LiveData<ArrayList<ProdukModel>?> = dataProduk
    override fun filterByKategori(): LiveData<ArrayList<ButtonModel>?> = dataButton

    override fun removeListener() {
        produkRef.removeEventListener(produkListener)
    }
}