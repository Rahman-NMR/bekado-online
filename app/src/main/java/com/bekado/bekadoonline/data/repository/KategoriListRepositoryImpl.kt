package com.bekado.bekadoonline.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.KategoriModel
import com.bekado.bekadoonline.domain.repositories.KategoriListRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList

class KategoriListRepositoryImpl(db: FirebaseDatabase) : KategoriListRepository {
    private val reference = db.getReference("produk")

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _kategoriList = MutableLiveData<ArrayList<KategoriModel>?>()
    private val kategoriList: LiveData<ArrayList<KategoriModel>?> = _kategoriList

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
        reference.addValueEventListener(kategoriListener)
    }

    override fun getLoading(): LiveData<Boolean> = isLoading
    override fun getDataKategoriList(): LiveData<ArrayList<KategoriModel>?> = kategoriList

    override fun addNewKategori(namaKategori: String, posisi: Long, response: (Boolean) -> Unit) {
        val kategoriRef = reference.child("kategori")
        val idKategori = kategoriRef.push().key.toString()
        val kategoriMap = HashMap<String, Any>(
            mapOf(
                "idKategori" to idKategori,
                "namaKategori" to namaKategori,
                "posisi" to posisi,
                "visibilitas" to false
            )
        )
        kategoriRef.child(idKategori).setValue(kategoriMap)
            .addOnCompleteListener { response.invoke(it.isSuccessful) }
            .addOnFailureListener { response.invoke(false) }
    }

    override fun updateNamaKategori(idKategori: String?, namaKategori: String, response: (Boolean) -> Unit) {
        if (!idKategori.isNullOrEmpty()) {
            reference.child("kategori/$idKategori/namaKategori").setValue(namaKategori)
                .addOnCompleteListener { response.invoke(it.isSuccessful) }
                .addOnFailureListener { response.invoke(false) }
        } else response.invoke(false)
    }

    override fun updateVisibilitasKategori(idKategori: String?, visibilitas: Boolean, response: (Boolean) -> Unit) {
        if (!idKategori.isNullOrEmpty()) {
            reference.child("kategori/$idKategori/visibilitas").setValue(visibilitas)
                .addOnCompleteListener { response.invoke(it.isSuccessful) }
                .addOnFailureListener { response.invoke(false) }
        } else response.invoke(false)
    }

    override fun removeListenerKategoriList() {
        reference.removeEventListener(kategoriListener)
    }
}