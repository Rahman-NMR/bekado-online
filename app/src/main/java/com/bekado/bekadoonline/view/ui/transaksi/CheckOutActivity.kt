package com.bekado.bekadoonline.view.ui.transaksi

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.data.model.AlamatModel
import com.bekado.bekadoonline.data.model.RincianPembayaranModel
import com.bekado.bekadoonline.data.model.TrxDetailModel
import com.bekado.bekadoonline.databinding.ActivityCheckOutBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.helper.Helper.addcoma3digit
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.Helper.showToastL
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.view.adapter.AdapterCheckout
import com.bekado.bekadoonline.view.ui.profil.AlamatActivity
import com.bekado.bekadoonline.view.viewmodel.keranjang.KeranjangViewModel
import com.bekado.bekadoonline.view.viewmodel.keranjang.KeranjangViewModelFactory
import com.bekado.bekadoonline.view.viewmodel.transaksi.CheckoutViewModel
import com.bekado.bekadoonline.view.viewmodel.transaksi.CheckoutViewModel.Companion.selectedProduk
import com.bekado.bekadoonline.view.viewmodel.transaksi.TransaksiViewModelFactory
import com.bekado.bekadoonline.view.viewmodel.user.AlamatViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModelFactory
import java.util.Date

class CheckOutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCheckOutBinding

    private val userViewModel: UserViewModel by viewModels { UserViewModelFactory.getInstance(this) }
    private val addressViewModel: AlamatViewModel by viewModels { UserViewModelFactory.getInstance(this) }
    private val cartViewModel: KeranjangViewModel by viewModels { KeranjangViewModelFactory.getInstance() }
    private val checkoutViewModel: CheckoutViewModel by viewModels { TransaksiViewModelFactory.getInstance() }

    private var metodePembayaran: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckOutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        dataAkunHandler()
        setupDaftarProduk()
        dataAlamatHandler()

        binding.appBar.setNavigationOnClickListener { finish() }
        binding.btnUbahAlamat.setOnClickListener { startActivity(Intent(this@CheckOutActivity, AlamatActivity::class.java)) }
    }

    private fun dataAkunHandler() {
        userViewModel.getDataAkun().observe(this) { akun ->
            akun?.let { if (it.statusAdmin) finish() } ?: finish()
        }
    }

    private fun dataAlamatHandler() {
        val rincian = checkoutViewModel.rincianHarga(selectedProduk, 0)

        addressViewModel.getDataAlamat().observe(this) { alamatModel ->
            val latitude = alamatModel?.latitude
            val longitude = alamatModel?.longitude

            setupDisplayHarga(rincian)
            updateRincianHarga(true, latitude, longitude)
            toogleButton(latitude, longitude)
            alamatModel?.let { model -> setupUIalamatPenerima(model) }
            alamatModel?.let { model -> validateBeforeConfirm(model, rincian) }
        }
        addressViewModel.isLoading().observe(this) { isLoading ->
            if (isLoading) {
                val loadingTxt = "Loading..."
                binding.namaNohp.text = loadingTxt
                binding.alamat.text = loadingTxt
            }
        }
    }

    private fun toogleButton(latitude: String?, longitude: String?) {
        binding.toggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked)
                when (checkedId) {
                    R.id.btn_pengiriman_transfer -> updateRincianHarga(true, latitude, longitude)
                    R.id.btn_pengiriman_cod -> updateRincianHarga(false, latitude, longitude)
                }
        }
    }

    private fun setupUIalamatPenerima(alamat: AlamatModel) {
        binding.namaNohp.text = when {
            !alamat.nama.isNullOrEmpty() && !alamat.noHp.isNullOrEmpty() -> "${alamat.nama} - ${alamat.noHp}"
            !alamat.nama.isNullOrEmpty() -> alamat.nama
            !alamat.noHp.isNullOrEmpty() -> alamat.noHp
            else -> getString(R.string.tidak_ada_data)
        }
        binding.alamat.text = when {
            !alamat.alamatLengkap.isNullOrEmpty() && !alamat.kodePos.isNullOrEmpty() -> "${alamat.alamatLengkap}, ${alamat.kodePos}"
            !alamat.alamatLengkap.isNullOrEmpty() -> "${alamat.alamatLengkap}"
            !alamat.kodePos.isNullOrEmpty() -> "${alamat.kodePos}"
            else -> getString(R.string.tidak_ada_data)
        }

        val endDrawable = if (!alamat.latitude.isNullOrEmpty() && !alamat.longitude.isNullOrEmpty()) R.drawable.icon_round_task_alt_24 else 0
        binding.alamat.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, endDrawable, 0)
    }

    private fun setupHitungJarak(latitude: String?, longitude: String?): String {
        return "${getString(R.string.total_ongkos_kirim)} " +
                if (!latitude.isNullOrEmpty() && !longitude.isNullOrEmpty()) {
                    "(${checkoutViewModel.hitungJarak(latitude, longitude)})"
                } else ""
    }

    private fun setupDisplayHarga(rincian: RincianPembayaranModel) {
        val itemTxt = "${getString(R.string.total_harga)} (${rincian.totalItem} produk)"
        val hargaTxt = "Rp${addcoma3digit(rincian.totalHarga)}"
        val belanjaTxt = if (rincian.totalBelanja > 0) "Rp${addcoma3digit(rincian.totalHarga + rincian.ongkir)}" else "Gratis"

        binding.xProduk.text = itemTxt
        binding.totalHarga.text = hargaTxt
        binding.totalBelanjaHarga.text = belanjaTxt
    }

    private fun setupDaftarProduk() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvDaftarProduk.layoutManager = layoutManager

        val adapterCheckout = AdapterCheckout(selectedProduk)
        adapterCheckout.isExpanded = true
        binding.rvDaftarProduk.adapter = adapterCheckout
    }

    private fun updateRincianHarga(transfer: Boolean, latitude: String?, longitude: String?) {
        val ongkirTxt = if (!latitude.isNullOrEmpty() && !longitude.isNullOrEmpty()) "Gratis" else getString(R.string.strip)
        metodePembayaran = if (transfer) getString(R.string.transfer) else getString(R.string.cod)

        binding.ongkirTxt.text = setupHitungJarak(latitude, longitude)
        binding.ongkirHarga.text = ongkirTxt
        binding.pembayaranMetodeTxt.text = metodePembayaran
    }

    private fun validateBeforeConfirm(alamatModel: AlamatModel, rincian: RincianPembayaranModel) {
        binding.btnKonfirmasiPesanan.setOnClickListener {
            if (HelperConnection.isConnected(this)) {
                if (!alamatModel.latitude.isNullOrEmpty() && !alamatModel.longitude.isNullOrEmpty() &&
                    !alamatModel.nama.isNullOrEmpty() && !alamatModel.noHp.isNullOrEmpty() &&
                    !alamatModel.alamatLengkap.isNullOrEmpty() && !alamatModel.kodePos.isNullOrEmpty()
                ) {
                    if (metodePembayaran.isNotEmpty()) {
                        if (rincian.totalBelanja >= 50000) {
                            Helper.showAlertDialog(
                                getString(R.string.konfirmasi_pesanan_),
                                getString(R.string.msg_konf_pesanan),
                                getString(R.string.konfirmasi),
                                this@CheckOutActivity,
                                getColor(R.color.blue_grey_700)
                            ) { addTransaksi(alamatModel, rincian) }
                        } else showToastL(getString(R.string.syarat_checkout), this@CheckOutActivity)
                    } else showToast(getString(R.string.metode_pembayaran_unselected), this@CheckOutActivity)
                } else showToast(getString(R.string.alamat_uncompleate), this@CheckOutActivity)
            } else showToast(getString(R.string.no_internet_connection), this@CheckOutActivity)
        }
    }

    private fun addTransaksi(alamatModel: AlamatModel, rincian: RincianPembayaranModel) {
        val currentTime = Date().time.toString()
        val noPesanan = checkoutViewModel.generateIdPesanan(currentTime, rincian.totalItem)
        val statusPesananByMetode =
            if (metodePembayaran == getString(R.string.transfer)) getString(R.string.status_menunggu_pembayaran)
            else getString(R.string.status_menunggu_konfirmasi)

        val produkMap = mutableMapOf<String, Any>()
        mapOfProdukList(produkMap)

        val transaksiData = TrxDetailModel(
            currency = "Rp",
            metodePembayaran = metodePembayaran,
            noPesanan = noPesanan,
            ongkir = rincian.ongkir,
            parentStatus = getString(R.string.key_antrian),
            statusPesanan = statusPesananByMetode,
            timestamp = currentTime,
            totalBelanja = rincian.totalBelanja,
            totalHarga = rincian.totalHarga,
            totalItem = rincian.totalItem.toLong()
        )

        checkoutViewModel.addNewTransaksi(transaksiData, alamatModel, produkMap, rincian.totalBelanja) { isSuccessful ->
            if (isSuccessful) {
                actionSuccessListener()
                showToastL(getString(R.string.pesanan_baru_berhasil), this)
            } else showToastL(getString(R.string.pesanan_baru_gagal), this)
        }
    }

    private fun actionSuccessListener() {
        cartViewModel.deleteSelectedProduk(selectedProduk) { isSuccessful ->
            if (isSuccessful) finish()
            else {
                showToastL(getString(R.string.gagal_hapus_produk, " dari keranjang"), this)
                finish()
            }
        }
    }

    private fun mapOfProdukList(produkMap: MutableMap<String, Any>) {
        if (selectedProduk.isNotEmpty()) {
            for (produk in selectedProduk) {
                val dataProduk = mapOf(
                    "idProduk" to produk.produkModel?.idProduk,
                    "fotoProduk" to produk.produkModel?.fotoProduk,
                    "currency" to produk.produkModel?.currency,
                    "hargaProduk" to produk.produkModel?.hargaProduk,
                    "namaProduk" to produk.produkModel?.namaProduk,
                    "jumlahProduk" to produk.keranjangModel?.jumlahProduk,
                    "timestamp" to produk.keranjangModel?.timestamp
                )
                produkMap[produk.produkModel?.idProduk.toString()] = dataProduk
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.btnPengirimanTransfer.isChecked = true
    }

    override fun onResume() {
        super.onResume()
        if (selectedProduk.isEmpty()) finish()
    }
}