package com.bekado.bekadoonline.helper

import com.bekado.bekadoonline.model.TransaksiModel
import com.google.firebase.database.DataSnapshot

object HelperTransaksi {
    const val semua = 0
    const val nungguBayar = 11
    const val nungguKonfirm = 22
    const val ngeproses = 33
    const val ngirim = 44
    const val selese = 55
    const val dibatalin = 66

    const val day7 = 77
    const val day30 = 3030
    const val day90 = 9090

    fun getData(item: DataSnapshot, dataTransaksi: ArrayList<TransaksiModel>) {
        val idTrx = item.child("idTransaksi").value.toString()
        val noPsnn = item.child("noPesanan").value.toString()
        val timestp = item.child("timestamp").value.toString()
        val sttsPsnn = item.child("statusPesanan").value.toString()
        val currency = item.child("currency").value.toString()
        val totalBlnj = item.child("totalBelanja").value as Long
        val produkList = item.child("produkList").children.toList()
        val lastProduk = produkList[0]
        val fotoPrdk = lastProduk.child("fotoProduk").value.toString()
        val namaPrdk = lastProduk.child("namaProduk").value.toString()
        val jmlhPrdk = lastProduk.child("jumlahProduk").value as Long
        val lainnya = item.child("totalItem").value as Long
        val transaksi = TransaksiModel(idTrx, noPsnn, timestp, sttsPsnn, fotoPrdk, namaPrdk, jmlhPrdk, currency, totalBlnj, lainnya)

        dataTransaksi.add(transaksi)
        dataTransaksi.sortByDescending { it.timestamp }
    }
}