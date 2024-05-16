package com.bekado.bekadoonline.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.model.BuktiPembayaranModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class BuktiPembayaranViewModel : ViewModel() {
    private val _dataInvoice = MutableLiveData<BuktiPembayaranModel?>(null)
    val dataInvoice: LiveData<BuktiPembayaranModel?> = _dataInvoice

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var invoiceListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                val invoice = snapshot.getValue(BuktiPembayaranModel::class.java)
                _dataInvoice.value = invoice
                _isLoading.value = false
            } else {
                _dataInvoice.value = null
                _isLoading.value = false
            }
        }

        override fun onCancelled(error: DatabaseError) {
            _dataInvoice.value = null
            _isLoading.value = true
        }
    }

    fun loadInvoice(invoiceRef: DatabaseReference) {
        _isLoading.value = true

        invoiceRef.removeEventListener(invoiceListener)
        invoiceRef.addValueEventListener(invoiceListener)
    }

    fun removeInvoiceListener(invoiceRef: DatabaseReference) {
        invoiceRef.removeEventListener(invoiceListener)
    }
}