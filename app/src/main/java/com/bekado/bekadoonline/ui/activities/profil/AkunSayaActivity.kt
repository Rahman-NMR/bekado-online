package com.bekado.bekadoonline.ui.activities.profil

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bekado.bekadoonline.R
import com.bekado.bekadoonline.databinding.ActivityAkunSayaBinding
import com.bekado.bekadoonline.helper.Helper.showToast
import com.bekado.bekadoonline.helper.HelperConnection.isConnected
import com.bekado.bekadoonline.data.viewmodel.AkunViewModel
import com.bekado.bekadoonline.data.viewmodel.AlamatViewModel
import com.bekado.bekadoonline.ui.ViewModelFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AkunSayaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAkunSayaBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    private lateinit var akunRef: DatabaseReference
    private lateinit var alamatRef: DatabaseReference
    private val akunViewModel: AkunViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private lateinit var alamatViewModel: AlamatViewModel

    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private var imageUri: Uri = Uri.parse("")

    private var namaUser: String = ""
    private var nohpUser: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAkunSayaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        val currentUser = auth.currentUser

//        akunViewModel = ViewModelProvider(this)[AkunViewModel::class.java]
        alamatViewModel = ViewModelProvider(this)[AlamatViewModel::class.java]
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val selectedImageUri: Uri? = data?.data
                if (selectedImageUri != null) {
                    imageUri = selectedImageUri
                    uploadImgtoDb(selectedImageUri, currentUser?.uid)
                }
            } else if (result.resultCode == ImagePicker.RESULT_ERROR) showToast(ImagePicker.getError(result.data), this)
        }

        dataAkunHandler()

        with(binding) {
            appBar.setNavigationOnClickListener { finish() }
            btnEditFoto.setOnClickListener { pickImage() }
            btnGotoAlamat.setOnClickListener { startActivity(Intent(this@AkunSayaActivity, AlamatActivity::class.java)) }

            btnEditNama.setOnClickListener {
                toggleEditView(namaEdit, namaView, btnEditNama, btnCancelNama, btnEditNohp)
                if (!namaEdit.isEnabled) {
                    if (isConnected(this@AkunSayaActivity)) if (namaUser != namaEdit.text.toString())
                        updateData(currentUser?.uid, "nama", namaEdit, getString(R.string.nama), namaEdit.text.toString().trim())
                } else focusRequest(namaEdit)
            }
            btnCancelNama.setOnClickListener { toggleEditView(namaEdit, namaView, btnEditNama, btnCancelNama, btnEditNohp) }
            namaEdit.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    toggleEditView(namaEdit, namaView, btnEditNama, btnCancelNama, btnEditNohp)
                    if (!namaEdit.isEnabled)
                        if (isConnected(this@AkunSayaActivity)) if (namaUser != namaEdit.text.toString())
                            updateData(currentUser?.uid, "nama", namaEdit, getString(R.string.nama), namaEdit.text.toString().trim())

                    true
                } else false
            }

            btnEditNohp.setOnClickListener {
                toggleEditView(nohpEdit, nohpView, btnEditNohp, btnCancelNohp, btnEditNama)
                if (!nohpEdit.isEnabled) {
                    if (isConnected(this@AkunSayaActivity)) if (nohpUser != nohpEdit.text.toString())
                        updateData(currentUser?.uid, "noHp", nohpEdit, getString(R.string.nomor_telepon), nohpEdit.text.toString().trim())
                } else focusRequest(nohpEdit)
            }
            btnCancelNohp.setOnClickListener { toggleEditView(nohpEdit, nohpView, btnEditNohp, btnCancelNohp, btnEditNama) }
            nohpEdit.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    toggleEditView(nohpEdit, nohpView, btnEditNohp, btnCancelNohp, btnEditNama)
                    if (!nohpEdit.isEnabled)
                        if (isConnected(this@AkunSayaActivity)) if (nohpUser != nohpEdit.text.toString())
                            updateData(currentUser?.uid, "noHp", nohpEdit, getString(R.string.nomor_telepon), nohpEdit.text.toString().trim())

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

    private fun uploadImgtoDb(selectedImageUri: Uri, uid: String?) {
        val storageReference = storage.getReference("akun/$uid/$uid.png")

        storageReference.putFile(selectedImageUri).addOnSuccessListener {
            storageReference.downloadUrl.addOnCompleteListener { task ->
                val imgLink = task.result.toString()
                akunRef.child("fotoProfil").setValue(imgLink).addOnSuccessListener {
                    showToast("Foto profil ${getString(R.string.berhasil_diperbarui)}", this@AkunSayaActivity)
                }
            }
        }.addOnFailureListener { showToast(getString(R.string.masalah_database), this@AkunSayaActivity) }
    }

    private fun dataAkunHandler() {
        viewModelLoader()

        akunViewModel.currentUser.observe(this) { if (it == null) finish() }
        akunViewModel.akunModel.observe(this) { akunModel ->
            if (akunModel != null) {
                akunRef = db.getReference("akun/${akunModel.uid}")
                alamatRef = db.getReference("alamat/${akunModel.uid}")

                namaUser = akunModel.nama.toString()
                nohpUser = akunModel.noHp.toString()

                binding.namaView.text = akunModel.nama.toString()
                if (!akunModel.noHp.isNullOrEmpty()) binding.nohpView.text = akunModel.noHp.toString()
                else binding.nohpView.text = getString(R.string.tidak_ada_data)
                binding.emailView.text = akunModel.email.toString()
                if (!isDestroyed) Glide.with(this@AkunSayaActivity).load(akunModel.fotoProfil)
                    .apply(RequestOptions()).centerCrop()
                    .placeholder(R.drawable.img_broken_image_circle).into(binding.fotoProfil)

                binding.namaEdit.setText(akunModel.nama.toString())
                binding.nohpEdit.setText(akunModel.noHp.toString())
            } else {
                akunRef = db.getReference("akun")
                alamatRef = db.getReference("alamat")
            }
        }
        akunViewModel.isLoading.observe(this) { isLoading ->
            binding.btnEditFoto.isEnabled = !isLoading
            binding.btnEditNama.isEnabled = !isLoading
            binding.btnEditNohp.isEnabled = !isLoading

            loadingTextColor(binding.namaView, isLoading)
            loadingTextColor(binding.nohpView, isLoading)
            loadingTextColor(binding.emailView, isLoading)

            if (isLoading) {
                loadingDisplayText(binding.namaView)
                loadingDisplayText(binding.nohpView)
                loadingDisplayText(binding.emailView)
            }
        }
        alamatViewModel.alamatModel.observe(this) { alamatModel ->
            if (alamatModel != null) {
                if (!alamatModel.alamatLengkap.isNullOrEmpty() && !alamatModel.kodePos.isNullOrEmpty()) {
                    val alamatn = "${alamatModel.alamatLengkap}, ${alamatModel.kodePos}"
                    binding.alamatView.text = alamatn
                } else binding.alamatView.text = getString(R.string.tidak_ada_data)
            } else binding.alamatView.text = getString(R.string.tidak_ada_data)
        }
        alamatViewModel.isLoading.observe(this) { isLoading ->
            loadingTextColor(binding.alamatView, isLoading)
            if (isLoading) loadingDisplayText(binding.alamatView)
        }
    }

    private fun loadingDisplayText(textView: TextView) {
        val txtLoading = "Loading..."
        textView.text = txtLoading
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
        editText.visibility = if (editText.isEnabled) View.VISIBLE else View.GONE
        textView.visibility = if (!editText.isEnabled) View.VISIBLE else View.GONE
        editButton.setIconResource(if (editText.isEnabled) R.drawable.icon_round_done_24 else R.drawable.icon_round_mode_edit_24)
        cancelButton.visibility = if (editText.isEnabled) View.VISIBLE else View.GONE
        disableButton.isEnabled = !editText.isEnabled
    }

    private fun focusRequest(editText: EditText) {
        editText.requestFocus()

        editText.setSelection(editText.text.length)
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun updateData(uid: String?, pathDb: String, editText: EditText, string: String, value: String) {
        if (editText.text.isNotEmpty()) {
            if (pathDb == "nama" && editText.text.length > 30) showToast("$string ${getString(R.string.terlalu_panjang)}", this)
            else if (pathDb == "noHp" && editText.text.length < 9) showToast("$string ${getString(R.string.terlalu_singkat)}", this)
            else db.getReference("akun/$uid/$pathDb").setValue(value)
                .addOnSuccessListener { showToast("$string ${getString(R.string.berhasil_mengubah)}", this) }
                .addOnFailureListener { showToast("$string ${getString(R.string.gagal_mengubah)}", this) }
        } else showToast("$string ${getString(R.string.tidak_dapat_kosong)}", this)
    }

    private fun viewModelLoader() {
        akunViewModel.loadCurrentUser()
        akunViewModel.loadAkunData()
        alamatViewModel.loadCurrentUser()
        alamatViewModel.loadAlamatData()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (auth.currentUser != null) {
            alamatViewModel.removeAlamatListener(alamatRef)
        }
    }
}