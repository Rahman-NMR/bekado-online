package com.bekado.bekadoonline.ui.bottomsheet

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.adapter.AdapterSort
import com.bekado.bekadoonline.databinding.BottomsheetSelectedTextBinding
import com.bekado.bekadoonline.helper.itemDecoration.Divider
import com.bekado.bekadoonline.data.model.SortModel
import com.bekado.bekadoonline.helper.HelperSort.SORT_BY_NAME_ASCENDING
import com.bekado.bekadoonline.helper.HelperSort.SORT_BY_NAME_DESCENDING
import com.bekado.bekadoonline.helper.HelperSort.SORT_BY_PRICE_ASCENDING
import com.bekado.bekadoonline.helper.HelperSort.SORT_BY_PRICE_DESCENDING
import com.bekado.bekadoonline.helper.HelperSort.SORT_BY_DEFAULT
import com.google.android.material.bottomsheet.BottomSheetDialog

class SortProdukBottomSheet(val context: Context) {
    private var bindingBS: BottomsheetSelectedTextBinding = BottomsheetSelectedTextBinding.inflate(LayoutInflater.from(context))
    var dialog: BottomSheetDialog
    private var dataSort: ArrayList<SortModel>
    private lateinit var adapterSort: AdapterSort
    var sortFilter: Int = 0

    init {
        dataSort = ArrayList()
        dialog = BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme)
        dialog.setContentView(bindingBS.root)
    }

    fun showDialog(idSortFilter: Int) {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val paddingHorizontal = context.resources.getDimensionPixelSize(R.dimen.normaldp)
        with(bindingBS) {
            title.text = context.getString(R.string.urutkan_berdasarkan)
            recyclerView.layoutManager = layoutManager
            recyclerView.addItemDecoration(Divider(context, R.drawable.divider, paddingHorizontal))
            loadingIndicator.visibility = View.GONE
        }

        dataSort.add(SortModel(context.getString(R.string.random), SORT_BY_DEFAULT, true))
        dataSort.add(SortModel(context.getString(R.string.sortir_a_z), SORT_BY_NAME_ASCENDING, false))
        dataSort.add(SortModel(context.getString(R.string.sortir_z_a), SORT_BY_NAME_DESCENDING, false))
        dataSort.add(SortModel(context.getString(R.string.harga_tertinggi), SORT_BY_PRICE_DESCENDING, false))
        dataSort.add(SortModel(context.getString(R.string.harga_terendah), SORT_BY_PRICE_ASCENDING, false))

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

            dialog.dismiss()
        }
        bindingBS.recyclerView.adapter = adapterSort

        dialog.setOnShowListener { this.sortFilter = idSortFilter }
        dialog.show()
    }
}