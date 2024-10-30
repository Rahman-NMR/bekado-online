package com.bekado.bekadoonline.domain.interactor

import android.net.Uri
import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.model.AlamatModel
import com.bekado.bekadoonline.data.model.BuktiPembayaranModel
import com.bekado.bekadoonline.data.model.CombinedKeranjangModel
import com.bekado.bekadoonline.data.model.TrxListModel
import com.bekado.bekadoonline.data.model.TrxCountModel
import com.bekado.bekadoonline.data.model.TrxDetailModel
import com.bekado.bekadoonline.domain.repositories.TrxDetailRepository
import com.bekado.bekadoonline.domain.repositories.TrxRepository
import com.bekado.bekadoonline.domain.usecase.TrxUseCase

class TrxUseCaseInteractor(
    private val trxRepository: TrxRepository,
    private val detailRepository: TrxDetailRepository
) : TrxUseCase {
    override fun executeLoading(): LiveData<Boolean> {
        return trxRepository.getLoading()
    }

    override fun executeListDataTransaksi(akunModel: AkunModel?): LiveData<ArrayList<TrxListModel>?> {
        return trxRepository.getListDataTransaksi(akunModel)
    }

    override fun executeTotalTransaksi(akunModel: AkunModel?): LiveData<TrxCountModel?> {
        return trxRepository.getTotalTransaksi(akunModel)
    }

    override fun executeRemoveListener(akunModel: AkunModel?) {
        trxRepository.removeListener(akunModel)
    }

    override fun executeCreateTransaksi(
        dataTransaksi: TrxDetailModel,
        dataAlamat: AlamatModel,
        produkMap: MutableMap<String, Any>,
        totalBelanja: Long,
        response: (Boolean) -> Unit
    ) {
        trxRepository.createTransaksi(dataTransaksi, dataAlamat, produkMap, totalBelanja, response)
    }

    override fun executeGetDetailTransaksi(pathDetailTrx: String?): LiveData<TrxDetailModel?> {
        return detailRepository.getDetailTransaksi(pathDetailTrx)
    }

    override fun executeLoadingDetailTrx(): LiveData<Boolean> {
        return detailRepository.getLoading()
    }

    override fun executeGetAlamat(): LiveData<AlamatModel?> {
        return detailRepository.getAlamat()
    }

    override fun executeGetPayment(): LiveData<BuktiPembayaranModel?> {
        return detailRepository.getPayment()
    }

    override fun executeGetProdukList(): LiveData<ArrayList<CombinedKeranjangModel>?> {
        return detailRepository.getProdukList()
    }

    override fun executeUploadBuktiPembayaran(imageUri: Uri, statusPesanan: String, pathDetailTrx: String?, response: (Boolean) -> Unit) {
        detailRepository.uploadBuktiPembayaran(imageUri, statusPesanan, pathDetailTrx, response)
    }

    override fun executeGetDataAkunOwner(): LiveData<AkunModel?> {
        return detailRepository.getDataAkunOwner()
    }

    override fun executeUpdateStatusPesanan(pathDetailTrx: String, selectedStatus: String, selectedParent: String, response: (Boolean) -> Unit) {
        detailRepository.updateStatusPesanan(pathDetailTrx, selectedStatus, selectedParent, response)
    }

    override fun executeRemoveDetailListener(pathDetailTrx: String?) {
        detailRepository.removeListener(pathDetailTrx)
    }
}