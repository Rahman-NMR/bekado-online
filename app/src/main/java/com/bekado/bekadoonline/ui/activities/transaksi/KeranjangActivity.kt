package com.bekado.bekadoonline.ui.activities.transaksi

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.adapter.AdapterKeranjang
import com.bekado.bekadoonline.data.model.CombinedKeranjangModel
import com.bekado.bekadoonline.data.viewmodel.AkunViewModel
import com.bekado.bekadoonline.data.viewmodel.KeranjangViewModel
import com.bekado.bekadoonline.databinding.ActivityKeranjangBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.helper.Helper.addcoma3digit
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.helper.constval.VariableConstant
import com.bekado.bekadoonline.helper.itemDecoration.GridSpacing
import com.bekado.bekadoonline.ui.ViewModelFactory
import com.bekado.bekadoonline.ui.activities.transaksi.CheckOutActivity.Companion.selectedProduk
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class KeranjangActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKeranjangBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase

    private lateinit var adapterKeranjang: AdapterKeranjang
    private lateinit var adapterKeranjangHide: AdapterKeranjang

    private lateinit var akunRef: DatabaseReference
    private lateinit var keranjangRef: DatabaseReference
    private lateinit var produkRef: DatabaseReference
    private lateinit var kategoriRef: DatabaseReference

    private val akunViewModel: AkunViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private lateinit var keranjangViewModel: KeranjangViewModel
    private lateinit var resultCheckout: ActivityResultLauncher<Intent>

    private var available = false
    private var unavailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeranjangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()

        keranjangRef = db.getReference("keranjang/${auth.currentUser?.uid}")
        produkRef = db.getReference("produk/produk")
        kategoriRef = db.getReference("produk/kategori")

//        akunViewModel = ViewModelProvider(this)[AkunViewModel::class.java]
        keranjangViewModel = ViewModelProvider(this)[KeranjangViewModel::class.java]
        resultCheckout = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data?.getStringExtra(VariableConstant.RESULT_ACTION)
                if (data == VariableConstant.ACTION_REFRESH_UI) viewModelLoader()
            }
        }

        val lmActive = LinearLayoutManager(this@KeranjangActivity, LinearLayoutManager.VERTICAL, false)
        val lmNonActive = LinearLayoutManager(this@KeranjangActivity, LinearLayoutManager.VERTICAL, false)
        val padding = resources.getDimensionPixelSize(R.dimen.smalldp)

        with(binding) {
            appBar.setNavigationOnClickListener { finish() }

            rvDaftarPesanan.layoutManager = lmActive
            rvDaftarPesanan.addItemDecoration(GridSpacing(1, padding, false))
            rvDaftarPesananHide.layoutManager = lmNonActive
            rvDaftarPesananHide.addItemDecoration(GridSpacing(1, padding, false))

            swipeRefresh.setOnRefreshListener {
                if (HelperConnection.isConnected(this@KeranjangActivity)) setDataHandler()
                binding.swipeRefresh.isRefreshing = false
            }
        }

        setDataHandler()
    }

    private fun setDataHandler() {
        viewModelLoader()
        setAkunObserve()
        setupAdapter()
        setKeranjangOberve()
    }

    private fun setAkunObserve() {
        akunViewModel.currentUser.observe(this) { if (it == null) finish() }
        akunViewModel.akunModel.observe(this) { akunModel ->
            akunRef = if (akunModel != null) db.getReference("akun/${akunModel.uid}") else db.getReference("akun")
            if (akunModel?.statusAdmin == true) finish()
        }
    }

    private fun setupAdapter() {
        adapterKeranjang = AdapterKeranjang({ itemKeranjang, isChecked ->
            itemKeranjang.keranjangModel?.diPilih = isChecked

            val ref = keranjangRef.child("${itemKeranjang.produkModel?.idProduk}")
            keranjangViewModel.updateCheckedItem(ref, isChecked)
        }, { itemKeranjang ->
            if (HelperConnection.isConnected(this))
                actionDelete(itemKeranjang, true)
        }, { item, isPlus ->
            if (HelperConnection.isConnected(this))
                keranjangViewModel.updateItemCount(keranjangRef.child("${item.produkModel?.idProduk}"), isPlus)
        })
        adapterKeranjangHide = AdapterKeranjang({ _, _ -> }, { itemKeranjang ->
            if (HelperConnection.isConnected(this))
                actionDelete(itemKeranjang, false)
        }, { _, _ -> })
    }

    private fun setKeranjangOberve() {
        keranjangViewModel.keranjangModel.observe(this) { keranjang ->
            adapterKeranjang.submitList(keranjang)
            updateTotalHarga(keranjang)

            binding.rvDaftarPesanan.adapter = adapterKeranjang
            binding.rvDaftarPesanan.visibility = if (!keranjang.isNullOrEmpty()) View.VISIBLE else View.GONE

            if (keranjang != null) {
                val selectedProduk = keranjang.filter { it.keranjangModel?.diPilih ?: false } as ArrayList
                binding.btnPesanSekarang.setOnClickListener { checkout(selectedProduk) }
            }

            available = keranjang?.isNotEmpty() ?: false
        }
        keranjangViewModel.keranjangModelHide.observe(this) { keranjangHide ->
            adapterKeranjangHide.submitList(keranjangHide)

            binding.rvDaftarPesananHide.adapter = adapterKeranjangHide
            binding.rvDaftarPesananHide.visibility = if (!keranjangHide.isNullOrEmpty()) View.VISIBLE else View.GONE
            binding.llProdukNoProses.visibility = if (!keranjangHide.isNullOrEmpty()) View.VISIBLE else View.GONE

            binding.btnDelallProdukDihide.setOnClickListener { showAlertDialog(keranjangHide?.size ?: 0, true, keranjangHide) }

            unavailable = keranjangHide?.isNotEmpty() ?: false
        }
        keranjangViewModel.isLoading.observe(this) { isLoading ->
            with(binding) {
                loadingIndicator.visibility = if (!isLoading) View.GONE else View.VISIBLE
                if (!isLoading) {
                    keranjangKosong.visibility = if (!available && !unavailable) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun checkout(dataKeranjang: ArrayList<CombinedKeranjangModel>) {
        if (dataKeranjang.isNotEmpty()) {
            selectedProduk = dataKeranjang
            resultCheckout.launch(Intent(this, CheckOutActivity::class.java))
        } else showToast(getString(R.string.pilih_produk_dulu), this)
    }

    private fun updateTotalHarga(dataKeranjang: ArrayList<CombinedKeranjangModel>?) {
        val selectedItems = dataKeranjang?.filter { it.keranjangModel?.diPilih ?: false }
        val sumPrice: Long = selectedItems?.sumOf {
            val hargaInt = it.produkModel?.hargaProduk ?: 0
            val jumlahProduk = it.keranjangModel?.jumlahProduk ?: 0
            hargaInt * jumlahProduk
        } ?: 0
        val totalItem = selectedItems?.count() ?: 0

        val selectedKeranjang = selectedItems?.isNotEmpty() ?: false
        val txtDelDiPilih = "$totalItem produk terpilih"
        val btnTxt = "${getString(R.string.pesan_sekarang)} ($totalItem)"
        val ttlHrgBlnj = "Rp${addcoma3digit(sumPrice)}"

        with(binding) {
            totalHarga.text = if (selectedKeranjang) ttlHrgBlnj else getString(R.string.strip)
            llProdukSelected.visibility = if (!selectedKeranjang) View.GONE else View.VISIBLE
            btnDeleteDiCeklis.visibility = if (!selectedKeranjang) View.GONE else View.VISIBLE
            xProdukTerpilih.text = txtDelDiPilih
            btnPesanSekarang.isEnabled = selectedKeranjang
            btnPesanSekarang.text = if (selectedKeranjang) btnTxt else getString(R.string.pesan_sekarang)

            btnDeleteDiCeklis.setOnClickListener { showAlertDialog(totalItem, false, dataKeranjang) }
        }
    }

    private fun actionDelete(itemKeranjang: CombinedKeranjangModel, isShown: Boolean) {
        val path = "${itemKeranjang.produkModel?.idProduk}"
        keranjangRef.child(path).removeValue()

        val cancelAction = {
            itemKeranjang.keranjangModel?.let { keranjangModel ->
                val restoreData = mapOf(
                    "idProduk" to itemKeranjang.produkModel?.idProduk,
                    "jumlahProduk" to keranjangModel.jumlahProduk,
                    "timestamp" to keranjangModel.timestamp,
                    "diPilih" to keranjangModel.diPilih
                )

                keranjangRef.child(path).setValue(restoreData)
            }
        }
        val textSnackbar = "${itemKeranjang.produkModel?.namaProduk} dihapus dari keranjang"
        val snackbar = Snackbar.make(binding.root, textSnackbar, Snackbar.LENGTH_SHORT)

        snackbar.anchorView = binding.llContainerPesanan
        if (isShown) snackbar.setAction("Batalkan") { cancelAction() }
        snackbar.show()
    }

    private fun showAlertDialog(totalItem: Int, deleteHide: Boolean, keranjang: ArrayList<CombinedKeranjangModel>?) {
        val title = if (!deleteHide) "Hapus $totalItem produk?" else "Hapus $totalItem produk yang tidak dapat diproses?"
        val msg = if (!deleteHide) getString(R.string.hapus_produk_dipilih) else getString(R.string.hapus_produk_semua)
        val positifBtn = if (!deleteHide) getString(R.string.hapus) else getString(R.string.hapus_semua)

        Helper.showAlertDialog(title, msg, positifBtn, this, getColor(R.color.error)) { deleteAllSelected(deleteHide, keranjang) }
    }

    private fun deleteAllSelected(deleteHide: Boolean, keranjang: ArrayList<CombinedKeranjangModel>?) {
        val selectedKeranjang = keranjang?.filter { it.keranjangModel?.diPilih ?: false }

        if (HelperConnection.isConnected(this)) {
            if (!deleteHide) selectedKeranjang?.forEach {
                keranjangRef.child("${it.produkModel?.idProduk}")
                    .removeValue().addOnSuccessListener { viewModelLoader() }
            } else keranjang?.forEach {
                keranjangRef.child("${it.produkModel?.idProduk}")
                    .removeValue().addOnSuccessListener { viewModelLoader() }
            }
        }
    }

    private fun viewModelLoader() {
        akunViewModel.loadCurrentUser()
        akunViewModel.loadAkunData()
        keranjangViewModel.loadKeranjangData()
    }

    override fun onDestroy() {
        super.onDestroy()
        keranjangViewModel.clearKeranjangListeners()
    }
}