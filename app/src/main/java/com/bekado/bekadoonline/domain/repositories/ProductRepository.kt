package com.bekado.bekadoonline.domain.repositories

import androidx.lifecycle.LiveData
import com.bekado.bekadoonline.data.model.ButtonModel
import com.bekado.bekadoonline.data.model.ProdukModel
import java.util.ArrayList

interface ProductRepository {
    fun getLoading(): LiveData<Boolean>
    fun getAllDataProduk(): LiveData<ArrayList<ProdukModel>?>
    fun filterByKategori(): LiveData<ArrayList<ButtonModel>?>
    fun removeListener()
}