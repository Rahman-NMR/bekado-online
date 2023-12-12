package com.bekado.bekadoonline.bottomsheet

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import com.bekado.bekadoonline.LoginActivity
import com.bumptech.glide.Glide
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.BottomsheetShowProdukBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.helper.HelperProduk
import com.bekado.bekadoonline.model.ProdukModel
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShowProdukBottomSheet(context: Context) {
    private var bindingPBS: BottomsheetShowProdukBinding
    private var dialog: BottomSheetDialog
    private lateinit var keranjangListener: ValueEventListener
    private lateinit var keranjangRef: DatabaseReference
    private var jumlahPesanV: Int = 0

    init {
        bindingPBS = BottomsheetShowProdukBinding.inflate(LayoutInflater.from(context))
        dialog = BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme)
        dialog.setContentView(bindingPBS.root)
    }

    fun showDialog(context: Context, produk: ProdukModel, auth: FirebaseAuth, db: FirebaseDatabase) {
        val hargaProdukShows = produk.currency + Helper.addcoma3digit(produk.hargaProduk)

        with(bindingPBS) {
            Glide.with(context).load(produk.fotoProduk)
                .apply(RequestOptions()).centerCrop()
                .into(fotoProduk)
            namaProduk.text = produk.namaProduk
            hargaProduk.text = hargaProdukShows
        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            keranjangRef = db.getReference("keranjang/${currentUser.uid}/${produk.idProduk}")
            setupValueEventListener(keranjangRef)
            checkDataExistence(keranjangRef)
            plusMinusJumlah(keranjangRef)

            dialog.setOnDismissListener {
                keranjangListener.let {
                    keranjangRef.removeEventListener(it)
                }
            }
        } else {
            with(bindingPBS) {
                btnTambahKeranjang.visibility = View.VISIBLE
                llKeranjangJumlah.visibility = View.GONE
                line.visibility = View.GONE
            }
        }

        bindingPBS.btnTambahKeranjang.setOnClickListener {
            if (currentUser != null) {
                val keranjangRef = db.getReference("keranjang/${currentUser.uid}")
                HelperProduk.addToKeranjang(produk, keranjangRef, context)
                dialog.cancel()
            } else context.startActivity(Intent(context, LoginActivity::class.java))
        }

        dialog.show()
    }

    private fun setupValueEventListener(keranjangRef: DatabaseReference) {
        keranjangListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val jumlahPesan = snapshot.child("jumlahProduk").getValue(Long::class.java) ?: 0
                jumlahPesanV = jumlahPesan.toInt()
                bindingPBS.jumlahProduk.text = jumlahPesan.toString()
                plusMinusJumlah(keranjangRef)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        keranjangRef.addValueEventListener(keranjangListener)
    }

    private fun plusMinusJumlah(ref: DatabaseReference) {
        bindingPBS.tambahJumlahProduk.apply {
            isEnabled = jumlahPesanV != 100
            setOnClickListener { HelperProduk.plusMinus(ref, true) }
        }
        bindingPBS.kurangJumlahProduk.setOnClickListener {
            if (jumlahPesanV == 1) {
                ref.removeValue()
                dialog.cancel()
            } else HelperProduk.plusMinus(ref, false)
        }
    }

    private fun checkDataExistence(keranjangRef: DatabaseReference) {
        keranjangRef.get().addOnSuccessListener { dataSnapshot ->
            with(bindingPBS) {
                btnTambahKeranjang.visibility = if (dataSnapshot.exists()) View.GONE else View.VISIBLE
                llKeranjangJumlah.visibility = if (dataSnapshot.exists()) View.VISIBLE else View.GONE
                line.visibility = if (dataSnapshot.exists()) View.VISIBLE else View.GONE
            }
        }
    }
}