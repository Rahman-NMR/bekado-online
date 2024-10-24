package com.bekado.bekadoonline.domain.repositories

import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.model.TransaksiModel
import com.bekado.bekadoonline.data.model.TrxCountModel

interface TrxRepository {
    fun getLoading(): LiveData<Boolean>
    fun getListDataTransaksi(akunModel: AkunModel?): LiveData<ArrayList<TransaksiModel>?>
    fun getTotalTransaksi(akunModel: AkunModel?): LiveData<TrxCountModel?>
    fun removeListener(akunModel: AkunModel?)
}