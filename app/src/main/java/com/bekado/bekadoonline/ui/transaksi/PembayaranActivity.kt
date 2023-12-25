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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.adapter.AdapterBankList
import com.bekado.bekadoonline.databinding.ActivityPembayaranBinding
import com.bekado.bekadoonline.helper.Helper
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.Helper.showToastL
import com.bekado.bekadoonline.model.BankModel
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
    private var dataBank: ArrayList<BankModel> = ArrayList()
    private lateinit var adapterBank: AdapterBankList
    private var imageUri: Uri = Uri.parse("")

    private lateinit var trxRef: DatabaseReference
    private var isAdmin: Boolean = false
    private lateinit var uidnIdtrx: String
    private lateinit var totalBelanja: String
    private lateinit var bankNama: String
    private lateinit var bankLogo: String
    private lateinit var bankNoRek: String

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
        totalBelanja = intent.getStringExtra("totalBelanjaK") ?: ""
        uidnIdtrx = intent.getStringExtra("pathTrx") ?: ""
        if (uidnIdtrx.isNotEmpty()) trxRef = db.getReference("transaksi/$uidnIdtrx")

        getDataBuktiTrx()

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val drawableTop = if (isAdmin) 0 else R.drawable.icon_outline_add_photo_alternate_24
        val txtBuktiTrx = if (isAdmin) getString(R.string.belum_upload_bukti) else getString(R.string.tambah_bukti_pembayaran)

        with(binding) {
            appBar.setNavigationOnClickListener { onBackPressed() }

            rvMetodeTransferList.layoutManager = layoutManager
            btnPilihMetodeSkrng.setOnClickListener { saveMetodeSelected() }

            salinNamaBank.setOnClickListener { Helper.salinPesan(this@PembayaranActivity, namaBankDipilih.text) }
            salinNoRek.setOnClickListener { Helper.salinPesan(this@PembayaranActivity, noRekDipilih.text) }
            salinNominalTf.setOnClickListener { Helper.salinPesan(this@PembayaranActivity, nominalTf.text) }

            btnUbahImageBukti.visibility = if (!isAdmin) View.VISIBLE else View.GONE
            tvBuktiPmbyrn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, drawableTop, 0, 0)
            tvBuktiPmbyrn.text = txtBuktiTrx
            btnSimpanBuktPmbyrn.setOnClickListener { uploadImage() }
            btnUbahImageBukti.setOnClickListener { pilihGambarIntent() }
        }
    }

    private fun getDataBuktiTrx() {
        if (uidnIdtrx.isNotEmpty()) {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdmin) binding.tvBuktiPmbyrn.setOnClickListener {
                        if (snapshot.exists()) pilihGambarIntent()
                        else showToastL("Anda belum memilih metode transfer", this@PembayaranActivity)
                    }

                    if (snapshot.exists())
                        with(binding) {
                            subtitlePilihMetTrns.visibility = View.GONE
                            cvPilihMetTrns.visibility = View.GONE
                            btnPilihMetodeSkrng.visibility = View.GONE

                            nominalTf.text = snapshot.child("biayaTransfer").value.toString()
                            namaBankDipilih.text = snapshot.child("namaBank").value.toString()
                            noRekDipilih.text = snapshot.child("noRek").value.toString()
                            if (snapshot.child("buktiTransaksi").exists()) {
                                val buktiTrx = snapshot.child("buktiTransaksi").value.toString()
                                if (!isDestroyed) Glide.with(this@PembayaranActivity)
                                    .load(buktiTrx).apply(RequestOptions().centerInside())
                                    .into(imageBuktiPmbyrn)
                                tvBuktiPmbyrn.visibility = View.GONE
                                clImgBktiExist.visibility = View.VISIBLE
                            } else {
                                tvBuktiPmbyrn.visibility = View.VISIBLE
                                clImgBktiExist.visibility = View.GONE
                            }
                        }
                    else {
                        getBankList()
                        with(binding) {
                            if (!isAdmin) subtitlePilihMetTrns.visibility = View.VISIBLE
                            if (!isAdmin) cvPilihMetTrns.visibility = View.VISIBLE
                            if (!isAdmin) btnPilihMetodeSkrng.visibility = View.VISIBLE
                            tvBuktiPmbyrn.visibility = View.VISIBLE
                        }
                    }
                    binding.loadingIndicator.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {}
            }
            trxRef.child("buktiTransaksi").addListenerForSingleValueEvent(listener)
        }
    }

    private fun getBankList() {
        db.getReference("aaabdfiklnrstt").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.subtitlePilihMetTrns.visibility = View.VISIBLE
                binding.cvPilihMetTrns.visibility = View.VISIBLE
                dataBank.clear()

                for (data in snapshot.children) {
                    bankNama = data.child("name").value as String
                    bankLogo = data.child("logoWiki").value as String
                    bankNoRek = data.child("noRek").value as String
                    val model = BankModel("", "", "", bankNama, "", bankLogo, bankNoRek, false)
                    dataBank.add(model)

                    dataBank[0].isActive = true
                    binding.namaBankDipilih.text = dataBank[0].name
                    binding.noRekDipilih.text = dataBank[0].noRek

                    adapterBank = AdapterBankList(dataBank) { bank ->
                        if (bank.isActive) {
                            binding.namaBankDipilih.text = bank.name
                            binding.noRekDipilih.text = bank.noRek
                        }
                    }
                }
                binding.nominalTf.text = totalBelanja
                binding.rvMetodeTransferList.adapter = adapterBank
            }

            override fun onCancelled(error: DatabaseError) {
                binding.subtitlePilihMetTrns.visibility = View.GONE
                binding.cvPilihMetTrns.visibility = View.GONE
            }
        })
    }

    private fun saveMetodeSelected() {
        val buktiTransaksi = hashMapOf(
            "namaBank" to bankNama,
            "fotoBank" to bankLogo,
            "biayaTransfer" to totalBelanja,
            "noRek" to bankNoRek,
        )
        if (uidnIdtrx.isNotEmpty())
            trxRef.child("buktiTransaksi").setValue(buktiTransaksi).addOnSuccessListener {
                showToast("Metode transfer dipilih", this)
                getDataBuktiTrx()
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
        Glide.with(this).load(imageUri).apply(requestOptions).into(imageBuktiPmbyrn)
        binding.clImgBktiExist.visibility = View.VISIBLE
        binding.tvBuktiPmbyrn.visibility = View.GONE
        binding.btnSimpanBuktPmbyrn.visibility = View.VISIBLE
    }

    private fun uploadImage() {
        val storageReference = storage.getReference("transaksi/$uidnIdtrx.jpg")

        storageReference.putFile(imageUri).addOnSuccessListener {
            storageReference.downloadUrl.addOnCompleteListener { task ->
                val imgLink = task.result.toString()
                trxRef.child("buktiTransaksi/buktiTransaksi").setValue(imgLink).addOnSuccessListener {
                    showToast("upload rtdb ok", this@PembayaranActivity)
                }.addOnFailureListener { showToast("upload rtdb fail", this@PembayaranActivity) }
            }.addOnFailureListener { showToast("upload downloadUrl fail", this@PembayaranActivity) }
        }.addOnFailureListener { showToast("upload putFile fail", this@PembayaranActivity) }
    }
}