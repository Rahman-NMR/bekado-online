package com.bekado.bekadoonline.helper

import android.content.Context
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.bekado.bekadoonline.adapter.AdapterProduk
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.HelperSort.sortNameAsc
import com.bekado.bekadoonline.helper.HelperSort.sortNameDesc
import com.bekado.bekadoonline.helper.HelperSort.sortPriceDesc
import com.bekado.bekadoonline.helper.HelperSort.sortPriceAsc
import com.bekado.bekadoonline.model.CombinedKeranjangModel
import com.bekado.bekadoonline.model.KategoriModel
import com.bekado.bekadoonline.model.ProdukModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.util.Date
import java.util.HashMap

object HelperProduk {
    fun getFiltered(
        snapshot: DataSnapshot,
        idKategoriSkrng: String,
        dataProduk: ArrayList<ProdukModel>,
        adapter: AdapterProduk,
        sortFilter: Int?
    ) {
        dataProduk.clear()
        for (data in snapshot.child("produk").children) {
            val idKategori = data.child("idKategori").value.toString()
            if (idKategoriSkrng == idKategori) {
                val produk = data.getValue(ProdukModel::class.java)
                if (produk != null) if (produk.visibility) dataProduk.add(produk)
            }
        }
        sortProduk(dataProduk, sortFilter)
        adapter.notifyDataSetChanged()
    }

    fun getAllProduk(
        snapshot: DataSnapshot,
        dataProduk: ArrayList<ProdukModel>,
        dataKategori: ArrayList<KategoriModel>,
        adapter: AdapterProduk,
        sortFilter: Int?
    ) {
        dataKategori.clear()
        for (data in snapshot.child("kategori").children) {
            val kategori = data.getValue(KategoriModel::class.java)
            dataKategori.add(kategori!!)

            dataProduk.clear()
            for (item in snapshot.child("produk").children) {
                val produk = item.getValue(ProdukModel::class.java)
                if (produk != null) {
                    val produkDiKategori = dataKategori.find { it.idKategori == produk.idKategori && it.visibilitas }
                    if (produkDiKategori != null) if (produk.visibility) dataProduk.add(produk)
                }
            }
        }
        sortProduk(dataProduk, sortFilter)
        adapter.notifyDataSetChanged()
    }

    fun sortProduk(dataProduk: ArrayList<ProdukModel>, sortFilter: Int?) {
        when (sortFilter) {
            sortNameAsc -> dataProduk.sortBy { it.namaProduk }
            sortNameDesc -> dataProduk.sortByDescending { it.namaProduk }
            sortPriceAsc -> dataProduk.sortBy { it.hargaProduk }
            sortPriceDesc -> dataProduk.sortByDescending { it.hargaProduk }
            else -> dataProduk.sortBy { it.idProduk }
        }
    }

    fun addToKeranjang(produk: ProdukModel, keranjangRef: DatabaseReference, context: Context) {
        val idProduk = produk.idProduk.toString()
        keranjangRef.child(idProduk).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tambahKeranjang = HashMap<String, Any>()
                val jumlahPesan = snapshot.child("jumlahProduk").value as Long? ?: 0

                tambahKeranjang["idProduk"] = idProduk
                tambahKeranjang["jumlahProduk"] = jumlahPesan + 1
                tambahKeranjang["timestamp"] = Date().time.toString()
                tambahKeranjang["diPilih"] = false
                keranjangRef.child(idProduk).setValue(tambahKeranjang)
                    .addOnSuccessListener { showToast("${produk.namaProduk} ditambahkan ke keranjang", context) }
                    .addOnFailureListener { showToast("Tidak dapat menambahkan ${produk.namaProduk} ke keranjang", context) }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Tidak dapat mengambil data keranjang", context)
            }
        })
    }

    fun deleteKeranjang(
        keranjang: CombinedKeranjangModel,
        keranjangRef: DatabaseReference,
        viewBinding: CoordinatorLayout,
        anchorLayout: CardView,
        ditampilkan: Boolean,
        notifyDataSetChanged: Unit
    ) {
        keranjangRef.removeValue()

        val nama = keranjang.produkModel?.namaProduk.toString()
        val idProduk = keranjang.produkModel?.idProduk.toString()
        val jumlahP = keranjang.keranjangModel?.jumlahProduk
        val waktu = keranjang.keranjangModel?.timestamp.toString()

        val snackbar = Snackbar.make(viewBinding, "$nama dihapus dari keranjang", Snackbar.LENGTH_LONG)
        val actionText = "Batalkan"
        val cancelAction = {
            val temp = HashMap<String, Any?>()
            temp["idProduk"] = idProduk
            temp["jumlahProduk"] = jumlahP
            temp["timestamp"] = waktu
            temp["diPilih"] = false
            keranjangRef.setValue(temp)
            notifyDataSetChanged
        }

        snackbar.anchorView = anchorLayout
        if (ditampilkan) snackbar.setAction(actionText) { cancelAction() }
        snackbar.show()
    }

    fun plusMinus(keranjangRef: DatabaseReference, add: Boolean) {
        keranjangRef.child("jumlahProduk").get().addOnSuccessListener { dataSnapshot ->
            val jumlahPesanOld = dataSnapshot.getValue(Long::class.java) ?: 0
            val item = if (add) jumlahPesanOld + 1 else jumlahPesanOld - 1

            keranjangRef.child("jumlahProduk").setValue(item)
        }
    }
}