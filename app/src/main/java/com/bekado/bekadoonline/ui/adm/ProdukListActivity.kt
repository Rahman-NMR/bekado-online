package com.bekado.bekadoonline.ui.adm

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.adapter.admn.AdapterProdukList
import com.bekado.bekadoonline.databinding.ActivityProdukListBinding
import com.bekado.bekadoonline.helper.GridSpacingItemDecoration
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.Helper.showToastL
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.model.KategoriModel
import com.bekado.bekadoonline.model.ProdukModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProdukListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProdukListBinding
    private lateinit var db: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var adapterProduk: AdapterProdukList
    private var dataProduk: ArrayList<ProdukModel> = ArrayList()
    private var totalProduk: Int = 0

    private lateinit var kategoriData: KategoriModel
    private lateinit var produkRef: DatabaseReference
    private lateinit var produkListener: ValueEventListener
    private lateinit var kategoriRef: DatabaseReference
    private lateinit var kategoriListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProdukListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        db = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        kategoriData = intent.getParcelableExtra("idKategori") ?: KategoriModel()
        produkRef = db.getReference("produk/produk")
        kategoriRef = db.getReference("produk/kategori/${kategoriData.idKategori}")

        val padding = resources.getDimensionPixelSize(R.dimen.normaldp)

        monitorKategori()
        getDataProduk()

        with(binding) {
            rvDaftarProduk.layoutManager = LinearLayoutManager(this@ProdukListActivity, LinearLayoutManager.VERTICAL, false)
            rvDaftarProduk.addItemDecoration(GridSpacingItemDecoration(1, padding, false))

            appBar.setNavigationOnClickListener { onBackPressed() }
            btnHapusKategori.setOnClickListener { showAlertDialog() }
            tvKategoriSekarang.text = kategoriData.namaKategori
            fabTambahProduk.setOnClickListener {
                val ninten = Intent(this@ProdukListActivity, ProdukAddUpdateActivity::class.java)
                    .putExtra("isEditProduk", false).putExtra("kategoriData", kategoriData)
                startActivity(ninten)
            }
        }
    }

    private fun monitorKategori() {
        kategoriListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    showToast(getString(R.string.kategori_terhapus), this@ProdukListActivity)
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        kategoriRef.addValueEventListener(kategoriListener)
    }

    private fun getDataProduk() {
        produkListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dataProduk.clear()
                totalProduk = 0

                for (item in snapshot.children) {
                    val idKategori = item.child("idKategori").value.toString()
                    if (this@ProdukListActivity.kategoriData.idKategori == idKategori) {
                        totalProduk++
                        val produk = item.getValue(ProdukModel::class.java)
                        dataProduk.add(produk!!)
                    }
                }

                if (dataProduk.isEmpty()) {
                    binding.kosong.visibility = View.VISIBLE
                    binding.rvDaftarProduk.visibility = View.GONE
                } else {
                    binding.kosong.visibility = View.GONE
                    binding.rvDaftarProduk.visibility = View.VISIBLE

                    adapterProduk = AdapterProdukList(dataProduk, { produk ->
                        val ntent = Intent(this@ProdukListActivity, ProdukAddUpdateActivity::class.java)
                            .putExtra("produkData", produk).putExtra("isEditProduk", true).putExtra("kategoriData", kategoriData)
                        startActivity(ntent)
                    }, { produk, isChecked ->
                        Handler().postDelayed({ setVisibility(produk, isChecked) }, 500)
                    })
                    binding.rvDaftarProduk.adapter = adapterProduk
                }

                val totalProdukTxt = "$totalProduk produk"
                binding.tvLimitProduk.text = totalProdukTxt
                if (totalProduk == 0) kategoriRef.child("visibilitas").setValue(false)

                binding.loadingProduk.visibility = View.GONE
                binding.rvDaftarProduk.visibility = View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.loadingProduk.visibility = View.VISIBLE
                binding.rvDaftarProduk.visibility = View.GONE
            }
        }

        produkRef.orderByChild("namaProduk").addValueEventListener(produkListener)
    }

    private fun setVisibility(produk: ProdukModel, isChecked: Boolean) {
        val visibilitas = if (isChecked) "ditampilkan" else "disembunyikan"

        if (HelperConnection.isConnected(this)) {
            produkRef.child("${produk.idProduk}/visibility").setValue(isChecked)
                .addOnSuccessListener { showToast("${produk.namaProduk} $visibilitas", this) }
        }
    }

    private fun showAlertDialog() {
        Helper.showAlertDialog(
            "Hapus Kategori ${kategoriData.namaKategori}?",
            "Kategori ${kategoriData.namaKategori} akan dihapus secara permanen bersama dengan daftar produk didalamnya",
            getString(R.string.hapus_kategori),
            this,
            getColor(R.color.error)
        ) {
            deleteKategoriNProduknya()
            finish()
        }
    }

    private fun deleteKategoriNProduknya() {
        if (HelperConnection.isConnected(this)) {
            produkRef.removeEventListener(produkListener)
            kategoriRef.removeEventListener(kategoriListener)

            val namaKategori = kategoriData.namaKategori
            val idKategori = kategoriData.idKategori
            val kategoriRef = db.getReference("produk")
            val idProdukList = ArrayList<String>()

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (item in snapshot.children) {
                        val idProduk = item.child("idProduk").value.toString()
                        idProdukList.add(idProduk)
                        item.ref.removeValue()
                    }
                    for (idProduk in idProdukList) {
                        val storageReference = storage.getReference("produk/$idProduk.png")
                        storageReference.delete()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            }

            kategoriRef.child("produk").orderByChild("idKategori").equalTo(idKategori).addListenerForSingleValueEvent(listener)
            kategoriRef.child("kategori/${idKategori}").removeValue()
            showToastL("Kategori $namaKategori berhasil dihapus", this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        kategoriRef.removeEventListener(kategoriListener)
        produkRef.orderByChild("namaProduk").removeEventListener(produkListener)
    }
}