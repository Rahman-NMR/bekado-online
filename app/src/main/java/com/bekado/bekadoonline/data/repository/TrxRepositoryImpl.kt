package com.bekado.bekadoonline.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bekado.bekadoonline.data.model.AkunModel
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

    private val _totalTransaksi = MutableLiveData<TrxCountModel?>()
    private val totalTransaksiData: LiveData<TrxCountModel?> get() = _totalTransaksi

    private var trxLstnrAdm: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            var totalAntrian = 0
            var totalProses = 0
            var totalSelesai = 0

            _isLoading.value = true
            for (userTransaksi in dataSnapshot.children) {
                for (item in userTransaksi.children) {
                    when (item.child("parentStatus").value.toString()) {
                        "antrian" -> totalAntrian += 1
                        "proses" -> totalProses += 1
                        "selesai" -> totalSelesai += 1
                    }
                }
            }

            returnValue(totalAntrian, totalProses, totalSelesai)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            errorValue()
        }
    }

    private var trxLstnrUsr: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            var totalAntrian = 0
            var totalProses = 0
            var totalSelesai = 0

            _isLoading.value = true
            for (item in dataSnapshot.children) {
                when (item.child("parentStatus").value.toString()) {
                    "antrian" -> totalAntrian += 1
                    "proses" -> totalProses += 1
                    "selesai" -> totalSelesai += 1
                }
            }

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
        _totalTransaksi.value = TrxCountModel(0, 0, 0)
        _isLoading.value = true
    }

    private fun trxRef(akunModel: AkunModel?): DatabaseReference {
        return if (akunModel?.statusAdmin == true) db.getReference("transaksi")
        else db.getReference("transaksi/${akunModel?.uid}")
    }

    override fun getLoading(): LiveData<Boolean> = isLoading

    override fun getTotalTransaksi(akunModel: AkunModel?): LiveData<TrxCountModel?> {
        if (akunModel?.statusAdmin == true) {
            trxRef(akunModel).removeEventListener(trxLstnrAdm)
            trxRef(akunModel).addValueEventListener(trxLstnrAdm)
        } else {
            trxRef(akunModel).removeEventListener(trxLstnrUsr)
            trxRef(akunModel).addValueEventListener(trxLstnrUsr)
        }
        return totalTransaksiData
    }

    override fun removeListener(akunModel: AkunModel?) {
        if (akunModel?.statusAdmin == true) trxRef(akunModel).removeEventListener(trxLstnrAdm)
        else trxRef(akunModel).removeEventListener(trxLstnrUsr)
    }
}