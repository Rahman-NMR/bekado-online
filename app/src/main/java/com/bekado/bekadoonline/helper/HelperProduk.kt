package com.bekado.bekadoonline.helper

import com.bekado.bekadoonline.adapter.AdapterProduk
import com.bekado.bekadoonline.helper.HelperSort.sortNameAsc
import com.bekado.bekadoonline.helper.HelperSort.sortNameDesc
import com.bekado.bekadoonline.helper.HelperSort.sortPriceDesc
import com.bekado.bekadoonline.helper.HelperSort.sortPriceAsc
import com.bekado.bekadoonline.model.KategoriModel
import com.bekado.bekadoonline.model.ProdukModel
import com.google.firebase.database.DataSnapshot

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
}