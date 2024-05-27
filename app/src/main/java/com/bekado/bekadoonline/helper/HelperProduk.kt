package com.bekado.bekadoonline.helper

import android.content.Context
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.helper.Helper.showToast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.util.Date

object HelperProduk {
    fun addToKeranjang(produk: ProdukModel, keranjangRef: DatabaseReference, context: Context) {
        val idProduk = produk.idProduk.toString()
        keranjangRef.child(idProduk).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tambahKeranjang = HashMap<String, Any>()
                val jumlahPesan = snapshot.child("jumlahProduk").value as Long? ?: 0

                tambahKeranjang["idProduk"] = idProduk
                tambahKeranjang["jumlahProduk"] = jumlahPesan + 1
                tambahKeranjang["timestamp"] = Date().time.toString()
                tambahKeranjang["diPilih"] = false
                keranjangRef.child(idProduk).setValue(tambahKeranjang)
                    .addOnSuccessListener { showToast("${produk.namaProduk} ditambahkan ke keranjang", context) }
                    .addOnFailureListener { showToast("Tidak dapat menambahkan ${produk.namaProduk} ke keranjang", context) }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Tidak dapat mengambil data keranjang", context)
            }
        })
    }

    fun plusMinus(keranjangRef: DatabaseReference, add: Boolean) {
        keranjangRef.child("jumlahProduk").get().addOnSuccessListener { dataSnapshot ->
            val jumlahPesanOld = dataSnapshot.getValue(Long::class.java) ?: 0
            val item = if (add) jumlahPesanOld + 1 else jumlahPesanOld - 1

            keranjangRef.child("jumlahProduk").setValue(item)
        }
    }
}