package com.bekado.bekadoonline.bottomsheet

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.adapter.AdapterSort
import com.bekado.bekadoonline.databinding.BottomsheetSelectedTextBinding
import com.bekado.bekadoonline.model.SortModel
import com.bekado.bekadoonline.helper.HelperSort.sortNameAsc
import com.bekado.bekadoonline.helper.HelperSort.sortNameDesc
import com.bekado.bekadoonline.helper.HelperSort.sortPriceAsc
import com.bekado.bekadoonline.helper.HelperSort.sortPriceDesc
import com.bekado.bekadoonline.helper.HelperSort.sortRelevance
import com.google.android.material.bottomsheet.BottomSheetDialog

class SortProdukBottomSheet(context: Context) {
    private var bindingBS: BottomsheetSelectedTextBinding
    var dialog: BottomSheetDialog
    private var dataSort: ArrayList<SortModel>
    private lateinit var adapterSort: AdapterSort
    var sortFilter: Int = 0

    init {
        bindingBS = BottomsheetSelectedTextBinding.inflate(LayoutInflater.from(context))
        dataSort = ArrayList()
        dialog = BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme)
        dialog.setContentView(bindingBS.root)
    }

    fun showDialog(context: Context, idSortFilter: Int) {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        with(bindingBS) {
            title.text = context.getString(R.string.urutkan_berdasarkan)
            recyclerView.layoutManager = layoutManager
            loadingIndicator.visibility = View.GONE
        }

        dataSort.add(SortModel(context.getString(R.string.rekomendasi), sortRelevance, true))
        dataSort.add(SortModel(context.getString(R.string.sortir_a_z), sortNameAsc, false))
        dataSort.add(SortModel(context.getString(R.string.sortir_z_a), sortNameDesc, false))
        dataSort.add(SortModel(context.getString(R.string.harga_tertinggi), sortPriceDesc, false))
        dataSort.add(SortModel(context.getString(R.string.harga_terendah), sortPriceAsc, false))

        dataSort = dataSort.map { nilai ->
            nilai.dipilih = nilai.id == idSortFilter
            nilai
        } as ArrayList<SortModel>

        adapterSort = AdapterSort(dataSort) { sortModel ->
            for (nilai in dataSort) {
                nilai.dipilih = sortModel.dipilih
            }

            this.sortFilter = sortModel.id!!
            sortModel.dipilih = true

            adapterSort.notifyDataSetChanged()
            dialog.dismiss()
        }
        bindingBS.recyclerView.adapter = adapterSort

        dialog.setOnShowListener { this.sortFilter = idSortFilter }
        dialog.show()
    }
}