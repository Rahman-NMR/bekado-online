package com.bekado.bekadoonline.view.ui.admn

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.data.model.KategoriModel
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.data.viewmodel.KategoriListViewModel
import com.bekado.bekadoonline.data.viewmodel.ProdukSingleViewModel
import com.bekado.bekadoonline.databinding.ActivityProdukAddUpdateBinding
import com.bekado.bekadoonline.helper.Helper.showAlertDialog
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.helper.constval.VariableConstant
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class ProdukAddUpdateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProdukAddUpdateBinding
    private lateinit var storage: FirebaseStorage
    private lateinit var produkRef: DatabaseReference

    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private var imageUri: Uri = Uri.parse("")

    private lateinit var produkViewModel: ProdukSingleViewModel
    private lateinit var kategoriListViewModel: KategoriListViewModel

    private var updateKategoriId: String? = ""
    private var dataIdProduk: String = ""

    private var produkNama: String? = ""
    private var produkHarga: Long? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProdukAddUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        storage = FirebaseStorage.getInstance()
        produkRef = FirebaseDatabase.getInstance().getReference("produk/produk")
        dataIdProduk = intent.getStringExtra(VariableConstant.ID_PRODUCT) ?: ""
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

        produkViewModel = ViewModelProvider(this)[ProdukSingleViewModel::class.java]
        kategoriListViewModel = ViewModelProvider(this)[KategoriListViewModel::class.java]
        updateKategoriId = dataKategoriModel?.idKategori

        dataHandler()

        with(binding) {
            namaProduk.addTextChangedListener(produkTextWatcher)
            hargaProduk.addTextChangedListener(produkTextWatcher)

            appBar.setNavigationOnClickListener { finish() }
            fotoEditProduk.setOnClickListener { pilihGambarIntent() }
            kategoriDropdown.setOnItemClickListener { parent, _, position, _ ->
                val selectedNamaKategori = parent.getItemAtPosition(position) as String
                val selectedIdKategori = kategoriListViewModel.kategoriList.value?.find { it.namaKategori == selectedNamaKategori }?.idKategori

                updateKategoriId = selectedIdKategori.toString()
                binding.btnSimpanPerubahan.isEnabled = selectedIdKategori != dataKategoriModel?.idKategori
            }
        }
    }

    private fun dataHandler() {
        produkViewModel.loadProdukProduk(dataIdProduk)

        produkViewModel.isLoading.observe(this) {
            binding.progressbarProdukDetail.visibility = if (it) View.VISIBLE else View.GONE
            binding.layoutProdukDetail.visibility = if (!it) View.VISIBLE else View.GONE
        }
        produkViewModel.produkModel.observe(this) { produk ->
            with(binding) {
                appBar.title = if (produk != null) getString(R.string.edit_produk) else getString(R.string.tambah_produk)
                appBar.menu.findItem(R.id.menu_hapus).isVisible = produk != null

                outlineKategoriDropdown.isEnabled = produk != null
                kategoriDropdown.isEnabled = produk != null

                btnSimpanPerubahan.text = if (produk != null) getString(R.string.simpan_perubahan) else getString(R.string.tambah_produk)
                btnSimpanPerubahan.setOnClickListener {
                    if (HelperConnection.isConnected(this@ProdukAddUpdateActivity))
                        if (outlineNamaProduk.helperText == null && outlineHargaProduk.helperText == null) {
                            validateNull(produk)
                        } else {
                            val snackbar = Snackbar.make(root, getString(R.string.pastikan_no_error), Snackbar.LENGTH_LONG)
                            snackbar.setAction("Oke") { snackbar.dismiss() }.show()
                        }
                }

                if (produk != null) {
                    produkNama = produk.namaProduk
                    produkHarga = produk.hargaProduk

                    namaProduk.setText(produk.namaProduk)
                    hargaProduk.setText(produk.hargaProduk.toString())
                    Glide.with(this@ProdukAddUpdateActivity).load(produk.fotoProduk)
                        .apply(RequestOptions().centerCrop())
                        .placeholder(R.drawable.img_placeholder)
                        .error(R.drawable.img_error)
                        .transition(DrawableTransitionOptions.withCrossFade(300))
                        .into(fotoEditProduk)

                    appBar.setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.menu_hapus -> dialogHapusProduk(produk)
                        }
                        true
                    }
                }
            }
        }

        kategoriListViewModel.isLoading.observe(this) {
            binding.progressbarKategoriProduk.visibility = if (it) View.VISIBLE else View.GONE
            binding.outlineKategoriDropdown.visibility = if (!it) View.VISIBLE else View.GONE
        }
        kategoriListViewModel.kategoriList.observe(this) { kategoriList ->
            if (kategoriList != null) {
                val listNamaKategori = kategoriList.mapNotNull { it.namaKategori }
                val adapterKategori = ArrayAdapter(this, R.layout.drop_down_kategori_produk, listNamaKategori)

                binding.kategoriDropdown.setAdapter(adapterKategori)
                binding.kategoriDropdown.setText(dataKategoriModel?.namaKategori ?: getString(R.string.pilih_kategori), false)
            }
        }
    }

    private fun pilihGambarIntent() {
        ImagePicker.with(this).galleryOnly().compress(2048)
            .cropSquare().maxResultSize(1080, 1080)
            .createIntent { intent ->
                pickImageLauncher.launch(intent)
            }
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
        Glide.with(this).load(imageUri).apply(RequestOptions().centerInside())
            .placeholder(R.drawable.img_placeholder)
            .error(R.drawable.img_error)
            .transition(DrawableTransitionOptions.withCrossFade(300))
            .into(imageBuktiPmbyrn)
        binding.btnSimpanPerubahan.isEnabled = true
    }

    private fun uploadImage(produkId: String?) {
        val storageReference = storage.getReference("produk/$produkId.png")

        storageReference.putFile(imageUri).addOnSuccessListener {
            storageReference.downloadUrl.addOnCompleteListener { task ->
                produkRef.child("$produkId/fotoProduk").setValue(task.result.toString())
            }
        }.addOnFailureListener { showToast(getString(R.string.masalah_database), this@ProdukAddUpdateActivity) }
    }

    private fun ActivityProdukAddUpdateBinding.validateNull(produk: ProdukModel?) {
        when {
            namaProduk.text.isNullOrEmpty() -> showToast(
                getString(R.string.tidak_dapat_kosong, getString(R.string.nama_produk)),
                this@ProdukAddUpdateActivity
            )

            hargaProduk.text.isNullOrEmpty() -> showToast(
                getString(R.string.tidak_dapat_kosong, getString(R.string.harga_produk)),
                this@ProdukAddUpdateActivity
            )

            updateKategoriId.isNullOrEmpty() -> showToast(getString(R.string.kategori_unselected), this@ProdukAddUpdateActivity)
            else -> uploadToDatabase(produk)
        }
    }

    private fun ActivityProdukAddUpdateBinding.uploadToDatabase(produk: ProdukModel?) {
        val isEdit = produk != null
        val produkId = if (produk != null) produk.idProduk else produkRef.push().key
        val kategoriSkrng = if (isEdit) updateKategoriId else dataKategoriModel?.idKategori
        val visibiliti = produk?.visibility ?: false

        val namaProduk = namaProduk.text.toString().trim()
        if (imageUri != Uri.parse("")) uploadImage(produkId)

        val produkHash = HashMap<String, Any>()
        produkHash["currency"] = "Rp"
        produkHash["hargaProduk"] = binding.hargaProduk.text.toString().trim()
        produkHash["idKategori"] = kategoriSkrng.toString()
        produkHash["idProduk"] = produkId.toString()
        produkHash["namaProduk"] = namaProduk
        produkHash["visibility"] = visibiliti

        val ref = produkRef.child(produkId.toString())
        val reference = if (isEdit) ref.updateChildren(produkHash) else ref.setValue(produkHash)
        val toastTxt = if (isEdit) getString(R.string.berhasil_diperbarui) else getString(R.string.berhasil_ditambahkan)
        reference.addOnSuccessListener {
            showToast("$namaProduk $toastTxt", this@ProdukAddUpdateActivity)
            finish()
        }
    }

    private val produkTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val namaInput = binding.namaProduk.text.toString().trim { it <= ' ' }
            val hargaInput = binding.hargaProduk.text.toString().trim { it <= ' ' }
            binding.btnSimpanPerubahan.isEnabled = namaInput.isNotEmpty() && hargaInput.isNotEmpty()
        }

        override fun afterTextChanged(s: Editable?) {
            val namaInput = binding.namaProduk.text
            val hargaInput = binding.hargaProduk.text

            val namaProduk = getString(R.string.nama_produk)
            val hargaProduk = getString(R.string.harga_produk)

            when (s) {
                namaInput -> binding.outlineNamaProduk.helperText =
                    if (namaInput.toString().trim().isEmpty()) getString(R.string.tidak_dapat_kosong, namaProduk) else null

                hargaInput -> binding.outlineHargaProduk.helperText =
                    if (hargaInput.isNullOrEmpty()) getString(R.string.tidak_dapat_kosong, hargaProduk) else null
            }

            binding.btnSimpanPerubahan.isEnabled = !(namaInput.toString() == produkNama && hargaInput.toString() == produkHarga.toString())
        }
    }

    private fun dialogHapusProduk(produk: ProdukModel) {
        showAlertDialog(
            "Hapus ${produk.namaProduk}?",
            "${produk.namaProduk} akan dihapus secara permanen.",
            getString(R.string.hapus_produk),
            this@ProdukAddUpdateActivity,
            getColor(R.color.error)
        ) {
            if (HelperConnection.isConnected(this@ProdukAddUpdateActivity)) {
                storage.getReference("produk/${produk.idProduk}.png").delete()
                produkRef.child(produk.idProduk.toString()).removeValue()
                showToast("${produk.namaProduk} berhasil dihapus", this@ProdukAddUpdateActivity)
                finish()
            }
        }
    }

    companion object {
        var dataKategoriModel: KategoriModel? = null
    }
}