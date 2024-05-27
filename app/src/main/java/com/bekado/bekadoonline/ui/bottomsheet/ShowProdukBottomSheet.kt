package com.bekado.bekadoonline.ui.bottomsheet

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.BottomsheetShowProdukBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.helper.HelperProduk
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShowProdukBottomSheet(val context: Context) {
    private var bindingPBS: BottomsheetShowProdukBinding = BottomsheetShowProdukBinding.inflate(LayoutInflater.from(context))
    private var dialog: BottomSheetDialog = BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme)
    private lateinit var keranjangListener: ValueEventListener
    private lateinit var keranjangRef: DatabaseReference
    private var jumlahPesanV: Int = 0

    init {
        dialog.setContentView(bindingPBS.root)
    }

    fun showDialog(produk: ProdukModel, currentUser: FirebaseUser?, db: FirebaseDatabase, onClick: () -> Unit) {
        val hargaProdukShows = produk.currency + Helper.addcoma3digit(produk.hargaProduk)

        with(bindingPBS) {
            Glide.with(context).load(produk.fotoProduk)
                .apply(RequestOptions()).centerCrop()
                .placeholder(R.drawable.img_broken_image).into(fotoProduk)
            namaProduk.text = produk.namaProduk
            hargaProduk.text = hargaProdukShows
        }

        if (currentUser != null) {
            keranjangRef = db.getReference("keranjang/${currentUser.uid}/${produk.idProduk}")
            setupValueEventListener(produk.namaProduk, context)
            checkDataExistence()
            plusMinusJumlah(produk.namaProduk, context)

            dialog.setOnDismissListener { keranjangListener.let { keranjangRef.removeEventListener(it) } }
            dialog.setOnCancelListener { keranjangListener.let { keranjangRef.removeEventListener(it) } }
        } else {
            with(bindingPBS) {
                btnTambahKeranjang.visibility = View.VISIBLE
                llKeranjangJumlah.visibility = View.GONE
                line.visibility = View.GONE
            }
        }

        bindingPBS.btnTambahKeranjang.setOnClickListener {
            onClick()
            dialog.cancel()
        }

        dialog.show()
    }

    private fun setupValueEventListener(namaProduk: String?, context: Context) {
        keranjangListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val jumlahPesan = snapshot.child("jumlahProduk").getValue(Long::class.java) ?: 0
                jumlahPesanV = jumlahPesan.toInt()
                bindingPBS.jumlahProduk.text = jumlahPesan.toString()
                plusMinusJumlah(namaProduk, context)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        keranjangRef.addValueEventListener(keranjangListener)
    }

    private fun plusMinusJumlah(namaProduk: String?, context: Context) {
        bindingPBS.tambahJumlahProduk.apply {
            isEnabled = jumlahPesanV < 100
            setOnClickListener { HelperProduk.plusMinus(keranjangRef, true) }
        }
        bindingPBS.kurangJumlahProduk.setOnClickListener {
            if (jumlahPesanV <= 1) {
                keranjangRef.removeValue()
                Helper.showToast("$namaProduk dihapus dari keranjang", context)
                dialog.cancel()
            } else HelperProduk.plusMinus(keranjangRef, false)
        }
    }

    private fun checkDataExistence() {
        keranjangRef.get().addOnSuccessListener { dataSnapshot ->
            with(bindingPBS) {
                btnTambahKeranjang.visibility = if (dataSnapshot.exists()) View.GONE else View.VISIBLE
                llKeranjangJumlah.visibility = if (dataSnapshot.exists()) View.VISIBLE else View.GONE
                line.visibility = if (dataSnapshot.exists()) View.VISIBLE else View.GONE
            }
        }
    }
}