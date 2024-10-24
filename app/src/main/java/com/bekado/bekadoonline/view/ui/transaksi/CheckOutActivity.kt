package com.bekado.bekadoonline.view.ui.transaksi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.view.adapter.AdapterCheckout
import com.bekado.bekadoonline.data.model.CombinedKeranjangModel
import com.bekado.bekadoonline.data.model.TransaksiModel
import com.bekado.bekadoonline.data.viewmodel.AlamatViewModel
import com.bekado.bekadoonline.databinding.ActivityCheckOutBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.helper.Helper.addcoma3digit
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.Helper.showToastL
import com.bekado.bekadoonline.helper.constval.VariableConstant
import com.bekado.bekadoonline.view.ui.profil.AlamatActivity
import com.bekado.bekadoonline.view.ui.transaksi.PembayaranActivity.Companion.BuktiDetailTransaksi
import com.bekado.bekadoonline.view.ui.transaksi.PembayaranActivity.Companion.uidnIdtrx
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Date
import java.util.Locale

class CheckOutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCheckOutBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase

    private lateinit var keranjangRef: DatabaseReference
    private lateinit var alamatRef: DatabaseReference
    private lateinit var transaksiRef: DatabaseReference
    private lateinit var alamatViewModel: AlamatViewModel

    private var ongkir: Long = 0
    private var jarak: Long = 0
    private var totalHarga: Long = 0
    private var totalItem = 0
    private var totalBelanja: Long = 0
    private var metodePembayaran: String = ""

    private val lati = -7.4547115
    private val longi = 109.258109

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckOutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        alamatViewModel = ViewModelProvider(this)[AlamatViewModel::class.java]

        val currentUid = auth.currentUser?.uid
        keranjangRef = db.getReference("keranjang/$currentUid")
        alamatRef = db.getReference("alamat/$currentUid")
        transaksiRef = db.getReference("transaksi/$currentUid")

        setupDaftarProduk()
        dataAlamatHandler(currentUid)
        setupDisplayHarga()

        binding.appBar.setNavigationOnClickListener { finish() }
        binding.btnUbahAlamat.setOnClickListener { startActivity(Intent(this@CheckOutActivity, AlamatActivity::class.java)) }
    }

    private fun dataAlamatHandler(currentUid: String?) {
        alamatViewModel.loadCurrentUser()
        alamatViewModel.loadAlamatData()

        alamatViewModel.alamatModel.observe(this) { alamatModel ->
            val nama = alamatModel?.nama
            val noHp = alamatModel?.noHp
            val alamatLengkap = alamatModel?.alamatLengkap
            val kodePos = alamatModel?.kodePos
            val latitude = alamatModel?.latitude
            val longitude = alamatModel?.longitude

            updateRincianHarga(true, latitude, longitude)
            toogleButton(latitude, longitude)
            setupUI(nama, noHp, alamatLengkap, kodePos, latitude, longitude)
            setupJarak(latitude, longitude)
            konfirmPesanan(currentUid, nama, noHp, alamatLengkap, kodePos, latitude, longitude)
        }
        alamatViewModel.isLoading.observe(this) { isLoading ->
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

    private fun setupUI(nama: String?, noHp: String?, alamatLengkap: String?, kodePos: String?, latitude: String?, longitude: String?) {
        binding.namaNohp.text =
            if (nama?.isNotEmpty() == true && noHp?.isNotEmpty() == true) "$nama - $noHp"
            else getString(R.string.tidak_ada_data)
        binding.alamat.text =
            if (alamatLengkap?.isNotEmpty() == true && kodePos?.isNotEmpty() == true) "$alamatLengkap, $kodePos"
            else getString(R.string.tidak_ada_data)

        val endDrawable = if (latitude?.isNotEmpty() == true && longitude?.isNotEmpty() == true) R.drawable.icon_round_task_alt_24 else 0
        binding.alamat.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, endDrawable, 0)
    }

    private fun setupJarak(latitude: String?, longitude: String?) {
        if (latitude?.isNotEmpty() == true && longitude?.isNotEmpty() == true) {
            val distance = Helper.calcDistance(latitude.toDouble(), longitude.toDouble(), lati, longi)
            val jarakPerKm = String.format(Locale.getDefault(), "%.0f", distance)
            val jarakT = if (distance < 1) {
                val distanceMeter = (distance * 1000).toInt()
                "$distanceMeter m"
            } else String.format(Locale.getDefault(), "%.1f km", distance)
            val jarakOngkirTxt = "${getString(R.string.total_ongkos_kirim)} ($jarakT)"

            binding.ongkirTxt.text = jarakOngkirTxt
            jarak = jarakPerKm.toLong() // TODO: release jarak dari toko ke pembeli. kejauhan = skip
        } else binding.ongkirTxt.text = getString(R.string.total_ongkos_kirim)
    }

    private fun konfirmPesanan(uidNow: String?, nama: String?, noHp: String?, alamatFull: String?, kodePos: String?, lati: String?, longi: String?) {
        binding.btnKonfirmasiPesanan.setOnClickListener {
            if (lati?.isNotEmpty() == true && longi?.isNotEmpty() == true &&
                nama?.isNotEmpty() == true && noHp?.isNotEmpty() == true &&
                alamatFull?.isNotEmpty() == true && kodePos?.isNotEmpty() == true
            ) if (totalBelanja >= 50000) {
                Helper.showAlertDialog(
                    getString(R.string.konfirmasi_pesanan_),
                    getString(R.string.msg_konf_pesanan),
                    getString(R.string.konfirmasi),
                    this@CheckOutActivity,
                    getColor(R.color.blue_grey_700)
                ) { addTransaksi(uidNow, nama, noHp, alamatFull, kodePos, lati, longi) }
            } else showToastL(getString(R.string.syarat_checkout), this@CheckOutActivity)
            else showToast(getString(R.string.alamat_uncompleate), this@CheckOutActivity)
        }
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

    private fun addTransaksi(uidNow: String?, nama: String, noHp: String, alamatFull: String, kodePos: String, lati: String, longi: String) {
        val idTransaksi = transaksiRef.push().key.toString()
        val trxChildRef = transaksiRef.child(idTransaksi)
        val currentTime = Date().time.toString()
        val noPesanan = generateIdPesanan(currentTime)
        val statusPesananMetode =
            if (metodePembayaran == getString(R.string.transfer)) getString(R.string.status_menunggu_pembayaran)
            else getString(R.string.status_menunggu_konfirmasi)

        val dataBuktiTrx = hashMapOf<String, Any>()
        val produkMap = mutableMapOf<String, Any>()
        val dataTransaksi = hashMapOf(
            "idTransaksi" to idTransaksi,
            "noPesanan" to noPesanan,
            "timestamp" to currentTime,
            "parentStatus" to getString(R.string.key_antrian),
            "statusPesanan" to statusPesananMetode,

            "metodePembayaran" to metodePembayaran,
            "currency" to "Rp",
            "totalBelanja" to totalBelanja,
            "ongkir" to ongkir,

            "totalHarga" to totalHarga,
            "totalItem" to totalItem
        )
        val dataAlamat = hashMapOf(
            "alamatLengkap" to alamatFull,
            "kodePos" to kodePos,
            "latitude" to lati,
            "longitude" to longi,
            "nama" to nama,
            "noHp" to noHp
        )
        mapOfBuktiTransaksi(dataBuktiTrx)
        mapOfProdukList(produkMap)

        trxChildRef.setValue(dataTransaksi).addOnSuccessListener {
            showToastL(getString(R.string.pesanan_baru_berhasil), this)
            trxChildRef.child("alamatPenerima").setValue(dataAlamat)
            trxChildRef.child("buktiTransaksi").setValue(dataBuktiTrx)
            trxChildRef.child("produkList").setValue(produkMap)
                .addOnSuccessListener { actionSuccessListener(idTransaksi, noPesanan, currentTime, statusPesananMetode, uidNow) }
        }
    }

    private fun actionSuccessListener(
        idTransaksi: String,
        noPesanan: String,
        currentTime: String,
        statusPesananMetode: String,
        uidNow: String?
    ) {
        selectedProduk.forEach { keranjangRef.child("${it.produkModel?.idProduk}").removeValue() }

        when (metodePembayaran) {
            getString(R.string.transfer) -> {
                uidnIdtrx = "$uidNow/$idTransaksi"
                BuktiDetailTransaksi = TransaksiModel(
                    idTransaksi = idTransaksi,
                    noPesanan = noPesanan,
                    timestamp = currentTime,
                    statusPesanan = statusPesananMetode,
                    currency = "Rp",
                    totalBelanja = totalBelanja,
                    produkLainnya = totalItem.toLong()
                )

                startActivity(Intent(this, PembayaranActivity::class.java))
                resultRefresh()
                finish()
            }

            else -> {
                resultRefresh()
                finish()
            }
        }
    }

    private fun mapOfBuktiTransaksi(dataBuktiTrx: HashMap<String, Any>) {
        db.getReference("aaabdfiklnrstt").get().addOnSuccessListener { data ->
            dataBuktiTrx["pemilikBank"] = data.child("pemilik").value as String
            dataBuktiTrx["biayaTransfer"] = totalBelanja
            dataBuktiTrx["fotoBank"] = data.child("logoWiki").value as String
            dataBuktiTrx["namaBank"] = data.child("name").value as String
            dataBuktiTrx["noRek"] = data.child("noRek").value as String
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

    private fun generateIdPesanan(currentTime: String): String {
        val idPesanan = "$totalItem$currentTime"
        val timestampCrop = idPesanan.chunked(4).joinToString("/")
        return "INV/$timestampCrop"
    }

    private fun resultRefresh() {
        val resultIntent = Intent().apply {
            putExtra(VariableConstant.RESULT_ACTION, VariableConstant.ACTION_REFRESH_UI)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (auth.currentUser != null) {
            alamatViewModel.removeAlamatListener(alamatRef)
        }
    }

    override fun onStart() {
        super.onStart()
        binding.btnPengirimanTransfer.isChecked = true
    }

    companion object {
        var selectedProduk = ArrayList<CombinedKeranjangModel>()
    }
}