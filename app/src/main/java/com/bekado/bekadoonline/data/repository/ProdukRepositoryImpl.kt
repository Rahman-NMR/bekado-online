package com.bekado.bekadoonline.data.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.domain.repositories.ProdukRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProdukRepositoryImpl(db: FirebaseDatabase, private val storage: FirebaseStorage) : ProdukRepository {
    private val produkRef = db.getReference("produk/produk")

    private val _isLoading = MutableLiveData<Boolean>()
    private val isLoading: LiveData<Boolean> get() = _isLoading

    private val _produkModel = MutableLiveData<ProdukModel?>()
    private val produkModel: LiveData<ProdukModel?> get() = _produkModel

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

    override fun getLoading(): LiveData<Boolean> = isLoading

    override fun getDataProduk(idProduk: String?): LiveData<ProdukModel?> {
        if (!idProduk.isNullOrEmpty()) produkRef.child("$idProduk").addValueEventListener(produkListener)
        else {
            _produkModel.value = null
            _isLoading.value = false
        }
        return produkModel
    }

    private fun uploadImage(idProduk: String?, imageUri: Uri) {
        val storageReference = storage.getReference("produk/$idProduk.png")

        storageReference.putFile(imageUri).addOnSuccessListener {
            storageReference.downloadUrl.addOnCompleteListener { task ->
                val imgLink = task.result.toString()
                produkRef.child("$idProduk/fotoProduk").setValue(imgLink)
            }
        }
    }

    override fun updateDetailProduk(
        isEdit: Boolean,
        imageUri: Uri?,
        idProduk: String?,
        idKategori: String?,
        namaProduk: String,
        hargaProduk: Long,
        response: (Boolean) -> Unit
    ) {
        _isLoading.value = true
        if (isEdit && idProduk.isNullOrEmpty()) {
            response.invoke(false)
            _isLoading.value = false
            return
        }

        val keyProduk = if (isEdit) idProduk else produkRef.push().key

        val produkMap = HashMap<String, Any>()
        produkMap["currency"] = "Rp"
        produkMap["hargaProduk"] = hargaProduk
        produkMap["idKategori"] = idKategori.toString()
        produkMap["idProduk"] = keyProduk.toString()
        produkMap["namaProduk"] = namaProduk

        if (!isEdit) produkMap["visibility"] = false
        if (imageUri != null && imageUri != Uri.parse("")) uploadImage(keyProduk, imageUri)

        val ref = produkRef.child(keyProduk.toString())
        val reference = when (isEdit) {
            true -> ref.updateChildren(produkMap)
            false -> ref.setValue(produkMap)
        }
        reference.addOnCompleteListener {
            response.invoke(it.isSuccessful)
            _isLoading.value = false
        }.addOnFailureListener {
            response.invoke(false)
            _isLoading.value = false
        }
    }

    override fun deleteProduk(idProduk: String?, response: (Boolean) -> Unit) {
        if (!idProduk.isNullOrEmpty()) {
            _isLoading.value = true
            val delete = storage.getReference("produk/$idProduk.png").delete()
            val removeValue = produkRef.child(idProduk).removeValue()

            Tasks.whenAll(delete, removeValue).addOnCompleteListener {
                response.invoke(it.isSuccessful)
                _isLoading.value = false
            }.addOnFailureListener {
                response.invoke(false)
                _isLoading.value = false
            }
        } else response.invoke(false)
    }

    override fun removeListenerProduk(idProduk: String?) {
        produkRef.child("$idProduk").removeEventListener(produkListener)
    }
}