package com.bekado.bekadoonline.domain.usecase

import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.ButtonModel
import com.bekado.bekadoonline.data.model.ProdukModel
import java.util.ArrayList

interface ProductUseCase {
    fun executeLoadingProduk(): LiveData<Boolean>
    fun executeGetAllDataProduk(): LiveData<ArrayList<ProdukModel>?>
    fun executeFilterByKategori(): LiveData<ArrayList<ButtonModel>?>
    fun executeRemoveListener()
}