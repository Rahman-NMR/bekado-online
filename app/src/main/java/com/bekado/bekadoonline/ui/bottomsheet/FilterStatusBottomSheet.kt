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

class FilterStatusBottomSheet(val context: Context) {
    private var bindingBS: BottomsheetSelectedTextBinding = BottomsheetSelectedTextBinding.inflate(LayoutInflater.from(context))
    var dialog: BottomSheetDialog
    private var dataStatus: ArrayList<SortModel>
    private lateinit var adapterSort: AdapterSort
    var sortFilter: Int = 0
    lateinit var filteredName: String

    init {
        dataStatus = ArrayList()
        dialog = BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme)
        dialog.setContentView(bindingBS.root)
    }

    fun showDialog(idSortFilter: Int, filtered: String) {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val paddingHorizontal = context.resources.getDimensionPixelSize(R.dimen.normaldp)
        with(bindingBS) {
            title.text = context.getString(R.string.status_pesanan)
            recyclerView.layoutManager = layoutManager
            recyclerView.addItemDecoration(Divider(context, R.drawable.divider, paddingHorizontal))
            loadingIndicator.visibility = View.GONE
        }

        dataStatus.add(SortModel(context.getString(R.string.f_semua_stspsnn), HelperTransaksi.semua, true))
        dataStatus.add(SortModel(context.getString(R.string.status_menunggu_pembayaran), HelperTransaksi.nungguBayar, false))
        dataStatus.add(SortModel(context.getString(R.string.status_menunggu_konfirmasi), HelperTransaksi.nungguKonfirm, false))
        dataStatus.add(SortModel(context.getString(R.string.status_dalam_proses), HelperTransaksi.ngeproses, false))
        dataStatus.add(SortModel(context.getString(R.string.status_dalam_pengiriman), HelperTransaksi.ngirim, false))
        dataStatus.add(SortModel(context.getString(R.string.status_selesai), HelperTransaksi.selese, false))
        dataStatus.add(SortModel(context.getString(R.string.status_dibatalkan), HelperTransaksi.dibatalin, false))

        dataStatus = dataStatus.map { nilai ->
            nilai.dipilih = nilai.id == idSortFilter
            nilai
        } as ArrayList<SortModel>

        adapterSort = AdapterSort(dataStatus) { sortModel ->
            for (nilai in dataStatus) {
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