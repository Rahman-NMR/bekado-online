package com.bekado.bekadoonline.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.domain.repositories.ProdukListRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList

class ProdukListRepositoryImpl(db: FirebaseDatabase) : ProdukListRepository {
    private val reference = db.getReference("produk")

    private val _isLoading = MutableLiveData<Boolean>()
    private val isLoading: LiveData<Boolean> get() = _isLoading

    private val _produkList = MutableLiveData<ArrayList<ProdukModel>?>()
    private val produkList: LiveData<ArrayList<ProdukModel>?> get() = _produkList

    private fun produkListener(kategoriId: String?) = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            _isLoading.value = true
            val listProduk = ArrayList<ProdukModel>()

            if (snapshot.exists()) {
                for (item in snapshot.children) {
                    val idKategori = item.child("idKategori").value as? String ?: ""
                    if (kategoriId == idKategori) {
                        val produk = item.getValue(ProdukModel::class.java) ?: ProdukModel()
                        listProduk.add(produk)
                    }
                }

                val produkHidden = listProduk.filter { !it.visibility }.size
                val totalProduk = listProduk.size
                if (totalProduk == 0 || produkHidden == totalProduk)
                    if (!kategoriId.isNullOrEmpty()) reference.child("kategori/$kategoriId/visibilitas").setValue(false)

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

    override fun getLoading(): LiveData<Boolean> = isLoading

    override fun getDataProdukList(idKategori: String?): LiveData<ArrayList<ProdukModel>?> {
        reference.child("produk").orderByChild("namaProduk").addValueEventListener(produkListener(idKategori))
        return produkList
    }

    override fun updateVisibilityProduk(idProduk: String?, visibility: Boolean, response: (Boolean) -> Unit) {
        if (!idProduk.isNullOrEmpty()) {
            reference.child("produk/$idProduk/visibility").setValue(visibility)
                .addOnCompleteListener { response.invoke(it.isSuccessful) }
                .addOnFailureListener { response.invoke(false) }
        } else response.invoke(false)
    }

    override fun removeListenerProdukList(idKategori: String?) {
        reference.child("produk").orderByChild("namaProduk").removeEventListener(produkListener(idKategori))
    }
}