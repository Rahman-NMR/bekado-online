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
import com.bekado.bekadoonline.helper.HelperTransaksi
import com.google.android.material.bottomsheet.BottomSheetDialog

class FilterDateBottomSheet(val context: Context) {
    private var bindingBS: BottomsheetSelectedTextBinding = BottomsheetSelectedTextBinding.inflate(LayoutInflater.from(context))
    var dialog: BottomSheetDialog
    private var dataDate: ArrayList<SortModel>
    private lateinit var adapterSort: AdapterSort
    var sortFilter: Int = 0
    lateinit var filteredName: String

    init {
        dataDate = ArrayList()
        dialog = BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme)
        dialog.setContentView(bindingBS.root)
    }

    fun showDialog(idSortFilter: Int, filtered: String) {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val paddingHorizontal = context.resources.getDimensionPixelSize(R.dimen.normaldp)
        with(bindingBS) {
            title.text = context.getString(R.string.pilih_waktu)
            recyclerView.layoutManager = layoutManager
            recyclerView.addItemDecoration(Divider(context, R.drawable.divider, paddingHorizontal))
            loadingIndicator.visibility = View.GONE
        }

        dataDate.add(SortModel(context.getString(R.string.f_semua_wktutrx), HelperTransaksi.semua, true))
        dataDate.add(SortModel(context.getString(R.string.f_seminggu_lalu), HelperTransaksi.day7, false))
        dataDate.add(SortModel(context.getString(R.string.f_30hari), HelperTransaksi.day30, false))
        dataDate.add(SortModel(context.getString(R.string.f_90hari), HelperTransaksi.day90, false))

        dataDate = dataDate.map { nilai ->
            nilai.dipilih = nilai.id == idSortFilter
            nilai
        } as ArrayList<SortModel>

        adapterSort = AdapterSort(dataDate) { sortModel ->
            for (nilai in dataDate) {
                nilai.dipilih = sortModel.dipilih
            }

            this.sortFilter = sortModel.id!!
            this.filteredName = sortModel.nama!!
            sortModel.dipilih = true

            adapterSort.notifyDataSetChanged()
            dialog.dismiss()
        }
        bindingBS.recyclerView.adapter = adapterSort

        dialog.setOnShowListener {
            this.sortFilter = idSortFilter
            this.filteredName = filtered
        }
        dialog.show()
    }
}