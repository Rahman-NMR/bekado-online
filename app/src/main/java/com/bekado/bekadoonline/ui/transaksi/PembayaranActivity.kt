package com.bekado.bekadoonline.ui.transaksi

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.ActivityPembayaranBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class PembayaranActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPembayaranBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private var imageUri: Uri = Uri.parse("")

    private lateinit var trxRef: DatabaseReference
    private var isAdmin: Boolean = false
    private lateinit var uidnIdtrx: String
    private lateinit var statusPesanan: String
    private lateinit var metodePembayaran: String

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

        isAdmin = intent.getBooleanExtra("statusAdmin", false)
        uidnIdtrx = intent.getStringExtra("pathTrx") ?: ""
        statusPesanan = intent.getStringExtra("statusPesanan") ?: ""
        metodePembayaran = intent.getStringExtra("metodePembayaran") ?: ""
        if (uidnIdtrx.isNotEmpty()) trxRef = db.getReference("transaksi/$uidnIdtrx")

        getDataBuktiTrx()

        val drawableTop = if (isAdmin) 0 else R.drawable.icon_outline_add_photo_alternate_24
        val txtBuktiTrx = if (isAdmin) getString(R.string.belum_upload_bukti) else getString(R.string.tambah_bukti_pembayaran)

        with(binding) {
            appBar.setNavigationOnClickListener { finish() }

            salinNoRek.setOnClickListener { Helper.salinPesan(this@PembayaranActivity, noRek.text) }
            salinNominalTf.setOnClickListener { Helper.salinPesan(this@PembayaranActivity, nominalTf.text) }

            tvBuktiPmbyrn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, drawableTop, 0, 0)
            tvBuktiPmbyrn.text = txtBuktiTrx
            btnUbahImageBukti.visibility = if (!isAdmin) View.VISIBLE else View.GONE
            btnUbahImageBukti.setOnClickListener { pilihGambarIntent() }
            btnSimpanBuktPmbyrn.setOnClickListener { uploadImage() }
            btnSimpanBuktPmbyrn.isEnabled = imageUri != Uri.parse("")
            if (!isAdmin) binding.tvBuktiPmbyrn.setOnClickListener { pilihGambarIntent() }

            val status = listOf(
                getString(R.string.status_dalam_pengiriman),
                getString(R.string.status_dalam_proses),
                getString(R.string.status_selesai),
                getString(R.string.status_dibatalkan)
            )

            if (status.any { statusPesanan.contains(it) }) {
                btnUbahImageBukti.visibility = View.GONE
                btnSimpanBuktPmbyrn.visibility = View.GONE
            }
        }
    }

    private fun getDataBuktiTrx() {
        if (uidnIdtrx.isNotEmpty()) {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    with(binding) {
                        val nilai = snapshot.child("biayaTransfer").value as? Long ?: 0
                        val nominal = if (nilai.toInt() != 0) "Rp${Helper.addcoma3digit(nilai)}" else getString(R.string.strip)

                        namaBank.text = snapshot.child("namaBank").value as? String ?: getString(R.string.strip)
                        noRek.text = snapshot.child("noRek").value as? String ?: getString(R.string.strip)
                        atasNama.text = snapshot.child("pemilikBank").value as? String ?: getString(R.string.strip)
                        nominalTf.text = nominal

                        val snapTrx = snapshot.child("buktiTransaksi")
                        if (snapTrx.exists()) {
                            val buktiTrx = snapshot.child("buktiTransaksi").value.toString()
                            if (!isDestroyed) Glide.with(this@PembayaranActivity)
                                .load(buktiTrx).apply(RequestOptions().centerInside())
                                .placeholder(R.drawable.img_broken_image)
                                .into(imageBuktiPmbyrn)
                        }
                        tvBuktiPmbyrn.visibility = if (snapTrx.exists()) View.GONE else View.VISIBLE
                        clImgBktiExist.visibility = if (snapTrx.exists()) View.VISIBLE else View.GONE

                        loadingIndicator.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.tvBuktiPmbyrn.visibility = View.GONE
                    binding.loadingIndicator.visibility = View.VISIBLE
                }
            }

            trxRef.child("buktiTransaksi").addListenerForSingleValueEvent(listener)
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
                trxRef.child("buktiTransaksi/buktiTransaksi").setValue(imgLink).addOnSuccessListener {
                    binding.btnSimpanBuktPmbyrn.isEnabled = false
                    showToast("${getString(R.string.bukti_pembayaran)} ${getString(R.string.berhasil_diupload)}", this@PembayaranActivity)
                }
                trxRef.child("statusPesanan").setValue(getString(R.string.status_menunggu_konfirmasi))
            }
        }.addOnFailureListener { showToast(getString(R.string.masalah_database), this@PembayaranActivity) }
    }

    override fun onStart() {
        super.onStart()
        if (metodePembayaran.isNotEmpty()) if (metodePembayaran == getString(R.string.cod)) finish()
    }
}