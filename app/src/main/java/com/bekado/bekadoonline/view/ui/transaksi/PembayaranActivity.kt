package com.bekado.bekadoonline.view.ui.transaksi

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.data.model.TrxDetailModel
import com.bekado.bekadoonline.databinding.ActivityPembayaranBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.constval.VariableConstant.Companion.EXTRA_ID_TRANSAKSI
import com.bekado.bekadoonline.view.viewmodel.transaksi.DetailTransaksiViewModel
import com.bekado.bekadoonline.view.viewmodel.transaksi.TransaksiViewModelFactory
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModelFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.File

class PembayaranActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPembayaranBinding

    private val userViewModel: UserViewModel by viewModels { UserViewModelFactory.getInstance(this) }
    private val detailTransaksiViewModel: DetailTransaksiViewModel by viewModels { TransaksiViewModelFactory.getInstance() }

    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private var extraIDtransaksi: String? = ""
    private var imageUri: Uri = Uri.parse("")
    private var status: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPembayaranBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        extraIDtransaksi = intent?.getStringExtra(EXTRA_ID_TRANSAKSI) ?: ""
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val selectedImageUri: Uri? = data?.data
                if (selectedImageUri != null) {
                    setImage(selectedImageUri)
                    imageUri = selectedImageUri
                }
            }
        }
        status = listOf(
            getString(R.string.status_dalam_pengiriman),
            getString(R.string.status_dalam_proses),
            getString(R.string.status_selesai),
            getString(R.string.status_dibatalkan)
        )

        dataAkunHandler()
        dataPembayaranHandler()

        with(binding) {
            appBar.setNavigationOnClickListener { finish() }

            salinNoRek.setOnClickListener { Helper.salinPesan(this@PembayaranActivity, noRek.text) }
            salinNominalTf.setOnClickListener { Helper.salinPesan(this@PembayaranActivity, nominalTf.text) }

            btnUbahImageBukti.setOnClickListener { pilihGambarIntent() }
            btnSimpanBuktPmbyrn.isEnabled = imageUri != Uri.parse("")
        }
    }

    private fun dataAkunHandler() {
        userViewModel.getDataAkun().observe(this) { akun ->
            if (akun != null) {
                val isAdmin = akun.statusAdmin
                val drawableTop = if (isAdmin) 0 else R.drawable.icon_outline_add_photo_alternate_24
                val txtBuktiTrx = if (isAdmin) getString(R.string.belum_upload_bukti) else getString(R.string.tambah_bukti_pembayaran)

                with(binding) {
                    tvBuktiPmbyrn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, drawableTop, 0, 0)
                    tvBuktiPmbyrn.text = txtBuktiTrx
                    btnUbahImageBukti.isVisible = !isAdmin
                    if (!isAdmin) tvBuktiPmbyrn.setOnClickListener { pilihGambarIntent() }
                    btnSimpanBuktPmbyrn.setOnClickListener { uploadImage() }
                }
            } else finish()
        }
    }

    private fun dataPembayaranHandler() {
        detailTransaksiViewModel.getDetailTransaksi(extraIDtransaksi).observe(this) { detail ->
            if (detail != null) {
                if (!detail.metodePembayaran.isNullOrEmpty()) if (detail.metodePembayaran == getString(R.string.cod)) finish()
                if (!detail.statusPesanan.isNullOrEmpty())
                    if (status.any { detail.statusPesanan.contains(it) }) {
                        binding.btnUbahImageBukti.visibility = View.GONE
                        binding.btnSimpanBuktPmbyrn.visibility = View.GONE
                        binding.btnSimpanBuktPmbyrn.setOnClickListener { limitedClickListener(detail) }
                        binding.tvBuktiPmbyrn.setOnClickListener { limitedClickListener(detail) }
                    }
            } else {
                showToast(getString(R.string.detail_transaksi_not_found), this@PembayaranActivity)
                finish()
            }
        }
        detailTransaksiViewModel.isLoading().observe(this) { isLoading ->
            binding.progressbarBuktiPembayaran.isVisible = isLoading
            binding.layoutBuktiPembayaran.isVisible = !isLoading
            binding.progressbarRingkasanPembayaran.isVisible = isLoading
            binding.layoutRingkasanPembayaran.isVisible = !isLoading
        }
        detailTransaksiViewModel.getPayment().observe(this) { detail ->
            if (detail != null) {
                val nilai = detail.biayaTransfer ?: 0
                val nominal = if (nilai.toInt() != 0) "Rp${Helper.addcoma3digit(nilai)}" else getString(R.string.strip)

                binding.nominalTf.text = nominal
                binding.namaBank.text = detail.namaBank ?: getString(R.string.strip)
                binding.noRek.text = detail.noRek ?: getString(R.string.strip)
                binding.atasNama.text = detail.pemilikBank ?: getString(R.string.strip)

                binding.tvBuktiPmbyrn.isGone = !detail.buktiTransaksi.isNullOrEmpty()
                binding.clImgBktiExist.isVisible = !detail.buktiTransaksi.isNullOrEmpty()

                if (!detail.buktiTransaksi.isNullOrEmpty()) {
                    Glide.with(this@PembayaranActivity).load(detail.buktiTransaksi)
                        .apply(RequestOptions().centerInside())
                        .placeholder(R.drawable.img_placeholder)
                        .error(R.drawable.img_error)
                        .into(binding.imageBuktiPmbyrn)
                }
            }
        }
    }

    private fun limitedClickListener(data: TrxDetailModel) {
        showToast(getString(R.string.late_upload_bukti, data.statusPesanan), this@PembayaranActivity)
    }

    private fun pilihGambarIntent() {
        val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(pickImageIntent)
    }

    private fun setImage(selectedImageUri: Uri) {
        val inputStream = contentResolver.openInputStream(selectedImageUri)
        val timestamp = System.currentTimeMillis()
        val fileName = "galeri$timestamp.jpg"
        val destinationFile = File(cacheDir, fileName)
        inputStream?.use { input ->
            destinationFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        loadImageWithGlide(Uri.fromFile(destinationFile), binding.imageBuktiPmbyrn)
    }

    private fun loadImageWithGlide(imageUri: Uri?, imageBuktiPmbyrn: ImageView) {
        Glide.with(this).load(imageUri)
            .apply(RequestOptions().centerInside())
            .placeholder(R.drawable.img_placeholder)
            .error(R.drawable.img_error)
            .into(imageBuktiPmbyrn)

        binding.clImgBktiExist.visibility = View.VISIBLE
        binding.tvBuktiPmbyrn.visibility = View.GONE
        binding.btnSimpanBuktPmbyrn.visibility = View.VISIBLE
        binding.btnSimpanBuktPmbyrn.isEnabled = true
    }

    private fun uploadImage() {
        detailTransaksiViewModel.uploadBuktiPembayaran(imageUri, getString(R.string.status_menunggu_konfirmasi)) { isSucceful ->
            if (isSucceful) {
                binding.btnSimpanBuktPmbyrn.isEnabled = false
                showToast("${getString(R.string.bukti_pembayaran)} ${getString(R.string.berhasil_diupload)}", this@PembayaranActivity)
            } else showToast(getString(R.string.masalah_database), this@PembayaranActivity)
        }
    }
}