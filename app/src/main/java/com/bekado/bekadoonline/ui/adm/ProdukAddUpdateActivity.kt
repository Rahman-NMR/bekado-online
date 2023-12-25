package com.bekado.bekadoonline.ui.adm

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.ActivityProdukAddUpdateBinding
import com.bekado.bekadoonline.helper.Helper.addcoma3digit
import com.bekado.bekadoonline.helper.Helper.delComa3digit
import com.bekado.bekadoonline.helper.Helper.formatPriceString
import com.bekado.bekadoonline.helper.Helper.showAlertDialog
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.Helper.showToastL
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.model.KategoriModel
import com.bekado.bekadoonline.model.ProdukModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.NumberFormat

class ProdukAddUpdateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProdukAddUpdateBinding
    private lateinit var db: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    private lateinit var produkRef: DatabaseReference
    private lateinit var kategoriRef: DatabaseReference
    private lateinit var kategoriListener: ValueEventListener

    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private var imageUri: Uri = Uri.parse("")

    private lateinit var produkData: ProdukModel
    private var isEdit: Boolean = false
    private lateinit var kategoriData: KategoriModel
    private var updateKategoriId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProdukAddUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        db = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        produkRef = db.getReference("produk/produk")
        kategoriRef = db.getReference("produk/kategori")
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

        produkData = intent.getParcelableExtra("produkData") ?: ProdukModel()
        isEdit = intent.getBooleanExtra("isEditProduk", false)
        kategoriData = intent.getParcelableExtra("kategoriData") ?: KategoriModel()
        if (isEdit) updateKategoriId = produkData.idKategori.toString()

        getDataKategori()

        with(binding) {
            namaProduk.addTextChangedListener(produkTextWatcher)
            hargaProduk.addTextChangedListener(produkTextWatcher)

            appBar.title = if (isEdit) getString(R.string.edit_produk) else getString(R.string.tambah_produk)
            appBar.menu.findItem(R.id.menu_hapus).isVisible = isEdit
            appBar.setNavigationOnClickListener { onBackPressed() }
            appBar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_hapus -> showAlertDialog()
                }
                true
            }

            if (isEdit)
                Glide.with(this@ProdukAddUpdateActivity).load(produkData.fotoProduk)
                    .apply(RequestOptions().centerCrop())
                    .placeholder(R.drawable.img_broken_image).into(fotoEditProduk)
            fotoEditProduk.setOnClickListener { pilihGambarIntent() }
            if (isEdit) namaProduk.setText(produkData.namaProduk)
            if (isEdit) hargaProduk.setText(addcoma3digit(produkData.hargaProduk))
            outlineKategoriDropdown.isEnabled = isEdit
            kategoriDropdown.isEnabled = isEdit
            kategoriDropdown.setText(kategoriData.namaKategori)
            btnSimpanPerubahan.text = if (isEdit) getString(R.string.simpan_perubahan) else getString(R.string.tambah_produk)
            btnSimpanPerubahan.setOnClickListener {
                if (HelperConnection.isConnected(this@ProdukAddUpdateActivity))
                    if (outlineNamaProduk.helperText == null && outlineHargaProduk.helperText == null)
                        validateNull(isEdit)
                    else {
                        val snackbar = Snackbar.make(root, getString(R.string.pastikan_no_error), Snackbar.LENGTH_LONG)
                        snackbar.setAction("Oke") { snackbar.dismiss() }.show()
                    }
            }
        }
    }

    private fun getDataKategori() {
        val kategoriList = ArrayList<Pair<String, String>>()

        kategoriListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                kategoriList.clear()

                for (item in snapshot.children) {
                    val idKategori = item.child("idKategori").value.toString()
                    val namaKategori = item.child("namaKategori").value.toString()
                    kategoriList.add(Pair(namaKategori, idKategori))
                }
                val arrayAdapter = ArrayAdapter(this@ProdukAddUpdateActivity, R.layout.drop_down_kategori_produk, kategoriList.map { it.first })
                binding.kategoriDropdown.apply {
                    setAdapter(arrayAdapter)
                    setOnItemClickListener { _, _, position, _ ->
                        val kategoriId = kategoriList[position].second
                        updateKategoriId = kategoriId
                        binding.btnSimpanPerubahan.isEnabled = kategoriList[position].second != kategoriData.idKategori
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        kategoriRef.orderByChild("posisi").addValueEventListener(kategoriListener)
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

        loadImageWithGlide(Uri.fromFile(destinationFile), binding.fotoEditProduk)
    }

    private fun loadImageWithGlide(imageUri: Uri?, imageBuktiPmbyrn: ImageView) {
        val requestOptions = RequestOptions().centerInside()
        Glide.with(this).load(imageUri).apply(requestOptions)
            .placeholder(R.drawable.img_broken_image).into(imageBuktiPmbyrn)
        binding.btnSimpanPerubahan.isEnabled = true
    }

    private fun uploadImage(produkId: String?) {
        val storageReference = storage.getReference("produk/$produkId.png")

        storageReference.putFile(imageUri).addOnSuccessListener {
            storageReference.downloadUrl.addOnCompleteListener { task ->
                produkRef.child("$produkId/fotoProduk").setValue(task.result.toString())
            }
        }
    }

    private fun ActivityProdukAddUpdateBinding.validateNull(isEdit: Boolean) {
        if (namaProduk.text.isNullOrEmpty())
            showToast("${getString(R.string.nama_produk)} ${getString(R.string.tidak_dapat_kosong)}", this@ProdukAddUpdateActivity)
        else if (hargaProduk.text.isNullOrEmpty())
            showToast("${getString(R.string.harga_produk)} ${getString(R.string.tidak_dapat_kosong)}", this@ProdukAddUpdateActivity)
        else toRtdb(isEdit)
    }

    private fun toRtdb(isEdit: Boolean) {
        val produkId = if (isEdit) produkData.idProduk else produkRef.push().key
        val kategoriSkrng = if (isEdit) updateKategoriId else kategoriData.idKategori
        val visibilitu = if (isEdit) produkData.visibility else false

        val namaProduk = binding.namaProduk.text.toString().trim()
        if (imageUri != Uri.parse("")) uploadImage(produkId)

        val produkHash = HashMap<String, Any>()
        produkHash["currency"] = "Rp"
        produkHash["hargaProduk"] = delComa3digit(binding.hargaProduk.text.toString().trim())
        produkHash["idKategori"] = kategoriSkrng.toString()
        produkHash["idProduk"] = produkId.toString()
        produkHash["namaProduk"] = namaProduk
        produkHash["visibility"] = visibilitu

        val ref = produkRef.child(produkId.toString())
        val reference = if (isEdit) ref.updateChildren(produkHash) else ref.setValue(produkHash)
        val toastTxt = if (isEdit) getString(R.string.berhasil_ditambahkan) else getString(R.string.berhasil_diperbarui)
        reference.addOnSuccessListener {
            showToast("$namaProduk $toastTxt", this)
            finish()
        }
    }

    private val produkTextWatcher: TextWatcher = object : TextWatcher {
        private var current: String = ""

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val namaInput = binding.namaProduk.text.toString().trim { it <= ' ' }
            val hargaInput = binding.hargaProduk.text.toString().trim { it <= ' ' }
            binding.btnSimpanPerubahan.isEnabled = namaInput.isNotEmpty() && hargaInput.isNotEmpty()

            // menahan error paste
            if (s == binding.hargaProduk.text)
                if (s!!.length > 3) {
                    s.let {
                        if (it.isNotEmpty() && !it.contains(".")) {
                            binding.hargaProduk.setText(formatPriceString(it.toString()))
                            binding.hargaProduk.setSelection(binding.hargaProduk.text?.length ?: 0)
                        }
                    }
                }
        }

        override fun afterTextChanged(s: Editable?) {
            val namaInput = binding.namaProduk.text
            val hargaInput = binding.hargaProduk.text

            val namaProduk = getString(R.string.nama_produk)
            val hargaProduk = getString(R.string.harga_produk)
            val notNull = getString(R.string.tidak_dapat_kosong)

            if (s == namaInput) {
                if (namaInput.isNullOrEmpty()) binding.outlineNamaProduk.helperText = "$namaProduk $notNull"
                else {
                    if (namaInput.toString().trim().isEmpty()) binding.outlineNamaProduk.helperText = "$namaProduk $notNull"
                    else binding.outlineNamaProduk.helperText = null
                }
            } else if (s == hargaInput) {
                if (hargaInput.isNullOrEmpty()) binding.outlineHargaProduk.helperText = "$hargaProduk $notNull"
                else {
                    if (hargaInput.toString().length != 12) {
                        if (s.toString() != current) {
                            binding.hargaProduk.removeTextChangedListener(this)

                            val cleanString = s.toString().replace("\\D+".toRegex(), "")
                            val parsed = cleanString.toDouble()
                            val formatted = NumberFormat.getNumberInstance().format(parsed)

                            current = formatted
                            binding.hargaProduk.setText(formatted)
                            binding.hargaProduk.setSelection(formatted.length)
                            binding.hargaProduk.addTextChangedListener(this)
                        }
                        binding.outlineHargaProduk.helperText = null
                    }
                }
            }

            binding.btnSimpanPerubahan.isEnabled = !(namaInput.toString() == produkData.namaProduk
                    && hargaInput.toString() == addcoma3digit(produkData.hargaProduk))
        }
    }

    private fun showAlertDialog() {
        showAlertDialog(
            "Hapus ${produkData.namaProduk}?",
            "Produk ${produkData.namaProduk} akan dihapus secara permanen.",
            getString(R.string.hapus_produk),
            this@ProdukAddUpdateActivity,
            getColor(R.color.error)
        ) {
            if (HelperConnection.isConnected(this@ProdukAddUpdateActivity)) {
                storage.getReference("produk/${produkData.idProduk}.png").delete()
                produkRef.child(produkData.idProduk.toString()).removeValue()
                showToastL("${produkData.namaProduk} berhasil dihapus", this@ProdukAddUpdateActivity)
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        kategoriRef.orderByChild("posisi").removeEventListener(kategoriListener)
    }
}