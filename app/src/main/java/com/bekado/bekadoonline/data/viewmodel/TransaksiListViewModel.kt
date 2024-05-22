package com.bekado.bekadoonline.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.model.TransaksiModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class TransaksiListViewModel : ViewModel() {
    private val _transaksiModel = MutableLiveData<ArrayList<TransaksiModel>?>(null)
    val transaksiModel: LiveData<ArrayList<TransaksiModel>?> = _transaksiModel

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var trxLstnrAdm: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val dataTrx = ArrayList<TransaksiModel>()

            _isLoading.value = true
            for (userTransaksi in dataSnapshot.children) {
                for (item in userTransaksi.children) {
                    getDataTransaksi(item, dataTrx)
                }
            }

            dataTrx.sortByDescending { it.timestamp }
            _transaksiModel.value = dataTrx
            _isLoading.value = false
        }

        override fun onCancelled(databaseError: DatabaseError) {
            _transaksiModel.value = null
            _isLoading.value = true
        }
    }

    private var trxLstnrUsr: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val dataTrx = ArrayList<TransaksiModel>()

            _isLoading.value = true
            for (item in dataSnapshot.children) {
                getDataTransaksi(item, dataTrx)
            }

            dataTrx.sortByDescending { it.timestamp }
            _transaksiModel.value = dataTrx
            _isLoading.value = false
        }

        override fun onCancelled(databaseError: DatabaseError) {
            _transaksiModel.value = null
            _isLoading.value = true
        }
    }

    private fun getDataTransaksi(item: DataSnapshot, dataTransaksi: ArrayList<TransaksiModel>) {
        val idTrx = item.child("idTransaksi").value.toString()
        val noPsnn = item.child("noPesanan").value.toString()
        val timestp = item.child("timestamp").value.toString()
        val sttsPsnn = item.child("statusPesanan").value.toString()
        val currency = item.child("currency").value.toString()
        val totalBlnj = item.child("totalBelanja").value as Long
        val produkList = item.child("produkList").children.sortedBy { it.child("timestamp").value.toString() }.toList()
        val lastProduk = produkList[0]
        val fotoPrdk = lastProduk.child("fotoProduk").value.toString()
        val namaPrdk = lastProduk.child("namaProduk").value.toString()
        val jmlhPrdk = lastProduk.child("jumlahProduk").value as Long
        val lainnya = item.child("totalItem").value as Long
        val transaksi = TransaksiModel(idTrx, noPsnn, timestp, sttsPsnn, fotoPrdk, namaPrdk, jmlhPrdk, currency, totalBlnj, lainnya)

        dataTransaksi.add(transaksi)
    }

    fun loadTransaksiData(transaksiRef: DatabaseReference, isAdmin: Boolean) {
        if (isAdmin) {
            transaksiRef.removeEventListener(trxLstnrAdm)
            transaksiRef.addValueEventListener(trxLstnrAdm)
        } else {
            transaksiRef.removeEventListener(trxLstnrUsr)
            transaksiRef.addValueEventListener(trxLstnrUsr)
        }
    }

    fun removeTransaksiListener(transaksiRef: DatabaseReference, isAdmin: Boolean) {
        if (isAdmin) transaksiRef.removeEventListener(trxLstnrAdm)
        else transaksiRef.removeEventListener(trxLstnrUsr)
    }
}