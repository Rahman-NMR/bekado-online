package com.bekado.bekadoonline.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bekado.bekadoonline.adapter.AdapterBankList
import com.bekado.bekadoonline.databinding.ActivityPembayaranBinding
import com.bekado.bekadoonline.helper.Helper.showToast
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

class PembayaranActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPembayaranBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
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

    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 4576
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPembayaranBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        isAdmin = intent.getBooleanExtra("statusAdmin", false)
        totalBelanja = intent.getStringExtra("totalBelanjaK") ?: ""
        uidnIdtrx = intent.getStringExtra("pathTrx") ?: ""
        if (uidnIdtrx.isNotEmpty()) trxRef = db.getReference("transaksi/$uidnIdtrx")

        getDataBuktiTrx()

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        with(binding) {
            appBar.setNavigationOnClickListener { onBackPressed() }

            rvMetodeTransferList.layoutManager = layoutManager
            btnPilihMetodeSkrng.setOnClickListener { saveMetodeSelected() }

            if (isAdmin) btnUpbukPembayaran.visibility = View.GONE
            btnUpbukPembayaran.setOnClickListener { selectImage() }
            btnSimpanBuktPmbyrn.setOnClickListener { uploadImage() }
        }
    }

    private fun getDataBuktiTrx() {
        if (uidnIdtrx.isNotEmpty()) {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists())
                        with(binding) {
                            subtitlePilihMetTrns.visibility = View.GONE
                            cvPilihMetTrns.visibility = View.GONE
                            btnPilihMetodeSkrng.visibility = View.GONE
                            btnUpbukPembayaran.isEnabled = true

                            nominalTf.text = snapshot.child("biayaTransfer").value.toString()
                            namaBankDipilih.text = snapshot.child("namaBank").value.toString()
                            noRekDipilih.text = snapshot.child("noRek").value.toString()
                            if (snapshot.child("buktiTransaksi").exists()) {
                                val buktiTrx = snapshot.child("buktiTransaksi").value.toString()
                                if (!isDestroyed) Glide.with(this@PembayaranActivity)
                                    .load(buktiTrx).apply(RequestOptions().centerInside())
                                    .into(imageBuktiPmbyrn)
                                tvBuktiPmbyrn.visibility = View.GONE
                                imageBuktiPmbyrn.visibility = View.VISIBLE
                            } else {
                                tvBuktiPmbyrn.visibility = View.VISIBLE
                                imageBuktiPmbyrn.visibility = View.GONE
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
                with(binding) {
                    subtitlePilihMetTrns.visibility = View.GONE
                    cvPilihMetTrns.visibility = View.GONE
                    btnPilihMetodeSkrng.visibility = View.GONE
                    btnUpbukPembayaran.isEnabled = true
                }
            }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    private fun loadImageWithGlide(imageUri: Uri?, imageBuktiPmbyrn: ImageView) {
        val requestOptions = RequestOptions().centerInside()
        Glide.with(this).load(imageUri).apply(requestOptions).into(imageBuktiPmbyrn)
        imageBuktiPmbyrn.visibility = View.VISIBLE
        binding.tvBuktiPmbyrn.visibility = View.GONE
        binding.btnUpbukPembayaran.visibility = View.GONE
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.data!!
            loadImageWithGlide(imageUri, binding.imageBuktiPmbyrn)
        }
    }
}