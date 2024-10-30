package com.bekado.bekadoonline.domain.usecase

import android.net.Uri
import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.model.AlamatModel
import com.bekado.bekadoonline.data.model.BuktiPembayaranModel
import com.bekado.bekadoonline.data.model.CombinedKeranjangModel
import com.bekado.bekadoonline.data.model.TrxListModel
import com.bekado.bekadoonline.data.model.TrxCountModel
import com.bekado.bekadoonline.data.model.TrxDetailModel

interface TrxUseCase {
    fun executeLoading(): LiveData<Boolean>
    fun executeListDataTransaksi(akunModel: AkunModel?): LiveData<ArrayList<TrxListModel>?>
    fun executeTotalTransaksi(akunModel: AkunModel?): LiveData<TrxCountModel?>
    fun executeRemoveListener(akunModel: AkunModel?)
    fun executeCreateTransaksi(
        dataTransaksi: TrxDetailModel,
        dataAlamat: AlamatModel,
        produkMap: MutableMap<String, Any>,
        totalBelanja: Long,
        response: (Boolean) -> Unit
    )

    fun executeGetDetailTransaksi(pathDetailTrx: String?): LiveData<TrxDetailModel?>
    fun executeLoadingDetailTrx(): LiveData<Boolean>
    fun executeGetAlamat(): LiveData<AlamatModel?>
    fun executeGetPayment(): LiveData<BuktiPembayaranModel?>
    fun executeGetProdukList(): LiveData<ArrayList<CombinedKeranjangModel>?>
    fun executeUploadBuktiPembayaran(imageUri: Uri, statusPesanan: String, pathDetailTrx: String?, response: (Boolean) -> Unit)
    fun executeGetDataAkunOwner(): LiveData<AkunModel?>
    fun executeUpdateStatusPesanan(pathDetailTrx: String, selectedStatus: String, selectedParent: String, response: (Boolean) -> Unit)
    fun executeRemoveDetailListener(pathDetailTrx: String?)
}