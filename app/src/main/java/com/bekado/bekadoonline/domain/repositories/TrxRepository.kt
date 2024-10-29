package com.bekado.bekadoonline.domain.repositories

import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.model.AlamatModel
import com.bekado.bekadoonline.data.model.TrxListModel
import com.bekado.bekadoonline.data.model.TrxCountModel
import com.bekado.bekadoonline.data.model.TrxDetailModel

interface TrxRepository {
    fun getLoading(): LiveData<Boolean>
    fun getListDataTransaksi(akunModel: AkunModel?): LiveData<ArrayList<TrxListModel>?>
    fun getTotalTransaksi(akunModel: AkunModel?): LiveData<TrxCountModel?>
    fun removeListener(akunModel: AkunModel?)
    fun createTransaksi(
        dataTransaksi: TrxDetailModel,
        dataAlamat: AlamatModel,
        produkMap: MutableMap<String, Any>,
        totalBelanja: Long,
        response: (Boolean) -> Unit
    )
}