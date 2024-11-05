package com.bekado.bekadoonline.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.KategoriModel
import com.bekado.bekadoonline.domain.repositories.KategoriRepository
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class KategoriRepositoryImpl(db: FirebaseDatabase, private val storage: FirebaseStorage) : KategoriRepository {
    private val reference = db.getReference("produk")

    private val _isLoading = MutableLiveData<Boolean>()
    private val isLoading: LiveData<Boolean> get() = _isLoading

    private val _kategoriModel = MutableLiveData<KategoriModel?>()
    private val kategoriModel: LiveData<KategoriModel?> get() = _kategoriModel

    private var kategoriListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            _isLoading.value = true

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

    override fun getLoading(): LiveData<Boolean> = isLoading

    override fun getDataKategori(idKategori: String?): LiveData<KategoriModel?> {
        reference.child("kategori/$idKategori").addValueEventListener(kategoriListener)
        return kategoriModel
    }

    override fun deleteKategori(idKategori: String?, response: (Boolean) -> Unit) {
        if (!idKategori.isNullOrEmpty()) {
            val idProdukList = ArrayList<String>()
            val deleteTasks = mutableListOf<Task<Void>>()

            _isLoading.value = true
            val deleteRef = reference.child("produk").orderByChild("idKategori").equalTo(idKategori)
            deleteRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (item in snapshot.children) {
                        val idProduk = item.child("idProduk").value.toString()
                        idProdukList.add(idProduk)
                        deleteTasks.add(item.ref.removeValue())
                    }
                    for (idProduk in idProdukList) {
                        val storageReference = storage.getReference("produk/$idProduk.png")
                        deleteTasks.add(storageReference.delete())
                    }

                    Tasks.whenAll(deleteTasks).addOnCompleteListener {
                        reference.child("kategori/$idKategori").removeValue()
                            .addOnCompleteListener {
                                response.invoke(it.isSuccessful)
                                _isLoading.value = false
                            }.addOnFailureListener {
                                response.invoke(false)
                                _isLoading.value = false
                            }
                    }.addOnFailureListener {
                        response.invoke(false)
                        _isLoading.value = false
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    response.invoke(false)
                    _isLoading.value = false
                }
            })
        } else response.invoke(false)
    }

    override fun removeListenerKategori(idKategori: String?) {
        reference.child("kategori/$idKategori").removeEventListener(kategoriListener)
    }
}