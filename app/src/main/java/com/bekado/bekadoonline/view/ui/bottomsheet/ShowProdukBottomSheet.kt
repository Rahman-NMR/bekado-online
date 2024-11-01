package com.bekado.bekadoonline.view.ui.bottomsheet

import android.content.Context
import android.view.LayoutInflater
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.databinding.BottomsheetShowProdukBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.view.viewmodel.keranjang.KeranjangViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog

class ShowProdukBottomSheet(val context: Context) {
    private var bindingPBS: BottomsheetShowProdukBinding = BottomsheetShowProdukBinding.inflate(LayoutInflater.from(context))
    private var dialog: BottomSheetDialog = BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme)

    init {
        dialog.setContentView(bindingPBS.root)
    }

    fun showDialog(produk: ProdukModel, keranjangViewModel: KeranjangViewModel, onClick: () -> Unit) {
        val hargaProdukShows = produk.currency + Helper.addcoma3digit(produk.hargaProduk)

        with(bindingPBS) {
            Glide.with(context).load(produk.fotoProduk)
                .apply(RequestOptions()).centerCrop()
                .placeholder(R.drawable.img_placeholder)
                .error(R.drawable.img_error)
                .transition(DrawableTransitionOptions.withCrossFade(300))
                .into(fotoProduk)
            namaProduk.text = produk.namaProduk
            hargaProduk.text = hargaProdukShows
        }
        keranjangViewModel.produkExistsInKeranjang(produk.idProduk) { isExists, jumlahProduk ->
            bindingPBS.btnTambahKeranjang.isEnabled = !isExists

            if (!isExists) {
                bindingPBS.btnTambahKeranjang.text = context.getString(R.string.tambah_ke_keranjang)
                bindingPBS.btnTambahKeranjang.setOnClickListener {
                    onClick()
                    dialog.cancel()
                }
            } else bindingPBS.btnTambahKeranjang.text = context.getString(R.string.jumlah_di_keranjang, jumlahProduk)
        }

        dialog.show()
    }
}