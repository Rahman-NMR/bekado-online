package com.bekado.bekadoonline.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.adapter.AdapterCheckout
import com.bekado.bekadoonline.databinding.ActivityCheckOutBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.helper.Helper.addcoma3digit
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.Helper.showToastL
import com.bekado.bekadoonline.model.CombinedKeranjangModel
import com.bekado.bekadoonline.ui.profil.AlamatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Date

class CheckOutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCheckOutBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var selectedKeranjang: ArrayList<CombinedKeranjangModel>

    private lateinit var keranjangRef: DatabaseReference
    private lateinit var alamatRef: DatabaseReference
    private lateinit var alamatListener: ValueEventListener

    private var ongkir: Long = 0
    private var jarak: Long = 0
    private var totalHarga: Long = 0
    private var totalItem = 0
    private var totalBelanja: Long = 0
    private val biayaKurir: Long = 50000
    private var metodePembayaran: String = ""

    private var namaPnrm: String = ""
    private var noHpPnrm: String = ""
    private var alamatPnrm: String = ""
    private var kodePosPnrm: String = ""
    private var latitude: String = ""
    private var longitude: String = ""

    private val lati = -7.4547115
    private val longi = 109.258109

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckOutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        selectedKeranjang = intent.getParcelableArrayListExtra("selected_dataKeranjang") ?: ArrayList()
        val currentUid = auth.currentUser?.uid
        keranjangRef = db.getReference("keranjang/$currentUid")
        alamatRef = db.getReference("alamat/$currentUid")

        dataFromIntentExtra()
        getDataProduk()
        getAlamatPenerima()

        with(binding) {
            appBar.setNavigationOnClickListener { onBackPressed() }
            btnUbahAlamat.setOnClickListener { startActivity(Intent(this@CheckOutActivity, AlamatActivity::class.java)) }
            toggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked)
                    when (checkedId) {
                        R.id.btn_pengiriman_transfer -> updateRincianHarga(true)
                        R.id.btn_pengiriman_cod -> updateRincianHarga(false)
                    }
            }
            btnKonfirmasiPesanan.setOnClickListener {
                if (latitude.isNotEmpty() && longitude.isNotEmpty() &&
                    namaPnrm.isNotEmpty() && noHpPnrm.isNotEmpty() &&
                    alamatPnrm.isNotEmpty() && kodePosPnrm.isNotEmpty()
                ) Helper.showAlertDialog(
                    getString(R.string.konfirmasi_pesanan_),
                    getString(R.string.msg_konf_pesanan),
                    getString(R.string.konfirmasi),
                    this@CheckOutActivity,
                    getColor(R.color.blue_grey_700)
                ) { addTransaksi(currentUid) }
                else showToast(getString(R.string.alamat_uncompleate), this@CheckOutActivity)
            }
        }
    }

    private fun getAlamatPenerima() {
        alamatListener = object : ValueEventListener {
            override fun onDataChange(snapshotAlamat: DataSnapshot) {
                if (snapshotAlamat.exists()) {
                    namaPnrm = snapshotAlamat.child("nama").value?.toString() ?: ""
                    noHpPnrm = snapshotAlamat.child("noHp").value?.toString() ?: ""
                    alamatPnrm = snapshotAlamat.child("alamatLengkap").value?.toString() ?: ""
                    kodePosPnrm = snapshotAlamat.child("kodePos").value?.toString() ?: ""

                    val penerima = if (namaPnrm.isNotEmpty() && noHpPnrm.isNotEmpty()) "$namaPnrm - $noHpPnrm"
                    else getString(R.string.tidak_ada_data)
                    val address = if (alamatPnrm.isNotEmpty() && kodePosPnrm.isNotEmpty()) "$alamatPnrm, $kodePosPnrm"
                    else getString(R.string.tidak_ada_data)

                    binding.namaNohp.text = penerima
                    binding.alamat.text = address

                    latitude = snapshotAlamat.child("latitude").value?.toString() ?: ""
                    longitude = snapshotAlamat.child("longitude").value?.toString() ?: ""

                    val endDrawable = if (latitude.isNotEmpty() && longitude.isNotEmpty()) R.drawable.icon_round_task_alt_24 else 0
                    binding.alamat.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, endDrawable, 0)

                    if (latitude.isNotEmpty() && longitude.isNotEmpty()) {
                        val distance = Helper.calcDistance(latitude.toDouble(), longitude.toDouble(), lati, longi)
                        val jarakPerKm = String.format("%.0f", distance)
                        val jarakT = if (distance < 1) {
                            val distanceInMeter = (distance * 1000).toInt()
                            "$distanceInMeter m"
                        } else String.format("%.1f km", distance)
                        val jarakOngkirTxt = "${getString(R.string.total_ongkos_kirim)} ($jarakT)"

                        binding.ongkirTxt.text = jarakOngkirTxt
                        jarak = jarakPerKm.toLong()
                        //todo:kalo ada kebijakan baru dari pihak toko; ongkir = biayaKurir * jarak
                        ongkir = if (jarak < 1) 0 else biayaKurir
                    }

                    updateRincianHarga(true)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        alamatRef.addValueEventListener(alamatListener)
    }

    private fun dataFromIntentExtra() {
        for (keranjang in selectedKeranjang) {
            val hargaInt = keranjang.produkModel?.hargaProduk!!
            val jumlahHarga = hargaInt * keranjang.keranjangModel?.jumlahProduk!!
            totalHarga += jumlahHarga
            totalItem++
        }
        val itemTxt = "${getString(R.string.total_harga)} ($totalItem produk)"
        val hargaTxt = "Rp${addcoma3digit(totalHarga)}"

        binding.xProduk.text = itemTxt
        binding.totalHarga.text = hargaTxt
    }

    private fun getDataProduk() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvDaftarProduk.layoutManager = layoutManager

        val adapterCheckout = AdapterCheckout(selectedKeranjang)
        binding.rvDaftarProduk.adapter = adapterCheckout
    }

    private fun updateRincianHarga(transfer: Boolean) {
        totalBelanja = totalHarga + ongkir
        val belanjaTxt = if (totalBelanja > 0) "Rp${addcoma3digit(totalHarga + ongkir)}" else "Gratis"
        val ongkirTxt =
            if (latitude.isNotEmpty() && longitude.isNotEmpty())
                if (jarak > 0) "Rp${addcoma3digit(ongkir)}" else "Gratis"
            else getString(R.string.strip)
        metodePembayaran = if (transfer) getString(R.string.transfer) else getString(R.string.cod)

        binding.totalBelanjaHarga.text = belanjaTxt
        binding.ongkirHarga.text = ongkirTxt
        binding.pembayaranMetodeTxt.text = metodePembayaran
    }

    private fun addTransaksi(uidNow: String?) {
        val transaksiRef = db.getReference("transaksi/$uidNow")
        val idTransaksi = transaksiRef.push().key.toString()
        val trxChildRef = transaksiRef.child(idTransaksi)
        val currentTime = Date().time.toString()
        val noPesanan = generateIdPesanan(currentTime)

        val produkMap = mutableMapOf<String, Any>()
        val dataTransaksi = hashMapOf(
            "idTransaksi" to idTransaksi,
            "noPesanan" to noPesanan,
            "timestamp" to currentTime,
            "parentStatus" to getString(R.string.key_antrian),
            "statusPesanan" to getString(R.string.status_menunggu_pembayaran),

            "metodePembayaran" to metodePembayaran,
            "currency" to "Rp",
            "totalBelanja" to totalBelanja,
            "ongkir" to ongkir,

            "totalHarga" to totalHarga,
            "totalItem" to totalItem
        )
        val dataAlamat = hashMapOf(
            "alamatLengkap" to alamatPnrm,
            "kodePos" to kodePosPnrm,
            "latitude" to latitude,
            "longitude" to longitude,
            "nama" to namaPnrm,
            "noHp" to noHpPnrm
        )
        if (selectedKeranjang.isNotEmpty()) {
            for (produk in selectedKeranjang) {
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

        trxChildRef.setValue(dataTransaksi).addOnSuccessListener {
            showToastL(getString(R.string.pesanan_baru_berhasil), this)
            trxChildRef.child("alamatPenerima").setValue(dataAlamat)
            trxChildRef.child("produkList").setValue(produkMap)
                .addOnSuccessListener {
                    selectedKeranjang.forEach { keranjangRef.child("${it.produkModel?.idProduk}").removeValue() }

                    if (metodePembayaran == getString(R.string.transfer)) {
                        val flag = Intent(this, PembayaranActivity::class.java)
                        flag.putExtra("statusAdmin", false)
                        flag.putExtra("totalBelanjaK", "Rp.${addcoma3digit(totalBelanja)}")
                        flag.putExtra("pathTrx", "$uidNow/$idTransaksi")
                        flag.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

                        startActivity(flag)
                    }
                    finish()
                }
        }
    }

    private fun generateIdPesanan(currentTime: String): String {
        val idPesanan = "$totalItem$currentTime"
        val timestampCrop = idPesanan.chunked(4).joinToString("/")
        return "INV/$timestampCrop"
    }

    override fun onDestroy() {
        super.onDestroy()
        alamatRef.removeEventListener(alamatListener)
    }

    override fun onStart() {
        super.onStart()
        binding.btnPengirimanTransfer.isChecked = true
    }
}