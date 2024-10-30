package com.bekado.bekadoonline.view.ui.transaksi

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.data.model.AlamatModel
import com.bekado.bekadoonline.view.adapter.AdapterCheckout
import com.bekado.bekadoonline.data.model.CombinedKeranjangModel
import com.bekado.bekadoonline.data.model.TrxDetailModel
import com.bekado.bekadoonline.databinding.ActivityCheckOutBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.helper.Helper.addcoma3digit
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.Helper.showToastL
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.view.ui.profil.AlamatActivity
import com.bekado.bekadoonline.view.viewmodel.keranjang.KeranjangViewModel
import com.bekado.bekadoonline.view.viewmodel.keranjang.KeranjangViewModelFactory
import com.bekado.bekadoonline.view.viewmodel.transaksi.CheckoutViewModel
import com.bekado.bekadoonline.view.viewmodel.transaksi.TransaksiViewModelFactory
import com.bekado.bekadoonline.view.viewmodel.user.AlamatViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModelFactory
import java.util.Date
import java.util.Locale

class CheckOutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCheckOutBinding

    private val userViewModel: UserViewModel by viewModels { UserViewModelFactory.getInstance(this) }
    private val addressViewModel: AlamatViewModel by viewModels { UserViewModelFactory.getInstance(this) }
    private val cartViewModel: KeranjangViewModel by viewModels { KeranjangViewModelFactory.getInstance() }
    private val checkoutViewModel: CheckoutViewModel by viewModels { TransaksiViewModelFactory.getInstance() }

    private var ongkir: Long = 0
    private var jarakToBuyer: Long = 0 //todo: hitung ongkir berdasarkan/dikali jarak
    private var totalHarga: Long = 0
    private var totalItem = 0
    private var totalBelanja: Long = 0
    private var metodePembayaran: String = ""

    private val latiStore = -7.4547115
    private val longiStore = 109.258109

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckOutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        dataAkunHandler()
        setupDaftarProduk()
        dataAlamatHandler()
        setupDisplayHarga()

        binding.appBar.setNavigationOnClickListener { finish() }
        binding.btnUbahAlamat.setOnClickListener { startActivity(Intent(this@CheckOutActivity, AlamatActivity::class.java)) }
    }

    private fun dataAkunHandler() {
        userViewModel.getDataAkun().observe(this) { akun ->
            akun?.let { if (it.statusAdmin) finish() } ?: finish()
        }
    }

    private fun dataAlamatHandler() {
        addressViewModel.getDataAlamat().observe(this) { alamatModel ->
            val nama = alamatModel?.nama
            val noHp = alamatModel?.noHp
            val alamatLengkap = alamatModel?.alamatLengkap
            val kodePos = alamatModel?.kodePos
            val latitude = alamatModel?.latitude
            val longitude = alamatModel?.longitude

            updateRincianHarga(true, latitude, longitude)
            toogleButton(latitude, longitude)
            setupUIalamatPenerima(nama, noHp, alamatLengkap, kodePos, latitude, longitude)
            binding.ongkirTxt.text = setupCalcJarak(latitude, longitude)
            validateBeforeConfirm(nama, noHp, alamatLengkap, kodePos, latitude, longitude)
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

    private fun setupUIalamatPenerima(nama: String?, noHp: String?, alamatLengkap: String?, kodePos: String?, latitude: String?, longitude: String?) {
        binding.namaNohp.text = when {
            nama?.isNotEmpty() == true && noHp?.isNotEmpty() == true -> "$nama - $noHp"
            nama?.isNotEmpty() == true -> nama
            noHp?.isNotEmpty() == true -> noHp
            else -> getString(R.string.tidak_ada_data)
        }
        binding.alamat.text = when {
            alamatLengkap?.isNotEmpty() == true && kodePos?.isNotEmpty() == true -> "$alamatLengkap, $kodePos"
            alamatLengkap?.isNotEmpty() == true -> "$alamatLengkap"
            kodePos?.isNotEmpty() == true -> "$kodePos"
            else -> getString(R.string.tidak_ada_data)
        }

        val endDrawable = if (latitude?.isNotEmpty() == true && longitude?.isNotEmpty() == true) R.drawable.icon_round_task_alt_24 else 0
        binding.alamat.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, endDrawable, 0)
    }

    private fun setupCalcJarak(latitude: String?, longitude: String?): String {
        if (latitude?.isNotEmpty() == true && longitude?.isNotEmpty() == true) {
            val distance = Helper.calcDistance(latitude.toDouble(), longitude.toDouble(), latiStore, longiStore)
            val jarakPerKM = String.format(Locale.getDefault(), "%.0f", distance)
            val jarakTxt =
                if (distance < 1) String.format(Locale.getDefault(), "%.0f m", distance * 1000)
                else String.format(Locale.getDefault(), "%.1f km", distance)

            jarakToBuyer = jarakPerKM.toLong() // TODO: release jarak dari toko ke pembeli. kejauhan = skip
            return "${getString(R.string.total_ongkos_kirim)} ($jarakTxt)"
        } else return getString(R.string.total_ongkos_kirim)
    }

    private fun setupDisplayHarga() {
        totalHarga = selectedProduk.sumOf {
            val hargaInt = it.produkModel?.hargaProduk ?: 0
            val jumlahHarga = it.keranjangModel?.jumlahProduk ?: 0
            hargaInt * jumlahHarga
        }
        totalItem = selectedProduk.count()

        val itemTxt = "${getString(R.string.total_harga)} ($totalItem produk)"
        val hargaTxt = "Rp${addcoma3digit(totalHarga)}"

        binding.xProduk.text = itemTxt
        binding.totalHarga.text = hargaTxt
    }

    private fun setupDaftarProduk() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvDaftarProduk.layoutManager = layoutManager

        val adapterCheckout = AdapterCheckout(selectedProduk)
        adapterCheckout.isExpanded = true
        binding.rvDaftarProduk.adapter = adapterCheckout
    }

    private fun updateRincianHarga(transfer: Boolean, latitude: String?, longitude: String?) {
        totalBelanja = totalHarga + ongkir
        val belanjaTxt = if (totalBelanja > 0) "Rp${addcoma3digit(totalHarga + ongkir)}" else "Gratis"
        val ongkirTxt = if (latitude?.isNotEmpty() == true && longitude?.isNotEmpty() == true) "Gratis" else getString(R.string.strip)
        metodePembayaran = if (transfer) getString(R.string.transfer) else getString(R.string.cod)

        binding.totalBelanjaHarga.text = belanjaTxt
        binding.ongkirHarga.text = ongkirTxt
        binding.pembayaranMetodeTxt.text = metodePembayaran
    }

    private fun validateBeforeConfirm(nama: String?, noHp: String?, alamatFull: String?, kodePos: String?, lati: String?, longi: String?) {
        binding.btnKonfirmasiPesanan.setOnClickListener {
            if (HelperConnection.isConnected(this)) {
                if (lati?.isNotEmpty() == true && longi?.isNotEmpty() == true &&
                    nama?.isNotEmpty() == true && noHp?.isNotEmpty() == true &&
                    alamatFull?.isNotEmpty() == true && kodePos?.isNotEmpty() == true
                ) {
                    if (metodePembayaran.isNotEmpty()) {
                        if (totalBelanja >= 50000) {
                            Helper.showAlertDialog(
                                getString(R.string.konfirmasi_pesanan_),
                                getString(R.string.msg_konf_pesanan),
                                getString(R.string.konfirmasi),
                                this@CheckOutActivity,
                                getColor(R.color.blue_grey_700)
                            ) { addTransaksi(nama, noHp, alamatFull, kodePos, lati, longi) }
                        } else showToastL(getString(R.string.syarat_checkout), this@CheckOutActivity)
                    } else showToast(getString(R.string.metode_pembayaran_unselected), this@CheckOutActivity)
                } else showToast(getString(R.string.alamat_uncompleate), this@CheckOutActivity)
            } else showToast(getString(R.string.no_internet_connection), this@CheckOutActivity)
        }
    }

    private fun generateIdPesanan(currentTime: String): String {
        val idPesanan = "$totalItem$currentTime"
        val timestampCrop = idPesanan.chunked(4).joinToString("/")
        return "INV/$timestampCrop"
    }

    private fun addTransaksi(nama: String, noHp: String, alamatFull: String, kodePos: String, lati: String, longi: String) {
        val currentTime = Date().time.toString()
        val noPesanan = generateIdPesanan(currentTime)
        val statusPesananByMetode =
            if (metodePembayaran == getString(R.string.transfer)) getString(R.string.status_menunggu_pembayaran)
            else getString(R.string.status_menunggu_konfirmasi)

        val produkMap = mutableMapOf<String, Any>()
        mapOfProdukList(produkMap)

        val alamatData = AlamatModel(nama, noHp, alamatFull, kodePos, lati, longi)
        val transaksiData = TrxDetailModel(
            currency = "Rp",
            metodePembayaran = metodePembayaran,
            noPesanan = noPesanan,
            ongkir = ongkir,
            parentStatus = getString(R.string.key_antrian),
            statusPesanan = statusPesananByMetode,
            timestamp = currentTime,
            totalBelanja = totalBelanja,
            totalHarga = totalHarga,
            totalItem = totalItem.toLong()
        )

        checkoutViewModel.addNewTransaksi(transaksiData, alamatData, produkMap, totalBelanja) { isSuccessful ->
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

    companion object {
        var selectedProduk = ArrayList<CombinedKeranjangModel>()
    }
}