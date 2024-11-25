package com.bekado.bekadoonline.view.ui.profil

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.ActivityAkunSayaBinding
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.HelperConnection.isConnected
import com.bekado.bekadoonline.view.viewmodel.user.AlamatViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserDataUpdateViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModel
import com.bekado.bekadoonline.view.viewmodel.user.UserViewModelFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.button.MaterialButton

class AkunSayaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAkunSayaBinding

    private val userViewModel: UserViewModel by viewModels { UserViewModelFactory.getInstance(this) }
    private val userUpdateViewModel: UserDataUpdateViewModel by viewModels { UserViewModelFactory.getInstance(this) }
    private val alamatViewModel: AlamatViewModel by viewModels { UserViewModelFactory.getInstance(this) }

    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private var imageUri: Uri = Uri.parse("")

    private var namaUser: String = ""
    private var nohpUser: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAkunSayaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val data: Intent? = result.data
                    val selectedImageUri: Uri? = data?.data
                    if (selectedImageUri != null) {
                        imageUri = selectedImageUri
                        uploadImgtoDb(selectedImageUri)
                    }
                }

                ImagePicker.RESULT_ERROR -> {
                    showToast(ImagePicker.getError(result.data), this)
                }
            }
        }

        dataAkunHandler()
        dataAlamatHandler()

        with(binding) {
            appBar.setNavigationOnClickListener { finish() }
            btnEditFoto.setOnClickListener { pickImage() }
            btnGotoAlamat.setOnClickListener { startActivity(Intent(this@AkunSayaActivity, AlamatActivity::class.java)) }

            btnEditNama.setOnClickListener {
                toggleEditView(namaEdit, namaView, btnEditNama, btnCancelNama, btnEditNohp)
                if (!namaEdit.isEnabled) {
                    if (isConnected(this@AkunSayaActivity)) if (namaUser != namaEdit.text.toString())
                        updateDataUser("nama", namaEdit, getString(R.string.nama), namaEdit.text.toString().trim())
                } else focusRequest(namaEdit)
            }
            btnCancelNama.setOnClickListener { toggleEditView(namaEdit, namaView, btnEditNama, btnCancelNama, btnEditNohp) }
            namaEdit.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    toggleEditView(namaEdit, namaView, btnEditNama, btnCancelNama, btnEditNohp)
                    if (!namaEdit.isEnabled)
                        if (isConnected(this@AkunSayaActivity)) if (namaUser != namaEdit.text.toString())
                            updateDataUser("nama", namaEdit, getString(R.string.nama), namaEdit.text.toString().trim())

                    true
                } else false
            }

            btnEditNohp.setOnClickListener {
                toggleEditView(nohpEdit, nohpView, btnEditNohp, btnCancelNohp, btnEditNama)
                if (!nohpEdit.isEnabled) {
                    if (isConnected(this@AkunSayaActivity)) if (nohpUser != nohpEdit.text.toString())
                        updateDataUser("noHp", nohpEdit, getString(R.string.nomor_telepon), nohpEdit.text.toString().trim())
                } else focusRequest(nohpEdit)
            }
            btnCancelNohp.setOnClickListener { toggleEditView(nohpEdit, nohpView, btnEditNohp, btnCancelNohp, btnEditNama) }
            nohpEdit.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    toggleEditView(nohpEdit, nohpView, btnEditNohp, btnCancelNohp, btnEditNama)
                    if (!nohpEdit.isEnabled)
                        if (isConnected(this@AkunSayaActivity)) if (nohpUser != nohpEdit.text.toString())
                            updateDataUser("noHp", nohpEdit, getString(R.string.nomor_telepon), nohpEdit.text.toString().trim())

                    true
                } else false
            }
        }
    }

    private fun pickImage() {
        ImagePicker.with(this).galleryOnly().compress(1024)
            .cropSquare().maxResultSize(1080, 1080)
            .createIntent { intent ->
                pickImageLauncher.launch(intent)
            }
    }

    private fun uploadImgtoDb(selectedImageUri: Uri) {
        userUpdateViewModel.updateImageUri(selectedImageUri) { isSuccessful ->
            if (isSuccessful) showToast(getString(R.string.berhasil_diperbarui, "Foto profil"), this@AkunSayaActivity)
            else showToast(getString(R.string.masalah_database), this@AkunSayaActivity)
        }
    }

    private fun dataAkunHandler() {
        userViewModel.getDataAkun().observe(this) { akun ->
            if (akun != null) {
                namaUser = akun.nama.toString()
                nohpUser = akun.noHp.toString()

                binding.namaView.text = if (!akun.nama.isNullOrEmpty()) akun.nama.toString() else getString(R.string.tidak_ada_data)
                binding.nohpView.text = if (!akun.noHp.isNullOrEmpty()) akun.noHp.toString() else getString(R.string.tidak_ada_data)
                binding.emailView.text = if (!akun.email.isNullOrEmpty()) akun.email.toString() else getString(R.string.tidak_ada_data)

                binding.namaEdit.setText(akun.nama.toString())
                binding.nohpEdit.setText(akun.noHp.toString())

                if (!isDestroyed) {
                    val fotopp = if (akun.fotoProfil.isNullOrEmpty()) null else akun.fotoProfil
                    Glide.with(this@AkunSayaActivity).load(fotopp)
                        .apply(RequestOptions()).centerCrop()
                        .placeholder(R.drawable.img_placeholder_profil)
                        .fallback(R.drawable.img_fallback_profil)
                        .error(R.drawable.img_error_profil)
                        .into(binding.fotoProfil)
                }
            } else finish()
        }
        userViewModel.isLoading().observe(this) { isLoading ->
            binding.btnEditFoto.isEnabled = !isLoading
            binding.btnEditNama.isEnabled = !isLoading
            binding.btnEditNohp.isEnabled = !isLoading

            loadingTextColor(binding.namaView, isLoading)
            loadingTextColor(binding.nohpView, isLoading)
            loadingTextColor(binding.emailView, isLoading)

            if (isLoading) {
                binding.namaView.text = getString(R.string.loading)
                binding.nohpView.text = getString(R.string.loading)
                binding.emailView.text = getString(R.string.loading)
            }
        }
    }

    private fun dataAlamatHandler() {
        alamatViewModel.getDataAlamat().observe(this) { alamat ->
            val alamatLengkap = alamat?.alamatLengkap ?: ""
            val kodePos = alamat?.kodePos ?: ""

            binding.alamatView.text = alamat?.let {
                when {
                    alamatLengkap.isNotEmpty() && kodePos.isNotEmpty() -> "$alamatLengkap, $kodePos"
                    alamatLengkap.isNotEmpty() -> alamatLengkap
                    kodePos.isNotEmpty() -> kodePos
                    else -> getString(R.string.tidak_ada_data)
                }
            } ?: getString(R.string.tidak_ada_data)
        }
        alamatViewModel.isLoading().observe(this) { isLoading ->
            loadingTextColor(binding.alamatView, isLoading)
            if (isLoading) binding.alamatView.text = getString(R.string.loading)
        }
    }

    private fun loadingTextColor(textView: TextView, isLoading: Boolean) {
        val colorLoading = ContextCompat.getColor(this, R.color.outline_variant)
        val colorReady = ContextCompat.getColor(this, R.color.grey_700)
        val setColor = if (isLoading) colorLoading else colorReady

        textView.setTextColor(setColor)
    }

    private fun toggleEditView(
        editText: EditText,
        textView: TextView,
        editButton: MaterialButton,
        cancelButton: MaterialButton,
        disableButton: MaterialButton
    ) {
        editText.isEnabled = !editText.isEnabled
        editText.isVisible = editText.isEnabled
        textView.isVisible = !editText.isEnabled
        editButton.setIconResource(if (editText.isEnabled) R.drawable.icon_round_done_24 else R.drawable.icon_round_mode_edit_24)
        cancelButton.isVisible = editText.isEnabled
        disableButton.isEnabled = !editText.isEnabled
    }

    private fun focusRequest(editText: EditText) {
        editText.requestFocus()

        editText.setSelection(editText.text.length)
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun updateDataUser(pathDb: String, editText: EditText, string: String, value: String) {
        if (editText.text.isNotEmpty()) {
            when {
                pathDb == "nama" && editText.text.length > 30 -> showToast(getString(R.string.terlalu_panjang, string), this)
                pathDb == "noHp" && editText.text.length < 9 -> showToast(getString(R.string.terlalu_singkat, string), this)
                else -> userUpdateViewModel.updateDataAkun(pathDb, value) { isSuccessful ->
                    if (isSuccessful) showToast(getString(R.string.berhasil_diperbarui, string), this)
                    else showToast(getString(R.string.gagal_memperbarui_x, string), this)
                }
            }
        } else showToast(getString(R.string.tidak_dapat_kosong, string), this)
    }
}