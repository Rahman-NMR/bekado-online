package com.bekado.bekadoonline.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class TransaksiViewModel : ViewModel() {
    private val _totalAntrian = MutableLiveData(0)
    val totalAntrian: LiveData<Int?> = _totalAntrian

    private val _totalProses = MutableLiveData(0)
    val totalProses: LiveData<Int?> = _totalProses

    private val _totalSelesai = MutableLiveData(0)
    val totalSelesai: LiveData<Int?> = _totalSelesai

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var trxLstnrAdm: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            var totalAntrian = 0
            var totalProses = 0
            var totalSelesai = 0

            _isLoading.value = true
            for (userTransaksi in dataSnapshot.children) {
                for (item in userTransaksi.children) {
                    when (item.child("parentStatus").value.toString()) {
                        "antrian" -> _totalAntrian.value = totalAntrian++
                        "proses" -> _totalProses.value = totalProses++
                        "selesai" -> _totalSelesai.value = totalSelesai++
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
                    "antrian" -> _totalAntrian.value = totalAntrian++
                    "proses" -> _totalProses.value = totalProses++
                    "selesai" -> _totalSelesai.value = totalSelesai++
                }
            }

            returnValue(totalAntrian, totalProses, totalSelesai)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            errorValue()
        }
    }

    private fun returnValue(totalAntrian: Int, totalProses: Int, totalSelesai: Int) {
        _totalAntrian.value = totalAntrian
        _totalProses.value = totalProses
        _totalSelesai.value = totalSelesai
        _isLoading.value = false
    }

    private fun errorValue() {
        _totalAntrian.value = 0
        _totalProses.value = 0
        _totalSelesai.value = 0
        _isLoading.value = true
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