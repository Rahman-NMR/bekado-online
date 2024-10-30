package com.bekado.bekadoonline.view.ui.bottomsheet.admn

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.BottomsheetSetStatusPesananBinding
import com.bekado.bekadoonline.helper.Helper.showAlertDialog
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.view.viewmodel.transaksi.DetailTransaksiViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog

class BottomSheetStatusPesanan(
    private val context: Context,
    private val extraPathDTransaksi: String?,
    private val viewModel: DetailTransaksiViewModel
) {
    private var bindingBS: BottomsheetSetStatusPesananBinding = BottomsheetSetStatusPesananBinding.inflate(LayoutInflater.from(context))
    var dialog: BottomSheetDialog = BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme)
    var selectedParent: String = ""
    var selectedStatus: String = ""
    var selected: Boolean = false

    init {
        dialog.setContentView(bindingBS.root)
    }

    fun showDialog(statusPesanan: String?) {
        with(bindingBS) {
            title.text = context.getString(R.string.pilih_status_pesanan)

            when (statusPesanan) {
                context.getString(R.string.status_menunggu_pembayaran) -> updateIsActivatedStatus(stsTungguBayar)
                context.getString(R.string.status_menunggu_konfirmasi) -> updateIsActivatedStatus(stsTungguKonfirm)
                context.getString(R.string.status_dalam_proses) -> updateIsActivatedStatus(stsDlmProses)
                context.getString(R.string.status_dalam_pengiriman) -> updateIsActivatedStatus(stsDlmKirim)
                context.getString(R.string.status_selesai) -> updateIsActivatedStatus(stsSelesai)
                context.getString(R.string.status_dibatalkan) -> updateIsActivatedStatus(stsDibatalkan)
            }

            val clicked = View.OnClickListener { view ->
                for (textView in listOf(stsTungguBayar, stsTungguKonfirm, stsDlmProses, stsDlmKirim, stsSelesai, stsDibatalkan)) {
                    textView.isActivated = false
                }

                selectedParent = when (view) {
                    stsTungguBayar, stsTungguKonfirm -> context.getString(R.string.key_antrian)
                    stsDlmProses, stsDlmKirim -> context.getString(R.string.key_proses)
                    stsSelesai, stsDibatalkan -> context.getString(R.string.key_selesai)
                    else -> ""
                }

                selectedStatus = (view as TextView).text.toString()
                btnPilihStatus.isEnabled = statusPesanan != selectedStatus
                view.isActivated = true
            }

            for (textView in listOf(stsTungguBayar, stsTungguKonfirm, stsDlmProses, stsDlmKirim, stsSelesai, stsDibatalkan)) {
                textView.setOnClickListener(clicked)
            }

            btnPilihStatus.setOnClickListener {
                showAlertDialog(
                    context.getString(R.string.ubah_status_pesanan),
                    context.getString(R.string.sts_psnsn_ubah_mjd, "'$selectedStatus'."),
                    context.getString(R.string.ubah),
                    context,
                    context.getColor(R.color.blue_grey_700)
                ) { setStatusValue() }
            }
        }

        dialog.show()
    }

    private fun setStatusValue() {
        if (HelperConnection.isConnected(context)) {
            if (selectedParent.isNotEmpty() && selectedStatus.isNotEmpty())
                if (!extraPathDTransaksi.isNullOrEmpty()) {
                    viewModel.updateStatusPesanan(extraPathDTransaksi, selectedStatus, selectedParent) { isSuccessful ->
                        if (isSuccessful) {
                            showToast(context.getString(R.string.status_pesanan_response, "berhasil"), context)
                            selected = true
                            dialog.cancel()
                        } else showToast(context.getString(R.string.status_pesanan_response, "gagal"), context)
                    }
                } else showToast(context.getString(R.string.detail_transaksi_not_found, "ID "), context)
            else showToast(context.getString(R.string.status_pesanan_belom_pilih), context)
        }
    }

    private fun updateIsActivatedStatus(textView: TextView) {
        textView.isActivated = true
    }
}