package com.bekado.bekadoonline.view.viewmodel.transaksi

import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.model.AlamatModel
import com.bekado.bekadoonline.data.model.CombinedKeranjangModel
import com.bekado.bekadoonline.data.model.RincianPembayaranModel
import com.bekado.bekadoonline.data.model.TrxDetailModel
import com.bekado.bekadoonline.domain.usecase.TrxUseCase
import com.bekado.bekadoonline.helper.Helper
import java.util.Locale

class CheckoutViewModel(private val trxUseCase: TrxUseCase) : ViewModel() {
    private val latiStore = -7.4547115
    private val longiStore = 109.258109

    fun hitungJarak(latitude: String, longitude: String): String {
        val distance = Helper.calcDistance(latitude.toDouble(), longitude.toDouble(), latiStore, longiStore)
        val jarakTxt =
            if (distance < 1) String.format(Locale.getDefault(), "%.0f m", distance * 1000)
            else String.format(Locale.getDefault(), "%.1f km", distance)

        return jarakTxt
    }

    fun rincianHarga(produkList: ArrayList<CombinedKeranjangModel>, ongkir: Long = 0): RincianPembayaranModel {
        val totalHarga = produkList.sumOf {
            val hargaInt = it.produkModel?.hargaProduk ?: 0
            val jumlahHarga = it.keranjangModel?.jumlahProduk ?: 0
            hargaInt * jumlahHarga
        }
        val totalItem = produkList.count()
        val totalBelanja = totalHarga + ongkir

        return RincianPembayaranModel(totalHarga = totalHarga, totalItem = totalItem, ongkir = ongkir, totalBelanja = totalBelanja)
    }

    fun generateIdPesanan(currentTime: String, totalItem: Int): String {
        val idPesanan = "$totalItem$currentTime"
        val timestampCrop = idPesanan.chunked(4).joinToString("/")
        return "INV/$timestampCrop"
    }

    fun addNewTransaksi(
        dataTransaksi: TrxDetailModel,
        dataAlamat: AlamatModel,
        produkMap: MutableMap<String, Any>,
        totalBelanja: Long,
        response: (Boolean) -> Unit
    ) {
        trxUseCase.executeCreateTransaksi(dataTransaksi, dataAlamat, produkMap, totalBelanja, response)
    }

    override fun onCleared() {
        super.onCleared()
        selectedProduk.clear()
    }

    companion object {
        var selectedProduk = ArrayList<CombinedKeranjangModel>()
    }
}