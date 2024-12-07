package com.bekado.bekadoonline.view.ui.transaksi

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.data.model.AkunModel
import com.bekado.bekadoonline.data.model.TrxDetailModel
import com.bekado.bekadoonline.databinding.ActivityDetailTransaksiBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.constval.VariableConstant.Companion.EXTRA_ID_TRANSAKSI
import com.bekado.bekadoonline.helper.constval.VariableConstant.Companion.EXTRA_PATH_DTRANSAKSI
import com.bekado.bekadoonline.view.adapter.AdapterCheckout
import com.bekado.bekadoonline.view.ui.bottomsheet.admn.BottomSheetStatusPesanan
import com.bekado.bekadoonline.view.viewmodel.transaksi.DetailTransaksiViewModel
import com.bekado.bekadoonline.view.viewmodel.transaksi.TransaksiViewModelFactory
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModelFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class DetailTransaksiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailTransaksiBinding

    private val userViewModel: UserViewModel by viewModels { UserViewModelFactory.getInstance(this) }
    private val detailTransaksiViewModel: DetailTransaksiViewModel by viewModels { TransaksiViewModelFactory.getInstance() }

    private var extraPathDTransaksi: String? = ""

    private var latitude: String = ""
    private var longitude: String = ""

    private var txtStatusPesananWadmin: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTransaksiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        extraPathDTransaksi = intent?.getStringExtra(EXTRA_PATH_DTRANSAKSI) ?: ""

        dataAkunHandler()
        detailTransaksiHandler()

        binding.appBar.setNavigationOnClickListener { finish() }
        binding.rvDaftarProduk.layoutManager = LinearLayoutManager(this@DetailTransaksiActivity, LinearLayoutManager.VERTICAL, false)
    }

    private fun setStatusPesanan(akunModel: AkunModel) {
        binding.btnUbahStatus.setOnClickListener {
            if (akunModel.statusAdmin) {
                val bsStatusPsnn = BottomSheetStatusPesanan(this, extraPathDTransaksi, detailTransaksiViewModel)
                bsStatusPsnn.showDialog(txtStatusPesananWadmin)

                bsStatusPsnn.dialog.setOnCancelListener {
                    if (bsStatusPsnn.selectedStatus.isNotEmpty() && bsStatusPsnn.selectedParent.isNotEmpty() && bsStatusPsnn.selected) {
                        txtStatusPesananWadmin = bsStatusPsnn.selectedStatus
                        binding.status.text = bsStatusPsnn.selectedStatus
                        binding.tvStatusPesanan.text = bsStatusPsnn.selectedStatus
                    }
                }
            } else showToast(getString(R.string.restricted_non_admin), this)
        }
    }

    private fun dataAkunHandler() {
        userViewModel.getDataAkun().observe(this) { akunModel ->
            binding.containerChangeStatus.isVisible = akunModel?.statusAdmin == true
            if (akunModel != null) {
                setStatusPesanan(akunModel)
                dataAkunOwnerTrxHandler(akunModel)
            } else finish()
        }
    }

    private fun detailTransaksiHandler() {
        detailTransaksiViewModel.isLoading().observe(this) { isLoading -> setupIsLoading(isLoading) }
        detailTransaksiViewModel.getDetailTransaksi(extraPathDTransaksi).observe(this) { detailTransaksi ->
            setupStatusPesanan(detailTransaksi)
            val metodePembayaran = setupRincianPembayaran(detailTransaksi)
            val isTransfer = metodePembayaran == getString(R.string.transfer)

            binding.btnUbahStatus.isEnabled = detailTransaksi?.parentStatus != getString(R.string.key_selesai)
            binding.lihatPembayaran.isEnabled = isTransfer
            binding.rlLihatPembayaran.isVisible = isTransfer

            if (isTransfer) {
                binding.lihatPembayaran.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue_700))
                binding.lihatPembayaran.setOnClickListener {
                    val mIntent = Intent(this@DetailTransaksiActivity, PembayaranActivity::class.java)
                        .putExtra(EXTRA_ID_TRANSAKSI, extraPathDTransaksi)
                    startActivity(mIntent)
                }
            }
        }
        detailTransaksiViewModel.getAlamat().observe(this) { alamatPenerima ->
            if (alamatPenerima != null) {
                binding.namaPenerima.text = alamatPenerima.nama
                binding.noHpPenerima.text = alamatPenerima.noHp

                val fullAddress = "${alamatPenerima.alamatLengkap}, ${alamatPenerima.kodePos}"
                binding.alamatPenerima.text = fullAddress
                latitude = alamatPenerima.latitude.toString()
                longitude = alamatPenerima.longitude.toString()
            }
        }
        detailTransaksiViewModel.getProdukList().observe(this) { dataProduk ->
            binding.subTitleDaftarProduk.isVisible = dataProduk != null

            if (dataProduk != null) {
                dataProduk.sortBy { it.keranjangModel?.timestamp }

                val adapterDaftarProduk = AdapterCheckout(ArrayList(dataProduk))
                binding.rvDaftarProduk.adapter = adapterDaftarProduk

                val buttonText = "+${dataProduk.size - 1} produk lainnya"
                val showD = R.drawable.icon_round_expand_more_24
                val hideD = R.drawable.icon_round_expand_less_24

                binding.buttonShowAll.apply {
                    isGone = dataProduk.size <= 1
                    text = buttonText
                    setIconResource(showD)
                    setOnClickListener {
                        adapterDaftarProduk.setExpanded()
                        binding.buttonShowAll.text = if (adapterDaftarProduk.isExpanded) getString(R.string.tampilkan_lebih_sedikit) else buttonText
                        binding.buttonShowAll.setIconResource(if (adapterDaftarProduk.isExpanded) hideD else showD)
                    }
                }
            }
        }
    }

    private fun setupIsLoading(isLoading: Boolean) {
        binding.progressbarStatusPesanan.isVisible = isLoading
        binding.layoutStatusPesanan.isVisible = !isLoading
        binding.progressbarInformasiPengiriman.isVisible = isLoading
        binding.layoutInformasiPengiriman.isVisible = !isLoading
        binding.progressbarRincianPembayaran.isVisible = isLoading
        binding.layoutRincianPembayaran.isVisible = !isLoading
        binding.progressbarDaftarProduk.isVisible = isLoading
        binding.rvDaftarProduk.isVisible = !isLoading
    }

    private fun dataAkunOwnerTrxHandler(akunModel: AkunModel) {
        detailTransaksiViewModel.getDataAkunOwner().observe(this) { dataAkun ->
            binding.userCard.isVisible = dataAkun != null && akunModel.statusAdmin
            binding.userCardTitle.isVisible = dataAkun != null && akunModel.statusAdmin

            if (dataAkun != null) {
                with(binding) {
                    namaUser.text = dataAkun.nama ?: getString(R.string.tidak_ada_data)
                    noHpUser.text = dataAkun.noHp ?: getString(R.string.tidak_ada_data)
                    noHpUser.isVisible = !dataAkun.noHp.isNullOrEmpty()

                    val fotopp = if (dataAkun.fotoProfil.isNullOrEmpty()) null else dataAkun.fotoProfil
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

    private fun setupStatusPesanan(transaksi: TrxDetailModel?) {
        txtStatusPesananWadmin = transaksi?.statusPesanan

        binding.status.text = transaksi?.statusPesanan ?: getString(R.string.strip)
        binding.noPesanan.text = transaksi?.noPesanan ?: getString(R.string.strip)
        binding.tvStatusPesanan.text = transaksi?.statusPesanan ?: getString(R.string.strip)

        val timeBuy = "${detailTransaksiViewModel.timestampToFormated(transaksi?.timestamp?.toLong() ?: 0)} WIB"
        binding.waktuPembelian.text = timeBuy
    }

    private fun setupRincianPembayaran(transaksi: TrxDetailModel?): String? {
        val rp = transaksi?.currency
        val ongkir = transaksi?.ongkir ?: 0
        val metodePembayaran = transaksi?.metodePembayaran
        val totalBelanja = transaksi?.totalBelanja ?: 0

        val ttlPrdk = "${getString(R.string.total_harga)} (${transaksi?.totalItem} produk)"
        val ttlHrg = rp + Helper.addcoma3digit(transaksi?.totalHarga)
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
}