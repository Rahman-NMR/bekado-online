package com.bekado.bekadoonline.ui.activities.admn

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.adapter.admn.AdapterProdukList
import com.bekado.bekadoonline.data.model.KategoriModel
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.data.viewmodel.KategoriProdukListViewModel
import com.bekado.bekadoonline.data.viewmodel.KategoriSingleViewModel
import com.bekado.bekadoonline.databinding.ActivityProdukListBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.Helper.showToastL
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.helper.constval.VariableConstant
import com.bekado.bekadoonline.helper.itemDecoration.GridSpacing
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

    private lateinit var kategoriRef: DatabaseReference
    private var namaKategori: String? = ""

    private lateinit var singleKategori: KategoriSingleViewModel
    private lateinit var produkViewModel: KategoriProdukListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProdukListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        db = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        produkViewModel = ViewModelProvider(this)[KategoriProdukListViewModel::class.java]
        singleKategori = ViewModelProvider(this)[KategoriSingleViewModel::class.java]

        monitorKategori()
        setupAdapter()
        handlerDataProduk()

        val padding = resources.getDimensionPixelSize(R.dimen.normaldp)
        binding.rvDaftarProduk.layoutManager = LinearLayoutManager(this@ProdukListActivity, LinearLayoutManager.VERTICAL, false)
        binding.rvDaftarProduk.addItemDecoration(GridSpacing(1, padding, false))

        binding.appBar.setNavigationOnClickListener { finish() }
    }

    private fun monitorKategori() {
        dataKategoriModel?.idKategori?.let { singleKategori.loadKategoriProduk(it) }

        singleKategori.isLoading.observe(this) { binding.loadingKategori.visibility = if (it) View.VISIBLE else View.GONE }
        singleKategori.kategoriModel.observe(this) { kategori ->
            kategoriRef = db.getReference("produk/kategori/${kategori?.idKategori}")

            binding.tvKategoriSekarang.text = kategori?.namaKategori ?: getString(R.string.strip)
            binding.fabTambahProduk.visibility = if (kategori != null) View.VISIBLE else View.GONE
            binding.btnHapusKategori.visibility = if (kategori != null) View.VISIBLE else View.GONE
            binding.btnHapusKategori.setOnClickListener { if (kategori != null) showAlertDialog(kategori) }

            if (kategori != null) {
                namaKategori = kategori.namaKategori
                produkViewModel.loadKategoriProduk(kategori.idKategori)

                binding.fabTambahProduk.setOnClickListener {
                    val intentExtra = Intent(this@ProdukListActivity, ProdukAddUpdateActivity::class.java)
                        .putExtra(VariableConstant.ID_PRODUCT, "")
                    ProdukAddUpdateActivity.dataKategoriModel = kategori

                    startActivity(intentExtra)
                }
            } else {
                showToast(getString(R.string.kategori_terhapus), this@ProdukListActivity)
                finish()
            }
        }
    }

    private fun handlerDataProduk() {
        produkViewModel.produkList.observe(this) { produk ->
            if (produk != null) {
                adapterProduk.submitList(produk)

                val produkHidden = produk.filter { !it.visibility }.size
                val totalProduk = produk.size
                val totalProdukTxt = "$totalProduk produk"

                binding.tvLimitProduk.text = totalProdukTxt
                if (totalProduk == 0 || produkHidden == totalProduk) {
                    kategoriRef.child("visibilitas").setValue(false)
                    showToast("Visibilitas $namaKategori dinonaktifkan", this)
                }

                binding.kosong.visibility = if (produk.isNotEmpty()) View.GONE else View.VISIBLE
                binding.rvDaftarProduk.visibility = if (produk.isNotEmpty()) View.VISIBLE else View.GONE
            } else {
                binding.kosong.visibility = View.VISIBLE
                binding.rvDaftarProduk.visibility = View.GONE
            }
        }
        produkViewModel.isLoading.observe(this) {
            binding.loadingProduk.visibility = if (it) View.VISIBLE else View.GONE
            binding.kosong.text = if (!it) getString(R.string.tidak_ada_data) else "Loading..."
        }
    }

    private fun setupAdapter() {
        adapterProduk = AdapterProdukList({ itemProduk ->
            val intentExtra = Intent(this@ProdukListActivity, ProdukAddUpdateActivity::class.java)
                .putExtra(VariableConstant.ID_PRODUCT, itemProduk.idProduk)
            ProdukAddUpdateActivity.dataKategoriModel = dataKategoriModel

            startActivity(intentExtra)
        }, { itemProduk, isChecked ->
            Handler(Looper.getMainLooper()).postDelayed({ setVisibility(itemProduk, isChecked) }, 500)
        })
        binding.rvDaftarProduk.adapter = adapterProduk
    }

    private fun setVisibility(produk: ProdukModel, isChecked: Boolean) {
        val visibilitas = if (isChecked) "ditampilkan" else "disembunyikan"

        if (HelperConnection.isConnected(this)) {
            db.getReference("produk/produk").child("${produk.idProduk}/visibility").setValue(isChecked)
                .addOnSuccessListener { showToast("${produk.namaProduk} $visibilitas", this) }
        }
    }

    private fun showAlertDialog(kategori: KategoriModel) {
        Helper.showAlertDialog(
            "Hapus Kategori ${kategori.namaKategori}?",
            "Kategori ${kategori.namaKategori} akan dihapus secara permanen bersama dengan daftar produk didalamnya.",
            getString(R.string.hapus_kategori),
            this,
            getColor(R.color.error)
        ) {
            deleteKategoriNProduknya(kategori)
            finish()
        }
    }

    private fun deleteKategoriNProduknya(kategori: KategoriModel) {
        if (HelperConnection.isConnected(this)) {
            val idKategori = kategori.idKategori
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
            showToastL("Kategori ${kategori.namaKategori} berhasil dihapus", this)
        }
    }

    companion object {
        var dataKategoriModel: KategoriModel? = null
    }
}