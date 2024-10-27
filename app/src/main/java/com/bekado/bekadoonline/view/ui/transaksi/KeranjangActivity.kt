package com.bekado.bekadoonline.view.ui.transaksi

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.data.model.CombinedKeranjangModel
import com.bekado.bekadoonline.databinding.ActivityKeranjangBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.helper.Helper.addcoma3digit
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.helper.constval.VariableConstant
import com.bekado.bekadoonline.helper.itemDecoration.GridSpacing
import com.bekado.bekadoonline.view.adapter.AdapterKeranjang
import com.bekado.bekadoonline.view.ui.transaksi.CheckOutActivity.Companion.selectedProduk
import com.bekado.bekadoonline.view.viewmodel.keranjang.KeranjangViewModel
import com.bekado.bekadoonline.view.viewmodel.keranjang.KeranjangViewModelFactory
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModelFactory
import com.google.android.material.snackbar.Snackbar

class KeranjangActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKeranjangBinding
    private lateinit var adapterKeranjang: AdapterKeranjang

    private val userViewModel: UserViewModel by viewModels { UserViewModelFactory.getInstance(this) }
    private val cartViewModel: KeranjangViewModel by viewModels { KeranjangViewModelFactory.getInstance() }

    private lateinit var resultCheckout: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeranjangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        resultCheckout = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data?.getStringExtra(VariableConstant.RESULT_ACTION)
//                if (data == VariableConstant.ACTION_REFRESH_UI) viewModelLoader()
            }
        }

        val layoutManager = LinearLayoutManager(this@KeranjangActivity, LinearLayoutManager.VERTICAL, false)
        val padding = resources.getDimensionPixelSize(R.dimen.smalldp)

        with(binding) {
            appBar.setNavigationOnClickListener { finish() }

            rvDaftarPesanan.layoutManager = layoutManager
            rvDaftarPesanan.addItemDecoration(GridSpacing(1, padding, false))
        }

        dataAkunHandler()
        setupAdapter()
        dataKeranjangHandler()
    }

    private fun dataAkunHandler() {
        userViewModel.getDataAkun().observe(this) { akunModel ->
            if (akunModel == null) finish()
            if (akunModel?.statusAdmin == true) finish()
        }
    }

    private fun setupAdapter() {
        adapterKeranjang = AdapterKeranjang({ itemKeranjang, isChecked ->
            itemKeranjang.keranjangModel?.diPilih = isChecked
            cartViewModel.updateProdukTerpilih(itemKeranjang.produkModel?.idProduk, isChecked) { isSuccessful ->
                if (!isSuccessful) showToast(getString(R.string.gagal_ubah_selected_produk, itemKeranjang.produkModel?.namaProduk), this)
            }
        }, { itemKeranjang ->
            if (HelperConnection.isConnected(this))
                actionDelete(itemKeranjang)
        }, { item, isPlus ->
            if (HelperConnection.isConnected(this))
                cartViewModel.addJumlahProduk(item.produkModel?.idProduk, isPlus) { isSuccessful ->
                    if (!isSuccessful) showToast(getString(R.string.gagal_ubah_jumlah_produk), this)
                }
        })
    }

    private fun dataKeranjangHandler() {
        cartViewModel.getDataKeranjang().observe(this) { keranjang ->
            adapterKeranjang.submitList(keranjang)
            updateTotalHarga(keranjang)

            binding.rvDaftarPesanan.adapter = adapterKeranjang
            binding.rvDaftarPesanan.isVisible = !keranjang.isNullOrEmpty()

            if (keranjang != null) {
                val selectedProduk = keranjang.filter { it.keranjangModel?.diPilih ?: false } as ArrayList
                binding.btnPesanSekarang.setOnClickListener { checkout(selectedProduk) }
            }
        }
        cartViewModel.isLoading().observe(this) { isLoading ->
            with(binding) {
                loadingIndicator.isVisible = isLoading
                if (!isLoading) {
                    keranjangKosong.isVisible = cartViewModel.getDataKeranjang().value.isNullOrEmpty()
                }
            }
        }
    }

    private fun checkout(dataKeranjang: ArrayList<CombinedKeranjangModel>) {
        if (dataKeranjang.isNotEmpty()) {
            selectedProduk = dataKeranjang
            resultCheckout.launch(Intent(this, CheckOutActivity::class.java))
            cartViewModel.clearListener()
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
            llProdukSelected.isGone = !selectedKeranjang
            btnDeleteDiCeklis.isGone = !selectedKeranjang
            xProdukTerpilih.text = txtDelDiPilih
            btnPesanSekarang.isEnabled = selectedKeranjang
            btnPesanSekarang.text = if (selectedKeranjang) btnTxt else getString(R.string.pesan_sekarang)

            btnDeleteDiCeklis.setOnClickListener { openAlertDialog(totalItem, dataKeranjang) }
        }
    }

    private fun actionDelete(itemKeranjang: CombinedKeranjangModel) {
        cartViewModel.deleteThisProduk(itemKeranjang.produkModel?.idProduk) { isSuccessful: Boolean ->
            if (isSuccessful) {
                val textSnackbar = "${itemKeranjang.produkModel?.namaProduk} dihapus dari keranjang"
                val snackbar = Snackbar.make(binding.root, textSnackbar, Snackbar.LENGTH_SHORT)

                snackbar.anchorView = binding.llContainerPesanan
                snackbar.setAction("Batalkan") { cartViewModel.cancelAction(itemKeranjang) }
                snackbar.show()
            } else showToast(getString(R.string.gagal_hapus_produk, ""), this)
        }
    }

    private fun openAlertDialog(totalItem: Int, keranjang: ArrayList<CombinedKeranjangModel>?) {
        Helper.showAlertDialog(
            getString(R.string.hapus_produk_selected, totalItem),
            getString(R.string.hapus_produk_dipilih),
            getString(R.string.hapus),
            this,
            getColor(R.color.error)
        ) {
            val selectedKeranjang = keranjang?.filter { it.keranjangModel?.diPilih == true }

            if (HelperConnection.isConnected(this)) {
                cartViewModel.deleteSelectedProduk(selectedKeranjang) { isSuccessful ->
                    if (!isSuccessful) showToast(getString(R.string.gagal_hapus_produk, " terpilih"), this)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cartViewModel.startListener()
    }
}