package com.bekado.bekadoonline.model.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.model.CombinedKeranjangModel
import com.bekado.bekadoonline.model.ProdukModel
import com.bekado.bekadoonline.model.KeranjangModel
import com.google.firebase.database.DatabaseReference

class DaftarProdukTransaksiViewModel : ViewModel() {
    private val _dataProduk = MutableLiveData<ArrayList<CombinedKeranjangModel>?>(null)
    val dataProduk: LiveData<ArrayList<CombinedKeranjangModel>?> = _dataProduk

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadDaftarProduk(produkRef: DatabaseReference, statusAdmin: Boolean, idTransaksi: String?) {
        produkRef.get().addOnSuccessListener { snapshot ->
            val produkData = ArrayList<CombinedKeranjangModel>()
            _isLoading.value = true

            if (statusAdmin) {
                for (data in snapshot.children) {
                    for (item in data.child("$idTransaksi/produkList").children) {
                        val produk = item.getValue(ProdukModel::class.java)
                        val keranjang = item.getValue(KeranjangModel::class.java)
                        produkData.add(CombinedKeranjangModel(produk, keranjang))
                    }
                }

                _dataProduk.value = produkData
                _isLoading.value = false
            } else {
                for (item in snapshot.child("$idTransaksi/produkList").children) {
                    val produk = item.getValue(ProdukModel::class.java)
                    val keranjang = item.getValue(KeranjangModel::class.java)
                    produkData.add(CombinedKeranjangModel(produk, keranjang))
                }

                _dataProduk.value = produkData
                _isLoading.value = false
            }
        }
    }
}