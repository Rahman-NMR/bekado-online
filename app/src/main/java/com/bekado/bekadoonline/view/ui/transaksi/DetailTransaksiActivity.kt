package com.bekado.bekadoonline.view.ui.transaksi

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.view.adapter.AdapterCheckout
import com.bekado.bekadoonline.view.ui.bottomsheet.admn.BottomSheetStatusPesanan
import com.bekado.bekadoonline.databinding.ActivityDetailTransaksiBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.helper.constval.VariableConstant
import com.bekado.bekadoonline.data.model.DetailTransaksiModel
import com.bekado.bekadoonline.data.model.TransaksiModel
import com.bekado.bekadoonline.data.viewmodel.AkunViewModel
import com.bekado.bekadoonline.data.viewmodel.ClientDataViewModel
import com.bekado.bekadoonline.data.viewmodel.DaftarProdukTransaksiViewModel
import com.bekado.bekadoonline.data.viewmodel.TransaksiDetailViewModel
import com.bekado.bekadoonline.ui.ViewModelFactory
import com.bekado.bekadoonline.view.ui.transaksi.PembayaranActivity.Companion.BuktiDetailTransaksi
import com.bekado.bekadoonline.view.ui.transaksi.PembayaranActivity.Companion.uidnIdtrx
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DetailTransaksiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailTransaksiBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase

    private val akunViewModel: AkunViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private lateinit var transaksiViewModel: TransaksiDetailViewModel
    private lateinit var clientDataViewModel: ClientDataViewModel
    private lateinit var produkViewModel: DaftarProdukTransaksiViewModel

    private lateinit var akunRef: DatabaseReference
    private lateinit var trxRef: DatabaseReference

    private var latitude: String = ""
    private var longitude: String = ""
    private var statusAdmin: Boolean = false
    private var onStartViewActive: String? = ""
    private var keyRefresh: String = ""

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private val onBackInvokedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackPress()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTransaksiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this@DetailTransaksiActivity, onBackInvokedCallback)
        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.getStringExtra(VariableConstant.RESULT_ACTION)

                if (data == VariableConstant.ACTION_REFRESH_UI) detailTransaksiHandler()
            }
        }
//        akunViewModel = ViewModelProvider(this)[AkunViewModel::class.java]
        transaksiViewModel = ViewModelProvider(this)[TransaksiDetailViewModel::class.java]
        clientDataViewModel = ViewModelProvider(this)[ClientDataViewModel::class.java]
        produkViewModel = ViewModelProvider(this)[DaftarProdukTransaksiViewModel::class.java]

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        detailTransaksiHandler()

        with(binding) {
            appBar.setNavigationOnClickListener { onBackPress() }
            rvDaftarProduk.layoutManager = layoutManager
            btnUbahStatus.isEnabled = !(tvStatusPesanan.text == getString(R.string.status_selesai)
                    || tvStatusPesanan.text == getString(R.string.status_dibatalkan))
        }
    }

    private fun setStatusPesanan(path: String) {
        binding.btnUbahStatus.setOnClickListener {
            val bsStatusPsnn = BottomSheetStatusPesanan(this)
            bsStatusPsnn.showDialog(this, trxRef.child(path), onStartViewActive)

            bsStatusPsnn.dialog.setOnCancelListener {
                if (bsStatusPsnn.selectedStatus.isNotEmpty() && bsStatusPsnn.selectedParent.isNotEmpty() && bsStatusPsnn.selected) {
                    onStartViewActive = bsStatusPsnn.selectedStatus
                    binding.status.text = bsStatusPsnn.selectedStatus
                    binding.tvStatusPesanan.text = bsStatusPsnn.selectedStatus
                    keyRefresh = VariableConstant.REFRESH_DATA

                    binding.btnUbahStatus.isEnabled = !(binding.status.text == getString(R.string.status_selesai)
                            || binding.status.text == getString(R.string.status_dibatalkan))
                }
            }
        }
    }

    private fun detailTransaksiHandler() {
//        akunViewModel.loadCurrentUser()
        akunViewModel.loadAkunData()

//        akunViewModel.currentUser.observe(this) { if (it == null) finish() }
        akunViewModel.akunModel.observe(this) { akunModel ->
            if (akunModel != null) {
                binding.containerChangeStatus.visibility = if (akunModel.statusAdmin) View.VISIBLE else View.GONE

                val ref = if (akunModel.statusAdmin) "transaksi" else "transaksi/${akunModel.uid}/${detailTransaksi.idTransaksi}"
                akunRef = db.getReference("akun/${akunModel.uid}")
                if (!akunModel.statusAdmin) uidnIdtrx = "${akunModel.uid}/${detailTransaksi.idTransaksi}"

                trxRef = db.getReference(ref)
                statusAdmin = akunModel.statusAdmin

                transaksiViewModel.loadDetailTransaksi(trxRef, akunModel.statusAdmin, detailTransaksi.idTransaksi)
                getDataProduk(akunModel.statusAdmin, akunModel.uid)
            } else {
                akunRef = db.getReference("akun")
                trxRef = db.getReference("transaksi")
                finish()
            }
        }
        transaksiViewModel.isLoading.observe(this) { setupIsLoadingTransaksi(it) }
        transaksiViewModel.detailTransaksi.observe(this) { transaksi ->
            if (transaksi != null) {
                setupStatusPesanan(transaksi)
                setupAlamatPenerima(transaksi)

                val metodePembayaran = setupRincianPembayaran(transaksi)
                val isTransfer = metodePembayaran == getString(R.string.transfer)

                binding.lihatPembayaran.isEnabled = isTransfer
                if (isTransfer) {
                    binding.rlLihatPembayaran.visibility = View.VISIBLE
                    binding.lihatPembayaran.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue_700))
                    binding.lihatPembayaran.setOnClickListener {
                        BuktiDetailTransaksi = detailTransaksi

                        resultLauncher.launch(Intent(this@DetailTransaksiActivity, PembayaranActivity::class.java))
                    }
                }
            }
        }
        transaksiViewModel.uidClient.observe(this) { uid ->
            if (uid != null) {
                uidnIdtrx = "$uid/${detailTransaksi.idTransaksi}"
                clientDataViewModel.loadDataClient(db.getReference("akun/$uid"))
                setStatusPesanan("$uid/${detailTransaksi.idTransaksi}")
            }
        }
        clientDataViewModel.isLoading.observe(this) { binding.rlLihatPembayaran.visibility = if (!it) View.VISIBLE else View.GONE }
        clientDataViewModel.dataAkun.observe(this) { data ->
            binding.userCard.visibility = if (data != null) View.VISIBLE else View.GONE
            binding.userCardTitle.visibility = if (data != null) View.VISIBLE else View.GONE

            if (data != null) {
                with(binding) {
                    namaUser.text = data.nama ?: getString(R.string.tidak_ada_data)
                    noHpUser.text = data.noHp ?: getString(R.string.tidak_ada_data)

                    val fotopp = if (data.fotoProfil.isNullOrEmpty()) null else data.fotoProfil
                    Glide.with(this@DetailTransaksiActivity).load(fotopp)
                        .apply(RequestOptions()).centerCrop()
                        .placeholder(R.drawable.img_placeholder_profil)
                        .fallback(R.drawable.img_fallback_profil)
                        .error(R.drawable.img_error_profil)
                        .into(fotoProfil)

                    openMaps.setOnClickListener {
                        val gmmIntentUri = Uri.parse("geo:0,0?q=$latitude,$longitude")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")

                        startActivity(mapIntent)
                    }
                }
            }
        }
    }

    private fun setupIsLoadingTransaksi(isLoading: Boolean) {
        binding.progressbarStatusPesanan.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.layoutStatusPesanan.visibility = if (!isLoading) View.VISIBLE else View.GONE
        binding.progressbarInformasiPengiriman.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.layoutInformasiPengiriman.visibility = if (!isLoading) View.VISIBLE else View.GONE
        binding.progressbarRincianPembayaran.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.layoutRincianPembayaran.visibility = if (!isLoading) View.VISIBLE else View.GONE
    }

    private fun setupStatusPesanan(transaksi: DetailTransaksiModel) {
        onStartViewActive = transaksi.statusPesanan

        binding.status.text = transaksi.statusPesanan
        binding.noPesanan.text = transaksi.noPesanan
        if (statusAdmin) binding.tvStatusPesanan.text = transaksi.statusPesanan

        val timeBuy = "${convertTstmp(transaksi.timestamp?.toLong() ?: 0)} WIB"
        binding.waktuPembelian.text = timeBuy
    }

    private fun setupAlamatPenerima(transaksi: DetailTransaksiModel) {
        val alamatPenerima = transaksi.alamatModel
        if (alamatPenerima != null) {
            binding.namaPenerima.text = alamatPenerima.nama
            binding.noHpPenerima.text = alamatPenerima.noHp

            val fullAddress = "${alamatPenerima.alamatLengkap}, ${alamatPenerima.kodePos}"
            binding.alamatPenerima.text = fullAddress
            latitude = alamatPenerima.latitude.toString()
            longitude = alamatPenerima.longitude.toString()
        }
    }

    private fun setupRincianPembayaran(transaksi: DetailTransaksiModel): String? {
        val rp = transaksi.currency
        val ongkir = transaksi.ongkir ?: 0
        val metodePembayaran = transaksi.metodePembayaran
        val totalBelanja = transaksi.totalBelanja ?: 0

        val ttlPrdk = "${getString(R.string.total_harga)} (${transaksi.totalItem} produk)"
        val ttlHrg = rp + Helper.addcoma3digit(transaksi.totalHarga)
        val ongkr = if (ongkir > 0) rp + Helper.addcoma3digit(ongkir) else "Gratis"
        val ttlBlnj = if (totalBelanja >= 1) rp + Helper.addcoma3digit(totalBelanja) else "Gratis"

        with(binding) {
            xProduk.text = ttlPrdk
            totalHarga.text = ttlHrg
            pembayaranMetodeTxt.text = metodePembayaran
            ongkirHarga.text = ongkr
            totalBelanjaHarga.text = ttlBlnj
        }
        return metodePembayaran
    }

    private fun getDataProduk(isAdmin: Boolean, uidNow: String?) {
        val reference = if (!isAdmin) "transaksi/$uidNow" else "transaksi"

        produkViewModel.loadDaftarProduk(db.getReference(reference), isAdmin, detailTransaksi.idTransaksi)
        produkViewModel.dataProduk.observe(this) { dataProduk ->
            if (dataProduk != null) {
                dataProduk.sortBy { it.keranjangModel?.timestamp }
                val adapterDaftarProduk = AdapterCheckout(ArrayList(dataProduk))
                binding.rvDaftarProduk.adapter = adapterDaftarProduk

                val txt = "+${dataProduk.size - 1} produk lainnya"
                val showD = R.drawable.icon_round_expand_more_24
                val hideD = R.drawable.icon_round_expand_less_24

                binding.buttonShowAll.apply {
                    visibility = if (dataProduk.size <= 1) View.GONE else View.VISIBLE
                    text = txt
                    setIconResource(showD)
                    setOnClickListener {
                        adapterDaftarProduk.setExpanded()
                        if (adapterDaftarProduk.isExpanded) {
                            binding.buttonShowAll.text = getString(R.string.tampilkan_lebih_sedikit)
                            binding.buttonShowAll.setIconResource(hideD)
                        } else {
                            binding.buttonShowAll.text = txt
                            binding.buttonShowAll.setIconResource(showD)
                        }
                    }
                }
            }
        }
        produkViewModel.isLoading.observe(this) { isLoading ->
            binding.progressbarDaftarProduk.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.rvDaftarProduk.visibility = if (!isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun convertTstmp(trxTimestamp: Long): String {
        val sdfTanggal = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale.getDefault())
        Calendar.getInstance().timeInMillis = trxTimestamp

        return sdfTanggal.format(Date(trxTimestamp))
    }

    private fun onBackPress() {
        val resultIntent = Intent().apply {
            putExtra(VariableConstant.RESULT_ACTION, keyRefresh)
            putExtra(VariableConstant.UPDATE_TRANSACTION, detailTransaksi.noPesanan)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    companion object {
        var detailTransaksi: TransaksiModel = TransaksiModel()
    }
}