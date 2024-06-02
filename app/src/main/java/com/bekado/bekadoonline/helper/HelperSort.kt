package com.bekado.bekadoonline.helper

import com.bekado.bekadoonline.data.model.ProdukModel

object HelperSort {
    const val SORT_BY_DEFAULT = 0
    const val SORT_BY_NAME_ASCENDING = 111
    const val SORT_BY_NAME_DESCENDING = 222
    const val SORT_BY_PRICE_DESCENDING = 333
    const val SORT_BY_PRICE_ASCENDING = 444

    fun sortProduk(dataProduk: ArrayList<ProdukModel>, sortFilter: Int?) {
        when (sortFilter) {
            SORT_BY_NAME_ASCENDING -> dataProduk.sortBy { it.namaProduk }
            SORT_BY_NAME_DESCENDING -> dataProduk.sortByDescending { it.namaProduk }
            SORT_BY_PRICE_ASCENDING -> dataProduk.sortBy { it.hargaProduk }
            SORT_BY_PRICE_DESCENDING -> dataProduk.sortByDescending { it.hargaProduk }
            else -> dataProduk.shuffle()
        }
    }
}