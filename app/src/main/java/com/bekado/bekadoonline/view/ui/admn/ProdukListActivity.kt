package com.bekado.bekadoonline.view.ui.admn

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.view.adapter.admn.AdapterProdukList
import com.bekado.bekadoonline.data.model.KategoriModel
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.databinding.ActivityProdukListBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.Helper.showToastL
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.helper.constval.VariableConstant.Companion.EXTRA_ID_KATEGORI
import com.bekado.bekadoonline.helper.constval.VariableConstant.Companion.EXTRA_ID_PRODUK
import com.bekado.bekadoonline.helper.itemDecoration.GridSpacing
import com.bekado.bekadoonline.view.viewmodel.admin.AdminViewModelFactory
import com.bekado.bekadoonline.view.viewmodel.admin.KategoriViewModel
import com.bekado.bekadoonline.view.viewmodel.admin.ProdukListViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModelFactory

class ProdukListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProdukListBinding
    private lateinit var adapterProduk: AdapterProdukList

    private val userViewModel: UserViewModel by viewModels { UserViewModelFactory.getInstance(this) }
    private val kategoriViewModel: KategoriViewModel by viewModels { AdminViewModelFactory.getInstance() }
    private val produkListViewModel: ProdukListViewModel by viewModels { AdminViewModelFactory.getInstance() }

    private var extraIdKategori: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProdukListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        extraIdKategori = intent?.getStringExtra(EXTRA_ID_KATEGORI) ?: ""

        dataAkunHandler()
        kategoriHandler()
        setupAdapter()
        produkListHandler()

        val padding = resources.getDimensionPixelSize(R.dimen.normaldp)
        binding.rvDaftarProduk.layoutManager = LinearLayoutManager(this@ProdukListActivity, LinearLayoutManager.VERTICAL, false)
        binding.rvDaftarProduk.addItemDecoration(GridSpacing(1, padding, false))

        binding.appBar.setNavigationOnClickListener { finish() }
    }

    private fun dataAkunHandler() {
        userViewModel.getDataAkun().observe(this) { akun ->
            akun?.let { if (!it.statusAdmin) finish() } ?: finish()
        }
    }

    private fun kategoriHandler() {
        kategoriViewModel.getDataKategori(extraIdKategori).observe(this) { kategori ->
            binding.tvKategoriSekarang.text = kategori?.namaKategori ?: getString(R.string.strip)
            binding.fabTambahProduk.isVisible = kategori != null
            binding.btnHapusKategori.isVisible = kategori != null
            binding.btnHapusKategori.setOnClickListener { if (kategori != null) showAlertDialog(kategori) }

            if (kategori != null) {
                binding.fabTambahProduk.setOnClickListener {
                    val intentExtra = Intent(this@ProdukListActivity, ProdukAddUpdateActivity::class.java)
                        .putExtra(EXTRA_ID_PRODUK, "")
                        .putExtra(EXTRA_ID_KATEGORI, extraIdKategori)

                    startActivity(intentExtra)
                }
            } else {
                showToast(getString(R.string.kategori_terhapus), this@ProdukListActivity)
                finish()
            }
        }
        kategoriViewModel.isLoading().observe(this) { binding.loadingKategori.isVisible = it }
    }

    private fun produkListHandler() {
        produkListViewModel.getProdukList(extraIdKategori).observe(this) { produk ->
            adapterProduk.submitList(produk)

            val totalProdukTxt = "${produk?.size ?: 0} produk"
            binding.tvLimitProduk.text = totalProdukTxt

            if (produk != null) {
                binding.kosong.isGone = produk.isNotEmpty()
                binding.rvDaftarProduk.isVisible = produk.isNotEmpty()
            } else {
                binding.kosong.visibility = View.VISIBLE
                binding.rvDaftarProduk.visibility = View.GONE
            }
        }
        produkListViewModel.isLoading().observe(this) { isLoading ->
            binding.loadingProduk.isVisible = isLoading
            binding.kosong.text = if (!isLoading) getString(R.string.msg_beranda_kosong) else getString(R.string.loading)
        }
    }

    private fun setupAdapter() {
        adapterProduk = AdapterProdukList({ itemProduk ->
            val intentExtra = Intent(this@ProdukListActivity, ProdukAddUpdateActivity::class.java)
                .putExtra(EXTRA_ID_PRODUK, itemProduk.idProduk)

            startActivity(intentExtra)
        }, { itemProduk, isChecked ->
            Handler(Looper.getMainLooper()).postDelayed({ setVisibility(itemProduk, isChecked) }, 300)
        })
        binding.rvDaftarProduk.adapter = adapterProduk
    }

    private fun setVisibility(produk: ProdukModel, isChecked: Boolean) {
        val visibilitas = if (isChecked) "ditampilkan" else "disembunyikan"

        if (HelperConnection.isConnected(this)) {
            produkListViewModel.updateVisibilitasProduk(produk.idProduk, isChecked) { isSuccessful ->
                if (isSuccessful) showToast("${produk.namaProduk} $visibilitas", this)
                else showToast(getString(R.string.tidak_dapat_menampilkan_x, produk.namaProduk), this)
            }
        }
    }

    private fun showAlertDialog(kategori: KategoriModel) {
        Helper.showAlertDialog(
            "Hapus Kategori ${kategori.namaKategori}?",
            getString(R.string.msg_del_kategori, kategori.namaKategori),
            getString(R.string.hapus_kategori),
            this,
            getColor(R.color.error)
        ) {
            if (HelperConnection.isConnected(this)) {
                kategoriViewModel.deleteKategori(kategori.idKategori) { isSuccessful ->
                    if (isSuccessful) {
                        showToastL("Kategori ${kategori.namaKategori} berhasil dihapus", this)
                        finish()
                    }
                }
            }
        }
    }
}