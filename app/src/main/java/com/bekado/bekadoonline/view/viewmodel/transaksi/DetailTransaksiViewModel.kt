package com.bekado.bekadoonline.view.viewmodel.transaksi

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.model.AlamatModel
import com.bekado.bekadoonline.data.model.BuktiPembayaranModel
import com.bekado.bekadoonline.data.model.CombinedKeranjangModel
import com.bekado.bekadoonline.data.model.TrxDetailModel
import com.bekado.bekadoonline.domain.usecase.TrxUseCase

class DetailTransaksiViewModel(private val trxUseCase: TrxUseCase) : ViewModel() {
    private var pathDetailTrx: String? = ""

    fun isLoading(): LiveData<Boolean> = trxUseCase.executeLoadingDetailTrx()
    fun getAlamat(): LiveData<AlamatModel?> = trxUseCase.executeGetAlamat()
    fun getPayment(): LiveData<BuktiPembayaranModel?> = trxUseCase.executeGetPayment()
    fun getProdukList(): LiveData<ArrayList<CombinedKeranjangModel>?> = trxUseCase.executeGetProdukList()
    fun getDataAkunOwner(): LiveData<AkunModel?> = trxUseCase.executeGetDataAkunOwner()

    fun getDetailTransaksi(pathDetailTrx: String?): LiveData<TrxDetailModel?> {
        this.pathDetailTrx = pathDetailTrx
        return trxUseCase.executeGetDetailTransaksi(pathDetailTrx)
    }

    fun uploadBuktiPembayaran(imageUri: Uri, statusPesanan: String, response: (Boolean) -> Unit) {
        trxUseCase.executeUploadBuktiPembayaran(imageUri, statusPesanan, pathDetailTrx, response)
    }

    fun updateStatusPesanan(pathDetailTrx: String, selectedStatus: String, selectedParent: String, response: (Boolean) -> Unit) {
        trxUseCase.executeUpdateStatusPesanan(pathDetailTrx, selectedStatus, selectedParent, response)
    }

    override fun onCleared() {
        super.onCleared()
        trxUseCase.executeRemoveDetailListener(pathDetailTrx)
    }
}