package com.bekado.bekadoonline.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.model.TransaksiModel
import com.bekado.bekadoonline.data.model.TrxCountModel
import com.bekado.bekadoonline.domain.repositories.TrxRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TrxRepositoryImpl(private val db: FirebaseDatabase) : TrxRepository {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _transaksiModel = MutableLiveData<ArrayList<TransaksiModel>?>()
    private val transaksiModel: LiveData<ArrayList<TransaksiModel>?> get() = _transaksiModel

    private val _totalTransaksi = MutableLiveData<TrxCountModel?>()
    private val totalTransaksiData: LiveData<TrxCountModel?> get() = _totalTransaksi

    private var trxLstnrAdm: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val dataTrx = ArrayList<TransaksiModel>()
            var totalAntrian = 0
            var totalProses = 0
            var totalSelesai = 0

            _isLoading.value = true
            for (userTransaksi in dataSnapshot.children) {
                for (item in userTransaksi.children) {
                    getDataTransaksi(item, dataTrx)
                    when (item.child("parentStatus").value.toString()) {
                        "antrian" -> totalAntrian += 1
                        "proses" -> totalProses += 1
                        "selesai" -> totalSelesai += 1
                    }
                }
            }

            dataTrx.sortByDescending { it.timestamp }
            _transaksiModel.value = dataTrx
            returnValue(totalAntrian, totalProses, totalSelesai)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            errorValue()
        }
    }

    private var trxLstnrUsr: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val dataTrx = ArrayList<TransaksiModel>()
            var totalAntrian = 0
            var totalProses = 0
            var totalSelesai = 0

            _isLoading.value = true
            for (item in dataSnapshot.children) {
                getDataTransaksi(item, dataTrx)
                when (item.child("parentStatus").value.toString()) {
                    "antrian" -> totalAntrian += 1
                    "proses" -> totalProses += 1
                    "selesai" -> totalSelesai += 1
                }
            }

            dataTrx.sortByDescending { it.timestamp }
            _transaksiModel.value = dataTrx
            returnValue(totalAntrian, totalProses, totalSelesai)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            errorValue()
        }
    }

    private fun returnValue(totalAntrian: Int, totalProses: Int, totalSelesai: Int) {
        _totalTransaksi.value = TrxCountModel(totalAntrian, totalProses, totalSelesai)
        _isLoading.value = false
    }

    private fun errorValue() {
        _transaksiModel.value = null
        _totalTransaksi.value = TrxCountModel(0, 0, 0)
        _isLoading.value = true
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

    private fun trxRef(akunModel: AkunModel?): DatabaseReference {
        return if (akunModel?.statusAdmin == true) db.getReference("transaksi")
        else db.getReference("transaksi/${akunModel?.uid}")
    }

    private fun startTransaksiListener(akunModel: AkunModel?) {
        if (akunModel?.statusAdmin == true) {
            trxRef(akunModel).removeEventListener(trxLstnrAdm)
            trxRef(akunModel).addValueEventListener(trxLstnrAdm)
        } else {
            trxRef(akunModel).removeEventListener(trxLstnrUsr)
            trxRef(akunModel).addValueEventListener(trxLstnrUsr)
        }
    }

    override fun getLoading(): LiveData<Boolean> = isLoading

    override fun getListDataTransaksi(akunModel: AkunModel?): LiveData<ArrayList<TransaksiModel>?> {
        startTransaksiListener(akunModel)
        return transaksiModel
    }

    override fun getTotalTransaksi(akunModel: AkunModel?): LiveData<TrxCountModel?> {
        startTransaksiListener(akunModel)
        return totalTransaksiData
    }

    override fun removeListener(akunModel: AkunModel?) {
        if (akunModel?.statusAdmin == true) trxRef(akunModel).removeEventListener(trxLstnrAdm)
        else trxRef(akunModel).removeEventListener(trxLstnrUsr)
    }
}