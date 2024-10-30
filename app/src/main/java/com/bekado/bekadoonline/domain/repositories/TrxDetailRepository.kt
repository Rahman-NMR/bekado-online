package com.bekado.bekadoonline.domain.repositories

import android.net.Uri
import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.model.AlamatModel
import com.bekado.bekadoonline.data.model.BuktiPembayaranModel
import com.bekado.bekadoonline.data.model.CombinedKeranjangModel
import com.bekado.bekadoonline.data.model.TrxDetailModel

interface TrxDetailRepository {
    fun getDetailTransaksi(pathDetailTrx: String?): LiveData<TrxDetailModel?>
    fun getLoading(): LiveData<Boolean>
    fun getAlamat(): LiveData<AlamatModel?>
    fun getPayment(): LiveData<BuktiPembayaranModel?>
    fun getProdukList(): LiveData<ArrayList<CombinedKeranjangModel>?>
    fun uploadBuktiPembayaran(imageUri: Uri, statusPesanan: String, pathDetailTrx: String?, response: (Boolean) -> Unit)
    fun getDataAkunOwner(): LiveData<AkunModel?>
    fun updateStatusPesanan(pathDetailTrx: String, selectedStatus: String, selectedParent: String, response: (Boolean) -> Unit)
    fun removeListener(pathDetailTrx: String?)
}