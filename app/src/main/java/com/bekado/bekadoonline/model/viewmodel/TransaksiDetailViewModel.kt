package com.bekado.bekadoonline.model.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.model.AlamatModel
import com.bekado.bekadoonline.model.DetailTransaksiModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference

class TransaksiDetailViewModel : ViewModel() {
    private val _detailTransaksi = MutableLiveData<DetailTransaksiModel?>(null)
    val detailTransaksi: LiveData<DetailTransaksiModel?> = _detailTransaksi

    private val _uidClient = MutableLiveData<String?>()
    val uidClient: LiveData<String?> = _uidClient

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private fun getDataTransaksi(snapshot: DataSnapshot) {
        val dataAlamat = snapshot.child("alamatPenerima").getValue(AlamatModel::class.java)
        val idTrx = snapshot.child("idTransaksi").value as String
        val noPsnn = snapshot.child("noPesanan").value as String
        val tmeStmp = snapshot.child("timestamp").value as String
        val stspsnn = snapshot.child("statusPesanan").value as String
        val rp = snapshot.child("currency").value as String
        val ttlItm = snapshot.child("totalItem").value as Long
        val ttlHrg = snapshot.child("totalHarga").value as Long
        val onkr = snapshot.child("ongkir").value as Long
        val mtdPmbyrn = snapshot.child("metodePembayaran").value as String
        val ttlBlnj = snapshot.child("totalBelanja").value as Long
        val transaksi = DetailTransaksiModel(dataAlamat, idTrx, noPsnn, tmeStmp, stspsnn, rp, ttlItm, ttlHrg, onkr, mtdPmbyrn, ttlBlnj)

        _detailTransaksi.value = transaksi
        _isLoading.value = false
    }

    fun loadDetailTransaksi(transaksiRef: DatabaseReference, isAdmin: Boolean, idTransaksi: String?) {
        if (isAdmin) {
            transaksiRef.get().addOnSuccessListener { data ->
                _isLoading.value = true

                for (item in data.children) {
                    val snapshot = item.child("$idTransaksi")
                    if (snapshot.exists()) {
                        _uidClient.value = item.key
                        getDataTransaksi(snapshot)
                    } else {
                        _uidClient.value = null
                        _detailTransaksi.value = null
                        _isLoading.value = false
                    }
                }
            }.addOnFailureListener {
                _detailTransaksi.value = null
                _isLoading.value = false
            }
        } else {
            transaksiRef.get().addOnSuccessListener { data ->
                _isLoading.value = true

                getDataTransaksi(data)
            }.addOnFailureListener {
                _detailTransaksi.value = null
                _isLoading.value = false
            }
        }
    }
}