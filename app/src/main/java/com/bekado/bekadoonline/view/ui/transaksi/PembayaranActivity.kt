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
import androidx.lifecycle.ViewModelProvider
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.data.model.DetailTransaksiModel
import com.bekado.bekadoonline.databinding.ActivityPembayaranBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.constval.VariableConstant
import com.bekado.bekadoonline.data.model.TransaksiModel
import com.bekado.bekadoonline.data.viewmodel.AkunViewModel
import com.bekado.bekadoonline.data.viewmodel.BuktiPembayaranViewModel
import com.bekado.bekadoonline.data.viewmodel.ClientDataViewModel
import com.bekado.bekadoonline.data.viewmodel.TransaksiDetailViewModel
import com.bekado.bekadoonline.ui.ViewModelFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class PembayaranActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPembayaranBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private var imageUri: Uri = Uri.parse("")

    private val akunViewModel: AkunViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private lateinit var transaksiViewModel: TransaksiDetailViewModel
    private lateinit var clientDataViewModel: ClientDataViewModel
    private lateinit var invoiceViewModel: BuktiPembayaranViewModel

    private lateinit var akunRef: DatabaseReference
    private lateinit var invRef: DatabaseReference
    private var status: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPembayaranBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
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
        invRef = db.getReference("transaksi/$uidnIdtrx")
        status = listOf(
            getString(R.string.status_dalam_pengiriman),
            getString(R.string.status_dalam_proses),
            getString(R.string.status_selesai),
            getString(R.string.status_dibatalkan)
        )

//        akunViewModel = ViewModelProvider(this)[AkunViewModel::class.java]
        transaksiViewModel = ViewModelProvider(this)[TransaksiDetailViewModel::class.java]
        clientDataViewModel = ViewModelProvider(this)[ClientDataViewModel::class.java]
        invoiceViewModel = ViewModelProvider(this)[BuktiPembayaranViewModel::class.java]

        dataHandler()
        getDataBuktiTrx()

        with(binding) {
            appBar.setNavigationOnClickListener { finish() }

            salinNoRek.setOnClickListener { Helper.salinPesan(this@PembayaranActivity, noRek.text) }
            salinNominalTf.setOnClickListener { Helper.salinPesan(this@PembayaranActivity, nominalTf.text) }

            btnUbahImageBukti.setOnClickListener { pilihGambarIntent() }
            btnSimpanBuktPmbyrn.isEnabled = imageUri != Uri.parse("")
        }
    }

    private fun dataHandler() {
//        akunViewModel.loadCurrentUser()
        akunViewModel.loadAkunData()

//        akunViewModel.currentUser.observe(this) { if (it == null) finish() }
        akunViewModel.akunModel.observe(this) { akunModel ->
            if (akunModel != null) {
                akunRef = db.getReference("akun/${akunModel.uid}")

                val isAdmin = akunModel.statusAdmin
                val drawableTop = if (isAdmin) 0 else R.drawable.icon_outline_add_photo_alternate_24
                val txtBuktiTrx = if (isAdmin) getString(R.string.belum_upload_bukti) else getString(R.string.tambah_bukti_pembayaran)

                with(binding) {
                    tvBuktiPmbyrn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, drawableTop, 0, 0)
                    tvBuktiPmbyrn.text = txtBuktiTrx
                    btnUbahImageBukti.visibility = if (!isAdmin) View.VISIBLE else View.GONE
                    if (!isAdmin) tvBuktiPmbyrn.setOnClickListener { pilihGambarIntent() }
                    btnSimpanBuktPmbyrn.setOnClickListener { uploadImage() }
                }

                transaksiViewModel.loadDetailTransaksi(invRef, akunModel.statusAdmin, BuktiDetailTransaksi.idTransaksi)
            } else {
                akunRef = db.getReference("akun")
                finish()
            }
        }
        transaksiViewModel.detailTransaksi.observe(this) { data ->
            if (data != null) {
                if (!data.metodePembayaran.isNullOrEmpty()) if (data.metodePembayaran == getString(R.string.cod)) finish()
                if (!data.statusPesanan.isNullOrEmpty())
                    if (status.any { data.statusPesanan.contains(it) }) {
                        binding.btnUbahImageBukti.visibility = View.GONE
                        binding.btnSimpanBuktPmbyrn.visibility = View.GONE
                        binding.btnSimpanBuktPmbyrn.setOnClickListener { limitedClickListener(data) }
                        binding.tvBuktiPmbyrn.setOnClickListener { limitedClickListener(data) }
                    }
            }
        }
    }

    private fun limitedClickListener(data: DetailTransaksiModel) {
        showToast("${getString(R.string.late_upload_bukti)} ${data.statusPesanan}", this@PembayaranActivity)
    }

    private fun getDataBuktiTrx() {
        invoiceViewModel.loadInvoice(invRef.child("buktiTransaksi"))

        invoiceViewModel.dataInvoice.observe(this) { invoice ->
            if (invoice != null) {
                val nilai = invoice.biayaTransfer ?: 0
                val nominal = if (nilai.toInt() != 0) "Rp${Helper.addcoma3digit(nilai)}" else getString(R.string.strip)

                binding.nominalTf.text = nominal
                binding.namaBank.text = invoice.namaBank
                binding.noRek.text = invoice.noRek
                binding.atasNama.text = invoice.pemilikBank

                if (!invoice.buktiTransaksi.isNullOrEmpty()) {
                    Glide.with(this@PembayaranActivity)
                        .load(invoice.buktiTransaksi).apply(RequestOptions().centerInside())
                        .placeholder(R.drawable.img_broken_image).into(binding.imageBuktiPmbyrn)

                    binding.tvBuktiPmbyrn.visibility = View.GONE
                    binding.clImgBktiExist.visibility = View.VISIBLE
                } else {
                    binding.tvBuktiPmbyrn.visibility = View.VISIBLE
                    binding.clImgBktiExist.visibility = View.GONE
                }
            }
        }
        invoiceViewModel.isLoading.observe(this) { isLoading ->
            binding.progressbarBuktiPembayaran.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.layoutBuktiPembayaran.visibility = if (!isLoading) View.VISIBLE else View.GONE
            binding.progressbarRingkasanPembayaran.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.layoutRingkasanPembayaran.visibility = if (!isLoading) View.VISIBLE else View.GONE
        }
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
        val requestOptions = RequestOptions().centerInside()
        Glide.with(this).load(imageUri).apply(requestOptions)
            .placeholder(R.drawable.img_broken_image).into(imageBuktiPmbyrn)
        binding.clImgBktiExist.visibility = View.VISIBLE
        binding.tvBuktiPmbyrn.visibility = View.GONE
        binding.btnSimpanBuktPmbyrn.visibility = View.VISIBLE
        binding.btnSimpanBuktPmbyrn.isEnabled = true
    }

    private fun uploadImage() {
        val storageReference = storage.getReference("transaksi/$uidnIdtrx.jpg")

        storageReference.putFile(imageUri).addOnSuccessListener {
            storageReference.downloadUrl.addOnCompleteListener { task ->
                val imgLink = task.result.toString()
                invRef.child("buktiTransaksi/buktiTransaksi").setValue(imgLink).addOnSuccessListener {
                    binding.btnSimpanBuktPmbyrn.isEnabled = false
                    showToast("${getString(R.string.bukti_pembayaran)} ${getString(R.string.berhasil_diupload)}", this@PembayaranActivity)

                    setResult(RESULT_OK, Intent().putExtra(VariableConstant.RESULT_ACTION, VariableConstant.ACTION_REFRESH_UI))
                }
                invRef.child("statusPesanan").setValue(getString(R.string.status_menunggu_konfirmasi))
            }
        }.addOnFailureListener { showToast(getString(R.string.masalah_database), this@PembayaranActivity) }
    }

    override fun onDestroy() {
        super.onDestroy()
        invoiceViewModel.removeInvoiceListener(invRef.child("buktiTransaksi"))
    }

    companion object {
        var BuktiDetailTransaksi: TransaksiModel = TransaksiModel()
        var uidnIdtrx: String? = ""
    }
}