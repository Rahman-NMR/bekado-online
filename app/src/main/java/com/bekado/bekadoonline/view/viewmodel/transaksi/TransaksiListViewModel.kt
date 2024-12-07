package com.bekado.bekadoonline.view.viewmodel.transaksi

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.model.TrxListModel
import com.bekado.bekadoonline.domain.usecase.TrxUseCase

class TransaksiListViewModel(private val trxUseCase: TrxUseCase) : ViewModel() {
    private var dataAkunModel: AkunModel? = AkunModel()

    fun isLoading(): LiveData<Boolean> = trxUseCase.executeLoading()
    fun getDataTransaksi(akunModel: AkunModel?): LiveData<ArrayList<TrxListModel>?> {
        dataAkunModel = akunModel
        return trxUseCase.executeListDataTransaksi(akunModel)
    }

    fun searchTransaksi(dataTransaksi: ArrayList<TrxListModel>, searchText: String): ArrayList<TrxListModel> {
        return dataTransaksi.filter { data ->
            val textToSearch = searchText.lowercase()

            data.namaProduk.toString().contains(textToSearch, ignoreCase = true) ||
                    data.totalBelanja.toString().contains(textToSearch, ignoreCase = true) ||
                    data.noPesanan.toString().contains(textToSearch, ignoreCase = true) ||
                    data.statusPesanan.toString().contains(textToSearch, ignoreCase = true)
        } as ArrayList<TrxListModel>
    }

    override fun onCleared() {
        super.onCleared()
        trxUseCase.executeRemoveListener(dataAkunModel)
    }
}