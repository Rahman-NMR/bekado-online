package com.bekado.bekadoonline.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.model.CombinedKeranjangModel
import com.bekado.bekadoonline.data.model.KeranjangModel
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.helper.HelperProduk
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class KeranjangViewModel : ViewModel() {
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()

    private val _keranjangModel = MutableLiveData<ArrayList<CombinedKeranjangModel>?>(null)
    val keranjangModel: LiveData<ArrayList<CombinedKeranjangModel>?> = _keranjangModel

    private val _keranjangModelHide = MutableLiveData<ArrayList<CombinedKeranjangModel>?>(null)
    val keranjangModelHide: LiveData<ArrayList<CombinedKeranjangModel>?> = _keranjangModelHide

    private val produkListeners = mutableMapOf<String, ValueEventListener>()
    private val kategoriListeners = mutableMapOf<String, ValueEventListener>()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var keranjangListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(keranjangSnapshot: DataSnapshot) {
            val keranjangItem = arrayListOf<CombinedKeranjangModel>()
            val keranjangItemHide = arrayListOf<CombinedKeranjangModel>()

            _isLoading.value = true
            if (keranjangSnapshot.exists()) {
                for (keranjangList in keranjangSnapshot.children) {
                    val idProduk = keranjangList.child("idProduk").getValue(String::class.java)

                    if (idProduk != null) {
                        val keranjangModel = KeranjangModel(
                            diPilih = keranjangList.child("diPilih").value as? Boolean ?: false,
                            jumlahProduk = keranjangList.child("jumlahProduk").value as? Long ?: 0,
                            timestamp = keranjangList.child("timestamp").value as? String ?: ""
                        )

                        checkProdukVisibility(idProduk, keranjangModel, keranjangItem, keranjangItemHide)
                    }
                }
            } else {
                _keranjangModel.value = null
                _keranjangModelHide.value = null
                _isLoading.value = false
            }
        }

        override fun onCancelled(error: DatabaseError) {
            _keranjangModel.value = null
            _keranjangModelHide.value = null
            _isLoading.value = true
        }
    }

    fun loadKeranjangData(uid: String) {
        val keranjangRef = db.getReference("keranjang/$uid")
        keranjangRef.orderByChild("timestamp").addValueEventListener(keranjangListener)
    }

    fun clearKeranjangListeners(uid: String) {
//        val iterator = produkListeners.iterator()
//        while (iterator.hasNext()) {
//            val key = iterator.next().key
//            val listener = iterator.next().value
//            db.getReference("produk/produk/$key").removeEventListener(listener)
//        }
//        produkListeners.clear()

        val keranjangRef = db.getReference("keranjang/$uid")
        keranjangRef.orderByChild("timestamp").removeEventListener(keranjangListener)

        produkListeners.forEach { (idProduk, listener) ->
            val produkRef = db.getReference("produk/produk/$idProduk")
            produkRef.removeEventListener(listener)
        }
        produkListeners.clear()

        kategoriListeners.forEach { (idKategori, listener) ->
            val kategoriRef = db.getReference("produk/kategori/$idKategori")
            kategoriRef.removeEventListener(listener)
        }
        kategoriListeners.clear()
    }

    private fun checkProdukVisibility(
        idProduk: String,
        keranjangModel: KeranjangModel,
        keranjangItem: ArrayList<CombinedKeranjangModel>,
        keranjangItemHide: ArrayList<CombinedKeranjangModel>
    ) {
        val produkRef = db.getReference("produk/produk/$idProduk")

        val produkListener = object : ValueEventListener {
            override fun onDataChange(produkSnapshot: DataSnapshot) {
                val idKategori = produkSnapshot.child("idKategori").getValue(String::class.java) ?: return
                val visibility = produkSnapshot.child("visibility").getValue(Boolean::class.java) ?: return

                val produkModel = ProdukModel(
                    fotoProduk = produkSnapshot.child("fotoProduk").getValue(String::class.java) ?: "",
                    namaProduk = produkSnapshot.child("namaProduk").getValue(String::class.java) ?: "",
                    currency = produkSnapshot.child("currency").getValue(String::class.java) ?: "",
                    hargaProduk = produkSnapshot.child("hargaProduk").getValue(Long::class.java) ?: 0,
                    visibility = visibility,
                    idProduk = idProduk,
                    idKategori = idKategori
                )

                if (visibility) checkKategoriVisibility(idKategori, produkModel, keranjangModel, keranjangItem, keranjangItemHide)
                else updateProdukList(produkModel, keranjangModel, keranjangItem, keranjangItemHide, false)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        produkRef.addValueEventListener(produkListener)
        produkListeners[idProduk] = produkListener
    }

    private fun checkKategoriVisibility(
        idKategori: String,
        produkModel: ProdukModel,
        keranjangModel: KeranjangModel,
        keranjangItem: ArrayList<CombinedKeranjangModel>,
        keranjangItemHide: ArrayList<CombinedKeranjangModel>
    ) {
        val kategoriRef = db.getReference("produk/kategori/$idKategori")

        val kategoriListener = object : ValueEventListener {
            override fun onDataChange(kategoriSnapshot: DataSnapshot) {
                val visibilitas = kategoriSnapshot.child("visibilitas").getValue(Boolean::class.java) ?: return

                if (visibilitas) updateProdukList(produkModel, keranjangModel, keranjangItem, keranjangItemHide, true)
                else updateProdukList(produkModel, keranjangModel, keranjangItem, keranjangItemHide, false)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        kategoriRef.addValueEventListener(kategoriListener)
        kategoriListeners[idKategori] = kategoriListener
    }

    private fun updateProdukList(
        produkModel: ProdukModel,
        keranjangModel: KeranjangModel,
        keranjangItem: ArrayList<CombinedKeranjangModel>,
        keranjangItemHide: ArrayList<CombinedKeranjangModel>,
        isVisible: Boolean
    ) {
        keranjangItem.removeAll { it.produkModel?.idProduk == produkModel.idProduk }
        keranjangItemHide.removeAll { it.produkModel?.idProduk == produkModel.idProduk }

        if (isVisible) keranjangItem.add(CombinedKeranjangModel(produkModel, keranjangModel))
        else keranjangItemHide.add(CombinedKeranjangModel(produkModel, null))

        _keranjangModel.value = keranjangItem
        _keranjangModelHide.value = keranjangItemHide
        _isLoading.value = false
    }

    fun updateCheckedItem(keranjangRef: DatabaseReference, isChecked: Boolean) {
        keranjangRef.child("diPilih").setValue(isChecked)
    }

    fun updateItemCount(keranjangRef: DatabaseReference, isPlus: Boolean) {
        HelperProduk.plusMinus(keranjangRef, isPlus)
    }
}