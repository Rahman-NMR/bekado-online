package com.bekado.bekadoonline.view.ui.admn

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.data.model.ProdukModel
import com.bekado.bekadoonline.databinding.ActivityProdukAddUpdateBinding
import com.bekado.bekadoonline.helper.Helper.showAlertDialog
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.HelperConnection
import com.bekado.bekadoonline.helper.constval.VariableConstant
import com.bekado.bekadoonline.helper.constval.VariableConstant.Companion.EXTRA_ID_KATEGORI
import com.bekado.bekadoonline.view.viewmodel.admin.AdminViewModelFactory
import com.bekado.bekadoonline.view.viewmodel.admin.KategoriListViewModel
import com.bekado.bekadoonline.view.viewmodel.admin.ProdukViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModelFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import java.io.File

class ProdukAddUpdateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProdukAddUpdateBinding
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    private val userViewModel: UserViewModel by viewModels { UserViewModelFactory.getInstance(this) }
    private val kategoriListViewModel: KategoriListViewModel by viewModels { AdminViewModelFactory.getInstance() }
    private val produkViewModel: ProdukViewModel by viewModels { AdminViewModelFactory.getInstance() }

    private var extraIdProduk: String? = ""
    private var extraIdKategori: String? = ""

    private var imageUri: Uri? = Uri.parse("")
    private var idKategori: String? = ""
    private var produkNama: String? = ""
    private var produkHarga: Long? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProdukAddUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        extraIdProduk = intent.getStringExtra(VariableConstant.EXTRA_ID_PRODUK) ?: ""
        extraIdKategori = intent?.getStringExtra(EXTRA_ID_KATEGORI) ?: ""
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

        dataAkunHandler()
        produkHandler()
        kategoriListHandler()

        with(binding) {
            namaProduk.addTextChangedListener(produkTextWatcher)
            hargaProduk.addTextChangedListener(produkTextWatcher)

            appBar.setNavigationOnClickListener { finish() }
            fotoEditProduk.setOnClickListener { pilihGambarIntent() }
        }
    }

    private fun dataAkunHandler() {
        userViewModel.getDataAkun().observe(this) { akun ->
            akun?.let { if (!it.statusAdmin) finish() } ?: finish()
        }
    }

    private fun produkHandler() {
        produkViewModel.isLoading().observe(this) { isLoading ->
            binding.progressbarProdukDetail.isVisible = isLoading
            binding.layoutProdukDetail.isVisible = !isLoading
        }
        produkViewModel.getDataProduk(extraIdProduk).observe(this) { produk ->
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
                kategoriDropdown.setOnItemClickListener { parent, _, position, _ ->
                    val selectedNamaKategori = parent.getItemAtPosition(position) as String
                    val selectedIdKategori = kategoriListViewModel.getKategoriList()
                        .value?.find { it.namaKategori == selectedNamaKategori }?.idKategori

                    idKategori = selectedIdKategori.toString()
                    binding.btnSimpanPerubahan.isEnabled = selectedIdKategori != produk?.idKategori
                }

                idKategori = if (produk != null) produk.idKategori else extraIdKategori
                produkNama = produk?.namaProduk ?: ""
                produkHarga = produk?.hargaProduk ?: 0

                if (produk != null) {
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
    }

    private fun kategoriListHandler() {
        kategoriListViewModel.isLoading().observe(this) { isLoading ->
            binding.progressbarKategoriProduk.isVisible = isLoading
            binding.outlineKategoriDropdown.isVisible = !isLoading
        }
        kategoriListViewModel.getKategoriList().observe(this) { kategoriList ->
            if (kategoriList != null) {
                val listNamaKategori = kategoriList.mapNotNull { it.namaKategori }
                val adapterKategori = ArrayAdapter(this, R.layout.drop_down_kategori_produk, listNamaKategori)
                val dataKategoriModel = kategoriList.find { it.idKategori == idKategori }

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

    private fun loadImageWithGlide(imageUri: Uri?, fotoProduk: ImageView) {
        Glide.with(this).load(imageUri).apply(RequestOptions().centerInside())
            .placeholder(R.drawable.img_placeholder)
            .error(R.drawable.img_error)
            .transition(DrawableTransitionOptions.withCrossFade(300))
            .into(fotoProduk)
        binding.btnSimpanPerubahan.isEnabled = true
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

            idKategori.isNullOrEmpty() -> showToast(getString(R.string.kategori_unselected), this@ProdukAddUpdateActivity)
            else -> uploadToDatabase(produk)
        }
    }

    private fun ActivityProdukAddUpdateBinding.uploadToDatabase(produk: ProdukModel?) {
        val isEdit = produk != null
        val idProduk = produk?.idProduk ?: ""
        val namaProduk = namaProduk.text.toString().trim()
        val hargaProduk = hargaProduk.text.toString().trim().toLong()
        val toastTxt = if (isEdit) getString(R.string.berhasil_diperbarui, namaProduk) else getString(R.string.berhasil_ditambahkan, namaProduk)

        produkViewModel.updateDataProduk(isEdit, imageUri, idProduk, idKategori, namaProduk, hargaProduk) { isSuccessful ->
            if (isSuccessful) {
                showToast(toastTxt, this@ProdukAddUpdateActivity)
                finish()
            } else showToast(getString(R.string.masalah_database), this@ProdukAddUpdateActivity)
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
                produkViewModel.deleteProduk(produk.idProduk) { isSuccessful ->
                    if (isSuccessful) {
                        showToast("${produk.namaProduk} berhasil dihapus", this@ProdukAddUpdateActivity)
                        finish()
                    } else showToast(getString(R.string.gagal_hapus_produk, ""), this@ProdukAddUpdateActivity)
                }
            }
        }
    }
}